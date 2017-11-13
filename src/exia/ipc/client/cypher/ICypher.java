package exia.ipc.client.cypher;

public interface ICypher {

	/**
	 * Encoder une chaîne de caractère.
	 */
	public String encode(String data);
	
	/**
	 * Décoder une chaîne de caractère.
	 */
	public String decode(String data);
	
	/**
	 * Affiche le nom de l'algorithme.
	 */
	public String toString();
	
}
