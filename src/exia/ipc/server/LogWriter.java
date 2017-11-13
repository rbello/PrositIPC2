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
 * Les méthodes d'écriture des logs sont synchronisées.
 */
public class LogWriter {
	
	/**
	 * Emplacement du fichier de log.
	 * Configurable de manière statique.
	 */
	public static File FILE = null;

	/**
	 * Instance unique de la classe (singleton).
	 */
	private static LogWriter INSTANCE = null;

	/**
	 * Mutex utilisé pour éviter l'écriture concurrente.
	 */
	private final Lock mutex;

	/**
	 * Le format d'affichage des dates.
	 */
	private SimpleDateFormat dateFormat;
	
	/**
	 * Constructeur privé (singleton).
	 */
	private LogWriter() {
		mutex = new ReentrantLock(true);
		dateFormat = new SimpleDateFormat("[yyyy-MM-dd]");
	}

	/**
	 * Accès à l'instance unique (singleton). Création si c'est la première fois.
	 *
	 * @return L'instance unique du logger.
	 */
	public static LogWriter getInstance() {
		// Protection en cas d'appels simultanés à cette méthode.
		// Utilisation du moniteur avec le mot clé 'synchronized' avec en paramètre l'instance
		// qui servira d'"identifiant" pour désigner le verrou.
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
		
		// On écrit sur la sortie standard
		out.println(log);
		
		// Ecriture sur le fichier
		if (FILE != null) {
			writeFile(log, FILE);
		}
		
		// On libère le mutex
		mutex.unlock();
		
	}

	private void writeFile(String msg, File file) {
		// TODO
	}

}
