package pt.uminho.ceb.biosystems.merlin.core.operations.homology;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.Map;

import org.apache.axis.AxisFault;
import org.apache.commons.validator.routines.EmailValidator;
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
import pt.uminho.ceb.biosystems.merlin.core.remote.SearchAndLoadHomologueSequences;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.BlastMatrix;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.BlastProgram;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.GeneticCode;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.HmmerRemoteDatabasesEnum;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.HomologySearchServer;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.NcbiRemoteDatabasesEnum;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.WordSize;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.utilities.Enumerators.Matrix;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

@Operation(description="perform a semi-automatic (re)annotation of the organism's genome.\nthis process may take several hours, depending on the web-server availability.")
public class NcbiBlastHomologySearch {
	private String program;
	private BlastMatrix blastMatrix=BlastMatrix.AUTOSELECTION;
	private String database;
	private String numberOfAlignments;
	private String eVal;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private SearchAndLoadHomologueSequences blastLoader;
	private WordSize wordSize;
	private String organism;
	//private String taxonomicIdentification=null;
	private boolean autoEval;
	private GeneticCode geneticCode;
	private boolean uniprotStatus;
	private int latencyWaitingPeriod;
	private String email;

	@Port(direction=Direction.INPUT, name="BLAST type", validateMethod="checkProgram",defaultValue="blastp",description="BLAST program (default: 'blastp')",order=1)
	public void setDatabaseName(BlastProgram program) {

		this.program = program.toString();
	}

	@Port(direction=Direction.INPUT, name="genetic code",defaultValue="standard",description="genetic Code used for blastX, otherwise ignored",order=2)
	public void setGeneticCode(GeneticCode geneticCode) {

		this.geneticCode = geneticCode;
	}

	@Port(direction=Direction.INPUT, name="e-value",defaultValue="1E-30",description="default: '1E-30'",order=3)
	public void setUser(String eVal) {

		if(eVal.isEmpty())
			this.eVal="1E-30";
		else 
			this.eVal = eVal;
	}

	@Port(direction=Direction.INPUT, name="adjust E-Value",defaultValue="true",description="automatically adjust e-value for smaller sequences search",order=4)
	public void setEValueAutoSelection(boolean autoEval){
		this.autoEval=autoEval;
	}

	@Port(direction=Direction.INPUT, name="remote database", validateMethod="checkDatabase", description="select the sequence database to run searches against",order=5)
	public void setRemoteDatabase(NcbiRemoteDatabasesEnum ls){
		this.database=ls.toString();
	}

	@Port(direction=Direction.INPUT, name="number of results",defaultValue="100",description="select the maximum number of aligned sequences to display (default: '100')",order=6)
	public void setNumberOfAlignments(String numberOfAlignments) {

		if(numberOfAlignments.isEmpty())
			this.numberOfAlignments = "100";
		else
			this.numberOfAlignments = numberOfAlignments;
	}

	@Port(direction=Direction.INPUT, name="substitution matrix",defaultValue="AUTO",description="assigns a score for aligning pairs of residues (default: 'adapts to Sequence length')",order=7)
	public void setMatrix(BlastMatrix blastMatrix){
		this.blastMatrix = blastMatrix;
	}

	@Port(direction=Direction.INPUT, name="word size",defaultValue="3",description="the length of the seed that initiates an alignment (default: '3')",order=8)
	public void setWordSize(WordSize wordSize){
		this.wordSize = wordSize;
	}

	@Port(direction=Direction.INPUT, name="organism",defaultValue="",description="(optional) enter organism ncbi taxonomy id",order=9)
	public void setOrganism(String organism) {

		if(organism.isEmpty())
			this.organism = "no_org";
		else
			this.organism = organism;
	}
	
	@Port(direction=Direction.INPUT, name="Uniprot Status",description="retrieve homologue genes status from uniprot", defaultValue = "false", order=10)
	public void retrieveUniprotStatus(boolean uniprotStatus) {

		this.uniprotStatus = uniprotStatus;
	}

	@Port(direction=Direction.INPUT, name="latency period",description="request latency waiting period (minutes)",validateMethod="checkLatencyWaitingPeriod", defaultValue = "30", order=11)
	public void setLatencyWaitingPeriod(int latencyWaitingPeriod) {

		this.latencyWaitingPeriod = latencyWaitingPeriod;
	}


	@Port(direction=Direction.INPUT, name="email",description="user email address", validateMethod="checkEmail", order=12)
	public void setEmail(String email) {

	}
	
	/**
	 * @param project
	 * @throws SQLException
	 * @throws AxisFault
	 */
	@Port(direction=Direction.INPUT, name="workspace",description="select Project",validateMethod="checkProject", order=13)
	public void selectProject(Project project) throws SQLException{

		try {

			FileExtensions extension = FileExtensions.PROTEIN_FAA;
			if(this.program == "blastx")
				extension = FileExtensions.CDS_FROM_GENOMIC;
			
			

			Map<String, AbstractSequence<?>> sequences = CreateGenomeFile.getGenomeFromID(project.getDatabase().getDatabaseName(), project.getTaxonomyID(),  extension);
			this.blastLoader = new SearchAndLoadHomologueSequences(sequences, project, HomologySearchServer.NCBI, this.email);

			//it has to be milliseconds
			this.blastLoader.setLatencyWaitingPeriod(this.latencyWaitingPeriod*60000);
			if(database.equalsIgnoreCase("swissprot"))
				this.uniprotStatus=true;
			
			this.blastLoader.setRetrieveUniprotStatus(uniprotStatus);

			if(this.program == "blastx")
				this.blastLoader.setGeneticCode(this.geneticCode.index());

			this.blastLoader.setTaxonomicID(project.getTaxonomyID()+"");

			short word = -1;
			if(this.wordSize.index()!=0) {

				word = Short.parseShort(this.wordSize.index()+"");
				blastLoader.setWordSize(word);
			}

			if(!this.organism.equals("no_org"))
				this.blastLoader.setOrganism(this.organism);

			if(blastMatrix!=BlastMatrix.AUTOSELECTION) {

				if(blastMatrix==BlastMatrix.BLOSUM62)
					this.blastLoader.setBlastMatrix(Matrix.BLOSUM62);
				if(blastMatrix==BlastMatrix.BLOSUM45)
					this.blastLoader.setBlastMatrix(Matrix.BLOSUM45);
				if(blastMatrix==BlastMatrix.BLOSUM80)
					this.blastLoader.setBlastMatrix(Matrix.BLOSUM80);
				if(blastMatrix==BlastMatrix.PAM30)
					this.blastLoader.setBlastMatrix(Matrix.PAM30);
				if(blastMatrix==BlastMatrix.PAM70)
					this.blastLoader.setBlastMatrix(Matrix.PAM70);
			}
			this.blastLoader.setTimeLeftProgress(this.progress);

			int errorOutput = this.blastLoader.blastSequencesNCBI(this.program, this.database, Integer.parseInt(this.numberOfAlignments), Double.parseDouble(this.eVal), this.autoEval, word);

			if(errorOutput == 0) {

				if(this.blastLoader.removeDuplicates() && !this.blastLoader.isCancel().get())
					errorOutput = this.blastLoader.blastSequencesNCBI(this.program, this.database, Integer.parseInt(this.numberOfAlignments), Double.parseDouble(this.eVal), this.autoEval, word);

				if(errorOutput == 0 && !this.blastLoader.isCancel().get()) {

					MerlinUtils.updateEnzymesAnnotationView(project.getName());
					Workbench.getInstance().info("BLAST process complete!");
				}
			}

			if(errorOutput > 0)
				Workbench.getInstance().error("Errors have ocurred while processsing "+errorOutput+" query(ies).");

			if(this.blastLoader.isCancel().get())
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
		blastLoader.setCancel();
		Workbench.getInstance().warn("BLAST cancelled!");
	}



	/**
	 * @param database
	 */
	public void checkDatabase(NcbiRemoteDatabasesEnum database) {

		this.database=database.toString();
	}



	/**
	 * @param project
	 */
	public void checkProject(Project project) {

		if(project == null) {
			
			throw new IllegalArgumentException("no project selected!");
		}
		
		else {
			
			String dbName = project.getDatabase().getDatabaseName();
			Long taxID = project.getTaxonomyID();
							
			if(!Project.isFaaFiles(dbName,taxID) && !Project.isFnaFiles(dbName,taxID)) 
				throw new IllegalArgumentException("Set the genome fasta file(s) for project "+project.getName());

			if(project.getTaxonomyID()<0)
				throw new IllegalArgumentException("The loaded genome is not in the NCBI fasta format.\nPlease enter the taxonomic identification from NCBI taxonomy.");

			if(this.program.toString().equalsIgnoreCase("blastp") && !Project.isFaaFiles(dbName,taxID))
				throw new IllegalArgumentException("Please add 'faa' files to perform blastp homology searches.");

			if(this.program.toString().equalsIgnoreCase("blastx") && !Project.isFnaFiles(dbName,taxID))
				throw new IllegalArgumentException("Please add 'fna' files to perform blastx homology searches.");

			String ebiBlastDatabase = project.getEbiBlastDatabase();

			if(ebiBlastDatabase!=null)
				throw new IllegalArgumentException("A previous EBI BLAST was already performed. To perform a NCBI BLAST please clean the enzymes annotation data from the database menu.");

			String hmmerDatabase = project.getHmmerDatabase();
			if(hmmerDatabase!=null) {

				boolean go = true;
				for(HmmerRemoteDatabasesEnum r :HmmerRemoteDatabasesEnum.values())
					if(this.database.toString().toLowerCase().contains(r.toString().toLowerCase()))
						go=false;

				if(go)
					throw new IllegalArgumentException("the database selected for performing NCBI BLAST is not compatible with HMMER");
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

			if (validator.isValid(email))
				this.email = email;
			else
				throw new IllegalArgumentException("Please set a valid email address!");
	}

	/**
	 * @param project
	 */
	public void checkLatencyWaitingPeriod(int latencyWaitingPeriod) {

		if(latencyWaitingPeriod <0)
			throw new IllegalArgumentException("the latency waiting period must be greater than 0 (zero)");
	}

}
