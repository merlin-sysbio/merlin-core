/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.datatypes;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.CompartmentsAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.TransportersAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.transporters.core.transport.reactions.containerAssembly.ProteinFamiliesSet;
import pt.uminho.ceb.biosystems.merlin.transporters.core.transport.reactions.containerAssembly.TransportContainer;
import pt.uminho.ceb.biosystems.merlin.transporters.core.transport.reactions.containerAssembly.TransportReactionCI;
import pt.uminho.ceb.biosystems.merlin.transporters.core.utils.TransportersUtilities;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;
import pt.uminho.ceb.biosystems.merlin.utilities.External.ExternalRefSource;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.ContainerUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;

/**
 * @author ODias
 *
 */
public class IntegrateTransportersData implements IntegrateData {

	private static final Logger logger = LoggerFactory.getLogger(IntegrateTransportersData.class);
	private Connection connection;
	private TransportContainer transportContainer;
	private Map<String,String> metabolites_ids;
	private Map<String,ProteinFamiliesSet> genes_protein_ids;
	private AtomicBoolean cancel;
	private TimeLeftProgress progress;
	private long startTime;
	private List<String> metabolitesInModel;
	private int compartmentID;

	/**
	 * @param project
	 */
	public IntegrateTransportersData (Project project) {

		try {

			this.compartmentID = -1;
			this.connection = new Connection(project.getDatabase().getDatabaseAccess());
			this.transportContainer = project.getTransportContainer();
			this.metabolites_ids = new HashMap<String, String>();
			this.getMetabolitesInModel();
			this.cancel = new AtomicBoolean(false);
			this.genes_protein_ids = project.getTransportContainer().getGenesProteins();
			this.startTime = GregorianCalendar.getInstance().getTimeInMillis();
		} 
		catch (SQLException e) {

			e.printStackTrace();
		}
	}

	/**
	 * @return
	 */
	private void getMetabolitesInModel() {

		this.metabolitesInModel = new ArrayList<String>();
		Statement statement;

		try {

			statement = this.connection.createStatement();
			
			List<String> data = ModelAPI.getMetabolitesInModel(statement);

			for(int i=0; i<data.size(); i++)
				this.metabolitesInModel.add(data.get(i));	

			if (this.compartmentID<0) {

				//stmt.execute("LOCK TABLES compartment WRITE");
				
				int compID = CompartmentsAPI.getCompartmentID("inside", statement);

				if(compID<0) {

					String query = "INSERT INTO compartment (name, abbreviation) VALUES('inside','in')";
					
					compID = ProjectAPI.executeAndGetLastInsertID(query, statement);
				}
				this.compartmentID = compID;
				//stmt.execute("UNLOCK TABLES");
			}

		} catch (SQLException e) {

			e.printStackTrace();
		}

	}

	/**
	 * @return
	 */
	public boolean performIntegration() {

		int counter = 0;

		Statement statement;
		try {

			statement = this.connection.createStatement();

			Map<String, String> compartments_ids = new HashMap<>();
			Map<String, String> geneDatabaseIDs = null;

			for(String reactionID :this.transportContainer.getTransportReactions().keySet()) {

				if(this.cancel.get()) {

					counter = this.transportContainer.getTransportReactions().keySet().size();
					break;
				}
				else {

					TransportReactionCI reaction = this.transportContainer.getTransportReactions().get(reactionID);
					boolean isReversible = reaction.isReversible();

					try  {

						String idPathway = TransportersAPI.addPathway("Transporters pathway", statement);

						if(TransportersUtilities.areAllMetabolitesKEGG(reactionID, this.transportContainer)) {

							Map<String, StoichiometryValueCI> reactants = reaction.getReactants();
							Map<String, StoichiometryValueCI> products = reaction.getProducts();
							boolean reactionInModel = true;

							boolean go = true;

							for(String metabolite : reactants.keySet()) {

								if(!this.metabolites_ids.containsKey(metabolite)) {

									String idcompound = TransportersAPI.getCompoundID(ExternalRefSource.KEGG_CPD.getSourceId(this.transportContainer.getKeggMiriam().get(metabolite)), statement);

									if(idcompound == null)
										go = false;
									else
										this.metabolites_ids.put(metabolite, idcompound);
								}

								if(this.metabolites_ids.containsKey(metabolite) && !this.metabolitesInModel.contains(this.metabolites_ids.get(metabolite)))
									reactionInModel = false;
							}

							if(go) {

								for(String metabolite : products.keySet()) {

									if(!this.metabolites_ids.containsKey(metabolite)) {

										String idcompound = TransportersAPI.getCompoundID(ExternalRefSource.KEGG_CPD.getSourceId(this.transportContainer.getKeggMiriam().get(metabolite)), statement);

										if(idcompound == null)
											go = false;
										else
											this.metabolites_ids.put(metabolite, idcompound);
									}

									if(this.metabolites_ids.containsKey(metabolite) && !this.metabolitesInModel.contains(this.metabolites_ids.get(metabolite)))
										reactionInModel = false;
								}
							}

							if(go) {

								String equation="";
								for(String key :reactants.keySet())
									equation=equation.concat(reactants.get(key).getStoichiometryValue()+" "+transportContainer.getMetabolites().get(reactants.get(key).getMetaboliteId()).getName()+" ("+reactants.get(key).getCompartmentId())+") + ";

								equation=equation.substring(0, equation.lastIndexOf("+")-1);

								if(transportContainer.getReactions().get(reaction.getId()).isReversible())
									equation=equation.concat(" <=> ");
								else
									equation=equation.concat(" => ");

								for(String key :products.keySet())
									equation=equation.concat(products.get(key).getStoichiometryValue()+" "+transportContainer.getMetabolites().get(products.get(key).getMetaboliteId()).getName()+" ("+products.get(key).getCompartmentId())+") + ";

								equation=equation.substring(0, equation.lastIndexOf("+")-1);

								boolean ontology = true;
								for(String gene : reaction.getIsOriginalReaction_byGene().keySet())
									if(reaction.getIsOriginalReaction_byGene().get(gene))
										ontology = false;

								String idReaction = TransportersAPI.addReactionID(reactionID, equation, compartmentID, isReversible, ontology, reactionInModel, this.connection.getDatabaseType(), statement);

								TransportersAPI.addPathway_has_Reaction(idPathway, idReaction, statement);

								for(String metabolite : reactants.keySet()) {

									String compartment;
									if (reactants.get(metabolite).getCompartmentId().equalsIgnoreCase("in"))
										compartment = "inside";
									else
										compartment = "outside";

									String idCompartment = TransportersAPI.getCompartmentsID(compartment, compartments_ids, statement);

									double reactants_stoichiometry = -1*reactants.get(metabolite).getStoichiometryValue();
									TransportersAPI.addStoichiometry(idReaction, this.metabolites_ids.get(metabolite), idCompartment, reactants_stoichiometry, statement);
								}

								for(String metabolite : products.keySet()) {

									String compartment;
									if (products.get(metabolite).getCompartmentId().equalsIgnoreCase("in"))
										compartment = "inside";
									else
										compartment = "outside";

									String idCompartment = TransportersAPI.getCompartmentsID(compartment, compartments_ids, statement);

									TransportersAPI.addStoichiometry(idReaction, this.metabolites_ids.get(metabolite), idCompartment, products.get(metabolite).getStoichiometryValue(), statement);
								}

								for(String sequence_id : reaction.getGenesIDs()) {

									geneDatabaseIDs = TransportersAPI.getGenesDatabaseIDs(sequence_id, geneDatabaseIDs, statement);
									String idDatabaseGene = geneDatabaseIDs.get(sequence_id);
									
									if(idDatabaseGene != null) {

										Set<String> tcNumbers = new HashSet<String>();

										this.genes_protein_ids.get(sequence_id).calculateTCfamily_score();

										if(this.genes_protein_ids.get(sequence_id).getTc_families_above_half() == null || this.genes_protein_ids.get(sequence_id).getTc_families_above_half().isEmpty())
											tcNumbers.add(this.genes_protein_ids.get(sequence_id).getMax_score_family());
										else
											tcNumbers = this.genes_protein_ids.get(sequence_id).getTc_families_above_half().keySet();

										for(String tcNumber : tcNumbers)  {

											if(reaction.getProteinIds().contains(tcNumber)) {

												String idProtein = TransportersAPI.addProteinIDs(tcNumber, reactionID, statement);

												TransportersAPI.addSubunit(idProtein, tcNumber, idDatabaseGene, statement);

												TransportersAPI.addReaction_has_Enzyme(idProtein, tcNumber, idReaction, statement);

												TransportersAPI.addPathway_has_Enzyme(idProtein, tcNumber, idPathway, statement);
											}
										}
									}
								}
							}
							else {

								logger.debug("Could not integrate reaction {} {}",reactionID, ContainerUtils.getReactionToString(reaction));
							}
						}
					} 
					catch (Exception e) { 

						e.printStackTrace();
					}
				}
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime),counter,this.transportContainer.getReactions().size());
				counter++;
			}
			statement.close();
			return true;

		}
		catch (SQLException e1) {

			e1.printStackTrace();
			return false;
		}
	}



	/**
	 * 
	 */
	public void setCancel() {

		this.cancel = new AtomicBoolean(true);
	}

	/**
	 * @param progress
	 */
	public void setTimeLeftProgress(TimeLeftProgress progress) {

		this.progress = progress;
	}


	/**
	 * @return
	 */
	public AtomicBoolean isCancel() {

		return this.cancel;
	}

}
