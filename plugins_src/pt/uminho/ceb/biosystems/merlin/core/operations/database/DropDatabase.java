package pt.uminho.ceb.biosystems.merlin.core.operations.database;

import java.io.File;
import java.io.IOException;
import java.util.List;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseSchemas;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

@Operation(description="Drop database.", name="Drop Database.")
public class DropDatabase{

	private String host;
	private String password;
	private String username;
	private String database;
	private String port;
	private DatabaseType dbType;

	/**
	 * @param project
	 */
	@Port(name="workspace",description="select workspace",direction=Direction.INPUT,order=1)
	public void setProject(Project project) throws IOException{
		
		this.host = project.getDatabase().getDatabaseAccess().get_database_host();
		this.password = project.getDatabase().getDatabaseAccess().get_database_password();
		this.username = project.getDatabase().getDatabaseAccess().get_database_user();
		this.port = project.getDatabase().getDatabaseAccess().get_database_port();
		this.database = project.getDatabase().getDatabaseAccess().get_database_name();
		this.dbType = project.getDatabase().getDatabaseAccess().get_database_type();
	
		DatabaseSchemas schemas = new DatabaseSchemas( this.username, this.password, this.host, this.port, this.dbType);
		
		if(schemas.dropDatabase(this.database)) {

			File databaseFolder = new File(FileUtils.getWorkspaceFolderPath(database));
			FileUtils.deleteDirectory(databaseFolder);
			
			Workbench.getInstance().info("Database "+this.database+" successfuly droped.");
			
			List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);
			for (ClipboardItem item : cl){
				DatabaseAccess link2 = ((Project)item.getUserData()).getDatabase().getDatabaseAccess();
				
				if(this.database.equals(link2.get_database_name()) && this.host.equals(link2.get_database_host())) {
					
					Core.getInstance().getClipboard().removeClipboardItem(item);
				}
			}
		}
		else {
			
			Workbench.getInstance().error("There was an error when trying to drop "+this.database+"!!");
		}
		
	}
}
