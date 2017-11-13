package exia.ipc.server.events;

import exia.ipc.server.ClientSocketThread;

/**
 * Interface pour les observateurs des threads liés aux socket des clients.
 */
public interface IClientThreadObserver {

	/**
	 * Quand un message a été reçu par un client.
	 * 
	 * @param socket Le socket+thread du client.
	 * @param log Le message.
	 */
	public void onMessage(ClientSocketThread socket, String log);

	/**
	 * Quand un client a fermé son socket.
	 * 
	 * @param socket Le client déconnecté.
	 */
	public void onDisconnect(ClientSocketThread socket);
	
}
