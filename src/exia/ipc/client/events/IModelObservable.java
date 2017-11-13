package exia.ipc.client.events;

import exia.ipc.client.entities.Log;
import exia.ipc.client.entities.User;

/**
 * Design pattern Observer.
 */
public interface IModelObservable {

	/**
	 * Ajouter un observateur.
	 */
	public void addListener(IModelObserver listener);
	
	/**
	 * Retirer un observateur.
	 */
	public void removeListener(IModelObserver listener);
	
	/**
	 * Lancer un événement quand un message est reçu.
	 */
	public void notifyListeners(Log log);
	
	/**
	 * Lancer un événement quand un utilisateur vient d'être connecté ou déconnecté.
	 */
	public void notifyListeners(User user, boolean connected);
	
}
