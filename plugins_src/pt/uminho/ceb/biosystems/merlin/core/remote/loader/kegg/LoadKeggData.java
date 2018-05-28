/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.remote.loader.kegg;

import java.rmi.RemoteException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.GregorianCalendar;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.rpc.ServiceException;

import pt.uminho.ceb.biosystems.merlin.core.remote.retriever.kegg.RetrieveKeggData;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.CompartmentContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.EnzymeContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.GeneContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.MetaboliteContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.ModuleContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.PathwaysHierarchyContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.ReactionContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.datastructures.list.ListUtilities;

/**
 * @author ODias
 *
 */
public class LoadKeggData implements Runnable{

	private final static int  LIST_SIZE = 1000;
	private ConcurrentLinkedQueue<MetaboliteContainer> resultMetabolites;
	private ConcurrentLinkedQueue<EnzymeContainer> resultEnzymes;
	private ConcurrentLinkedQueue<ReactionContainer> resultReactions;
	private ConcurrentLinkedQueue<GeneContainer> resultGenes;
	private ConcurrentLinkedQueue<ModuleContainer> resultModules;
	private ConcurrentLinkedQueue<String[]> resultPathways;
	private ConcurrentLinkedQueue<PathwaysHierarchyContainer> kegg_Pathways_Hierarchy;
	private ConcurrentLinkedQueue<String> enzymesPathwayList,orthologueEntities;
	private KeggLoader keggLoader;
	private ConcurrentLinkedQueue<Integer> reactionsPathwayList, metabolitesPathwayList, modulesPathwayList;
	private ConcurrentHashMap<String,Integer> pathways_id;
	private TimeLeftProgress progress;
	private AtomicBoolean cancel;
	private long startTime;
	private AtomicInteger dataSize;
	private AtomicInteger datum;
	private boolean error, loadAll;
	private Connection connection;
	private AtomicBoolean addMetPathDataSize, addModPathDataSize, addReacPathDataSize, addEnzPathDataSize;
	
	private ConcurrentLinkedQueue<String> compoundsWithBiologicalRoles;
	private boolean importFromSBML;
	private ConcurrentLinkedQueue<CompartmentContainer> resultCompartments;

	/**
	 * Constructor for load KEGG data runnable
	 * 
	 * @param connection
	 * @param retrieveKeggData
	 * @param databaseInitialData
	 * @param progress
	 * @param datum
	 * @param cancel
	 * @param dataSize
	 * @param startTime
	 * @throws RemoteException
	 * @throws InterruptedException
	 * @throws SQLException
	 */
	public LoadKeggData(Connection connection, RetrieveKeggData retrieveKeggData, DatabaseInitialData databaseInitialData, 
			TimeLeftProgress progress, AtomicInteger datum, AtomicBoolean cancel, AtomicInteger dataSize, long startTime) throws SQLException {

		this.connection = connection;
		this.cancel = cancel;
		this.progress = progress;
		this.startTime = startTime;
		this.datum = datum;
		this.dataSize = dataSize;
		//this.setResultMetabolites(retrieveKeggData.getResultMetabolites());
		this.setResultEnzymes(retrieveKeggData.getResultEnzymes());
		this.setResultReactions(retrieveKeggData.getResultReactions());
		this.setResultGenes(retrieveKeggData.getResultGenes());
		this.setResultModules(retrieveKeggData.getResultModules());
		this.setKegg_Pathways_Hierarchy(retrieveKeggData.getKegg_Pathways_Hierarchy());
		this.setOrthologueEntities(retrieveKeggData.getOrthologueEntities());
		this.setPathways_id(databaseInitialData.getPathways_id());
		this.setReactionsPathwayList(databaseInitialData.getReactionsPathwayList());
		this.setEnzymesPathwayList(databaseInitialData.getEnzymesPathwayList());
		this.setMetabolitesPathwayList(databaseInitialData.getMetabolitesPathwayList());
		this.setModulesPathwayList(databaseInitialData.getModulesPathwayList());
		this.keggLoader = new KeggLoader(databaseInitialData, retrieveKeggData.getCompoundsWithBiologicalRoles(), false);

		this.setError(false);

		this.addMetPathDataSize = new AtomicBoolean(true);
		this.addEnzPathDataSize = new AtomicBoolean(true);
		this.addModPathDataSize = new AtomicBoolean(true);
		this.addReacPathDataSize = new AtomicBoolean(true);
		this.loadAll = true;
		if(this.resultGenes != null && this.resultGenes.size()>0)
			this.loadAll = false;
	}
	
	
	
	///////////////////////////////////////////////////////////////
	
	/**
	 * test constructor to load from imported SBML models
	 * 
	 * @param connection
	 * @param progress
	 * @param datum
	 * @param cancel
	 * @param dataSize
	 * @param startTime
	 * @throws SQLException
	 */
	public LoadKeggData(Connection connection, DatabaseInitialData databaseInitialData, TimeLeftProgress progress, AtomicInteger datum, AtomicBoolean cancel, AtomicInteger dataSize, long startTime
			) throws SQLException {

		this.connection = connection;
		this.cancel = cancel;
		this.progress = progress;
		this.startTime = startTime;
		this.datum = datum;
		this.dataSize = dataSize;
		
		this.importFromSBML = true;
		this.compoundsWithBiologicalRoles = new ConcurrentLinkedQueue<String>();
		
		
		this.setPathways_id(databaseInitialData.getPathways_id());
		this.setReactionsPathwayList(databaseInitialData.getReactionsPathwayList());
		this.setEnzymesPathwayList(databaseInitialData.getEnzymesPathwayList());
		this.setMetabolitesPathwayList(databaseInitialData.getMetabolitesPathwayList());
		this.setModulesPathwayList(databaseInitialData.getModulesPathwayList());
		
		
		this.keggLoader = new KeggLoader(databaseInitialData, compoundsWithBiologicalRoles, importFromSBML);

		this.addMetPathDataSize = new AtomicBoolean(false);
		this.addEnzPathDataSize = new AtomicBoolean(false);
		this.addModPathDataSize = new AtomicBoolean(false);
		this.addReacPathDataSize = new AtomicBoolean(false);
//		loadAll = false;
		loadAll = true;
		
	}

	/////////////////////////////////////////////////////////////////////////////////
	
	
	
	/**
	 * 
	 * Constructor to be removed after changing all loaders to prepared statements
	 * 
	 * @param connection2
	 * @param retrieveKeggData
	 * @param databaseInitialData
	 * @param progress2
	 * @param datum2
	 * @param cancel2
	 * @param dataSize2
	 * @param startTime2
	 * @param flag
	 * @throws SQLException 
	 */
	public LoadKeggData(Connection connection, RetrieveKeggData retrieveKeggData, DatabaseInitialData databaseInitialData, 
			TimeLeftProgress progress, AtomicInteger datum, AtomicBoolean cancel, AtomicInteger dataSize, long startTme, boolean b) throws SQLException {

		this.connection = connection;
		this.cancel = cancel;
		this.progress = progress;
		this.startTime = startTme;
		this.datum = datum;
		this.dataSize = dataSize;
		this.setResultMetabolites(retrieveKeggData.getResultMetabolites());
		this.keggLoader = new KeggLoader(databaseInitialData, retrieveKeggData.getCompoundsWithBiologicalRoles(), false);

		this.setError(false);

		this.addMetPathDataSize = new AtomicBoolean(false);
		this.addEnzPathDataSize = new AtomicBoolean(false);
		this.addModPathDataSize = new AtomicBoolean(false);
		this.addReacPathDataSize = new AtomicBoolean(false);
		loadAll = false;
	}

	@Override
	public void run() {

		try {

			Statement stmt = connection.createStatement();
			DatabaseType databaseType = connection.getDatabaseType();
			
			if(!this.cancel.get()) {
				
				if(importFromSBML) {
					System.out.println("Loading Compartments...");
					this.loadCompartments(stmt, databaseType);
				}
				
				System.out.println("Loading Pathways...");
				this.loadPathways(stmt, databaseType);
				System.out.println("Loading Compounds...");
				this.loadCompounds();
				
//				this.loadCompartments(stmt, databaseType);
				
				System.out.println("Loading Genes...");
				this.loadGenes(stmt, databaseType);
				System.out.println("Loading Enzymes...");
				this.loadEnzymes(stmt, databaseType);
				System.out.println("Loading Modules...");
				this.loadModules(stmt, databaseType);
				System.out.println("Loading Reactions...");
				this.loadReactions(stmt, databaseType);
				
				if(loadAll) {
					
					this.keggLoader.setPathways_id(this.getPathways_id());
					
					if(this.addModPathDataSize.get()) {

						this.dataSize = new AtomicInteger(this.dataSize.get() + this.modulesPathwayList.size());
						this.addModPathDataSize.set(false);
					}
					this.loadModulesPathwayList(stmt);

					if(this.addReacPathDataSize.get()) {

						this.dataSize = new AtomicInteger(this.dataSize.get() + this.reactionsPathwayList.size());
						this.addReacPathDataSize.set(false);
					}
					
					this.loadReactionsPathwayList(stmt);

					if(this.addEnzPathDataSize.get()) {

						this.dataSize = new AtomicInteger(this.dataSize.get() + this.enzymesPathwayList.size());
						this.addEnzPathDataSize.set(false);
					}
					this.loadEnzymesPathwayList(stmt);

					if(this.addMetPathDataSize.get()) {

						this.dataSize = new AtomicInteger(this.dataSize.get() + this.metabolitesPathwayList.size());
						this.addMetPathDataSize.set(false);
					}			
					this.loadMetabolitePathwayList(stmt);
					
				}
			}
		}
		catch (RemoteException e) {e.printStackTrace(); this.setError(true);}
		catch (SQLException e) {e.printStackTrace(); this.setError(true);}
	}


	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadGenes(Statement stmt, DatabaseType databaseType) throws RemoteException, SQLException{
				
		if(this.resultGenes!=null) {
			
			while(!this.resultGenes.isEmpty()) {

				GeneContainer geneList;
				if((geneList = this.resultGenes.poll()) != null) {
					
					this.keggLoader.loadGene(geneList, stmt, databaseType);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize.get(), "Loading Genes");
				}
			}
		}
	}
	
	
	
	//////////////////
	/**
	 * @throws SQLException
	 */
	public void loadCompartments(Statement stmt, DatabaseType databaseType) throws SQLException{
		
		if(this.resultCompartments != null) {
			
			while(!this.resultCompartments.isEmpty() && !this.cancel.get()) {
				
				CompartmentContainer compartment;
				if((compartment = this.resultCompartments.poll()) != null) {
					
					this.keggLoader.loadCompartment(compartment, stmt, databaseType);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize.get(), "Loading Genes");
				}
				
//				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize.get(), "Loading Compounds");
//				@SuppressWarnings("unchecked")
//				ConcurrentLinkedQueue<MetaboliteContainer> compartmentContainers = MerlinUtils.getConcurrentList(this.resultCompartments, LIST_SIZE);

//				this.keggLoader.loadCompartments(resultCompartments);
//				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize.get(), "Loading Compounds");
			}
		}
	}
	/////////////////////////////	
	
	
	
	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadCompounds() throws RemoteException, SQLException{

		if(this.resultMetabolites!=null) {

			while(!this.resultMetabolites.isEmpty() && !this.cancel.get()) {

				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize.get(), "Loading Compounds");
				@SuppressWarnings("unchecked")
				ConcurrentLinkedQueue<MetaboliteContainer> containers = (ConcurrentLinkedQueue<MetaboliteContainer>) ListUtilities.getConcurrentList(this.resultMetabolites, LIST_SIZE);

				this.keggLoader.loadMetabolites(containers);
				this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize.get(), "Loading Compounds");
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	//	public void loadCompounds(Statement stmt, DatabaseType databaseType) throws RemoteException, SQLException{
	//
	//		if(this.resultMetabolites!=null) {
	//
	//			while(!this.resultMetabolites.isEmpty() && !this.cancel.get()) {
	//
	//				MetaboliteContainer metaboliteList;
	//				if((metaboliteList = this.resultMetabolites.poll()) != null) {
	//
	//					this.keggLoader.loadMetabolite(metaboliteList, stmt, databaseType);
	//					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize, "Loading Compounds");
	//				}
	//			}
	//		}
	//	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadEnzymes(Statement stmt, DatabaseType databaseType) throws RemoteException, SQLException{

		if(this.resultEnzymes!=null) {

			while(!this.resultEnzymes.isEmpty() && !this.cancel.get()) {

				EnzymeContainer enzymesList;
				if((enzymesList = this.resultEnzymes.poll()) != null) {

					this.keggLoader.loadProtein(enzymesList, stmt, databaseType);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize.get(), "Loading Enzymes");
				}
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadReactions(Statement stmt, DatabaseType databaseType) throws RemoteException, SQLException{

		if(this.resultReactions!=null) {
			
			while(!this.resultReactions.isEmpty() && !this.cancel.get()) {

				ReactionContainer reactionList;
				
				if((reactionList =this.resultReactions.poll()) != null) {
					
					this.keggLoader.loadReaction(reactionList, stmt, databaseType);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize.get(), "Loading Reactions");
				}
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadModules(Statement stmt, DatabaseType databaseType) throws RemoteException, SQLException{

		if(this.resultModules!=null) {

			while(!this.resultModules.isEmpty() && !this.cancel.get()) {

				ModuleContainer moduleList;
				if((moduleList =this.resultModules.poll()) != null) {

					this.keggLoader.loadModule(moduleList, stmt, databaseType);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize.get(), "Loading Modules");
				}
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadPathways(Statement stmt, DatabaseType databaseType) throws RemoteException, SQLException{

		if(this.getKegg_Pathways_Hierarchy()!=null) {

			while(!this.getKegg_Pathways_Hierarchy().isEmpty() && !this.cancel.get()) {

				PathwaysHierarchyContainer reactionPathList;
				if((reactionPathList = this.getKegg_Pathways_Hierarchy().poll()) != null) {

					this.keggLoader.loadPathways(reactionPathList, stmt, databaseType);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize.get(), "Loading Pathways");
				}
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadReactionsPathwayList(Statement stmt) throws RemoteException, SQLException{
		
		if(this.getReactionsPathwayList() != null) {
			
			while(!this.getReactionsPathwayList().isEmpty() && !this.cancel.get()) {
				
				Integer reactionPathList;

				if((reactionPathList = this.getReactionsPathwayList().poll()) != null) {
					
					this.keggLoader.load_ReactionsPathway(reactionPathList, stmt);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize.get(), "Loading Reactions Pathways");
				}
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadEnzymesPathwayList(Statement stmt) throws RemoteException, SQLException{

		if(this.getEnzymesPathwayList()!=null) {

			while(!this.getEnzymesPathwayList().isEmpty() && !this.cancel.get()) {

				String enzymesPathwayList;
				if((enzymesPathwayList = this.getEnzymesPathwayList().poll()) != null) {

					this.keggLoader.load_EnzymesPathway(enzymesPathwayList, stmt);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize.get(), "Loading Enzymes Pathways");
				}
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadModulesPathwayList(Statement stmt) throws RemoteException, SQLException{

		if(this.getModulesPathwayList()!=null) {

			while(!this.getModulesPathwayList().isEmpty() && !this.cancel.get()) {

				Integer modulePathList;
				if((modulePathList = this.getModulesPathwayList().poll()) != null) {

					this.keggLoader.load_ModulePathway(modulePathList, stmt);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize.get(), "Loading Modules Pathways");
				}
			}
		}
	}

	/**
	 * @throws RemoteException
	 * @throws SQLException
	 * @throws ServiceException
	 */
	public void loadMetabolitePathwayList(Statement stmt) throws RemoteException, SQLException{

		if(this.getMetabolitesPathwayList()!=null) {

			while(!this.getMetabolitesPathwayList().isEmpty() && !this.cancel.get()) {

				Integer metaPathList;
				if((metaPathList = this.getMetabolitesPathwayList().poll()) != null) {

					this.keggLoader.load_MetabolitePathway(metaPathList, stmt);
					this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), this.dataSize.get(), "Loading Metabolites Pathways");
				}
			}
		}
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
		return resultReactions;
	}



	/**
	 * @param resultPathways the resultPathways to set
	 */
	public void setResultPathways(ConcurrentLinkedQueue<String[]> resultPathways) {
		this.resultPathways = resultPathways;
	}



	/**
	 * @return the resultPathways
	 */
	public ConcurrentLinkedQueue<String[]> getResultPathways() {
		return resultPathways;
	}



	/**
	 * @param kegg_Pathways_Hierarchy the kegg_Pathways_Hierarchy to set
	 */
	public void setKegg_Pathways_Hierarchy(ConcurrentLinkedQueue<PathwaysHierarchyContainer> kegg_Pathways_Hierarchy) {
		this.kegg_Pathways_Hierarchy = kegg_Pathways_Hierarchy;
	}



	/**
	 * @return the kegg_Pathways_Hierarchy
	 */
	public ConcurrentLinkedQueue<PathwaysHierarchyContainer> getKegg_Pathways_Hierarchy() {
		return kegg_Pathways_Hierarchy;
	}



	/**
	 * @param orthologueEntities the orthologueEntities to set
	 */
	public void setOrthologueEntities(ConcurrentLinkedQueue<String> orthologueEntities) {
		this.orthologueEntities = orthologueEntities;
	}



	/**
	 * @return the orthologueEntities
	 */
	public ConcurrentLinkedQueue<String> getOrthologueEntities() {
		return orthologueEntities;
	}

	/**
	 * @return the resultMetabolites
	 */
	public ConcurrentLinkedQueue<MetaboliteContainer> getResultMetabolites() {
		return resultMetabolites;
	}

	/**
	 * @param resultMetabolites the resultMetabolites to set
	 */
	public void setResultMetabolites(
			ConcurrentLinkedQueue<MetaboliteContainer> resultMetabolites) {
		this.resultMetabolites = resultMetabolites;
	}

	/**
	 * @return the resultEnzymes
	 */
	public ConcurrentLinkedQueue<EnzymeContainer> getResultEnzymes() {
		return resultEnzymes;
	}

	/**
	 * @param resultEnzymes the resultEnzymes to set
	 */
	public void setResultEnzymes(ConcurrentLinkedQueue<EnzymeContainer> resultEnzymes) {
		this.resultEnzymes = resultEnzymes;
	}

	/**
	 * @return the resultGenes
	 */
	public ConcurrentLinkedQueue<GeneContainer> getResultGenes() {
		return resultGenes;
	}

	/**
	 * @param resultGenes the resultGenes to set
	 */
	public void setResultGenes(ConcurrentLinkedQueue<GeneContainer> resultGenes) {
		this.resultGenes = resultGenes;
	}

	/**
	 * @return the resultModules
	 */
	public ConcurrentLinkedQueue<ModuleContainer> getResultModules() {
		return resultModules;
	}

	/**
	 * @param resultModules the resultModules to set
	 */
	public void setResultModules(ConcurrentLinkedQueue<ModuleContainer> resultModules) {
		this.resultModules = resultModules;
	}
	
	/**
	 * @return the resultCompartments
	 */
	public ConcurrentLinkedQueue<CompartmentContainer> getresultCompartments() {
		return resultCompartments;
	}

	/**
	 * @param the resultCompartments to set
	 */
	public void setResultCompartments(ConcurrentLinkedQueue<CompartmentContainer> resultCompartments) {
		this.resultCompartments = resultCompartments;
	}
	
	/**
	 * @return the keggLoader
	 */
	public KeggLoader getKegg_loader() {
		return this.keggLoader;
	}

	/**
	 * @param keggLoader the keggLoader to set
	 */
	public void setKegg_loader(KeggLoader keggLoader) {
		this.keggLoader = keggLoader;
	}

	/**
	 * @param reactionsPathwayList the reactionsPathwayList to set
	 */
	public void setReactionsPathwayList(ConcurrentLinkedQueue<Integer> reactionsPathwayList) {
		this.reactionsPathwayList = reactionsPathwayList;
	}

	/**
	 * @return the reactionsPathwayList
	 */
	public ConcurrentLinkedQueue<Integer> getReactionsPathwayList() {
		return this.reactionsPathwayList;
	}

	/**
	 * @param enzymesPathwayList the enzymesPathwayList to set
	 */
	public void setEnzymesPathwayList(ConcurrentLinkedQueue<String> enzymesPathwayList) {
		this.enzymesPathwayList = enzymesPathwayList;
	}

	/**
	 * @return the enzymesPathwayList
	 */
	public ConcurrentLinkedQueue<String> getEnzymesPathwayList() {
		return this.enzymesPathwayList;
	}

	/**
	 * @param metabolitesPathwayList the metabolitesPathwayList to set
	 */
	public void setMetabolitesPathwayList(ConcurrentLinkedQueue<Integer> metabolitesPathwayList) {
		this.metabolitesPathwayList = metabolitesPathwayList;
	}

	/**
	 * @return the metabolitesPathwayList
	 */
	public ConcurrentLinkedQueue<Integer> getMetabolitesPathwayList() {
		return this.metabolitesPathwayList;
	}

	/**
	 * @param modulesPathwayList the modulesPathwayList to set
	 */
	public void setModulesPathwayList(ConcurrentLinkedQueue<Integer> modulesPathwayList) {
		this.modulesPathwayList = modulesPathwayList;
	}

	/**
	 * @return the modulesPathwayList
	 */
	public ConcurrentLinkedQueue<Integer> getModulesPathwayList() {
		return modulesPathwayList;
	}

	/**
	 * @param pathways_id the pathways_id to set
	 */
	public void setPathways_id(ConcurrentHashMap<String,Integer> pathways_id) {
		this.pathways_id = pathways_id;
	}

	/**
	 * @return the pathways_id
	 */
	public ConcurrentHashMap<String,Integer> getPathways_id() {
		return this.pathways_id;
	}

	/**
	 * @param cancel
	 */
	public void setCancel(AtomicBoolean cancel) {

		this.cancel = cancel;
	}

	/**
	 * @return the error
	 */
	public boolean isError() {
		return this.error;
	}

	/**
	 * @param error the error to set
	 */
	public void setError(boolean error) {
		this.error = error;
	}

}
