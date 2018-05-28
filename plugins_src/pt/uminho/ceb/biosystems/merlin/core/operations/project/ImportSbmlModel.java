package pt.uminho.ceb.biosystems.merlin.core.operations.project;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.NcbiAPI;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.Enumerators.ModelSources;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.readers.MerlinImportUtils;
import pt.uminho.ceb.biosystems.merlin.core.remote.loader.LoadSbmlData;
import pt.uminho.ceb.biosystems.merlin.core.remote.loader.kegg.DatabaseInitialData;
import pt.uminho.ceb.biosystems.merlin.core.utilities.LoadFromConf;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseSchemas;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.ceb.biosystems.merlin.utilities.DatabaseFilesPaths;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.io.readers.JSBMLReader;


/**
 * @author amaromorais
 *
 */
@Operation(description="Import model from SBML.")
public class ImportSbmlModel {

	private String workspace;
	private String username = null, password = null, host = null, port = null;
	private DatabaseType databaseType;
	private ModelSources source;
	private File model;
	private Long taxonomyID;

	//	private AtomicBoolean cancel; 


	@Port(direction=Direction.INPUT, name="new workspace", validateMethod = "checkNewWorkspace", description = "", defaultValue = "test_import_final", order=1)
	public void setProject(String newWorkspace) {

	}

	/**
	 * @param newWorkspace name
	 */
	public void checkNewWorkspace(String newWorkspace) {

		if(newWorkspace.equals("")) {

			throw new IllegalArgumentException("Please set the workspace name.");
		}
		else {
			this.workspace = newWorkspace;

			Map<String, String> credentials = LoadFromConf.loadDatabaseCredentials(FileUtils.getConfFolderPath());

			this.databaseType = DatabaseType.H2;
			if (credentials.get("dbtype").equals("mysql"))
				this.databaseType = DatabaseType.MYSQL;

			this.username = credentials.get("username");
			this.password = credentials.get("password");
			if (this.databaseType.equals(DatabaseType.MYSQL)) {
				this.host = credentials.get("host");
				this.port = credentials.get("port");
			}

			DatabaseSchemas schemas = new DatabaseSchemas( this.username, this.password, this.host, this.port, this.databaseType);

			boolean databaseT = false;

			if (this.databaseType.equals(DatabaseType.MYSQL))
				databaseT = true;

			String[] filePath= DatabaseFilesPaths.getPathsList(databaseT);
			
			try {
				schemas.newSchemaAndScript(this.workspace, filePath);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Workbench.getInstance().error("There was an error when trying to create the workspace!!");
			}
		}
	}

	@Port(direction=Direction.INPUT,validateMethod="",name="sbml source", description="",order=2)
	public void setExtensions(ModelSources modelSource) {

		this.source = modelSource;

	}

	/**
	 * @param directory
	 * @throws IOException 
	 */
	@Port(direction=Direction.INPUT, name="sbml model:",description="",validateMethod="checkModelPath",order=3)
	public void selectDirectory(File model){

	}


	/**
	 * @param taxID
	 */
	@Port(direction=Direction.INPUT, name="taxonomy ID:",description="",validateMethod="checkTaxonomyID", defaultValue = "323259", order=4)
	public void setNewWorkspaceTaxID(Long taxID){
		
		if(!model.getAbsolutePath().endsWith(".xml") && !model.getAbsolutePath().endsWith(".sbml")){

			throw new IllegalArgumentException("Please select a valid '.xml'/'.sbml' file!");
		}

		try {

			JSBMLReader reader = new JSBMLReader(model.getAbsolutePath(), "NoName");			
			Container cont = new Container(reader);

			MerlinImportUtils data = new MerlinImportUtils(cont);

			Connection conn = new Connection(this.host, this.port, this.workspace, this.username, this.password, this.databaseType);

			DatabaseInitialData databaseInitialData = new DatabaseInitialData(conn);
			databaseInitialData.retrieveAllData();

			TimeLeftProgress progress = new TimeLeftProgress();
			AtomicInteger datum = new AtomicInteger(0);
			AtomicBoolean cancel = new AtomicBoolean(false);
			AtomicInteger dataSize = new AtomicInteger(10); 
			Long startTime = System.currentTimeMillis();


			Runnable importModel;

			importModel = new LoadSbmlData(data, conn, databaseInitialData, progress, datum, cancel, dataSize, startTime);
			importModel.run();

			conn.closeConnection();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new IllegalArgumentException("Error while importing.");
		}

		Workbench.getInstance().info("Model successfully imported with name: " + this.workspace);
	}



	/**
	 * @param directory
	 */
	public void checkModelPath(File model) {

		if(model == null || model.toString().isEmpty()) {

			throw new IllegalArgumentException("Please select a '.xml'/'.sbml' file!");
		}
		else
			this.model = model;
		
	}


	/**
	 * @param taxID
	 */
	public void checkTaxonomyID(Long taxID){

		if(taxID!= null && !taxID.toString().isEmpty() && taxID>0) {

			try {
				NcbiAPI.getTaxonomyFromNCBI(taxID, 0);
				this.taxonomyID = taxID;
			} 
			catch (Exception e) {

				throw new IllegalArgumentException("error validating taxonomy identifier, please verify your input and try again.");
			}
		}
		else {

			Workbench.getInstance().error("please insert a valid taxonomy identifier!");
		}

	}


	//	/**
	//	 * 
	//	 */
	//	@Cancel
	//	public void cancel() {
	//		
	//		this.cancel.set(true);
	////		this.keggLoader.setCancel();
	//	}
}
