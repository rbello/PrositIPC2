package exia.ipc.client.cypher;

public interface ICypher {

	/**
	 * Encoder une cha�ne de caract�re.
	 */
	public String encode(String data);
	
	/**
	 * D�coder une cha�ne de caract�re.
	 */
	public String decode(String data);
	
	/**
	 * Affiche le nom de l'algorithme.
	 */
	public String toString();
	
}
