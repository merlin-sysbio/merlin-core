/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.blast;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.blast.org.biojava3.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.blast.org.biojava3.ws.alignment.qblast.NCBIQBlastOutputFormat;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.blast.org.biojava3.ws.alignment.qblast.NCBIQBlastOutputProperties;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.blast.org.biojava3.ws.alignment.qblast.NcbiBlastService;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

/**
 * @author ODias
 *
 */
public class SubmitNcbiBlast implements Runnable {

	private static final boolean verbose = true;
	private ConcurrentLinkedQueue<String> rids;
	private NCBIQBlastOutputProperties rof;
	private Project project;
	private NcbiBlastService rbw;
	private Map<String, String> queryRIDMap;
	private NCBIQBlastAlignmentProperties rqb;
	private String[] orgArray;
	private AtomicBoolean cancel;
	private TimeLeftProgress progress;
	private int sequences_size;
	private long startTime, latencyWaitingPeriod;
	private AtomicInteger sequencesCounter, errorCounter;
	private ConcurrentHashMap<String, String[]> taxonomyMap;
	private ConcurrentHashMap<String, Boolean> uniprotStar;
	private boolean uniprotStatus;
	private Map<String, Long> ridsLatency;
	private int thread_number;

	/**
	 * http://biochem.uthscsa.edu/~hs_lab/frames/molgen/tutor/blast_databases.html
	 * 
	 * @param rbw
	 * @param rids
	 * @param numberOfAlignments
	 * @param project
	 * @param queryRIDMap
	 * @param rqb
	 * @param orgArray
	 * @param cancel
	 * @param sequences_size
	 * @param startTime
	 * @param progress
	 * @param thread_number
	 * @param sequencesCounter
	 * @param taxonomyMap
	 * @param uniprotStar
	 * @param errorCounter
	 * @param uniprotStatus
	 * @param latencyWaitingPeriod
	 * @throws Exception 
	 */
	public SubmitNcbiBlast(NcbiBlastService rbw, ConcurrentLinkedQueue<String> rids, int numberOfAlignments, Project project, Map<String, String> queryRIDMap, 
			NCBIQBlastAlignmentProperties rqb, String[] orgArray, AtomicBoolean cancel, int sequences_size, long startTime, TimeLeftProgress progress,
			int thread_number, AtomicInteger sequencesCounter, ConcurrentHashMap<String, String[]> taxonomyMap, 
			ConcurrentHashMap<String, Boolean> uniprotStar, AtomicInteger errorCounter, boolean uniprotStatus,
			long latencyWaitingPeriod) throws Exception  {

		this.sequencesCounter = sequencesCounter;
		this.sequences_size = sequences_size;
		this.startTime = startTime;		
		this.orgArray=orgArray;
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
		//this.currentlyBeingProcessed = currentlyBeingProcessed;
		this.thread_number = thread_number;
		this.taxonomyMap = taxonomyMap;
		this.uniprotStar = uniprotStar;
		this.errorCounter = errorCounter;
		this.uniprotStatus = uniprotStatus;
		this.ridsLatency = new HashMap<String, Long>();
		this.latencyWaitingPeriod = latencyWaitingPeriod;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}

//	@Override
//	public void run() {
//
//		System.out.println("SubmitBLAST\t"+Thread.currentThread().getName()+"\t"+Thread.currentThread().getId()+"\tstarted.");
//
//		int counter = 0;
//		long lastRequestTimer = GregorianCalendar.getInstance().getTimeInMillis();
//
//		while(this.rids.size()>0) {
//			
//			if(this.cancel.get()) {
//
//				this.rids.clear();
//				this.sequencesCounter.set(this.sequences_size);
//			}
//			else {
//
//				String aRid = null;
//				boolean requestReady = false;
//
//				try {
//
//					synchronized(this.rids) {
//
//						aRid = this.rids.poll();
//
//						if(!this.ridsLatency.containsKey(aRid)) 
//							this.ridsLatency.put(aRid, GregorianCalendar.getInstance().getTimeInMillis());
//
//						this.rids.notifyAll();
//
//						if(aRid!=null) {
//
//							long currentRequestTimer = GregorianCalendar.getInstance().getTimeInMillis();
//							long timeSinceDeployment = currentRequestTimer - this.ridsLatency.get(aRid);
//
//							if(timeSinceDeployment<this.latencyWaitingPeriod) {
//
//								if(timeSinceDeployment>(this.latencyWaitingPeriod/2))
//									MySleep.myWait(180000);
//
//								// http://www.ncbi.nlm.nih.gov/staff/tao/URLAPI/new/node96.html
//								// c. While polling for results with RIDs, do NOT request results at rates greater than once per minute
//								if(currentRequestTimer - lastRequestTimer > 60000) {
//
//									System.out.println("Requesting status for RID "+aRid);
//									requestReady = this.rbw.isReady(aRid, GregorianCalendar.getInstance().getTimeInMillis());
//									System.out.println("Status for RID "+aRid+" "+requestReady);
//									lastRequestTimer = currentRequestTimer;
//								}
//								else {
//
//									long sleep  = 65000 - (currentRequestTimer - lastRequestTimer); 
//									System.out.println("Sleeping..." + (sleep /1000) +" sec RID "+aRid);
//									MySleep.myWait(sleep);
//								}
//
//								if(!requestReady) {
//
//									if(!this.cancel.get()) {
//
//										this.rids.offer(aRid);
//										//System.out.println(this.rids.size()+"\t"+this.rids);
//										//https://blast.ncbi.nlm.nih.gov/Blast.cgi?CMD=Get&RID=aRid
//									}
//								}
//							}
//							else {
//
//								System.err.println(" Timeout for rid waiting exceeded! Skiping RID "+aRid);
//								errorCounter.incrementAndGet();
//							}
//						}
//					}
//					
//					if(requestReady) {
//
//						InputStream inputStream = this.rbw.getAlignmentResults(aRid, this.rof);
//						InputStream bufferedinputStream = new BufferedInputStream(inputStream);
//						ReadBlasttoList blastToList = new ReadBlasttoList(bufferedinputStream);
//
//						if(blastToList.isReprocessQuery()) {
//
//							if(!this.cancel.get())
//								this.reprocessQuery(aRid,this.queryRIDMap.get(aRid),0);
//						}
//						else {
//
//							if(blastToList.isSimilarityFound()) {
//
//								//System.out.println("Found for "+blastToList.getQuery());
//
//								if(!this.cancel.get()) {
//
//									HomologyDataClient homologyDataNcbiClient = new HomologyDataClient(blastToList, this.orgArray, this.taxonomyMap, this.uniprotStar,this.cancel, HomologySearchServer.NCBI, this.rqb.getHitlistSize(), this.uniprotStatus, project.getTaxonomyID());
//
//									if(homologyDataNcbiClient.getFastaSequence()==null)
//										homologyDataNcbiClient.setFastaSequence(this.queryRIDMap.get(aRid).split("%0D")[1].replace("%3E", "").replace("%0A", ""));
//
//									if(homologyDataNcbiClient.isDataRetrieved()) {
//
//										homologyDataNcbiClient.setDatabaseID(rqb.getBlastDatabase());
//										
//										LoadSimilarityResultstoDatabase lbr = new LoadSimilarityResultstoDatabase(homologyDataNcbiClient,this.rqb,this.this.rof.getAlignmentNumber(),this.project,this.cancel);
//
//										lbr.loadData();
//
//										if(!lbr.isLoaded()) {
//
//											this.rids.offer(aRid);
//
//											if(this.rids.size()<100)
//												MySleep.myWait(1000);
//										}
//										else {
//
//											counter = 0;
//											this.sequencesCounter.incrementAndGet();
//											System.out.println("Gene\t"+homologyDataNcbiClient.getLocus_tag()+"\tprocessed. "+this.rids.size()+" genes left in cue "+thread_number);
//										}
//									}
//									else {
//
//										if(!this.cancel.get()) {
//
//											System.out.println("Reprocessing "+aRid);
//											this.reprocessQuery(aRid,this.queryRIDMap.get(aRid),0);
//										}
//									}
//								}
//							}
//							else {
//
//								if(!this.cancel.get()) {
//
//									HomologyDataClient homologyDataNcbiClient = new HomologyDataClient(this.queryRIDMap.get(aRid).split("%0D")[0].replace("%3E", ""),this.rqb.getBlastProgram(),this.cancel, this.uniprotStatus,HomologySearchServer.NCBI, project.getTaxonomyID());
//									homologyDataNcbiClient.setFastaSequence(this.queryRIDMap.get(aRid).split("%0D")[1].replace("%3E", "").replace("%0A", ""));
//									homologyDataNcbiClient.setDatabaseID(rqb.getBlastDatabase());
//									LoadSimilarityResultstoDatabase lbr = new LoadSimilarityResultstoDatabase(homologyDataNcbiClient,this.rqb,this.rof.getAlignmentNumber(), this.project,this.cancel);
//									lbr.loadData();
//									counter = 0;
//									this.sequencesCounter.incrementAndGet();
//									System.out.println("Gene\t"+homologyDataNcbiClient.getLocus_tag()+"\tprocessed. No similarities. "+this.rids.size()+" genes left in cue "+thread_number);
//								}
//							}
//						}
//					}
//				}
//				catch (Exception e) {
//					
//					if(verbose)
//						e.printStackTrace();
//
//					counter = counter + 1;
//					if(!this.cancel.get()) {
//
//						if(counter<5) {
//
//							System.out.println("Submit Blast Exception "+e.getMessage()+"\n" +
//									"Reprocessing:\t"+aRid+"\n" 
//									+ "Counter: "+counter+"\n" 
//									//+ "Sequence\t"+queryRIDMap.get(aRid)+"\n"
//									);
//							this.rids.remove(aRid);
//							this.reprocessQuery(aRid,this.queryRIDMap.get(aRid),0);
//						}
//						else {
//
//							e.printStackTrace();
//							errorCounter.incrementAndGet();
//							counter = 0;
//						}
//					}
//				}
//			}
//
//			if(!this.cancel.get())
//				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.sequencesCounter.get(), this.sequences_size);
//		}
//
//		System.out.println("SubmitBLAST\t"+Thread.currentThread().getName()+"\t"+Thread.currentThread().getId()+"\t ended!");
//	}

	/**
	 * @param aRid
	 * @param sequence
	 * @param counter
	 * @return
	 * @throws Exception 
	 */
//	private String reprocessQuery(String aRid, String sequence, int counter) {
//
//		try {
//
//			String newRid = this.rbw.sendAlignmentRequest(sequence,this.rqb);
//
//			if(newRid == null) {
//
//				if(counter<5) {
//
//					return this.reprocessQuery(aRid,sequence,counter++);
//				}
//				else {
//
//					System.out.println("Error getting new rid for rid \t"+aRid);
//					System.out.println("For sequence \t"+sequence);
//				}
//			}
//			else {
//
//				this.rids.offer(newRid);
//				this.queryRIDMap.put(newRid, sequence);
//				return newRid;
//			}
//
//			// http://www.ncbi.nlm.nih.gov/staff/tao/URLAPI/new/node96.html
//			// b. For URLAPI scripts, do NOT send requests faster than once every 3 seconds. 
//			MySleep.myWait(5000);
//		}
//		catch (Exception e){
//			
//			if(verbose)
//				e.printStackTrace();
//
//			if(e.getMessage() == null)  {
//
//				System.err.println("Submit BLAST. Cannot perform BLAST at this time, try again later!");
//			}
//			else {
//
//				//this.myWait(2000);
//				if(e.getMessage().contains("NCBI QBlast refused this request because")) {
//
//					System.err.println("Submit BLAST. Cannot perform BLAST at this time, try again later!");
//				}
//				else if(e.getMessage().contains("Cannot get RID for alignment!")) {
//
//					System.out.println("Submit BLAST. Cannot get RID for sequence "+sequence+"\nRetrying query!");
//					return this.reprocessQuery(aRid,sequence,counter++);
//				}
//				else {
//
//					return this.reprocessQuery(aRid,sequence,counter++);
//				}
//			}
//		}
//		finally {
//
//			this.queryRIDMap.remove(aRid);
//			this.rids.remove(aRid);
//		}
//		return null;
//	}


	/**
	 * @return the progress
	 */
//	public TimeLeftProgress getProgress() {
//		return progress;
//	}

	/**
	 * @param progress the progress to set
	 */
//	public void setProgress(TimeLeftProgress progress) {
//		this.progress = progress;
//	}

	/**
	 * @return the cancel
	 */
//	public AtomicBoolean isCancel() {
//		return cancel;
//	}
//
//	/**
//	 * @param cancel the cancel to set
//	 */
//	public void setCancel(AtomicBoolean cancel) {
//		this.cancel = cancel;
//	}

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

