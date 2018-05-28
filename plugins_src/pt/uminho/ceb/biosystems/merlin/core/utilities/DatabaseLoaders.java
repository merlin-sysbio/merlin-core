package pt.uminho.ceb.biosystems.merlin.core.utilities;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import pt.uminho.ceb.biosystems.merlin.BiomassMetabolite;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.InformationType;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.GeneCompartments;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.ProcessCompartments;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.capsules.DatabaseReactionContainer;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * @author Oscar Dias
 *
 */
public class DatabaseLoaders {


	/**
	 * Method for loading the genome annotation.
	 * 
	 * @param locusTag
	 * @param sequence_id
	 * @param geneName
	 * @param chromosome
	 * @param direction
	 * @param left_end
	 * @param right_end
	 * @param ecNumbers
	 * @param proteinName
	 * @param statement
	 * @param integratePartial
	 * @param integrateFull
	 * @param insertProductNames
	 * @param project
	 * @param informationType
	 * @param genesCompartments
	 * @return
	 * @throws Exception
	 */
	public static boolean loadGeneAnnotation(String locusTag, String  sequence_id, String geneName, String chromosome, String direction, String left_end, String right_end, Set<String> ecNumbers, String proteinName, Statement statement,
			boolean integratePartial, boolean integrateFull, boolean insertProductNames, Project project, InformationType informationType, Map<String, GeneCompartments> genesCompartments) throws Exception {

		String idGene = DatabaseLoaders.loadGene(locusTag, sequence_id, geneName, chromosome, direction, left_end, right_end, statement, project.getDatabaseType(), informationType);
		
		if (! ecNumbers.isEmpty()){
			Map<String, List<String>> enzymesReactions = ModelAPI.loadEnzymeGetReactions(idGene, ecNumbers, proteinName, statement, integratePartial, integrateFull, insertProductNames, project.getDatabaseType());
	
			if(ProjectAPI.isCompartmentalisedModel(statement) && !ModelAPI.isGeneCompartmentLoaded(idGene, statement)) {
	
				GeneCompartments geneCompartments = genesCompartments.get(locusTag);
	
				Map<String, String> compartmentsDatabaseIDs = new HashMap<>();
				String primaryCompartment = geneCompartments.getPrimary_location();
				String primaryCompartmentAbb = geneCompartments.getPrimary_location_abb();
				double scorePrimaryCompartment = geneCompartments.getPrimary_score();
				Map<String, Double> secondaryCompartments = geneCompartments.getSecondary_location();
				Map<String, String> secondaryCompartmentsAbb = geneCompartments.getSecondary_location_abb();
	
				compartmentsDatabaseIDs = ModelAPI.getCompartmentsDatabaseIDs(primaryCompartment, primaryCompartmentAbb, secondaryCompartments, secondaryCompartmentsAbb, compartmentsDatabaseIDs, statement);
				//associate gene to compartments
	
				ModelAPI.loadGenesCompartments(idGene, compartmentsDatabaseIDs, statement, primaryCompartment, scorePrimaryCompartment, secondaryCompartments);
	
				Map<Integer,String> compartmentsAbb_ids = ModelAPI.getIdCompartmentAbbMap(statement);
				Map<String,Integer> idCompartmentAbbIdMap = ModelAPI.getCompartmentAbbIdMap(statement);
	
				ProcessCompartments processCompartments = new ProcessCompartments();
				processCompartments.autoSetInteriorCompartment(statement);
	
				for(String ecNumber : enzymesReactions.keySet()) {
					//Compartmentalize reactions
					List<String> idReactions = enzymesReactions.get(ecNumber);
					for(String idReaction : idReactions) {
						
						Map<String, Object> subMap = ModelAPI.getDatabaseReactionContainer(idReaction, statement);
	
						DatabaseReactionContainer databaseReactionContainer = DatabaseLoaders.getDatabaseReactionContainer(idReaction, subMap, statement);
						List<Integer> enzymeCompartments = ModelAPI.getEnzymeCompartments(ecNumber, statement);
						Set<Integer> parsedCompartments = processCompartments.parseCompartments(enzymeCompartments, compartmentsAbb_ids,idCompartmentAbbIdMap, null);
	
						boolean inModelFromCompartment = databaseReactionContainer.isInModel();
						//all enzyme compartments are assigned to the reactions
						for(int idCompartment: parsedCompartments) {
	
							if(idCompartment>0) {
	
								if(processCompartments.getIgnoreCompartmentsID().contains(idCompartment))
									inModelFromCompartment = false;
	
								DatabaseLoaders.loadReaction(idCompartment, inModelFromCompartment, databaseReactionContainer, ecNumber, statement, false, project.getDatabaseType());
							}
						}
					}
				}
			}
		}

		return true;
	}

	/**
	 *  * Method for loading not original reactions into database.
	 * 
	 * @param idCompartment
	 * @param inModel
	 * @param databaseReactionContainer
	 * @param ecNumber
	 * @param statement
	 * @param isTransport
	 * @param databaseType
	 * @throws SQLException 
	 */
	public static void loadReaction(int idCompartment, boolean inModel, DatabaseReactionContainer databaseReactionContainer, String ecNumber, Statement statement, boolean isTransport, DatabaseType databaseType) throws SQLException {

		String name = databaseReactionContainer.getName(), equation = databaseReactionContainer.getEquation(), reactionSource = databaseReactionContainer.getSource(), notes = databaseReactionContainer.getNotes();
		boolean reversible = databaseReactionContainer.isReversible(), generic = databaseReactionContainer.isGeneric(), nonEnzymatic = databaseReactionContainer.isNonEnzymatic(), spontaneous = databaseReactionContainer.isSpontaneous();
		List<String> proteins = databaseReactionContainer.getProtein_id(), enzymes = databaseReactionContainer.getEcnumbers(), pathways = databaseReactionContainer.getPathways(),

				stoichiometry = databaseReactionContainer.getStoichiometric_coefficients() , chains = databaseReactionContainer.getNumbersofchains();
				
		List<Integer> compounds = databaseReactionContainer.getCompound_idcompounds(), compartments = databaseReactionContainer.getCompartment_idcompartment();
		
		Map<String, List<String>> ecNumbers = databaseReactionContainer.getEcnumbers_protein_id();

		ModelAPI.loadReaction(idCompartment, inModel, ecNumber, statement, isTransport, databaseType, name, equation, reversible, generic, spontaneous,
				nonEnzymatic, reactionSource, notes, proteins, enzymes, ecNumbers, pathways, compounds, compartments, stoichiometry, chains);

	}

	/**
	 * Method for loading the genome annotation.
	 * Integrates all ecnumbers and protein names
	 * 
	 * @param locusTag
	 * @param sequence_id
	 * @param geneName
	 * @param chromosome
	 * @param ecNumber
	 * @param proteinName
	 * @param statement
	 * @param project 
	 * @param informationType 
	 * @return
	 * @throws Exception 
	 */
	public static boolean loadGeneAnnotation(String locusTag, String sequence_id, String geneName, String chromosome, String direction, String left_end, String right_end, Set<String> ecNumber, String proteinName, Statement statement, Project project, InformationType informationType) throws Exception {
		
		return DatabaseLoaders.loadGeneAnnotation(locusTag, sequence_id, geneName, chromosome, direction, left_end, right_end, ecNumber, proteinName, statement, true, true, true, project, informationType, null);
	}

	/**
	 * Load Enzyme Information
	 * Returns reactions associated to the given enzymes in database.
	 * 
	 * Integrates all ecnumbers and protein names
	 * 
	 * @param idGene
	 * @param ecNumber
	 * @param proteinName
	 * @param statement
	 * @return
	 * @throws SQLException 
	 */
	public static  Map<String, List<String>> loadEnzymeGetReactions(String idGene, Set<String> ecNumber, String proteinName, Statement statement, DatabaseType databaseType) throws SQLException {

		return ModelAPI.loadEnzymeGetReactions(idGene, ecNumber, proteinName, statement, true, true, true, databaseType);
	}

	/**
	 * Method for loading gene information retrieved from homology data for a given sequence_id.
	 * 
	 * @param sequence_id
	 * @param statement
	 * @param informationType
	 * @return
	 * @throws SQLException
	 */
	public static String loadGeneLocusFromHomologyData (String sequence_id, Statement statement, DatabaseType databaseType, InformationType informationType) throws SQLException {

		return ModelAPI.loadGeneLocusFromHomologyData(sequence_id, statement, databaseType, informationType.toString());
	}



	/**
	 * Load Gene Information
	 * Returns gene id in database.
	 * 
	 * @param locusTag
	 * @param sequence_id
	 * @param geneName
	 * @param chromosome
	 * @param statement
	 * @param informationType
	 * @return
	 * @throws SQLException
	 */
	public static String loadGene(String locusTag, String sequence_id, String geneName, String chromosome, String direction, String left_end, String right_end, Statement statement, DatabaseType databaseType, InformationType informationType) throws SQLException {
		
		return ModelAPI.loadGene(locusTag, sequence_id, geneName, chromosome, direction, left_end, right_end, statement, databaseType, informationType.toString());
	}

	/**
	 * Method for loading the genome annotation.
	 * 
	 * Null chromosome, protein name and gene name
	 * 
	 * @param locusTag
	 * @param sequence_id
	 * @param ecNumber
	 * @param statement
	 * @param project 
	 * @param informationType 
	 * @return
	 * @throws Exception 
	 */
	public static boolean loadGeneAnnotation(String locusTag, String sequence_id, Set<String> ecNumber, Statement statement, Project project, InformationType informationType) throws Exception {

		String chromosome = null;
		String proteinName = null;
		String geneName = null;
		String direction = null;
		String left_end = null;
		String right_end = null;
		return DatabaseLoaders.loadGeneAnnotation(locusTag, sequence_id, geneName, chromosome, direction, left_end, right_end, ecNumber, proteinName, statement, project, informationType);
	}



	/**
	 * Get information for e-biomass.
	 * 
	 * @param data
	 * @param statment
	 * @return
	 */
	public static Map<String, BiomassMetabolite> getModelInformationForBiomass(Map<String, BiomassMetabolite> data, Statement statment) {

		List<String> keggs = new ArrayList<>();

		for(String name : data.keySet())
			keggs.add(data.get(name).getKeggId());

		Map<String, Pair<String, Double>> map= ModelAPI.getModelInformationForBiomass(keggs, statment);

		for(String name : data.keySet()) {

			String kegg = data.get(name).getKeggId();
			data.get(name).setModelId(map.get(kegg).getA());
			data.get(name).setMolecularWeight(map.get(kegg).getB());
		}
		return data;
	}


	/**
	 * Method for retrieving reaction containers associated to reactions.
	 * 
	 * @param statement
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, DatabaseReactionContainer> getEnzymesReactionsMap(Statement statement, boolean isTransporters ) throws SQLException {

		Map<String, DatabaseReactionContainer> reactionsMap = new HashMap<>();

		Map<String, Map<String, Object>> map = ModelAPI.getEnzymesReactionsMap(statement, isTransporters);

		for(String id : map.keySet()) {

			Map<String, Object> subMap = map.get(id);
//			String name = (String) subMap.get("name");
//			String equation = (String) subMap.get("equation");
//			String source = (String) subMap.get("source");
//			boolean reversible = (boolean) subMap.get("reversible");
//			boolean inModel = (boolean) subMap.get("inModel");
//			boolean isGeneric = (boolean) subMap.get("isGeneric");
//			boolean isSpontaneous = (boolean) subMap.get("isSpontaneous");
//			boolean isNonEnzymatic = (boolean) subMap.get("isNonEnzymatic");
//
//			String upperBound = (String) subMap.get("upperBound");
//			String lowerBound = (String) subMap.get("lowerBound");
//			String notes = (String) subMap.get("notes");
//
//			DatabaseReactionContainer drc = new DatabaseReactionContainer(id, name, equation, source, reversible, inModel, isGeneric, isSpontaneous, isNonEnzymatic);
//			drc.setUpperBound(upperBound);
//			drc.setLowerBound(lowerBound);
//			drc.setNotes(notes);
//			
//			if(subMap.containsKey("proteins")){
//				for(Pair<String, String> pair : (List<Pair<String, String>>) subMap.get("proteins")){
//					drc.addProteins(pair.getA(), pair.getB());
//				}
//			}
//
//			if(subMap.containsKey("pathways"))
//				drc.setPathways((List<String>) subMap.get("pathways"));
//			
//			if(subMap.containsKey("entry")){
//				for(String[] array : (List<String[]>) subMap.get("entry"))				
//					drc.addEntry(Integer.parseInt(array[0]), array[1], array[2], Integer.parseInt(array[3]));
//			}
			
			DatabaseReactionContainer drc = getDatabaseReactionContainer(id, subMap, statement);

			reactionsMap.put(id, drc);
		}
		return reactionsMap;

	}

	/**
	 * Method for retrieving the reactions container for a reaction.
	 * 
	 * @param idReaction
	 * @param subMap
	 * @param statement
	 * @return
	 * @throws SQLException
	 */
	public static DatabaseReactionContainer getDatabaseReactionContainer(String idReaction, Map<String, Object> subMap, Statement statement) throws SQLException {

		String name = (String) subMap.get("name");
		String equation = (String) subMap.get("equation");
		String source = (String) subMap.get("source");
		boolean reversible = (boolean) subMap.get("reversible");
		boolean inModel = (boolean) subMap.get("inModel");
		boolean isGeneric = (boolean) subMap.get("isGeneric");
		boolean isSpontaneous = (boolean) subMap.get("isSpontaneous");
		boolean isNonEnzymatic = (boolean) subMap.get("isNonEnzymatic");

		String upperBound = (String) subMap.get("upperBound");
		String lowerBound = (String) subMap.get("lowerBound");
		String notes = (String) subMap.get("notes");

		DatabaseReactionContainer drc = new DatabaseReactionContainer(idReaction, name, equation, source, reversible, inModel, isGeneric, isSpontaneous, isNonEnzymatic);
		drc.setUpperBound(upperBound);
		drc.setLowerBound(lowerBound);
		drc.setNotes(notes);

//		for(Pair<String, String> pair : (List<Pair<String, String>>) subMap.get("proteins")){
//			drc.addProteins(pair.getA(), pair.getB());
//		}
//
//		drc.setPathways((List<String>) subMap.get("pathways"));
//
//		for(String[] array : (List<String[]>) subMap.get("entry"))				
//			drc.addEntry(Integer.parseInt(array[0]), array[1], array[2], Integer.parseInt(array[3]));
		
		if(subMap.containsKey("proteins")){
			for(Pair<String, String> pair : (List<Pair<String, String>>) subMap.get("proteins")){
				drc.addProteins(pair.getA(), pair.getB());
			}
		}

		if(subMap.containsKey("pathways"))
			drc.setPathways((List<String>) subMap.get("pathways"));
		
		if(subMap.containsKey("entry")){
			for(String[] array : (List<String[]>) subMap.get("entry"))				
				drc.addEntry(Integer.parseInt(array[0]), array[1], array[2], Integer.parseInt(array[3]));
		}

		return drc;
	}
}
