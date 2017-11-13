package exia.ipc.client.ihm;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;

import exia.ipc.client.ChatClient;
import exia.ipc.client.cypher.Base64Cypher;
import exia.ipc.client.cypher.ClearText;
import exia.ipc.client.cypher.ICypher;
import exia.ipc.client.entities.Log;
import exia.ipc.client.entities.User;
import exia.ipc.client.events.IModelObserver;

/**
 * Le controleur de la vue.
 */
public class Controller implements IModelObserver, Runnable {

	/**
	 * Référence vers la vue.
	 */
	private View<ICypher> view;
	
	/**
	 * Référence vers le modèle.
	 */
	private Model model;
	
	/**
	 * Référence vers la client de connexion.
	 */
	private ChatClient client;

	/**
	 * Constructeur.
	 * 
	 * @param view La vue.
	 * @param model Le modèle.
	 * @param client Le client de chat.
	 */
	public Controller(View<ICypher> view, Model model, ChatClient client) {
		this.view = view;
		this.model = model;
		this.client = client;
	}

	/**
	 * Démarrer le controleur. Doit être fait dans l'EDT.
	 */
	@Override
	public void run() {
		
		// Force le lancement dans l'EDT
		if (!EventQueue.isDispatchThread()) {
			EventQueue.invokeLater(this);
			return;
		}

		// On change le titre de la fenêtre.
		view.setTitle("Private Chat");
		
		// On modifie l'action par défaut quand on utilise le bouton FERMER.
		// On choisit de simplement fermer la fenêtre, alors que par défaut
		// cela coupe toute l'application.
		view.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		// On affiche le nom d'utilisateur dans la vue
		view.getUserNameLabel().setText(model.getCurrentUser().getName());
		
		// On enregistre les algos de chiffrement
		view.getCypherModel().addElement(new ClearText());
		view.getCypherModel().addElement(new Base64Cypher());
		
		// Le controller est observateur des changements dans le modèle
		model.addListener(this);
		
		// Ramène le focus sur le champ d'input quand on clic sur d'autres composants
		final MouseListener handler = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				view.getInputArea().requestFocus();
			}
		};
		view.getOutputArea().addMouseListener(handler);
		view.getUsersList().addMouseListener(handler);
		
		// Appui sur le bouton "Envoyer"
		view.getSendButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// On recupère le message à envoyer
				String log = view.getInputArea().getText().trim();
				// On reset le champ de saisie
				view.getInputArea().setText("");
				// On ne fait rien si la ligne est vide
				if (log.isEmpty()) return;
				// Si le client n'est pas connecté
				if (!client.isConnected()) {
					connect(log);
					return;
				}
				// On envoie le message au serveur
				else {
					client.getOutputStream().println(client.getProtocol().sendMessageToServer(log));
				}
			}
		});
		
		// Quand on appuie sur ENTREE dans le champ d'input
		view.getInputArea().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// On simule un clic sur le bouton Envoyer
				view.getSendButton().doClick();
			}
		});
		
		// Quand on change d'algorithme de chiffrement
		view.getCypherSelector().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.setCypher((ICypher) view.getCypherSelector().getSelectedItem());
			}
		});
		view.getCypherSelector().setSelectedItem(client.getCypher());
		
		// Quand la fenêtre se ferme, on arrête la connexion au serveur
		view.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				client.stop();
			}
		});
		
		// On affiche la fenêtre
		view.setVisible(true);
		
		// On met le focus par défaut sur le champ d'input
		view.getInputArea().requestFocus();
		
	}

	/**
	 * Connecter le client.
	 * 
	 * @param input La destination au format: host[:port]
	 */
	private void connect(String input) {
		String[] tokens = input.split(":", 2);
		String hostName = tokens[0].trim();
		int portNumber = tokens.length > 1 ? Integer.parseInt(tokens[1]) : 500;
		view.appendOutput(String.format("Connexion à %s:%s ...", hostName, portNumber));
		client.connect(hostName, portNumber);
	}

	/**
	 * Met à jour la vue quand un utilisateur se connecte ou se déconnecte.
	 */
	@Override
	public void onUserEvent(User user, boolean connected) {
		String fqn = user.getName() + "@" + user.getLocation();
		DefaultListModel<String> model = view.getUsersListModel();
		if (connected) {
			if (!model.contains(fqn)) view.getUsersListModel().addElement(fqn);
		}
		else {
			model.removeElement(fqn);
		}
	}
	
	/**
	 * Met à jour la vue quand un client a envoyé un message.
	 */
	@Override
	public void onLogReceived(Log log) {
		view.appendOutput(String.format("[%s] %s: %s", log.getDate(), log.getUser(), log.getMessage()));
	}

	public View<ICypher> getView() {
		return view;
	}

	public Model getModel() {
		return model;
	}

}
