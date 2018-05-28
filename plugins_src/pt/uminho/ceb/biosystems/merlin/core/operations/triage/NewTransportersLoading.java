package pt.uminho.ceb.biosystems.merlin.core.operations.triage;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.GregorianCalendar;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.transporters.core.transport.reactions.TransportReactionsGeneration;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

@Operation(name="Load Transporters", description="Load TCDB transporters annotations.")
public class NewTransportersLoading  implements Observer {
	
	private static final Logger logger = LoggerFactory.getLogger(NewTransportersLoading.class);
	
	private Project project;
	private File file;
	
	private AtomicBoolean cancel = new AtomicBoolean();
	private TimeLeftProgress progress = new TimeLeftProgress();
	private AtomicInteger counter;
	private AtomicInteger querySize; 
	private long startTime;
	

	@Port(direction=Direction.INPUT, name="Workspace",description="Select Workspace",validateMethod="checkProject", order=5)
	public void load(Project project) {

		this.project = project;
	}

	/**
	 * @param project
	 */
	public void checkProject(Project project) {

		if(project == null)
			Workbench.getInstance().error("No Project Selected!");
		else
			this.project = project;
	}

	@Port(name="File", direction=Direction.INPUT,validateMethod="validateFile", description="Select File", order=2)
	public void loadNewTransporter(File file) {

		Connection connection;
		try {
			
			this.startTime = GregorianCalendar.getInstance().getTimeInMillis();
			connection = new Connection (project.getDatabase().getDatabaseAccess());
			Statement statment = connection.createStatement();
			
			TransportReactionsGeneration tre = new TransportReactionsGeneration();
			
			this.counter = new AtomicInteger(0);
			tre.setCounter(this.counter);
			this.querySize = new AtomicInteger(0);
			tre.addObserver(this);
			tre.setQuerySize(this.querySize);
			tre.setCancel(this.cancel);
			
			boolean output = tre.parseAndLoadTransportersDatabase(project.getDatabase().getDatabaseAccess().get_database_name(),
					this.file, statment, project.getDatabase().getDatabaseAccess().get_database_type());

			if(output) 
				Workbench.getInstance().info("information successfully loaded!");
			else
				Workbench.getInstance().error("an error occurred while loading the information.");
			
			connection.closeConnection();
		} 
		catch (SQLException e) {
			
			e.printStackTrace();
		}
		
	}

	/**
	 * @param file
	 */
	public void validateFile(File file) {

		if(file.isDirectory())			
			throw new IllegalArgumentException("Please select a single file");

		if(file.getName().endsWith(".out"))			
			throw new IllegalArgumentException("Please send this file to odias@ceb.uminho.pt for format validation!");
		else
			if(file.getName().endsWith(".out_checked"))
				this.file = file;
			else				
				throw new IllegalArgumentException("Please select a file with transporters annotation!");
	}
	
	@Progress
	public TimeLeftProgress getProgress() {

		return progress;
	}

	/**
	 * @param cancel the cancel to set
	 */
	@Cancel
	public void setCancel() {

		this.progress.setTime(0, 0, 0);
		this.cancel.set(true);
	}

	@Override
	public void update(Observable o, Object arg) {

		logger.debug("Counter on {}: {}", this.getClass(), this.counter.get());
		progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.counter.get(), this.querySize.get());
	}

}
