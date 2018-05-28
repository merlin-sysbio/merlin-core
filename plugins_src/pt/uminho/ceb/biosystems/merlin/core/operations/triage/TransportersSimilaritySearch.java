package pt.uminho.ceb.biosystems.merlin.core.operations.triage;

import java.io.File;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.biojava.nbio.core.sequence.template.AbstractSequence;

import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ebi.EbiAPI;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.NcbiAPI;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.Enumerators.FileExtensions;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.TransportersAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.local.alignments.core.RunSimilaritySearch;
import pt.uminho.ceb.biosystems.merlin.transporters.core.utils.TransportersUtilities;
import pt.uminho.ceb.biosystems.merlin.utilities.Enumerators.AlignmentScoreType;
import pt.uminho.ceb.biosystems.merlin.utilities.Enumerators.Method;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.capsules.AlignmentCapsule;

/**
 * @author ODias
 *
 */
@Operation(name="Transporters Identification",description="Perform a semi-automatic identication of the transporter systems encoded in the genome. This process may take several hours, depending on the user's local computer processing unit.")
public class TransportersSimilaritySearch implements Observer {

	private static final int _LIMIT = 5;

	private static  AlignmentScoreType _SCORE_TYPE =  AlignmentScoreType.ALIGNMENT;

	private Project project;
	private String tcdb_url;
	private  int minimum_number_of_helices;
	private  double similarity_threshold;
	private Method method;
	private Map<String, AbstractSequence<?>> sequences;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private AtomicBoolean cancel;
	private AtomicInteger querySize;
	private AtomicInteger counter = new AtomicInteger(0);
	private long startTime;
	private boolean isPhobius;
	private Map<String, Integer> transmembraneGenes;
	private AtomicInteger errorCounter;
	private String message;

	private String email;


	/**
	 * @param tcdb_url the tcdb_url to set
	 */
	@Port(direction=Direction.INPUT, name="TCDB URL",description="URL for TCDB sequences fasta file",defaultValue="http://www.tcdb.org/public/tcdb",order=1)
	public void setTcdb_url(String tcdb_url) {
		this.tcdb_url = tcdb_url;
	}

	/**
	 * @param isPhobius
	 */
	@Port(direction=Direction.INPUT, name="Use phobius.",description="Use phobius, else load TMHMM reports.",defaultValue="true",validateMethod="checkPhobius",order=2)
	public void usePhobius(boolean isPhobius) {

		this.isPhobius = isPhobius;
	}

	@Port(direction=Direction.INPUT, name="email",description="user email address", validateMethod="checkEmail", order=3)
	public void setEmail(String email) {

	}

	/**
	 * @param tmhmm_file_dir the tmhmm_file_dir to set
	 */
	@Port(direction=Direction.INPUT, name="TMHMM Files",description="Path to TMHMM files directory",defaultValue="files path",validateMethod="checkTMHMM",order=4)
	public void setTmhmm_file_dir(File tmhmm_file_dir) {
	}

	/**
	 * @param minimum_number_of_helices the minimum_number_of_helices to set
	 */
	@Port(direction=Direction.INPUT, name="Minimum number of helices",description="Minimum number of helices on gene sequence",defaultValue="1",validateMethod="checkMNOH",order=5)
	public void setMinimum_number_of_helices(int minimum_number_of_helices) {
		this.minimum_number_of_helices = minimum_number_of_helices;
	}

	/**
	 * @param similarity_threshold the similarity_threshold to set
	 */
	@Port(direction=Direction.INPUT, name="Similarity threshold",description="Initial threshold for similarity to TCDB gene",defaultValue="0.1",validateMethod="checkSimilarityThreshold",order=6)
	public void setSimilarity_threshold(double similarity_threshold) {
		this.similarity_threshold = similarity_threshold;
	}

	/**
	 * @param method the method to set
	 */
	@Port(direction=Direction.INPUT, name="Method",description="Method for alignment",order=7)
	public void setMethod(Method method) {

		this.method = method;
	}


	/**
	 * @param project
	 */
	@Port(direction=Direction.INPUT, name="Workspace",description="Select Workspace",validateMethod="checkProject",order=8)
	public void setProject(Project project) {

		long waitingPeriod = 300000; //wait for 300 seconds for response to single query on phobius

		this.project = project;
		int project_id = this.project.getProjectID();

		this.cancel = new AtomicBoolean();
		this.startTime = GregorianCalendar.getInstance().getTimeInMillis();
		this.errorCounter = new AtomicInteger(0);
		this.transmembraneGenes = new HashMap<>();
		
		ConcurrentHashMap<String, AbstractSequence<?>> all_sequences = new ConcurrentHashMap<>();
		
		try {
			
			Connection connection = new Connection(project.getDatabase().getDatabaseAccess());
			
			Statement statement = connection.createStatement();
			
			if(this.cancel.get())
				Workbench.getInstance().warn("Transport candidates search cancelled!");

			message = "Alignments";

			if(this.isPhobius) {

				message = "Stage 1: Phobius ";

				transmembraneGenes.putAll(TransportersAPI.getGenesTransmembraneHelices(connection, project.getProjectID()));
				Map<String, AbstractSequence<?>> queries = new HashMap<>(this.sequences);
				queries.keySet().removeAll(transmembraneGenes.keySet());

				this.querySize = new AtomicInteger(queries.size());
				List<Map<String, AbstractSequence<?>>> subMaps = new ArrayList<>();
				Map <String, AbstractSequence<?>> map = new HashMap<>();
				
				if(this.cancel.get())
					Workbench.getInstance().warn("Transport candidates search cancelled!");

				for(Entry<String, AbstractSequence<?>> entry : queries.entrySet()) {

					map.put(entry.getKey(), entry.getValue());

					if(map.size()==250) {

						subMaps.add(map);
						map = new HashMap<>();
					}
				}

				if(!map.isEmpty())
					subMaps.add(map);
				
				if(this.cancel.get())
					Workbench.getInstance().warn("Transport candidates search cancelled!");

				for(Map<String, AbstractSequence<?>> subMap : subMaps) {

					EbiAPI ebiAPI = new EbiAPI();
					ebiAPI.addObserver(this);
					Map<String, Integer> result = ebiAPI.getHelicesFromPhobius(subMap, this.errorCounter, this.cancel, waitingPeriod, this.counter, this.email);
					TransportersAPI.loadTransmembraneHelicesMap(result, project.getProjectID(), connection);
					transmembraneGenes.putAll(result);
				}
			}
			if(this.cancel.get())
				Workbench.getInstance().warn("Transport candidates search cancelled!");
				
			message = "Stage two: Alignments ";
			
			for (String id : this.sequences.keySet()){
				if(transmembraneGenes.containsKey(id)){
					all_sequences.put(id,this.sequences.get(id));
				}
			}

			this.counter = new AtomicInteger(1);
			this.querySize = new AtomicInteger(all_sequences.size());

			this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.counter.get(), this.querySize.get(), message);
			
			if(this.cancel.get())
				Workbench.getInstance().warn("Transport candidates search cancelled!");

			all_sequences.keySet().removeAll(TransportersAPI.retrieveProcessingTransportAlignmentGenes(statement));

			all_sequences = TransportersUtilities.filterTransmembraneGenes(all_sequences, transmembraneGenes, minimum_number_of_helices, project_id, statement);

			Map<String, Double> querySpecificThreshold = TransportersUtilities.getHelicesSpecificThresholds(transmembraneGenes, minimum_number_of_helices, _LIMIT);
			
			RunSimilaritySearch run_smith_waterman = new RunSimilaritySearch(TransportersUtilities.convertTcdbToMap(this.tcdb_url), this.similarity_threshold,
					this.method, all_sequences, this.cancel, this.querySize, this.counter, _SCORE_TYPE);
			
			run_smith_waterman.addObserver(this);

			ConcurrentLinkedQueue<AlignmentCapsule> results = null;
			
			if(all_sequences.keySet().size()>0)
				results = run_smith_waterman.runTransportSearch(querySpecificThreshold);
			else
				Workbench.getInstance().warn("Transporter candidates already processed.");

			message = "Stage three: Loading ";
			
			Map<String, Integer> genesIDs  = TransportersAPI.getTransportAlignmentGenes(statement);
			
			if(results!= null){

				for (AlignmentCapsule alignmentContainer : results)
					TransportersAPI.loadTransportInfo(alignmentContainer, genesIDs.get(alignmentContainer.getQuery()), statement);
			}
			
			for(String query: all_sequences.keySet())
				TransportersAPI.setProcessed(genesIDs.get(query), "PROCESSED", statement);

			if(this.cancel.get()) {

				Workbench.getInstance().warn("Transport candidates search cancelled!");
			}
			else {

				MerlinUtils.updateTransportersAnnotationView(project.getName());
				Workbench.getInstance().info("Transporter candidates search performed.");
			}
		}
		catch (Exception e) {

			if(errorCounter.get()>0)
				Workbench.getInstance().error("Errors occurred in "+errorCounter.get()+"sequnces. Please try again.");
			else
				Workbench.getInstance().error("An error occurred while performing the Similarity search.\n" +
						" Please try again later.");

			e.printStackTrace();
		}
	}

	/**
	 * @param project
	 */
	public void checkProject(Project project) {

		if(project == null) {

			throw new IllegalArgumentException("No Project Selected!");
		}
		else {

			this.project = project;
			String dbName = project.getDatabase().getDatabaseName();
			Long taxID = project.getTaxonomyID();

			if(!Project.isFaaFiles(dbName,taxID) && !Project.isFnaFiles(dbName,taxID)) {

				throw new IllegalArgumentException("Please set the project fasta files!");
			}
			else if(project.getTaxonomyID()<0) {

				throw new IllegalArgumentException("Please enter the taxonomic identification from NCBI taxonomy.");
			}
			else {

				try {

					this.sequences = CreateGenomeFile.getGenomeFromID(this.project.getDatabase().getDatabaseName(), this.project.getTaxonomyID(), FileExtensions.PROTEIN_FAA);

					if(this.sequences==null)
						throw new IllegalArgumentException("Please set the project fasta files!");
				} 
				catch (Exception e) {

					e.printStackTrace();
					throw new IllegalArgumentException("Please set the project fasta files!");
				}
			}

			if(!Project.isFaaFiles(dbName,taxID)) {

				throw new IllegalArgumentException("Please add 'faa' files to perform the transporters identification.");
			}
		}
	}

	/**
	 * @param isPhobius
	 */
	public void checkPhobius(boolean isPhobius) {

		this.isPhobius = isPhobius;
	}

	/**
	 * @param contents
	 */
	public void checkEmail(String email) {

		try {

			InternetAddress emailAddr = new InternetAddress(email);
			emailAddr.validate();
			this.email = email;
		} 
		catch (AddressException ex) {

			throw new IllegalArgumentException ("Please set a valid email address!");
		}
	}

	/**
	 * @param tmhmm_file_dir
	 */
	public void checkTMHMM(File tmhmm_file_dir) {

		if(!this.isPhobius) {

			if(tmhmm_file_dir == null || tmhmm_file_dir.toString().isEmpty()) {

				throw new IllegalArgumentException("TMHMM files directory not set!");
			}
			else {

				if(!tmhmm_file_dir.isDirectory())
					tmhmm_file_dir = new File(tmhmm_file_dir.getParent().toString());

				List<File> tmhmmFiles = new ArrayList<File>();
				for(File f: tmhmm_file_dir.listFiles())
					if(f.getName().toLowerCase().contains(".tmhmm"))
						tmhmmFiles.add(f);

				if(tmhmmFiles.isEmpty()) {

					throw new IllegalArgumentException("Please Select a directory with TMHMM files!");
				}
				else{

					transmembraneGenes = new ConcurrentHashMap<String, Integer>();

					for(File tmhmm_file:tmhmmFiles)

						if(tmhmm_file.isFile()) {

							try {

								transmembraneGenes.putAll(NcbiAPI.readTMHMMGenbank(tmhmm_file, 0));
							} 
							catch (Exception e) {

								throw new IllegalArgumentException ("Verify tmhmm files path!");
							}
						}

					if(transmembraneGenes.size()==0)
						throw new IllegalArgumentException ("Verify tmhmm files path!");
				}
			}
		}
	}

	/**
	 * @param minimum_number_of_helices
	 */
	public void checkMNOH(int minimum_number_of_helices) {

		if(minimum_number_of_helices<0) {

			throw new IllegalArgumentException("The minimum number of helices should be higher than 1!");
		}
		else {

			this.minimum_number_of_helices=minimum_number_of_helices;
		}
	}

	/**
	 * @param similarity_threshold
	 */
	public void checkSimilarityThreshold(double similarity_threshold) {

		if(similarity_threshold>1 || similarity_threshold<0) {

			throw new IllegalArgumentException("Please set a valid threshold (0<threshold<1).");
		}
		else {

			this.similarity_threshold = similarity_threshold;
		}
	}

	/**
	 * @return the progress
	 */
	@Progress
	public TimeLeftProgress getProgress() {

		return progress;
	}

	/**
	 * @param cancel the cancel to set
	 */
	@Cancel
	public void setCancel() {

		progress.setTime(0, 0, 0);
		this.cancel.set(true);
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {

		this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.counter.get(), this.querySize.get(), message);
	}
}
