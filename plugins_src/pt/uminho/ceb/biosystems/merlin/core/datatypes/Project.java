package pt.uminho.ceb.biosystems.merlin.core.datatypes;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Observable;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.Enumerators.FileExtensions;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.ceb.biosystems.merlin.transporters.core.transport.reactions.containerAssembly.TransportContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

/**
 * @author Oscar Dias
 *
 */
@Datatype(structure = Structure.COMPLEX,namingMethod="getName",removable=true,removeMethod ="removeProject")
public class Project extends Observable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Database database;
	private DatabaseType databaseType;

	private String fileName;
//	private String name;
//	private String proxy_host = "";
//	private String proxy_port = "";
//	private String proxy_username = "";
//	private String proxy_password = "";
//	private Map <String,String> oldPID;
//	private String genomeCodeName;
//	private boolean useProxy;
//	private boolean useAuthentication;
	private static int counter=0;
	private TransportContainer transportContainer;
	@Deprecated
//	private Map<String, CompartmentResult> compartmentResults;
	private boolean initialiseHomologyData;
	@Deprecated
//	private Map<String, GeneCompartments> geneCompartments;
	private long taxonomyID;
	private String organismName;
	private String organismLineage;

	/**
	 * @param database
	 * @param name
	 */
	public Project(Database database) {

		this.database = database;
		this.databaseType = database.getDatabaseType();
//		this.name = this.getName();
		this.fileName = "";
		this.setTransportContainer(null);
		this.taxonomyID = -1;
	}

	@Clipboard(name="model",order=1)
	public Entities getEntities() {
		return this.database.getEntities();
	}
	
	@Clipboard(name="annotation",order=2)
	public Annotations getAnnotations() {
		return this.database.getAnnotations();
	}

//	@Clipboard(name="database")
	public Database getDatabase() {
		return database;
	}

	public void setDatabase(Database db) {
		this.database = db;
	}
	
	public DatabaseType getDatabaseType() {
		return this.databaseType;
	}

	public String getName() {
		return this.database.getDatabaseName();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	/**
	 * @return the taxonomyID
	 */
	public long getTaxonomyID() {
		return taxonomyID;
	}

	/**
	 * @param taxonomyID the taxonomyID to set
	 */
	public void setTaxonomyID(long taxonomyID) {

		this.taxonomyID = taxonomyID;
	}

	/**
	 * @return
	 */
	public static int getCounter() {
		return counter;
	}

	/**
	 * @param counter
	 */
	public static void setCounter(int counter) {
		Project.counter = counter;
	}

	/**
	 * 
	 */
	public void removeProject(){

		List<ClipboardItem> items = Core.getInstance().getClipboard().getItemsByClass(Project.class);
		ClipboardItem torem = null;
		for(ClipboardItem item : items){
			
			if(item.getUserData().equals(this)){
				
				torem = item;
				break;
			}
		}
		Core.getInstance().getClipboard().removeClipboardItem(torem);
		System.gc();
	}

//	/**
//	 * @return the proxy_host
//	 */
//	public String getProxy_host() {
//
//		return proxy_host;
//	}
//
//	/**
//	 * @param proxy_host the proxy_host to set
//	 */
//	public void setProxy_host(String proxy_host) {
//		this.proxy_host = proxy_host;
//	}
//
//	/**
//	 * @return the proxy_port
//	 */
//	public String getProxy_port() {
//		return proxy_port;
//	}
//
//	/**
//	 * @param proxy_port the proxy_port to set
//	 */
//	public void setProxy_port(String proxy_port) {
//		this.proxy_port = proxy_port;
//	}
//
//	/**
//	 * @return the proxy_username
//	 */
//	public String getProxy_username() {
//		return proxy_username;
//	}
//
//	/**
//	 * @param proxy_username the proxy_username to set
//	 */
//	public void setProxy_username(String proxy_username) {
//		this.proxy_username = proxy_username;
//	}
//
//	/**
//	 * @return the proxy_password
//	 */
//	public String getProxy_password() {
//		return proxy_password;
//	}
//
//	/**
//	 * @param proxy_password the proxy_password to set
//	 */
//	public void setProxy_password(String proxy_password) {
//		this.proxy_password = proxy_password;
//	}
//
//	/**
//	 * @param useProxy the useProxy to set
//	 */
//	public void setUseProxy(boolean useProxy) {
//		this.useProxy = useProxy;
//	}
//
//	/**
//	 * @return the useProxy
//	 */
//	public boolean isUseProxy() {
//		return useProxy;
//	}

//	/**
//	 * @param useAuthentication the useAuthentication to set
//	 */
//	public void setUseAuthentication(boolean useAuthentication) {
//		this.useAuthentication = useAuthentication;
//	}
//
//	/**
//	 * @return the useAuthentication
//	 */
//	public boolean isUseAuthentication() {
//		return useAuthentication;
//	}

//	/**
//	 * @param mysqlPID
//	 */
//	public void setMysqlPID(String mysqlPID) {
//		this.mysqlPID = mysqlPID;
//	}
//
//	/**
//	 * @return
//	 */
//	public String getMysqlPID() {
//		return mysqlPID;
//	}

//	/**
//	 * @param oldPID the oldPID to set
//	 */
//	public void setOldPID(Map <String,String> oldPID) {
//		this.oldPID = oldPID;
//	}
//
//	/**
//	 * @return the oldPID
//	 */
//	public Map <String,String> getOldPID() {
//		return oldPID;
//	}

//	/**
//	 * @return the genomeID
//	 */
//	public String getGenomeCodeName() {
//		return genomeCodeName;
//	}
//
//	/**
//	 * @param genomeID the genomeID to set
//	 */
//	public void setGenomeCodeName(String genomeID) {
//		this.genomeCodeName = genomeID;
//	}

//	/**
//	 * @return the isNCBIGenome
//	 */
//	public boolean isNCBIGenome() {
//		return isNCBIGenome;
//	}

//	/**
//	 * @param isNCBIGenome the isNCBIGenome to set
//	 */
//	public void setNCBIGenome(boolean isNCBIGenome) {
//		this.isNCBIGenome = isNCBIGenome;
//	}

	/**
	 * @return the transportContainer
	 */
	public TransportContainer getTransportContainer() {
		return transportContainer;
	}

	/**
	 * @param transportContainer the transportContainer to set
	 */
	public void setTransportContainer(TransportContainer transportContainer) {
		this.transportContainer = transportContainer;
	}

	/**
	 * @return the sw_TransportersSearch
	 */
	public boolean isSW_TransportersSearch() {
		boolean out = false;
		Connection connection;
		try {
			connection = new Connection(this.database.getDatabaseAccess());
			out = ProjectAPI.isSW_TransportersSearch(connection);
			connection.closeConnection();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return out; 
	}
	
	/**
	 * @return
	 */
	public boolean isTransportersIntegrated() {
		boolean out = false;
		Connection connection;
		try {
			connection = new Connection(this.database.getDatabaseAccess());
			out = ProjectAPI.isTransportersIntegrated(connection, this.getProjectID());
			connection.closeConnection();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return out; 
	}

	/**
	 * @param sw_TransportersSearch the sw_TransportersSearch to set
	 */
	@Deprecated
	public void setSW_TransportersSearch(boolean sw_TransportersSearch) {
		
		
	}

	/**
	 * @return
	 */
	public boolean isTransporterLoaded() {
		boolean out = false;
		Connection connection;
		try {

			connection = new Connection(this.database.getDatabaseAccess());
			out = ProjectAPI.isTransporterLoaded(connection);
			connection.closeConnection();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return out;
	}

	/**
	 * @param transporterLoaded
	 */
	@Deprecated
	public void setTransporterLoaded(boolean transporterLoaded) {

	}

	/**
	 * @return
	 */
	public boolean isInitialiseHomologyData() {
		return initialiseHomologyData;
	}

	/**
	 * @param initialiseHomologyData
	 */
	public void setInitialiseHomologyData(boolean initialiseHomologyData) {
		this.initialiseHomologyData = initialiseHomologyData;
	}

	/**
	 * @return
	 */
	public boolean areCompartmentsPredicted () {

		boolean compartmentsLoaded = false;
		Connection connection;
		try {

			connection = new Connection(this.database.getDatabaseAccess());
			compartmentsLoaded = ProjectAPI.findComparmtents(connection);
			connection.closeConnection();
		}
		catch (SQLException e) {
			
			e.printStackTrace();
		}

		return compartmentsLoaded;
	}

	/**
	 * @return
	 */
	public boolean isGeneDataAvailable() {

		boolean geneDataAvailable = false;

		Connection connection;

		try {

			connection = new Connection(this.database.getDatabaseAccess());
			geneDataAvailable = ProjectAPI.isDatabaseGenesDataLoaded(connection);
			connection.closeConnection();
			
		}
		catch (SQLException e) {

			e.printStackTrace();
		}
		return geneDataAvailable;
	}

	/**
	 * @return
	 */
	public boolean isMetabolicDataAvailable() {

		//if(!this.metabolicDataAvailable) {
		boolean metabolicDataAvailable = false;
		Connection connection;
		try {

			connection = new Connection(this.database.getDatabaseAccess());
			metabolicDataAvailable = ProjectAPI.isMetabolicDataLoaded(connection);
			connection.closeConnection();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		//}

		return metabolicDataAvailable;
	}

	//	/**
	//	 * @param metabolicDataAvailable
	//	 */
	//	public void setCompartmentalisedModel(boolean compartmentalisedModel) {
	//		this.compartmentalisedModel = compartmentalisedModel;
	//	}

	/**
	 * @return
	 */
	public boolean isCompartmentalisedModel() {

		boolean compartmentalisedModel = false;
		Connection connection;
		try {

			connection = new Connection(this.database.getDatabaseAccess());
			compartmentalisedModel = ProjectAPI.isCompartmentalisedModel(connection);
			connection.closeConnection();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return compartmentalisedModel;
	}
	
	/**
	 * @return
	 */
	public String getEbiBlastDatabase() {
		
		String out = null;
		Connection connection;
		try {

			connection = new Connection(this.database.getDatabaseAccess());
			out = ModelAPI.getEbiBlastDatabase(connection);
			connection.closeConnection();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return out;
	}
	
	/**
	 * @return
	 */
	public String getNcbiBlastDatabase() {
		
		String out = null;
		Connection connection;
		try {

			connection = new Connection(this.database.getDatabaseAccess());
			out = ModelAPI.getNcbiBlastDatabase(connection);
			connection.closeConnection();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return out;
	}
	
	/**
	 * @return
	 */
	public String getHmmerDatabase() {
		
		String out = null;
		Connection connection;
		try {

			connection = new Connection(this.database.getDatabaseAccess());
			out = ModelAPI.getHmmerDatabase(connection);
			connection.closeConnection();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return out;
	}
	
	
	/**
	 * verify if a database directory exists, if not creats it
	 * @param databaseName
	 * @return
	 */
	public static boolean checkFastaExistence(String databaseName, Long taxID, String extension) {
		File newPath = null;
		if (extension==".faa")
			newPath = new File(FileUtils.getWorkspaceTaxonomyFolderPath(databaseName, taxID) + FileExtensions.PROTEIN_FAA.getExtension());
		else
			newPath = new File(FileUtils.getWorkspaceTaxonomyFolderPath(databaseName, taxID) + FileExtensions.CDS_FROM_GENOMIC.getExtension());
		
		if (newPath.exists())
			return true;
		
		return false;
	}
	
	public static boolean isFnaFiles(String databaseName, Long taxID) {
		
		return Project.checkFastaExistence(databaseName, taxID, ".fna");
	}
	

	public static boolean isFaaFiles(String databaseName, Long taxID) {
		
		return Project.checkFastaExistence(databaseName, taxID, ".faa");
}

	/**
	 * @return the project_id
	 */
	public int getProjectID() {

		try {
			
			Statement statement = new Connection(this.getDatabase().getDatabaseAccess()).createStatement();
			int project_id = ProjectAPI.getProjectID(statement, this.getTaxonomyID());
			statement.close();
			
			return project_id;
		} 
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * @param project_id the project_id to set
	 */
	@Deprecated
	public void setProjectID(int projectID) {

	}

	/**
	 * @return the organismName
	 */
	public String getOrganismName() {
		return organismName;
	}

	/**
	 * @param organismName the organismName to set
	 */
	public void setOrganismName(String organismName) {
		this.organismName = organismName;
	}

	/**
	 * @return the organismLineage
	 */
	public String getOrganismLineage() {
		return organismLineage;
	}

	/**
	 * @param organismLineage the organismLineage to set
	 */
	public void setOrganismLineage(String organismLineage) {
		this.organismLineage = organismLineage;
	}
}
