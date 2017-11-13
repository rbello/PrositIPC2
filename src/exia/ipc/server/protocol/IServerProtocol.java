package exia.ipc.server.protocol;

import exia.ipc.server.ChatServer;
import exia.ipc.server.ClientSocketThread;

public interface IServerProtocol {

	/**
	 * Le protocole a besoin d'un serveur pour fonctionner.
	 * 
	 * @param server
	 */
	public void setServer(ChatServer server);
	
	/**
	 * Quand un client envoie une commande.
	 * 
	 * @param client
	 * @param inputLine
	 * @return La r�ponse � renvoyer au client, ou NULL si aucune r�ponse.
	 */
	public String processClientInput(ClientSocketThread client, String inputLine);

	/**
	 * Renvoie le message � envoyer aux clients quand on souhaite diffuser un message provenant d'un client.
	 * 
	 * @param socket Le client qui a envoy� le message.
	 * @param log Le message.
	 * @return Le message � propager � tous les clients.
	 */
	public String sendMessage(ClientSocketThread socket, String log);

	/**
	 * Renvoie le message � envoyer aux clients quand un client vient de se connecter.
	 * 
	 * @param clientThread Le client qui vient de se connecter.
	 * @return Le message � propager � tous les clients.
	 */
	public String sendClientConnected(ClientSocketThread clientThread);

	/**
	 * Renvoie le message � envoyer aux clients quand un client vient de se d�connecter.
	 * 
	 * @param clientThread Le client qui vient de se d�connecter.
	 * @return Le message � propager � tous les clients.
	 */
	public String sendClientDisconnected(ClientSocketThread socket);

}
