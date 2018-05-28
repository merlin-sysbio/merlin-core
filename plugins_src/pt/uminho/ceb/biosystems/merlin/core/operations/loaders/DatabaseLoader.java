package pt.uminho.ceb.biosystems.merlin.core.operations.loaders;

import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicBoolean;

import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.remote.SearchAndLoadKeggData;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.TransportersAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

@Operation(name="Load Database",description="Load KEGG metabolic information")
public class DatabaseLoader {


	private Project project;
//	private String organism;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private SearchAndLoadKeggData keggLoader;
	private AtomicBoolean cancel;

	@Port(direction=Direction.INPUT, name="Workspace",description="Select Workspace",//validateMethod="checkProject",
			order=1)
	public void setProject(Project project){
		
		try {
			 Connection conn = new Connection(project.getDatabase().getDatabaseAccess());

			if(!TransportersAPI.checkReactionData(conn)) {

				this.project = project;

				this.cancel = new AtomicBoolean(false);

				this.keggLoader = new SearchAndLoadKeggData(this.cancel, this.progress);

				this.keggLoader.setTimeLeftProgress(this.progress);

				boolean output = this.keggLoader.getMetabolicData();

				if (output)			
					output = this.keggLoader.loadData(conn);

				if(output) {

					if(!this.keggLoader.isCancel().get()){

						MerlinUtils.updateAllViews(project.getName());
						Workbench.getInstance().info("Database successfully loaded.");
					}
					else {

						Workbench.getInstance().warn("Database loading cancelled!");
					}
				}
				else {

					Workbench.getInstance().info("An error occurred while performing the operation.");
				}
			}
			else{
				Workbench.getInstance().warn("Metabolic data already loaded!");
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
			Workbench.getInstance().info("An error occurred while performing the operation.");
		}
	}
	
//	/**
//	 * @param project
//	 */
//	public void checkProject(Project project){
//		this.project = project;
//
//		if(this.project==null)
//		{
//			throw new IllegalArgumentException("Please select a project.");
//		}
//
//	}

//	@Port(direction=Direction.INPUT, name="Organism",//validateMethod="validateOrg",
//			order=2)
//	public void setOrganism(String organism) throws SQLException {
//		
//		this.cancel = new AtomicBoolean(false);
//		
//		this.organism=organism;
//		this.keggLoader = new SearchAndLoadKeggData("", this.cancel, this.progress);
//		
//		this.keggLoader.setTimeLeftProgress(this.progress);
//		
//		boolean output = this.keggLoader.getData();
//		
//		if (output)			
//			output = this.keggLoader.loadData(new Connection(this.project.getDatabase().getDatabaseAccess()));
//		
//		if(output) {
//			
//			if(!this.keggLoader.isCancel().get()){
//				
//				MerlinUtils.updateAllViews(project.getName());
//				Workbench.getInstance().info("Database successfully loaded.");
//			}
//			else {
//				
//				Workbench.getInstance().warn("Database loading cancelled!");
//			}
//		}
//		else {
//			
//			Workbench.getInstance().info("An error occurred while performing the operation.");
//		}
//		
//	}

//	/**
//	 * @param org
//	 */
//	public void validateOrg(String organism) {
//		
//		this.organism=organism;
//		
//		if(organism.isEmpty()) {
//			
//			Workbench.getInstance().info("No organism related information will be loaded!");
//		}
//		new Get_and_load_Kegg_data(this.project, this.organism);
//	}
	
	/**
	 * @return
	 */
	@Progress
	public TimeLeftProgress getProgress() {
		
		return progress;
	}
	
	/**
	 * 
	 */
	@Cancel
	public void cancel() {
		
		this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-GregorianCalendar.getInstance().getTimeInMillis()),1,1);
		this.cancel.set(true);
		this.keggLoader.setCancel();
	}
}
