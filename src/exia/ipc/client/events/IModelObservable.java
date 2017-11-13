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
	 * Lancer un �v�nement quand un message est re�u.
	 */
	public void notifyListeners(Log log);
	
	/**
	 * Lancer un �v�nement quand un utilisateur vient d'�tre connect� ou d�connect�.
	 */
	public void notifyListeners(User user, boolean connected);
	
}
