package pt.uminho.ceb.biosystems.merlin.core.operations.homology;

import java.io.File;
import java.io.FileFilter;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.biojava.nbio.core.sequence.template.AbstractSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.Enumerators.FileExtensions;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.blast.BlastReportsLoader;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.BlastProgram;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.BlastSource;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.FileExtension;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MyFileFilter;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.HomologyAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

/**
 * @author Antonio Dias and Oscar Dias
 *
 */
@Operation(name="load BLAST reports", description="load local BLAST reports")
public class LoadBlastReport {

	final static Logger logger = LoggerFactory.getLogger(LoadBlastReport.class);

	private Project project;
	private File file;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private AtomicBoolean cancel;
	private BlastSource blastSource;
	private BlastProgram blastProgram;
	private FileExtension extension;

	/**
	 * @param file
	 */
	@Port(direction=Direction.INPUT, name="files", description = "select folder containing BLAST results",order=1)
	public void setFileName(File file){
		this.file=file;
	}
	
	/**
	 * @param fileExtension
	 */
	@Port(direction=Direction.INPUT, name="extension", description = "set extension of the BLAST results files", defaultValue="TXT",order=2)
	public void fileNameExtension(FileExtension fileExtension) {
		
		this.extension = fileExtension;
	}
	
	/**
	 * @param blastSource
	 */
	@Port(direction=Direction.INPUT, name="source", description = "select file origin",order=3)
	public void setSource(BlastSource blastSource){
		this.blastSource = blastSource;
	}


	@Port(direction=Direction.INPUT, name="BLAST type", description = "BLAST type", order=4)
	public void setType(BlastProgram blastProgram){
		this.blastProgram = blastProgram;
	}

	/**
	 * @param project
	 * @throws Exception 
	 */
	@Port(direction=Direction.INPUT, name="workspace",description="select Project",validateMethod="checkProject", order=5)
	public void setProject(Project project) throws Exception {

		String[] orgData = new String[2];
		orgData[0] = this.project.getOrganismName();
		orgData[1] = this.project.getOrganismLineage();
		String[] organismTaxa = orgData;

		this.cancel = new AtomicBoolean(false);

		ConcurrentHashMap<String,String[]> taxonomyMap = new ConcurrentHashMap<>();
		taxonomyMap.put(this.project.getTaxonomyID()+"", organismTaxa);
		ConcurrentHashMap<String, Boolean> uniprotStar = new ConcurrentHashMap<>();
		
		Connection conn = new Connection(this.project.getDatabase().getDatabaseAccess());
		Statement statement = conn.createStatement();
		ConcurrentLinkedQueue<String> existingGenes = new ConcurrentLinkedQueue<> (HomologyAPI.getGenesFromDatabase(this.blastProgram.toString(), true, statement));
		statement.close();
		conn.closeConnection();

		
		FileExtensions extension = FileExtensions.PROTEIN_FAA;
		if(this.blastProgram.toString() == "blastx")
			extension = FileExtensions.CDS_FROM_GENOMIC;
		Map<String, AbstractSequence<?>> sequences = CreateGenomeFile.getGenomeFromID(this.project.getDatabase().getDatabaseName(), this.project.getTaxonomyID(), extension);

		
		FileFilter filter = new MyFileFilter(this.extension.toString().toLowerCase());

		if(!this.file.isDirectory())
			file = new File(this.file.getParent().toString());

		File[] fileArray = file.listFiles(filter);
		ConcurrentLinkedQueue<File> outFiles = new ConcurrentLinkedQueue<>(Arrays.asList(fileArray));
		
		int max = outFiles.size();
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		AtomicInteger errorCounter = new AtomicInteger(0);
		AtomicInteger counter = new AtomicInteger(0);

		int threadsNumber=0;
		int numberOfCores = Runtime.getRuntime().availableProcessors()*4;
		List<Thread> threads = new ArrayList<Thread>();
		ArrayList<Runnable> runnables = new ArrayList<Runnable>();

		if(max<numberOfCores)
			threadsNumber=outFiles.size();
		else
			threadsNumber=numberOfCores;

		for(int i=0; i<threadsNumber; i++) {		
			
			BlastReportsLoader brl = new BlastReportsLoader(this.extension.toString().toLowerCase(), existingGenes, organismTaxa, 
					this.project, blastProgram, blastSource, outFiles, sequences, 
					taxonomyMap, uniprotStar, max, startTime, this.cancel, errorCounter, counter, this.progress);
			
			Thread thread = new Thread(brl);
			runnables.add(brl);
			threads.add(thread);
			logger.info("Start "+i);
			thread.start();
		}

		for(Thread thread :threads)
			thread.join();

		if(errorCounter.get()>0) {

			Workbench.getInstance().error("An error occurred when performing the operation!");
		}
		else {

			MerlinUtils.updateEnzymesAnnotationView(project.getName());
			Workbench.getInstance().info("BLAST report successfully loaded.");
		}

	}

	/**
	 * @return
	 */
	@Progress
	public TimeLeftProgress getProgress() {

		return this.progress;
	}

	/**
	 * 
	 */
	@Cancel
	public void cancel(){

		this.progress.setTime(0,1,1);
		this.cancel.set(true);
	}

	/**
	 * @param project
	 */
	public void checkProject(Project project) {

		if(project == null)
			throw new IllegalArgumentException("No Project Selected!");
		else
			this.project = project;
	}

}
