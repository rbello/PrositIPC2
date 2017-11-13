package exia.ipc.server.events;

import exia.ipc.server.ClientSocketThread;

/**
 * Interface pour les observateurs des threads li�s aux socket des clients.
 */
public interface IClientThreadObserver {

	/**
	 * Quand un message a �t� re�u par un client.
	 * 
	 * @param socket Le socket+thread du client.
	 * @param log Le message.
	 */
	public void onMessage(ClientSocketThread socket, String log);

	/**
	 * Quand un client a ferm� son socket.
	 * 
	 * @param socket Le client d�connect�.
	 */
	public void onDisconnect(ClientSocketThread socket);
	
}
