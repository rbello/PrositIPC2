package exia.ipc.client.events;

import exia.ipc.client.entities.Log;
import exia.ipc.client.entities.User;

/**
 * Design pattern Observer.
 */
public interface IModelObserver {

	/**
	 * Quand un utilisateur vient de se connecter ou de se d�connecter.
	 */
	public void onUserEvent(User user, boolean connected);
	
	/**
	 * Quand un message a �t� re�u.
	 */
	public void onLogReceived(Log log);
	
}
