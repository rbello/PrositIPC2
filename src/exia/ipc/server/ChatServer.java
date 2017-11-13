package exia.ipc.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import exia.ipc.server.events.IClientThreadObserver;
import exia.ipc.server.protocol.ChatServerProtocol;
import exia.ipc.server.protocol.IServerProtocol;

/**
 * Un thread qui �coute sur un port donn�, et qui va cr�er des ClientSocketThread � chaque
 * nouvelle connexion.
 */
public class ChatServer extends Thread implements IClientThreadObserver {

	/**
	 * Le port d'�coute.
	 */
	private int portNumber;
	
	/**
	 * Le socket du serveur.
	 */
	private ServerSocket serverSocket;

	/**
	 * Le pool des threads des clients.
	 */
	private ExecutorService threadPool;
	
	/**
	 * La liste des clients connect�s.
	 */
	private List<ClientSocketThread> clientList;

	/**
	 * Le protocole de communication.
	 */
	private IServerProtocol protocol;

	/**
	 * Constructeur.
	 * 
	 * @param portNumber Le port d'�coute.
	 * @param protocol Le protocole de communication.
	 */
	public ChatServer(int portNumber) {
		this(portNumber, new ChatServerProtocol());
	}
	
	/**
	 * Constructeur.
	 * 
	 * @param portNumber Le port d'�coute.
	 * @param protocol Le protocole de communication.
	 */
	public ChatServer(int portNumber, IServerProtocol protocol) {
		
		// On donne un nom au thread du serveur
		super("ChatServer");
		
		// On enregistre le port
		this.portNumber = portNumber;
		
		// On enregistre le protocol, et on lui communique l'instance du serveur.
		// On utilise ici de la strat�gie, donc on pourra changer de protocol par la suite.
		this.protocol = protocol;
		this.protocol.setServer(this);
		
		// On fabrique le pool de thread correspondant aux clients
		this.threadPool = Executors.newFixedThreadPool(50);
		
		// On fabrique une liste vide pour les clients connect�s
		this.clientList = new ArrayList<>();
		
	}
	
	/**
	 * Lancer le thread.
	 */
	@Override
	public synchronized void start() {
		
		// On fabrique le socket serveur
		try {
			serverSocket = new ServerSocket(portNumber);
			serverSocket.setSoTimeout(1000);
		}
		catch (IOException e) {
			// En cas d'erreur, on l�ve une exception
			throw new RuntimeException(e);
		}
		
		// On appelle start() sur la classe parente, c-�-d Thread.
		super.start();
		
		// On log
		LogWriter.getInstance().writeLog("Serveur lanc�, �coute sur le port " + portNumber + "...");
		
	}
	
	/**
	 * Execution du thread.
	 */
	@Override
	public void run() {
		
		// Tant qu'on ne demande pas l'interruption du thread
		while (!Thread.interrupted()) {
			
			// On est � l'�coute d'un client
			try {
				
				// On a un client
				Socket clientSocket = serverSocket.accept();
				
				// On fabrique un thread pour maintenir le lien
				ClientSocketThread clientThread = new ClientSocketThread(clientSocket, this, protocol);
				
				// On conserve sa r�f�rence
				synchronized (clientList) {
					clientList.add(clientThread);	
				}
				
				// On execute ce thread dans la pool
				threadPool.execute(clientThread);
				
				// Si le client n'est pas seul dans le chat, on lui envoie la liste des clients
				// d�j� connect�s.
				if (clientList.size() > 1) {
					sendConnectedClients(clientThread);
				}
				
			}
			// Cette erreur n'en est pas une, c'est simplement que le SO_TIMEOUT
			// a �t� atteint. On ne fait rien de sp�cial.
			catch (SocketTimeoutException e) {
				
			}
			// En cas d'erreur plus importante, on va les logger
			catch (IOException e) {
				LogWriter.getInstance().writeError("accept() throws " + e.getClass().getSimpleName() + " : " + e.getMessage());
			}
			
		}
		
	}

	/**
	 * M�thode pour envoyer une donn�e � tous les clients connect�s.
	 * @param data
	 */
	public void broadcast(String data) {
		synchronized (clientList) {
			// On parcours l'ensemble des clients et on propage le message
			//clientList.forEach(client -> {
			//	client.write(data);
			//});
			for (ClientSocketThread client : clientList) {
				client.write(data);
			}
		}
	}
	
	/**
	 * M�thode pour envoyer � un nouveau client les noms de tous les clients d�j� connect�s.
	 * @param newClient
	 */
	public void sendConnectedClients(ClientSocketThread newClient) {
		synchronized (clientList) {
			//clientList.forEach(client -> {
			//	if (client != newClient) {
			//		newClient.write(protocol.sendClientConnected(client));
			//	}
			//});
			for (ClientSocketThread client : clientList) {
				if (client != newClient)
					newClient.write(protocol.sendClientConnected(client));
			}
		}
	}
	
	/**
	 * Quand un client a envoy� un message.
	 */
	@Override
	public void onMessage(ClientSocketThread socket, String log) {
		// On log
		LogWriter.getInstance().writeLog("Message de " + socket.getUserName() + " (" + socket.getAddress() + ")");
		// Et on propage � tous les clients
		broadcast(protocol.sendMessage(socket, log));
	}

	/**
	 * Quand un client vient de se deconnecter.
	 */
	@Override
	public void onDisconnect(ClientSocketThread socket) {
		
		// On log
		LogWriter.getInstance().writeLog("D�connexion de " + socket.getUserName() + " (" + socket.getAddress() + ")");
		
		// On retire le client d�connect�
		synchronized (clientList) {
			clientList.remove(socket);
		}
		
		// Et on propage � tous les clients
		broadcast(protocol.sendClientDisconnected(socket));
		
	}
	
	/**
	 * On surcharge cette m�thode pour fermer correctement le thread et les sockets ouverts.
	 */
	@Override
	public void interrupt() {
		
		// On arr�te le thread de traitement qui surveille le serversocket
		super.interrupt();
		
		// On arr�te toutes les connexions aux clients, et on vide la liste des clients
		synchronized (clientList) {
			//clientList.forEach(client -> client.interrupt());
			for (ClientSocketThread c : clientList) c.interrupt();
			clientList.clear();
		}
		
		// Et on arr�te la pool
		threadPool.shutdownNow();
		
		// On ferme le server socket
		try {
			serverSocket.close();
		}
		catch (IOException e) {
			// En cas d'erreur on ne fait rien de sp�cial
		}
		finally {
			// On lib�re la ressource
			serverSocket = null;
		}
		
	}
	
}
