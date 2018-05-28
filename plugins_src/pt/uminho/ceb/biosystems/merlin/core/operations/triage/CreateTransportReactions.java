package pt.uminho.ceb.biosystems.merlin.core.operations.triage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.Statement;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
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
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.transporters.core.transport.reactions.containerAssembly.PopulateTransportContainer;
import pt.uminho.ceb.biosystems.merlin.transporters.core.transport.reactions.containerAssembly.TransportContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

/**
 *
 */
@Operation(name="Create Transport Reactions", description="Generates transport reactions report")
public class CreateTransportReactions implements Observer {

	private static final Logger logger = LoggerFactory.getLogger(PopulateTransportContainer.class);
	private Project project;
	private double alpha;
	private final static int minimalFrequency = 2;
	private double threshold;
	private final static double beta = 0.05;
	private boolean saveOnlyReactionsWithKEGGmetabolites = true;
	private boolean validateReaction = true;
	private AtomicBoolean cancel;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private AtomicInteger geneProcessingCounter;
	private AtomicInteger querySize; 
	private long startTime;
	private String ignoreSymportMetabolites;
	private int generations;

	@Port(direction=Direction.INPUT, name="alpha",description="select alpha",validateMethod="setAlpha",order=1)
	public void setAlph(double alpha){};
	
	@Port(direction=Direction.INPUT, name="threshold",description="select threshold",validateMethod="setThreshold",order=2)
	public void setThold(double threshold){};
	
	@Port(direction=Direction.INPUT, name="ignore Metabolites",description="select metabolites to ignore",validateMethod="setIgnoreSymportMetabolites",order=3)
	public void setMetabolites(String metabolite){};
	
	@Port(direction=Direction.INPUT, name="Workspace",description="Select Workspace",//validateMethod="checkProject",
			order=4)
	public void setProject(Project project) {
		this.project = project;
		
		this.cancel = new AtomicBoolean(false);
		this.startTime = GregorianCalendar.getInstance().getTimeInMillis();
		DatabaseAccess databaseAccess = project.getDatabase().getDatabaseAccess();
		String db_name = databaseAccess.get_database_name();
		String filePrefix = "Th_"+threshold+"__al_"+alpha;
		//String dir = (db_name+"/"+filePrefix+"/reactionValidation"+this.validateReaction+"/kegg_only"+this.saveOnlyReactionsWithKEGGmetabolites);
//		String dir = (db_name+"/"+filePrefix);
		String folderPath = FileUtils.getWorkspaceTaxonomyTriageFolderPath(db_name, this.project.getTaxonomyID());
		if(!new File(folderPath).exists())
			new File(folderPath).mkdir();
		String path = folderPath.concat(filePrefix);
		
		try {
			
			TransportContainer transportContainer = this.project.getTransportContainer();
			
			if(transportContainer == null || transportContainer.getAlpha()!= this.alpha 
					|| transportContainer.getThreshold()!= this.threshold)
					{

				transportContainer = null;

				String fileName = path+".transContainer";

				if(new File(fileName).exists()) {

					File file = new File(fileName);
					file.createNewFile();
					FileInputStream f_in = new  FileInputStream (file);
					ObjectInputStream obj_in = new ObjectInputStream (f_in);

					try {
						
						transportContainer = (TransportContainer) obj_in.readObject();
						this.project.setTransportContainer(transportContainer);
						obj_in.close();
						f_in.close();
					}
					catch (ClassNotFoundException e) {
						e.printStackTrace();
					}
					
					this.querySize = new AtomicInteger(1);
					this.geneProcessingCounter = this.querySize;
					this.progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.geneProcessingCounter.get(), this.querySize.get());
				}
				else {
						
						Connection connection = new Connection(databaseAccess);
						Statement statement = connection.createStatement();
						int project_id = ProjectAPI.getProjectID(statement, project.getTaxonomyID());
						statement.close();
						
						Set<String> ignoreSymportMetabolitesSet = new HashSet<>();
						if(this.ignoreSymportMetabolites!=null || !this.ignoreSymportMetabolites.isEmpty()) {
							
							String[] set = this.ignoreSymportMetabolites.split(";");
							
							for(String s : set)
								ignoreSymportMetabolitesSet.add(s.trim());
						}
						
						PopulateTransportContainer populateTransportContainer = new PopulateTransportContainer(connection, this.alpha, CreateTransportReactions.minimalFrequency, CreateTransportReactions.beta, this.threshold,
								project.getTaxonomyID(), project_id, ignoreSymportMetabolitesSet, project.getDatabaseType());
						
						populateTransportContainer.getDataFromDatabase();
						this.geneProcessingCounter = new AtomicInteger(0);
						populateTransportContainer.setGeneProcessingCounter(this.geneProcessingCounter);
						this.querySize = new AtomicInteger(0);
						populateTransportContainer.addObserver(this);
						populateTransportContainer.setQuerySize(this.querySize);
						populateTransportContainer.setCancel(this.cancel);
						
						this.generations = 5;
						
						transportContainer = populateTransportContainer.loadContainer(this.saveOnlyReactionsWithKEGGmetabolites, generations);
						
						this.saveTransportContainerFile(transportContainer, fileName);

						if(this.validateReaction) {

							transportContainer = populateTransportContainer.containerValidation(transportContainer, false);
							transportContainer.setReactionsValidated(this.validateReaction);
						}

						populateTransportContainer.creatReactionsFiles(transportContainer,path);
						populateTransportContainer = null;
						
						connection.closeConnection();
					}

					if(this.cancel.get()) {

						Workbench.getInstance().info("transport reactions creation cancelled!");
					}
					else {

						System.gc();
						this.project.setTransportContainer(transportContainer);
						Workbench.getInstance().info("transport reactions successfully created!");
					}
			}
			else {

				Workbench.getInstance().info("transport reactions already created!");
			}
		} 
		catch (Exception e) {

			e.printStackTrace();
			Workbench.getInstance().error(e.getMessage());
		} 
		MerlinUtils.updateTransportersAnnotationView(project.getName());
		MerlinUtils.updateProjectView(project.getName());
	}
	
	public void setAlpha(double alpha){
		this.alpha = alpha;
	}

	public void setThreshold(double threshold){
		this.threshold = threshold;
	}
	
	public void setIgnoreSymportMetabolites(String ignoreSymportMetabolites) {
		this.ignoreSymportMetabolites = ignoreSymportMetabolites;
	}

	/**
	 * @param transportContainer
	 * @param fileName
	 * @return
	 */
	private boolean saveTransportContainerFile(TransportContainer transportContainer, String fileName) {

		try {

			File transContainer = new File(fileName);
			transContainer.createNewFile();
			FileOutputStream f_out = new  FileOutputStream(transContainer);
			ObjectOutputStream obj_out = new ObjectOutputStream (f_out);
			obj_out.writeObject(transportContainer);
			obj_out.close();
			f_out.close();
			return true;
		} 
		catch (IOException e1) {

			e1.printStackTrace();
		} 
		return false;
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

	@Override
	public void update(Observable o, Object arg) {

		logger.debug("Counter on {}: {}", this.getClass(), this.geneProcessingCounter.get());
		progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.geneProcessingCounter.get(), this.querySize.get());
	}
}
