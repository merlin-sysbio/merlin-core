package pt.uminho.ceb.biosystems.merlin.core.operations.modelTools;

import java.io.IOException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.readers.ContainerBuilder;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.auxiliary.ReactionGapsAux;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactionsInterface;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.ContainerUtils;

/**
 * @author ODias
 *
 */
@Operation(name="Find unconnected reactions", description="Find reactions with unconnected metabolites.")
public class FindGaps {

	private Project project;
	private ReactionsInterface reaction;

	/**
	 * 
	 * @param project
	 */
	@Port(name="Workspace",description="Select Workspace",direction=Direction.INPUT,order=1, validateMethod="checkProject")
	public void setProject(Project project) {

		Container container;
		try {
			container = new Container(new ContainerBuilder(project.getDatabase().getDatabaseAccess(),
					project.getName(),project.isCompartmentalisedModel(), project.getOrganismName(), null));
			
			Set<String> compounds = new HashSet<>(), 
					neighbourReactions = new HashSet<>(), neighbourReactionsOriginal = new HashSet<>(), 
					gapIds = ContainerUtils.identyfyReactionWithDeadEnds(container);
			
			Map<String, Set<String>> reactions = new HashMap<>(),
					gapReactionsOriginal= new HashMap<String, Set<String>>(),
					gapReactions = new HashMap<String, Set<String>>();
		
			Map<String,String> idsMap = new HashMap<>();
			
			Set<String> mets = container.identifyDeadEnds(true);
		
			for(String id : mets)
				compounds.add(container.getMetabolitesExtraInfo().get(id).get("KEGG_CPD"));
			
			for(String id : gapIds)
				reactions.put(container.getReactionsExtraInfo().get(id).get("MERLIN_ID"), new HashSet<>());
			
			Connection connection = new Connection(project.getDatabase().getDatabaseAccess());
			
			Statement stmt = connection.createStatement();

			String aux = "";

			if(project.isCompartmentalisedModel())
				aux = aux.concat(" WHERE NOT originalReaction ");
			else
				aux = aux.concat(" WHERE originalReaction ");

			ArrayList<String[]> result = ModelAPI.getDataFromReaction2(aux, stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				String reaction = list[0];
				String compound = list[1];
				String id = list[2];
				
				if(reactions.containsKey(reaction))
					reactions.get(reaction).add(compound);
				
				idsMap.put(reaction, id);
			}
			stmt.close();
			connection.closeConnection();
			
			for(String reaction : reactions.keySet())
				for(String metabolite: reactions.get(reaction))
					if(compounds.contains(metabolite)) {
						
						gapReactions.put(reaction, reactions.get(reaction));
						gapReactionsOriginal.put(reaction.split("_")[0], reactions.get(reaction));
						
					}
					else {
						
						neighbourReactions.add(reaction);
						neighbourReactionsOriginal.add(reaction.split("_")[0]);
					}
			
			ReactionGapsAux rga = new ReactionGapsAux(compounds, gapReactions, neighbourReactions, idsMap);
			rga.setReactionsOriginal(gapReactionsOriginal);
			rga.setNeighbourReactionsOriginal(neighbourReactionsOriginal);
			this.reaction.setGapReactions(rga);
			
			MerlinUtils.updateReactionsView(project.getName());
			Workbench.getInstance().info(gapIds.size()+" unconnected reaction(s) found \n for "+mets.size()+" dead end metabolite(s)!");
		} 
		catch (IOException e) {

			e.printStackTrace();
		} catch (Exception e) {

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

			for(Entity ent : this.project.getDatabase().getEntities().getEntities())
				if(ent.getName().equalsIgnoreCase("Reactions"))
					reaction = (ReactionsInterface) ent;

			if(this.reaction.getActiveReactions() == null)
				throw new IllegalArgumentException("Please open the reactions viewer!");
		}
	}
}
