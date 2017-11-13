package exia.ipc.client.protocol;

import exia.ipc.client.ChatClient;
import exia.ipc.client.entities.User;

public class ChatClientProtocol implements IClientProtocol {

	private ChatClient client;

	@Override
	public void setClient(ChatClient client) {
		this.client = client;
	}
	
	@Override
	public String sayHelloToServer(String clientName) {
		return "HELLO " + clientName.trim();
	}

	@Override
	public String sendMessageToServer(String log) {
		return "MSG " + client.getCypher().encode(log);
	}

	@Override
	public String processServerInput(String inputLine) {
		
		// Debug
		// System.out.println("Le serveur a envoyé: " + inputLine);
		
		// On explose la chaîne avec les espaces. On cherche à isoler le
		// premier mot (qui indique la commande) et le reste de la ligne.
		String[] tokens = inputLine.trim().split("\\s+", 2);
		
		// Message vide
		if (tokens.length < 2) {
			return "Erreur: message invalide du serveur";
		}
		
		// On évalue le type de fonction
		switch (tokens[0].toUpperCase()) {
		
		// Un message a été reçu
		case "MSG":
			tokens = tokens[1].trim().split("\\s+", 4);
			String msg = client.getCypher().decode(tokens[3]);
			return String.format("%s a dit: %s", tokens[0], msg);
			
		case "CONNECTED":
			tokens = tokens[1].trim().split("\\s+", 2);
			// On ajoute l'utilisateur dans la modèle
			client.getModel().addUser(new User(tokens[0], tokens[1]));
			return String.format("* %s vient de se connecter (%s)", tokens[0], tokens[1]);
			
		case "DISCONNECTED":
			tokens = tokens[1].trim().split("\\s+", 2);
			// On cherche l'utilisateur qui vient de se déconnecter
			User user = client.getModel().findUser(tokens[0], tokens[1]);
			// On retire l'utilisateur
			client.getModel().removeUser(user);
			return String.format("* %s vient de se déconnecter (%s)", tokens[0], tokens[1]);
			
		// Par défaut
		default :
			return "Commande invalide " + tokens[0] + " envoyée par le serveur";
		
		}
	}

}
