package pt.uminho.ceb.biosystems.merlin.core.operations.loaders;

import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation.CompartmentsAnnotationDataContainer;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.CompartmentResult;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.CompartmentsInterface;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.WoLFPSORT;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

@Operation(name="Load Compartments' Reports", description="Load compartments reports' to merlin.")
public class LoadWoLFPSORTReports {


	private String link;
	private String tool = "WoLFPSORT";
	private TimeLeftProgress progress = new TimeLeftProgress();
	private CompartmentsInterface compartmentsInterface;

	/**
	 * @param file_dir
	 * @throws Exception 
	 */
	@Port(direction=Direction.INPUT, name="predictions link",description="predictions website link", validateMethod="checkFiles",order=2)
	public void setFile_dir(String link) throws Exception {

	}

	/**
	 * @param project
	 * @throws Exception 
	 */
	@Port(direction=Direction.INPUT, name="workspace",description="select workspace",validateMethod="checkProject", order=3)
	public void setProject(Project project) throws Exception {

		DatabaseAccess mysqlmt = project.getDatabase().getDatabaseAccess();
		Connection connection = new Connection(mysqlmt);
//		DatabaseMetaData metadata = connection.getMetaData();
		Statement statement = connection.createStatement();

		String query;
//		boolean exists = ProjectAPI.checkDatabaseMetadata(metadata);
//		
//		if(!exists) {
//			
//			query = "ALTER TABLE projects ADD compartments_tool VARCHAR(60);";
//			ProjectAPI.executeQuery(query, statement);
//		}
		
		query = "UPDATE projects SET compartments_tool = '"+ tool +"' WHERE id="+project.getProjectID()+";";
		ProjectAPI.executeQuery(query, statement);

		Map<String, CompartmentResult> results = new HashMap<>();

		boolean error = false;

		if(link != null && !link.equals("")) {
			
			Map<String, CompartmentResult> tempResults = null;
			
			try {
				this.compartmentsInterface = new WoLFPSORT();

				tempResults = compartmentsInterface.addGeneInformation(link);
				
			}
			catch (Exception e) {

				error=true;
				e.printStackTrace();
			}

			if(tempResults!=null)
				results.putAll(tempResults); 				
		}

		if(error) {

			Workbench.getInstance().error("An error occurred when performing the operation!");
		}
		else {

			if(results.isEmpty()) {

				Workbench.getInstance().warn("merlin could not find any compartments information, skipping results loading!");
			}
			else {

				CompartmentsAnnotationDataContainer.loadPredictions(project, tool, results, statement);

				MerlinUtils.updateCompartmentsAnnotationView(project.getName());
				Workbench.getInstance().info("compartments prediction loaded.");
			}
		}
		connection.closeConnection();
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
		this.compartmentsInterface.setCancel(new AtomicBoolean(true));
	}

	/**
	 * @param project
	 */
	public void checkProject(Project project) {

		if(project == null)
			throw new IllegalArgumentException("no project selected!");

		if(!Project.isFaaFiles(project.getDatabase().getDatabaseName(), project.getTaxonomyID()))
			throw new IllegalArgumentException("Please set amino acid fasta files!");
	}

	/**
	 * @param project
	 * @throws Exception 
	 */
	public void checkFiles(String link) {

		if(link == null)
			throw new IllegalArgumentException("please set valid link");
		else
			this.link = link;
	}
}
