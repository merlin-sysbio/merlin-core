package pt.uminho.ceb.biosystems.merlin.core.operations.loaders;

import java.sql.SQLException;
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

@Operation(name="load KEGG annotation",description="load model with KEGG annotation")
public class LoadKeggAnnotation {


	private Project project;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private SearchAndLoadKeggData keggLoader;
	private AtomicBoolean cancel;

	@Port(direction=Direction.INPUT, name="workspace",description="select workspace", order=1)
	public void setProject(Project project){
		this.project = project;
	}
	

	@Port(direction=Direction.INPUT, name="organism", order=2)
	public void setOrganism(String organism) throws SQLException {
		
		Connection connection;
		connection = new Connection(this.project.getDatabase().getDatabaseAccess());
		if(TransportersAPI.checkReactionData(connection)) {

			this.cancel = new AtomicBoolean(false);

			this.keggLoader = new SearchAndLoadKeggData(this.cancel, this.progress);

			this.keggLoader.setTimeLeftProgress(this.progress);

			boolean output = this.keggLoader.getOrganismData(organism);

			if (output)			
				output = this.keggLoader.loadData(connection);

			if(output) {

				if(!this.keggLoader.isCancel().get()){

					MerlinUtils.updateAllViews(project.getName());
					Workbench.getInstance().info("annotation successfully loaded.");
				}
				else {

					Workbench.getInstance().warn("annotation loading cancelled!");
				}
			}
			else {

				Workbench.getInstance().info("an error occurred while performing the operation.");
			}
		}

		else{
			Workbench.getInstance().warn("Please load metabolic data!");
		}
	}

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
