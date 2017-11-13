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
 * Le thread qui va g�rer l'interconnexion avec un client. Le thread est actif durant toute la dur�e
 * de vie du socket.
 */
public class ClientSocketThread extends Thread implements IClientThreadObservable {

	/**
	 * Le socket connect� au client.
	 */
	private Socket clientSocket;
	
	/**
	 * Le flux pour �crire sur le socket.
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
	 * Le nom de l'utilisateur utilisant le client. Par d�faut Anonymous.
	 */
	private String userName = "Anonymous";

	/**
	 * L'observateur de ce client.
	 */
	private IClientThreadObserver observer;

	/**
	 * Constructeur.
	 * 
	 * @param clientSocket Le socket connect� au client.
	 * @param observer L'observer, qui sera notifi� des �v�nements du socket.
	 * @param protocol Le protocole de communication.
	 * @throws IOException Erreur d'ouverture de flux entr�e ou sortie.
	 */
	public ClientSocketThread(Socket clientSocket, IClientThreadObserver observer, IServerProtocol protocol) throws IOException {
		
		// On donne un nom unique au thread
		super("ClientSocket-" + clientSocket.getPort());
		
		// On enregistre le socket du client, l'observer et le protocole de communication
		this.clientSocket = clientSocket;
		this.observer = observer;
		this.protocol = protocol;
		
		// On ouvre les flux d'entr�es/sorties
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
		
		// On pr�pare des cha�nes d'entr�es/sorties
		String inputLine, outputLine;
		
		try {
			// On lit le socket ligne par ligne
			while ((inputLine = inStream.readLine()) != null) {
				
				// On laisse le protocol g�rer le traitement
				outputLine = protocol.processClientInput(this, inputLine);
				
				// Si on a une r�ponse � envoyer
				if (outputLine != null) {
					write(outputLine);
				}
				
			}
		}
		// Erreur de lecture/�criture
		catch (IOException e) {
			LogWriter.getInstance().writeError(e.getClass().getSimpleName() + " while readding/writing socket " + toString()
				+ " : " + e.getMessage());
		}
		
		// Il n'y a plus de donn�es � lire, donc le client a ferm� proprement sa connexion.
		// Si le socket est d�j� ferm�, c'est que c'est une interruption.
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
		// On pr�vient l'observer
		observer.onDisconnect(client);
		// On lib�re les ressources
		close();
		// Et le thread va s'arr�ter de lui m�me maintenant
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
		// On arr�te le thread qui lit le socket
		super.interrupt();
		// On ferme le socket et les streams
		close();
	}
	
}
