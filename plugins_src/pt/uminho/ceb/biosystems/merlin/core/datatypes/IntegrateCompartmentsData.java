package pt.uminho.ceb.biosystems.merlin.core.datatypes;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.uminho.ceb.biosystems.merlin.core.utilities.DatabaseLoaders;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.CompartmentsAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.HomologyAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.TransportersAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.GeneCompartments;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.ProcessCompartments;
import pt.uminho.ceb.biosystems.merlin.transporters.core.utils.TransportersUtilities;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.capsules.DatabaseReactionContainer;

/**
 * @author ODias
 *
 */
public class IntegrateCompartmentsData extends Observable implements IntegrateData {

	private static final Logger logger = LoggerFactory.getLogger(IntegrateCompartmentsData.class);

	private Connection connection;
	private Map<String,GeneCompartments> geneCompartments;
	private long startTime;
	private TimeLeftProgress progress;
	private AtomicBoolean cancel;
	private ProcessCompartments processCompartments;
	private AtomicInteger processingTotal;
	private AtomicInteger processingCounter;


	/**
	 * @param project
	 * @param threshold
	 */
	public IntegrateCompartmentsData(Project project, Map<String,GeneCompartments> geneCompartments) {

		try {

			this.connection = new Connection(project.getDatabase().getDatabaseAccess());
			this.geneCompartments = geneCompartments;
			this.startTime = GregorianCalendar.getInstance().getTimeInMillis();
			this.cancel = new AtomicBoolean(false);
			this.processCompartments = new ProcessCompartments();
		} 
		catch (SQLException e) {

			e.printStackTrace();
		}

	}

	/**
	 * @param bool
	 * @throws SQLException 
	 */
	public boolean initProcessCompartments() throws SQLException {

		Set<String> compartments = new HashSet<>();
		Statement stmt = this.connection.createStatement();

		compartments = ModelAPI.getCompartments(stmt);
		stmt.close();

		this.processCompartments.initProcessCompartments(compartments);
		return true;
	}

	/**
	 * @return
	 */
	public boolean performIntegration() {

		try {

			Statement statement = this.connection.createStatement();
			this.processingTotal.set(this.geneCompartments.size());

			Map<String, String> sequenceID_geneID = HomologyAPI.getSequenceID(statement);

			Map<String,String> compartmentsDatabaseIDs = new HashMap<String,String>();

			for(String sequence_id :this.geneCompartments.keySet()) {

				if(this.cancel.get()) {

					this.processingCounter = new AtomicInteger(this.geneCompartments.keySet().size());
					break;
				}
				else {

					GeneCompartments geneCompartments = this.geneCompartments.get(sequence_id);
					String primaryCompartment = geneCompartments.getPrimary_location();
					String primaryCompartmentAbb = geneCompartments.getPrimary_location_abb();
					double scorePrimaryCompartment = geneCompartments.getPrimary_score();
					Map<String, Double> secondaryCompartments = geneCompartments.getSecondary_location();
					Map<String, String> secondaryCompartmentsAbb = geneCompartments.getSecondary_location_abb();

					compartmentsDatabaseIDs.putAll(ModelAPI.getCompartmentsDatabaseIDs(primaryCompartment, primaryCompartmentAbb, secondaryCompartments, secondaryCompartmentsAbb, compartmentsDatabaseIDs, statement));

					String idGene = null;
					if(sequenceID_geneID.containsKey(geneCompartments.getGene()))
						idGene = sequenceID_geneID.get(geneCompartments.getGene());

					if(idGene==null)
						logger.trace("Gene {} not found!", sequence_id);
					else						
						ModelAPI.loadGenesCompartments(idGene, compartmentsDatabaseIDs, statement, primaryCompartment, scorePrimaryCompartment, secondaryCompartments);

					this.processCompartments.initProcessCompartments(compartmentsDatabaseIDs.keySet());
				}

				this.processingCounter.incrementAndGet();
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.processingCounter.get(), this.processingTotal.get());
				setChanged();
				notifyObservers();
			}
			statement.close();
			return true;
		}
		catch (SQLException e) {e.printStackTrace();}
		return false;

	}


	/**
	 * @param ignoreList
	 * @return
	 */
	/**
	 * @param ignoreList
	 * @return
	 */
	public boolean assignCompartmentsToMetabolicReactions(List<String> ignoreList) {

		Statement statement;
		try {

			statement = this.connection.createStatement();

			this.processCompartments.autoSetInteriorCompartment(statement);
			Map<Integer,String> compartmentsAbb_ids = ModelAPI.getIdCompartmentAbbMap(statement);
			Map<String,Integer> idCompartmentAbbIdMap = ModelAPI.getCompartmentAbbIdMap(statement);

			Map<String, List<String>> enzymesReactions = CompartmentsAPI.getEnzymesReactions2(statement);

			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			Map<String, List<Integer>> enzymesCompartments = CompartmentsAPI.getEnzymesCompartments(statement);

			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			Map<String, DatabaseReactionContainer> reactionsMap = DatabaseLoaders.getEnzymesReactionsMap(statement, false);

			this.processingTotal.set(this.processingTotal.get()+enzymesReactions.size());

			for(String ecnumber : enzymesReactions.keySet()) {

				for(String idReaction: enzymesReactions.get(ecnumber)) {

					DatabaseReactionContainer reaction = new DatabaseReactionContainer(reactionsMap.get(idReaction));

					if(enzymesCompartments.containsKey(ecnumber)) {

						Set<Integer> parsedCompartments = this.processCompartments.parseCompartments(enzymesCompartments.get(ecnumber), compartmentsAbb_ids,idCompartmentAbbIdMap, ignoreList);

						boolean inModelFromCompartment =  reaction.isInModel();

						//all compartments are assigned to the enzyme
						for(int idCompartment: parsedCompartments) {

							if(idCompartment>0) {

								if(this.processCompartments.getIgnoreCompartmentsID().contains(idCompartment))
									inModelFromCompartment = false;

								DatabaseLoaders.loadReaction(idCompartment, inModelFromCompartment, reaction, ecnumber, statement, false, this.connection.getDatabaseType());
							}
						}
					}
					else {

						int idCompartment = idCompartmentAbbIdMap.get(this.processCompartments.getInteriorCompartment().toLowerCase());

						//reactions are not in model because these are associated with enzymes not in model
						DatabaseLoaders.loadReaction(idCompartment, false, reaction, ecnumber, statement, false, this.connection.getDatabaseType());
					}
				}

				this.processingCounter.incrementAndGet();
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.processingCounter.get(), this.processingTotal.get());
				setChanged();
				notifyObservers();
			}

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			//if no enzyme is assigned to the reaction

			List<String> reactionsIDs = CompartmentsAPI.getReactionID(statement);

			this.processingTotal.set(this.processingTotal.get()+reactionsIDs.size());

			for(String idReaction: reactionsIDs) {

				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				//				ArrayList<String[]> result = HomologyAPI.getEnzymeProteinID(idReaction, statement);
				//
				//				Map<Integer,String> proteinID = new HashMap<Integer, String>();
				//				Map<Integer,String> ecNumber = new HashMap<Integer, String>();
				//
				//				for(int i=0; i<result.size(); i++){
				//					String[] list = result.get(i);
				//
				//					proteinID.put(i, list[0]);
				//					ecNumber.put(i, list[1]);
				//				}
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				//List<String> pathwayID = ProjectAPI.getPathwaysIDsByReactionID(idReaction, statement); ---> never used

				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

				int idCompartment = idCompartmentAbbIdMap.get(this.processCompartments.getInteriorCompartment().toLowerCase());

				DatabaseLoaders.loadReaction(idCompartment, reactionsMap.get(idReaction).isInModel(), reactionsMap.get(idReaction), null, statement, false, this.connection.getDatabaseType());

				this.processingCounter.incrementAndGet();
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.processingCounter.get(), this.processingTotal.get());
				setChanged();
				notifyObservers();

			}
			statement.close();
			return true;
		}
		catch (Exception e) { 

			e.printStackTrace();
		}

		return false;
	}


	/**
	 * @param ignoreList
	 * @return
	 * @throws Exception 
	 */
	public boolean assignCompartmentsToTransportReactions(List<String> ignoreList) throws Exception {

		Statement statement = this.connection.createStatement();

		this.processCompartments.autoSetInteriorCompartment(connection);

		Map<Integer,String> idCompartmentMap = ModelAPI.getIdCompartmentAbbMap(statement);

		List<String> reactionReversibility = TransportersAPI.getReactionReversibility(statement);

		Map<String,Integer> idCompartmentAbbIdMap = ModelAPI.getCompartmentAbbIdMap(statement);

		Map<String, List<String>> transportProteins_reactions = new HashMap<String, List<String>>();
		List<String> reactions_ids;

		//TODO MAKE this a static method on database loaders

		Map<String, DatabaseReactionContainer> reactionsMap = TransportersAPI.getDataFromReactionForTransp(statement);

		ArrayList<String[]> result = TransportersAPI.getTransportReactions(statement);

		for(int i=0; i<result.size(); i++){
			String[] list = result.get(i);

			String key = list[1].concat("_").concat(list[2]);
			reactions_ids = new ArrayList<String>();

			if(transportProteins_reactions.containsKey(key))
				reactions_ids = transportProteins_reactions.get(key);	

			reactions_ids.add(list[0]);
			transportProteins_reactions.put(key,reactions_ids);

			reactionsMap.get(list[0]).addProteins(list[2], list[1]);
		}

		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		Map<String, Set<Integer>> transportProteinsCompartments = CompartmentsAPI.getTransportProteinsCompartments(statement);

		result = ProjectAPI.getReactionIdAndPathwayID(statement);

		for(int i=0; i<result.size(); i++){
			String[] list = result.get(i);
			reactionsMap.get(list[0]).getPathways().add(list[1]);
		}

		result = TransportersAPI.getAllFromStoichiometry(statement);

		for(int i=0; i<result.size(); i++){
			String[] list = result.get(i);
			reactionsMap.get(list[1]).addEntry(Integer.parseInt(list[2]), list[4], list[5], Integer.parseInt(list[3]));
		}

		for(String transporter : transportProteins_reactions.keySet()) {

			for(String idReaction: transportProteins_reactions.get(transporter)) {
				
				DatabaseReactionContainer transportReaction = new DatabaseReactionContainer(reactionsMap.get(idReaction));

				Set<Integer> tpcCompartments = new HashSet<>();
				int tpcCompartmentsSize = 1;

				if(transportProteinsCompartments.containsKey(transporter)) {
					
					tpcCompartments = transportProteinsCompartments.get(transporter);
					tpcCompartmentsSize = transportProteinsCompartments.get(transporter).size();

				}
				else 
					tpcCompartments.add(idCompartmentAbbIdMap.get(TransportersUtilities.DEFAULT_MEMBRANE));


				String originalEquation = transportReaction.getEquation();
				
				List<Integer> originalIDCompartments = transportReaction.getCompartment_idcompartment();

				for(int idCompartment: tpcCompartments) {

					String abb = idCompartmentMap.get(idCompartment);
					
//					String newAbb = abb;
					
//					if(abb.toLowerCase().contains("me"))
//						newAbb = TransportersUtilities.getOutsideMembrane(abb.toLowerCase(),  this.processCompartments.getStain());
					
					if(reactionReversibility.contains(idReaction) && abb.equalsIgnoreCase("extr")) {
						
						if(this.processCompartments.getInteriorCompartment().equalsIgnoreCase("cyto"))
							abb = "PLAS";
						else
							abb = "outme";
					}
					
					if(abb.toLowerCase().contains("me") || abb.toLowerCase().contains("pla")) {
						
						boolean inModelFromCompartment = transportReaction.isInModel();

						if(ignoreList.contains(abb.toLowerCase())) 
							inModelFromCompartment = false;

						String equation = originalEquation.replace("(out)","("+this.processCompartments.processTransportCompartments("out", abb )+")").replace("(in)",
								"("+this.processCompartments.processTransportCompartments("in", abb )+")");
						
						transportReaction.setEquation(equation);

						//////////////////////////////////////////////////////////////////

						List<Integer> newIDCompartments = new ArrayList<>();
						for(int j = 0 ; j < originalIDCompartments.size(); j++ ) {

							int metaboliteCompartmentID = originalIDCompartments.get(j);

							String compartment = this.processCompartments.processTransportCompartments(idCompartmentMap.get(metaboliteCompartmentID), abb );						

							if(!idCompartmentAbbIdMap.containsKey(compartment.toLowerCase())) {

								String query = "INSERT INTO compartment (name, abbreviation) VALUES('"+compartment+"','"+compartment+"')";

								idCompartmentAbbIdMap.put(compartment.toLowerCase(), ProjectAPI.executeAndGetLastInsertID(query, statement));
							}
							metaboliteCompartmentID = idCompartmentAbbIdMap.get(compartment.toLowerCase());
							newIDCompartments.add(j, metaboliteCompartmentID);
						}

						transportReaction.setCompartment_idcompartment(newIDCompartments);
						//////////////////////////////////////////////////////////////////
						DatabaseLoaders.loadReaction(idCompartment, inModelFromCompartment, transportReaction, null, statement, true, this.connection.getDatabaseType());
					}
					else if(abb.equalsIgnoreCase("cytop") || tpcCompartmentsSize==1){
						
						String newAbb = TransportersUtilities.DEFAULT_MEMBRANE;

						boolean inModel = false;

						String equation = originalEquation.replace("(out)","("+this.processCompartments.processTransportCompartments("out", newAbb )+")").replace("(in)",
								"("+this.processCompartments.processTransportCompartments("in", newAbb )+")");

						transportReaction.setEquation(equation);

						//////////////////////////////////////////////////////////////////

						List<Integer> newIDCompartments = new ArrayList<>();
						for(int j = 0 ; j < transportReaction.getCompound_idcompounds().size(); j++ ) {

							int metaboliteCompartmentID = transportReaction.getCompartment_idcompartment().get(j);
							String compartment = this.processCompartments.processTransportCompartments(idCompartmentMap.get(metaboliteCompartmentID), newAbb );

							if(!idCompartmentAbbIdMap.containsKey(compartment.toLowerCase())) {

								String query = "INSERT INTO compartment (name, abbreviation) VALUES('"+compartment+"','"+compartment+"')";

								idCompartmentAbbIdMap.put(compartment.toLowerCase(), ProjectAPI.executeAndGetLastInsertID(query, statement));
							}
							metaboliteCompartmentID = idCompartmentAbbIdMap.get(compartment.toLowerCase());
							newIDCompartments.add(j, metaboliteCompartmentID);

						}

						transportReaction.setCompartment_idcompartment(newIDCompartments);

						//////////////////////////////////////////////////////////////////
						DatabaseLoaders.loadReaction(idCompartment, inModel, transportReaction, null, statement, true, this.connection.getDatabaseType());

						//							logger.debug("Transporter compartment {}",abb);
					}
				}
			}
		}

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		List<String> reactionsIDs = CompartmentsAPI.getTransportReactionID(statement);

		int idCompartment = idCompartmentAbbIdMap.get(TransportersUtilities.DEFAULT_MEMBRANE.toString());

		for(String idReaction: reactionsIDs) {
			
			DatabaseReactionContainer transportReaction = new DatabaseReactionContainer(reactionsMap.get(idReaction));

			String originalEquation = transportReaction.getEquation();

			String newAbb = TransportersUtilities.DEFAULT_MEMBRANE;

			boolean inModel = false;

			String equation = originalEquation.replace("(out)","("+this.processCompartments.processTransportCompartments("out", newAbb )+")").replace("(in)",
					"("+this.processCompartments.processTransportCompartments("in", newAbb )+")");

			transportReaction.setEquation(equation);

			//////////////////////////////////////////////////////////////////

			List<Integer> newIDCompartments = new ArrayList<>();
			for(int j = 0 ; j < transportReaction.getCompound_idcompounds().size(); j++ ) {

				int metaboliteCompartmentID = transportReaction.getCompartment_idcompartment().get(j);

				String compartment = this.processCompartments.processTransportCompartments(idCompartmentMap.get(metaboliteCompartmentID), newAbb );

				if(!idCompartmentAbbIdMap.containsKey(compartment.toLowerCase())) {

					String query = "INSERT INTO compartment (name, abbreviation) VALUES('"+compartment+"','"+compartment+"')";

					idCompartmentAbbIdMap.put(compartment.toLowerCase(), ProjectAPI.executeAndGetLastInsertID(query, statement));
				}
				metaboliteCompartmentID = idCompartmentAbbIdMap.get(compartment.toLowerCase());
				newIDCompartments.add(j, metaboliteCompartmentID);

			}

			transportReaction.setCompartment_idcompartment(newIDCompartments);

			//////////////////////////////////////////////////////////////////
			DatabaseLoaders.loadReaction(idCompartment, inModel, transportReaction, null, statement, true, this.connection.getDatabaseType());

			//				logger.debug("Transporter compartment {}",abb);

		}
		statement.close();
		return true;
	}

	//	/**
	//	 * @param idprotein
	//	 * @param tcNumber
	//	 * @param idReaction
	//	 * @return
	//	 * @throws SQLException
	//	 */
	//	private boolean addReaction_has_Enzyme(String idprotein, String ecNumber, String idReaction) throws SQLException {
	//
	//		Connection conn = this.connection;
	//		Statement stmt = conn.createStatement();
	//		ResultSet rs = stmt.executeQuery("SELECT * FROM reaction_has_enzyme " +
	//				"WHERE reaction_idreaction = "+idReaction+" " +
	//				"AND enzyme_protein_idprotein = "+idprotein+" " +
	//				"AND enzyme_ecnumber = '"+ecNumber+"';");
	//
	//		if(rs.next())
	//			return false;
	//		else
	//			stmt.execute("INSERT INTO reaction_has_enzyme (reaction_idreaction, enzyme_protein_idprotein, enzyme_ecnumber) " +
	//					"VALUES("+idReaction+","+idprotein+",'"+ecNumber+"');");
	//		
	//		stmt.close();
	//
	//		return true;
	//	}
	//
	//
	//	/**
	//	 * @param idpathway
	//	 * @param idreaction
	//	 * @return
	//	 * @throws SQLException
	//	 */
	//	private boolean addPathway_has_Reaction(String idPathway, String idReaction) throws SQLException {
	//
	//		Connection conn = this.connection;
	//		Statement stmt = conn.createStatement();
	//		ResultSet rs = stmt.executeQuery("SELECT * FROM pathway_has_reaction " +
	//				"WHERE reaction_idreaction = '"+idReaction+"' " +
	//				"AND pathway_idpathway = '"+idPathway+"';");
	//
	//		if(rs.next())
	//			return false;
	//		else
	//
	//			stmt.execute("INSERT INTO pathway_has_reaction (reaction_idreaction, pathway_idpathway) " +
	//					"VALUES("+idReaction+","+idPathway+");");
	//		
	//		rs.close();
	//		stmt.close();
	//
	//		return true;
	//	}

	/**
	 * @param processingTotal
	 */
	public void setQuerySize(AtomicInteger querySize) {

		this.processingTotal = querySize; 		
	}

	public void setCancel(AtomicBoolean cancel) {

		this.cancel = cancel;
	}

	public void cancel() {

		this.cancel.set(true);
	}

	/**
	 * @return the processingCounter
	 */
	public AtomicInteger getProcessingCounter() {
		return processingCounter;
	}

	/**
	 * @param processingCounter the processingCounter to set
	 */
	public void setProcessingCounter(AtomicInteger processingCounter) {
		this.processingCounter = processingCounter;
	}

	/**
	 * @param progress
	 */
	public void setTimeLeftProgress(TimeLeftProgress progress) {

		this.progress = progress;
	}

}
