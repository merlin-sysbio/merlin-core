/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.operations.homology;

import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.Map;

import org.apache.axis.AxisFault;
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
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.EbiRemoteDatabasesEnum;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.HmmerRemoteDatabasesEnum;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.HomologySearchServer;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.NcbiRemoteDatabasesEnum;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

/**
 * @author ODias
 *
 */
@Operation(description="Perform a semi-automatic (re)annotation of the organism's genome. This process may take several hours, depending on the web-server availability.")
public class HmmerHomologySearch {

	private HmmerRemoteDatabasesEnum database;
	private String numberOfAlignments;
	private String eVal;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private SearchAndLoadHomologueSequences hmmer_loader;
	//private String taxonomicIdentification=null;
	private boolean uniprotStatus;
	private int latencyWaitingPeriod;
	//private String email;

	@Port(direction=Direction.INPUT, name="Expected Value",defaultValue="1e-30",description="Default: '1e-30'",order=1)
	public void setUser(String eVal){

		if(eVal.isEmpty()) {

			this.eVal="1e-30";
		}
		else {

			this.eVal = eVal;
		}
	}

	@Port(direction=Direction.INPUT, name="Remote database", validateMethod="checkDatabase", description="Select the sequence database to run searches against",order=2)
	public void setRemoteDatabase(HmmerRemoteDatabasesEnum database){
		this.database=database;
	}

	@Port(direction=Direction.INPUT, name="Number of results",defaultValue="100",description="Select the maximum number of aligned sequences to display. Default: '100'",order=3)
	public void setNumberOfAlignments(String numberOfAlignments) {

		if(numberOfAlignments.isEmpty()) {

			this.numberOfAlignments = "100";
		}
		else {

			this.numberOfAlignments = numberOfAlignments;
		}
	}
	

	@Port(direction=Direction.INPUT, name="Uniprot Status",description="Retrieve status from uniprot",defaultValue = "false", order=4)
	public void retrieveUniprotStatus(boolean uniprotStatus) {

		this.uniprotStatus = uniprotStatus;
	}
	
	@Port(direction=Direction.INPUT, name="Latency period",description="Request latency waiting period (minutes)",validateMethod="checkLatencyWaitingPeriod", defaultValue = "30", order=5)
	public void setLatencyWaitingPeriod(int latencyWaitingPeriod) {
	
		this.latencyWaitingPeriod = latencyWaitingPeriod;
	}

	/**
	 * @param project
	 * @throws SQLException
	 * @throws AxisFault
	 */
	@Port(direction=Direction.INPUT, name="Workspace",description="Select Workspace",validateMethod="checkProject", order=6)
	public void selectProject(Project project) throws SQLException{

		try  {

			Map<String, AbstractSequence<?>> sequences = CreateGenomeFile.getGenomeFromID(project.getDatabase().getDatabaseName(), project.getTaxonomyID(), FileExtensions.PROTEIN_FAA);
			this.hmmer_loader = new SearchAndLoadHomologueSequences(sequences, project, HomologySearchServer.HMMER, null);
			this.hmmer_loader.setTimeLeftProgress(this.progress);
			this.hmmer_loader.setLatencyWaitingPeriod(this.latencyWaitingPeriod*60000);

			this.hmmer_loader.setTaxonomicID(project.getTaxonomyID()+"", this.database);
			int errorOutput =  this.hmmer_loader.hmmerSearchSequences(this.database, Integer.parseInt(this.numberOfAlignments), Double.parseDouble(this.eVal), this.uniprotStatus);

			if(errorOutput == 0) {

				if(this.hmmer_loader.removeDuplicates() && !this.hmmer_loader.isCancel().get())
					errorOutput = this.hmmer_loader.hmmerSearchSequences(this.database, Integer.parseInt(this.numberOfAlignments), Double.parseDouble(this.eVal), this.uniprotStatus);

				if(errorOutput == 0 && !hmmer_loader.isCancel().get()) {

					MerlinUtils.updateEnzymesAnnotationView(project.getName());
					Workbench.getInstance().info("Hmmer search complete!");
				}
			}

			if(errorOutput > 0)
				Workbench.getInstance().error("Errors have ocurred while processsing "+errorOutput+" query(ies).");

			if(this.hmmer_loader.isCancel().get())
				Workbench.getInstance().warn("HMMER search cancelled!");
		}
		catch (Error e) {Workbench.getInstance().error(e); e.printStackTrace();}//e.printStackTrace();
		catch (IOException e) {Workbench.getInstance().error(e);e.printStackTrace();}//e.printStackTrace();
		catch (ParseException e) {Workbench.getInstance().error(e);e.printStackTrace();}//e.printStackTrace();
		catch (Exception e) {Workbench.getInstance().error(e);e.printStackTrace();}
	}



	/**
	 * @return
	 */
	@Progress
	public TimeLeftProgress getProgress(){
		return this.progress;
	}

	/**
	 * 
	 */
	@Cancel
	public void cancel() {

		this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-GregorianCalendar.getInstance().getTimeInMillis()),1,1);
		this.hmmer_loader.setCancel();
	}
	
	/**
	 * @param database
	 */
	public void checkDatabase(HmmerRemoteDatabasesEnum database) {
		
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
			
			if(!Project.isFaaFiles(dbName, taxID) && !Project.isFnaFiles(dbName, taxID))
				throw new IllegalArgumentException("Set the genome fasta file(s) for project "+project.getName());

			if(project.getTaxonomyID()<0)
				throw new IllegalArgumentException("The loaded genome is not in the NCBI fasta format.\nPlease enter the taxonomic identification from NCBI taxonomy.");

			if(!Project.isFaaFiles(dbName, taxID))
				throw new IllegalArgumentException("Please add 'faa' files to perform hmmer homology searches.");

			String ebiBlastDatabase = project.getEbiBlastDatabase();

			if(ebiBlastDatabase!=null) {

				//				boolean go = true;
				//				for(HmmerRemoteDatabasesEnum r : HmmerRemoteDatabasesEnum.values())
				//					if(r.toString().equalsIgnoreCase(ebiBlastDatabase))
				//						go=false;

				boolean go = true;
				for(EbiRemoteDatabasesEnum r : EbiRemoteDatabasesEnum.values())
					if(r.toString().replace("uniprotkb_", "").equalsIgnoreCase(this.database.toString()))
						go=false;

				if(go)
					throw new IllegalArgumentException("The database selected for performing HMMER is not compatible with the EBI BLAST.");
			}

			String nciBlastDatabase = project.getNcbiBlastDatabase();

			if(nciBlastDatabase!=null) {

				//				boolean go = true;
				//				for(HmmerRemoteDatabasesEnum r : HmmerRemoteDatabasesEnum.values())
				//					if(r.toString().equalsIgnoreCase(nciBlastDatabase))
				//						go=false;

				boolean go = true;
				for(NcbiRemoteDatabasesEnum r :NcbiRemoteDatabasesEnum.values())
					if(r.toString().equalsIgnoreCase(this.database.toString()))
						go=false;

				if(go)
					throw new IllegalArgumentException("The database selected for performing HMMER is not compatible with the NCBI BLAST.");
			}
		}
	}
	
	/**
	 * @param project
	 */
	public void checkLatencyWaitingPeriod(int latencyWaitingPeriod) {

		if(latencyWaitingPeriod <0) {
			
			throw new IllegalArgumentException("The latency waiting period must be greater than 0 (zero)");
		}
			
	}
	
//	/**
//	 * @param contents
//	 */
//	public void checkEmail(String email) {
//		
//		try {
//			
//			InternetAddress emailAddr = new InternetAddress(email);
//			emailAddr.validate();
//			this.email = email;
//		} 
//		catch (AddressException ex) {
//			
//			Workbench.getInstance().warn("Please set a valid email address!");
//		}
//	}
}
