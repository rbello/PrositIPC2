package exia.ipc.server;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Logger de l'application.
 * Applique le design pattern Singleton.
 * Les m�thodes d'�criture des logs sont synchronis�es.
 */
public class LogWriter {
	
	/**
	 * Emplacement du fichier de log.
	 * Configurable de mani�re statique.
	 */
	public static File FILE = null;

	/**
	 * Instance unique de la classe (singleton).
	 */
	private static LogWriter INSTANCE = null;

	/**
	 * Mutex utilis� pour �viter l'�criture concurrente.
	 */
	private final Lock mutex;

	/**
	 * Le format d'affichage des dates.
	 */
	private SimpleDateFormat dateFormat;
	
	/**
	 * Constructeur priv� (singleton).
	 */
	private LogWriter() {
		mutex = new ReentrantLock(true);
		dateFormat = new SimpleDateFormat("[yyyy-MM-dd]");
	}

	/**
	 * Acc�s � l'instance unique (singleton). Cr�ation si c'est la premi�re fois.
	 *
	 * @return L'instance unique du logger.
	 */
	public static LogWriter getInstance() {
		// Protection en cas d'appels simultan�s � cette m�thode.
		// Utilisation du moniteur avec le mot cl� 'synchronized' avec en param�tre l'instance
		// qui servira d'"identifiant" pour d�signer le verrou.
		synchronized (LogWriter.class) {
			if (INSTANCE == null) {
				INSTANCE = new LogWriter();
			}
		}
		return INSTANCE;
	}
	
	/**
	 * Ecrire un log normal.
	 */
	public void writeLog(String log) {
		write(log, System.out);
	}
	
	/**
	 * Ecrire un log d'erreur.
	 */
	public void writeError(String log) {
		write(log, System.err);
	}
	
	private void write(String log, PrintStream out) {
		
		// On prend le mutex
		mutex.lock();
		
		// On ajoute la date
		log = dateFormat.format(new Date()) + " " + log;
		
		// On �crit sur la sortie standard
		out.println(log);
		
		// Ecriture sur le fichier
		if (FILE != null) {
			writeFile(log, FILE);
		}
		
		// On lib�re le mutex
		mutex.unlock();
		
	}

	private void writeFile(String msg, File file) {
		// TODO
	}

}
