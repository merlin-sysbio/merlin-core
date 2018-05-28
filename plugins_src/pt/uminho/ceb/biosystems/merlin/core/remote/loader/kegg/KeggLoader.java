/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.remote.loader.kegg;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.CompartmentsAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseUtilities;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.CompartmentContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.EnzymeContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.GeneContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.MetaboliteContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.ModuleContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.PathwaysHierarchyContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.ReactionContainer;

/**
 * @author ODias
 *
 */
public class KeggLoader {

	private static int _LOWERBOUND = -999999, _UPPERBOUND = 999999;
	private int compartmentID;
	private ConcurrentHashMap<String,Integer> genes_id,
	metabolites_id, chromosome_id, proteins_id, reactions_id, pathways_id, 
	modules_id,
	orthologues_id;
	//	, similar_metabolites_to_load;
	private ConcurrentLinkedQueue<String> enzymesInModel;
	private ConcurrentHashMap<Integer,Set<String>> reactionsPathway, metabolitesPathway, modulesPathway;
	private ConcurrentHashMap<String,Set<String>> enzymesPathway;
	private ConcurrentLinkedQueue<Integer> reactionsPathwayList, metabolitesPathwayList, modulesPathwayList;
	private ConcurrentLinkedQueue<String> compoundsWithBiologicalRoles, enzymesPathwayList;
	private ConcurrentHashMap<String,List<String>> genes_modules_id;
	private Connection connection;
	private ConcurrentHashMap<String, List<Integer>> enzymesReactions;
	
	private boolean importSBML;


	/**
	 * @param databaseInitialData
	 * @param compoundsWithBiologicalRoles
	 * @throws SQLException
	 */
	public KeggLoader(DatabaseInitialData databaseInitialData, ConcurrentLinkedQueue<String> compoundsWithBiologicalRoles, boolean importFromSBML) throws SQLException {

		this.enzymesInModel = databaseInitialData.getEnzymesInModel();
		this.genes_id= databaseInitialData.getGenes_id();
		this.chromosome_id=databaseInitialData.getChromosome_id();
		this.metabolites_id=databaseInitialData.getMetabolites_id();
		this.proteins_id=databaseInitialData.getProteins_id();
		this.reactions_id=databaseInitialData.getReactions_id();
		this.pathways_id=databaseInitialData.getPathways_id();
		this.modules_id=databaseInitialData.getModules_id();
		this.orthologues_id=databaseInitialData.getOrthologues_id();
		this.reactionsPathway=databaseInitialData.getReactionsPathway();
		this.enzymesReactions=databaseInitialData.getEnzymesReactions(); 
		this.enzymesPathway=databaseInitialData.getEnzymesPathway();
		this.metabolitesPathway=databaseInitialData.getMetabolitesPathway();
		this.modulesPathway=databaseInitialData.getModulesPathway();
		//		this.similar_metabolites_to_load=databaseInitialData.getSimilar_metabolites();
		this.reactionsPathwayList=databaseInitialData.getReactionsPathwayList();
		this.enzymesPathwayList=databaseInitialData.getEnzymesPathwayList();
		this.modulesPathwayList=databaseInitialData.getModulesPathwayList();
		this.metabolitesPathwayList=databaseInitialData.getMetabolitesPathwayList();
		this.setCompoundsWithBiologicalRoles(compoundsWithBiologicalRoles);
		this.connection = databaseInitialData.getConnection();
		this.genes_modules_id = new ConcurrentHashMap<String, List<String>>();
		this.compartmentID = -1;
		this.importSBML = importFromSBML;
	}

	/**
	 * @param geneContainer
	 * @throws SQLException
	 */
	public void loadGene(GeneContainer geneContainer, Statement stmt, DatabaseType databaseType) throws SQLException {

		if(this.genes_id.containsKey(geneContainer.getEntryID())) {

			if(geneContainer.getModules()!=null)
				this.genes_modules_id.put(geneContainer.getEntryID(),geneContainer.getModules());
		}
		else {

			int geneID;

			String chromosome = "";

			if(geneContainer.getChromosome_name()!=null)
				chromosome = DatabaseUtilities.databaseStrConverter(geneContainer.getChromosome_name(),databaseType);

			String aux1 = "", aux2 = "";

			if(!chromosome.isEmpty()) {

				if(!this.chromosome_id.containsKey(chromosome)) {

					String query = "INSERT INTO chromosome(name) VALUES('"+chromosome+"')";

					int res = ProjectAPI.executeAndGetLastInsertID(query, stmt);
					this.chromosome_id.put(chromosome, res);
				}
				aux1 = ", chromosome_idchromosome";
				aux2 = ","+ this.chromosome_id.get(chromosome);
			}

			if(!this.genes_id.containsKey(geneContainer.getEntryID())) {

				String query;

				if(geneContainer.getName()!=null){
					query = "INSERT INTO gene(locusTag, sequence_id, name "+aux1+", origin) VALUES('"+geneContainer.getEntryID()
					+"','"+geneContainer.getEntryID()+"','"+DatabaseUtilities.databaseStrConverter(geneContainer.getName(),databaseType)
					+"'"+aux2+",'KEGG')";
				}
				else{
					query = "INSERT INTO gene(locusTag "+aux1+", sequence_id, origin) VALUES('"+geneContainer.getEntryID()+"'"+aux2+",'"+geneContainer.getEntryID()+"','KEGG')";
				}

				int res = ProjectAPI.executeAndGetLastInsertID(query, stmt);
				this.genes_id.put(geneContainer.getEntryID(), res);

				if(geneContainer.getModules()!=null)
					this.genes_modules_id.put(geneContainer.getEntryID(),geneContainer.getModules());
			}

			geneID= this.genes_id.get(geneContainer.getEntryID());

			String query;
			if(geneContainer.getLeft_end_position()!=null && geneContainer.getRight_end_position()!=null){
				query = "UPDATE gene SET left_end_position='"+DatabaseUtilities.databaseStrConverter(geneContainer.getLeft_end_position(),databaseType)
				+"', right_end_position='"+DatabaseUtilities.databaseStrConverter(geneContainer.getRight_end_position(),databaseType)
				+"' WHERE idgene="+geneID;
				ProjectAPI.executeQuery(query, stmt);
			}

			if(geneContainer.getDblinks()!=null)
			{
				for(String dbLink:geneContainer.getDblinks())
				{
					String database = dbLink.split(":")[0], link = dbLink.split(":")[1];

					boolean exists = ProjectAPI.checkInternalIdFromDblinks("g", geneID, database, stmt);

					if(!exists)
					{
						query = "INSERT INTO dblinks(class,internal_id,external_database,external_id) VALUES('g',"+geneID+",'"+database+"','"+link+"')";
						ProjectAPI.executeQuery(query, stmt);
					}
				}
			}

			if(geneContainer.getNames()!=null)
			{	
				for(String synonym:geneContainer.getNames())
				{
					String aux = DatabaseUtilities.databaseStrConverter(synonym,databaseType);

					boolean exists = ProjectAPI.checkEntityFromAliases("g", geneID, aux, stmt);
					if(!exists)
					{
						query = "INSERT INTO aliases(class,entity,alias) "
								+ "VALUES('g',"+geneID+",'"+DatabaseUtilities.databaseStrConverter(synonym,databaseType)+"')";
						ProjectAPI.executeQuery(query, stmt);
					}
				}
			}

			if(geneContainer.getAasequence()!=null)
			{
				boolean exists = ProjectAPI.checkGeneIDFromSequence(geneID, "aa", stmt);
				if(!exists)
				{
					query = "INSERT INTO sequence(gene_idgene,sequence_type,sequence,sequence_length) "
							+ "VALUES("+geneID+",'aa','"+geneContainer.getAasequence()+"',"+geneContainer.getAalength()+")";
					ProjectAPI.executeQuery(query, stmt);
				}
			}
			if(geneContainer.getNtsequence()!=null)
			{
				boolean exists = ProjectAPI.checkGeneIDFromSequence(geneID, "nt", stmt);
				if(!exists)
				{
					query = "INSERT INTO sequence(gene_idgene,sequence_type,sequence,sequence_length) "
							+ "VALUES("+geneID+", 'nt','"+geneContainer.getNtsequence()+"',"+geneContainer.getNtlength()+")";
					ProjectAPI.executeQuery(query, stmt);
				}
			}

			if(geneContainer.getOrthologues()!=null) {

				for(String orthologue:geneContainer.getOrthologues()) {

					if(!this.orthologues_id.containsKey(orthologue)) {

						query = "INSERT INTO orthology (entry_id) VALUES('"+orthologue+"')";

						int res = ProjectAPI.executeAndGetLastInsertID(query, stmt);
						this.orthologues_id.put(orthologue, res);
					}

					boolean exists = ProjectAPI.checkGeneHasOrthologyEntries(geneID, this.orthologues_id.get(orthologue), stmt);

					if(!exists) {

						query = "INSERT INTO gene_has_orthology (gene_idgene, orthology_id) VALUES("+geneID+","+this.orthologues_id.get(orthologue)+")";
						ProjectAPI.executeQuery(query, stmt);
					}
				}
			}
		}
	}


	/**
	 * Metabolites container
	 * 
	 * @param metabolites
	 * @throws SQLException
	 */
	public void loadMetabolites(ConcurrentLinkedQueue<MetaboliteContainer> metabolites) throws SQLException {

		PreparedStatement pStatement = this.connection.prepareStatement("INSERT INTO compound(name, formula, molecular_weight, hasBiologicalRoles, entry_type, kegg_id) VALUES(?,?,?,?,?,?);");

		ModelAPI.loadMetabolites(metabolites, this.metabolites_id, this.getCompoundsWithBiologicalRoles(), pStatement, this.connection.getDatabaseType());

		pStatement = this.connection.prepareStatement("SELECT idcompound FROM compound WHERE kegg_id=?;");

		ModelAPI.getCompoundID(metabolites, pStatement);

		for(MetaboliteContainer m : metabolites)
			this.metabolites_id.put(m.getEntryID(), m.getMetaboliteID());

		pStatement = this.connection.prepareStatement("INSERT INTO dblinks(class,internal_id,external_database,external_id) VALUES('c',?,?,?)");

		ModelAPI.load_dbLinks(metabolites, pStatement);

		pStatement = this.connection.prepareStatement("INSERT INTO aliases(class,entity,alias) "+ "VALUES('c', ?, ?)");

		ModelAPI.loadALiases(metabolites, pStatement, this.connection.getDatabaseType());

		//		pStatement = this.connection.prepareStatement("INSERT INTO same_as (metabolite_id, similar_metabolite_id) VALUES(?,?);");
		//
		//		ModelAPI.loadSameAs(metabolites, pStatement);

		for(MetaboliteContainer metaboliteContainer : metabolites) {

			if(metaboliteContainer.getPathways()!=null) {

				this.metabolitesPathway.put(metaboliteContainer.getMetaboliteID(), metaboliteContainer.getPathways().keySet());
				this.metabolitesPathwayList.add(metaboliteContainer.getMetaboliteID());
			}
		}
	}

	/**
	 * @param metaboliteContainer
	 * @throws SQLException
	 */
	//	public void loadMetabolite(MetaboliteContainer metaboliteContainer, Statement stmt, DatabaseType databaseType) throws SQLException {
	//
	//		String query;
	//
	//		String entry_type = null;
	//		if(metaboliteContainer.getEntryID().startsWith("C"))
	//		{entry_type="COMPOUND";}
	//		if(metaboliteContainer.getEntryID().startsWith("G"))
	//		{entry_type="GLYCAN";}
	//		if(metaboliteContainer.getEntryID().startsWith("D"))
	//		{entry_type="DRUGS";}
	//		if(metaboliteContainer.getEntryID().startsWith("B"))
	//		{entry_type="BIOMASS";}
	//
	//		if(!this.metabolites_id.containsKey(metaboliteContainer.getEntryID())) {
	//			
	//			String entries = "";
	//			String values = "";
	//
	//			if(metaboliteContainer.getName()!=null) {
	//
	//				entries = entries.concat("name, ");
	//				values = values.concat("'").concat(DatabaseUtilities.databaseStrConverter(metaboliteContainer.getName(),databaseType)).concat("',");
	//				
	//			}
	//
	//			if(metaboliteContainer.getFormula()!=null) {
	//
	//				entries = entries.concat("formula, ");
	//				values = values.concat("'").concat(DatabaseUtilities.databaseStrConverter(metaboliteContainer.getFormula(),databaseType)).concat("',");
	//			}
	//
	//			if(metaboliteContainer.getMolecular_weight()!=null) {
	//
	//				entries = entries.concat("molecular_weight, ");
	//				values = values.concat("'").concat(DatabaseUtilities.databaseStrConverter(metaboliteContainer.getMolecular_weight(),databaseType)).concat("',");
	//			}
	//
	//			if(this.getCompoundsWithBiologicalRoles().contains(metaboliteContainer.getEntryID())) {
	//
	//				entries = entries.concat("hasBiologicalRoles, ");
	//				values = values.concat("true, ");
	//				
	//			}
	//			
	//			query = "INSERT INTO compound(" + entries + " entry_type, kegg_id) VALUES( "+ values + " '"+entry_type+"','"+metaboliteContainer.getEntryID()+"');";
	//			
	//			ProjectAPI.executeQuery(query, stmt);
	//			int res = ProjectAPI.getCompoundIDbyKeggID(metaboliteContainer.getEntryID(), stmt);
	//			this.metabolites_id.put(metaboliteContainer.getEntryID(), res);
	//		}
	//		
	//		int metaboliteID = this.metabolites_id.get(metaboliteContainer.getEntryID());
	//
	//		//TODO add when using prepared statements
	//		
	//		if(metaboliteContainer.getDblinks()!=null) {
	//
	//			for(String dbLink:metaboliteContainer.getDblinks()) {
	//
	//				String database = dbLink.split(":")[0], link = dbLink.split(":")[1];
	//				
	//				boolean exists = ProjectAPI.checkInternalIdFromDblinks("c", metaboliteID, database, stmt);
	//
	//				if(!exists) {
	//
	//					query = "INSERT INTO dblinks(class,internal_id,external_database,external_id) VALUES('c',"+metaboliteID+",'"+database+"','"+link+"')";
	//					
	//					
	//					try {
	//						
	//						ProjectAPI.executeQuery(query, stmt);
	//					} 
	//					catch (Exception e) {
	//
	//						System.out.println(metaboliteContainer.toString());
	//						e.printStackTrace();
	//					}
	//					
	//					
	//				}
	//			}
	//		}
	//
	//		for(String same_as:metaboliteContainer.getSame_as()) {
	//
	//			if(metabolites_id.contains(same_as)) {
	//
	//				boolean firstCase = !ProjectAPI.checkDataFromSameAs(metaboliteID, metabolites_id.get(same_as), stmt);
	//
	//				boolean secondCase = !ProjectAPI.checkDataFromSameAs(metabolites_id.get(same_as), metaboliteID, stmt);
	//
	//				if(firstCase && secondCase) {
	//
	//					query = "INSERT INTO same_as (metabolite_id, similar_metabolite_id) VALUES("+metaboliteID+","+metabolites_id.get(same_as)+")";
	//					ProjectAPI.executeQuery(query, stmt);
	//				}
	//			}
	//			else {
	//
	//				similar_metabolites_to_load.put(same_as,metaboliteID);
	//			}
	//		}
	//
	//		if(similar_metabolites_to_load.keySet().contains(metaboliteContainer.getEntryID())) {
	//
	//			int original_metabolite_id=similar_metabolites_to_load.get(metaboliteContainer.getEntryID());
	//
	//			boolean firstCase = !ProjectAPI.checkDataFromSameAs(original_metabolite_id, metaboliteID, stmt);
	//
	//			boolean secondCase = !ProjectAPI.checkDataFromSameAs(metaboliteID, original_metabolite_id, stmt);
	//
	//			if(firstCase && secondCase) {
	//
	//				query = "INSERT INTO same_as (metabolite_id, similar_metabolite_id) VALUES("+original_metabolite_id+","+metaboliteID+")";
	//				ProjectAPI.executeQuery(query, stmt);
	//			}
	//			similar_metabolites_to_load.remove(metaboliteContainer.getEntryID());	
	//		}
	//
	//		if(metaboliteContainer.getNames() != null) {
	//
	//			for(String synonym:metaboliteContainer.getNames()) {
	//
	//				String aux = DatabaseUtilities.databaseStrConverter(synonym,databaseType);
	//				boolean exists = ProjectAPI.checkEntityFromAliases("c", metaboliteID, aux, stmt);
	//				
	//				if(!exists)
	//				{
	//					query = "INSERT INTO aliases(class,entity,alias) "
	//							+ "VALUES('c',"+metaboliteID+",'"+DatabaseUtilities.databaseStrConverter(synonym,databaseType)+"')";
	//					ProjectAPI.executeQuery(query, stmt);
	//				}
	//			}
	//		}
	//
	//		if(metaboliteContainer.getPathways()!=null) {
	//
	//			this.metabolitesPathway.put(metaboliteID, metaboliteContainer.getPathways().keySet());
	//			this.metabolitesPathwayList.add(metaboliteID);
	//		}
	//
	//		;
	//	}

	/**
	 * @param enzymeContainer
	 * @throws SQLException
	 */
	public void loadProtein(EnzymeContainer enzymeContainer, Statement stmt, DatabaseType databaseType) throws SQLException {

		int enzymeClass = Integer.valueOf(enzymeContainer.getEntryID().substring(0,1));

		String source = "KEGG";
		boolean inModel = false;
		
		if(enzymeContainer.getName()!=null) {

			String protein_name = enzymeContainer.getName();
			//			if(enzyme_entry.getName().startsWith("Transferred to"))
			//			{
			//				System.out.println("TRANSFERRED "+enzyme_entry.getEntry());
			//			}
			//			else if(enzyme_entry.getName().startsWith("Deleted entry"))
			//			{
			//				System.out.println("Deleted "+enzyme_entry.getEntry());
			//			}
			//			else
			{

				if(!this.proteins_id.containsKey(enzymeContainer.getEntryID())) {

					String query = "INSERT INTO protein(name,class) "
							+ " VALUES('"+DatabaseUtilities.databaseStrConverter(protein_name,databaseType)+"','"+getEnzymeClass(enzymeClass)+"');";	

					ProjectAPI.executeQuery(query, stmt);
					
					int protein_id = ProjectAPI.getProteinIDFromName(DatabaseUtilities.databaseStrConverter(protein_name,databaseType), stmt);

					this.proteins_id.put(enzymeContainer.getEntryID(), protein_id);
					
					if(importSBML){
						source = "SBML model";
						inModel = true;
					}
						
					query = "INSERT INTO enzyme(protein_idprotein,ecnumber,inModel, source) "
							+ "VALUES ("+protein_id+",'"+enzymeContainer.getEntryID()+"'," + inModel + ", '"+ source +"')";
					ProjectAPI.executeQuery(query, stmt);

					if(enzymeContainer.getCofactors()!=null) {

						for(String cofactor_string:enzymeContainer.getCofactors()) {

							if(!this.metabolites_id.containsKey(cofactor_string)) {

								int res = ProjectAPI.getCompoundIDbyKeggID(cofactor_string, stmt);
								this.metabolites_id.put(cofactor_string,res);
							}

							boolean exists = ProjectAPI.checkProteinIdFromEnzymaticCofactor(this.metabolites_id.get(cofactor_string), protein_id, stmt);
							if(!exists) {

								query = "INSERT INTO enzymatic_cofactor (protein_idprotein,compound_idcompound) VALUES ("+protein_id+","+this.metabolites_id.get(cofactor_string)+")";
								ProjectAPI.executeQuery(query, stmt);
							}
						}
					}

					//TODO add when using prepared statements  
					if(enzymeContainer.getDblinks()!=null) {

						for(String dbLink:enzymeContainer.getDblinks()) {

							String database = dbLink.split(":")[0], link = dbLink.split(":")[1];
							boolean exists = ProjectAPI.checkInternalIdFromDblinks("p", protein_id, database, stmt);
							if(!exists) {

								query = "INSERT INTO dblinks(class,internal_id,external_database,external_id) VALUES('p',"+protein_id+",'"+database+"','"+link+"')";
								try {
									ProjectAPI.executeQuery(query, stmt);
								} catch (Exception e) {
									System.out.println(protein_id+" "+database+" "+link);
									e.printStackTrace();
								}
							}
						}
					}

					if(enzymeContainer.getNames()!=null) {

						for(String synonym:enzymeContainer.getNames()) {

							String aux = DatabaseUtilities.databaseStrConverter(synonym,databaseType);
							boolean exists = ProjectAPI.checkEntityFromAliases("p", protein_id, aux, stmt);

							if(!exists) {
								query = "INSERT INTO aliases(class,entity,alias) VALUES('p',"+protein_id+",'"+ aux.replace(";", "")+"')";
								ProjectAPI.executeQuery(query, stmt);
							}
						}
					}
				}

				if(enzymeContainer.getPathways()!=null) {

					this.enzymesPathway.put(enzymeContainer.getEntryID(), enzymeContainer.getPathways().keySet());
					this.enzymesPathwayList.add(enzymeContainer.getEntryID());
				}

				int protein_id = this.proteins_id.get(enzymeContainer.getEntryID());

				if(enzymeContainer.getGenes()!=null) {

					this.enzymesInModel.add(enzymeContainer.getEntryID());

					String query = "UPDATE enzyme SET inModel=true WHERE protein_idprotein =" + protein_id + " AND ecnumber = '" + enzymeContainer.getEntryID() + "'";// AND source='KEGG'";

					ProjectAPI.executeQuery(query, stmt);

					for(String gene:enzymeContainer.getGenes()) {

						if(!this.genes_id.containsKey(gene)) {

							int res = ProjectAPI.getGeneID(gene, stmt);
							if(res > -1)
								this.genes_id.put(gene,res);

						}

						if(this.genes_modules_id.contains(gene)) {

							for (String module : this.genes_modules_id.get(gene)) {

								String aux = enzymeContainer.getEntryID()+"'"+" AND module_id='"+this.modules_id.get(module);

								boolean exists = ProjectAPI.checkModules(this.genes_id.get(gene), protein_id, aux, stmt);

								if(!exists) {

									query = "INSERT INTO subunit (enzyme_protein_idprotein,gene_idgene,enzyme_ecnumber,module_id) " +
											"VALUES ("+protein_id+","+this.genes_id.get(gene)+",'"+enzymeContainer.getEntryID()+"','"+this.modules_id.get(module)+"')";
									ProjectAPI.executeQuery(query, stmt);
								}
							}
						}
						else {

							boolean exists = ProjectAPI.checkModules(this.genes_id.get(gene), protein_id, enzymeContainer.getEntryID(), stmt);
							if(!exists){
								query = "INSERT INTO subunit (enzyme_protein_idprotein,gene_idgene,enzyme_ecnumber) " +
										"VALUES ("+protein_id+","+this.genes_id.get(gene)+",'"+enzymeContainer.getEntryID()+"')";
								ProjectAPI.executeQuery(query, stmt);
							}
						}

						if(this.enzymesReactions!=null && this.enzymesReactions.containsKey(enzymeContainer.getEntryID())) {

							for(int idReaction : this.enzymesReactions.get(enzymeContainer.getEntryID())) {

								query = "UPDATE reaction SET inModel=true WHERE idreaction = "+idReaction;
								ProjectAPI.executeQuery(query, stmt);
							}
						}
					}
				}
			}
		}
		else {

			//System.out.println("NULL NAME "+enzyme_entry.getEntry());

			int res = ProjectAPI.getProteinIDFromProtein(getEnzymeClass(enzymeClass), stmt);
			if(res<0) {

				String query ="INSERT INTO protein(name,class) VALUES('-','"+getEnzymeClass(enzymeClass)+"')";	
				res = ProjectAPI.executeAndGetLastInsertID(query, stmt);
			}

			this.proteins_id.put(enzymeContainer.getEntryID(), res);

			boolean exists = ProjectAPI.checkEnzymeData(this.proteins_id.get(enzymeContainer.getEntryID()), enzymeContainer.getEntryID(), stmt);
			if(!exists) {
				
				if(importSBML){
					source = "SBML model";
					inModel = true;
				}

				String query = "INSERT INTO enzyme(protein_idprotein,ecnumber,inModel, source) "
						+ "VALUES ("+this.proteins_id.get(enzymeContainer.getEntryID())+",'"+enzymeContainer.getEntryID()+"'," + inModel + ", '"+ source +"')";;
				ProjectAPI.executeQuery(query, stmt);
			}
		}
	}

	/**
	 * Load reaction data from KEGG
	 * 
	 * @param reactionContainer
	 * @param stmt
	 * @param databaseType
	 * @throws SQLException
	 */
	public void loadReaction(ReactionContainer reactionContainer, Statement stmt, DatabaseType databaseType) throws SQLException {

		if(this.reactions_id.containsKey(reactionContainer.getEntryID())) {

			boolean inModel = false;
			
			if(reactionContainer.getEnzymes() != null){
				
				for(String enzyme : reactionContainer.getEnzymes()) {

					if(this.enzymesInModel.contains(enzyme)) {

						if(reactionContainer.getPathwaysMap()== null) {

							inModel=true;
						}
						else {

							if(this.enzymesPathway.containsKey(enzyme)) {

								for(String path : reactionContainer.getPathwaysMap().keySet()) {

									if(this.enzymesPathway.get(enzyme).contains(path))
										inModel=true;
								}
							}
						}
					}
				}
			}
			
			if(inModel) {

				String query = "UPDATE reaction SET inModel = "+inModel+" WHERE idreaction = "+this.reactions_id.get(reactionContainer.getEntryID()); 
				ProjectAPI.executeQuery(query, stmt);
			}
		}

		else {
			
			if (this.compartmentID<0) {

				int res = CompartmentsAPI.getCompartmentID("inside", stmt);

				if(res<0) {

					String query = "INSERT INTO compartment (name, abbreviation) VALUES('inside','in')";
					res = ProjectAPI.executeAndGetLastInsertID(query, stmt);
				}
				this.compartmentID = res;
			}
			
			Integer compartment_id = this.compartmentID;
			
			if(importSBML && reactionContainer.getLocalisation() != null)
				compartment_id = CompartmentsAPI.getCompartmentID(reactionContainer.getLocalisation(), stmt);
			
			
			Map<String,String[]> results = new HashMap<String,String[]>();

			if(reactionContainer.getReactantsStoichiometry()!=null) {


				for(String reactant : reactionContainer.getReactantsStoichiometry().keySet()) {

					results.put("-"+reactant,reactionContainer.getReactantsStoichiometry().get(reactant));		
				}
			}

			if(reactionContainer.getProductsStoichiometry()!=null) {

				results.putAll(reactionContainer.getProductsStoichiometry());
			}

			boolean go = true;
			for(String metabolite: results.keySet()) {
				
				if(!this.metabolites_id.containsKey(metabolite.replace("-", "")))
					go = false;
			}

			if(go) {

				boolean inModel = false;

				if(reactionContainer.getEnzymes() != null){

					for(String enzyme : reactionContainer.getEnzymes()) {

						if(this.enzymesInModel.contains(enzyme)) {

							if(reactionContainer.getPathwaysMap()== null) {

								inModel=true;
							}
							else {

								if(this.enzymesPathway.containsKey(enzyme)) {

									for(String path : reactionContainer.getPathwaysMap().keySet()) {

										if(this.enzymesPathway.get(enzyme).contains(path))
											inModel=true;
									}
								}
							}
						}
					}
				}
				if (reactionContainer.isSpontaneous()) {

					if(!this.pathways_id.contains("SPONT")) {

						int res = ProjectAPI.getPathwayData("Spontaneous", "SPONT", stmt);
						if(res<0) {

							String query = "INSERT INTO pathway (name,code) VALUES('Spontaneous','SPONT')";
							res = ProjectAPI.executeAndGetLastInsertID(query, stmt);
						}
						this.pathways_id.put("SPONT", res);
					}

					Map<String,String> reaction_pathways;

					if(reactionContainer.getPathwaysMap()==null) { 

						reaction_pathways = new HashMap<String, String>();
					}
					else {

						reaction_pathways = reactionContainer.getPathwaysMap();
					}
					reaction_pathways.put("SPONT","Spontaneous");
					reactionContainer.setPathwaysMap(reaction_pathways);

					inModel = true;
				} 

				if(reactionContainer.isNon_enzymatic()) {

					if(!this.pathways_id.contains("NOENZ")) {

						int res = ProjectAPI.getPathwayData("Non enzymatic", "NOENZ", stmt);

						if(res<0) {

							String query = "INSERT INTO pathway (name,code) VALUES('Non enzymatic','NOENZ')";
							res = ProjectAPI.executeAndGetLastInsertID(query, stmt);
						}
						this.pathways_id.put("NOENZ", res);
					}

					Map<String,String> reaction_pathways;

					if(reactionContainer.getPathwaysMap()==null) { 

						reaction_pathways = new HashMap<String, String>();
					}
					else {

						reaction_pathways = reactionContainer.getPathwaysMap();
					}
					reaction_pathways.put("NOENZ","Non enzymatic");
					reactionContainer.setPathwaysMap(reaction_pathways);
				}

				if(!this.reactions_id.containsKey(reactionContainer.getEntryID())) {
					
					String source;
					String isReversible;
					Integer lowerBound, upperBound;
					String boolean_rule = null;
					
					if(importSBML){
						source = "SBML model";
						isReversible = Boolean.toString(reactionContainer.isReversible());
						lowerBound = reactionContainer.getLowerBound().intValue();
						upperBound = reactionContainer.getUpperBound().intValue();
						inModel = true;
						boolean_rule = reactionContainer.getGeneRule();
						if(boolean_rule!=null)
							boolean_rule = "'".concat(DatabaseUtilities.databaseStrConverter(boolean_rule,databaseType)).concat("'");
					}
					else{
						source = "KEGG";
						isReversible = "true";
						lowerBound = _LOWERBOUND;
						upperBound = _UPPERBOUND;
					}

					String query = "INSERT INTO reaction (name,equation,boolean_rule,reversible,inModel,isSpontaneous,isNonEnzymatic,isGeneric,source, originalReaction, compartment_idcompartment, lowerBound, upperBound) " +
							"VALUES('"+DatabaseUtilities.databaseStrConverter(reactionContainer.getEntryID(),databaseType)+
							"','"+DatabaseUtilities.databaseStrConverter(reactionContainer.getEquation(),databaseType)+
							"'," + boolean_rule + "," + isReversible + ","+inModel+","+reactionContainer.isSpontaneous()+","+reactionContainer.isNon_enzymatic()+","+
							reactionContainer.isGeneric()+",'" + source + "',true,"+ compartment_id +"," + lowerBound + "," + upperBound +");";

					int res = ProjectAPI.executeAndGetLastInsertID(query, stmt);
					this.reactions_id.put(reactionContainer.getEntryID(), res);
				}

				int reaction_id = this.reactions_id.get(reactionContainer.getEntryID());

				for(String metabolite: results.keySet()) {

					if(this.metabolites_id.containsKey(metabolite.replace("-", ""))) {

						String stoichiometry = results.get(metabolite)[0];

						if(stoichiometry.contains("n")) {

							String pattern = ".*\\d+n";
							Pattern r = Pattern.compile(pattern);

							Matcher matcher = r.matcher(stoichiometry);
							if (matcher.find( ))
								stoichiometry = stoichiometry.replaceAll("n", "*1");
							else
								stoichiometry = stoichiometry.replaceAll("n", "1");

							ScriptEngineManager factory = new ScriptEngineManager();
							ScriptEngine engine = factory.getEngineByName("JavaScript");

							try {

								stoichiometry = ""+engine.eval(stoichiometry);
							} 
							catch (ScriptException e) {
								e.printStackTrace();
							}
						}

						int metabolite_id = this.metabolites_id.get(metabolite.replace("-", ""));
						String query = "SELECT * FROM stoichiometry" +
								" WHERE compound_idcompound="+ metabolite_id +
								" AND reaction_idreaction="+reaction_id+"" +
								" AND compartment_idcompartment = "+this.compartmentID +
								" AND stoichiometric_coefficient = '"+stoichiometry+"'" +
								" AND numberofchains = '"+results.get(metabolite)[1]+"'";
						int res = ProjectAPI.getData(query, stmt);
						
						
//						if(importSBML && !results.get(metabolite)[2].isEmpty())
//							compartment_id = CompartmentsAPI.getCompartmentID(results.get(metabolite)[2], stmt);
//						else
//							compartment_id = this.compartmentID;
						
						
						if(res<0){
							query = "INSERT INTO stoichiometry(reaction_idreaction,compound_idcompound,compartment_idcompartment,stoichiometric_coefficient,numberofchains) " +
									"VALUES("+reaction_id+","+metabolite_id+","+compartment_id+",'"+stoichiometry+"','"+results.get(metabolite)[1]+"')";
							ProjectAPI.executeQuery(query, stmt);
						}
					}
				}


				if(reactionContainer.getEnzymes()!=null) {

					for(String enzyme : reactionContainer.getEnzymes()) {

						if(!enzyme.contains("-")) {

							int protein_id=this.proteins_id.get(enzyme);

							boolean exists = ProjectAPI.checkReactionHasEnzymeData(enzyme, protein_id, reaction_id, stmt);

							if(!exists){

								String query = "INSERT INTO reaction_has_enzyme(enzyme_protein_idprotein,enzyme_ecnumber,reaction_idreaction) " +
										"VALUES("+protein_id+",'"+enzyme+"',"+reaction_id+")";
								ProjectAPI.executeQuery(query, stmt);
							}
						}
					}
				}

				if(reactionContainer.getNames()!=null) {

					for(String synonym:reactionContainer.getNames()) {

						String aux = DatabaseUtilities.databaseStrConverter(synonym,databaseType);

						boolean exists = ProjectAPI.checkEntityFromAliases("r", reaction_id, aux, stmt);

						if(!exists) {

							String query = "INSERT INTO aliases(class,entity,alias) " +
									"VALUES('r',"+reaction_id+",'"+DatabaseUtilities.databaseStrConverter(synonym,databaseType)+"')";
							ProjectAPI.executeQuery(query, stmt);
						}
					}
				}
				
				if(reactionContainer.getPathwaysMap()!=null) {

					this.reactionsPathway.put(reaction_id, reactionContainer.getPathwaysMap().keySet());
					this.reactionsPathwayList.add(reaction_id);
				}
			}
			else {

				System.out.println("\t reaction "+reactionContainer.getEntryID()+" has unexisting metabolites.");
			}
		}
	}

	/**
	 * @param moduleContainer
	 * @throws SQLException
	 */
	public void loadModule(ModuleContainer moduleContainer, Statement stmt, DatabaseType databaseType) throws SQLException{

		String query;

		if(!this.modules_id.containsKey(moduleContainer.getEntryID())) {

			query = "INSERT INTO module (id,type) VALUES('"+moduleContainer.getEntryID()+"','"+moduleContainer.getModuleType()+"')";
			int res = ProjectAPI.executeAndGetLastInsertID(query, stmt);

			this.modules_id.put((moduleContainer.getEntryID()),res);

			if(moduleContainer.getName()!=null) {

				query = "UPDATE module SET name='"+DatabaseUtilities.databaseStrConverter(moduleContainer.getName(),databaseType)+"'" +
						" WHERE id="+this.modules_id.get(moduleContainer.getEntryID());
				ProjectAPI.executeQuery(query, stmt);
			}

			if(moduleContainer.getStoichiometry()!=null) {

				query = "UPDATE module SET stoichiometry='"+DatabaseUtilities.databaseStrConverter(moduleContainer.getStoichiometry(),databaseType)+"'" +
						" WHERE id="+this.modules_id.get(moduleContainer.getEntryID());
				ProjectAPI.executeQuery(query, stmt);
			}

			if(moduleContainer.getDefinition()!=null) {

				query = "UPDATE module SET definition='"+DatabaseUtilities.databaseStrConverter(moduleContainer.getDefinition(),databaseType)+"'" +
						" WHERE id="+this.modules_id.get(moduleContainer.getEntryID());
				ProjectAPI.executeQuery(query, stmt);
			}

			if(moduleContainer.getModuleHieralchicalClass()!=null) {

				query = "UPDATE module SET hieralchical_class='"+DatabaseUtilities.databaseStrConverter(moduleContainer.getModuleHieralchicalClass(),databaseType)+"'" +
						" WHERE id="+this.modules_id.get(moduleContainer.getEntryID());
				ProjectAPI.executeQuery(query, stmt);
			}

		}
		int moduleID = this.modules_id.get((moduleContainer.getEntryID()));

		for(String orthologue:moduleContainer.getOrthologues()) {

			if(!this.orthologues_id.containsKey(orthologue)) {

				query = "INSERT INTO orthology (entry_id) VALUES('"+orthologue+"')";
				int res = ProjectAPI.executeAndGetLastInsertID(query, stmt);

				this.orthologues_id.put(orthologue,res);
			}

			boolean exists = ProjectAPI.checkModuleHasOrthology(moduleID, this.orthologues_id.get(orthologue), stmt);
			if(!exists) {

				query = "INSERT INTO module_has_orthology (module_id, orthology_id) VALUES("+moduleID+","+this.orthologues_id.get(orthologue)+")";
				ProjectAPI.executeQuery(query, stmt);
			}
		}

		if(moduleContainer.getPathways()!=null) {

			this.modulesPathway.put(moduleID, moduleContainer.getPathways().keySet());
			this.modulesPathwayList.add(moduleID);
		}
	}

	/**
	 * @param pathwaysHierarchyContainer
	 * @param stmt
	 * @param databaseType
	 * @throws SQLException
	 */
	public void loadPathways(PathwaysHierarchyContainer pathwaysHierarchyContainer, Statement stmt, DatabaseType databaseType) throws SQLException{

		String query;

		int res = ProjectAPI.getPathwayID(pathwaysHierarchyContainer.getSuper_pathway(), stmt);

		if(res<0) {

			query = "INSERT INTO pathway (code,name) VALUES('','"+pathwaysHierarchyContainer.getSuper_pathway()+"')";
			res = ProjectAPI.executeAndGetLastInsertID(query, stmt);
		}

		int super_pathway_id = res;

		for(String intermediary_pathway: pathwaysHierarchyContainer.getPathways_hierarchy().keySet()) {

			res = ProjectAPI.getPathwayID(intermediary_pathway, stmt);

			if(res<0) {

				query = "INSERT INTO pathway (code,name) VALUES('','"+intermediary_pathway+"')";
				res = ProjectAPI.executeAndGetLastInsertID(query, stmt);
			}

			int intermediary_pathway_id = res;

			res = ProjectAPI.getSuperPathwayData(intermediary_pathway_id, super_pathway_id, stmt);
			if(res<0) {

				query = "INSERT INTO superpathway VALUES("+intermediary_pathway_id+","+super_pathway_id+")";
				ProjectAPI.executeQuery(query, stmt);
			}

			for(String[] pathway: pathwaysHierarchyContainer.getPathways_hierarchy().get(intermediary_pathway)) {

				if(!this.pathways_id.contains(pathway[0])) {
					String aux = DatabaseUtilities.databaseStrConverter(pathway[1],databaseType)+"' AND code='"+pathway[0];

					res = ProjectAPI.getPathwayID(aux, stmt);

					if(res<0) {

						query = "INSERT INTO pathway (name,code) "
								+ "VALUES('"+DatabaseUtilities.databaseStrConverter(pathway[1],databaseType)+"','"+pathway[0]+"')";
						res = ProjectAPI.executeAndGetLastInsertID(query, stmt);
						
//						query = "SELECT idpathway FROM pathway WHERE code = '" + pathway[0] + "';";
						
						res = ProjectAPI.getPathwayID(DatabaseUtilities.databaseStrConverter(pathway[1],databaseType), stmt);
					}
					this.pathways_id.put(pathway[0], res);
				}
				int pathway_id = this.pathways_id.get(pathway[0]);

				res = ProjectAPI.getSuperPathwayData(pathway_id, intermediary_pathway_id, stmt);
				if(res<0) {

					query = "INSERT INTO superpathway VALUES("+pathway_id+","+intermediary_pathway_id+")";
					ProjectAPI.executeQuery(query, stmt);
				}
			}
		}
		//}
	}

	/**
	 * @param idreaction
	 * @param stmt
	 * @param databaseType
	 * @throws SQLException
	 */
	public void load_ReactionsPathway(int idreaction, Statement stmt) throws SQLException{

		for(String pathway:this.reactionsPathway.get(idreaction)) {

			if(this.pathways_id.containsKey(pathway)) {

				boolean exists = ProjectAPI.checkPathwayHasReactionData(idreaction, this.pathways_id.get(pathway), stmt);
				if(!exists) {

					String query = "INSERT INTO pathway_has_reaction (reaction_idreaction, pathway_idpathway) "
							+ "VALUES("+idreaction+","+this.pathways_id.get(pathway)+")";
					ProjectAPI.executeQuery(query, stmt);
				}
			}
		}
	}

	/**
	 * @param ecnumber
	 * @throws SQLException
	 */
	public void load_EnzymesPathway(String ecnumber, Statement stmt) throws SQLException{

		for(String pathway:this.enzymesPathway.get(ecnumber)) {

			boolean exists = ProjectAPI.checkPathwayHasEnzymeData(this.proteins_id.get(ecnumber), this.pathways_id.get(pathway), stmt);

			if(!exists) {

				String query = "INSERT INTO pathway_has_enzyme (enzyme_protein_idprotein, pathway_idpathway, enzyme_ecnumber) "
						+ "VALUES("+this.proteins_id.get(ecnumber)+","+this.pathways_id.get(pathway)+",'"+ecnumber+"')";
				ProjectAPI.executeQuery(query, stmt);
			}
		}
	}

	/**
	 * @param moduleID
	 * @throws SQLException
	 */
	public void load_ModulePathway(int moduleID, Statement stmt) throws SQLException{

		for(String pathway:this.modulesPathway.get(moduleID))
		{
			boolean exists = ProjectAPI.checkPathwayHasModuleData(moduleID, this.pathways_id.get(pathway), stmt);
			if(!exists)
			{
				String query = "INSERT INTO pathway_has_module (module_id, pathway_idpathway) VALUES("+moduleID+","+this.pathways_id.get(pathway)+")";
				ProjectAPI.executeQuery(query, stmt);
			}
		}
	}

	/**
	 * @param metaboliteID
	 * @throws SQLException
	 */
	public void load_MetabolitePathway(int metaboliteID, Statement stmt) throws SQLException{

		for(String pathway:this.metabolitesPathway.get(metaboliteID)) {

			if(this.pathways_id.contains(pathway)) {

				boolean exists = ProjectAPI.checkPathwayHasCompoundData(metaboliteID, this.pathways_id.get(pathway), stmt);
				if(!exists) {

					String query = "INSERT INTO pathway_has_compound (compound_idcompound, pathway_idpathway) VALUES("+metaboliteID+","+this.pathways_id.get(pathway)+")";
					ProjectAPI.executeQuery(query, stmt);
				}
			}

		}
		;
	}
	
	
	/////////
	/**
	 * @param compartments
	 * @param stmt
	 * @throws SQLException 
	 */
	public void loadCompartment(CompartmentContainer compartment, Statement stmt, DatabaseType databaseType) throws SQLException{
		
		String compartmentName = compartment.getName();
		String abbreviation = compartment.getAbbreviation();
		
		int compartmentID = CompartmentsAPI.getCompartmentID(compartmentName, stmt);
		
		if(compartmentID == -1) {
		
			String query = "INSERT INTO compartment (name,abbreviation) VALUES('" + compartmentName +"','"+ abbreviation +"');";
			ProjectAPI.executeQuery(query, stmt);
		}
	}
	///////////
	
	
	
	/**
	 * @param module_id
	 * @throws SQLException
	 */
	public static void buildViews(Connection connection, Statement stmt) throws SQLException {

		String query;
		if (connection.getDatabaseType().equals(DatabaseType.H2)){

			query = "SELECT * FROM \"reactions_view\";";
			ProjectAPI.executeQuery(query, stmt);

			query = "SELECT * FROM \"reactions_view_noPath_or_noEC\";";
			ProjectAPI.executeQuery(query, stmt);
		}

		else {

			query = "SELECT * FROM reactions_view;";
			ProjectAPI.executeQuery(query, stmt);

			query = "SELECT * FROM reactions_view_noPath_or_noEC;";
			ProjectAPI.executeQuery(query, stmt);
		}
		//stmt.execute("SELECT * FROM sbml_query;");
		;
	}

	/**
	 * @param enzyme
	 * @return
	 */
	private static String getEnzymeClass(int enzymeClass){
		String classes = null;

		switch (enzymeClass)  {

		case 1:  classes = "Oxidoreductases";break;
		case 2:  classes = "Transferases";break;
		case 3:  classes = "Hydrolases";break;
		case 4:  classes = "Lyases";break;
		case 5:  classes = "Isomerases";break;
		case 6:  classes = "Ligases";break;
		}
		return classes;
	}

	/**
	 * @param compoundsWithBiologicalRoles the compoundsWithBiologicalRoles to set
	 */
	public void setCompoundsWithBiologicalRoles(
			ConcurrentLinkedQueue<String> compoundsWithBiologicalRoles) {
		this.compoundsWithBiologicalRoles = compoundsWithBiologicalRoles;
	}

	/**
	 * @return the compoundsWithBiologicalRoles
	 */
	public ConcurrentLinkedQueue<String> getCompoundsWithBiologicalRoles() {
		return compoundsWithBiologicalRoles;
	}

	/**
	 * @return the pathways_id
	 */
	public ConcurrentHashMap<String, Integer> getPathways_id() {
		return pathways_id;
	}

	/**
	 * @param pathwaysId the pathways_id to set
	 */
	public void setPathways_id(ConcurrentHashMap<String, Integer> pathwaysId) {
		pathways_id = pathwaysId;
	}

}