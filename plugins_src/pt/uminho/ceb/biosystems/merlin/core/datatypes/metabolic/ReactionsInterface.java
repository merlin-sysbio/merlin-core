package pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic;

import java.awt.Color;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.auxiliary.ReactionGapsAux;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.auxiliary.ReactionsInterfaceAux;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.CompartmentsAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.HomologyAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseUtilities;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.ceb.biosystems.merlin.utilities.RulesParser;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.capsules.ReactionsCapsule;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.MetaboliteContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.ReactionContainer;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.BalanceValidator;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * @author ODias
 *
 */
@Datatype(structure= Structure.SIMPLE, namingMethod="getName",removable=true,removeMethod ="remove")
public class ReactionsInterface extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//	private static final int REACTION_NAME_COLUMN = 2;
	private static final int IN_MODEL_COLUMN = 7;
	//	private static final int IS_GENERIC_COLUMN = 7;
	private static final int IS_REVERSIBLE_COLUMN = 6;
	private static final int NOTES_COLUMN = 5;
	private HashMap<String,String> namesIndex;
	private HashMap<String,String> formulasIndex;
	private Map<Integer, String> ids;
	private Map<Integer, Integer> selectedPathIndexID;
	private Map<Integer,Color> pathwayColors;
	private String[] paths;
	private Connection connection;
	private Integer[] tableColumnsSize;
	private Set<String> activeReactions;
	private ReactionGapsAux gapReactions;
	private BalanceValidator balanceValidator;
	private Map<String, String> externalModelIds;
	//used in compartments integration
	private boolean newGaps;


	/**
	 * @param dbt
	 * @param name
	 */
	public ReactionsInterface(Table dbt, String name) {

		super(dbt, name);
		this.selectedPathIndexID = new TreeMap<Integer, Integer>();
		this.externalModelIds = new HashMap<>();
		this.connection=dbt.getConnection();
		this.colorPaths();
		this.setNewGaps(false);
	}


	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		int num=0;
		int noname=0;
		int noequation=0;
		int reversible=0;
		int irreversible=0;

		String[][] res = new String[16][];
		Statement statement;

		try {

			String aux = "";
			if(this.getProject().isCompartmentalisedModel())
				aux = aux.concat(" WHERE NOT originalReaction");
			else
				aux = aux.concat(" WHERE originalReaction");

			statement = this.connection.createStatement();
			ArrayList<String[]> data = ProjectAPI.getReactionsData(aux, statement);

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);

				num++;
				if(list[0]==null || list[0].trim().equals("")) {

					noname++;
				}

				if(list[1]==null || list[1].trim().equals("")) {

					noequation++;
				}

				if(list[2]!= null) {

					if(list[2].equals("0")){irreversible++;}
					else{reversible++;}
				}
			}

			int i=0;
			int value = 0;

			value = ModelAPI.countReactionsInModel(aux, statement);
			res[i] = new String[] {"Total number of reactions in the model", ""+value};
			i++;

			value = ModelAPI.countReactionsInModelKEGG(aux, statement);
			res[i] = new String[] {"Number of KEGG reactions in the model", ""+value};
			i++;

			value = ModelAPI.countReactionsInModelHomology(aux, statement);
			res[i] = new String[] {"Number of reactions inserted by HOMOLOGY in the model", ""+value};
			i++;

			value = ModelAPI.countReactionsInModelTransporters(aux, statement);
			res[i] = new String[] {"Number of reactions from the TRANSPORTERS annotation tool in the model", ""+value};
			i++;

			value = ModelAPI.countReactionsInModelManual(aux, statement);
			res[i] = new String[] {"Number of reactions inserted MANUALLY in the model", ""+value};
			i++;

			res[i] = new String[] {"", ""};
			i++;

			res[i] = new String[] {"Number of reactions", ""+num};
			i++;

			res[i] = new String[] {"Number of reversible reactions", ""+reversible};
			i++;

			res[i] = new String[] {"Number of irreversible reactions", ""+irreversible};
			i++;

			value = ModelAPI.countReactionsKEGG(aux, statement);
			res[i] = new String[] {"Number of reactions from KEGG", ""+value};
			i++;

			value = ModelAPI.countReactionsTransporters(aux, statement);
			res[i] = new String[] {"Number of reactions from the TRANSPORTERS annotation tool", ""+value};
			i++;

			res[i] = new String[] {"	Number of reactions with no name associated", ""+noname};
			i++;

			res[i] = new String[] {"	Number of reactions with no equation associated", ""+noequation};
			i++;

			value = ModelAPI.countPathwayHasReaction(aux, statement);
			res[i] = new String[] {"Number of reactions with no pathway associated", ""+(num-value)};
			i++;

			Pair<Double, Double> result = ProjectAPI.getReactantsAndProducts(aux, statement);

			res[i] = new String[] {"Average number of reactants by reaction",""+(result.getA()/(Double.valueOf(num)).doubleValue())};
			i++;
			res[i] = new String[] {"Average number of products by reaction", ""+(result.getB()/(Double.valueOf(num)).doubleValue())};

			statement.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @param encodedOnly
	 * @param completeOnly
	 * @return
	 */
	public PathwayReaction getReactionsData(boolean encodedOnly) { //, boolean completeOnly){

		this.ids = new TreeMap<Integer,String>(); 
		this.namesIndex = new HashMap<String,String>();
		this.activeReactions = new HashSet<>();
		this.formulasIndex = new HashMap<String,String>();
		PathwayReaction reactionsData=null;
		Map <String, String> pathways = new TreeMap <String, String>();
		Statement statement;
		//		Set<String> pathwaysSet=new TreeSet<String>();
		//		List<String> pathwaysList = new ArrayList<String>();
		//		Map <String, Integer> pathID = new TreeMap<String, Integer>();

		try {

			statement = this.connection.createStatement();

			pathways = ProjectAPI.getPathwaysNames(statement);

			ArrayList<String> columnsNames = new ArrayList<String>();
			columnsNames.add("info");
			columnsNames.add("pathway name");
			columnsNames.add("reaction name");
			columnsNames.add("equation");
			columnsNames.add("source");
			columnsNames.add("notes");
			columnsNames.add("reversible");
			columnsNames.add("in model");

			reactionsData = new PathwayReaction(columnsNames, "reactions", pathways, encodedOnly) {

				private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int col){
					if (col==0 || col>4) {

						return true;
					}
					else return false;
				}
			};

			ReactionsCapsule capsule = ModelAPI.getActiveReactions(statement, encodedOnly, this.getProject().isCompartmentalisedModel(), this.connection.getDatabaseType());

			this.setTableColumnsSize(capsule.getTableColumnsSize());

			this.ids = capsule.getIds();
			this.namesIndex = capsule.getNamesIndex();
			this.activeReactions = capsule.getActiveReactions();
			this.formulasIndex = capsule.getFormulasIndex();

			ArrayList<Object> data = capsule.getReactionsData();
			for (int i=0; i<data.size(); i++ ) {
				
				ArrayList<Object> line = (ArrayList<Object>) data.get(i);
				reactionsData.addLine((ArrayList<Object>) line.get(0), (String) line.get(1), (String) line.get(2));
			}
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return reactionsData;
	}

	/**
	 * @return a list with all pathways, except the SuperPathways
	 */
	public String[] getPathsBoolean(boolean encoded) {

		try {

			Statement statement;
			statement = this.connection.createStatement();
			List<String> pathways = new ArrayList<String>();
			Map <String, Integer> pathID = new TreeMap<String, Integer>();
			pathID.put("", 0);
			pathways.add("");
			Set<String> pathwaysSet=new TreeSet<String>();
			ArrayList<String[]> res = new ArrayList<>();
			String query = null;

			if(encoded) {

				query = "SELECT DISTINCT(idpathway), pathway.name, reaction.name " +
						" FROM reaction" +
						" INNER JOIN pathway_has_reaction ON idreaction=pathway_has_reaction.reaction_idreaction" +
						" INNER JOIN pathway ON pathway.idpathway=pathway_has_reaction.pathway_idpathway" +
						" AND  reaction.inModel ORDER BY pathway.name,  reaction.name ";
			}
			else {

				query = "SELECT idpathway, name FROM pathway ORDER BY name";

			}

			try {

				res = ProjectAPI.getPathways(query, statement);
			}
			catch(CommunicationsException e) {

				statement = HomologyAPI.checkStatement(this.getProject().getDatabase().getDatabaseAccess(), statement);
				res = ProjectAPI.getPathways(query, statement);
			}

			for(int i = 0; i<res.size(); i++){
				String[] list = res.get(i);

				pathID.put(list[1], Integer.parseInt(list[0]));
				pathwaysSet.add(list[1]);					
			}

			pathways.addAll(pathwaysSet);
			java.util.Collections.sort(pathways);
			this.paths = new String[pathways.size()+1];
			this.paths[0] = "All";

			for(int i=0;i<pathways.size();i++) {

				this.selectedPathIndexID.put(i+1, pathID.get(pathways.get(i)));		
				this.paths[i+1] = pathways.get(i);
			}
			statement.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return paths;
	}

	/**
	 * @param idReaction
	 * @return
	 */
	public String[] getEnzymes(int idReaction, int pathway) {

		Statement statement;
		String[] res = null;

		try {
			statement = this.connection.createStatement();

			if(pathway < 0) {
				res = HomologyAPI.getEnzymesByReaction(idReaction, statement);

				statement.close();
				return res;
			}

			res = HomologyAPI.getEnzymesByReactionAndPathway(idReaction, pathway, statement);

			statement.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @return
	 */
	public String[] getGenesModel() {

		Statement statement;
		ArrayList<String> lls = new ArrayList<String>();

		try {

			statement = this.connection.createStatement();

			lls = ModelAPI.getGenesModel(statement);

			statement.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		String[] res = new String[lls.size()+1];

		res[0] = "";

		for(int i=0;i<lls.size();i++)
			res[i+1] = lls.get(i);

		return res;
	}

	/**
	 * @return
	 */
	public Map<String, Integer> getGenesModelMap() {

		Statement statement;
		Map<String, Integer> ret = new HashMap<>();

		try {

			statement = this.connection.createStatement();

			Map<Integer, Pair<String, String>> pairMap = ModelAPI.getGenesFromDatabase(statement);

			statement.close();

			for(Integer idgene : pairMap.keySet()) {

				Pair<String, String> pair = pairMap.get(idgene); 
				String gene = pair.getA();

				if(pair.getB() != null && !pair.getB().trim().isEmpty())
					gene = gene.concat(" (").concat(pair.getB()).concat(")");

				ret.put(gene, idgene);
			}

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		return ret;
	}

	/**
	 * @return
	 */
	public String[] getEnzymesModel() {

		Statement statement;
		ArrayList<String> lls = new ArrayList<String>();

		try {

			statement = this.connection.createStatement();

			lls = ModelAPI.getEnzymesModel(statement);

			statement.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		String[] res = new String[lls.size()+1];

		res[0] = "";

		for(int i=0;i<lls.size();i++){res[i+1] = lls.get(i);}

		return res;
	}

	/**
	 * @param rowID
	 * @return
	 */
	public Set<String> getEnzymesForReaction(int rowID) {

		Statement statement;
		Set<String> res = new TreeSet<>();

		try  {

			statement = this.connection.createStatement();

			res = ModelAPI.getEnzymesForReaction(rowID, statement);

			statement.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @param rowID
	 * @return
	 */
	public String[] getPathways(int rowID) {

		Statement statement;
		String[] res = new String[0];

		try  {

			statement = this.connection.createStatement();

			res = ModelAPI.getPathwaysByRowID(rowID, statement);

			statement.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @return list of all pathways, including superpathways
	 */
	public String[] getPathways(boolean inModel) {


		String aux = "";

		if(inModel)
			aux = " INNER JOIN pathway_has_reaction on (pathway_idpathway = idpathway) "+
					" INNER JOIN reaction on (reaction_idreaction = idreaction) "+
					" WHERE inModel ";

		Statement statement;
		List<String> lls = new ArrayList<String>();
		try
		{
			statement = this.connection.createStatement();

			lls = ModelAPI.getPathways2(aux, statement);

			statement.close();
		}
		catch (SQLException ex)
		{
			ex.printStackTrace();
			// handle any errors
			//			System.out.println("SQLException: " + ex.getMessage());
			//			System.out.println("SQLState: " + ex.getSQLState());
			//			System.out.println("VendorError: " + ex.getErrorCode());
		}

		String[] res = new String[lls.size()+1];

		res[0] = "";

		for(int i=0;i<lls.size();i++){res[i+1] = lls.get(i);}

		return res;
	}

	/**
	 * @param name
	 * @return
	 */
	public int getPathwayID(String name) {

		Statement statement;
		int res=-1;

		try {

			statement = this.connection.createStatement();
			String query = "SELECT idpathway, name FROM pathway " +
					"WHERE name='"+DatabaseUtilities.databaseStrConverter(name,this.connection.getDatabaseType())+"';";

			res = ModelAPI.getPathwayID(query, statement);

			statement.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @param name
	 * @return
	 */
	public String getPathwayCode(String name) {

		Statement statement;
		String res="";

		try {

			statement = this.connection.createStatement();
			String query = "SELECT code FROM pathway " +
					"WHERE name='"+DatabaseUtilities.databaseStrConverter(name,this.connection.getDatabaseType())+"';";

			res = ModelAPI.getPathwayCode(query, statement);

			statement.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	public DataTable[] getRowInfo(int id) {


		Map<String,String> mets = new HashMap<>();
		DataTable[] results = new DataTable[5];
		Statement statement;

		try {

			if(this.getBalanceValidator()!= null)
				results = new DataTable[results.length+1];

			if(this.getGapReactions()!= null)
				results = new DataTable[results.length+1];

			statement = this.connection.createStatement();
			
			List<List<Pair<String, String>>> geneRules = ModelAPI.getBooleanRuleFromReaction(id, statement);

			if(geneRules != null)
				results = new DataTable[results.length+1];

			int counter = 0;

			List<String> columnsNames = new ArrayList<String>();
			columnsNames.add("metabolite");
			columnsNames.add("formula");
			columnsNames.add("KEGG ID");
			columnsNames.add("compartment");
			columnsNames.add("stoichiometric coefficient");
			columnsNames.add("number of chains");
			results[counter++] = new DataTable(columnsNames, "reaction");

			columnsNames = new ArrayList<String>();
			columnsNames.add("identifier");
			columnsNames.add("proteins");
			columnsNames.add("in model");
			results[counter++] = new DataTable(columnsNames, "enzymes");

			columnsNames = new ArrayList<String>();
			columnsNames.add("property");
			columnsNames.add("values");
			results[counter++] = new DataTable(columnsNames, "properties");

			columnsNames = new ArrayList<String>();
			columnsNames.add("synonyms");
			results[counter++] = new DataTable(columnsNames, "synonyms");

			columnsNames = new ArrayList<String>();
			columnsNames.add("pathways");
			results[counter++] = new DataTable(columnsNames, "pathways");

			if(this.getBalanceValidator()!= null) {

				columnsNames = new ArrayList<String>();
				columnsNames.add("stoichiometric balance");
				columnsNames.add("values");
				results[counter++] = new DataTable(columnsNames, "balance");
			}

			if(this.getGapReactions()!= null) {

				columnsNames = new ArrayList<String>();
				columnsNames.add("metabolite");
				columnsNames.add("KEGG ID");
				columnsNames.add("dead end");
				results[counter++] = new DataTable(columnsNames, "Gaps");
			}

			if(geneRules != null) {

				columnsNames = new ArrayList<String>();
				columnsNames.add("rules");
				results[counter++] = new DataTable(columnsNames, "GPRs");
			}

			ArrayList<String> resultsList = new ArrayList<String>();
			counter = 0;

			ArrayList<String[]> result = ModelAPI.getCompoundData(id, statement);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				resultsList = new ArrayList<String>();

				if(list[0]==null || list[0].isEmpty())
					resultsList.add(list[5]);
				else
					resultsList.add(list[0]);

				resultsList.add(list[1]);
				resultsList.add(list[5]);
				resultsList.add(list[2]);
				resultsList.add(list[3]);
				resultsList.add(list[4]);
				results[counter].addLine(resultsList);
				mets.put(resultsList.get(2), resultsList.get(0));
			}

			counter++;

			result = ModelAPI.getReactionHasEnzymeData(id, statement);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				resultsList = new ArrayList<String>();
				resultsList.add(list[0]);
				resultsList.add(list[1]);
				resultsList.add(list[2]);
				results[counter].addLine(resultsList);
			}

			counter++;

			result = ModelAPI.getReactionData(id, statement);

			String name = "";

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				//				if(rs.getString("boolean_rule")!=null && !rs.getString("boolean_rule").equalsIgnoreCase("null") && !rs.getString("boolean_rule").isEmpty()) {
				//
				//					resultsList = new ArrayList<String>();
				//					resultsList.add("Gene-Protein-Reaction rule");
				//					resultsList.add(rs.getString("boolean_rule"));
				//					results[counter].addLine(resultsList);
				//				}

				resultsList = new ArrayList<String>();
				resultsList.add("Generic");
				resultsList.add(list[0]);
				results[counter].addLine(resultsList);

				resultsList = new ArrayList<String>();
				resultsList.add("Spontaneous");
				resultsList.add(list[1]);
				results[counter].addLine(resultsList);

				resultsList = new ArrayList<String>();
				resultsList.add("Non Enzymatic");
				resultsList.add(list[2]);
				results[counter].addLine(resultsList);

				String lb = list[3];

				if((list[3] == null || list[3].equalsIgnoreCase("null")) || list[3].isEmpty()) {

					lb = "0";
					if(Boolean.valueOf(list[5]))
						lb = "-999999";
				}

				String ub = list[4];

				if(((list[4] == null || list[4].equalsIgnoreCase("null")) || list[4].isEmpty()))
					ub = "999999";

				resultsList = new ArrayList<String>();
				resultsList.add("Lower Bound");
				resultsList.add(lb);
				results[counter].addLine(resultsList);

				resultsList = new ArrayList<String>();
				resultsList.add("Upper Bound");
				resultsList.add(ub);
				results[counter].addLine(resultsList);

				name=list[6];
			}


			if(name.contains("_C"))
				id = ProjectAPI.getReactionIdByName(name.split("_C")[0], statement);

			counter++;

			ArrayList<String> alias = ProjectAPI.getAliasClassR(id, statement);

			if(alias.size() == 0)
				alias.add("");

			results[counter].addLine(alias);

			counter++;
			for(String pathway : this.getPathways(id)) {

				resultsList = new ArrayList<String>();
				resultsList.add(pathway);
				results[counter].addLine(resultsList);
			}


			if(this.getBalanceValidator()!= null && this.externalModelIds.containsKey(name)) {
				counter++;

				resultsList = new ArrayList<String>();
				resultsList.add("Sum of reactants");
				resultsList.add(balanceValidator.getSumOfReactantsToString(this.externalModelIds.get(name)));
				results[counter].addLine(resultsList);
				resultsList = new ArrayList<String>();
				resultsList.add("Sum of products");
				resultsList.add(balanceValidator.getSumOfProductsToString(this.externalModelIds.get(name)));
				results[counter].addLine(resultsList);
				resultsList = new ArrayList<String>();
				resultsList.add("Balance");
				resultsList.add(balanceValidator.getDifResultToString(this.externalModelIds.get(name)));
				results[counter].addLine(resultsList);
			}


			if(this.getGapReactions()!= null) {

				counter++;
				for(String kegg_id : mets.keySet()) {

					resultsList = new ArrayList<String>();
					resultsList.add(mets.get(kegg_id));
					resultsList.add(kegg_id);
					resultsList.add(this.getGapReactions().getCompounds().contains(kegg_id)+"");
					results[counter].addLine(resultsList);
				}
			}

			if(geneRules !=null) {

				counter++;

				for(List<Pair<String, String>> geneRule : geneRules) {

					resultsList = new ArrayList<String>();
					String rule = null;
					for(Pair<String, String> gene : geneRule) {

						if(rule == null) {

							rule = "";
						}
						else {

							rule = rule.concat(" AND ");
						}

						rule = rule.concat(gene.getA());

						if(gene.getB() != null)
							rule = rule.concat(" (").concat(gene.getB()).concat(")");

					}
					resultsList.add(rule);					
					results[counter].addLine(resultsList);					
				}
			}
			statement.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		return results;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getReactionName(int id) {

		return this.namesIndex.get(id + "");
	}

	/**
	 * @param name
	 * @return
	 */
	public int getReactionID(String name) {

		int id = -1;
		Statement statement;

		try {

			statement = this.connection.createStatement();

			String aux = DatabaseUtilities.databaseStrConverter(name,this.connection.getDatabaseType());

			id = ProjectAPI.getReactionIdByName(aux, statement);

			statement.close();

		}
		catch (Exception e) {

			e.printStackTrace();
		}
		return id;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getFormula(int id) {

		if(this.formulasIndex.containsKey(String.valueOf(id)))
			return this.formulasIndex.get(String.valueOf(id));
		else
			return null;
	}

	/**
	 * @param selectedRow
	 * 
	 * duplicate a given reaction
	 */
	public void duplicateReaction(int  reactionID) {

		Map<String, Set<String>> selectedEnzymesPathway = new TreeMap<String, Set<String>>();

		Statement statement;

		try  {

			statement = this.connection.createStatement();

			String[] list = ModelAPI.getReactionData2(reactionID, statement); 

			String name=list[0],
					equation=list[1],
					compartment_name = list[4], //7
					source = list[10],  //13
					boolean_rule = list[11];
			double  lowerBound = Double.parseDouble(list[8]), //11
					upperBound = Double.parseDouble(list[9]); //12
			boolean reversibility = Boolean.valueOf(list[2]),
					inModel = Boolean.valueOf(list[3]), //6
					isSpontaneous = Boolean.valueOf(list[5]), //8 
					isNonEnzymatic = Boolean.valueOf(list[6]), //9
					isGeneric = Boolean.valueOf(list[7]); //10

			Map<String, String> chains=new TreeMap<String, String>(), compartment=new TreeMap<String, String>(),
					metabolites=new TreeMap<String, String>();

			list = ModelAPI.getStoichiometryData(reactionID, statement);

			if(list[1].startsWith("-")) {

				metabolites.put("-"+list[3],list[1]);
				chains.put("-"+list[3],list[2]);
				compartment.put("-"+list[3], list[0]);
			}
			else {

				metabolites.put(list[3],list[1]);
				chains.put(list[3],list[2]);
				compartment.put(list[3], list[0]);
			}

			for(String pathway: this.getPathways(reactionID)) {

				Set<String> enzymesSet=new TreeSet<String>();
				enzymesSet.addAll(new TreeSet<String>(Arrays.asList(this.getEnzymes(reactionID, this.getPathwayID(pathway)))));
				selectedEnzymesPathway.put(pathway, enzymesSet);
			}

			if(selectedEnzymesPathway.isEmpty())
				selectedEnzymesPathway.put("-1allpathwaysinreaction", this.getEnzymesForReaction(reactionID));

			this.insertNewReaction(incrementName(name,statement), equation, reversibility, 
					chains, compartment, metabolites, inModel, selectedEnzymesPathway, 
					compartment_name, isSpontaneous, isNonEnzymatic, isGeneric, lowerBound, upperBound, source, boolean_rule);
			statement.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
	}

	/**
	 * Insert a new reaction on the database
	 * 
	 * @param name
	 * @param equation
	 * @param reversibility
	 * @param metabolitesChains
	 * @param metabolitesCompartments
	 * @param metabolitesStoichiometry
	 * @param inModel
	 * @param enzymesInPathway
	 * @param reactionCompartment
	 * @param isSpontaneous
	 * @param isNonEnzymatic
	 * @param isGeneric
	 * @param lowerBound
	 * @param upperBound
	 * @param source 
	 * @param boolean_rule 
	 */
	public void insertNewReaction(String name, String equation, boolean reversibility, //Set<String> pathways, Set<String> enzymes, 
			Map<String,String> metabolitesChains, Map<String, String > metabolitesCompartments, Map<String, String> metabolitesStoichiometry, boolean inModel, Map<String, 
			Set<String>> enzymesInPathway, String reactionCompartment, boolean isSpontaneous, boolean isNonEnzymatic,
			boolean isGeneric, double lowerBound, double upperBound, String source, String boolean_rule) {

		Statement statement;
		try {

			statement = this.connection.createStatement();

			ModelAPI.insertNewReaction(name, equation, reversibility, metabolitesChains, metabolitesCompartments, metabolitesStoichiometry, inModel, 
					enzymesInPathway, reactionCompartment, isSpontaneous, isNonEnzymatic, isGeneric, lowerBound, upperBound, source, boolean_rule, 
					this.getProject().isCompartmentalisedModel(), this.connection.getDatabaseType(), statement);

			statement.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		catch (Exception e) {

			Workbench.getInstance().error("Reaction with the same name ("+name+") already exists. Aborting operation!");
		}
	}

	/**
	 * @param selectedRow
	 * 
	 * remove a given reaction
	 */
	public void removeReaction(int  reaction_id) {

		Statement statement;

		try {

			statement = this.connection.createStatement();

			ModelAPI.removeSelectedReaction(statement, reaction_id);

			statement.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
	}
	
	/**
	 * @param rowID
	 * @param name
	 * @param equation
	 * @param reversibility
	 * @param chains
	 * @param compartment
	 * @param metabolites
	 * @param inModel
	 * @param selectedEnzymesPathway
	 * @param localisation
	 * @param isSpontaneous
	 * @param isNonEnzymatic
	 * @param isGeneric
	 * @param lowerBound
	 * @param upperBound
	 * @param boolean_rule 
	 */
	public void updateReaction(int idReaction, String name, String equation, boolean reversibility, //Set<String> enzymes,
			Map<String, String> chains, Map<String, String > compartment, Map<String, String> metabolites, boolean inModel, 
			Map<String, Set<String>> selectedEnzymesPathway, String localisation, boolean isSpontaneous, boolean isNonEnzymatic,
			boolean isGeneric, double lowerBound, double upperBound, String boolean_rule) {

		Statement statement;
		try {

			if(equation.contains(" <= ")) {

				String [] equationArray = equation.split(" <= ");
				equation = equationArray[1]+" => "+equationArray[0];
			}

			if(!name.startsWith("R") && !name.startsWith("T") && !name.startsWith("K") && !name.toLowerCase().contains("biomass"))
				name = "R_"+name;

			statement = this.connection.createStatement();

			int idCompartment = CompartmentsAPI.getCompartmentID(localisation, statement);

			String aux="";

			boolean inModelReaction= ProjectAPI.isReactionInModel(idReaction, statement);

			if(inModelReaction != inModel)
				aux=", inModel="+ inModel ;

			if(boolean_rule!=null)
				boolean_rule = "'"+boolean_rule+"'";

			String query = "UPDATE reaction SET name = '" + DatabaseUtilities.databaseStrConverter(name,this.connection.getDatabaseType()) + 
					"', equation = '" + DatabaseUtilities.databaseStrConverter(equation,this.connection.getDatabaseType()) + 
					"', reversible = " + reversibility + " " +
					", compartment_idcompartment = "+idCompartment+
					", isSpontaneous = "+isSpontaneous+
					", isNonEnzymatic = "+isNonEnzymatic+
					", isGeneric = "+isNonEnzymatic+
					", boolean_rule = "+boolean_rule+ 
					", lowerBound = "+lowerBound+
					", upperBound = "+upperBound+
					aux+
					" WHERE idreaction='"+idReaction+"'";

			ProjectAPI.executeQuery(query, statement);


			//PATHWAYS AND ENZYMES PROCESSING
			{
				Map<Integer,String> existingPathwaysID = new TreeMap<>();
				Map<Integer,Set<String>> newPathwaysID = new TreeMap<>();
				Map<Integer,Set<String>>  editedPathwaysID = new TreeMap<> ();
				selectedEnzymesPathway.remove("");

				existingPathwaysID = ProjectAPI.getExistingPathwaysID(idReaction, statement);

				// IF There are enzymes and no pathway! or add to all pathways
				List<String> existingEnzymesID = new ArrayList<String>();

				if(selectedEnzymesPathway!= null && selectedEnzymesPathway.get("-1allpathwaysinreaction")!= null && selectedEnzymesPathway.get("-1allpathwaysinreaction").size()>0) {

					existingEnzymesID = ProjectAPI.getExistingEnzymesID(idReaction, statement);

					for(String enzyme: new ArrayList<String>(selectedEnzymesPathway.get("-1allpathwaysinreaction"))) {

						if(existingEnzymesID.contains(enzyme)) {

							existingEnzymesID.remove(enzyme);
							selectedEnzymesPathway.get("-1allpathwaysinreaction").remove(enzyme);
						}
					}

					for(String enzyme: selectedEnzymesPathway.get("-1allpathwaysinreaction")) {

						String ecnumber = enzyme.split("___")[0];

						int idProtein = Integer.parseInt(enzyme.split("___")[2]);

						boolean exists = ProjectAPI.checkReactionHasEnzymeData(ecnumber, idProtein, idReaction, statement);

						if(!exists) {

							query = "INSERT INTO reaction_has_enzyme (enzyme_ecnumber,enzyme_protein_idprotein,reaction_idreaction) "
									+ "VALUES ('" + ecnumber + "'," +idProtein+","+idReaction+") ";

							ProjectAPI.executeQuery(query, statement);
						}
					}

					for(String enzyme:existingEnzymesID) {

						String ecnumber = enzyme.split("___")[0];

						String idProtein = enzyme.split("___")[2];

						query = "DELETE FROM reaction_has_enzyme WHERE reaction_idreaction = "+idReaction+" "
								+ "AND enzyme_protein_idprotein = "+idProtein+" AND enzyme_ecnumber='"+ecnumber+"'";

						ProjectAPI.executeQuery(query, statement);
					}
				}
				else {

					existingEnzymesID = ProjectAPI.getExistingEnzymesID2(idReaction, statement);

					for(String enzyme:existingEnzymesID) {

						String ecnumber = enzyme.split("___")[0];

						String idProtein = enzyme.split("___")[2];

						query = "DELETE FROM reaction_has_enzyme WHERE reaction_idreaction = "+idReaction
								+" AND enzyme_protein_idprotein = "+idProtein+" AND enzyme_ecnumber='"+ecnumber+"'";

						ProjectAPI.executeQuery(query, statement);
					}
				}

				Set<String> genericECNumbers = new TreeSet<String>();
				if(selectedEnzymesPathway.get("-1allpathwaysinreaction") != null) {

					genericECNumbers.addAll(selectedEnzymesPathway.get("-1allpathwaysinreaction"));
				}

				selectedEnzymesPathway.remove("-1allpathwaysinreaction");

				if(selectedEnzymesPathway.size()>0) {

					existingPathwaysID = ProjectAPI.getExistingPathwaysID2(existingPathwaysID, idReaction, statement);

					for(String pathway:selectedEnzymesPathway.keySet()) {

						String aux2 = DatabaseUtilities.databaseStrConverter(pathway,this.connection.getDatabaseType());

						int res = ProjectAPI.getPathwayID(aux2, statement);

						Set<String> enzymes = new TreeSet<String>(selectedEnzymesPathway.get(pathway));
						if(enzymes.isEmpty()) {

							enzymes.addAll(genericECNumbers);
						}
						newPathwaysID.put(res, enzymes);
					}

					for(Integer pathway: new ArrayList<>(existingPathwaysID.keySet())) {

						if(newPathwaysID.containsKey(pathway)) {

							editedPathwaysID.put(pathway, new TreeSet<>(newPathwaysID.get(pathway)));
							newPathwaysID.remove(pathway);
							existingPathwaysID.remove(pathway);
						}
					}

					//when pathways are deleted, they are just removed from the pathway has reaction association
					for(int pathway:existingPathwaysID.keySet()) {

						query = "DELETE FROM pathway_has_reaction WHERE reaction_idreaction = "+idReaction+" AND pathway_idpathway = "+pathway;

						ProjectAPI.executeQuery(query, statement);
					}

					Map<Integer,Set<Integer>> pathsEnzymesIn = new TreeMap<>();
					Map<Integer,Set<Integer>> pathsReactionsIn = new TreeMap<>();
					//insert the new pathways

					for(int pathway : newPathwaysID.keySet()) {

						query = "INSERT INTO pathway_has_reaction (pathway_idpathway, reaction_idreaction) VALUES ("+pathway+","+idReaction+")";

						ProjectAPI.executeQuery(query, statement);

						for(String enzyme: newPathwaysID.get(pathway)) {

							Set<Integer> existsEnzReaction=new TreeSet<>();
							Set<Integer> existsEnzPathway=new TreeSet<>();

							String ecnumber = enzyme.split("___")[0];

							int idProtein = Integer.parseInt(enzyme.split("___")[2]);

							boolean exists = ProjectAPI.checkPathwayHasEnzymeData(ecnumber, idProtein, pathway, statement);

							if(!exists) {

								query = "INSERT INTO pathway_has_enzyme (pathway_idpathway, enzyme_ecnumber,enzyme_protein_idprotein) " +
										"VALUES ("+pathway+",'"+ecnumber+"',"+idProtein+")";

								ProjectAPI.executeQuery(query, statement);

								existsEnzPathway.add(idProtein);
								pathsEnzymesIn.put(pathway, existsEnzPathway);
							}

							exists = ProjectAPI.checkReactionHasEnzymeData(ecnumber, idProtein, idReaction, statement);
							if(!exists) {

								query = "INSERT INTO reaction_has_enzyme (enzyme_ecnumber,enzyme_protein_idprotein,reaction_idreaction) " +
										"VALUES ('" + ecnumber + "',"+idProtein+","+idReaction+") ";

								ProjectAPI.executeQuery(query, statement);

								existsEnzReaction.add(idProtein);
								pathsReactionsIn.put(pathway, existsEnzReaction);
							}
						}
					}
					// edited pathways
					for(int pathway:editedPathwaysID.keySet()) {

						existingEnzymesID = new ArrayList<String>(Arrays.asList(this.getEnzymes(idReaction,pathway)));
						editedPathwaysID.get(pathway).remove("");

						for(String ecnumber: new TreeSet<String>(editedPathwaysID.get(pathway))) {

							for(String existingEcnumber :new TreeSet<String>(existingEnzymesID)) {

								if(existingEcnumber.equals(ecnumber)) {

									editedPathwaysID.get(pathway).remove(ecnumber);
									existingEnzymesID.remove(existingEcnumber);
								}
							}
						}

						for(String enzyme: new TreeSet<String>(editedPathwaysID.get(pathway))) {

							Set<Integer> existsEnzReaction=new TreeSet<>();
							Set<Integer> existsEnzPathway=new TreeSet<>();

							String ecnumber = enzyme.split("___")[0];

							int idProtein = Integer.parseInt(enzyme.split("___")[2]);

							boolean exists = ProjectAPI.checkPathwayHasEnzymeData(ecnumber, idProtein, pathway, statement);

							if(!exists) {

								query ="INSERT INTO pathway_has_enzyme (pathway_idpathway, enzyme_ecnumber,enzyme_protein_idprotein) " +
										"VALUES ('" + pathway + "' , '"+ecnumber+ "' , '"+idProtein+ "')";

								ProjectAPI.executeQuery(query, statement);

								existsEnzPathway.add(idProtein);
								pathsEnzymesIn.put(pathway, existsEnzPathway);
							}

							exists = ProjectAPI.checkReactionHasEnzymeData(ecnumber, idProtein, idReaction, statement);

							if(!exists) {

								query = "INSERT INTO reaction_has_enzyme (enzyme_ecnumber,enzyme_protein_idprotein,reaction_idreaction) " +
										"VALUES ('" + ecnumber + "', '" +idProtein+"', '"+idReaction+" ') ";

								ProjectAPI.executeQuery(query, statement);

								existsEnzReaction.add(idProtein);
								pathsReactionsIn.put(pathway, existsEnzReaction);
							}
							//}

						}

						for(String enzyme: new TreeSet<String>(existingEnzymesID)) {

							String ecNumber = enzyme.split("___")[0];

							int idProtein = Integer.parseInt(enzyme.split("___")[2]);

							Set<Integer> reactionsID = ModelAPI.getReactionsID(pathway, ecNumber, idProtein, statement);

							//reactionsID.remove(idReaction);

							if(reactionsID.size()==0) {

								statement.execute("DELETE FROM pathway_has_enzyme WHERE pathway_has_enzyme.enzyme_ecnumber = '"+ecNumber+"'  AND pathway_has_enzyme.enzyme_protein_idprotein = '"+idProtein+"' " +
										" AND pathway_has_enzyme.pathway_idpathway = "+pathway);
							}

							Set<Integer> pathwayID = ModelAPI.getPathwayID2(idReaction, ecNumber, idProtein, statement);

							pathwayID.remove(pathway);

							if(reactionsID.size()==0) {

								statement.execute("DELETE FROM reaction_has_enzyme WHERE reaction_has_enzyme.enzyme_ecnumber='"+ecNumber+"'  AND reaction_has_enzyme.enzyme_protein_idprotein = '"+idProtein+"' " +
										" AND reaction_has_enzyme.reaction_idreaction='"+idReaction+"'");								
							}
						}
					}
				}
				else {

					//when pathways are deleted, they are just removed from the pathway has reaction association
					for(int pathway:existingPathwaysID.keySet()) {

						query = "DELETE FROM pathway_has_reaction WHERE reaction_idreaction = "+idReaction+" AND pathway_idpathway = "+pathway;

						ProjectAPI.executeQuery(query, statement);
					}
				}
			}

			Map<String,Pair<String,Pair<String,String>>> existingMetabolitesID = ModelAPI.getExistingMetabolitesID(idReaction, statement);

			for(String m: new ArrayList<String>(metabolites.keySet())) {

				if(existingMetabolitesID.keySet().contains(m) && existingMetabolitesID.get(m).getB().getA().equalsIgnoreCase(metabolites.get(m))) {

					if(existingMetabolitesID.get(m).getB().getB().equalsIgnoreCase(compartment.get(m))) {

						existingMetabolitesID.remove(m);
						metabolites.remove(m);
					}
				}
			}

			for(String compound : existingMetabolitesID.keySet()) {

				query = "DELETE FROM stoichiometry " +
						"WHERE " +
						//						" reaction_idreaction = "+idReaction+
						//						" AND compound_idcompound = "+compound.replace("-", "") +
						//						" AND stoichiometric_coefficient = '"+existingMetabolitesID.get(compound)+"'"
						"idstoichiometry = "+existingMetabolitesID.get(compound).getA();

				ProjectAPI.executeQuery(query, statement);
			}

			for(String m :metabolites.keySet()) {

				idCompartment = CompartmentsAPI.getCompartmentID(compartment.get(m), statement);

				int idstoichiometry = ModelAPI.getStoichiometryID(idReaction, m, idCompartment, metabolites.get(m), statement);

				if(idstoichiometry>0) {

					query = "UPDATE stoichiometry SET " +
							"stoichiometric_coefficient = '" + metabolites.get(m) + "', " +
							"compartment_idcompartment = " + idCompartment + ", " +
							"compound_idcompound = " + m.replace("-", "") + ", " +
							"numberofchains = '" + chains.get(m) + "' " +
							"WHERE idstoichiometry ='"+idstoichiometry+ "'";

					ProjectAPI.executeQuery(query, statement);
				}
				else {

					query= "INSERT INTO stoichiometry (stoichiometric_coefficient, reaction_idreaction, compartment_idcompartment,compound_idcompound,numberofchains) " +
							"VALUES('" + metabolites.get(m) + "',"+idReaction+","+idCompartment+", '" + m.replace("-", "") + "', '" + chains.get(m) + "')" ;

					ProjectAPI.executeQuery(query, statement);
				}
			}
			statement.close();
		}
		catch (SQLException ex) {

			Workbench.getInstance().error(ex);
			ex.printStackTrace();
		}
	}


	/**
	 * @param rowID
	 * @return
	 */
	public ReactionContainer getReaction(int rowID) {

		Statement statement;
		ReactionContainer res = new ReactionContainer(rowID+"");

		try  {

			statement = this.connection.createStatement();

			String[] list = ProjectAPI.getDataForReactionContainer(rowID, statement);

			if(list.length>0) {

				res.setName(list[0]);
				res.setEquation(list[1]);
				res.setReversible(Boolean.valueOf(list[2]));
				res.setInModel(Boolean.valueOf(list[4]));
				res.setLocalisation(list[5]);
				res.setSpontaneous(Boolean.valueOf(list[6]));
				res.setNon_enzymatic(Boolean.valueOf(list[7]));
				res.setGeneric(Boolean.valueOf(list[8]));
				if(list[9]!= null && !list[9].isEmpty())
					res.setLowerBound(Double.parseDouble(list[9]));
				if(list[10]!= null && !list[10].isEmpty())
					res.setUpperBound(Double.parseDouble(list[10]));
				if(list[11]!= null && !list[11].isEmpty())
					res.setGeneRule(RulesParser.getGeneRule(list[11], ModelAPI.getGenesFromDatabase(statement)));
			}
			statement.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @param rowID
	 * @return
	 */
	public Map<String, MetaboliteContainer> getMetabolites(int rowID) {

		Statement statement;
		Map<String, MetaboliteContainer> res = new TreeMap<String, MetaboliteContainer>();
		try  {

			statement = this.connection.createStatement();

			res =  ProjectAPI.getStoichiometryData(rowID, statement);

			statement.close();
		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @return
	 */
	public String[][] getAllMetabolites() {

		Statement statement;
		String[][] res = null;

		try  {

			res = new String[4][];
			statement = this.connection.createStatement();
			String aux = "CASEWHEN (name is NULL, 1, 0)";
			if(this.connection.getDatabaseType().equals(DatabaseType.MYSQL))
				aux = "IF(ISNULL(name),1,0)";

			res = ProjectAPI.getAllMetabolites(res, aux, statement);

			statement.close();

		}
		catch (SQLException ex) {
			ex.printStackTrace();
		}

		return res;
	}

	/**
	 * colorize pathways
	 * @param encoded 
	 */
	public void colorPaths(){

		this.pathwayColors= new TreeMap<Integer,Color>();
		String[] paths = getPathsBoolean(false);

		List<Color> usedColors = new ArrayList<Color>(); 
		// no path reactions gets merlin logo color
		Color merlin = new Color(0, 128, 128);
		usedColors.add(merlin);
		this.pathwayColors.put(0, merlin);

		for(Integer path=1; path<paths.length; path++)			
			usedColors = this.newColor(usedColors, generateColor(), path);
	}

	/**
	 * @param usedColors
	 * @param color
	 * @param path
	 * @return
	 */
	private List <Color> newColor(List <Color> usedColors, Color color, Integer path) {

		if(usedColors.contains(color) || color.equals(new Color(0,0,0)) || color.equals(new Color(255,255,255))) {

			newColor(usedColors, generateColor(), path);
		}
		else {

			usedColors.add(color);
			this.pathwayColors.put(path, color);
		}

		return usedColors;		
	}

	/**
	 * @return
	 */
	private Color generateColor(){
		//		//		int red = new Random().nextInt(70);
		//		int red = new Random().nextInt(256);
		//		int green = new Random().nextInt(256);
		//		//		while(green<200){green = new Random().nextInt(225);}
		//		int blue = new Random().nextInt(256);
		//		//		while(blue<112){blue = new Random().nextInt(225);}

		Color mix = new Color(255, 255, 255);

		Random random = new Random();
		int red = random.nextInt(256);
		int green = random.nextInt(256);
		int blue = random.nextInt(256);

		// mix the color
		if (mix != null) {
			red = (red + mix.getRed()) / 2;
			green = (green + mix.getGreen()) / 2;
			blue = (blue + mix.getBlue()) / 2;
		}


		return new Color(red, green, blue);
	}

	public Map<Integer, Color> getPathwayColors() {
		return pathwayColors;
	}

	/**
	 * @param pathwayID
	 * @return
	 */
	public ReactionsInterfaceAux get_enzymes_id_list(int pathwayID) {

		Statement statement;
		Set<String> enzymes = new HashSet<String>(), reactions = new HashSet<String>(), compounds = new HashSet<>();
		Map<String, Set<String>> enzymesGapReactions = new HashMap<>();
		ReactionsInterfaceAux rca = new ReactionsInterfaceAux();

		try {

			statement = this.connection.createStatement();

			String aux = "";
			if(this.getProject().isCompartmentalisedModel())
				aux = aux.concat(" WHERE NOT originalReaction ");
			else
				aux = aux.concat(" WHERE originalReaction ");

			ArrayList<String[]> data = ProjectAPI.getPathwayHasEnzymeData(aux, pathwayID, statement);

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);

				String reaction_id = list[3];
				String surrogateEnzID = list[0]+"___"+list[5]+"___"+list[1];

				if(reaction_id.contains("_"))
					reaction_id=reaction_id.substring(0,reaction_id.indexOf("_"));

				if(Boolean.valueOf(list[2]) && Boolean.valueOf(list[4]))
					enzymes.add(surrogateEnzID);

				if(Boolean.valueOf(list[2]))
					reactions.add(reaction_id);

				//gaps
				if(this.getGapReactions()!= null) {

					String metabolite = list[6];
					compounds.add(metabolite);


					Set<String> reactionsSet = new HashSet<String>();
					if(enzymesGapReactions.containsKey(surrogateEnzID))
						reactionsSet = enzymesGapReactions.get(surrogateEnzID);
					reactionsSet.add(reaction_id);
					enzymesGapReactions.put(surrogateEnzID, reactionsSet);
				}
			}

			statement.close();

			if(this.getGapReactions()!= null) {

				for(String ecn:enzymesGapReactions.keySet()) {

					boolean remove = false;

					for(String pathwayReaction : enzymesGapReactions.get(ecn))
						for(String gapReaction : this.getGapReactions().getAllReactions().keySet())
							if(gapReaction.contains(pathwayReaction))
								remove = true;

					if(remove)
						enzymes.remove(ecn);
					//					else if(!enzymesGapReactions.get(ecn).isEmpty())
					//						enzymes.remove(ecn);
				}
			}
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		rca.setCompounds(compounds);
		rca.setEnzymes(enzymes);
		rca.setReactions(reactions);

		return rca;
	}

	/**
	 * @param noEnzymes 
	 * @param pathwayID
	 * @return
	 */
	public ReactionsInterfaceAux getReactionsList(boolean noEnzymes, int pathwayID) {

		Statement statement;
		Set<String> reactions = new HashSet<>(), compounds = new HashSet<>(), pathwayGapReactions = new HashSet<>();
		ReactionsInterfaceAux rca = new ReactionsInterfaceAux();

		try {

			statement = this.connection.createStatement();

			String aux = "", aux2=" AND (isNonEnzymatic OR isSpontaneous OR source='MANUAL' OR reaction_has_enzyme.enzyme_ecnumber IS NULL) ";

			if(this.getProject().isCompartmentalisedModel())
				aux = aux.concat(" WHERE NOT originalReaction ");
			else
				aux = aux.concat(" WHERE originalReaction ");

			if (noEnzymes)
				aux2 = "";

			ArrayList<String[]> result = ProjectAPI.getReactionsList(aux, aux2, pathwayID, statement);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				String composed_id = list[0];
				String reaction_id = list[0];

				if(reaction_id.contains("_"))
					reaction_id=reaction_id.substring(0,reaction_id.indexOf("_"));

				if(this.getGapReactions()!=null && this.getGapReactions().getAllReactions().keySet().contains(composed_id))
					pathwayGapReactions.add(reaction_id);

				if(pathwayGapReactions.contains(reaction_id) && !this.getGapReactions().getAllReactions().keySet().contains(composed_id))
					pathwayGapReactions.remove(reaction_id);

				String metabolite = list[1];
				compounds.add(metabolite);
				reactions.add(reaction_id);

			}
			//			List<String[]> proteins = new ArrayList<String[]>();
			//
			//			rs = statement.executeQuery("SELECT enzyme_protein_idprotein, enzyme_ecnumber FROM pathway_has_enzyme "+
			//					"WHERE pathway_idpathway = "+pathwayID);
			//
			//			while(rs.next()) {
			//
			//				proteins.add(new String[] {rs.getString(1),rs.getString(2)});
			//			}
			//
			//			for(String[] protein:proteins) {
			//
			//				rs = statement.executeQuery("SELECT name  FROM reaction_has_enzyme "+
			//						"INNER JOIN reaction ON (reaction_has_enzyme.reaction_idreaction = reaction.idreaction) "+
			//						"WHERE inModel AND enzyme_protein_idprotein= "+protein[0]+" AND enzyme_ecnumber = '"+protein[1]+"'");
			//				
			//				System.out.println("SELECT name  FROM reaction_has_enzyme "+
			//						"INNER JOIN reaction ON (reaction_has_enzyme.reaction_idreaction = reaction.idreaction) "+
			//						"WHERE inModel AND enzyme_protein_idprotein= "+protein[0]+" AND enzyme_ecnumber = '"+protein[1]+"'");
			//
			//				while(rs.next()) {
			//
			//					if(reactions.contains(rs.getString(1))) {
			//
			//						reactions.remove(rs.getString(1));
			//					}
			//				}
			//
			//			}

			statement.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		rca.setReactions(reactions);
		rca.setCompounds(compounds);

		return rca;
	}

	/**
	 * @return
	 */
	public String[] getCompartments(boolean isMetabolites, boolean isCompartmentalisedModel) {

		Statement statement;
		ArrayList<String> cls = new ArrayList<String>();

		try {

			statement = this.connection.createStatement();

			cls = CompartmentsAPI.getCompartments(isMetabolites, isCompartmentalisedModel, statement);

			statement.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		String[] res = new String[cls.size()];

		for(int i=0;i<cls.size();i++) {

			res[i] = cls.get(i);
		}

		return res;
	}

	/**
	 * @return the selectedPathIndexID
	 */
	public Map<Integer, Integer> getSelectedPathIndexID() {
		return selectedPathIndexID;
	}


	/**
	 * @return
	 */
	public Map<Integer,String> getIds() {

		return ids;
	}

	/**
	 * @param name
	 * @param statement
	 * @return
	 * @throws SQLException
	 */
	private String incrementName(String name, Statement statement) throws SQLException {

		if(name.contains(".")) {

			String[] rName = name.split("\\.");
			int version = Integer.parseInt(rName[1]);
			version=version+1;
			name=name.replace("."+rName[1], "."+version);
		}
		else{name=name.concat(".1");}

		boolean exists = ProjectAPI.checkReactionExistence(name, statement);

		if(exists){
			name = incrementName(name, statement);}

		statement.close();

		return name;
	}

	/**
	 * @return
	 */
	public String[] getPaths() {

		return paths;
	}


	/**
	 * @param paths
	 */
	public void setPaths(String[] paths) {
		this.paths = paths;
	}

	/**
	 * @return
	 */
	public boolean existGenes(){

		Statement statement; 

		try {

			statement = this.connection.createStatement();

			boolean exists = ProjectAPI.checkGenes(statement);

			statement.close();

			return exists;
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return false;
	}


	/**
	 * @return the tableColumnsSize
	 */
	public Integer[] getTableColumnsSize() {

		return tableColumnsSize;
	}


	/**
	 * @param tableColumnsSize the tableColumnsSize to set
	 */
	public void setTableColumnsSize(Integer[] tableColumnsSize) {
		this.tableColumnsSize = tableColumnsSize;
	}


	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getName()
	 */
	public String getName() {

		return "reactions";

	}

	/**
	 * @return
	 */
	public String getDefaultCompartment() {

		if(super.getProject().isCompartmentalisedModel()) {

			Statement statement;
			String interior = "inside";
			try {

				statement = this.connection.createStatement();

				interior = CompartmentsAPI.getCompartmentAbbreviation(interior, statement);

				statement.close();
			} 
			catch (SQLException e) {

				e.printStackTrace();
			}
			return interior;
		}
		else {

			return "inside";
		}
	}


	/**
	 * @param reactionID
	 * @param columnNumber
	 * @param object
	 */
	public void updateReactionProperties(String reactionID, int columnNumber, Object object) {

		try {

			Statement statement = this.connection.createStatement();

			if(columnNumber==NOTES_COLUMN) {

				String query = "UPDATE reaction SET notes = '"+DatabaseUtilities.databaseStrConverter((String) object,this.connection.getDatabaseType())+
						"' WHERE idreaction='"+reactionID+"'";

				ProjectAPI.executeQuery(query, statement);
			}
			else {

				boolean value = (Boolean) object;

				Pair<Boolean, Boolean> pair = ModelAPI.checkReactionIsReversibleAndInModel(reactionID, statement);

				if((columnNumber == IS_REVERSIBLE_COLUMN && value!=pair.getA()) || //(columnNumber == IS_GENERIC_COLUMN && value!=rs.getBoolean(2)) ||
						(columnNumber == IN_MODEL_COLUMN && value!=pair.getB())) {

					String equation="", source="", lowerBound = "0";

					Pair<String, String> res = ModelAPI.getEquationAndSourceFromReaction(reactionID, statement);

					equation = res.getA();
					source = res.getB();

					if(columnNumber==IS_REVERSIBLE_COLUMN) {

						if(value) {

							equation=equation.replace(" => ", " <=> ").replace(" <= ", " <=> ");
							lowerBound = "-999999";
						}
						else {

							equation=equation.replace("<=>", "=>");
						}

						String query = "UPDATE reaction SET equation = '"+DatabaseUtilities.databaseStrConverter(equation,this.connection.getDatabaseType())+
								"', reversible = " + value + ", lowerBound = '"+lowerBound+"' WHERE idreaction='"+reactionID+"'";

						ProjectAPI.executeQuery(query, statement);
					}
					//					else if(columnNumber==IS_GENERIC_COLUMN) {
					//
					//						statement.execute("UPDATE reaction SET isGeneric = " + value + " WHERE idreaction='"+reactionID+"'");
					//					}
					else {

						if(source.equalsIgnoreCase("KEGG"))
							source = "MANUAL";

						String query = "UPDATE reaction SET inModel = " + value + ", source='"+source+"' WHERE idreaction='"+reactionID+"'";

						ProjectAPI.executeQuery(query, statement);
					}
				}
			}
			statement.close();
		} 
		catch (SQLException e) {

			e.printStackTrace();
		}
	}


	/**
	 * @return the activeReactions
	 */
	public Set<String> getActiveReactions() {
		return activeReactions;
	}


	/**
	 * @param activeReactions the activeReactions to set
	 */
	public void setActiveReactions(Set<String> activeReactions) {
		this.activeReactions = activeReactions;
	}


	/**
	 * @return the gapReactions
	 */
	public ReactionGapsAux getGapReactions() {
		return gapReactions;
	}


	/**
	 * @param gapReactions the gapReactions to set
	 */
	public void setGapReactions(ReactionGapsAux gapReactions) {
		this.gapReactions = gapReactions;

		this.setNewGaps(true);
	}


	/**
	 * @return the balanceValidator
	 */
	public BalanceValidator getBalanceValidator() {
		return balanceValidator;
	}


	/**
	 * @param balanceValidator the balanceValidator to set
	 */
	public void setBalanceValidator(BalanceValidator balanceValidator) {
		this.balanceValidator = balanceValidator;
	}


	/**
	 * @return the externalModelIds
	 */
	public Map<String, String> getExternalModelIds() {
		return externalModelIds;
	}


	/**
	 * @param externalModelIds the externalModelIds to set
	 */
	public void setExternalModelIds(Map<String, String> externalModelIds) {
		this.externalModelIds = externalModelIds;
	}


	/**
	 * @return the newGaps
	 */
	public boolean isNewGaps() {
		return newGaps;
	}


	/**
	 * @param newGaps the newGaps to set
	 */
	public void setNewGaps(boolean newGaps) {
		this.newGaps = newGaps;
	}


	/**
	 * @param row
	 * @return
	 */
	public String getRowID(int row) {

		return ids.get(row);
	}

	/**
	 * @param id
	 * @return
	 */
	public int getRowFromID(String id) {

		for(int i : ids.keySet())
			if(this.ids.get(i).equalsIgnoreCase(id))
				return i;

		return -1;
	}



}
