/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.remote.retriever.kegg;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.kegg.KeggAPI;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.kegg.KeggOperation;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.kegg.KeggRestful;
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
public class KEGGDataRetriever implements Runnable{

	final static Logger logger = LoggerFactory.getLogger(KEGGDataRetriever.class);
	
	private ConcurrentHashMap<String,MetaboliteContainer> resultMetabolites;
	private ConcurrentHashMap<String,EnzymeContainer> resultEnzymes;
	private ConcurrentHashMap<String,ReactionContainer> resultReactions;
	private ConcurrentHashMap<String,GeneContainer> resultGenes;
	private ConcurrentHashMap<String,ModuleContainer> resultModules;
	private String organismID;
	private EntityType entityTypeString;
	private ConcurrentLinkedQueue<String> entity;

	private int errorCount;
	private AtomicBoolean cancel;
	private TimeLeftProgress progress;
	private long startTime;
	private int dataSize;
	private AtomicInteger datum;

	/**
	 * Run Kegg metabolic data retriever concurrently.
	 * 
	 * @param entity
	 * @param organismID
	 * @param entityTypeString
	 * @param resultMetabolites
	 * @param resultEnzymes
	 * @param resultReactions
	 * @param resultModules
	 * @param cancel
	 * @param progress
	 * @param startTime
	 * @param dataSize
	 * @param datum
	 */
	public KEGGDataRetriever(
			ConcurrentLinkedQueue<String> entity,
			String organismID,
			EntityType entityTypeString,
			ConcurrentHashMap<String, MetaboliteContainer> resultMetabolites,
			ConcurrentHashMap<String, EnzymeContainer> resultEnzymes, 
			ConcurrentHashMap<String, ReactionContainer> resultReactions, 
			ConcurrentHashMap<String, ModuleContainer> resultModules,
			AtomicBoolean cancel, TimeLeftProgress progress, long startTime, int dataSize, AtomicInteger datum) {

		this.setEntity(entity);
		this.setEntityTypeString(entityTypeString);
		this.setOrganismID(organismID);
		this.setResultMetabolites(resultMetabolites);
		this.setResultEnzymes(resultEnzymes);
		this.setResultReactions(resultReactions);
		this.setResultModules(resultModules);
		this.progress = progress;
		this.cancel = cancel;
		this.startTime = startTime;
		this.dataSize = dataSize;
		this.datum = datum;
	}
	
	/**
	 * Constructor for retrieving genes annotation
	 * 
	 * @param organismID
	 * @param entityTypeString
	 * @param resultGenes
	 * @param resultEnzymes
	 * @param resultReactions
	 * @param cancel
	 * @param progress
	 * @param startTime
	 * @param dataSize
	 * @param datum
	 */
	public KEGGDataRetriever(
			ConcurrentLinkedQueue<String> entity,
			String organismID, 
			EntityType entityTypeString,
			ConcurrentHashMap<String, GeneContainer> resultGenes, 
			ConcurrentHashMap<String, EnzymeContainer> resultEnzymes, 
			ConcurrentHashMap<String, ReactionContainer> resultReactions,
			AtomicBoolean cancel, TimeLeftProgress progress, long startTime, int dataSize, AtomicInteger datum) {

		this.setEntity(entity);
		this.setEntityTypeString(entityTypeString);
		this.setOrganismID(organismID);
		this.setResultGenes(resultGenes);
		this.setResultEnzymes(resultEnzymes);
		this.setResultReactions(resultReactions);
		this.progress = progress;
		this.cancel = cancel;
		this.startTime = startTime;
		this.dataSize = dataSize;
		this.datum = datum;
	}


	@Override
	public void run() {

		List<String> poooledEntities=new ArrayList<String>();
		String poooledEntity;
		
		String message = "Getting " + entityTypeString;

		try  {

			if(entityTypeString.equals(EntityType.Compound) || entityTypeString.equals(EntityType.Drugs) || entityTypeString.equals(EntityType.Glycan)) {
				
				while(entity.size()>0 && !this.cancel.get()) {

					poooledEntity = entity.poll();
					poooledEntities.add(poooledEntity);
					String query = poooledEntity;
					int i=0;

					while(i<9 && entity.size()>0) {

						poooledEntity= entity.poll();
						poooledEntities.add(poooledEntity);
						query=query.concat("+"+poooledEntity);
						i++;
					}

					Map<String, MetaboliteContainer> ret = getMetabolitesArray(KeggRestful.fetch(KeggOperation.get,query));
					this.resultMetabolites.putAll(ret);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis() - startTime), datum.addAndGet(ret.size()), dataSize, message);
					
					poooledEntities=new ArrayList<String>();
				}
			}
			
			if(entityTypeString.equals(EntityType.Enzyme)) {

				while(entity.size()>0 && !this.cancel.get()) {

					poooledEntity= entity.poll();
					poooledEntities.add(poooledEntity);
					String query = poooledEntity;
					int i=0;

					while(i<9 && entity.size()>0) {

						poooledEntity= entity.poll();
						poooledEntities.add(poooledEntity);
						query=query.concat("+"+poooledEntity);
						i++;
					}

					Map<String, EnzymeContainer> ret = this.getEnzymesArray(KeggRestful.fetch(KeggOperation.get,query),organismID);
					this.resultEnzymes.putAll(ret);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis() - startTime), datum.addAndGet(ret.size()), dataSize, message);
					
					poooledEntities=new ArrayList<String>();
				}
			}

			if(entityTypeString.equals(EntityType.Reaction)) {

				while(entity.size()>0 && !this.cancel.get()) {

					poooledEntity= entity.poll(); 
					poooledEntities.add(poooledEntity);
					String query = poooledEntity;
					int i=0;

					while(i<9 && entity.size()>0) {

						poooledEntity= entity.poll();
						poooledEntities.add(poooledEntity);
						query=query.concat("+"+poooledEntity);
						i++;
					}

					Map<String, ReactionContainer> ret = this.getReactionsArray(KeggRestful.fetch(KeggOperation.get,query));
					this.resultReactions.putAll(ret);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis() - startTime), datum.addAndGet(ret.size()), dataSize, message);
					
					poooledEntities=new ArrayList<String>();
					
				}
			}

			if(entityTypeString.equals(EntityType.Gene)) {

				while(entity.size()>0 && !this.cancel.get()) {

					poooledEntity= entity.poll();
					poooledEntities.add(poooledEntity);
					String query = poooledEntity;
					int i=0;

					while(i<9 && entity.size()>0 && !this.cancel.get()) {

						poooledEntity= entity.poll();
						poooledEntities.add(poooledEntity);
						query=query.concat("+"+poooledEntity);
						i++;
						
					}
					
					Map<String, GeneContainer> ret = this.getGeneArray(KeggRestful.fetch(KeggOperation.get,query));
					this.resultGenes.putAll(ret);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis() - startTime), datum.addAndGet(ret.size()), dataSize, message);
					
					poooledEntities=new ArrayList<String>();
				}
			}

			if(entityTypeString.equals(EntityType.Module)) {

				while(entity.size()>0 && !this.cancel.get()) {

					poooledEntity= entity.poll();
					poooledEntities.add(poooledEntity);
					String query = poooledEntity;
					int i=0;

					while(i<9 && entity.size()>0 && !this.cancel.get()) {

						poooledEntity= entity.poll();
						poooledEntities.add(poooledEntity);
						query=query.concat("+"+poooledEntity);
						i++;
					}

					Map<String, ModuleContainer> ret = this.getStructuralComplexArray(KeggRestful.fetch(KeggOperation.get,query));
					this.resultModules.putAll(ret);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis() - startTime), datum.addAndGet(ret.size()), dataSize, message);
					
					poooledEntities=new ArrayList<String>();
				}
			}
		}
		catch (Exception e)  {
			
			e.printStackTrace();
			
			if(this.errorCount<10) {
				
				this.errorCount += 1;
				this.entity.addAll(poooledEntities);
				//System.err.println("\n\n\n\n\n\n\n\nTEM DE SE FAZER A REQUERY senao perdemos dados!!!!\n\n\n\n\n\n\n\n");
				logger.error("queries read {}", poooledEntities);

				try {

					Thread.sleep(60000);

				} 
				catch (InterruptedException e1){

					Thread.currentThread().interrupt();
				}

				this.run();
			}
			else {

				e.printStackTrace();
			}
		}

	}

	/**
	 * @param metabolite_Type
	 * @return
	 * @throws ServiceException
	 * @throws RemoteException

	public Map<String,MetaboliteContainer> get_All_Kegg_Metabolites(EntityType metabolite_Type) throws ServiceException, RemoteException{

		long startTime = System.currentTimeMillis();

		KEGGPortType serv = KeggAPI.getLocator().getKEGGPort();
		Map<String,MetaboliteContainer> result = new HashMap<String, MetaboliteContainer>();

		this.entity = getEntities(KeggAPI.getinfo(metabolite_Type.getEntity_Type()[0]+" "+metabolite_Type.getEntity_Type()[1]), metabolite_Type.getEntity_Type()[1]);

		for(int index = 0; index<entity.size(); index++)
		{
			String gene = entity.get(index);
			int i=0;
			while(i<9 && index+1<entity.size())
			{
				index++;
				metabolite=metabolite.concat("+"+entity.get(index));
				i++;
			}
			result.putAll(getMetabolitesArray(KeggRestful.fetch(KEGGOPERATION.GET,metabolite)));
		}

		long endTime = System.currentTimeMillis();

		//System.out.println("Total elapsed time in execution of method callMethod() is :"+ String.format("%d min, %d sec", 
				TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
		return result;
	}*/

	/**
	 * @param resultArray
	 * @return
	 */
	private Map<String ,MetaboliteContainer> getMetabolitesArray(String resultArray) {

		this.errorCount=0;
		Map<String ,MetaboliteContainer> result = new HashMap<String, MetaboliteContainer>();

		for(String results : resultArray.split("///")) {

			results=results.trim();
			if(!results.isEmpty()) {

				results=results.concat("\n///");
				Map<String, List<String>> resultsParsed = KeggAPI.parseFullEntry(results);
				String entry = KeggAPI.getFirstIfExists(KeggAPI.splitWhiteSpaces(resultsParsed.get("ENTRY")));

				if(entry!=null) {

					MetaboliteContainer metaboliteContainer = new MetaboliteContainer(entry);

					metaboliteContainer.setFormula(KeggAPI.getFirstIfExists(resultsParsed.get("FORMULA")));
					metaboliteContainer.setMolecular_weight(KeggAPI.getFirstIfExists(resultsParsed.get("MOL_WEIGHT")));
					String rawName = KeggAPI.getFirstIfExists(resultsParsed.get("NAME"));

					if(rawName != null) {

						metaboliteContainer.setName(rawName.replace(";", "").trim());
					}
					String same_as = KeggAPI.getFirstIfExists(resultsParsed.get("REMARK"));

					if(same_as != null) {

						if(same_as.contains("Same as:")) metaboliteContainer.setSame_as(same_as.split(":")[1].trim());
					}

					List<String> names = resultsParsed.get("NAME");
					if(names == null) names = new ArrayList<String>();
					names.remove(rawName);
					metaboliteContainer.setNames(names);

					List<String> ecnumbers = KeggAPI.splitWhiteSpaces(resultsParsed.get("ENZYME"));
					if(ecnumbers == null) ecnumbers = new ArrayList<String>();
					metaboliteContainer.setEnzymes(ecnumbers);

					List<String> reactions = KeggAPI.splitWhiteSpaces(resultsParsed.get("REACTION"));
					if(reactions == null) reactions = new ArrayList<String>();
					metaboliteContainer.setReactions(reactions);

					List<String> pathwaysData = resultsParsed.get("PATHWAY");

					if(pathwaysData != null) {

						Map<String, String> pathways = new HashMap<String, String>();

						for(String path: pathwaysData) {

							pathways.put(path.split("\\s")[0].replace("ko", "").replace("map", ""), path.replace(path.split("\\s")[0], "").trim());
						}
						metaboliteContainer.setPathways(pathways);

						//System.out.println(entry+"\t"+pathways);
					}

					List<String> crossRefs = resultsParsed.get("DBLINKS");
					if(crossRefs == null) crossRefs = new ArrayList<String>();
					metaboliteContainer.setDblinks(crossRefs);

					result.put(entry,metaboliteContainer);
					////System.out.println(result.get(entry).getEntry());
					////System.out.println(result.get(entry));
				}
			}
		}
		return result;
	}

	/**
	 * @param organismID
	 * @return
	 * @throws ServiceException
	 * @throws RemoteException

	public Map<String,GeneContainer> get_Kegg_Genes(String organismID) throws ServiceException, RemoteException{

		long startTime = System.currentTimeMillis();

		KEGGPortType serv = KeggAPI.getLocator().getKEGGPort();
		Map<String ,GeneContainer> result = new HashMap<String, GeneContainer>();

		entity = getEntities(KeggAPI.getinfo(EntityType.Gene.getEntity_Type()[0]+" "+organismID+":"),organismID.toLowerCase()+":");

		for(int index = 0; index<entity.size(); index++)
		{
			String gene = entity.get(index);
			int i=0;
			while(i<9 && index+1<entity.size())
			{
				index++;
				gene=gene.concat("+"+entity.get(index));
				i++;
			}
			result.putAll(getGeneArray(KeggRestful.fetch(KEGGOPERATION.GET,gene)));
		}

		long endTime = System.currentTimeMillis();

		//System.out.println("Total elapsed time in execution of method callMethod() is :"+ String.format("%d min, %d sec", 
				TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
		return result;
	}*/

	/**
	 * @param resultArray
	 * @return
	 */
	private Map<String ,GeneContainer> getGeneArray(String resultArray) {

		this.errorCount=0;
		Map<String ,GeneContainer> result = new HashMap<String, GeneContainer>();
		
		System.out.println("ResultArray---->"+resultArray);

		for(String results : resultArray.split("///")) {

			results=results.trim();

			if(!results.isEmpty()) {

				results=results.concat("\n///");
				Map<String, List<String>> resultsParsed = KeggAPI.parseFullEntry(results);
				String entry = KeggAPI.getFirstIfExists(KeggAPI.splitWhiteSpaces(resultsParsed.get("ENTRY")));

				if(entry!=null)  {

					GeneContainer geneContainer = new GeneContainer(entry);

					String rawName = KeggAPI.getFirstIfExists(resultsParsed.get("NAME"));

					if(rawName != null) {

						geneContainer.setName(rawName.replace(";", "").trim());
					}

					List<String> names = resultsParsed.get("NAME");

					if(names == null)
						names = new ArrayList<String>();

					names.remove(rawName);
					geneContainer.setNames(names);


					List<String> orthologyData = resultsParsed.get("ORTHOLOGY");

					if(orthologyData != null) {

						List<String> orthologues = new ArrayList<String>();

						for(String orthologue: orthologyData) {

							orthologues.add(orthologue.split("\\s")[0]);
						}
						geneContainer.setOrthologues(orthologues);
					}


					List<String> modulesData = resultsParsed.get("MODULE");

					if(modulesData != null) {

						List<String> modules = new ArrayList<String>();

						for(String module: modulesData) {

							modules.add(module.split("\\s")[0]);
						}
						geneContainer.setModules(modules);
					}

					String position = KeggAPI.getFirstIfExists(resultsParsed.get("POSITION"));

					if(position != null && position.contains(":")) {

						String[] position_array = position.split(":");

						if(position_array.length>1) {

							String chromosome_name = position_array[0];
							geneContainer.setChromosome_name(chromosome_name);
							//position = position.replace(chromosome_name+":", "").trim();
							position = position_array[1];
						}

						position = position.replaceFirst("\\.\\.", ":");
						geneContainer.setLeft_end_position(position.split(":")[0]);
						geneContainer.setRight_end_position(position.split(":")[1]);
					}

					List<String> sequenceAAData = resultsParsed.get("AASEQ");

					if(sequenceAAData != null) {
						geneContainer.setAalength(sequenceAAData.get(0));
						sequenceAAData.remove(0);
						String aaSequence = "";
						for (String aa :sequenceAAData) 
						{
							aaSequence=aaSequence.concat(aa);
						}
						geneContainer.setAasequence(aaSequence);
					}

					List<String> sequenceNTData = resultsParsed.get("NTSEQ");

					if(sequenceNTData != null) {

						geneContainer.setNtlength(sequenceNTData.get(0));
						sequenceNTData.remove(0);
						String ntSequence = "";

						for (String nt :sequenceNTData)  {

							ntSequence=ntSequence.concat(nt);
						}
						geneContainer.setNtsequence(ntSequence);
					}

					List<String> crossRefs = resultsParsed.get("DBLINKS");
					if(crossRefs == null) crossRefs = new ArrayList<String>();
					geneContainer.setDblinks(crossRefs);

					result.put(entry,geneContainer);
				}
			}
		}
		return result;
	}


	/**
	 * @param organismID
	 * @return
	 * @throws ServiceException
	 * @throws RemoteException

	public Map<String,EnzymeContainer> get_All_Kegg_Enzymes(String organismID) throws ServiceException, RemoteException{

		long startTime = System.currentTimeMillis();

		KEGGPortType serv = KeggAPI.getLocator().getKEGGPort();
		Map<String ,EnzymeContainer> result = new HashMap<String, EnzymeContainer>();

		entity = getEntities(KeggAPI.getinfo(EntityType.Enzyme.getEntity_Type()[0]+" "+EntityType.Enzyme.getEntity_Type()[1]),EntityType.Enzyme.getEntity_Type()[1]);
		for(int index = 0; index<entity.size(); index++)
		{
			String ecnumber = entity.get(index);
			int i=0;
			while(i<9 && index+1<entity.size())
			{
				index++;
				ecnumber=ecnumber.concat("+"+entity.get(index));
				i++;
			}
			result.putAll(getEnzymesArray(KeggRestful.fetch(KEGGOPERATION.GET,ecnumber), organismID));
		}

		long endTime = System.currentTimeMillis();

		//System.out.println("Total elapsed time in execution of method callMethod() is :"+ String.format("%d min, %d sec", 
				TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
		return result;
	}*/

	/**
	 * @param resultArray
	 * @param organismID
	 * @return
	 */
	private Map<String,EnzymeContainer> getEnzymesArray(String resultArray, String organismID) {

		this.errorCount=0;
		Map<String ,EnzymeContainer> result = new TreeMap<String, EnzymeContainer>();

		for(String results : resultArray.split("///")) {

			results=results.trim();
			if(!results.isEmpty()) {

				results=results.concat("\n///");
				Map<String, List<String>> resultsParsed = KeggAPI.parseFullEntry(results);
				String entry = KeggAPI.getSecondIfExists(KeggAPI.splitWhiteSpaces(resultsParsed.get("ENTRY")));

				if(entry!=null) {

					EnzymeContainer enzymeContainer = new EnzymeContainer(entry.replace("EC ", "").trim());

					List<String> names = resultsParsed.get("NAME");
					if(names == null) names = new ArrayList<String>();
					enzymeContainer.setNames(names);


					enzymeContainer.setName(KeggAPI.getFirstIfExists(resultsParsed.get("SYSNAME")));

					if(enzymeContainer.getName()==null) {

						if(!enzymeContainer.getNames().isEmpty()) {

							enzymeContainer.setName(enzymeContainer.getNames().get(0));
							names = enzymeContainer.getNames();
							names.remove(enzymeContainer.getName());
							enzymeContainer.setNames(names);
						}
					}

					enzymeContainer.setEnzyme_class(KeggAPI.getFirstIfExists(resultsParsed.get("CLASS")));

					List<String> genesList = resultsParsed.get("GENES");
					List<String> genes = new ArrayList<String>();

					if(genesList != null) {

						for(int i=0; i<genesList.size(); i++) {

							String gene = genesList.get(i);		

							if(gene.startsWith(organismID.toUpperCase()+":")) {

								gene=gene.split(":")[1].trim();

								for(String geneID: gene.split("\\s")) {

									if(geneID.contains("(")) {

										geneID=geneID.split("\\(")[0].trim();
									}
									genes.add(geneID.trim());
									i= genesList.size();
								}
								enzymeContainer.setGenes(genes);
							}
						}
					}

					List<String> cofactorsList = resultsParsed.get("COFACTOR");
					List<String> cofactors = new ArrayList<String>();

					if(cofactorsList!=null) {

						for(String cofactor:cofactorsList) {

							cofactors.add(cofactor.split(":")[1].replace("]", "").replace(";", "").trim());
						}
						enzymeContainer.setCofactors(cofactors);
					}

					List<String> reactions = resultsParsed.get("ALL_REAC");
					if(reactions == null) reactions = new ArrayList<String>();
					enzymeContainer.setReactions(reactions);

					List<String> pathwaysData = resultsParsed.get("PATHWAY");

					if(pathwaysData != null) {

						Map<String, String> pathways = new HashMap<String, String>();

						for(String path: pathwaysData) {

							pathways.put(path.split("\\s")[0].replace("ec", ""), path.replace(path.split("\\s")[0], "").trim());
						}
						enzymeContainer.setPathways(pathways);
					}

					List<String> orthologyData = resultsParsed.get("ORTHOLOGY");

					if(orthologyData != null) {

						List<String> orthologues = new ArrayList<String>();

						for(String orthologue: orthologyData) {

							orthologues.add(orthologue.split("\\s")[0]);
						}
						enzymeContainer.setOrthologues(orthologues);
					}

					List<String> crossRefs = resultsParsed.get("DBLINKS");
					if(crossRefs == null) crossRefs = new ArrayList<String>();
					enzymeContainer.setDblinks(crossRefs);

					result.put(entry,enzymeContainer);
				}
			}
		}
		return result;
	}


	/**
	 * @param resultArray
	 * @return
	 */
	private Map<String,ReactionContainer> getReactionsArray(String resultArray) {

		this.errorCount=0;
		//System.out.println(resultArray);
		Map<String,ReactionContainer> result = new TreeMap<String, ReactionContainer>();

		for(String results : resultArray.split("///")) {

			results=results.trim();
			if(!results.isEmpty()) {

				results=results.concat("\n///");
				Map<String, List<String>> resultsParsed = KeggAPI.parseFullEntry(results);
				String entry = KeggAPI.getFirstIfExists(KeggAPI.splitWhiteSpaces(resultsParsed.get("ENTRY")));

				if(entry!=null) {

					ReactionContainer reactionContainer = new ReactionContainer(entry);

					reactionContainer.setName(KeggAPI.getFirstIfExists(resultsParsed.get("NAME")));

					List<String> names = resultsParsed.get("NAME");
					if(names == null) {

						names = new ArrayList<String>();
					}
					reactionContainer.setNames(names);

					reactionContainer.setEquation(KeggAPI.getFirstIfExists(resultsParsed.get("DEFINITION")));

					String stoichiometry= KeggAPI.getFirstIfExists(resultsParsed.get("EQUATION"));
					String[] data=stoichiometry.split("<=>");
					String[] reactants = data[0].split("\\s\\+\\s");
					String[] products = data[1].split("\\s\\+\\s");

					reactionContainer.setReactantsStoichiometry(this.parseReactions(reactants,"-"));
					reactionContainer.setProductsStoichiometry(this.parseReactions(products,""));

					List<String> enzymesTemp = KeggAPI.splitWhiteSpaces(resultsParsed.get("ENZYME"));
					
					Set<String> enzymes = new HashSet<String>();
					if(enzymesTemp != null)
						enzymes = new HashSet<String>(enzymesTemp);
					
					reactionContainer.setEnzymes(enzymes);

					List<String> genericsTemp = resultsParsed.get("COMMENT");

					if(genericsTemp!=null) {

						for(String generic:genericsTemp) {

							if(generic.trim().toLowerCase().contains("general reaction"))
								reactionContainer.setGeneric(true);

							if(generic.trim().toLowerCase().contains("non-enzymatic") || generic.trim().toLowerCase().contains("non enzymatic"))
								reactionContainer.setNon_enzymatic(true);

							if(generic.trim().toLowerCase().contains("spontaneous"))
								reactionContainer.setSpontaneous(true);
						}
						Set<String> generics = new HashSet<>(genericsTemp);
						reactionContainer.setComments(generics);
					}
					
					List<String> pathwaysData = resultsParsed.get("PATHWAY");

					if(pathwaysData != null) {

						Map<String, String> pathways = new HashMap<String, String>();
						for(String path: pathwaysData)
						{
							pathways.put(path.split("\\s")[0].replace("rn", ""), path.replace(path.split("\\s")[0], "").trim());
						}
						reactionContainer.setPathwaysMap(pathways);
					}

					List<String> crossRefs = resultsParsed.get("DBLINKS");

					if(crossRefs == null) {

						crossRefs = new ArrayList<String>();
					}
					reactionContainer.setDblinks(crossRefs);

					result.put(entry,reactionContainer);
				}
			}
		}
		return result;
	}

	/**
	 * @return
	 * @throws ServiceException
	 * @throws RemoteException

	public Map<String,ModuleContainer> get_Kegg_Complex_Module() throws ServiceException, RemoteException{

		long startTime = System.currentTimeMillis();

		KEGGPortType serv = KeggAPI.getLocator().getKEGGPort();
		Map<String,ModuleContainer> result = new HashMap<String,ModuleContainer>();

		entity = getEntities(KeggAPI.getinfo(EntityType.Module.getEntity_Type()[0]+" "+EntityType.Module.getEntity_Type()[1]),EntityType.Module.getEntity_Type()[1]);

		for(int index = 0; index<entity.size(); index++)
		{
			String module = entity.get(index);
			int i=0;
			while(i<9 && index+1<entity.size())
			{
				index++;
				module=module.concat("+"+entity.get(index));
				i++;
			}
			result.putAll(getStructuralComplexArray(KeggRestful.fetch(KEGGOPERATION.GET,module)));
		}

		long endTime = System.currentTimeMillis();

		//System.out.println("Total elapsed time in execution of method callMethod() is :"+ String.format("%d min, %d sec", 
				TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
		return result;
	}
	 */

	/**
	 * @param resultArray
	 * @return
	 */
	private Map<String,ModuleContainer> getStructuralComplexArray(String resultArray) {

		this.errorCount=0;
		Map<String,ModuleContainer> result = new TreeMap<String, ModuleContainer>();
		for(String results : resultArray.split("///"))
		{
			results=results.trim();
			if(!results.isEmpty())
			{
				results=results.concat("\n///");
				Map<String,List<String>> resultsParsed = KeggAPI.parseFullEntry(results);
				String entry = KeggAPI.getFirstIfExists(KeggAPI.splitWhiteSpaces(resultsParsed.get("ENTRY")));
				String moduleType = KeggAPI.getSecondIfExists(KeggAPI.splitWhiteSpaces(resultsParsed.get("ENTRY")));
				if(entry!=null && moduleType.toLowerCase().equals("complex"))
				{
					ModuleContainer moduleContainer = new ModuleContainer(entry);
					moduleContainer.setName(KeggAPI.getFirstIfExists(resultsParsed.get("NAME")));
					moduleContainer.setDefinition(KeggAPI.getFirstIfExists(resultsParsed.get("DEFINITION")));
					moduleContainer.setModuleType(moduleType);

					List<String> orthologues = KeggAPI.splitLinesGetOrthologues(resultsParsed.get("ORTHOLOGY"));
					moduleContainer.setOrthologues(orthologues);


					List<String> comments = resultsParsed.get("COMMENT");
					if(comments!=null)
					{
						for(String comment:comments)
						{
							if(comment.trim().startsWith("Stoichiometry"))
							{
								moduleContainer.setStoichiometry(comment.replace("Stoichiometry: ", "").trim());
							}

							if(comment.trim().startsWith("Substrate"))
							{
								List<String> data = new ArrayList<String>();
								String[] substrates = comment.replace("Substrate: ", "").split(",");
								Pattern pat = Pattern.compile("[C|G|D|K]\\d{5}");
								Matcher m;
								for(String substrate:substrates)
								{
									m = pat.matcher(substrate);
									if (m.find()) 
									{
										data.add(m.group());				
									}
								}
								if(!data.isEmpty())
								{
									moduleContainer.setSubstrates(data);
								}
							}
						}
					}

					List<String> pathwaysData = resultsParsed.get("PATHWAY");
					if(pathwaysData != null)
					{
						Map<String, String> pathways = new HashMap<String, String>();
						for(String path: pathwaysData)
						{
							pathways.put(path.split("\\s")[0].replace("map", ""), path.replace(path.split("\\s")[0], "").trim());
						}
						moduleContainer.setPathways(pathways);
					}
					String moduleHieralchicalClass = KeggAPI.getFirstIfExists(resultsParsed.get("CLASS"));
					moduleContainer.setModuleHieralchicalClass(moduleHieralchicalClass);

					result.put(entry,moduleContainer);
					//					//System.out.println(result.get(entry).getEntry());
					//					//System.out.println(result.get(entry));
					//					//System.out.println(module_entry);

				}
			}

		}
		return result;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public static ConcurrentLinkedQueue<String[]> get_All_Kegg_Pathways() throws Exception {

		//long startTime = System.currentTimeMillis();

		List<String[]> pathways = KeggAPI.getPathways();
		ConcurrentLinkedQueue<String[]> result = new ConcurrentLinkedQueue<String[]>();

		for(int index = 0; index<pathways.size(); index++) {

			String[] data = new String[2];
			data[0]=pathways.get(index)[1].replace("map", "");
			data[1]=pathways.get(index)[0].replace(" - Reference pathway", "").trim();
			result.add(data);
		}

		//long endTime = System.currentTimeMillis();
		//System.out.println("Total elapsed time in execution of method callMethod() is :"+ String.format("%d min, %d sec", 
		//	TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));

		return result;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public static ConcurrentLinkedQueue<PathwaysHierarchyContainer> get_Kegg_Pathways_Hierarchy() throws Exception {

		//long startTime = System.currentTimeMillis();
		ConcurrentLinkedQueue<PathwaysHierarchyContainer> result = new ConcurrentLinkedQueue<PathwaysHierarchyContainer>();
		String resultString = KeggAPI.getXMLDataFromBriteID("br08901");	
		//System.out.println(resultString);

		String[] lines = resultString.split("\n");

		String key=null;
		PathwaysHierarchyContainer pathwaysHierarchyContainer=null;
		Map<String,List<String[]>> pathways_hierarchy_map = null;
		List<String[]> pathways=null;

		for (int i = 0; i < lines.length; i++)  {

			String data = lines[i];

			if(data.startsWith("A")) {

				if(pathwaysHierarchyContainer!=null) {

					pathways_hierarchy_map.put(key, pathways);
					pathwaysHierarchyContainer.setPathways_hierarchy(pathways_hierarchy_map);
					result.add(pathwaysHierarchyContainer);
				}
				pathwaysHierarchyContainer = new PathwaysHierarchyContainer(data.substring(1));
				pathways_hierarchy_map = new HashMap<String, List<String[]>>();
				key=null;
			}
			else {

				if(data.startsWith("B")) {

					if(key!=null) {

						pathways_hierarchy_map.put(key, pathways);
						pathwaysHierarchyContainer.setPathways_hierarchy(pathways_hierarchy_map);
					}
					key=data.substring(1).trim();
					pathways = new ArrayList<String[]>();
				}
				else {

					if(data.startsWith("C")) {

						String[] path = new String[2];
						path[0]=data.substring(1).trim().split("\\s")[0].trim();
						path[1]=data.substring(1).trim().replace(path[0],"").trim();
						pathways.add(path);												
					}
					else {

						if(data.equals("///")) {

							pathways_hierarchy_map.put(key, pathways);
							pathwaysHierarchyContainer.setPathways_hierarchy(pathways_hierarchy_map);
							result.add(pathwaysHierarchyContainer);
						}
					}
				}
			}
		}
		//long endTime = System.currentTimeMillis();
		//System.out.println("Total elapsed time in execution of method callMethod() is :"+ String.format("%d min, %d sec", 
		//	TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));
		return result;

	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public static ConcurrentLinkedQueue<String> getStructuralComplexModules() throws Exception {

		String resultString = KeggAPI.getXMLDataFromBriteID("ko00002");
		String[] lines = resultString.split("\n");

		ConcurrentLinkedQueue<String> result = new ConcurrentLinkedQueue<String>();
		for (int i = 0; i < lines.length; i++) 
		{
			String data = lines[i];
			if(data.startsWith("AStructural Complex"))
			{
				i++;
				data = lines[i];
				while(!data.startsWith("#"))
				{
					if(data.startsWith("B"))
					{
						i++;
						data = lines[i];
					}
					else
					{
						if(data.startsWith("C"))
						{
							i++;
							data = lines[i];
						}
						else
						{
							if(data.startsWith("D"))
							{
								String module_id=data.substring(1).trim().split("\\s")[0].trim();
								result.add("md:"+module_id);		
								i++;
								data = lines[i];
							}
						}
					}
				}
				i=lines.length;
			}
		}
		return result;
	}

	/**
	 * @return
	 * @throws Exception 
	 */
	public static ConcurrentLinkedQueue<String> getCompoundsWithBiologicalRoles() throws Exception {

		//Compounds with biological roles 	br08001

		String resultString = KeggAPI.getXMLDataFromBriteID("br08001");
		String[] lines = resultString.split("\n");

		ConcurrentLinkedQueue<String> result = new ConcurrentLinkedQueue<String>();
		for (int i = 0; i < lines.length; i++) 
		{
			String data=lines[i];
			if(data.startsWith("D"))
			{
				String metabolite_id=data.substring(1).trim().split("\\s")[0].trim();
				result.add(metabolite_id);	
			}
		}
		return result;
	}


	/**
	 * @param response
	 * @param pattern
	 * @return
	 */
	public static ConcurrentLinkedQueue<String> getEntities(String response, String pattern) {

		String[] lines = response.split("\n");

		Pattern p = Pattern.compile("^"+pattern+":"+".+");
		Matcher m;
		ConcurrentLinkedQueue<String> data = new ConcurrentLinkedQueue<String>();

		for (int i = 0; i < lines.length; i++)  {

			m = p.matcher(lines[i]);

			if (m.matches())  {

				data.add(m.group().split("\\s")[0]);				
			}
		}
		
		return data;
	}

	/**
	 * @param results
	 * @param metabolites
	 * @param signal
	 */
	private Map<String, String[]> parseReactions(String[] metabolites, String signal) {

		Map<String, String[]> result = new HashMap<String, String[]>();

		String metabolite_ID = null, stoichiometry, chains;

		Pattern pat = Pattern.compile("[C|G|D]\\d{5}");

		for(String metabolite:metabolites) {

			String[] data = new String[2];

			metabolite=metabolite.trim();
			Matcher mat = pat.matcher(metabolite);

			if(mat.find()) { 

				metabolite_ID=mat.group();
			}

			if(metabolite.startsWith(metabolite_ID)) {

				stoichiometry=signal.concat("1");
			}
			else {

				stoichiometry=signal.concat(metabolite.split(metabolite_ID)[0].trim());
			}

			if(metabolite.endsWith(metabolite_ID)) {

				chains="1";
			}
			else {

				chains=metabolite.split(metabolite_ID)[1].trim();
			}

			data[0]=stoichiometry;
			data[1]=chains;
			result.put(metabolite_ID, data);
		}

		return result;
	}

	/**
	 * @author ODias
	 *
	 */
	public static enum EntityType {

		Drugs(new String[]{"drug","dr"}),
		Compound(new String[]{"compound","cpd"}),
		Glycan(new String[]{"glycan","gl"}),
		Reaction(new String[]{"reaction","rn"}),
		Pathways(new String[]{"pathways","path"}),
		Enzyme(new String[]{"enzyme","ec"}),
		Gene(new String[]{"genes",""}),
		Module(new String[]{"module","md"});

		private String[] entity_Type;

		/**
		 * @param entity_Type
		 */
		private EntityType(String[] entity_Type){
			this.entity_Type = entity_Type;
		}

		/**
		 * @return
		 */
		public String[] getEntity_Type(){
			return this.entity_Type;
		}
	}

	/**
	 * @param resultMetabolites the resultMetabolites to set
	 */
	public void setResultMetabolites(ConcurrentHashMap<String,MetaboliteContainer> resultMetabolites) {
		this.resultMetabolites = resultMetabolites;
	}

	/**
	 * @return the resultMetabolites
	 */
	public ConcurrentHashMap<String,MetaboliteContainer> getResultMetabolites() {
		return resultMetabolites;
	}

	/**
	 * @param resultEnzymes the resultEnzymes to set
	 */
	public void setResultEnzymes(ConcurrentHashMap<String,EnzymeContainer> resultEnzymes) {
		this.resultEnzymes = resultEnzymes;
	}

	/**
	 * @return the resultEnzymes
	 */
	public ConcurrentHashMap<String,EnzymeContainer> getResultEnzymes() {
		return resultEnzymes;
	}

	/**
	 * @param resultReactions the resultReactions to set
	 */
	public void setResultReactions(ConcurrentHashMap<String,ReactionContainer> resultReactions) {
		this.resultReactions = resultReactions;
	}

	/**
	 * @return the resultReactions
	 */
	public ConcurrentHashMap<String,ReactionContainer> getResultReactions() {
		return resultReactions;
	}

	/**
	 * @param resultGenes the resultGenes to set
	 */
	public void setResultGenes(ConcurrentHashMap<String,GeneContainer> resultGenes) {
		this.resultGenes = resultGenes;
	}

	/**
	 * @return the resultGenes
	 */
	public ConcurrentHashMap<String,GeneContainer> getResultGenes() {
		return resultGenes;
	}

	/**
	 * @param resultModules the resultModules to set
	 */
	public void setResultModules(ConcurrentHashMap<String,ModuleContainer> resultModules) {
		this.resultModules = resultModules;
	}

	/**
	 * @return the resultModules
	 */
	public ConcurrentHashMap<String,ModuleContainer> getResultModules() {
		return resultModules;
	}

	/**
	 * @param organismID the organismID to set
	 */
	public void setOrganismID(String organismID) {
		this.organismID = organismID;
	}


	/**
	 * @return the organismID
	 */
	public String getOrganismID() {
		return organismID;
	}

	/**
	 * @return the entity
	 */
	public ConcurrentLinkedQueue<String> getEntity() {
		return entity;
	}


	/**
	 * @param entity the entity to set
	 */
	public void setEntity(ConcurrentLinkedQueue<String> entity) {
		this.entity = entity;
	}


	/**
	 * @return the entityTypeString
	 */
	public EntityType getEntity_Type_String() {
		return entityTypeString;
	}


	/**
	 * @param entityTypeString the entityTypeString to set
	 */
	public void setEntityTypeString(EntityType entityTypeString) {
		this.entityTypeString = entityTypeString;
	}


	/**
	 * @return the resultPathways

	public ConcurrentLinkedQueue<String[]> getResultPathways() {
		return resultPathways;
	}*/


	/**
	 * @param resultPathways the resultPathways to set

	public void setResultPathways(ConcurrentLinkedQueue<String[]> resultPathways) {
		this.resultPathways = resultPathways;
	}*/


	/**
	 * @param orthologueEntities the orthologueEntity to set

	public void setOrthologueEntities(ConcurrentLinkedQueue<String> orthologueEntities) {
		this.orthologueEntities = orthologueEntities;
	}


	/**
	 * @return the orthologueEntity

	public ConcurrentLinkedQueue<String> getOrthologueEntities() {
		return orthologueEntities;
	}


	/**
	 * @param kegg_Pathways_Hierarchy the kegg_Pathways_Hierarchy to set

	public void setKegg_Pathways_Hierarchy(ConcurrentLinkedQueue<PathwaysHierarchyContainer> kegg_Pathways_Hierarchy) {
		this.kegg_Pathways_Hierarchy = kegg_Pathways_Hierarchy;
	}


	/**
	 * @return the kegg_Pathways_Hierarchy

	public ConcurrentLinkedQueue<PathwaysHierarchyContainer> getKegg_Pathways_Hierarchy() {
		return kegg_Pathways_Hierarchy;
	}*/
}
