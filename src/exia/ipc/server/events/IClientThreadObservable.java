package exia.ipc.server.events;

import exia.ipc.server.ClientSocketThread;

/**
 * Interface pour d�finir les m�thodes li�s aux �v�nements sur un thread connect� � un socket client.
 * 
 * Ce n'est pas vraiment une impl�mentation correcte du DP Observer, car il n'est pas possible de
 * mettre plusieurs observateurs. Mais cela pourrait �tre simplement ajout� dans le futur, maintenant
 * qu'on a bien isol� les m�thodes.
 */
public interface IClientThreadObservable {
	
	/**
	 * Modifier l'observateur unique.
	 */
	public void setObserver(IClientThreadObserver observer);
	
	/**
	 * Quand un client a envoy� un message.
	 */
	public void notifyMessageReceived(ClientSocketThread client, String log);
	
	/**
	 * Quand un client vient de se d�connecter.
	 */
	public void notifyClientDisconnected(ClientSocketThread client);

}
