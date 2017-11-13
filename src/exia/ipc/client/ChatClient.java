package exia.ipc.client;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import exia.ipc.client.cypher.ClearText;
import exia.ipc.client.cypher.ICypher;
import exia.ipc.client.ihm.Controller;
import exia.ipc.client.ihm.Model;
import exia.ipc.client.ihm.View;
import exia.ipc.client.protocol.ChatClientProtocol;
import exia.ipc.client.protocol.IClientProtocol;

/**
 * Le client de chat privé.
 * Se démarre avec la méthode start().
 */
public class ChatClient implements Runnable {
	
	/**
	 * Le modèle de données.
	 */
	private Model model;
	
	/**
	 * Le controleur de la vue.
	 */
	private Controller ctrl;
	
	/**
	 * Le socket ouvert vers le serveur.
	 */
	private Socket socket;
	
	/**
	 * Le flux d'écriture sur le socket.
	 */
	private PrintWriter outStream;
	
	/**
	 * Le flux de lecture sur le socket.
	 */
	private BufferedReader inStream;
	
	/**
	 * Le thread qui écoute le socket. Le ChatClient est le runnable du thread.
	 */
	private Thread thread;
	
	/**
	 * Le protocole de communication.
	 * Design pattern strategy.
	 */
	private IClientProtocol protocol;
	
	/**
	 * L'algorithme de chiffrement.
	 * Design pattern strategy.
	 */
	private ICypher cypher;

	/**
	 * Constructeur.
	 */
	public ChatClient() {
		// On utilise le nom de la session
		this(System.getProperty("user.name"));
	}
	
	/**
	 * Constructeur.
	 * 
	 * @param userName Nom d'utilisateur à afficher dans le chat.
	 */
	public ChatClient(String userName) {
		
		// On fabrique le modèle de l'application
		this.model = new Model(userName);
		
		// On conserve l'objet d'encodage/décodage
		this.cypher = new ClearText();
		
		// On fabrique un protocole par défaut.
		this.protocol = new ChatClientProtocol();
		this.protocol.setClient(this);
		
	}
	
	/**
	 * Lance le client, ce qui va afficher la fenêtre.
	 */
	public void start() {
		start(null);
	}
	
	/**
	 * Lance le client. Cette méthode va provoquer l'affichage de la fenêtre.
	 * Une callback peut être passée : elle sera appelée quand la fenêtre
	 * sera construite. La callback est executée dans le thread EDT.
	 * 
	 * @param callback Une callback de fin d'execution.
	 */
	public void start(final Runnable callback) {

		// On fabrique l'IHM dans le thread EDT
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				
				// On fabrique la vue.
				// La notation "ChatClient.this" permet de récupérer le "this" de la
				// classe ChatClient, car sinon il désignerait l'instance du Runnable.
				// Demandez à votre pilote si vous ne comprenez pas :-)
				ChatClient.this.createView();
				
				// On lance le controleur, qui va afficher la vue
				ChatClient.this.getController().run();
				
				// Et on appelle la callback
				if (callback != null) {
					callback.run();
				}
				
			}
		});
		
	}

	protected void createView() {
		
		// On fabrique la vue
		View<ICypher> view = new View<ICypher>();
		
		// Et le controleur
		ctrl = new Controller(view, model, this);
		
	}

	/**
	 * Demande la connexion 
	 * 
	 * @param hostName
	 * @param portNumber
	 */
	public void connect(final String hostName, final int portNumber) {
		
		// On s'assure que le lancement de cette méthode se fasse en dehors de l'EDT
		if (EventQueue.isDispatchThread()) {
			// On fabrique un nouveau thread, et on rééxecute la méthode dedans
			//new Thread(() -> connect(hostName, portNumber)).start();
			new Thread(new Runnable() {
				public void run() {
					connect(hostName, portNumber);
				}
			}).start();
			// On arrête là ce traitement
			return;
		}
		
		// On est déjà connecté
		if (isConnected()) {
			getView().appendOutput("Vous êtes déjà connecté!");
			return;
		}
		
		try {
			
			// On ouvre un socket vers le serveur
			socket = new Socket(hostName, portNumber);
			
			// On ouvre les flux d'entrées/sortie
			outStream = new PrintWriter(socket.getOutputStream(), true);
			inStream  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			// On lance un thread pour lire le socket
			thread = new Thread(this);
			thread.start();

			// Information pour l'utilisateur
			getView().appendOutput(String.format("Connecté à %s:%s", hostName, portNumber));
			
			// On envoie le nom choisi par le client au serveur
			outStream.println(protocol.sayHelloToServer(model.getCurrentUser().getName()));
			
		}
		
		// En cas d'erreur
		catch (Exception e) {
			
			// On affiche une erreur
			displayMessage("Erreur : connexion impossible, " + e.getClass().getSimpleName() + " - " + e.getMessage());
			
			// Si le socket a été ouvert, on tente de le fermer
			if (socket != null) {
				try {
					socket.close();
				} catch (Exception e1) {
					// Cette fois on ne fait rien si cela n'a pas fonctionné
				}
			}
			
			// On reset les variables de connexion
			socket = null;
			outStream = null;
			inStream = null;
		}
		
	}

	public void displayMessage(String msg) {
		// On affiche le texte dans la console
		ctrl.getView().appendOutput(msg);
	}

	@Override
	public void run() {
		
		// On prépare la chaîne d'entrées
		String inputLine;
		
		try {
			// On lit le socket ligne par ligne
			while ((inputLine = inStream.readLine()) != null) {
				
				// On laisse le protocol traiter. Le message renvoyé doit être affiché en console.
				String msg = protocol.processServerInput(inputLine);
				
				// Affichage dans la console
				if (msg != null) {
					displayMessage(msg);
				}
				
				// Gestion de interruptions de thread
				if (thread.isInterrupted()) return;
				
			}
		}
		// Erreur de lecture/écriture
		catch (IOException e) {
			displayMessage("Erreur: lecture impossible sur le socket");
		}
		
		// Fin de connexion
		displayMessage("Déconnecté du serveur.");
			
	}

	public void stop() {
		
		// On coupe le thread de lecture du socket
		thread.interrupt();
		
		// On s'assure que toutes les données ont été envoyées au serveur
		outStream.flush();
		
		// On ferme le socket. Cela va prévenir le serveur.
		try {
			socket.close();
		}
		catch (IOException e) {
			// On n'a pas réussit à fermer le socket.
			// C'est difficile de faire quelque chose de plus à ce stade...
		}
		// Dans tous les cas on supprime les références
		finally {
			outStream = null;
			inStream = null;
			socket = null;
		}
		
	}

	public Socket getSocket() {
		return socket;
	}

	public PrintWriter getOutputStream() {
		return outStream;
	}

	public BufferedReader getInputStream() {
		return inStream;
	}

	public IClientProtocol getProtocol() {
		return protocol;
	}

	public ICypher getCypher() {
		return cypher;
	}

	public void setCypher(ICypher cypher) {
		this.cypher = cypher;
	}

	public View<ICypher> getView() {
		return this.ctrl.getView();
	}
	
	public Model getModel() {
		return this.model;
	}
	
	public Controller getController() {
		return this.ctrl;
	}
	
	public boolean isConnected() {
		return socket != null && socket.isConnected();
	}

}
