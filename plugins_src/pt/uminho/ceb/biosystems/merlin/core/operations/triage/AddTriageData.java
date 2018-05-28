package pt.uminho.ceb.biosystems.merlin.core.operations.triage;

import java.sql.Statement;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicInteger;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.transporters.core.transport.reactions.TransportReactionsGeneration;
import pt.uminho.ceb.biosystems.merlin.transporters.core.transport.reactions.parseTransporters.AlignedGenesContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

/**
 * @author Antonio Dias
 *
 */
@Operation(name="add TRIAGE data", description="adds TRIAGE data to similarities")
public class AddTriageData implements Observer {

	private TimeLeftProgress progress = new TimeLeftProgress();
	private AtomicInteger geneProcessingCounter;
	private AtomicInteger querySize; 
	private long startTime;

	@Port(direction=Direction.INPUT, name="workspace",description="select workspace", order=1)
	public void setProject(Project project) {

		this.startTime = GregorianCalendar.getInstance().getTimeInMillis();
		DatabaseAccess databaseAccess = project.getDatabase().getDatabaseAccess();
		String db_name = databaseAccess.get_database_name();
		String path = FileUtils.getWorkspaceTaxonomyTriageFolderPath(db_name, project.getTaxonomyID());
		String outPath = path+"UnAnnotatedTransporters.out";

		try {

			Connection connection = new Connection (project.getDatabase().getDatabaseAccess());
			Statement statement = connection.createStatement();
			TransportReactionsGeneration tre = new TransportReactionsGeneration(project.getTaxonomyID());

			List<AlignedGenesContainer> tc_data= tre.getCandidatesFromDatabase(outPath, project.getProjectID(), statement);
			boolean go=true;

			if(tre.getUnAnnotatedTransporters().size()>0) {

				float unAnnotated = tre.getUnAnnotatedTransporters().size();
				float initialSize = tre.getInitialHomolguesSize();
				go = MerlinUtils.unAnnotatedTransporters(unAnnotated,initialSize,outPath);
			}
			
			if(go) {
				
				//load candidates
				tre.parseAndLoadCandidates(tc_data, project.getProjectID(), statement, project.getDatabase().getDatabaseAccess().get_database_type());
			}

			this.querySize = new AtomicInteger(1);
			this.geneProcessingCounter = this.querySize;
			this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.geneProcessingCounter.get(), this.querySize.get(), "add TRIAGE data");
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

		Workbench.getInstance().info("TRIAGE data integration successful!");
		MerlinUtils.updateTransportersAnnotationView(project.getName());
	}

	@Override
	public void update(Observable arg0, Object arg1) {

		this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.geneProcessingCounter.get(), this.querySize.get(), "add TRIAGE data");
	}
}