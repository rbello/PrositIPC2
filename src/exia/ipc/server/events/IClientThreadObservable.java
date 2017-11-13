package exia.ipc.server.events;

import exia.ipc.server.ClientSocketThread;

/**
 * Interface pour définir les méthodes liés aux événements sur un thread connecté à un socket client.
 * 
 * Ce n'est pas vraiment une implémentation correcte du DP Observer, car il n'est pas possible de
 * mettre plusieurs observateurs. Mais cela pourrait être simplement ajouté dans le futur, maintenant
 * qu'on a bien isolé les méthodes.
 */
public interface IClientThreadObservable {
	
	/**
	 * Modifier l'observateur unique.
	 */
	public void setObserver(IClientThreadObserver observer);
	
	/**
	 * Quand un client a envoyé un message.
	 */
	public void notifyMessageReceived(ClientSocketThread client, String log);
	
	/**
	 * Quand un client vient de se déconnecter.
	 */
	public void notifyClientDisconnected(ClientSocketThread client);

}
