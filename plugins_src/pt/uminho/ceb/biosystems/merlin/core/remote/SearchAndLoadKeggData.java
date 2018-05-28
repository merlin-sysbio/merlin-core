/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.remote;

import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import pt.uminho.ceb.biosystems.merlin.core.remote.loader.kegg.DatabaseInitialData;
import pt.uminho.ceb.biosystems.merlin.core.remote.loader.kegg.KeggLoader;
import pt.uminho.ceb.biosystems.merlin.core.remote.loader.kegg.LoadKeggData;
import pt.uminho.ceb.biosystems.merlin.core.remote.retriever.kegg.RetrieveKeggData;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

/**
 * @author ODias
 *
 * @author davidelagoa
 *
 */
public class SearchAndLoadKeggData {

	private static final String DEFAULT_ORG = "no_org";
	private static final boolean ACTIVE_COMPOUNDS = true;
	private List<Runnable> runnables;
	private AtomicBoolean cancel;
	private TimeLeftProgress progress;
	private RetrieveKeggData retrieveKeggData;

	/**
	 * Constructor for SearchAndLoadKeggData
	 * 
	 * @param cancel
	 * @param progress
	 */
	public SearchAndLoadKeggData(AtomicBoolean cancel, TimeLeftProgress progress) {

		try  {

			this.cancel = cancel;
			this.progress = progress;
		}
		catch (Exception e) {

			e.printStackTrace();
		}

	}

	/**
	 * Get organisms annotation data from kegg web servers. 
	 * 
	 * @param project
	 * @param organismID
	 * @return
	 */
	public boolean getOrganismData(String organismID) {

		try  {

			this.retrieveKeggData = new RetrieveKeggData(organismID, this.progress, this.cancel);
			this.retrieveKeggData.retrieveOrganismData();
			return true;
		}
		catch (Exception e) {

			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Get data from kegg web servers. 
	 * 
	 * @param project
	 * @param organismID
	 * @return
	 */
	public boolean getMetabolicData() {

		try  {

			this.retrieveKeggData = new RetrieveKeggData(DEFAULT_ORG, this.progress, this.cancel);
			this.retrieveKeggData.retrieveMetabolicData(ACTIVE_COMPOUNDS);
			return true;
		}
		catch (Exception e) {

			e.printStackTrace();
		}
		return false;
	}

	/**
	 * @param connection
	 * @return
	 */
	public boolean loadData(Connection connection) {

		try  {

			Statement stmt = connection.createStatement();

			boolean error = false;
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			int numberOfProcesses =  Runtime.getRuntime().availableProcessors();
			List<Thread> threads = new ArrayList<Thread>();

			this.runnables = new ArrayList<Runnable>();

			DatabaseInitialData databaseInitialData = new DatabaseInitialData(connection);
			if(this.retrieveKeggData.getResultGenes()!=null && this.retrieveKeggData.getResultGenes().size()>0)
				databaseInitialData.retrieveAnnotationData();
			else
				databaseInitialData.retrieveAllData();

			AtomicInteger dataSize = new AtomicInteger(0);
			if(this.retrieveKeggData.getResultMetabolites()!=null)
				dataSize = new AtomicInteger( dataSize.get() + new Integer(retrieveKeggData.getResultMetabolites().size()));
			if(this.retrieveKeggData.getResultEnzymes()!=null)
				dataSize = new AtomicInteger( dataSize.get() + new Integer(retrieveKeggData.getResultEnzymes().size()));
			if(this.retrieveKeggData.getResultReactions()!=null)
				dataSize = new AtomicInteger( dataSize.get() + new Integer(retrieveKeggData.getResultReactions().size()));
			if(this.retrieveKeggData.getResultGenes()!=null)
				dataSize = new AtomicInteger( dataSize.get() + new Integer(retrieveKeggData.getResultGenes().size()));
			if(this.retrieveKeggData.getResultModules()!=null)
				dataSize = new AtomicInteger( dataSize.get() + new Integer(retrieveKeggData.getResultModules().size()));
			if(this.retrieveKeggData.getKegg_Pathways_Hierarchy()!=null)
				dataSize = new AtomicInteger( dataSize.get() + new Integer(retrieveKeggData.getKegg_Pathways_Hierarchy().size()));
			if(this.retrieveKeggData.getOrthologueEntities()!=null)
				dataSize = new AtomicInteger( dataSize.get() + new Integer(retrieveKeggData.getOrthologueEntities().size()));
			//			dataSize = dataSize + databaseInitialData.getReactionsPathwayList().size();
			//			dataSize = dataSize + databaseInitialData.getEnzymesPathwayList().size();
			//			dataSize = dataSize + databaseInitialData.getMetabolitesPathwayList().size();
			//			dataSize = dataSize + databaseInitialData.getModulesPathwayList().size();

			AtomicInteger datum = new AtomicInteger(0);
			long startTime = System.currentTimeMillis();

			//loading prepared statements

			for(int i=0; i<numberOfProcesses; i++) {

				Runnable loadKeggData = new LoadKeggData(connection, this.retrieveKeggData, databaseInitialData, this.progress, datum, this.cancel, dataSize, startTime, true);
				this.runnables.add(loadKeggData);
				Thread thread = new Thread(loadKeggData);
				threads.add(thread);
				thread.start();

				if(((LoadKeggData) loadKeggData).isError())					
					error = true;
			}

			for(Thread thread :threads)				
				thread.join();

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			numberOfProcesses =  Runtime.getRuntime().availableProcessors()/2;
			threads = new ArrayList<Thread>();


			//loading normal statments (remove after setting all loaders to prepared statements

			for(int i=0; i<numberOfProcesses; i++) {

				Runnable loadKeggData = new LoadKeggData(connection, this.retrieveKeggData, databaseInitialData, this.progress, datum, this.cancel, dataSize, startTime);
				this.runnables.add(loadKeggData);
				Thread thread = new Thread(loadKeggData);
				threads.add(thread);
				thread.start();

				if(((LoadKeggData) loadKeggData).isError())					
					error = true;
			}

			for(Thread thread :threads)				
				thread.join();

			long endTime2 = System.currentTimeMillis();

			long startTime1 = System.currentTimeMillis();

			KeggLoader.buildViews(connection, stmt);

			long endTime1 = System.currentTimeMillis();

			long endTime = System.currentTimeMillis();

			System.out.println("Total elapsed time in execution of method Load_kegg is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime2-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime2-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime2-startTime))));

			System.out.println("Total elapsed time in execution of method build view is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime1-startTime1),TimeUnit.MILLISECONDS.toSeconds(endTime1-startTime1) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime1-startTime1))));

			System.out.println("Total elapsed time in execution of method TOTAL is :"+ String.format("%d min, %d sec", 
					TimeUnit.MILLISECONDS.toMinutes(endTime-startTime),TimeUnit.MILLISECONDS.toSeconds(endTime-startTime) -TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(endTime-startTime))));

			if(error) 
				return false;
			else
				return true;
		}
		catch (Exception e) {

			e.printStackTrace();
		}
		return false;
	}


	/**
	 * @return the cancel
	 */
	public AtomicBoolean isCancel() {

		return cancel;
	}

	/**
	 * 
	 */
	public void setCancel() {

		this.cancel = new AtomicBoolean(true);

		if(this.retrieveKeggData != null) {

			this.retrieveKeggData.setCancel(this.cancel);
		}

		if (this.runnables != null) {

			for(Runnable lc :this.runnables) {

				((LoadKeggData) lc).setCancel(this.cancel);
			}
		}
	}

	/**
	 * @param progress
	 */
	public void setTimeLeftProgress(TimeLeftProgress progress) {

		this.progress = progress;		
	}
}


