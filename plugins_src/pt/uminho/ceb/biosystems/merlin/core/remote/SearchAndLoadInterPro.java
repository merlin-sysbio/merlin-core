package pt.uminho.ceb.biosystems.merlin.core.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.biojava.nbio.core.sequence.template.AbstractSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ebi.EbiAPI;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ebi.interpro.InterProResultsList;
import pt.uminho.ceb.biosystems.merlin.core.remote.loader.interpro.LoadInterProData;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseAccess;

/**
 * @author Oscar Dias
 *
 */
public class SearchAndLoadInterPro extends Observable implements Observer {

	private AtomicBoolean cancel;
	private AtomicInteger errorCounter;
	private DatabaseAccess databaseAccess;
	private AtomicInteger sequencesCounter;
	final static Logger logger = LoggerFactory.getLogger(SearchAndLoadInterPro.class);
	
	/**
	 * Constructor for processing InterPro scan.
	 * 
	 * @param databaseAccess
	 * @param cancel
	 * @param sequencesCounter
	 * @param errorCounter 
	 */
	public SearchAndLoadInterPro(DatabaseAccess databaseAccess, AtomicBoolean cancel, AtomicInteger sequencesCounter, AtomicInteger errorCounter) {
		
		this.cancel = cancel;
		this.sequencesCounter = sequencesCounter;
		this.errorCounter = errorCounter;
		this.databaseAccess = databaseAccess;
	}
	
	/**
	 * Get InterPro results map.
	 * @param waitingPeriod 
	 * 
	 * @return
	 * @throws InterruptedException 
	 * @throws IOException 
	 * @throws Exception
	 */
	public Map<String, InterProResultsList> getInterProResults(Map<String, AbstractSequence<?>> genome, long waitingPeriod, String email) throws InterruptedException, IOException  {
		
		EbiAPI ebiAPI = new EbiAPI();
		ebiAPI.addObserver(this);
		Map<String, InterProResultsList> interPro = ebiAPI.getInterProAnnotations(genome, this.errorCounter, this.cancel, waitingPeriod, this.sequencesCounter, email);
		
		return interPro;
	}
	
	/**
	 * Load the results to the database.
	 * 
	 * @param list
	 * @throws InterruptedException 
	 */
	public void loadInterProResults(Map<String, InterProResultsList> map) throws InterruptedException {

		int numberOfProcesses =  Runtime.getRuntime().availableProcessors();
		List<Thread> threads = new ArrayList<Thread>();
		List<Runnable> runnables = new ArrayList<Runnable>();
		ConcurrentLinkedQueue<String> list = new ConcurrentLinkedQueue<>(map.keySet());
		ConcurrentHashMap<String, Integer> auxiliaryMap = new ConcurrentHashMap<> ();

		for(int i=0; i<numberOfProcesses; i++) {

			logger.info("Starting process {} ", i);
			Runnable loadInterProData = new LoadInterProData(this.databaseAccess, map, list, this.cancel, this.sequencesCounter, auxiliaryMap);
			((LoadInterProData) loadInterProData).addObserver(this);
			runnables.add(loadInterProData);
			Thread thread = new Thread(loadInterProData);
			threads.add(thread);
			thread.start();
		}

		for(Thread thread :threads)			
			thread.join();
	}

	@Override
	public void update(java.util.Observable o, Object arg) {
		
		logger.debug("sequence updated {}", this.sequencesCounter.get());
		
		setChanged();
		notifyObservers();
	}

}
