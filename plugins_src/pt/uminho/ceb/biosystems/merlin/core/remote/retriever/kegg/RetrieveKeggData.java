/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.remote.retriever.kegg;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.kegg.KeggAPI;
import pt.uminho.ceb.biosystems.merlin.core.remote.retriever.kegg.KEGGDataRetriever.EntityType;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;
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
public class RetrieveKeggData {

	private ConcurrentLinkedQueue<MetaboliteContainer> resultMetabolites;
	private ConcurrentLinkedQueue<EnzymeContainer> resultEnzymes;
	private ConcurrentLinkedQueue<ReactionContainer> resultReactions;
	private ConcurrentLinkedQueue<GeneContainer> resultGenes;
	private ConcurrentLinkedQueue<ModuleContainer> resultModules;
	private ConcurrentLinkedQueue<PathwaysHierarchyContainer> keggPathwaysHierarchy;
	private ConcurrentLinkedQueue<String> orthologueEntities,compoundsWithBiologicalRoles;
	private AtomicBoolean cancel;
	private TimeLeftProgress progress;
	private String organismID;

	/**
	 * Retrieve Kegg data
	 * 
	 * @param organismID
	 * @param progress
	 * @param cancel
	 * @throws Exception
	 */
	public RetrieveKeggData(String organismID, TimeLeftProgress progress, AtomicBoolean cancel) throws Exception {

		this.organismID = organismID;
		this.cancel = cancel;
		this.progress = progress;
	}

	/**
	 * @param activeCompounds 
	 * @throws Exception
	 */
	public void retrieveMetabolicData(boolean activeCompounds) throws Exception {

		this.resultMetabolites = new ConcurrentLinkedQueue<MetaboliteContainer> ();
		this.resultEnzymes = new ConcurrentLinkedQueue<EnzymeContainer> ();
		this.resultReactions = new ConcurrentLinkedQueue<ReactionContainer> ();
		this.resultModules = new ConcurrentLinkedQueue<ModuleContainer> ();

		List<EntityType> data = new ArrayList<EntityType>();
		data.add(EntityType.Reaction);
		data.add(EntityType.Drugs);
		data.add(EntityType.Compound);
		data.add(EntityType.Glycan);
		data.add(EntityType.Pathways);
		data.add(EntityType.Enzyme);
		data.add(EntityType.Module);

		long startTime = System.currentTimeMillis();
		AtomicInteger datum = new AtomicInteger(0);

		int dataSize = 1 ;

		{	
			long startTime_cbr = System.currentTimeMillis();
			this.setCompoundsWithBiologicalRoles(KEGGDataRetriever.getCompoundsWithBiologicalRoles());
			long endTime_process_cbr = System.currentTimeMillis();

			this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis() - startTime), datum.incrementAndGet(), dataSize, "Get biological compounds");

			System.out.println("Total elapsed time in execution of method setCompoundsWithBiologicalRoles is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime_process_cbr-startTime_cbr),TimeUnit.MILLISECONDS.toSeconds(endTime_process_cbr-startTime_cbr) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime_process_cbr-startTime_cbr))));
		}

		//Concurrent Data structures 
		List<Thread> threads = new ArrayList<Thread>();
		int numberOfProcesses =  Runtime.getRuntime().availableProcessors()*2;
		ConcurrentHashMap<String,MetaboliteContainer> resultMetabolites=new ConcurrentHashMap<String,MetaboliteContainer>();
		ConcurrentHashMap<String,EnzymeContainer> resultEnzymes=new ConcurrentHashMap<String, EnzymeContainer>();
		ConcurrentHashMap<String,ReactionContainer> resultReactions=new ConcurrentHashMap<String, ReactionContainer>();
		ConcurrentHashMap<String,ModuleContainer> resultModules=new ConcurrentHashMap<String, ModuleContainer>();
		this.keggPathwaysHierarchy= new ConcurrentLinkedQueue<PathwaysHierarchyContainer>();
		this.orthologueEntities = new ConcurrentLinkedQueue<String>();
		Set<String> compoundsInReactions = null;
		

		for(EntityType entityTypeString:data) {

			long startTime_process = System.currentTimeMillis();
			ConcurrentLinkedQueue<String> entity = new ConcurrentLinkedQueue<String>();

			if(!this.cancel.get()) {

				if(entityTypeString.equals(EntityType.Compound)||entityTypeString.equals(EntityType.Drugs)||entityTypeString.equals(EntityType.Glycan)) {

					if(compoundsInReactions == null && resultReactions.size()>0)
						compoundsInReactions = RetrieveKeggData.getCompoundsInReactions(resultReactions);

					entity.addAll(KEGGDataRetriever.getEntities(KeggAPI.getInfo(entityTypeString.getEntity_Type()[0]/*+"/"+entity_Type_String.getEntity_Type()[1]+suffix+i*/), entityTypeString.getEntity_Type()[1]));

					if(activeCompounds)
						entity.retainAll(compoundsInReactions);

				}

				if(entityTypeString.equals(EntityType.Enzyme))
					entity.addAll(KEGGDataRetriever.getEntities(KeggAPI.getInfo(EntityType.Enzyme.getEntity_Type()[0]/*+"/"+EntityType.Enzyme.getEntity_Type()[1]+":"+i*/),EntityType.Enzyme.getEntity_Type()[1]));

				if(entityTypeString.equals(EntityType.Reaction))
					entity = KEGGDataRetriever.getEntities(KeggAPI.getInfo(EntityType.Reaction.getEntity_Type()[0]/*+"/"+EntityType.Reaction.getEntity_Type()[1]+":R"+i*/),EntityType.Reaction.getEntity_Type()[1]);

				if(entityTypeString.equals(EntityType.Module))
					entity = KEGGDataRetriever.getStructuralComplexModules();//entity = KEGGDataRetriever.getEntities(KeggAPI.getinfo(EntityType.Module.getEntity_Type()[0]+" "+EntityType.Module.getEntity_Type()[1]),EntityType.Module.getEntity_Type()[1]);

				if(entityTypeString.equals(EntityType.Pathways))
					this.keggPathwaysHierarchy = KEGGDataRetriever.get_Kegg_Pathways_Hierarchy();//resultPathways = KEGGDataRetriever.get_All_Kegg_Pathways();
					//this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis() - startTime), datum.incrementAndGet(), dataSize, "Get pathways");

				MetaboliteContainer  metaboliteContainer = new MetaboliteContainer("Biomass");
				metaboliteContainer.setName("Biomass");
				resultMetabolites.put("Biomass",metaboliteContainer);

				numberOfProcesses=Runtime.getRuntime().availableProcessors()*10;

				dataSize = entity.size();
				datum = new AtomicInteger(0);

				if(entity.size()>0) {
					
					for(int i=0; i<numberOfProcesses; i++) {

						Runnable keggDataRetriever = new KEGGDataRetriever(entity, organismID, entityTypeString, resultMetabolites, resultEnzymes, resultReactions, 
								resultModules, this.cancel, this.progress, startTime, dataSize, datum);
						Thread thread = new Thread(keggDataRetriever);
						threads.add(thread);
						//System.out.println("Start "+i);
						thread.start();

						try {

							Thread.sleep(1000);

						} 
						catch (InterruptedException e1){

							Thread.currentThread().interrupt();
						}
					}

					for(Thread thread :threads) {

						thread.join();
					}
				}

			}

			long endTime_process = System.currentTimeMillis();

			System.out.println("Total elapsed time in execution of method "+entityTypeString+" is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime_process-startTime_process),TimeUnit.MILLISECONDS.toSeconds(endTime_process-startTime_process) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime_process-startTime_process))));
		}
		long endTime = System.currentTimeMillis();

		System.out.println("Total elapsed time in execution of method GLOBAL is :"+ String.format("%d min, %d sec", 
				TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));

		//from maps to lists
		for(String entry : resultMetabolites.keySet())
			this.resultMetabolites.add(resultMetabolites.get(entry));

		for(String entry : resultEnzymes.keySet())
			this.resultEnzymes.add(resultEnzymes.get(entry));

		for(String entry : resultReactions.keySet())	
			this.resultReactions.add(resultReactions.get(entry));

		for(String entry : resultModules.keySet())			
			this.resultModules.add(resultModules.get(entry));

	}

	/**
	 * Retrieve Kegg data
	 * 
	 * @param organismID
	 * @param progress 
	 * @param cancel 
	 * @throws Exception
	 */
	public void retrieveOrganismData() throws Exception {

		this.resultGenes = new ConcurrentLinkedQueue<GeneContainer> ();
		this.resultEnzymes = new ConcurrentLinkedQueue<EnzymeContainer> ();
		this.resultReactions = new ConcurrentLinkedQueue<ReactionContainer> ();

		List<EntityType> data = new ArrayList<EntityType>();
		data.add(EntityType.Gene);
		data.add(EntityType.Enzyme);
//		data.add(EntityType.Reaction);

		long startTime = System.currentTimeMillis();
		AtomicInteger datum = new AtomicInteger(0);
		int dataSize = 1 ;

		for(EntityType entityTypeString:data) {

			List<Thread> threads = new ArrayList<Thread>();
			int numberOfProcesses =  Runtime.getRuntime().availableProcessors()*2;
			ConcurrentHashMap<String,GeneContainer> resultGenes = new ConcurrentHashMap<String, GeneContainer>();
			ConcurrentHashMap<String,EnzymeContainer> resultEnzymes = new ConcurrentHashMap<String, EnzymeContainer>();
			ConcurrentHashMap<String,ReactionContainer> resultReactions=new ConcurrentHashMap<String, ReactionContainer>();
			ConcurrentLinkedQueue<String> entity = new ConcurrentLinkedQueue<String>();

			if(entityTypeString.equals(EntityType.Gene))
				entity = KEGGDataRetriever.getEntities(KeggAPI.getInfo(/*EntityType.Gene.getEntity_Type()[0]+"/"+*/organismID/*+":"*/),organismID.toLowerCase());

			if(entityTypeString.equals(EntityType.Enzyme))
				entity.addAll(KEGGDataRetriever.getEntities(KeggAPI.getInfo(EntityType.Enzyme.getEntity_Type()[0]/*+"/"+EntityType.Enzyme.getEntity_Type()[1]+":"+i*/),EntityType.Enzyme.getEntity_Type()[1]));
			
			if(entityTypeString.equals(EntityType.Reaction))
				entity = KEGGDataRetriever.getEntities(KeggAPI.getInfo(EntityType.Reaction.getEntity_Type()[0]/*+"/"+EntityType.Reaction.getEntity_Type()[1]+":R"+i*/),EntityType.Reaction.getEntity_Type()[1]);

			numberOfProcesses=Runtime.getRuntime().availableProcessors()*10;

			dataSize = entity.size();
			datum = new AtomicInteger(0);

			for(int i=0; i<numberOfProcesses; i++) {

				Runnable keggDataRetriever = new KEGGDataRetriever(entity, organismID, entityTypeString, resultGenes, resultEnzymes, resultReactions, this.cancel, this.progress, startTime, dataSize, datum);
				Thread thread = new Thread(keggDataRetriever);
				threads.add(thread);
				//System.out.println("Start "+i);
				thread.start();

				try {

					Thread.sleep(1000);

				} 
				catch (InterruptedException e1){

					Thread.currentThread().interrupt();
				}

			}

			for(Thread thread :threads) {

				thread.join();
			}


			long endTime_process = System.currentTimeMillis();

			System.out.println("Total elapsed time in execution of method RETRIEVE is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime_process-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime_process-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime_process-startTime))));
			//from maps to lists

			if(!organismID.isEmpty())			
				for(String entry : resultGenes.keySet())
					this.resultGenes.add(resultGenes.get(entry));

			for(String entry : resultEnzymes.keySet())
				this.resultEnzymes.add(resultEnzymes.get(entry));
		}

	}

	/**
	 * @return the keggPathwaysHierarchy
	 */
	public ConcurrentLinkedQueue<PathwaysHierarchyContainer> getKegg_Pathways_Hierarchy() {

		return this.keggPathwaysHierarchy;
	}

	/**
	 * @param keggPathwaysHierarchy the keggPathwaysHierarchy to set
	 */
	public void setKegg_Pathways_Hierarchy(
			ConcurrentLinkedQueue<PathwaysHierarchyContainer> keggPathwaysHierarchy) {

		this.keggPathwaysHierarchy = keggPathwaysHierarchy;
	}

	/**
	 * @return the orthologueEntities
	 */
	public ConcurrentLinkedQueue<String> getOrthologueEntities() {

		return this.orthologueEntities;
	}

	/**
	 * @param orthologueEntities the orthologueEntities to set
	 */
	public void setOrthologueEntities(
			ConcurrentLinkedQueue<String> orthologueEntities) {

		this.orthologueEntities = orthologueEntities;
	}

	/**
	 * @param resultEnzymes the resultEnzymes to set
	 */
	public void setResultEnzymes(ConcurrentLinkedQueue<EnzymeContainer> resultEnzymes) {

		this.resultEnzymes = resultEnzymes;
	}

	/**
	 * @return the resultEnzymes
	 */
	public ConcurrentLinkedQueue<EnzymeContainer> getResultEnzymes() {

		return this.resultEnzymes;
	}

	/**
	 * @param resultReactions the resultReactions to set
	 */
	public void setResultReactions(ConcurrentLinkedQueue<ReactionContainer> resultReactions) {

		this.resultReactions = resultReactions;
	}



	/**
	 * @return the resultReactions
	 */
	public ConcurrentLinkedQueue<ReactionContainer> getResultReactions() {

		return this.resultReactions;
	}



	/**
	 * @param resultGenes the resultGenes to set
	 */
	public void setResultGenes(ConcurrentLinkedQueue<GeneContainer> resultGenes) {

		this.resultGenes = resultGenes;
	}



	/**
	 * @return the resultGenes
	 */
	public ConcurrentLinkedQueue<GeneContainer> getResultGenes() {

		return resultGenes;
	}



	/**
	 * @param resultModules the resultModules to set
	 */
	public void setResultModules(ConcurrentLinkedQueue<ModuleContainer> resultModules) {

		this.resultModules = resultModules;
	}



	/**
	 * @return the resultModules
	 */
	public ConcurrentLinkedQueue<ModuleContainer> getResultModules() {

		return resultModules;
	}



	/**
	 * @param resultMetabolites the resultMetabolites to set
	 */
	public void setResultMetabolites(ConcurrentLinkedQueue<MetaboliteContainer> resultMetabolites) {

		this.resultMetabolites = resultMetabolites;
	}



	/**
	 * @return the resultMetabolites
	 */
	public ConcurrentLinkedQueue<MetaboliteContainer> getResultMetabolites() {

		return resultMetabolites;
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
	 * Get compounds available in reactions.
	 * 
	 * @param resultReactions
	 * @return
	 */
	public static Set<String> getCompoundsInReactions(ConcurrentHashMap<String,ReactionContainer> resultReactions) {

		Set<String> compoundsInReactions = new HashSet<>();

		for(String key : resultReactions.keySet()) {

			ReactionContainer rc = resultReactions.get(key);

			for(String metabolite : rc.getReactantsStoichiometry().keySet()) {

				String prefix = "cpd:";
				if(metabolite.startsWith("D"))
					prefix = "dr:";
				else if(metabolite.startsWith("G"))
					prefix = "gl:";

				metabolite = prefix.concat(metabolite);
				if(!compoundsInReactions.contains(metabolite))
					compoundsInReactions.add(metabolite);
			}

			for(String metabolite : rc.getProductsStoichiometry().keySet()) {

				String prefix = "cpd:";
				if(metabolite.startsWith("D"))
					prefix = "dr:";
				else if(metabolite.startsWith("G"))
					prefix = "gl:";

				metabolite = prefix.concat(metabolite);
				if(!compoundsInReactions.contains(metabolite))
					compoundsInReactions.add(metabolite);
			}
		}

		return compoundsInReactions;
	}

	/**
	 * @param cancel
	 */
	public void setCancel(AtomicBoolean cancel) {

		this.cancel = cancel;
	}

}
