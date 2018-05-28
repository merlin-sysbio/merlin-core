package pt.uminho.ceb.biosystems.merlin.core.operations.database;

import java.sql.Statement;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.SchemaType;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Update;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseSchemas;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.ceb.biosystems.merlin.utilities.DatabaseFilesPaths;

@Operation(description="clean database.", name="clean database.")
public class CleanDatabase {

	private Project project;

	/**
	 * @param project
	 */
	@Port(name="workspace:",description="select workspace",direction=Direction.INPUT,order=1)
	public void setProject(Project project){
		this.project = project;
	}

	/**
	 * @param schema
	 */
	@Port(name="select information",description="be carefull when selecting information to delete! ", defaultValue = "ignore", direction = Direction.INPUT, order=2)
	public void cleanSchema(SchemaType schema) {
		
		DatabaseSchemas schemas = new DatabaseSchemas( this.project.getDatabase().getDatabaseAccess().get_database_user(),
				this.project.getDatabase().getDatabaseAccess().get_database_password(),
				this.project.getDatabase().getDatabaseAccess().get_database_host(),
				this.project.getDatabase().getDatabaseAccess().get_database_port(),
				this.project.getDatabase().getDatabaseAccess().get_database_type());


		String[] filePath=new String[1];
		
		boolean databaseType = false;
		
		switch (schema)
		{
		case all_information:
		{
			filePath=new String[7];
			
			if (this.project.getDatabase().getDatabaseAccess().get_database_type().equals(DatabaseType.MYSQL)){
				databaseType = true;
				filePath=new String[8];
			}
				
			filePath = DatabaseFilesPaths.getPathsList(databaseType);
			
			this.project.setInitialiseHomologyData(true);
			break;
		}
		case model: {

			filePath[0] = DatabaseFilesPaths.getModelPath();
			
			break;
		}
		case transport_proteins:
		{
			filePath[0]= DatabaseFilesPaths.getTransportersIdentificationPath();
			
			this.project.setTransportContainer(null);
			break;
		}
		case enzymes_annotation:
		{
			filePath[0]= DatabaseFilesPaths.getEnzymesAnnotationPath();
			
			this.project.setInitialiseHomologyData(true);
			break;
		}
		case transport_annotations:
		{
			
			if (this.project.getDatabase().getDatabaseAccess().get_database_type().equals(DatabaseType.MYSQL))
				databaseType = true;

			filePath = DatabaseFilesPaths.getTransportAnnotationPath(databaseType);
			
			this.project.setTransportContainer(null);
			break;
		}
		case compartment_annotation: {

			filePath[0] = DatabaseFilesPaths.getCompartmentsAnnotationPath();
			
			if(this.project.isCompartmentalisedModel()) {
			
				Workbench.getInstance().warn("Compartments already integrated in model. To remove compartments from model, all KEGG information should be removed and the database re-loaded!");
			}
			
			break;
		}
		case interpro_annotation:
		{
			filePath=new String[1];
			filePath[0] = DatabaseFilesPaths.getInterproAnnotationPath();
			break;
		}
		case ignore:
		{
			break;
		}
		}

		if(schema.equals(SchemaType.ignore)) {

			Workbench.getInstance().info("Database cleaning ignored.");
		}
		else {
			try {
				String database = this.project.getDatabase().getDatabaseAccess().get_database_name();
				long taxonomyID = this.project.getTaxonomyID();
				
				DatabaseAccess dbAccess = this.project.getDatabase().getDatabaseAccess();
				Connection connection = new Connection(dbAccess);
				Statement statement = connection.createStatement();
				
				Object[] data = ProjectAPI.getAllOrganismData(taxonomyID, statement);
				
				if(schemas.cleanSchema(this.project.getDatabase().getDatabaseAccess().get_database_name(), filePath)) {
					
					MerlinUtils.updateAllViews(project.getName());
					
					if(schema.equals(SchemaType.all_information)){
						
						ProjectAPI.dropTableUpdates(statement);
						
						Update.checkForUpdates(database, taxonomyID, !databaseType ,statement);
						
						ProjectAPI.restoreColumnCompartmentsTools(statement);

						ProjectAPI.setOrganismData(data, statement);
					}
					
					else if(schema.equals(SchemaType.compartment_annotation) || schema.equals(SchemaType.transport_proteins)){
						
						ProjectAPI.restoreOrganismColumns(statement);
						
						ProjectAPI.setOrganismData(data, statement);
					}
					
					Workbench.getInstance().info("Database "+ database +" successfuly cleaned.");
				}
				else {
					
					Workbench.getInstance().error("There was an error when trying to format "+this.project.getDatabase().getDatabaseAccess().get_database_name()+"!!");
				}
			} catch (Exception e) {
				Workbench.getInstance().error("There was an error when trying to format "+this.project.getDatabase().getDatabaseAccess().get_database_name()+"!!");
				e.printStackTrace();
			}
		}

	}
}
