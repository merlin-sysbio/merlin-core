/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.remote;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.jcs.access.exception.InvalidArgumentException;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.template.AbstractSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.blast.org.biojava3.ws.alignment.RemotePairwiseAlignmentService;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.blast.org.biojava3.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.blast.org.biojava3.ws.alignment.qblast.NcbiBlastService;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ebi.blast.EbiBlastClientRest;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ebi.uniprot.UniProtAPI;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.MySleep;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.blast.SubmitEbiBlast;
import pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.blast.SubmitNcbiBlast;
import pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.hmmer.SubmitHMMER;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.HmmerRemoteDatabasesEnum;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.HomologySearchServer;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.HomologyAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.utilities.Enumerators.Matrix;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

/**
 * @author ODias
 *
 */
public class SearchAndLoadHomologueSequences {

	final static Logger logger = LoggerFactory.getLogger(SearchAndLoadHomologueSequences.class);
	
	private ConcurrentLinkedQueue<String> blosum62, blosum80, pam30, pam70, smaller, otherSequences;
	private short gapExtensionPenalty, gapOpenPenalty;
	private Project project;
	private Set<String> loadedGenes;
	private String[] organismTaxa= null;
	private short wordSize;
	private Matrix blastMatrix;
	private Map<String, AbstractSequence<?>> sequenceFile;
	private String organism;
	private AtomicBoolean cancel;
	private TimeLeftProgress progress;
	private long startTime;
	private boolean similaritySearchProcessAvailable;
	private int sequences_size;
	private ArrayList<Runnable> runnables;
	private AtomicInteger sequencesCounter;
	private ConcurrentHashMap<String,String[]> taxonomyMap;
	private ConcurrentHashMap<String, Boolean> uniprotStar;
	private int geneticCode;
	private long latencyWaitingPeriod;
	private boolean uniprotStatus, reBlast = true;
	private int sequencesWithErrors;
	private HomologySearchServer source;
	private String email;


	/**
	 * @param sequences
	 * @param project
	 * @param source
	 * @param email
	 * @throws Exception
	 */
	public SearchAndLoadHomologueSequences(Map<String, AbstractSequence<?>> sequences, Project project, HomologySearchServer source, String email) throws Exception {

		this.gapExtensionPenalty=-1;
		this.gapOpenPenalty=-1;
		this.wordSize=-1;
		this.blastMatrix = null;
		this.organism=null;
		this.project=project;
		this.sequenceFile = sequences;
		this.cancel = new AtomicBoolean(false);
		this.startTime = GregorianCalendar.getInstance().getTimeInMillis();
		this.similaritySearchProcessAvailable=true;
		this.sequencesCounter = new AtomicInteger(0);
		this.taxonomyMap = new ConcurrentHashMap<String, String[]>();
		this.uniprotStar = new ConcurrentHashMap<String, Boolean>();
		this.geneticCode = -1;
		this.source = source;
		this.email = email;
	}
	

	/**
	 * @param orgTaxonomyID
	 * @throws Exception 
	 */
	public void setTaxonomicID(String orgTaxonomyID, HmmerRemoteDatabasesEnum database) throws Exception {

		String[] orgData = new String[2];
		orgData[0] = this.project.getOrganismName();
		orgData[1] = this.project.getOrganismLineage();
		this.organismTaxa = orgData;
		int taxon = Integer.valueOf(orgTaxonomyID);
		
		if(!database.equals(HmmerRemoteDatabasesEnum.pdb))
			this.organismTaxa = this.ebiNewTaxID(taxon);


		this.taxonomyMap.put(orgTaxonomyID, this.organismTaxa);
	}

	/**
	 * @param orgTaxonomyID
	 * @throws Exception 
	 */
	public void setTaxonomicID(String orgTaxonomyID) throws Exception {
		
		String[] orgData = new String[2];
		orgData[0] = this.project.getOrganismName();
		orgData[1] = this.project.getOrganismLineage();
		this.organismTaxa = orgData;
		int taxon = Integer.valueOf(orgTaxonomyID);
		
		if(HomologySearchServer.EBI.equals(this.source))
			this.organismTaxa = this.ebiNewTaxID(taxon);

		this.taxonomyMap.put(orgTaxonomyID, this.organismTaxa);
	}

	/**
	 * @param map
	 * @return
	 */
	public ConcurrentLinkedQueue<String> getRequestsList(Map<String, AbstractSequence<?>> map) {
		ConcurrentLinkedQueue<String> result = new ConcurrentLinkedQueue<String>();
		String request="";

		String beginning = "%3E", returnCode="%0D%0A";

		if(this.source==HomologySearchServer.EBI) {

			beginning = ">";
			returnCode="\n";
		}

		for(String key: map.keySet()) {

			if(!(this.getLoadedGenes()!=null && this.getLoadedGenes().contains(key))) {
				this.sequences_size++;
				request += beginning+key.trim()+returnCode;
				request += map.get(key).getSequenceAsString()+returnCode;
				result.add(request);
				request="";
			}
		}
		return result;
	} 

	/**
	 * @param program
	 * @param database
	 * @param numberOfAlignments
	 * @param expectedVal
	 * @param wordSize
	 * @param requests
	 * @param matrix
	 * @param gapExtensionPenalty
	 * @param gapOpenPenalty
	 * @throws Exception 
	 */
	private int blastSingleSequenceNcbi(String program, String database, int numberOfAlignments, double expectedVal, short wordSize, ConcurrentLinkedQueue<String> requests,
			Matrix matrix, short gapExtensionPenalty, short gapOpenPenalty) throws Exception {

		sequencesWithErrors = 0;

		Map<String,String> queryRIDMap = new HashMap<String, String>();

		NCBIQBlastAlignmentProperties rqb = new NCBIQBlastAlignmentProperties();
		rqb.setBlastProgram(program);
		rqb.setBlastDatabase(database);
		rqb.setBlastExpect(expectedVal);
		rqb.setBlastMatrix(matrix.toString().toUpperCase());
		if(this.geneticCode>0)
			rqb.setGeneticCode(this.geneticCode);

		if(gapOpenPenalty!=-1)
			rqb.setBlastGapCreation(gapOpenPenalty);

		if(gapExtensionPenalty!=-1)
			rqb.setBlastGapExtension(gapExtensionPenalty);

		rqb.setBlastWordSize(wordSize);
		rqb.setHitlistSize(numberOfAlignments);

		if(this.organism!=null) {

			logger.info("Setting organism to: "+this.organism);
			rqb.setOrganism(this.organism);
		}

		if(!this.cancel.get()) {

			AtomicInteger errorCounter = new AtomicInteger(0);

			int threadsNumber=0;
			int numberOfCores = Runtime.getRuntime().availableProcessors()*4;
			List<Thread> threads = new ArrayList<Thread>();
			this.runnables = new ArrayList<Runnable>();

			if(requests.size()<numberOfCores)
				threadsNumber=requests.size();
			else
				threadsNumber=numberOfCores;

			List<ConcurrentLinkedQueue<String>> rids = new ArrayList<ConcurrentLinkedQueue<String>>();
			NcbiBlastService[] ncbiBlastServiceArray = new NcbiBlastService[threadsNumber];

			int t=0;
			while( t<threadsNumber) {

				ConcurrentLinkedQueue<String> rid = new ConcurrentLinkedQueue<String>();
				rids.add(t,rid);
				ncbiBlastServiceArray[t] = new NcbiBlastService(30000, this.email);
				t++;
			}

			t=0;
			
			while(!requests.isEmpty() && !this.cancel.get()) {

				if(!this.similaritySearchProcessAvailable || this.cancel.get())
					requests.clear();

				if(!requests.isEmpty()) {

					String sequence=requests.poll();

					String newRid = this.processQuery(sequence, ncbiBlastServiceArray[t], rqb, 0);
					
					rids.get(t).offer(newRid);
					queryRIDMap.put(newRid, sequence);
					
					t++;
					if(t>=threadsNumber)
						t=0;
				}
			}

			if(this.similaritySearchProcessAvailable  && !this.cancel.get() && queryRIDMap.size()>0) {

				for(int i=0; i<threadsNumber; i++) {

					Runnable lc	= new SubmitNcbiBlast(ncbiBlastServiceArray[i], rids.get(i), numberOfAlignments, this.project, queryRIDMap, rqb,
							this.organismTaxa, this.cancel, this.sequences_size, this.startTime, this.progress, 
							i,this.sequencesCounter, this.taxonomyMap, this.uniprotStar, errorCounter, uniprotStatus, this.latencyWaitingPeriod);
					Thread thread = new Thread(lc);
					this.runnables.add(lc);
					threads.add(thread);
					logger.info("Start "+i);
					thread.start();
				}

				for(Thread thread :threads) {

					thread.join();
				}

				if(errorCounter.get()>0) {

					sequencesWithErrors += errorCounter.get();
					//Workbench.getInstance().error("Errors have ocurred while processsing "+errorCounter+" query(ies).");
					errorCounter.set(0);
					//this.similaritySearchProcessAvailable = false;
				}
			}
		}

		return sequencesWithErrors;
	}

	/**
	 * @param program
	 * @param database
	 * @param numberOfAlignments
	 * @param expectedVal
	 * @param wordSize
	 * @param requests
	 * @param matrix
	 * @param gapExtensionPenalty
	 * @param gapOpenPenalty
	 * @return
	 * @throws InterruptedException 
	 * @throws InvalidArgumentException 
	 * @throws Exception
	 */
	private int blastSingleSequenceEbi(String program, String database, int numberOfAlignments, double expectedVal, ConcurrentLinkedQueue<String> requests,
			Matrix matrix, short gapExtensionPenalty, short gapOpenPenalty) throws InterruptedException, InvalidArgumentException {

		sequencesWithErrors = 0;

		Map<String,String> queryRIDMap = new HashMap<String, String>();

		NCBIQBlastAlignmentProperties rqb = new NCBIQBlastAlignmentProperties();
		rqb.setBlastProgram(program);
		rqb.setBlastDatabase(database);
		rqb.setBlastExpectEBI(expectedVal);
		rqb.setBlastMatrix(matrix.toString().toUpperCase());

		if(gapOpenPenalty!=-1)
			rqb.setBlastGapCreation(gapOpenPenalty);

		if(gapExtensionPenalty!=-1)
			rqb.setBlastGapExtension(gapExtensionPenalty);

		rqb.setHitlistSize(numberOfAlignments);

		if(!this.cancel.get()) {

			AtomicInteger errorCounter = new AtomicInteger(0);

			int threadsNumber=0;
			int numberOfCores = Runtime.getRuntime().availableProcessors()*4;
			List<Thread> threads = new ArrayList<Thread>();
			this.runnables = new ArrayList<Runnable>();

			if(requests.size()<numberOfCores)
				threadsNumber=requests.size();
			else
				threadsNumber=numberOfCores;

			List<ConcurrentLinkedQueue<String>> rids = new ArrayList<ConcurrentLinkedQueue<String>>();
			EbiBlastClientRest[] rbwArray = new EbiBlastClientRest[threadsNumber];

			int t=0;
			while( t<threadsNumber) {

				ConcurrentLinkedQueue<String> rid = new ConcurrentLinkedQueue<String>();
				rids.add(t,rid);
				rbwArray[t] = new EbiBlastClientRest(30000, this.email);
				t++;
			}

			t=0;
			
			int serverErrors = 0;
			
			while(!requests.isEmpty() && !this.cancel.get()) {

				if(!this.similaritySearchProcessAvailable || this.cancel.get())
					requests.clear();


				String newRid = "";

				try {
					
					if(!requests.isEmpty()) {

						String query=requests.poll();

						newRid = this.processQuery(query, rbwArray[t], rqb, 0);

						if (newRid == null) {
							
							requests.add(query);
							
							serverErrors++;
							
							if(serverErrors == 3) {
								
								this.similaritySearchProcessAvailable = false;
								this.setReBlast(true);		//to restart the entire blast process
								
								MySleep.myWait(60000);
								
							}
						}
						else {
							
							rids.get(t).offer(newRid);
							queryRIDMap.put(newRid, query);
							t++;
							if (t >= threadsNumber)
								t = 0;
						}
						
					}
				}
				catch (Exception e) {

					e.printStackTrace();

				}
			}

			if(this.similaritySearchProcessAvailable  && !this.cancel.get() && queryRIDMap.size()>0) {

				for(int i=0; i<threadsNumber; i++) {

					Runnable lc	= new SubmitEbiBlast(rbwArray[i], rids.get(i), numberOfAlignments, expectedVal, this.project, queryRIDMap, rqb, 
							this.organismTaxa, this.cancel, this.sequences_size, this.startTime, this.progress, i,this.sequencesCounter, 
							this.taxonomyMap, this.uniprotStar, errorCounter, uniprotStatus, this.latencyWaitingPeriod);
					Thread thread = new Thread(lc);
					this.runnables.add(lc);
					threads.add(thread);
					logger.info("Start "+i);
					thread.start();
				}

				for(Thread thread :threads)
					thread.join();

				if(errorCounter.get()>0) {

					sequencesWithErrors += errorCounter.get();
					errorCounter.set(0);
				}
			}
		}

		return sequencesWithErrors;
	}

	/**
	 * @param sequence
	 * @param rbwArray
	 * @param rqb
	 * @param counter
	 * @return
	 */
	private String processQuery (String sequence, RemotePairwiseAlignmentService rbwArray, NCBIQBlastAlignmentProperties rqb, int counter) {

		try {

			String newRid = rbwArray.sendAlignmentRequest(sequence,rqb);

			if(newRid == null) {

				if(counter<10) {

					counter++;
					return this.processQuery(sequence, rbwArray, rqb, counter);
				}
				else {

					System.out.println("Error getting rid for sequence \t"+sequence);
					
					logger.error("Error getting rid for sequence \t"+sequence);
				}
			}
			else {

				return newRid;
			}

			// http://www.ncbi.nlm.nih.gov/staff/tao/URLAPI/new/node96.html
			// b. For URLAPI scripts, do NOT send requests faster than once every 3 seconds. 
			MySleep.myWait(3000);
		}
		catch (IOException e) {

			counter++;

			if(counter<3) {

				return this.processQuery(sequence, rbwArray, rqb, counter);
			}
			else {

				logger.warn("IO exception request for "+sequence+" Aborting.");
				
				e.printStackTrace();
				
				System.out.println("cause ----> " + e.getCause());
				
				return null;   	//davide
			}
		}
		catch (Exception e) {

			counter++;

			if(e!=null && e.getMessage()!=null && e.getMessage().contains("NCBI QBlast refused this request because")) {

				if(counter<3) {

					MySleep.myWait(3000);
					return this.processQuery(sequence, rbwArray, rqb, counter);
				}
				else {

					logger.error("NCBI QBlast refused this request for "+sequence+"\n" +
							"because: "+e.getMessage().replace("<ul id=\"msgR\" class=\"msg\"><li class=\"error\"><p class=\"error\">", "")+" Aborting.");
				}
			}
			else if(e!=null && e.getMessage()!=null && e.getMessage().contains("Cannot get RID for alignment")) {

				if(counter<3) {

					MySleep.myWait(3000);
					return this.processQuery(sequence, rbwArray, rqb, counter);
				}
				else {

					logger.warn("Cannot get RID for sequence "+sequence+"\nRetrying query! Trial\t"+counter);
				}
			}
			else {

				if(counter<3) {

					MySleep.myWait(3000);
					return this.processQuery(sequence, rbwArray, rqb, counter);
				}
				else {

					logger.error("Message: "+e.getMessage());
					//e.printStackTrace();
				}
			}

		}

		logger.error("BLAST Failed for sequence:\t"+sequence);
		sequencesWithErrors++;

		//Workbench.getInstance().error("Cannot perform BLAST at this time, try again later!");
		this.similaritySearchProcessAvailable = false;

		return null;
	}


	/**
	 * @param program
	 * @param database
	 * @param numberOfAlignments
	 * @param eVal
	 * @param eValueAutoAdjust
	 * @param word
	 * @return
	 * @throws Exception
	 */
	public int blastSequencesNCBI(String program, String database, int numberOfAlignments, double eVal, boolean eValueAutoAdjust, short word) throws Exception {

		try {

			int errorCount = 0;

			Connection conn = new Connection(this.project.getDatabase().getDatabaseAccess());
			Statement statement = conn.createStatement();
			Matrix matrix;
			int maxRequests = 50;

			if(blastMatrix==null) {

				Map<String, AbstractSequence<?>> smaller = new HashMap<>();
				Map<String, AbstractSequence<?>> pam30 = new HashMap<>();
				Map<String, AbstractSequence<?>> pam70 = new HashMap<>();
				Map<String, AbstractSequence<?>> blosum62 = new HashMap<>();
				Map<String, AbstractSequence<?>> blosum80 = new HashMap<>();

				int unitLength = 1;

				if(!program.equalsIgnoreCase("blastp") ) {

					unitLength=3;

					if(eVal==1E-30)
						eVal = 10;
				}

				for(String key:this.sequenceFile.keySet()) {

					int seqSize = this.sequenceFile.get(key).getLength()/unitLength;

					if(seqSize<15){smaller.put(key,this.sequenceFile.get(key));}
					else if(seqSize<35){pam30.put(key,this.sequenceFile.get(key));}
					else if(seqSize<50){pam70.put(key,this.sequenceFile.get(key));}
					else if(seqSize<85){blosum80.put(key,this.sequenceFile.get(key));}
					else{blosum62.put(key,this.sequenceFile.get(key));} 
				}

				if(!this.cancel.get()) {

					this.wordSize = word;
					matrix = this.selectMatrix(86);
					this.setLoadedGenes(HomologyAPI.getGenesFromDatabase(eVal, matrix.toString().toUpperCase(), numberOfAlignments, this.wordSize, program, database, true, statement));
					this.setBlosum62(this.getRequestsList(blosum62));

					if(this.blosum62.size()>0 && !this.cancel.get())
						errorCount += this.blastProcessGenesListNcbi(this.blosum62, program, database, numberOfAlignments, eVal, matrix, maxRequests);
				}

				if(!this.cancel.get()) {

					this.wordSize = word;
					matrix = this.selectMatrix(80);
					this.setLoadedGenes(HomologyAPI.getGenesFromDatabase(eVal, matrix.toString().toUpperCase(), numberOfAlignments, this.wordSize, program,  database, true, statement));
					this.setBlosum80(this.getRequestsList(blosum80));

					if(this.blosum80.size()>0 && !this.cancel.get())
						errorCount +=	this.blastProcessGenesListNcbi(this.blosum80,program, database, numberOfAlignments, eVal, matrix, maxRequests);
				}

				if(!this.cancel.get()) {

					this.wordSize = word;
					matrix = this.selectMatrix(40);
					this.setLoadedGenes(HomologyAPI.getGenesFromDatabase(eVal, matrix.toString().toUpperCase(), numberOfAlignments, this.wordSize, program,  database, true, statement));
					this.setPam70(this.getRequestsList(pam70));

					if(this.pam70.size()>0 && !this.cancel.get())
						errorCount +=this.blastProcessGenesListNcbi(this.pam70,program, database, numberOfAlignments, eVal, matrix, maxRequests);
				}


				if(!this.cancel.get()) {

					double newEval = eVal;
					if (eValueAutoAdjust) {

						newEval = 200000;
						logger.info("Setting e-value to "+200000+" for <35mer sequences.");
					}
					this.wordSize = word;
					matrix= this.selectMatrix(30);
					this.setLoadedGenes(HomologyAPI.getGenesFromDatabase(eVal, matrix.toString().toUpperCase(), numberOfAlignments, this.wordSize, program,  database, true, statement));
					this.setPam30(this.getRequestsList(pam30));
					if(this.pam30.size()>0 && !this.cancel.get())
						errorCount += this.blastProcessGenesListNcbi(this.pam30,program, database, numberOfAlignments, newEval, matrix, maxRequests);
				}

				if(!this.cancel.get()) {

					double newEval = eVal;
					if (eValueAutoAdjust) {

						newEval = 200000;
						logger.info("Setting e-value to "+newEval+" for 10-15mer or shorter sequences.");
					}
					this.wordSize = word;
					matrix= this.selectMatrix(15);
					this.setLoadedGenes(HomologyAPI.getGenesFromDatabase(eVal, matrix.toString().toUpperCase(), numberOfAlignments, this.wordSize, program,  database, true, statement));
					this.setSmaller(this.getRequestsList(smaller));

					if(this.smaller.size()>0 && !this.cancel.get())
						errorCount += this.blastProcessGenesListNcbi(this.smaller,program, database, numberOfAlignments, newEval, matrix, maxRequests);
				}
			}
			else {

				Map<String, AbstractSequence<?>> query = new HashMap<>();

				this.wordSize = word;

				for(String key:this.sequenceFile.keySet())
					query.put(key,this.sequenceFile.get(key));

				this.setOtherSequences(this.getRequestsList(query));

				if(this.wordSize == -1)
					this.wordSize=3;

				if(!this.cancel.get())
					errorCount += this.blastProcessGenesListNcbi(this.otherSequences,program, database, numberOfAlignments, eVal, this.blastMatrix, maxRequests);
			}

			HomologyAPI.deleteSetOfGenes(HomologyAPI.getProcessingGenes(program, statement), statement);
			conn.closeConnection();

			return errorCount;
		}
		catch(Exception e){e.printStackTrace();return -1;}
	}

	/**
	 * @param program
	 * @param database
	 * @param numberOfAlignments
	 * @param eVal
	 * @param eValueAutoAdjust
	 * @param sequenceType
	 * @return
	 * @throws SQLException 
	 * @throws InterruptedException 
	 * @throws InvalidArgumentException 
	 * @throws Exception
	 */
	public int blastSequencesEBI(String program, String database, int numberOfAlignments, double eVal, boolean eValueAutoAdjust, String sequenceType) throws SQLException, InvalidArgumentException, InterruptedException {

			Connection conn = new Connection(this.project.getDatabase().getDatabaseAccess());
			Statement statement = conn.createStatement();
			
			int errorCount = 0;
		
			Matrix matrix;
			int maxRequests = 50;

			if(blastMatrix==null) {

				Map<String, AbstractSequence<?>> smaller = new HashMap<>();
				Map<String, AbstractSequence<?>> pam30 = new HashMap<>();
				Map<String, AbstractSequence<?>> pam70 = new HashMap<>();
				Map<String, AbstractSequence<?>> blosum62 = new HashMap<>();
				Map<String, AbstractSequence<?>> blosum80 = new HashMap<>();

				int unitLength = 1;

				if(!program.equalsIgnoreCase("blastp") ) {

					unitLength=3;

					if(eVal==1E-10)
						eVal = 10;
				}
				
				for(String key:this.sequenceFile.keySet()) {

					int seqSize = this.sequenceFile.get(key).getLength()/unitLength;

					if(seqSize<15)
						smaller.put(key,this.sequenceFile.get(key));
					else if(seqSize<35)
						pam30.put(key,this.sequenceFile.get(key));
					else if(seqSize<50)
						pam70.put(key,this.sequenceFile.get(key));
					else if(seqSize<85)
						blosum80.put(key,this.sequenceFile.get(key));
					else
						blosum62.put(key,this.sequenceFile.get(key)); 
				}

				if(!this.cancel.get()) {

					matrix = this.selectMatrix(86);
					
					statement = HomologyAPI.checkStatement(this.project.getDatabase().getDatabaseAccess(), statement);
					
					this.setLoadedGenes(HomologyAPI.getGenesFromDatabase(eVal, matrix.toString().toUpperCase(), numberOfAlignments, this.wordSize, program,  database, true, statement));
					this.setBlosum62(this.getRequestsList(blosum62));

					if(this.blosum62.size()>0 && !this.cancel.get())
						errorCount += this.blastProcessGenesListEbi(this.blosum62, program, database, numberOfAlignments, eVal, matrix, maxRequests);
				}

				if(!this.cancel.get()) {

					matrix = this.selectMatrix(80);
					
					statement = HomologyAPI.checkStatement(this.project.getDatabase().getDatabaseAccess(), statement);
					
					this.setLoadedGenes(HomologyAPI.getGenesFromDatabase(eVal, matrix.toString().toUpperCase(), numberOfAlignments, this.wordSize, program,  database, true, statement));
					this.setBlosum80(this.getRequestsList(blosum80));

					if(this.blosum80.size()>0 && !this.cancel.get())
						errorCount +=	this.blastProcessGenesListEbi(this.blosum80,program, database, numberOfAlignments, eVal, matrix, maxRequests);
				}

				if(!this.cancel.get()) {

					matrix = this.selectMatrix(40);
					
					statement = HomologyAPI.checkStatement(this.project.getDatabase().getDatabaseAccess(), statement);
					
					this.setLoadedGenes(HomologyAPI.getGenesFromDatabase(eVal, matrix.toString().toUpperCase(), numberOfAlignments, this.wordSize, program,  database, true, statement));
					this.setPam70(this.getRequestsList(pam70));

					if(this.pam70.size()>0 && !this.cancel.get())
						errorCount +=this.blastProcessGenesListEbi(this.pam70,program, database, numberOfAlignments, eVal, matrix, maxRequests);
				}


				if(!this.cancel.get()) {

					double newEval = eVal;
					if (eValueAutoAdjust) {

						newEval = 1000;
						logger.info("Setting e-value to "+1000+" for <35mer sequences.");
					}
					matrix= this.selectMatrix(30);
					
					statement = HomologyAPI.checkStatement(this.project.getDatabase().getDatabaseAccess(), statement);
					
					this.setLoadedGenes(HomologyAPI.getGenesFromDatabase(eVal, matrix.toString().toUpperCase(), numberOfAlignments, this.wordSize, program,  database, true, statement));
					this.setPam30(this.getRequestsList(pam30));
					if(this.pam30.size()>0 && !this.cancel.get())
						errorCount += this.blastProcessGenesListEbi(this.pam30, program, database, numberOfAlignments, newEval, matrix, maxRequests);
				}

				if(!this.cancel.get()) {

					double newEval = eVal;
					if (eValueAutoAdjust) {

						newEval = 1000;
						logger.info("Setting e-value to "+newEval+" for 10-15mer or shorter sequences.");
					}
					matrix= this.selectMatrix(15);
					
					statement = HomologyAPI.checkStatement(this.project.getDatabase().getDatabaseAccess(), statement);
					
					this.setLoadedGenes(HomologyAPI.getGenesFromDatabase(eVal, matrix.toString().toUpperCase(), numberOfAlignments, this.wordSize, program,  database, true, statement));
					this.setSmaller(this.getRequestsList(smaller));

					if(this.smaller.size()>0 && !this.cancel.get())
						errorCount += this.blastProcessGenesListEbi(this.smaller,program, database, numberOfAlignments, newEval, matrix, maxRequests);
				}
			}
			else {

				Map<String, AbstractSequence<?>> query = new HashMap<>();

				for(String key:this.sequenceFile.keySet())
					query.put(key,this.sequenceFile.get(key));

				this.setOtherSequences(this.getRequestsList(query));

				if(this.wordSize == -1)
					this.wordSize=3;

				if(!this.cancel.get())
					errorCount += this.blastProcessGenesListEbi(this.otherSequences,program, database, numberOfAlignments, eVal, this.blastMatrix, maxRequests);
			}
			
			statement = HomologyAPI.checkStatement(this.project.getDatabase().getDatabaseAccess(), statement);
				
			HomologyAPI.deleteSetOfGenes(HomologyAPI.getProcessingGenes(program, statement), statement);

			conn.closeConnection();

			return errorCount;
	}



	/**
	 * @param list
	 * @param program
	 * @param database
	 * @param numberOfAlignments
	 * @param eVal
	 * @param matrix
	 * @param maxRequests
	 * @throws Exception 
	 */
	private int blastProcessGenesListNcbi(ConcurrentLinkedQueue<String> list, String program, String database, int numberOfAlignments, double eVal, Matrix matrix, int maxRequests) throws Exception  {

		int errorCount = 0;
		int requests = 0;
		logger.info(matrix+" size "+list.size());
		ConcurrentLinkedQueue<String> sequencesSubmited = new ConcurrentLinkedQueue<String>();

		while(list.size()>0) {

			if(this.cancel.get()) {

				list.clear();
			}
			else {

				sequencesSubmited.add(list.poll());
				requests++;

				if(requests>maxRequests) {

					errorCount += this.blastSingleSequenceNcbi(program, database, numberOfAlignments, eVal, this.wordSize, sequencesSubmited, matrix, this.gapExtensionPenalty, this.gapOpenPenalty);
					sequencesSubmited = new ConcurrentLinkedQueue<String>();
					requests = 0;
				}
			}
		}

		if(sequencesSubmited.size()>0 && !this.cancel.get()) {

			errorCount += this.blastSingleSequenceNcbi(program, database, numberOfAlignments, eVal, this.wordSize, sequencesSubmited, matrix, this.gapExtensionPenalty, this.gapOpenPenalty);
		}

		return errorCount;
	}

	/**
	 * @param list
	 * @param program
	 * @param database
	 * @param numberOfAlignments
	 * @param eVal
	 * @param matrix
	 * @param maxRequests
	 * @return
	 * @throws InterruptedException 
	 * @throws InvalidArgumentException 
	 * @throws Exception
	 */
	private int blastProcessGenesListEbi(ConcurrentLinkedQueue<String> list, String program, String database, int numberOfAlignments, double eVal, Matrix matrix, int maxRequests) throws InvalidArgumentException, InterruptedException  {

		int errorCount = 0;
		int requests = 0;
		logger.info(matrix+" size "+list.size());
		ConcurrentLinkedQueue<String> sequencesSubmited = new ConcurrentLinkedQueue<String>();

		while(list.size()>0) {

			if(this.cancel.get()) {

				list.clear();
			}
			else {

				sequencesSubmited.add(list.poll());
				requests++;

				if(requests>maxRequests) {

					errorCount += this.blastSingleSequenceEbi(program, database, numberOfAlignments, eVal, sequencesSubmited, matrix, this.gapExtensionPenalty, this.gapOpenPenalty);
					sequencesSubmited = new ConcurrentLinkedQueue<String>();
					requests = 0;
				}
			}
		}

		if(sequencesSubmited.size()>0 && !this.cancel.get()) {

			errorCount += this.blastSingleSequenceEbi(program, database, numberOfAlignments, eVal, sequencesSubmited, matrix, this.gapExtensionPenalty, this.gapOpenPenalty);
		}

		return errorCount;
	}

	/**
	 * @param seqLength
	 * @return
	 */
	private Matrix selectMatrix(int seqLength) {

		if(seqLength<16) {

			if(this.wordSize==-1)
				this.wordSize=2; 

			return Matrix.PAM30;
		}
		if(seqLength<35) {

			if(this.wordSize==-1)
				this.wordSize=2; 

			return Matrix.PAM30;
		}
		else if(seqLength<50) {

			if(this.wordSize==-1)
				this.wordSize=3; 

			return Matrix.PAM70;
		}
		else if(seqLength<85) {

			if(this.wordSize==-1)
				this.wordSize=3;
			
			return Matrix.BLOSUM80;
		}
		else {

			if(this.wordSize==-1)
				this.wordSize=3; 

			return Matrix.BLOSUM62;
		}
	}


	/**
	 * @param database
	 * @param numberOfAlignments
	 * @param eVal
	 * @param uniprotStatus
	 * @return
	 */
	public int hmmerSearchSequences(HmmerRemoteDatabasesEnum database, int numberOfAlignments, double eVal, boolean uniprotStatus) {

		int errorCount = 0;

		try {

			Connection conn = new Connection(this.project.getDatabase().getDatabaseAccess());
			Statement statement = conn.createStatement();
			int maxRequests = Runtime.getRuntime().availableProcessors()*2*10;
			this.setLoadedGenes(HomologyAPI.getGenesFromDatabase(eVal, null, numberOfAlignments, (short) -1, "hmmer",  database.toString(), true, statement));
			conn.closeConnection();
			
			Map<String, ProteinSequence> query = new HashMap<String, ProteinSequence>();
			this.uniprotStatus = uniprotStatus;

			for(String key:this.sequenceFile.keySet())
				if(!(this.getLoadedGenes()!=null && this.getLoadedGenes().contains(key)))
					query.put(key, new ProteinSequence(this.sequenceFile.get(key).getSequenceAsString()));

			errorCount += this.hmmerProcessGenesList(query, database, numberOfAlignments, eVal, maxRequests);
		}
		catch(Exception e){e.printStackTrace();return -1;}
		return errorCount;
	}


	/**
	 * @param list
	 * @param database
	 * @param numberOfAlignments
	 * @param eVal
	 * @param maxRequests
	 * @throws Exception
	 */
	private int hmmerProcessGenesList(Map<String, ProteinSequence> query, HmmerRemoteDatabasesEnum database, int numberOfAlignments, double eVal, int maxRequests) throws Exception{

		int errorCount = 0;
		int requests = 0;
		ConcurrentLinkedQueue<String> list = new ConcurrentLinkedQueue<String>(query.keySet());
		logger.info("HMMER size "+list.size());
		this.sequences_size = new Integer(list.size());
		ConcurrentLinkedQueue<String> sequencesSubmited = new ConcurrentLinkedQueue<String>();

		while(list.size()>0 && !this.cancel.get()) {

			if(this.cancel.get()) {

				list.clear();
			}
			else {

				sequencesSubmited.add(list.poll());
				requests++;

				if(requests>maxRequests) {

					errorCount += this.hmmerSearchSingleSequence(database, numberOfAlignments, eVal, sequencesSubmited, query);
					sequencesSubmited = new ConcurrentLinkedQueue<String>();
					requests = 0;
				}
			}
		}
		if(sequencesSubmited.size()> 0 && !this.cancel.get()) {

			errorCount += this.hmmerSearchSingleSequence(database, numberOfAlignments, eVal, sequencesSubmited, query);
		}

		return errorCount;
	}

	/**
	 * @param database
	 * @param numberOfAlignments
	 * @param expectedVal
	 * @param requests
	 * @param query
	 * @throws InterruptedException
	 */
	private int hmmerSearchSingleSequence(HmmerRemoteDatabasesEnum database, int numberOfAlignments,
			double expectedVal, ConcurrentLinkedQueue<String> requests, Map<String, ProteinSequence> query) throws InterruptedException {

		sequencesWithErrors = 0;

		if(!this.cancel.get()) {

			int threadsNumber=0;
			int numberOfCores = Runtime.getRuntime().availableProcessors()*2;
			List<Thread> threads = new ArrayList<Thread>();
			this.runnables = new ArrayList<Runnable>();
			if(requests.size()<numberOfCores){threadsNumber=requests.size();}
			else{threadsNumber=numberOfCores;}
			AtomicInteger errorCounter = new AtomicInteger(0);
			AtomicLong time = new AtomicLong(System.currentTimeMillis());

			if(this.similaritySearchProcessAvailable  && !this.cancel.get() && query.size()>0) {

				ConcurrentLinkedQueue<String> currentlyBeingProcessed = new ConcurrentLinkedQueue<String>();
				for(int i=0; i<threadsNumber; i++) {

					Runnable lc	= new SubmitHMMER(requests, query, expectedVal, numberOfAlignments, this.project, 
							this.organismTaxa, database, this.cancel, this.sequences_size, this.startTime, this.progress, currentlyBeingProcessed,
							this.sequencesCounter, this.taxonomyMap, this.uniprotStar, errorCounter, uniprotStatus, time, this.latencyWaitingPeriod);
					//((SubmitHMMER) lc).setEmail(this.email);
					Thread thread = new Thread(lc);
					this.runnables.add(lc);
					threads.add(thread);
					logger.info("Start "+i);
					thread.start();
				}

				for(Thread thread :threads) {

					thread.join();
				}

				if(errorCounter.get()>0) {

					sequencesWithErrors += errorCounter.get();
					//Workbench.getInstance().error("Errors have ocurred while processsing "+errorCounter+" query(ies).");
					errorCounter.set(0);
					//this.similaritySearchProcessAvailable = false;
				}

			}
		}

		return sequencesWithErrors;
	}

	/**
	 * @param blosum62 the blosum62 to set
	 */
	public void setBlosum62(ConcurrentLinkedQueue<String> blosum62) {
		this.blosum62 = blosum62;
	}

	/**
	 * @return the blosum62
	 */
	public ConcurrentLinkedQueue<String> getBlosum62() {
		return blosum62;
	}

	/**
	 * @param blosum80 the blosum80 to set
	 */
	public void setBlosum80(ConcurrentLinkedQueue<String> blosum80) {
		this.blosum80 = blosum80;
	}

	/**
	 * @return the blosum80
	 */
	public ConcurrentLinkedQueue<String> getBlosum80() {
		return blosum80;
	}

	/**
	 * @param pam30 the pam30 to set
	 */
	public void setPam30(ConcurrentLinkedQueue<String> pam30) {
		this.pam30 = pam30;
	}

	/**
	 * @return the pam30
	 */
	public ConcurrentLinkedQueue<String> getPam30() {
		return pam30;
	}

	/**
	 * @param pam70 the pam70 to set
	 */
	public void setPam70(ConcurrentLinkedQueue<String> pam70) {
		this.pam70 = pam70;
	}

	/**
	 * @return the pam70
	 */
	public ConcurrentLinkedQueue<String> getPam70() {
		return pam70;
	}

	/**
	 * @return
	 */
	public Set<String> getLoadedGenes() {
		return loadedGenes;
	}

	/**
	 * @param loadedGenes
	 */
	public void setLoadedGenes(Set<String> loadedGenes) {
		this.loadedGenes = loadedGenes;
	}

	

	/**
	 * @param orgID
	 * @return
	 * @throws Exception 
	 */
	public String[] ebiNewTaxID(int orgID) throws Exception {

		try {

			String[] newTax = UniProtAPI.newTaxID(orgID, 0);
			
			if(newTax == null)
				Workbench.getInstance().error("Service unavailable. Please check your internet connection.");
			
			return newTax;
		}
			
		catch (Error e) {
			
			e.printStackTrace();
			
			Workbench.getInstance().error("Service unavailable. Please check your internet connection.");
			
			throw new Error("Service unavailable");
			
		}
		catch (Exception e) {
			
			Workbench.getInstance().error("Service unavailable. Please check your internet connection.");

			this.similaritySearchProcessAvailable = false;
			throw e;
		}
	}

	/**
	 * @return the smaller
	 */
	public ConcurrentLinkedQueue<String> getSmaller() {
		return smaller;
	}

	/**
	 * @param smaller the smaller to set
	 */
	public void setSmaller(ConcurrentLinkedQueue<String> smaller) {
		this.smaller = smaller;
	}

	public short getGapExtensionPenalty() {
		return gapExtensionPenalty;
	}

	public void setGapExtensionPenalty(short gapExtensionPenalty) {
		this.gapExtensionPenalty = gapExtensionPenalty;
	}

	public short getGapOpenPenalty() {
		return gapOpenPenalty;
	}

	public void setGapOpenPenalty(short gapOpenPenalty) {
		this.gapOpenPenalty = gapOpenPenalty;
	}

	public short getWordSize() {
		return wordSize;
	}

	public void setWordSize(short wordSize) {
		this.wordSize = wordSize;
	}

	/**
	 * @return the organismTaxa
	 */
	public String[] getOrgArray() {
		return organismTaxa;
	}

	/**
	 * @param organismTaxa the organismTaxa to set
	 */
	public void setOrgArray(String[] orgArray) {
		this.organismTaxa = orgArray;
	}

	/**
	 * @return the blastMatrix
	 */
	public Matrix getBlastMatrix() {
		return blastMatrix;
	}

	/**
	 * @param blastMatrix the blastMatrix to set
	 */
	public void setBlastMatrix(Matrix blastMatrix) {
		this.blastMatrix = blastMatrix;
	}

	/**
	 * @return the otherSequences
	 */
	public ConcurrentLinkedQueue<String> getOtherSequences() {
		return otherSequences;
	}

	/**
	 * @param otherSequences the otherSequences to set
	 */
	public void setOtherSequences(ConcurrentLinkedQueue<String> otherSequences) {
		this.otherSequences = otherSequences;
	}

	/**
	 * @return the organism
	 */
	public String getOrganism() {
		return organism;
	}

	/**
	 * @param organism the organism to set
	 */
	public void setOrganism(String organism) {
		this.organism = organism;
	}

	/**
	 * @return the cancel
	 */
	public AtomicBoolean isCancel() {
		return cancel;
	}

	/**
	 * 
	 */
	public void setCancel() {

		this.cancel.set(true);
		for(Runnable lc :this.runnables) {

			if(lc.getClass().equals(SubmitNcbiBlast.class)) {

			//	((SubmitNcbiBlast) lc).setCancel(this.cancel);
			}
			else if(lc.getClass().equals(SubmitEbiBlast.class)) {

				((SubmitEbiBlast) lc).setCancel(this.cancel);
			}
			else {

				((SubmitHMMER) lc).setCancel(this.cancel);
			}
		}
	}

	/**
	 * @param progress
	 */
	public void setTimeLeftProgress(TimeLeftProgress progress) {
		this.progress = progress;		
	}

	/**
	 * @return
	 */
	public boolean removeDuplicates() {

		boolean result = false;
		Set<String> duplicateQueries = new HashSet<String>();

		try {

			Connection conn = new Connection(this.project.getDatabase().getDatabaseAccess());

			Statement statement = conn.createStatement();

			ArrayList<String> querys = ModelAPI.getDuplicatedQuerys(statement);
			
			for(int i=0; i < querys.size(); i++){

				duplicateQueries.add(querys.get(i));
				result = true;
			}

			for(String query : duplicateQueries) {
				
				ModelAPI.deleteDuplicatedQuerys(statement, query);
			}
		}
		catch (SQLException e) {

			result = false;
			e.printStackTrace();
		}
		return result;
	}

	public void setGeneticCode(int geneticCode) {

		this.geneticCode = geneticCode;		
	}

	/**
	 * @param uniprotStatus
	 */
	public void setRetrieveUniprotStatus(boolean uniprotStatus) {

		this.uniprotStatus = uniprotStatus;
	}


	public long getLatencyWaitingPeriod() {
		return latencyWaitingPeriod;
	}

	public void setLatencyWaitingPeriod(long latencyWaitingPeriod) {
		this.latencyWaitingPeriod = latencyWaitingPeriod;
	}


	/**
	 * @return the reBlast
	 */
	public boolean isReBlast() {
		return reBlast;
	}


	/**
	 * @param reBlast the reBlast to set
	 */
	public void setReBlast(boolean reBlast) {
		this.reBlast = reBlast;
	}


	/**
	 * @return the similaritySearchProcessAvailable
	 */
	public boolean isSimilaritySearchProcessAvailable() {
		return similaritySearchProcessAvailable;
	}


	/**
	 * @param similaritySearchProcessAvailable the similaritySearchProcessAvailable to set
	 */
	public void setSimilaritySearchProcessAvailable(boolean similaritySearchProcessAvailable) {
		this.similaritySearchProcessAvailable = similaritySearchProcessAvailable;
	}


	/**
	 * @return the sequences_size
	 */
	public int getSequences_size() {
		return sequences_size;
	}


	/**
	 * @param sequences_size the sequences_size to set
	 */
	public void setSequences_size(int sequences_size) {
		this.sequences_size = sequences_size;
	}

}