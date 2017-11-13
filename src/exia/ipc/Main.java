package exia.ipc;

import java.io.IOException;

import javax.swing.JOptionPane;

import exia.ipc.client.ChatClient;
import exia.ipc.server.ChatServer;

/**
 * Prosit Communication inter-processus et multithreading.
 * 
 * @author R�mi BELLO <rbello@cesi.fr>
 * @version 1.0
 * @see https://github.com/rbello/ExiaPrositIPC
 */
public class Main {

	/**
	 * Point d'entr�e du programme.
	 */
	public static void main(String[] args) {

		// On d�marre le serveur
		ChatServer server = startServer(500);

		// On d�marre plusieurs clients, qu'on va connecter au serveur
		startClient("127.0.0.1", 500, "alex");
		startClient("127.0.0.1", 500, "bob");

		// On �coute les entr�es clavier pour stopper le serveur 
		waitForExitRequest(server);

	}

	/**
	 * D�marrer un serveur sur le port donn�.
	 */
	public static ChatServer startServer(int portNumber) {
		try {
			// On lance un serveur sur le port donn�
			ChatServer server = new ChatServer(portNumber);
			server.start();
			return server;
		}
		catch (Throwable ex) {
			// En cas d'erreur au lancement
			String msg = String.format("Impossible de lancer le serveur. Le port %s est peut-�tre d�j� utilis�.\n%s - %s", 
					portNumber, ex.getClass().getSimpleName(), ex.getMessage());
			JOptionPane.showMessageDialog(null, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/**
	 * D�marrer un client, et le connecte � un serveur.
	 * 
	 * @param hostName Nom du serveur � connecter.
	 * @param hostPortNumber Port du serveur � connecter.
	 * @param userName Nom d'utilisateur sur le chat.
	 */
	public static void startClient(final String hostName, final int hostPortNumber, String userName) {
		// On fabrique un client
		final ChatClient client = new ChatClient(userName);
		// On le d�marre
		client.start(new Runnable() {
			// Quand la cr�ation est termin�e, on connecte le client au serveur
			public void run() {
				client.connect(hostName, hostPortNumber);
			}
		});
	}

	/**
	 * Ecoute l'entr�e standard (System.in) pour fermer le serveur. 
	 * 
	 * @param server L'instance du serveur � interrompre.
	 */
	private static void waitForExitRequest(ChatServer server) {
		System.out.println("Appuyez sur ENTREE pour arr�ter le serveur.");
		try {
			System.in.read();
		}
		catch (IOException e) {}
		System.out.println("Extinction du serveur...");
		server.interrupt();
	}

}
