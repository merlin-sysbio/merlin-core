package pt.uminho.ceb.biosystems.merlin.core.operations.project;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.NcbiAPI;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Annotations;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Database;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Entities;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Tables;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation.CompartmentsAnnotationDataContainer;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation.EnzymesAnnotationDataInterface;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation.TransportersAnnotationDataContainer;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.Pathway;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactantsProducts;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactionsInterface;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Genes;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Proteins;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Update;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.H2DatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.MySQLDatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;

/**
 * This is the class that creates a new project for the AIbench interface.
 * 
 *
 */
@Operation(name="Workspace",description="Create a new merlin project")
public class NewProject {

	private String databaseName = null;
	private String usr = null;
	private String pwd = null;
	private String host = null;
	private String port = null;
	//private String name = null;
	private DatabaseType dbType = DatabaseType.H2;
	//private String genomeID;
	private long taxonomyID;
	//	private boolean isFaaFastaFiles;
	//	private boolean isFnaFastaFiles;

	/**
	 *  
	 * @param host Host to use to log at the database
	 */
	@Port(direction=Direction.INPUT, name="Host",validateMethod ="validateHost", order=1)
	public void setHost(String host) {
		this.host = host;
	}

	/**
	 * 
	 * @param port Port to use to log at the database
	 */
	@Port(direction=Direction.INPUT, name="Port", order=2)
	public void setPort(String port) {
		this.port = port;
	}
	/**
	 * 
	 * @param usr User name to use to log at the database
	 */
	@Port(direction=Direction.INPUT, name="User", order=3)
	public void setUsr(String usr) {
		this.usr = usr;
	}

	/**
	 * 
	 * @param pwd Password to use to log at the database
	 */
	@Port(direction=Direction.INPUT, name="Password", order=4)
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	/**
	 * 
	 * @param dbType database type of the new project
	 */
	@Port(direction=Direction.INPUT,name="Database Type", order=5)
	public void setdbType(DatabaseType dbType) {
		this.dbType = dbType;
	}

	/**
	 * 
	 * @param db Name of the database to associate the project to.
	 */
	@Port(direction=Direction.INPUT,validateMethod ="validateDataBase", name="Database", order=6)
	public void setDb(String databaseName) {
		this.databaseName = databaseName;
	}

	//	/**
	//	 * 
	//	 * @param name Name of the new project
	//	 */
	//	@Port(direction=Direction.INPUT, validateMethod = "validateProjectName" ,name="New project name", order=7)
	//	public void setName(String name) {
	//		this.name = name;
	//
	//	}

	//	/**
	//	 * @param directory
	//	 */
	//	@Port(direction=Direction.INPUT, name="genomeID",order=10)
	//	public void setGenomeID(String genomeID) {
	//		this.genomeID = genomeID;
	//	}


	@Port(direction=Direction.INPUT, name="TaxonomyID",order=11, validateMethod = "validateTaxonomy" )
	public void setTaxonomyID(long taxonomyID) {

		this.taxonomyID = taxonomyID;
	}

	//	/**
	//	 * @param directory
	//	 */
	//	@Port(direction=Direction.INPUT, name="isFaaFastaFiles",order=13)
	//	public void setIsFaaFastaFiles(boolean isFaaFastaFiles) {
	//
	//		this.isFaaFastaFiles = isFaaFastaFiles;
	//	}
	//
	//	/**
	//	 * @param directory
	//	 */
	//	@Port(direction=Direction.INPUT, name="isFnaFastaFiles",order=14)
	//	public void setIsFnaFastaFiles(boolean isFnaFastaFiles) {
	//
	//		this.isFnaFastaFiles = isFnaFastaFiles;
	//	}

	/**
	 * @param taxonomyID
	 */
	public void validateTaxonomy(long taxonomyID) {

		//		if(taxonomyID>0) {
		//			
		//			try {
		//				
		//				System.out.println(NcbiAPI.getTaxonomyFromNCBI(taxonomyID, 0));
		//			} 
		//			catch (Exception e) {
		//				
		//				throw new IllegalArgumentException("Error validating taxonomy identifier, please verify your input and try agains.");
		//			}
		//		}
		//		else {
		//			
		//			throw new IllegalArgumentException("Please set a valid taxonomy identifier.");
		//		}
	}

	//	/**
	//	 * @param name
	//	 */
	//	public void validateProjectName(String name) {
	//
	//		List <String> projectNames = new ArrayList<String>();
	//
	//		List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);
	//
	//		for(int i=0; i<cl.size(); i++) {
	//
	//			ClipboardItem item = cl.get(i);
	//			projectNames.add(item.getName());
	//		}
	//
	//		if(name.isEmpty()) {
	//			while(projectNames.contains(name) || name.isEmpty())
	//				name = this.buildName(name);
	//		}
	//		else {
	//			if(projectNames.contains(name))
	//				throw new IllegalArgumentException("Project with the same name already exists!\nPlease insert another name.");
	//		}
	//
	//		this.name=name;
	//	}



	/**
	 * @param host
	 */
	public void validateHost(String host) {

		this.host  = host;

	}

	/**
	 * @param databaseName
	 */
	public void validateDataBase(String databaseName) {

		this.databaseName = databaseName;

		//		List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);
		//
		//		for (ClipboardItem item : cl) {
		//
		//			String host_previous = ((Project)item.getUserData()).getDatabase().getDatabaseAccess().get_database_host();
		//			String databaseName_previous = ((Project)item.getUserData()).getDatabase().getDatabaseAccess().get_database_name();
		//
		//			if(databaseName.equals(databaseName_previous) && host.equals(host_previous))
		//				throw new IllegalArgumentException("Project connected to the same data base already exists");
		//		}

	}

	/**
	 * 
	 * Method that returns the new project.
	 */
	/**
	 * @return
	 */
	@Port(direction=Direction.OUTPUT, name="workspace", order=15)
	public Project getDataBase() {

		DatabaseAccess dbAccess;
		boolean dbTypeH2 = false;

		if (this.dbType.equals(DatabaseType.MYSQL))			
			dbAccess = new MySQLDatabaseAccess(usr, pwd, host, port, databaseName);
		else{			
			dbAccess = new H2DatabaseAccess(usr, pwd, databaseName);
			dbTypeH2 = true;
		}

		Database database = new Database(dbAccess);

		try {

			Connection connection = new Connection(dbAccess);
			Statement statement = connection.createStatement();
			DatabaseMetaData metadata = connection.getMetaData();
			
			
			Update.checkForUpdates(database.getDatabaseName(), taxonomyID, dbTypeH2, statement);

			boolean exists = ProjectAPI.checkDatabaseMetadata(metadata);

			if(!exists) {

				String query = "ALTER TABLE `projects` ADD `compartments_tool` VARCHAR(60);";
				ProjectAPI.executeQuery(query, statement);
			}

			//		List<String> tablesNames = ProjectAPI.getAllTablesNames(statement);
			//		
			//		if(tablesNames.contains("projects"))
			//			Update.AlterTableNames(connection);

			String[] tables;

			tables = database.getDatabaseAccess().showTables();

			Tables existingTables = new Tables();
			Entity gene = null;
			Entity protein = null;
			Entity enzyme = null;
			//Entity compound = null;
			Entity reaction = null;
			Entity path = null;
			Entity repro = null;
			//Entity compoundsReactions = null;
			Entity homology = null;
			Entity transporters = null;
			Entity compartments = null;
			//			Entity tf = null;
			//			Entity enzirg = null;
			//			Entity ti = null;
			//			Entity tu = null;
			//			Entity promoter = null;

			for(int i=0;i<tables.length;i++) {

				if(!tables[i].equalsIgnoreCase("gene_to_metabolite_direction")) {

					String[] meta = dbAccess.getMeta(tables[i], connection);
					Table table = new Table(tables[i], meta, connection);

					if(table.getSize()!=null )
						existingTables.addToList(table);

					if(tables[i].equalsIgnoreCase("gene")) {

						gene = new Genes(table, "Genes", database.getUltimlyComplexComposedBy());
					}
					else if(tables[i].equalsIgnoreCase("protein")) {

						protein = new Proteins(table, "Proteins", database.getUltimlyComplexComposedBy());
					}

					//					else if(tables[i].equalsIgnoreCase("enzyme")) {
					//	
					//						enzyme = new EnzymesContainer(table, "Enzymes", database.getUltimlyComplexComposedBy());
					//					}

					else if(tables[i].equalsIgnoreCase("compound")) {

						//compound = new Compounds(table, "Compounds");
						//				enzirg = new EnzymeRegulator_UNUSED(dbt, "Enzymatic regulators");
						//				ti = new EnzimeInhibiter_UNUSED(dbt, "Effectors");
					}
					else if(tables[i].equalsIgnoreCase("stoichiometry")) {

						repro = new ReactantsProducts(table, "Metabolites");
						//compoundsReactions = new CompoundsReactions(table,"Compounds Reactions");
					}
					else if(tables[i].equalsIgnoreCase("reaction")) {

						reaction = new  ReactionsInterface(table, "Reactions");
					}
					//			else if(tables[i].equalsIgnoreCase("regulatory_event"))
					//			{
					//				tf = new  TranscriptionFactor(dbt, "Transcription factors");
					//			}
					//			else if(tables[i].equalsIgnoreCase("sigma_promoter"))
					//			{
					//			}
					else if(tables[i].equalsIgnoreCase("pathway")) {

						path = new  Pathway(table, "Pathways");
					}
					//			else if(tables[i].equalsIgnoreCase("transcription_unit"))
					//			{
					//				tu = new TranscriptionUnit(dbt, "Transcription Units");
					//			}
					//			else if(tables[i].equalsIgnoreCase("promoter"))
					//			{
					//				promoter = new Promoter(dbt, "Promoters");
					//			}

					if(tables[i].toLowerCase().equalsIgnoreCase("geneblast") || tables[i].equalsIgnoreCase("geneHomology")) {

						homology = new EnzymesAnnotationDataInterface(table, "Enzymes Homology Genes");
					}

					if(tables[i].toLowerCase().equalsIgnoreCase("genes")) {

						transporters = new TransportersAnnotationDataContainer (table, "Transport Homology Genes");
					}

					if(tables[i].toLowerCase().equalsIgnoreCase("psort_reports")) {

						compartments = new CompartmentsAnnotationDataContainer(table, "Compartments Data");
					}
				}
			}

			//		compoundReactions = new CompoundReactions(dbt);

			database.setTables(existingTables);

			ArrayList<Entity> entitiesList = new ArrayList<Entity>();

			if(gene!=null) {

				ArrayList<Entity> subs = new ArrayList<Entity>();
				//		if(tu!=null) subs.add(tu);
				//		if(promoter!=null) subs.add(promoter);

				if(subs!=null) {

					gene.setSubenties(subs);
				}
				entitiesList.add(gene);
			}

			if(protein!=null) {

				ArrayList<Entity> subs = new ArrayList<Entity>();

				//				if(enzyme!=null) {
				//
				//					subs.add(enzyme);
				//				}
				//		if(tf!=null) subs.add(tf);
				if(subs!=null) {

					protein.setSubenties(subs);
				}

				if(protein!=null) {

					entitiesList.add(protein);
				}
			}

			if(repro!=null) {

				ArrayList<Entity> subs = new ArrayList<Entity>();
				//		if(enzirg!=null) subs.add(enzirg);
				//		if(ti!=null) subs.add(ti);

				//				if(compoundsReactions!=null)
				//					subs.add(compoundsReactions);
				//				if(compound!=null)
				//					subs.add(compound);

				if(subs!=null)
					repro.setSubenties(subs);

				entitiesList.add(repro);
			}

			if(reaction!=null)
				entitiesList.add(reaction);

			if(path!=null)
				entitiesList.add(path);

			Entities entities = new Entities();
			entities.setEntities(entitiesList);
			database.setEntities(entities);

			entitiesList = new ArrayList<>();
			if(homology!=null)
				entitiesList.add(homology);	

			if(transporters!=null)
				entitiesList.add(transporters);	

			if(compartments!=null)
				entitiesList.add(compartments);	

			Annotations annotations = new Annotations();
			annotations.setAnnotations(entitiesList);
			database.setAnnotations(annotations);

			Project project = new Project(database);

			//project.setGenomeCodeName(this.genomeID);

			ProjectAPI.isSW_TransportersSearch(connection);
			ProjectAPI.isTransporterLoaded(connection);

			//			project.setCompartmentsLoaded(Project_Utils.findComparmtents(connection));
			//			project.setGeneDataAvailable(Project_Utils.isDatabaseGenesDataLoaded(connection));
			//			project.setMetabolicDataAvailable(Project_Utils.isMetabolicDataLoaded(connection));
			//			project.setCompartmentalisedModel(Project_Utils.isCompartmentalisedModel(connection));

			List<Entity> allentities = new ArrayList<Entity>();
			allentities.add(gene);
			allentities.add(protein);
			allentities.add(enzyme);
			//			allentities.add(compound);
			allentities.add(reaction);
			allentities.add(path);
			allentities.add(repro);
			//			allentities.add(compoundsReactions);
			allentities.add(homology);
			allentities.add(transporters);
			allentities.add(compartments);

			for(Entity ent : allentities)
				if(ent != null)
					ent.setProject(project);

			if(this.taxonomyID>0) {

				project.setTaxonomyID(this.taxonomyID);
				String[] orgData = ProjectAPI.getOrganismData(this.taxonomyID, statement);

				if (orgData == null){
					orgData =  NcbiAPI.ncbiNewTaxID(this.taxonomyID);
					ProjectAPI.updateOrganismData(taxonomyID, orgData, statement);
				}

				project.setOrganismName(orgData[0]);
				project.setOrganismLineage(orgData[1]);

			}

			project.getProjectID();
			//			project.setFaaFiles(isFaaFastaFiles);
			//			project.setFnaFiles(isFnaFastaFiles);

			return project;
		}
		catch (IllegalArgumentException e) {

			Workbench.getInstance().error("An error occurred while retrieving the taxonomy information.\nPlease verify the taxonomy ID and your internet connection.");
			Workbench.getInstance().error(e.getMessage());
		} 
		catch (SQLException e) {

			e.printStackTrace();
			Workbench.getInstance().error("An error occurred while creating project, please try again.");
		}
		return null;

	}
}
