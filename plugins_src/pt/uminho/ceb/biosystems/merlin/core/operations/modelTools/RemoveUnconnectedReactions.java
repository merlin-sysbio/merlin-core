/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.operations.modelTools;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactionsInterface;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;

/**
 * @author ODias
 *
 */
@Operation(name="Remove unconnected reactions", description="Remove reactions with unconnected metabolites.")
public class RemoveUnconnectedReactions {


	private boolean removeReactions;
	//private boolean recursively;
	private boolean removeTransportReactions;
	//private boolean removeExtracellularReactions;
	private ReactionsInterface reaction;
	private Project project;
	private boolean removeNeighbourReactions;

	/**
	 * @param removeReactions
	 */
	@Port(name="Remove unconnected chemical reactions",//description="Remove chemical reactions with unconnected metabolites from the model.", 
			defaultValue="true", direction = Direction.INPUT, order=1)
	public void removeReactions(boolean removeReactions) {

		this.removeReactions = removeReactions;
	}

	/**
	 * @param project
	 */
	@Port(name="Remove unconnected transport reactions",//description="Remove transport reactions with unconnected metabolites from the model.", 
			defaultValue="true", direction = Direction.INPUT, order=2)
	public void removeTransportReactions(boolean removeTransportReactions){

		this.removeTransportReactions = removeTransportReactions;
	}
	
	/**
	 * @param removeReactions
	 */
	@Port(name="Remove unconnected neighbour reactions",//description="Remove chemical reactions with unconnected metabolites from the model.", 
			defaultValue="true", direction = Direction.INPUT, order=3)
	public void removeNeighboursReactions(boolean removeNeighbourReactions) {

		this.removeNeighbourReactions = removeNeighbourReactions;
	}

	//	/**
	//	 * @param project
	//	 */
	//	@Port(name="Remove reactions recursively",description="Execut method recursively until only connected reactions are avalaible",
	//			defaultValue="false", direction = Direction.INPUT, order=3)
	//	public void recursively(boolean recursively) {
	//
	//		this.recursively = recursively;
	//	}

	//	/**
	//	 * @param removeReactions
	//	 */
	//	@Port(name="Remove unconnected extracellular chemical reactions",description="Remove chemical reactions with unconnected extracellular metabolites from the model.", 
	//			defaultValue="true", direction = Direction.INPUT, order=1)
	//	public void removeExtracellularReactions(boolean removeExtracellularReactions) {
	//
	//		this.removeExtracellularReactions = removeExtracellularReactions;
	//	}

	/**
	 * @param project
	 */
	@Port(name="Select Workspace",description="Select Workspace", validateMethod="checkProject", direction = Direction.INPUT, order=4)
	public void setProject(Project project) {

		if(project.isMetabolicDataAvailable() && (this.removeReactions || this.removeTransportReactions)) {

			try {

				Connection connection = new Connection(project.getDatabase().getDatabaseAccess());
				Statement stmt = connection.createStatement();

				List<String> removed_reactions = new ArrayList<String> ();

				Set<String> transportCounter = new HashSet<String>();
				Set<String> reactionsCounter = new HashSet<String>();
				Set<String> globalTransportCounter = new HashSet<String>();
				Set<String> globalReactionsCounter = new HashSet<String>();
				Set<String> globalcounter = new HashSet<String>();
				//Set<String> removedReactions = new HashSet<String>();

				boolean go = false;

				for(String reaction : this.reaction.getGapReactions().getReactions().keySet()) {

					if(removeTransportReactions && reaction.startsWith("T"))
						go = true;

					if(removeReactions && reaction.startsWith("R") && !reaction.toLowerCase().contains("biomass"))
						go = true;

					if(go) {

						globalcounter.add(reaction);

						if(reaction.startsWith("T")) {

							transportCounter.add(reaction);
							globalTransportCounter.add(reaction);
						}
						else {

							reactionsCounter.add(reaction);
							globalReactionsCounter.add(reaction);
						}

						String query = "UPDATE reaction SET inModel = false WHERE idreaction ="
						+this.reaction.getGapReactions().getAllReactions().get(reaction);
						
						ProjectAPI.executeQuery(query, stmt);
						go = false;
						removed_reactions.add(reaction);
					}
				}

				if(removeNeighbourReactions)
					for(String reaction : this.reaction.getGapReactions().getNeighbourReactions()) {

						if(removeTransportReactions && reaction.startsWith("T"))
							go = true;

						if(removeReactions && reaction.startsWith("R") && !reaction.toLowerCase().contains("biomass"))
							go = true;

						if(go) {

							globalcounter.add(reaction);

							if(reaction.startsWith("T")) {

								transportCounter.add(reaction);
								globalTransportCounter.add(reaction);
							}
							else {

								reactionsCounter.add(reaction);
								globalReactionsCounter.add(reaction);
							}

							String query = "UPDATE reaction SET inModel = false WHERE idreaction = "
							+this.reaction.getGapReactions().getAllReactions().get(reaction);
							
							ProjectAPI.executeQuery(query, stmt);
							go = false;
							removed_reactions.add(reaction);
						}
					}

				reaction.setNewGaps(false);
				MerlinUtils.updateReactionsView(project.getName());

				Workbench.getInstance().info(globalReactionsCounter.size()+" chemical reactions removed!\n"+
						globalTransportCounter.size()+" transport reactions removed!\n"
						+globalcounter.size()+" reactions removed!");
			}
			catch (SQLException e) {
				
				e.printStackTrace();
				Workbench.getInstance().info("An error occurred while performing the operation.");
			}
		}
		else {

			if(!this.removeReactions && !this.removeTransportReactions && !this.removeNeighbourReactions)
				Workbench.getInstance().error(new Exception("Please select reactions to be removed!"));
			else
				Workbench.getInstance().error("Gene data for integration unavailable!");
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

			for(Entity ent : this.project.getDatabase().getEntities().getEntities())
				if(ent.getName().equalsIgnoreCase("Reactions"))
					reaction = (ReactionsInterface) ent;

			if(this.reaction.getActiveReactions() == null)
				throw new IllegalArgumentException("Reactions view unavailable!");

			if(this.reaction.getGapReactions()==null)
				throw new IllegalArgumentException("Please find unconnect reactions in the model before performing this operation!");

			if(this.reaction.getGapReactions().getReactions().isEmpty())
				throw new IllegalArgumentException("There are no gaps in this model!");
		}
	}

}
