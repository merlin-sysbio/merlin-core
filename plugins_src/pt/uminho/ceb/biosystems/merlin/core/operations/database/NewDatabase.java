package pt.uminho.ceb.biosystems.merlin.core.operations.database;

import java.util.Map;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.utilities.LoadFromConf;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseSchemas;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.ceb.biosystems.merlin.utilities.DatabaseFilesPaths;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

@Operation(description="create new empty database.", name="new database.")
public class NewDatabase {
	private String database;
	private DatabaseType dbType;

	@Port(name="workspace name:",description="set workspace name.",direction=Direction.INPUT,order=1)
	public void setDatabase(String database){
		this.database = database;
	}
	
	@Port(name="database type:",description="set the database type.",direction=Direction.INPUT,order=2)
	public void setdbType(DatabaseType dbType){
		this.dbType = dbType;
		
		Map<String, String> credentials = LoadFromConf.loadDatabaseCredentials(FileUtils.getConfFolderPath());
		String username = null, password = null, host = null, port = null;
		
		DatabaseType databaseType =  DatabaseType.H2;
		if (credentials.get("dbtype").equals("mysql"))
			databaseType = DatabaseType.MYSQL;
		
		username = credentials.get("username");
		password = credentials.get("password");
		if (databaseType.equals(DatabaseType.MYSQL)) {
			host = credentials.get("host");
			port = credentials.get("port");
		}
		
		long startTime = System.currentTimeMillis();
		
		DatabaseSchemas schemas = new DatabaseSchemas( username, password, host, port, this.dbType);
		

		boolean databaseT = false;

		if (dbType.equals(DatabaseType.MYSQL))
			databaseT = true;
		
		String[] filePath= DatabaseFilesPaths.getPathsList(databaseT);

		if(schemas.newSchemaAndScript(this.database, filePath)){
	    	long endTime = System.currentTimeMillis();
			Workbench.getInstance().info("Database "+this.database+" successfuly created.");
		}
		else
//			Workbench.getInstance().error("There was an error when trying to create "+this.database+"!!");
			Workbench.getInstance().error("Error! Check your database configuration file in merlin directory at /utilities/"+this.dbType.toString()+"_settings.cfg");
	}
}
