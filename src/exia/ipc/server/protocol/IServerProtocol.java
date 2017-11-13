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
	 * @return La réponse à renvoyer au client, ou NULL si aucune réponse.
	 */
	public String processClientInput(ClientSocketThread client, String inputLine);

	/**
	 * Renvoie le message à envoyer aux clients quand on souhaite diffuser un message provenant d'un client.
	 * 
	 * @param socket Le client qui a envoyé le message.
	 * @param log Le message.
	 * @return Le message à propager à tous les clients.
	 */
	public String sendMessage(ClientSocketThread socket, String log);

	/**
	 * Renvoie le message à envoyer aux clients quand un client vient de se connecter.
	 * 
	 * @param clientThread Le client qui vient de se connecter.
	 * @return Le message à propager à tous les clients.
	 */
	public String sendClientConnected(ClientSocketThread clientThread);

	/**
	 * Renvoie le message à envoyer aux clients quand un client vient de se déconnecter.
	 * 
	 * @param clientThread Le client qui vient de se déconnecter.
	 * @return Le message à propager à tous les clients.
	 */
	public String sendClientDisconnected(ClientSocketThread socket);

}
