package pt.uminho.ceb.biosystems.merlin.core.operations.modelTools;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.readers.ContainerBuilder;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactionsInterface;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.InformationType;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.CompartmentsAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseUtilities;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.CompartmentCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;

@Operation(description="create drains for external metabolites",name="create drains")
public class CreateDrains {

	private static final Logger logger = LoggerFactory.getLogger(CreateDrains.class);

	private ReactionsInterface reaction;
	private Project project;
	private String outsideName;

	@Port(name="Workspace",description="Select Workspace",direction=Direction.INPUT,order=1, validateMethod="checkProject")
	public void setProject(Project project) {

	}

	@Port(name="External compartment", description="Set External compartment", direction=Direction.INPUT, defaultValue="auto", order=2, validateMethod="checkCompartment")
	public void setExternalCompartment(String compartment) {

		Set<String> drains = new HashSet<>(), drainsTemp = new HashSet<>();

		try {

			Connection 	connection = new Connection(project.getDatabase().getDatabaseAccess());
			Statement stmt = connection.createStatement();

			Container cont = new Container(new ContainerBuilder(project.getDatabase().getDatabaseAccess(),
					this.project.getName(), project.isCompartmentalisedModel(), this.project.getOrganismName(),
					null));
			
			cont.verifyDepBetweenClass();
			String compartmentID = null;
			
			for(CompartmentCI compCI : cont.getCompartments().values()){
				
				if(compCI.getName().equalsIgnoreCase(outsideName))
					compartmentID = compCI.getId();
			}

			if(compartmentID != null){
				Set<String> existingDrains = new HashSet<>(cont.getDrains());
				cont.clearInfoElements();
				cont.constructDrains(cont.getCompartment(compartmentID).getMetabolitesInCompartmentID(), compartmentID);
				drains = cont.getDrains();
				drainsTemp.addAll(drains);

				for(String drainTemp : drainsTemp)
					for(String exDrain : existingDrains)				
						if(cont.getReaction(exDrain).hasSameStoichiometry(cont.getReaction(drainTemp), true))
							drains.remove(drainTemp);

				Map<String, Set<String>> pathway  = new HashMap<>();
				
				if(drains.size()>0) {

					String name = ModelAPI.getDrainsPathway(stmt);

					if(name==null) {
						
						String query = "INSERT INTO pathway (code, name) VALUES ('D0001','Drains pathway');";
						ProjectAPI.executeQuery(query, stmt);
						name = ModelAPI.getDrainsPathway(stmt);
					}
					pathway.put(name, new HashSet<String>());
				}

				int drains_counter = 0;
				
				for(String drain : drains) {

					ReactionCI reactionCI = cont.getReaction(drain);
					
					String drainID = null;
					String reactant = null, reactantID = null;
					String product = null, productID = null;
					long lowerBound = 0, upperBound = 0;

					for(String metaid : reactionCI.getReactants().keySet()) {

						reactant = cont.getMetabolitesExtraInfo().get(metaid).get("MERLIN_ID");
						reactantID = metaid;
						drainID = cont.getMetabolite(metaid).getName();
					}

					for(String metaid : reactionCI.getProducts().keySet()) {

						product = cont.getMetabolitesExtraInfo().get(metaid).get("MERLIN_ID");
						productID = metaid;
						if(drainID==null)
							drainID = cont.getMetabolite(metaid).getName();
					}

					String prefix = "R_EX_";
					
					drainID = prefix.concat(drainID);
					
					String name = DatabaseUtilities.databaseStrConverter(drainID,connection.getDatabaseType());

					int reactionID = ProjectAPI.getReactionIdByName(name, stmt);
					
					if(reactionID<0) {

						drains_counter ++;

						Map<String, String> compartments  = new HashMap<>(), metabolites = new HashMap<String, String>(), chains= new HashMap<String, String>();

						if(reactant!=null) {

							compartments.put(reactant, cont.getCompartment(reactionCI.getReactants().get(reactantID).getCompartmentId()).getName());
							metabolites.put(reactant, "-"+reactionCI.getReactants().get(reactantID).getStoichiometryValue());
							chains.put(reactant, "0");
							lowerBound = 0;
							upperBound = 999999;
						}

						if(product!=null) {

							compartments.put(product, cont.getCompartment(reactionCI.getReactants().get(productID).getCompartmentId()).getName());
							metabolites.put(product, ""+reactionCI.getReactants().get(productID).getStoichiometryValue());
							chains.put(product, "0");

							lowerBound = -999999;
							upperBound = 0;

							logger.debug("drain product not null, verify bounds.");
						}
						
						reaction.insertNewReaction(drainID, reactionCI.getName(), false, 
								chains, compartments, metabolites, true, pathway, outsideName, false, false, false,
								lowerBound, upperBound, InformationType.DRAINS.toString(),null);
					}
				}

				stmt.close();
				reaction.setNewGaps(false);
				MerlinUtils.updateReactionsView(project.getName());
				Workbench.getInstance().info(drains_counter+" drains added.");
			}
		}
		catch (Exception e) {

			e.printStackTrace();
			Workbench.getInstance().error(e.getMessage());
		}
	}


	/**
	 * @param compartment
	 */
	public void checkCompartment(String compartment) {

		String aux = "";
		if(!compartment.equalsIgnoreCase("auto"))
			aux = " WHERE name='"+compartment+"'";

		Connection connection;

		try {

			connection = new Connection(project.getDatabase().getDatabaseAccess());

			Statement stmt = connection.createStatement();

			ArrayList<String[]> result = CompartmentsAPI.getCompartmentDataByName(aux, stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				if(compartment.equalsIgnoreCase("auto")) {

					if((list[1].equalsIgnoreCase("extracellular") & project.isCompartmentalisedModel()) || 
							(list[1].equalsIgnoreCase("outside") && !project.isCompartmentalisedModel())) {

						outsideName = list[1];
					}
				}
				else {

					outsideName = list[1];
				}
			}
			stmt.close();

			if(outsideName==null) {

				Workbench.getInstance().warn("No external compartment defined!");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param project
	 */
	public void checkProject(Project project) {

		if(project == null) {

			throw new IllegalArgumentException("No Project Selected!");
		}
		else {

			this.project = project;

			for(Entity ent : project.getDatabase().getEntities().getEntities()){
				if(ent.getName().equalsIgnoreCase("Reactions")){
					reaction = (ReactionsInterface) ent;
//					reaction.getReactionsData(false);
				}
			}

//			if(this.reaction.getActiveReactions() == null)
//				throw new IllegalArgumentException("Reactions view unavailable! Please open the reactions viewer!");
		}
	}

}
