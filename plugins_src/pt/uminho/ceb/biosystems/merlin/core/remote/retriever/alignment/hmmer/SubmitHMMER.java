/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.hmmer;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.biojava.nbio.core.sequence.ProteinSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.MySleep;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.remote.loader.alignment.LoadSimilarityResultstoDatabase;
import pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.HomologyDataClient;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.HmmerRemoteDatabasesEnum;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.HomologySearchServer;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

/**
 * @author ODias
 *
 */
public class SubmitHMMER implements Runnable {
	
	final static Logger logger = LoggerFactory.getLogger(SubmitHMMER.class);

	private String[] organismTaxa;
	private Project project;
	private int numberOfAlignments;
	private TimeLeftProgress progress;
	private int sequences_size;
	private long startTime;
	private ConcurrentLinkedQueue<String> sequences;
	private Map<String, ProteinSequence> query;
	private AtomicBoolean cancel;
	private HmmerRemoteDatabasesEnum database;
	private double eValue;
	private AtomicInteger sequencesCounter, errorCounter;
	private ConcurrentHashMap<String, String[]> taxonomyMap;
	private ConcurrentHashMap<String, Boolean> uniprotStar;
	private boolean uniprotStatus;
	private AtomicLong time;
	private long latencyWaitingPeriod;
	private Map<String, Long> ridsLatency;
	//private String email;

	/**
	 * @param sequences
	 * @param numberOfAlignments
	 * @param project
	 * @param organismTaxa
	 * @param database
	 * @param cancel
	 * @param sequences_size
	 * @param startTime
	 * @param progress
	 * @param taxonomyMap 
	 * @param uniprotStatus 
	 */
	public SubmitHMMER(ConcurrentLinkedQueue<String> sequences, Map<String, ProteinSequence> query, double expectedVal, int numberOfAlignments, Project project, 
			String[] organismTaxa, HmmerRemoteDatabasesEnum database, AtomicBoolean cancel, 
			int sequences_size, long startTime, TimeLeftProgress progress, ConcurrentLinkedQueue<String> currentlyBeingProcessed, AtomicInteger sequencesCounter, ConcurrentHashMap<String, String[]> taxonomyMap, 
			ConcurrentHashMap<String, Boolean> uniprotStar, AtomicInteger errorCounter, boolean uniprotStatus, AtomicLong time, long latencyWaitingPeriod) {

		this.sequencesCounter = sequencesCounter;
		this.sequences = sequences;
		this.query = query;
		this.eValue = expectedVal;
		this.organismTaxa=organismTaxa;
		this.project = project;
		this.numberOfAlignments = numberOfAlignments;
		this.database = database;
		this.cancel = cancel;
		this.sequences_size = sequences_size;
		this.startTime = startTime;
		this.progress = progress;
		this.taxonomyMap = taxonomyMap;
		this.uniprotStar = uniprotStar;
		this.errorCounter = errorCounter;
		this.uniprotStatus = uniprotStatus;
		this.time = time;
		this.latencyWaitingPeriod = latencyWaitingPeriod;
		this.ridsLatency = new HashMap<>();
	}

	@Override
	public void run() {

		logger.info(Thread.currentThread().getName()+"\t"+Thread.currentThread().getId()+"\tstarted.");
		
		int counter = 0;
		Map<String, String> hmmerResultKeys = new HashMap<>();

		while(this.sequences.size()>0 && !this.cancel.get()) {

			if(this.cancel.get()) {

				this.sequences.clear();
				this.sequencesCounter.set(sequences_size);
				
				for(String link : hmmerResultKeys.values()) {
					
					try {
						
						ReadHmmertoList.deleteJob(link);
					} 
					catch (Exception e) {

						e.printStackTrace();
					}
				}
			}
			else {

				String sequence = null;

				try {

					boolean processed = false;
					ReadHmmertoList hmmerToList = null;

					synchronized(this.sequences) {

						sequence = this.sequences.poll();

						if(!this.ridsLatency.containsKey(sequence))
							this.ridsLatency.put(sequence, GregorianCalendar.getInstance().getTimeInMillis());

						this.sequences.notifyAll();

						MySleep.myWait(3000);

						long currentRequestTimer = GregorianCalendar.getInstance().getTimeInMillis();
						long timeSinceDeployment = currentRequestTimer - this.ridsLatency.get(sequence);

						if(timeSinceDeployment<this.latencyWaitingPeriod) {

							if(timeSinceDeployment>(this.latencyWaitingPeriod/2))
								MySleep.myWait(180000);

							if(sequence != null) { 
								
								hmmerToList = new ReadHmmertoList(this.query.get(sequence).getSequenceAsString(), sequence,
										this.database,this.numberOfAlignments,this.eValue, this.cancel);//, this.email);

								String jobID;

								if(hmmerResultKeys.containsKey(sequence)) {

									jobID = hmmerResultKeys.get(sequence);
								}
								else {
									
									long delay = -1;
									synchronized (this.time) {
										
										while(delay<1500 && !cancel.get()) {
											
											delay = System.currentTimeMillis() - this.time.get();
											this.time.set(System.currentTimeMillis());
											
											if (delay < 1500)
												MySleep.myWait(1500);
										}
									}

									jobID = hmmerToList.getJobID();
									hmmerResultKeys.put(sequence, jobID);
								}
								
								logger.trace("Requesting status for jobID {}",jobID);
								processed = hmmerToList.scan(jobID);		
								logger.trace("Status for jobID "+jobID+" "+processed);
								
								if(!processed) {
									
									long sleep = 15000;
									MySleep.myWait(sleep);
									this.sequences.offer(sequence);
									logger.trace("Sleeping..." + (sleep /1000) +" sec jobID "+jobID);
								}
							}
						}
						else {

							logger.debug("Timeout for rid waiting exceeded! Skiping sequence "+sequence);
							errorCounter.incrementAndGet();
						}
					}

					if(processed) {

						HomologyDataClient homologyDataClient;

						if(hmmerToList.getResults().size()>0 && !this.cancel.get()) {

							homologyDataClient = new HomologyDataClient(hmmerToList, this.organismTaxa, this.taxonomyMap, this.uniprotStar,this.cancel, this.uniprotStatus, HomologySearchServer.HMMER, project.getTaxonomyID());

							if(homologyDataClient.getFastaSequence()==null)
								homologyDataClient.setFastaSequence(this.query.get(sequence).getSequenceAsString());
							
							if(homologyDataClient.isDataRetrieved()) {

								homologyDataClient.setDatabaseID(this.database.toString());
								LoadSimilarityResultstoDatabase lbr = new LoadSimilarityResultstoDatabase(homologyDataClient,this.numberOfAlignments,this.eValue,this.project,this.cancel);
								lbr.loadData();

								if(lbr.isLoaded()) {

									counter=0;
									System.gc();
									this.sequencesCounter.incrementAndGet();
									logger.debug("Gene\t"+homologyDataClient.getLocus_tag()+"\tprocessed.");
								}
								else {

									counter=0;
									this.sequences.offer(sequence);
									logger.error("Loading error for query "+sequence+"\t"+homologyDataClient.getLocus_tag());
								}
							}
							else
								this.sequences.offer(sequence);
						}
						else {

							if(processed && !this.cancel.get()) {

								homologyDataClient = new HomologyDataClient(hmmerToList.getQuery(),"hmmer",this.cancel, this.uniprotStatus, HomologySearchServer.HMMER, project.getTaxonomyID());
								homologyDataClient.setFastaSequence(this.query.get(sequence).getSequenceAsString());
								homologyDataClient.setDatabaseID(this.database.toString());
								homologyDataClient.setProgram("hmmer");
								homologyDataClient.setVersion("");
								homologyDataClient.setNoSimilarity(true);
								LoadSimilarityResultstoDatabase lbr = new LoadSimilarityResultstoDatabase(homologyDataClient,this.numberOfAlignments,this.eValue,this.project,this.cancel);
								lbr.loadData();
								counter=0;
								this.sequencesCounter.incrementAndGet();
								logger.debug("Gene\t"+homologyDataClient.getLocus_tag()+"\tprocessed. No similarities.");
							}
						}
					}
				}
				catch (Exception e) {

					counter = counter + 1 ;
					if(!this.cancel.get()) {
						
						if(counter<5) {

							logger.error("Reprocessing\t"+sequence+".\t counter:"+counter);
							if(sequence!=null) {

								this.sequences.offer(sequence);
								hmmerResultKeys.remove(sequence);
							}
						} 
						else {

							errorCounter.incrementAndGet();
							counter = 0;
							e.printStackTrace();
						}
					}
				}
			}

			if(!this.cancel.get())
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime),this.sequencesCounter.get(),this.sequences_size);
		}
		logger.info(Thread.currentThread().getName()+"\t"+Thread.currentThread().getId()+"\tended.");
	}

	/**
	 * @return the progress
	 */
	public TimeLeftProgress getProgress() {
		return progress;
	}

	/**
	 * @param progress the progress to set
	 */
	public void setProgress(TimeLeftProgress progress) {
		this.progress = progress;
	}

	/**
	 * @return the cancel
	 */
	public AtomicBoolean isCancel() {
		return cancel;
	}

	/**
	 * @param cancel the cancel to set
	 */
	public void setCancel(AtomicBoolean cancel) {
		this.cancel = cancel;
	}

//	/**
//	 * @return the email
//	 */
//	public String getEmail() {
//		return email;
//	}
//
//	/**
//	 * @param email the email to set
//	 */
//	public void setEmail(String email) {
//		this.email = email;
//	}

}
