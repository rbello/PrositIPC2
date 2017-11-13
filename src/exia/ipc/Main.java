package exia.ipc;

import java.io.IOException;

import javax.swing.JOptionPane;

import exia.ipc.client.ChatClient;
import exia.ipc.server.ChatServer;

/**
 * Prosit Communication inter-processus et multithreading.
 * 
 * @author Rémi BELLO <rbello@cesi.fr>
 * @version 1.0
 * @see https://github.com/rbello/ExiaPrositIPC
 */
public class Main {

	/**
	 * Point d'entrée du programme.
	 */
	public static void main(String[] args) {

		// On démarre le serveur
		ChatServer server = startServer(500);

		// On démarre plusieurs clients, qu'on va connecter au serveur
		startClient("127.0.0.1", 500, "alex");
		startClient("127.0.0.1", 500, "bob");

		// On écoute les entrées clavier pour stopper le serveur 
		waitForExitRequest(server);

	}

	/**
	 * Démarrer un serveur sur le port donné.
	 */
	public static ChatServer startServer(int portNumber) {
		try {
			// On lance un serveur sur le port donné
			ChatServer server = new ChatServer(portNumber);
			server.start();
			return server;
		}
		catch (Throwable ex) {
			// En cas d'erreur au lancement
			String msg = String.format("Impossible de lancer le serveur. Le port %s est peut-être déjà utilisé.\n%s - %s", 
					portNumber, ex.getClass().getSimpleName(), ex.getMessage());
			JOptionPane.showMessageDialog(null, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
			return null;
		}
	}

	/**
	 * Démarrer un client, et le connecte à un serveur.
	 * 
	 * @param hostName Nom du serveur à connecter.
	 * @param hostPortNumber Port du serveur à connecter.
	 * @param userName Nom d'utilisateur sur le chat.
	 */
	public static void startClient(final String hostName, final int hostPortNumber, String userName) {
		// On fabrique un client
		final ChatClient client = new ChatClient(userName);
		// On le démarre
		client.start(new Runnable() {
			// Quand la création est terminée, on connecte le client au serveur
			public void run() {
				client.connect(hostName, hostPortNumber);
			}
		});
	}

	/**
	 * Ecoute l'entrée standard (System.in) pour fermer le serveur. 
	 * 
	 * @param server L'instance du serveur à interrompre.
	 */
	private static void waitForExitRequest(ChatServer server) {
		System.out.println("Appuyez sur ENTREE pour arrêter le serveur.");
		try {
			System.in.read();
		}
		catch (IOException e) {}
		System.out.println("Extinction du serveur...");
		server.interrupt();
	}

}
