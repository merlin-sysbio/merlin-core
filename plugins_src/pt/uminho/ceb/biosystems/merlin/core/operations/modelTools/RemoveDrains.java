package pt.uminho.ceb.biosystems.merlin.core.operations.modelTools;
import java.sql.Statement;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactionsInterface;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;

@Operation(description="remove drains for external metabolites",name="remove drains")
public class RemoveDrains {
	private static final Logger logger = LoggerFactory.getLogger(RemoveDrains.class);
	private ReactionsInterface reaction;
	private Project project;
	/**
	 * @param project
	 */
	@Port(name="workspace",description="select workspace",direction=Direction.INPUT,order=1, validateMethod="checkProject")
	public void setProject(Project project) {
		
		try {  
			
			Connection  connection = new Connection(this.project.getDatabase().getDatabaseAccess());
			Statement stmt = connection.createStatement();
//			Container cont = new Container(new MerlinDBReader(project.getDatabase().getDatabaseAccess(),
//					this.project.getName(), project.isCompartmentalisedModel(),
//					this.project.getOrganismName(),
//					null));
//			int drains_counter = this.removeDrains(cont, connection.getDatabaseType(), stmt);
			//          if(project.isCompartmentalisedModel()) {
				//
			//              cont = new Container(new MerlinDBReader(project.getDatabase().getDatabaseAccess(),
			//                      this.project.getName(), !project.isCompartmentalisedModel(),
			//                      this.project.getOrganismName(),
			//                      null));
			//              drains_counter += this.removeDrains(cont, connection.getDatabaseType(), stmt);
			//          }
			
			Set<Integer> drains = ModelAPI.getModelDrains(stmt);
			int drains_counter = drains.size();
			this.removeDrains(drains, stmt);
			stmt.close();
			reaction.setNewGaps(false);
			MerlinUtils.updateReactionsView(project.getName());
			Workbench.getInstance().info(drains_counter+" drains removed.");
		}
		catch (Exception e) {

			e.printStackTrace();
		}
	}

	//    /**
	//     * @param cont
	//     * @param databaseType
	//     * @param stmt
	//     */
	//    private int removeDrains(Container cont, DatabaseType databaseType, Statement stmt ) {
	//        try {
	//            cont.verifyDepBetweenClass();
	//            Set<String> drains = cont.getDrains();
	//
	//            if(drains.size()>0) {
	//            	
	//                logger.info("{} drains will be removed!", drains.size());
	//                for(String drain : drains) {
	//                    String drainID = null;
	//                    ReactionCI reactionCI = cont.getReaction(drain);
	//                    drainID = reactionCI.getName();
	//                    String name =  DatabaseUtilities.databaseStrConverter(drainID,databaseType).split("__")[0];
	//                    int reactionID = ProjectAPI.getReactionIdByName(name, stmt);
	//                    ModelAPI.removeSelectedReaction(stmt, reactionID);
	//                }
	//            }
	//            return drains.size();
	//        }
	//        catch (Exception e) {
	//            e.printStackTrace();
	//            Workbench.getInstance().error(e.getMessage());
	//        }
	//        return 0;
	//    }

	/**
	 * @param drain
	 * @param stmt
	 * @return
	 */
	private int removeDrains(Set<Integer> drains, Statement stmt ) {

		try {

			if(drains.size()>0) {

				logger.info("{} drains will be removed!", drains.size());
				for(int drain : drains) {
					ModelAPI.removeSelectedReaction(stmt, drain);
				}
			}
			return drains.size();
		}
		catch (Exception e) {
			e.printStackTrace();
			Workbench.getInstance().error(e.getMessage());
		}
		return 0;
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
				}
			}
//			if(this.reaction.getActiveReactions() == null)
//				throw new IllegalArgumentException("Reactions view unavailable! Please open the reactions viewer!");
		}
	}
}
