/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.utilities;

import java.awt.Rectangle;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.JTable;
import javax.swing.JViewport;

import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ebi.uniprot.TaxonomyContainer;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.NcbiAPI;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation.CompartmentsAnnotationDataContainer;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation.EnzymesAnnotationDataInterface;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation.TransportersAnnotationDataContainer;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.EnzymesContainer;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactantsProducts;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactionsInterface;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Genes;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Proteins;
import pt.uminho.ceb.biosystems.merlin.core.gui.CustomGUI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseAccess;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * @author Oscar
 *
 */
public class MerlinUtils {


	/**
	 * @param projectName
	 */
	public static void updateAllViews(String projectName) {

		MerlinUtils.updateEntity(projectName);
		MerlinUtils.updateProteinView(projectName);
		MerlinUtils.updateGeneView(projectName);
		MerlinUtils.updateAnnotationViews(projectName);
		MerlinUtils.updateMetabolicViews(projectName);
		MerlinUtils.updateProjectView(projectName);
	}



	/**
	 * @param projectName
	 */
	public static void updateMetabolicViews(String projectName) {

		MerlinUtils.updateReactionsView(projectName);
		MerlinUtils.updateEnzymeView(projectName);
		MerlinUtils.updateReagentProductsView(projectName);
	}

	/**
	 * @param projectName
	 */
	public static void updateEnzymesAnnotationView(String projectName) {

		AIBenchUtils.updateView(projectName, EnzymesAnnotationDataInterface.class);
	}


	/**
	 * @param projectName
	 */
	public static void updateCompartmentsAnnotationView(String projectName) {

		AIBenchUtils.updateView(projectName, CompartmentsAnnotationDataContainer.class);
	}

	/**
	 * @param projectName
	 */
	public static void updateAnnotationViews(String projectName) {

		AIBenchUtils.updateView(projectName, EnzymesAnnotationDataInterface.class);
		AIBenchUtils.updateView(projectName, TransportersAnnotationDataContainer.class);
		AIBenchUtils.updateView(projectName, CompartmentsAnnotationDataContainer.class);
	}

	/**
	 * @param projectName
	 */
	public static void updateTransportersAnnotationView(String projectName) {

		AIBenchUtils.updateView(projectName, TransportersAnnotationDataContainer.class);
	}

	/**
	 * @param projectName
	 */
	public static void updateProjectView(String projectName) {

		AIBenchUtils.updateView(projectName, Project.class);
	}
	/**
	 * @param projectName
	 */
	public static void updateGeneView(String projectName) {

		AIBenchUtils.updateView(projectName, Genes.class);
	}

	/**
	 * @param projectName
	 */
	public static void updateProteinView(String projectName) {

		AIBenchUtils.updateView(projectName, Proteins.class);
	}


	/**
	 * @param projectName
	 */
	public static void updateEnzymeView(String projectName) {

		AIBenchUtils.updateView(projectName, EnzymesContainer.class);
	}

	/**
	 * @param projectName
	 */
	public static void updateReactionsView(String projectName) {

		AIBenchUtils.updateView(projectName, ReactionsInterface.class);
	}

	/**
	 * @param projectName
	 */
	public static void updateReagentProductsView(String projectName) {

		AIBenchUtils.updateView(projectName, ReactantsProducts.class);
	}


	/**
	 * @param projectName
	 */
	public static void updateEntity(String projectName) {

		AIBenchUtils.updateView(projectName, Entity.class);
	}

	/**
	 * @param remoteExceptionTrials
	 * @return
	 * @throws Exception
	 */
	public static int getOrganismTaxonomyCount(long taxonomy_id) throws Exception {

		TaxonomyContainer result = NcbiAPI.getTaxonomyFromNCBI(taxonomy_id, 3);
		return result.getTaxonomy().size()+1;
	}

	/**
	 * 
	 * Scroll to center of table
	 * 
	 * 
	 * @param table
	 * @param rowIndex
	 * @param vColIndex
	 */
	public static void scrollToCenter(JTable table, int rowIndex, int vColIndex) {
		if (!(table.getParent() instanceof JViewport)) {
			return;
		}
		JViewport viewport = (JViewport) table.getParent();
		Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);
		Rectangle viewRect = viewport.getViewRect();
		rect.setLocation(rect.x - viewRect.x, rect.y - viewRect.y);

		int centerX = (viewRect.width - rect.width) / 2;
		int centerY = (viewRect.height - rect.height) / 2;
		if (rect.x < centerX) {
			centerX = -centerX;
		}
		if (rect.y < centerY) {
			centerY = -centerY;
		}
		rect.translate(centerX, centerY);
		viewport.scrollRectToVisible(rect);
	}


	/**
	 * @param map
	 * @param msqlmt
	 * @return
	 * @throws SQLException
	 */
	public static Map<String, String> parseGeneLocusTag(Map<String, String> map, DatabaseAccess msqlmt) throws SQLException {

		Connection connection = new Connection(msqlmt);

		Statement stmt = connection.createStatement();

		Set<String> res = new HashSet<>();

		ArrayList<String[]> result = ModelAPI.getGeneIdLocusTag(stmt);
		String[] list;

		for(int i=0; i < result.size(); i++) {
			list = result.get(i);
			res.add(list[1]);
		}

		for(String id : map.keySet()) {

			String locus = map.get(id);

			if(!res.contains(locus))
				for(String newLocus : res)
					if(newLocus.replace("_", "").equalsIgnoreCase(locus.replace("_", ""))) {

						map.put(id, newLocus);
					}
		}
		stmt=null;
		connection.closeConnection();
		return map;
	}

	/**
	 * @param unAnnotated
	 * @param initialSize
	 * @param location
	 * @return
	 */
	public static boolean unAnnotatedTransporters(float unAnnotated, float initialSize, String location){

		float percentage = (unAnnotated/initialSize)*100;
		int i =CustomGUI.stopQuestion("TCDB Unannoted transporters",
				percentage+"% of the TCDB genes identified as homolgues of the genome being studied are currently not annotated. Do you wish to continue?",
				new String[]{"Continue", "Abort", "Info"});

		String slash = "/", newSlash = "/";

		if(System.getProperty("os.name").toLowerCase().contains("windows"))
			newSlash = "\\";

		if(i<2) {

			switch (i)
			{
			case 0:return true;
			case 1: return false;
			default:return true;
			}
		}
		else {

			Workbench.getInstance().info("This genome has homology with "+Math.round(initialSize)+" TCDB genes. However, "+Math.round(unAnnotated)+" of the "+Math.round(initialSize)+" genes\n" +
					"are not annotated in merlin's current database.\n" +
					"If you continue, merlin will generate transport reactions with the current database.\n" +
					"However, if you want to generate transport reactions for the whole genome, you\n" +
					"may annotate the missing TCDB entries, filling in the following columns:" +
					"\n\t-direction\n\t-metabolite\n\t-reversibility\n\t-reacting_metabolites\n\t-equation\n" +
					"in the file located at :\n"+location.replace("plugins_bin\\merlin_core","").replace("plugins_bin/merlin_core","").replace("/../..","").replace(slash,newSlash)+
					"\naccording to the example.\n" +
					"Afterwards go to Transporters> New Transporters Loading and select the file.\n" +
					"Please send your file to support@merlin-sysbio.org prior to submission, to check your data.");
			return unAnnotatedTransporters( unAnnotated, initialSize, location);
		}
	}


	/**
	 * Round a number to decimal places.
	 * This breaks down badly in corner cases with either a very high number of decimal places (e.g. round(1000.0d, 17)) or large integer part (e.g. round(90080070060.1d, 9)
	 * 
	 * @param value
	 * @param places
	 * @return
	 */
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

}
