package exia.ipc.server.protocol;

import java.util.Date;

import exia.ipc.server.ChatServer;
import exia.ipc.server.ClientSocketThread;
import exia.ipc.server.LogWriter;

public class ChatServerProtocol implements IServerProtocol {
	
	private ChatServer server;
	
	@Override
	public void setServer(ChatServer server) {
		this.server = server;
	}

	@Override
	public String processClientInput(ClientSocketThread client, String inputLine) {
		
		// Debug
		// System.out.println("Le client " + client + " a envoyé: " + inputLine);
		
		// On explose la chaîne avec les espaces. On cherche à isoler le
		// premier mot (qui indique la commande) et le reste de la ligne.
		String[] tokens = inputLine.trim().split("\\s+", 2);
		
		// Message vide
		if (tokens.length < 2) {
			LogWriter.getInstance().writeError("Message invalide de " + client + " : " + inputLine);
			return null;
		}
		
		// On évalue le type de fonction
		switch (tokens[0].toUpperCase()) {
		
		// Dans le cas où le client donne pour la première fois son nom, en début
		// de communication.
		case "HELLO" :
			// On associe le nom au socket
			client.setUserName(tokens[1]);
			// On propage l'information qu'un client est connecté
			server.broadcast(sendClientConnected(client));
			break;
		
		// Dans le cas où le client envoie un message
		case "MSG" :
			// On broadcast à tous les clients
			client.notifyMessageReceived(client, tokens[1]);
			break;
			
		default :
			LogWriter.getInstance().writeError("Commande invalide " + tokens[0] + " envoyé par " + client);
			break;
			
		}
		
		return null;
	}

	@Override
	public String sendMessage(ClientSocketThread from, String log) {
		return String.format("MSG %s %s %s %s", from.getUserName(), from.getAddress(), new Date().getTime(), log);
	}

	@Override
	public String sendClientConnected(ClientSocketThread client) {
		return String.format("CONNECTED %s %s", client.getUserName(), client.getAddress());
	}

	@Override
	public String sendClientDisconnected(ClientSocketThread client) {
		return String.format("DISCONNECTED %s %s", client.getUserName(), client.getAddress());
	}

}
