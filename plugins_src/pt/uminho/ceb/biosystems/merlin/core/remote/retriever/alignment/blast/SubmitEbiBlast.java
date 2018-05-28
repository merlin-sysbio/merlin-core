package pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.blast;

import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.axis.AxisFault;
import org.apache.jcs.access.exception.InvalidArgumentException;
import org.biojava.bio.search.SeqSimilaritySearchHit;
import org.biojava.bio.search.SeqSimilaritySearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.blast.org.biojava3.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.blast.org.biojava3.ws.alignment.qblast.NCBIQBlastOutputFormat;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.blast.org.biojava3.ws.alignment.qblast.NCBIQBlastOutputProperties;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ebi.blast.EbiBlastClientRest;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.MySleep;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.remote.loader.alignment.LoadSimilarityResultstoDatabase;
import pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.HomologyDataClient;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.HomologySearchServer;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

/**
 * @author Oscar
 *
 */
public class SubmitEbiBlast implements Runnable {

	final static Logger logger = LoggerFactory.getLogger(SubmitEbiBlast.class);

	private ConcurrentLinkedQueue<String> rids;
	private NCBIQBlastOutputProperties rof;
	private Project project;
	private EbiBlastClientRest rbw;
	private Map<String, String> queryRIDMap;
	private NCBIQBlastAlignmentProperties rqb;
	private String[] organismTaxa;
	private AtomicBoolean cancel;
	private TimeLeftProgress progress;
	private int sequences_size;
	private long startTime, latencyWaitingPeriod;
	private AtomicInteger sequencesCounter, errorsCounter;
	private ConcurrentHashMap<String, String[]> taxonomyMap;
	private ConcurrentHashMap<String, Boolean> uniprotStar;
	private boolean uniprotStatus;
	private Map<String, Long> ridsLatency;
	private int thread_number;

	private double expectedVal;

	/**
	 * http://www.ebi.ac.uk/Tools/webservices/services/sss/ncbi_blast_rest
	 * 
	 * @param rbw
	 * @param rids
	 * @param numberOfAlignments
	 * @param expectedVal 
	 * @param project
	 * @param queryRIDMap
	 * @param rqb
	 * @param organismTaxa
	 * @param cancel
	 * @param sequences_size
	 * @param startTime
	 * @param progress
	 * @param thread_number
	 * @param sequencesCounter
	 * @param taxonomyMap
	 * @param uniprotStar
	 * @param errorsCounter
	 * @param uniprotStatus
	 * @param latencyWaitingPeriod
	 * @throws InvalidArgumentException 
	 * @throws Exception
	 */
	public SubmitEbiBlast(EbiBlastClientRest rbw, ConcurrentLinkedQueue<String> rids, int numberOfAlignments, double expectedVal, Project project, Map<String, String> queryRIDMap, 
			NCBIQBlastAlignmentProperties rqb, String[] orgArray, AtomicBoolean cancel, int sequences_size, long startTime, TimeLeftProgress progress,
			int thread_number, AtomicInteger sequencesCounter, ConcurrentHashMap<String, String[]> taxonomyMap, 
			ConcurrentHashMap<String, Boolean> uniprotStar, AtomicInteger errorCounter, boolean uniprotStatus,
			long latencyWaitingPeriod) throws InvalidArgumentException  {

		this.sequencesCounter = sequencesCounter;
		this.sequences_size = sequences_size;
		this.startTime = startTime;		
		this.organismTaxa=orgArray;
		this.rqb = rqb;
		this.queryRIDMap = queryRIDMap;
		this.rbw = rbw;
		this.rids = rids;
		this.project = project;
		this.rof = new NCBIQBlastOutputProperties();
		this.rof.setOutputFormat(NCBIQBlastOutputFormat. TEXT);
		this.rof.setAlignmentOutputFormat(NCBIQBlastOutputFormat.PAIRWISE);
		this.rof.setDescriptionNumber(numberOfAlignments);
		this.rof.setAlignmentNumber(numberOfAlignments);
		if(rqb.getOrganism()!=null)
			this.rof.setOrganisms(rqb.getOrganism());
		this.cancel = cancel;
		this.progress = progress;
		this.thread_number = thread_number;
		this.taxonomyMap = taxonomyMap;
		this.uniprotStar = uniprotStar;
		this.errorsCounter = errorCounter;
		this.uniprotStatus = uniprotStatus;
		this.ridsLatency = new HashMap<String, Long>();
		this.latencyWaitingPeriod = latencyWaitingPeriod;
		this.expectedVal = expectedVal;
	}

	@Override
	public void run() {

		logger.info(Thread.currentThread().getName()+"\t"+Thread.currentThread().getId()+"\tstarted.");

		int errorCounter = 0;
		long lastRequestTimer = GregorianCalendar.getInstance().getTimeInMillis();

		while(this.rids.size()>0) {

			if(this.cancel.get()) {

				this.rids.clear();
				this.sequencesCounter.set(this.sequences_size);
			}
			else {
				
				String aRid = null;
				boolean requestReady = false;

				try {

					synchronized(this.rids){

						aRid = this.rids.poll();

						if(!this.ridsLatency.containsKey(aRid))
							this.ridsLatency.put(aRid, GregorianCalendar.getInstance().getTimeInMillis());

						this.rids.notifyAll();

						if(aRid!=null) {

							long currentRequestTimer = GregorianCalendar.getInstance().getTimeInMillis();
							long timeSinceDeployment = currentRequestTimer - this.ridsLatency.get(aRid);

							if(timeSinceDeployment<this.latencyWaitingPeriod) {

								if(timeSinceDeployment>(this.latencyWaitingPeriod/2))
									MySleep.myWait(timeSinceDeployment/60);

								if(currentRequestTimer - lastRequestTimer > 60000) {

									logger.trace("Requesting status for RID "+aRid);
									requestReady = this.rbw.isReady(aRid, GregorianCalendar.getInstance().getTimeInMillis());
									logger.trace("Status for RID "+aRid+" "+requestReady);
									lastRequestTimer = currentRequestTimer;
								}
								else {

									long sleep  = 63000 - (currentRequestTimer - lastRequestTimer); 
									logger.trace("Sleeping..." + (sleep/1000) +" sec "+aRid);
									MySleep.myWait(sleep);
								}

								if(!requestReady)
									if(!this.cancel.get()) 
										this.rids.offer(aRid);
							}
							else {

								logger.debug("Timeout for rid waiting exceeded! Skiping RID "+aRid);
								errorsCounter.incrementAndGet();
							}
						}
					}

					if(requestReady) {

						ReadBlasttoList blastToList = new ReadBlasttoList(this.rbw.getAlignmentResults(aRid, this.rof));

						if(blastToList.isReprocessQuery()) {

							if(!this.cancel.get())
								this.reprocessQuery(aRid,this.queryRIDMap.get(aRid),0);
						}
						else {

							if(blastToList.isSimilarityFound() && SubmitEbiBlast.checkUserEval(this.expectedVal, blastToList)) {

								logger.debug("Similarity found for "+blastToList.getQuery());

								if(!this.cancel.get()) {

									HomologyDataClient homologyDataEbiClient = new HomologyDataClient(blastToList, this.organismTaxa, this.taxonomyMap, this.uniprotStar, this.cancel, 
											HomologySearchServer.EBI, this.rqb.getHitlistSize(), this.uniprotStatus, project.getTaxonomyID());

									if(homologyDataEbiClient.getFastaSequence()==null)
										homologyDataEbiClient.setFastaSequence(this.queryRIDMap.get(aRid).split("\n")[1]);

									if(homologyDataEbiClient.isDataRetrieved()) {
										
										homologyDataEbiClient.setDatabaseID(rqb.getBlastDatabase());

										LoadSimilarityResultstoDatabase lbr = new LoadSimilarityResultstoDatabase(homologyDataEbiClient, this.rqb, this.expectedVal, this.rof.getAlignmentNumber(), this.project, this.cancel);

										lbr.loadData();

										if(lbr.isLoaded()) {

											errorCounter = 0;
											this.sequencesCounter.incrementAndGet();
											logger.debug("Gene\t"+homologyDataEbiClient.getLocus_tag()+"\tprocessed. "+this.rids.size()+" genes left in cue "+thread_number);
										}
										else {

											logger.error("Loading error for aRid "+aRid+"\t"+homologyDataEbiClient.getLocus_tag());
											this.rids.offer(aRid);
											if(this.rids.size()<100)
												MySleep.myWait(1000);
										}
									}
									else {

										if(!this.cancel.get()) {

											logger.debug("Reprocessing "+aRid);
											this.reprocessQuery(aRid,this.queryRIDMap.get(aRid),0);
										}
									}
								}
							}
							else {

								if(!this.cancel.get()) {

									//System.out.println(this.queryRIDMap.get(aRid));
									HomologyDataClient homologyDataEbiClient = new HomologyDataClient(
											this.queryRIDMap.get(aRid).split("\n")[0].replace(">", ""),
											this.rqb.getBlastProgram(),this.cancel, this.uniprotStatus, HomologySearchServer.EBI, project.getTaxonomyID());

									homologyDataEbiClient.setFastaSequence(this.queryRIDMap.get(aRid).split("\n")[1]);
									homologyDataEbiClient.setDatabaseID(rqb.getBlastDatabase());

									LoadSimilarityResultstoDatabase lbr = new LoadSimilarityResultstoDatabase(homologyDataEbiClient,this.rqb, this.expectedVal, this.rof.getAlignmentNumber(), this.project,this.cancel);
									lbr.loadData();
									errorCounter = 0;
									this.sequencesCounter.incrementAndGet();
									logger.debug("Gene\t"+homologyDataEbiClient.getLocus_tag()+"\tprocessed. No similarities. "+this.rids.size()+" genes left in cue "+thread_number);
								}
							}
						}
					}
				}
				catch (AxisFault e) {

					if(!this.cancel.get()) {

						logger.error("Submit blast NCBI server not responding. Aborting thread.");
						this.sequencesCounter.set(this.sequences_size);
						this.rids.clear();
					}
				}
				catch (Exception e) {
					
					e.printStackTrace();

					errorCounter = errorCounter + 1;
					if(!this.cancel.get()) {

						if(errorCounter<25) {

							logger.warn("Submit Blast Exception "+e.getMessage()+"\n Reprocessing:\t"+aRid+" Error ounter: "+errorCounter+"");
							this.rids.remove(aRid);
							this.reprocessQuery(aRid,this.queryRIDMap.get(aRid),0);
						}
						else {
							
							this.errorsCounter.incrementAndGet();
							errorCounter = 0;
						}
					}
				}
			}

			if(!this.cancel.get())
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.sequencesCounter.get(), this.sequences_size);
		}
		logger.info(Thread.currentThread().getName()+"\t"+Thread.currentThread().getId()+"\t ended!");
	}

	/**
	 * @param aRid
	 * @param sequence
	 * @param counter
	 * @return
	 * @throws Exception 
	 */
	private String reprocessQuery(String aRid, String sequence, int counter) {

		try {
			
			MySleep.myWait(3000);
			
			String newRid = this.rbw.sendAlignmentRequest(sequence,this.rqb);

			if(newRid == null) {

				if(counter<5) {

					return this.reprocessQuery(aRid,sequence,counter++);
				}
				else {

					logger.error("Error getting new rid for rid \t"+aRid+" for sequence \t"+sequence);
				}
			}
			else {

				this.rids.offer(newRid);
				this.queryRIDMap.put(newRid, sequence);
				this.queryRIDMap.remove(aRid);
				this.rids.remove(aRid);
				return newRid;
			}

			// http://www.ncbi.nlm.nih.gov/staff/tao/URLAPI/new/node96.html
			// b. For URLAPI scripts, do NOT send requests faster than once every 3 seconds. 
			
		}
		catch (Exception e){

			if(e.getMessage() == null)  {

				logger.error("Cannot perform BLAST at this time, try again later!");
			}
			else {

				if(e.getMessage().contains("NCBI QBlast refused this request because")) {

					logger.error("Cannot perform BLAST at this time, try again later!");
				}
				else if(e.getMessage().contains("Cannot get RID for alignment!")) {

					logger.error("Cannot get RID for sequence "+sequence+". Retrying query!");
					return this.reprocessQuery(aRid,sequence,counter++);
				}
				else {

					return this.reprocessQuery(aRid,sequence,counter++);
				}
			}
		}
		return null;
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
	
	private static boolean checkUserEval(double expectedVal, ReadBlasttoList blastToList) {

		for (SeqSimilaritySearchResult result : blastToList.getResults()) {

			@SuppressWarnings("unchecked")
			List<SeqSimilaritySearchHit> hits = (List<SeqSimilaritySearchHit>) result.getHits();

			for (int i = 0; i<hits.size();i++ ){

				SeqSimilaritySearchHit hit = hits.get(i);
				String id = hit.getSubjectID();

				if(id!=null)
					 if(hit.getEValue()<=expectedVal)
							return true;
			}
		}
		return false;
	}

	/**
	 * @param request
	 * @throws Exception 
	 * @throws IOException 
	private void getResult(String request) throws IOException, Exception{
		String path = dirPath+"/"+request;
		this.writeFile(rbw.getAlignmentResults(request, this.rof),path);
	}
	 */

	/**
	 * @param inputStream
	 * @param path
	 * @throws IOException 
	private void writeFile(InputStream inputStream, String path) throws IOException{
		//write the inputStream to a FileOutputStream
		OutputStream out = new FileOutputStream(new File(path));
		int read=0;
		byte[] bytes = new byte[1024];
		while((read = inputStream.read(bytes))!= -1){
			out.write(bytes, 0, read);
		}
		inputStream.close();
		out.flush();
		out.close();
	}
	 */

}