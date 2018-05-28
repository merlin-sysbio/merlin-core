package pt.uminho.ceb.biosystems.merlin.core.operations.integration;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.IntegrateCompartmentsData;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.GeneCompartments;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

@Operation(name="integrate compartments", description="integrate compartments to the model reactions")
public class IntegrateCompartmentstoDatabase implements Observer {

	private boolean loaded;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private IntegrateCompartmentsData integration;
	private List<String> ignoreList;
	private AtomicBoolean cancel;
	private AtomicInteger processingCounter;
	private AtomicInteger querySize; 
	private long startTime;
	private boolean biochemical;
	private boolean transport;
	private Project project;
	
	@Port(direction=Direction.INPUT, name="biochemical", order=1)
	public void setBiochemical(boolean biochemical){

		this.biochemical = biochemical;
	};
	
	@Port(direction=Direction.INPUT, name="transporters", order=2)
	public void setTransporters(boolean transport){

		this.transport = transport;
	};
	
	@Port(direction=Direction.INPUT, name="ignore", order=3)
	public void setIgnore(List<String> ignore){

		this.ignoreList = ignore;
	};
	
	@Port(direction=Direction.INPUT, name="project", order=5)
	public void setProject(Project project){

		this.checkProject(project);
	};

	@Port(direction=Direction.INPUT, name="geneCompartments", order=6)
	public void setGeneCompartments(Map<String, GeneCompartments> geneCompartments){

//	public IntegrateCompartmentstoDatabase(boolean biochemical, boolean transport, String ignore, Double threshold, Project project, Map<String, GeneCompartments> geneCompartments) throws SQLException {
		
		try {
			this.cancel = new AtomicBoolean(false);
			this.startTime = GregorianCalendar.getInstance().getTimeInMillis();
			
			if(this.project.isGeneDataAvailable()) {
				
				this.progress = new TimeLeftProgress();
				this.querySize = new AtomicInteger();
				this.processingCounter = new AtomicInteger();
				this.cancel = new AtomicBoolean();

				this.integration = new IntegrateCompartmentsData(project, geneCompartments);
				this.integration.addObserver(this);
				this.integration.setTimeLeftProgress(progress);
				this.integration.setQuerySize(this.querySize);
				this.integration.setProcessingCounter(this.processingCounter);
				this.integration.setCancel(this.cancel);
				
//			try {
//				@SuppressWarnings("unused")
//				CompartmentsPrediction Predictions = new CompartmentsPrediction(project,this.threshold);
//			} catch (Exception e1) {
//				e1.printStackTrace();
//			}
				
				boolean result = false;
				
				if(!this.cancel.get()){
					if(this.loaded)				
						result = integration.initProcessCompartments();
					else
						result = integration.performIntegration();
				}
				
				if(this.biochemical && !this.cancel.get())
					result = integration.assignCompartmentsToMetabolicReactions(ignoreList);

				if(this.transport && project.isTransporterLoaded() && !this.cancel.get()) {

					try {
						
						result = integration.assignCompartmentsToTransportReactions(ignoreList);
					}
					catch (Exception e) {
							
						result = false;
						Workbench.getInstance().error(e);
					}
				}
				
				MerlinUtils.updateCompartmentsAnnotationView(project.getName());
				MerlinUtils.updateReactionsView(project.getName());

				if(result && !this.cancel.get()) {

//					MerlinUtils.updateAllViews(project.getName());
					Workbench.getInstance().info("Compartments integration complete!");
				}
				else{

					Workbench.getInstance().error("An error occurred while performing the operation.");
				}
			}
			else {

				Workbench.getInstance().error("Gene data for integration unavailable!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

	};
	
//	public void checkList(String ignore){ 
//
//		String[] ignoreArray = ignore.split(",");
//		ignoreList = new ArrayList<String>();
//		
//		for(String ig :ignoreArray)			
//			ignoreList.add(ig.toLowerCase().trim());
//	}
	
//	public void checkThreshold(Double threshold){ 
//
//		if(threshold<0 || threshold>1) {
//			
//			Workbench.getInstance().warn("The value must be between 0 and 1");
//		}
//		
//	}

	/**
	 * @param project
	 */
	public void checkProject(Project project){

		if(project == null) {

			throw new IllegalArgumentException("No Project Selected!");
		}
		else {
			
			this.project = project;

//			if(project.getGeneCompartments()== null)
//				throw new IllegalArgumentException("Please perform the compartments prediction operation before integrating compartments data.");
//			else
				if(!project.areCompartmentsPredicted())
					throw new IllegalArgumentException("Please perform the compartments prediction operation before integrating compartments data.");

			Statement stmt;

			try {
				
				Connection connection = new Connection(project.getDatabase().getDatabaseAccess());

				stmt = connection.createStatement();

				int comp_genes = ProjectAPI.countGenesInGeneHasCompartment(stmt);

				int	genes = ProjectAPI.countGenes(stmt);
				
				if(genes<comp_genes) {

					this.loaded = true;		
				}
				stmt.close();
			}
			catch (SQLException e) {e.printStackTrace();}	
		}
	}
	
	/**
	 * @return
	 */
	@Progress
	public TimeLeftProgress getProgress() {

		return this.progress;
	}
	
	@Override
	public void update(Observable o, Object arg) {

		progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.processingCounter.get(), this.querySize.get());
	}

	/**
	 * 
	 */
	@Cancel
	public void cancel() {
		
		Workbench.getInstance().warn("operation canceled!");
		
		this.progress.setTime(0,1,1);
		this.integration.cancel();
	}
}
