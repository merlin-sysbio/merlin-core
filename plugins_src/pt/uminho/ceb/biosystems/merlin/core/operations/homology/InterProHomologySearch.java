package pt.uminho.ceb.biosystems.merlin.core.operations.homology;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.validator.routines.EmailValidator;
import org.biojava.nbio.core.sequence.template.AbstractSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ebi.interpro.InterProResultsList;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.Enumerators.FileExtensions;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation.EnzymesAnnotationDataInterface;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.core.remote.SearchAndLoadInterPro;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.HomologyAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

/**
 * @author Oscar Dias
 *
 */
@Operation(description="operation that performs InterPro scan searches on selected sequences", name="InterPro scan")
public class InterProHomologySearch implements Observer{


	private Project project;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private AtomicBoolean cancel;
	private Map<String,  AbstractSequence<?>> sequences;
	private double lowerThreshold;
	private double upperThreshold;
	private Set<String> genes;
	private long startTime;
	private int size;
	private String message;
	private AtomicInteger sequencesCounter;
	private int latencyWaitingPeriod;
	private boolean useManual;
	private String email;
	final static Logger logger = LoggerFactory.getLogger(InterProHomologySearch.class);

	@Port(direction=Direction.INPUT, name="lower threshold",description="select lower enzyme annotation threshold", defaultValue = "0", validateMethod="checkLowerThreshold", order=1)
	public void setLowerThreshold(double lowerThreshold) {

	}

	@Port(direction=Direction.INPUT, name="upper threshold",description="select upper enzyme annotation threshold", defaultValue = "0.5", validateMethod="checkUpperThreshold", order=2)
	public void setUpperThreshold(double upperThreshold) {

	}

	@Port(direction=Direction.INPUT, name="latency period",description="request latency waiting period (minimum 180 minutes)",validateMethod="checkLatencyWaitingPeriod", defaultValue = "180", order=3)
	public void setLatencyWaitingPeriod(int latencyWaitingPeriod) {

		this.latencyWaitingPeriod = latencyWaitingPeriod;
	}
	
	@Port(direction=Direction.INPUT, name="check manual",description="check manually inserted entries", defaultValue = "true", order=4)
	public void useManual(boolean useManual) {

		this.useManual = useManual;
	}
	
	@Port(direction=Direction.INPUT, name="email",description="user email address", validateMethod="checkEmail", order=5)
	public void setEmail(String email) {

	}

	/**
	 * @param project
	 */
	@Port(direction=Direction.INPUT, name="workspace",description="select workspace", validateMethod="checkProject", order=6)
	public void setProject(Project project) {

		this.project = project;
		DatabaseAccess databaseAccess = this.project.getDatabase().getDatabaseAccess();
		
		logger.debug("Sequences {} {}", sequences.size(), sequences.keySet());
		
		this.sequences.keySet().retainAll(this.genes);
		
		logger.debug("Genes {} {}", genes.size(), genes);

		this.startTime = GregorianCalendar.getInstance().getTimeInMillis();
		this.cancel = new AtomicBoolean(false);
		this.sequencesCounter = new AtomicInteger(0);
		AtomicInteger errorCounter = new AtomicInteger(0);

		try {
			
			Connection conn = new Connection(databaseAccess);
			Statement statement = conn.createStatement();
			Set<String> processed = HomologyAPI.getLoadedIntroProAnnotations(statement);
			
			logger.debug("Processed sequences {} {}", processed.size(), processed);
			
			this.sequences.keySet().removeAll(processed);

			logger.debug("Sequences {} {}", sequences.size(), sequences.keySet());

			if(sequences.size()>0) {

				SearchAndLoadInterPro interPro = new SearchAndLoadInterPro(databaseAccess, this.cancel, this.sequencesCounter, errorCounter);
				interPro.addObserver(this);

				Map<String, InterProResultsList> results = null;
				this.size = this.sequences.size();
				this.message = "Searching InterPro scan";
				
				if(!this.cancel.get())				
					results = interPro.getInterProResults(this.sequences, (this.latencyWaitingPeriod*60*1000), this.email);

				this.startTime = GregorianCalendar.getInstance().getTimeInMillis();
				this.size = results.size();
				this.message = "Loading InterPro results";
				if(!this.cancel.get())
					interPro.loadInterProResults(results);

				HomologyAPI.deleteInterProEntries("PROCESSING", statement);

				statement.close();
				conn.closeConnection();

				if(errorCounter.get()>0) {

					Workbench.getInstance().warn("InterPro scan finished with erros. Please run again.");
				}
				else {

					MerlinUtils.updateEnzymesAnnotationView(project.getName());
					Workbench.getInstance().info("InterPro scan finished.");
				}
			}
			else {

				Workbench.getInstance().info("No aminoacid sequences match the filtering criteria.");
			}
		} 
		catch (InterruptedException e) {

			Workbench.getInstance().error("InterruptedException "+e.getMessage()+" has occured.");
		}
		catch (SQLException e) {

			Workbench.getInstance().error("SQLException "+e.getMessage()+" has occured.");
		} 
		catch (IOException e) {

			Workbench.getInstance().error("IOException "+e.getMessage()+" has occured.");
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

		this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.sequencesCounter.get(), this.size, this.message);
	}

	/**
	 * @param contents
	 */
	public void checkEmail(String email) {
		
			EmailValidator validator = EmailValidator.getInstance();

			if (validator.isValid(email))
				this.email = email;
			else
				throw new IllegalArgumentException("Please set a valid email address!");
	}
	
	/**
	 * @param contents
	 */
	public void checkLowerThreshold(double lowerThreshold) {

		if(lowerThreshold<0 || lowerThreshold>1)
			throw new IllegalArgumentException("the threshold should be higher than 0 and lower than 1!");

		this.lowerThreshold = lowerThreshold;
	}


	/**
	 * @param contents
	 */
	public void checkUpperThreshold(double upperThreshold) {

		if(upperThreshold<0 || upperThreshold>1)
			throw new IllegalArgumentException("the threshold should be higher than 0 and lower than 1!");

		if(upperThreshold<this.lowerThreshold)
			throw new IllegalArgumentException("the upper threshold should be higher than the lower threshold!");

		this.upperThreshold = upperThreshold;
	}

	/**
	 * @param project
	 */
	public void checkProject(Project project) {
		
		if(project == null) {

			throw new IllegalArgumentException("no Project Selected!");
		}
		else {
			
			this.project = project;
			
			String dbName = this.project.getDatabase().getDatabaseName();

			Long taxID = this.project.getTaxonomyID();


			if(!Project.isFaaFiles(dbName, taxID)) {

				throw new IllegalArgumentException("please set the project fasta files!");
			}
			else {

				try {

					this.sequences = CreateGenomeFile.getGenomeFromID(this.project.getDatabase().getDatabaseName(), project.getTaxonomyID(), FileExtensions.PROTEIN_FAA);

					if(this.sequences==null)
						throw new IllegalArgumentException("please set the project fasta files!");
				} 
				catch (Exception e) {

					//e.printStackTrace();
					throw new IllegalArgumentException("Please set the project fasta files!");
				}
			}

			if(!Project.isFaaFiles(dbName, taxID)) {

				throw new IllegalArgumentException("please add 'faa' files to perform the InterPro similarity search.");
			}

			EnzymesAnnotationDataInterface enzymesAnnotation = null;

			for(Entity ent : project.getDatabase().getAnnotations().getEntitiesList())
				if(ent.getName().equalsIgnoreCase("enzymes"))
					enzymesAnnotation = (EnzymesAnnotationDataInterface) ent;

			try {

			if(enzymesAnnotation == null || enzymesAnnotation.getInitialProdItem() == null)
				throw new IllegalArgumentException("enzymes annotation view unavailable!");
			else
				this.genes = enzymesAnnotation.getGenesInThreshold(this.lowerThreshold, this.upperThreshold, this.useManual);
			
			}
			catch (SQLException e) {
				
				Workbench.getInstance().error(e);
			}
		}
	}

	/**
	 * @param project
	 */
	public void checkLatencyWaitingPeriod(int latencyWaitingPeriod) {

		if(latencyWaitingPeriod <180)
			throw new IllegalArgumentException("The latency waiting period must be greater than 180 (zero)");

		this.latencyWaitingPeriod = latencyWaitingPeriod;
	}

}
