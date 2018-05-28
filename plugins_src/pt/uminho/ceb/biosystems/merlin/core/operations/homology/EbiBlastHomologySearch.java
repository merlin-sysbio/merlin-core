package pt.uminho.ceb.biosystems.merlin.core.operations.homology;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.Map;

import org.apache.axis.AxisFault;
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
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.Enumerators.FileExtensions;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.MySleep;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.remote.SearchAndLoadHomologueSequences;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.BlastMatrix;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.BlastProgram;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.EbiRemoteDatabasesEnum;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.HmmerRemoteDatabasesEnum;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.HomologySearchServer;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.NumberofAlignments;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.SequenceType;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.utilities.Enumerators.Matrix;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

/**
 * @author Oscar
 *
 */
@Operation(name="EBI annotation", description="Perform a semi-automatic (re)annotation of the organism's genome. This process may take several hours, depending on the web-server availability.")
public class EbiBlastHomologySearch {

	final static Logger logger = LoggerFactory.getLogger(EbiBlastHomologySearch.class);
	private String program;
	private BlastMatrix blastMatrix=BlastMatrix.AUTOSELECTION;
	private EbiRemoteDatabasesEnum database;
	private NumberofAlignments numberOfAlignments;
	private String eVal;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private SearchAndLoadHomologueSequences ebiBlastLoader;
	private boolean autoEval;
	private int latencyWaitingPeriod;
	private SequenceType sequenceType;
	private String email;


	@Port(direction=Direction.INPUT, name="BLAST type", validateMethod="checkProgram",defaultValue="blastp",description="Blast program. Default: 'blastp'",order=1)
	public void setDatabaseName(BlastProgram program) {

		this.program = program.toString();
	}

	@Port(direction=Direction.INPUT, name="Sequence Type",defaultValue="protein",description="Default: 'protein'",order=2)
	public void setSequence(SequenceType sequenceType) {

		this.sequenceType=sequenceType;
	}

	@Port(direction=Direction.INPUT, name="e-value",defaultValue="1E-30",description="Default: '1E-30'",order=3)
	public void setUser(String eVal) {

		if(eVal.isEmpty())
			this.eVal="1E-30";
		else 
			this.eVal = eVal;
	}

	@Port(direction=Direction.INPUT, name="Adjust E-Value",defaultValue="true",description="Automatically adjust e-value for smaller sequences search",order=4)
	public void setEValueAutoSelection(boolean autoEval){
		
		this.autoEval=autoEval;
	}

	@Port(direction=Direction.INPUT, name="Remote database",defaultValue="uniprotkb", validateMethod="checkDatabase", description="Select the sequence database to run searches against",order=5)
	public void setRemoteDatabase(EbiRemoteDatabasesEnum ls){
		
		this.database=ls;
	}

	@Port(direction=Direction.INPUT, name="Number of results",defaultValue="100",description="Select the maximum number of aligned sequences to display. Default: '100'",order=6)
	public void setNumberOfAlignments(NumberofAlignments numberOfAlignments) {

		this.numberOfAlignments = numberOfAlignments;
	}

	@Port(direction=Direction.INPUT, name="Substitution matrix",defaultValue="AUTO",description="Assigns a score for aligning pairs of residues. Default: 'Adapts to Sequence length'.",order=7)
	public void setMatrix(BlastMatrix blastMatrix){

		this.blastMatrix = blastMatrix;
	}
	
	@Port(direction=Direction.INPUT, name="Latency period",description="Request latency waiting period (minutes)",validateMethod="checkLatencyWaitingPeriod", defaultValue = "30", order=8)
	public void setLatencyWaitingPeriod(int latencyWaitingPeriod) {

		this.latencyWaitingPeriod = latencyWaitingPeriod;
	}

	@Port(direction=Direction.INPUT, name="email",description="user email address", validateMethod="checkEmail", order=9)
	public void setEmail(String email) {

	}

	/**
	 * @param project
	 * @throws SQLException
	 * @throws AxisFault
	 */
	@Port(direction=Direction.INPUT, name="Workspace",description="Select Workspace",validateMethod="checkProject", order=10)
	public void selectProject(Project project) throws SQLException{

		try {

			FileExtensions extension = FileExtensions.PROTEIN_FAA;
			if(this.program == "blastx")
				extension = FileExtensions.CDS_FROM_GENOMIC;
			
			Map<String, AbstractSequence<?>> sequences = CreateGenomeFile.getGenomeFromID(project.getDatabase().getDatabaseName(), project.getTaxonomyID(), extension);
			
			this.ebiBlastLoader = new SearchAndLoadHomologueSequences(sequences, project, HomologySearchServer.EBI, this.email);

			//it has to be milliseconds
			this.ebiBlastLoader.setLatencyWaitingPeriod(this.latencyWaitingPeriod*60000);
			this.ebiBlastLoader.setRetrieveUniprotStatus(true);
			
			this.ebiBlastLoader.setTaxonomicID(project.getTaxonomyID()+"");


			if(blastMatrix!=BlastMatrix.AUTOSELECTION) {
				
				if(blastMatrix==BlastMatrix.BLOSUM62)
					this.ebiBlastLoader.setBlastMatrix(Matrix.BLOSUM62);
				if(blastMatrix==BlastMatrix.BLOSUM45)
					this.ebiBlastLoader.setBlastMatrix(Matrix.BLOSUM45);
				if(blastMatrix==BlastMatrix.BLOSUM80)
					this.ebiBlastLoader.setBlastMatrix(Matrix.BLOSUM80);
				if(blastMatrix==BlastMatrix.PAM30)
					this.ebiBlastLoader.setBlastMatrix(Matrix.PAM30);
				if(blastMatrix==BlastMatrix.PAM70)
					this.ebiBlastLoader.setBlastMatrix(Matrix.PAM70);
			}
			this.ebiBlastLoader.setTimeLeftProgress(this.progress);
			
			int errorOutput = 0;
			
			while(this.ebiBlastLoader.isReBlast()) {

				
				this.ebiBlastLoader.setReBlast(false);
				this.ebiBlastLoader.setSimilaritySearchProcessAvailable(true);
				
				errorOutput += this.ebiBlastLoader.blastSequencesEBI(this.program.toString(), this.database.toString(), 
						this.numberOfAlignments.index(), Double.parseDouble(this.eVal)
						, this.autoEval, this.sequenceType.toString());
				
				
				if(this.ebiBlastLoader.isReBlast()) {
					
					MySleep.myWait(300000);
					this.ebiBlastLoader.setSequences_size(0);
				}
			}

			if(errorOutput == 0) {

				if(this.ebiBlastLoader.removeDuplicates() && !this.ebiBlastLoader.isCancel().get()) {

					errorOutput = this.ebiBlastLoader.blastSequencesEBI(this.program.toString(), this.database.toString(), 
							this.numberOfAlignments.index(), Double.parseDouble(this.eVal)
							, this.autoEval, this.sequenceType.toString());
				}

				if(errorOutput == 0 && !this.ebiBlastLoader.isCancel().get()) {

					MerlinUtils.updateEnzymesAnnotationView(project.getName());
					Workbench.getInstance().info("BLAST process complete!");
				}
			}

			if(errorOutput > 0)
				Workbench.getInstance().error("Errors have ocurred while processsing "+errorOutput+" query(ies).");

			if(this.ebiBlastLoader.isCancel().get())
				Workbench.getInstance().warn("BLAST search cancelled!");
		}
		catch (Error e) {e.printStackTrace();}//e.printStackTrace();
		catch (IOException e) {e.printStackTrace();}//e.printStackTrace();
		catch (ParseException e) {e.printStackTrace();}//e.printStackTrace();
		catch (Exception e) {e.printStackTrace();}
	}


	/**
	 * @return
	 */
	@Progress
	public TimeLeftProgress getProgress(){

		return progress;
	}

	/**
	 * 
	 */
	@Cancel
	public void cancel() {

		progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-GregorianCalendar.getInstance().getTimeInMillis()),1,1);
		ebiBlastLoader.setCancel();
		
		logger.warn("BLAST cancelled!");
		Workbench.getInstance().warn("BLAST cancelled!");
	}


	/**
	 * @param database
	 */
	public void checkDatabase(EbiRemoteDatabasesEnum database) {

		this.database=database;
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
			
			if(!Project.isFaaFiles(dbName,taxID) && !Project.isFnaFiles(dbName,taxID))
				throw new IllegalArgumentException("Set the genome fasta file(s) for project "+project.getName());

			if(project.getTaxonomyID()<0)
				throw new IllegalArgumentException("Please enter the taxonomic identification from NCBI taxonomy.");

			if(this.program.toString().equalsIgnoreCase("blastp") && !Project.isFaaFiles(dbName, taxID))
				throw new IllegalArgumentException("Please add 'faa' files to perform blastp homology searches.");

			if(this.program.toString().equalsIgnoreCase("blastx") && !Project.isFnaFiles(dbName, taxID))
				throw new IllegalArgumentException("Please add 'fna' files to perform blastx homology searches.");

			String ncbiBlastDatabase = project.getNcbiBlastDatabase();

			if(ncbiBlastDatabase!=null)
				throw new IllegalArgumentException("A previous NCBI BLAST was already performed. To perform an EBI BLAST please clean the enzymes annotation data from the database menu.");

			String hmmerDatabase = project.getHmmerDatabase();
			if(hmmerDatabase!=null) {

				//				boolean go = true;
				//				for(EbiRemoteDatabasesEnum r : EbiRemoteDatabasesEnum.values())
				//					if(r.toString().equalsIgnoreCase(hmmerDatabase))
				//						go=false;
				//
				//				if(go)
				//					throw new IllegalArgumentException("The database selected for performing HMMER is unavailable for BLAST using EBI");

				boolean go = true;
				for(HmmerRemoteDatabasesEnum r :HmmerRemoteDatabasesEnum.values())
					if(r.toString().equalsIgnoreCase(this.database.toString()))
						go=false;

				if(go) {
					
					logger.error("The database selected for performing EBI BLAST is not compatible with HMMER.");
					throw new IllegalArgumentException("The database selected for performing EBI BLAST is not compatible with HMMER.");
				}
			}
		}
	}

	/**
	 * @param project
	 */
	public void checkProgram(BlastProgram program) {

		this.program = program.toString();
	}

	/**
	 * @param contents
	 */
	public void checkEmail(String email) {
		
			EmailValidator validator = EmailValidator.getInstance();

			if (validator.isValid(email)) {
				
				this.email = email;
			}
			else {
				
				logger.error("Please set a valid email address!");
				throw new IllegalArgumentException("Please set a valid email address!");
			}
	}
	
	/**
	 * @param project
	 */
	public void checkLatencyWaitingPeriod(int latencyWaitingPeriod) {

		if(latencyWaitingPeriod <0){

			logger.error("The latency waiting period must be greater than 0 (zero)");
			throw new IllegalArgumentException("The latency waiting period must be greater than 0 (zero)");
		}
	}

}
