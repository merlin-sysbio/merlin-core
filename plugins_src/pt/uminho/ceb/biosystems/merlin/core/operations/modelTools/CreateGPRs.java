/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.operations.modelTools;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.biojava.nbio.core.sequence.template.AbstractSequence;

import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.Enumerators.FileExtensions;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.utilities.LoadFromConf;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.gpr.rules.core.FilterModelReactions;
import pt.uminho.ceb.biosystems.merlin.gpr.rules.core.IdentifyGenomeSubunits;
import pt.uminho.ceb.biosystems.merlin.utilities.Enumerators.Method;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.gpr.ReactionsGPR_CI;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

/**
 * @author ODias
 *
 */
@Operation(description="Generate and integrate gene-reactions connections.",name="GPRs generator.")
public class CreateGPRs {

	private DatabaseAccess databaseAccess;
	private long reference_organism_id;
	private double similarity_threshold;
	private double referenceTaxonomyThreshold;
	private boolean compareToFullGenome;
	private boolean identifyGPRs;
	private boolean integrateToDatabase;
	private boolean keepReactionsWithNotes;
	private boolean originalReaction;
	private boolean generateGPRs;
	private TimeLeftProgress  progress = new TimeLeftProgress();
	private Project project;
	private Map<String, AbstractSequence<?>> genome;
	private boolean keepManualReactions;
	private boolean removeReactions;
	private double threshold;
	private AtomicBoolean cancel;

	/**
	 * @param project
	 */
	@Port(name="workspace",description="select workspace",direction=Direction.INPUT,order=1, validateMethod="checkProject")
	
	public void setProject(Project project) {

		this.project = project;
		this.databaseAccess = project.getDatabase().getDatabaseAccess();
		this.reference_organism_id = project.getTaxonomyID();
		this.originalReaction = !project.isCompartmentalisedModel();
		
		try {
			Connection connection = new Connection(databaseAccess);
			
			Map<String, String> settings = LoadFromConf.loadGPRsettings(FileUtils.getConfFolderPath());
			
			this.similarity_threshold = Float.parseFloat(settings.get("similarity_threshold"));
			this.referenceTaxonomyThreshold = Float.parseFloat(settings.get("referenceTaxonomyThreshold"));
			this.compareToFullGenome = Boolean.parseBoolean(settings.get("compareToFullGenome"));
			this.identifyGPRs = Boolean.parseBoolean(settings.get("identifyGPRs"));
			this.generateGPRs = Boolean.parseBoolean(settings.get("generateGPRs"));
			this.keepReactionsWithNotes = Boolean.parseBoolean(settings.get("keepReactionsWithNotes"));
			this.keepManualReactions = Boolean.parseBoolean(settings.get("keepManualReactions"));
			this.integrateToDatabase = Boolean.parseBoolean(settings.get("integrateToDatabase"));
			this.threshold = Float.parseFloat(settings.get("threshold"));
			this.removeReactions = Boolean.parseBoolean(settings.get("removeReactions"));

			this.cancel = new AtomicBoolean(false);

			boolean identifiedWithoutErros = false;
			
			if(this.identifyGPRs && !this.cancel.get()) {

				Method method = Method.SmithWaterman;

				Map<String, List<String>> ec_numbers = ModelAPI.getECNumbers(connection);
				
				IdentifyGenomeSubunits i = new IdentifyGenomeSubunits(ec_numbers, genome, reference_organism_id, databaseAccess, similarity_threshold, 
						referenceTaxonomyThreshold, method, compareToFullGenome);
				i.setProgress(progress);
				i.setCancel(this.cancel);
				identifiedWithoutErros = i.runIdentification(false);
			}
			
			if(!this.identifyGPRs || identifiedWithoutErros) {

				if(this.generateGPRs && !this.cancel.get()) {

					Map<String, ReactionsGPR_CI> ret = IdentifyGenomeSubunits.runGPRsAssignment(this.threshold, connection);
					
					FilterModelReactions f = new FilterModelReactions(databaseAccess, this.originalReaction);
					f.filterReactions(ret);

					if(this.integrateToDatabase && !this.cancel.get()) {

						if(this.removeReactions)
							f.removeReactionsFromModel(keepReactionsWithNotes, this.keepManualReactions);
						f.setModelGPRsFromTool();
					}
				}

				if(this.cancel.get()) {
					
					Workbench.getInstance().warn("GPR job cancelled!");
				}
				else {
					
					MerlinUtils.updateAllViews(project.getName());
					Workbench.getInstance().info("GPR job finished!");
				}
			}
			else {

				if(this.cancel.get())
					Workbench.getInstance().warn("GPR job cancelled!");

				Workbench.getInstance().error("merlin found some errors whilst performing this operation. Please try again later!");
			}
		} 
		catch (Exception e) {

			Workbench.getInstance().error("Error "+e.getMessage()+" has occured.");
			e.printStackTrace();
		}

	}

	/**
	 * @param similarity_threshold
	 */
	public void checkDouble(double double_) {

		if(double_>1 || double_<0) {

			throw new IllegalArgumentException("Please set a valid double (0<double<1).");
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

	/**
	 * @param project
	 */
	public void checkProject(Project project) {
		
		if(project == null) {

			throw new IllegalArgumentException("No Project Selected!");
		}
		else {
			
			String dbName = project.getDatabase().getDatabaseName();
			Long taxID = project.getTaxonomyID();

			this.project = project;

			if(!Project.isFaaFiles(dbName,taxID) && !Project.isFnaFiles(dbName,taxID)) {

				throw new IllegalArgumentException("Please set the project fasta files!");
			}
			else if(project.getTaxonomyID()<0) {

				throw new IllegalArgumentException("Please enter the organism taxonomic identification from NCBI taxonomy to perform this operation.");
			}
			else {

				try {

					this.genome = CreateGenomeFile.getGenomeFromID(this.project.getDatabase().getDatabaseName(), this.project.getTaxonomyID(), FileExtensions.PROTEIN_FAA);

					if(this.identifyGPRs && this.genome==null) {

						throw new IllegalArgumentException("Please set the project fasta files!");
					}
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

	
}
