package exia.ipc.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import exia.ipc.server.events.IClientThreadObservable;
import exia.ipc.server.events.IClientThreadObserver;
import exia.ipc.server.protocol.IServerProtocol;

/**
 * Le thread qui va gérer l'interconnexion avec un client. Le thread est actif durant toute la durée
 * de vie du socket.
 */
public class ClientSocketThread extends Thread implements IClientThreadObservable {

	/**
	 * Le socket connecté au client.
	 */
	private Socket clientSocket;
	
	/**
	 * Le flux pour écrire sur le socket.
	 */
	private PrintWriter outStream;
	
	/**
	 * Le flux pour lire le socket.
	 */
	private BufferedReader inStream;
	
	/**
	 * Le protocole de dialogue.
	 */
	private IServerProtocol protocol;

	/**
	 * Le nom de l'utilisateur utilisant le client. Par défaut Anonymous.
	 */
	private String userName = "Anonymous";

	/**
	 * L'observateur de ce client.
	 */
	private IClientThreadObserver observer;

	/**
	 * Constructeur.
	 * 
	 * @param clientSocket Le socket connecté au client.
	 * @param observer L'observer, qui sera notifié des événements du socket.
	 * @param protocol Le protocole de communication.
	 * @throws IOException Erreur d'ouverture de flux entrée ou sortie.
	 */
	public ClientSocketThread(Socket clientSocket, IClientThreadObserver observer, IServerProtocol protocol) throws IOException {
		
		// On donne un nom unique au thread
		super("ClientSocket-" + clientSocket.getPort());
		
		// On enregistre le socket du client, l'observer et le protocole de communication
		this.clientSocket = clientSocket;
		this.observer = observer;
		this.protocol = protocol;
		
		// On ouvre les flux d'entrées/sorties
		this.outStream = new PrintWriter(clientSocket.getOutputStream(), true);
		this.inStream  = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		
	}
	
	/**
	 * Affiche le thread sous forme de texte.
	 */
	@Override
	public String toString() {
		return getUserName() + "@" + getAddress();
	}

	public void setUserName(String userName) {
		// On vient d'avoir le nom du client
		if ("Anonymous".equals(this.userName)) {
			LogWriter.getInstance().writeLog("Nouvelle connexion de " + getAddress() + " (" + userName + ")");
		}
		this.userName = userName;
		
	}
	
	public String getUserName() {
		return userName;
	}

	public String getAddress() {
		if (clientSocket == null || clientSocket.isClosed()) return "null";
		return clientSocket.getInetAddress().getHostAddress();
	}

	@Override
	public void run() {
		
		// On prépare des chaînes d'entrées/sorties
		String inputLine, outputLine;
		
		try {
			// On lit le socket ligne par ligne
			while ((inputLine = inStream.readLine()) != null) {
				
				// On laisse le protocol gérer le traitement
				outputLine = protocol.processClientInput(this, inputLine);
				
				// Si on a une réponse à envoyer
				if (outputLine != null) {
					write(outputLine);
				}
				
			}
		}
		// Erreur de lecture/écriture
		catch (IOException e) {
			LogWriter.getInstance().writeError(e.getClass().getSimpleName() + " while readding/writing socket " + toString()
				+ " : " + e.getMessage());
		}
		
		// Il n'y a plus de données à lire, donc le client a fermé proprement sa connexion.
		// Si le socket est déjà fermé, c'est que c'est une interruption.
		if (clientSocket != null) {
			notifyClientDisconnected(this);
		}
		
	}
	
	public void write(String data) {
		outStream.println(data);
		outStream.flush();
	}

	@Override
	public void notifyMessageReceived(ClientSocketThread client, String log) {
		observer.onMessage(client, log);
	}
	
	@Override
	public void notifyClientDisconnected(ClientSocketThread client) {
		// On prévient l'observer
		observer.onDisconnect(client);
		// On libère les ressources
		close();
		// Et le thread va s'arrêter de lui même maintenant
	}

	private void close() {
		try {
			clientSocket.close();
			inStream.close();
			outStream.close();
		}
		catch (Exception ex) { }
		finally {
			clientSocket = null;
			inStream = null;
			outStream = null;
		}
	}

	@Override
	public void setObserver(IClientThreadObserver observer) {
		this.observer = observer;
	}

	@Override
	public void interrupt() {
		// On arrête le thread qui lit le socket
		super.interrupt();
		// On ferme le socket et les streams
		close();
	}
	
}
