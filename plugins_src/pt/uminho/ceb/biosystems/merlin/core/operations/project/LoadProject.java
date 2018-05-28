package pt.uminho.ceb.biosystems.merlin.core.operations.project;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.H2DatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.MySQLDatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;

/**
 * @author Oscar Dias
 *
 */
@Operation(name="load project", description="load existing project")
public class LoadProject {

	private Project project;

	@Port(name="file", direction=Direction.BOTH,validateMethod="validateFile", description="select File", order=1)
	public Project load(File file) {

		return this.project;
	}

	/**
	 * @param file
	 */
	public void validateFile(File file) {

		if(file.isDirectory())
			throw new IllegalArgumentException("Please select a project file");

		Project res = null;
		FileInputStream fi = null;
		try {

			fi = new FileInputStream(file);
			ObjectInputStream oi = new ObjectInputStream(fi);

			res = (Project) oi.readObject();

			oi.close();
			fi.close();

			String user = res.getDatabase().getDatabaseAccess().get_database_user();
			String port = res.getDatabase().getDatabaseAccess().get_database_port();
			String host = res.getDatabase().getDatabaseAccess().get_database_host();
			String password = res.getDatabase().getDatabaseAccess().get_database_password();
			String databaseName = res.getDatabase().getDatabaseAccess().get_database_name();
			DatabaseType databaseType = res.getDatabaseType();
			
//			try {
//
//				if(databaseType.equals(DatabaseType.MYSQL))
//					res.getDatabase().getDB_Credentials().openConnection();
//			} 
//			catch (MySQLNonTransientConnectionException c) {
//
//				System.out.println("NO CONNECTION AVAILABLE!!!");
//				String os_name = System.getProperty("os.name");
//				if(os_name.contains("Windows"))
//					DatabaseProcess.startDBProcess(user, password, host, port);
//				res.getDatabase().getDB_Credentials().openConnection();
//			}

			DatabaseAccess dsa1;
			if(databaseType.equals(DatabaseType.MYSQL))
				dsa1 = new MySQLDatabaseAccess(user, password, host, port, databaseName);
			else
				dsa1 = new H2DatabaseAccess(user, password, host);

			List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);

			for (ClipboardItem item : cl) {

				DatabaseAccess dsa = ((Project)item.getUserData()).getDatabase().getDatabaseAccess();

				if(dsa1.get_database_name().equals(dsa.get_database_name()) && dsa1.get_database_type().equals(dsa.get_database_type()))
					throw new IllegalArgumentException("Project connected to the same data base already exists");

			}
			fi.close();
			
			this.project = res;
		}
		catch (Exception e) {
			
			try {
				
				fi.close();
			} 
			catch (IOException e1) {

				e1.printStackTrace();
			}
			
			e.printStackTrace();

			System.out.println("Message "+e.getMessage()+" cause "+e.getCause());

			if(e.getCause()!= null && e.getCause().toString().toLowerCase().contains("Unknown database".toLowerCase())) {

				throw new IllegalArgumentException("The database used in this project " +
						"("+e.getCause().toString().replace("com.mysql.jdbc.exceptions.jdbc4.MySQLSyntaxErrorException: Unknown database ", "").trim()+")" +
						" was not found in this MySQL server!");
			}
			else if(e.getMessage()!= null && e.getMessage().toString().toLowerCase().contains("already exists")) {

				throw new IllegalArgumentException(e.getMessage());
			}
			else {

				throw new IllegalArgumentException("Project file incompatible with this version.\nPlease create a new project.", e.getCause());
			}
		}
	}
}
