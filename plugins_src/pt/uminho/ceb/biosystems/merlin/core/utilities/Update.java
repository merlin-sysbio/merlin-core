package pt.uminho.ceb.biosystems.merlin.core.utilities;

import java.io.IOException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.utilities.datastructures.map.MapUtils;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

public class Update {
	
	/**
	 * Method to check for possible updates.
	 * 
	 * @param database
	 * @param taxonomyID
	 * @param statement
	 */
	public static void checkForUpdates(String database, long taxonomyID, boolean h2, Statement statement){
		
		try {
			
			ProjectAPI.checkIfTableUpdatesExists(statement);
				
			String path = FileUtils.getConfFolderPath() + "updates";
			
			Map<Integer, String> data = null;
			
			data = MapUtils.readFile(path, 0, 1, "\t");
			
			Set<Integer> forUpdate = ProjectAPI.checkIfUpdated(data, statement);
			
			if(forUpdate.size() > 0)
				ProjectAPI.update(forUpdate, data, statement);
			
			List<String> columns = ProjectAPI.getAllColumns("scorerConfig", statement);
			
			if(!columns.contains("blastdb") && !columns.contains("blastDB"))
				ProjectAPI.executeQuery("ALTER TABLE `scorerConfig` ADD COLUMN `blastDB` VARCHAR(45) NOT NULL AFTER `minHomologies`;", statement);
			
			if(!columns.contains("bestalpha") && !columns.contains("bestAlpha"))
				ProjectAPI.executeQuery("ALTER TABLE `scorerConfig` ADD COLUMN `bestAlpha` TINYINT(1) NOT NULL AFTER `blastDB`", statement);
			
			if(!columns.contains("latest"))
				ProjectAPI.executeQuery("ALTER TABLE `scorerConfig` ADD COLUMN `latest` TINYINT(1) NOT NULL AFTER `bestAlpha`;", statement);
			
			if(!columns.contains("upperThreshold") && !columns.contains("upperthreshold")) {
				ProjectAPI.executeQuery("ALTER TABLE `scorerConfig` ADD COLUMN `upperThreshold` FLOAT NOT NULL AFTER `threshold`;", statement);
				ProjectAPI.executeQuery("UPDATE scorerConfig SET upperThreshold = 1.0;", statement);
			}
			
			checkGPRTablesUpdates(statement);
			
			ProjectAPI.cleanProjectsTables(h2, statement);
		
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Method to alter tables names.
	 * 
	 * @param connection
	 */
	public static void AlterTableNames(Connection connection){
		
		try {
			
			String path = FileUtils.getUtilitiesFolderPath() + "alter_table_names.txt";
			
			Map<String, String> data = MapUtils.getInfoInFile(path, 0, 1, ",");
			
			for(String key : data.keySet())
				ProjectAPI.checkTableName(connection, key, data.get(key));
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Method to perform changes for GPRs identification.
	 * 
	 * @param connection
	 */
	private static void checkGPRTablesUpdates(Statement statement){
		
		try {
			
			List<String> columns = ProjectAPI.getAllColumns("subunit", statement);
			
			if(columns.contains("note")) {
				
				ProjectAPI.executeQuery("ALTER TABLE `subunit` DROP COLUMN `gpr_status`;", statement);
			
				ProjectAPI.executeQuery("ALTER TABLE `subunit` DROP COLUMN `note`;", statement);

				ProjectAPI.executeQuery("ALTER TABLE `subunit` DROP COLUMN `protein_complex_idprotein_complex`;", statement);
				
				ProjectAPI.executeQuery("ALTER TABLE `subunit` DROP COLUMN `module_id`;", statement);
				
				ProjectAPI.executeQuery("DROP TABLE IF EXISTS `protein_complex`;", statement);
				
				ProjectAPI.executeQuery("ALTER TABLE `enzyme` ADD COLUMN `gpr_status` VARCHAR(45) NULL AFTER `source`;", statement);
			
				ProjectAPI.executeQuery("CREATE TABLE IF NOT EXISTS `enzyme_has_module` (`enzyme_ecnumber` VARCHAR(15) NOT NULL, `enzyme_protein_idprotein`"
						+ " INT UNSIGNED NOT NULL, `module_id` INT NOT NULL, `name` VARCHAR(45) NULL, `note` VARCHAR(45) NULL, "
						+ " PRIMARY KEY (`enzyme_ecnumber`, `enzyme_protein_idprotein`, `module_id`), "
						+ "CONSTRAINT `fk_enzyme_has_module_enzyme1` FOREIGN KEY (`enzyme_ecnumber` , `enzyme_protein_idprotein`) "
						+ "REFERENCES `enzyme` (`ecnumber` , `protein_idprotein`) ON DELETE CASCADE ON UPDATE CASCADE, "
						+ "CONSTRAINT `fk_enzyme_has_module_module1` FOREIGN KEY (`module_id`) REFERENCES `module` (`id`) "
						+ "ON DELETE CASCADE ON UPDATE CASCADE);", statement);
			}
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
