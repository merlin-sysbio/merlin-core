package pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation;

/**
 * @author Antonio Dias
 *
 */
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.CompartmentsTool;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.CompartmentsAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.HomologyAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.CompartmentResult;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.CompartmentsInterface;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.GeneCompartments;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.ReadLocTree;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.ReadPSort3;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.WoLFPSORT;
import pt.uminho.ceb.biosystems.merlin.transporters.core.utils.Enumerators.KINGDOM;

@Datatype(structure= Structure.SIMPLE, namingMethod="getName",removable=true,removeMethod ="remove")
public class CompartmentsAnnotationDataContainer extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TreeMap<Integer,String> ids;
	private double threshold;
	private Map<Integer, String> names;

	/**
	 * @param dbt
	 * @param name
	 */
	public CompartmentsAnnotationDataContainer(Table dbt, String name) {

		super(dbt, name);
		this.connection=dbt.getConnection();
		this.names = new HashMap<>();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getName()
	 */
	public String getName() {

		return "compartments";
	}


	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {
		
		String[][] res = new String[2][];

		int num = 0, num_comp = 0;

		try {
			Statement stmt = this.connection.createStatement();
			
			stmt = HomologyAPI.checkStatement(this.getProject().getDatabase().getDatabaseAccess(), stmt);

			num = HomologyAPI.getNumberOfGenes(this.table.getName(), stmt);
			
			num_comp = CompartmentsAPI.getNumberOfCompartments(stmt);

			res[0] = new String[] {"Number of genes", ""+num};
			res[1] = new String[] {"Number of distinct compartments ", ""+num_comp};
			
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;

	}

	/**
	 * @return
	 */
	public GenericDataTable getInfo() {

		ids = new TreeMap<Integer,String>();

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("info");
		columnsNames.add("genes");
		columnsNames.add("primary compartment");
		columnsNames.add("score");
		columnsNames.add("secondary compartments");
		columnsNames.add("scores");
//		columnsNames.add("Edit");

		GenericDataTable qrt = new GenericDataTable(columnsNames, "genes", "gene data"){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {

				if (col==0 || col==4) {

					return true;
				}
				else return false;
			}
		};

		try {
			Statement statement = this.connection.createStatement();

			Map<String, GeneCompartments> geneCompartments = runCompartmentsInterface(statement);

			if(geneCompartments != null){

				int g = 0;

				List<String> collection = new ArrayList<>(geneCompartments.keySet());

				Collections.sort(collection);

				Map<String, String> allLocusTag = CompartmentsAPI.getAllLocusTag(statement);

				for(String query : collection) {

					GeneCompartments geneCompartment = geneCompartments.get(query);
					String id = geneCompartment.getGeneID();
					
					ArrayList<Object> ql = new ArrayList<Object>();
					ql.add("");
					this.ids.put(g, id);

					String locusTag = allLocusTag.get(query);
					
					if(locusTag != null){
						this.names.put(g, locusTag);
						ql.add(locusTag);
					}
					else{
						this.names.put(g, query);
						ql.add(query);
					}
					ql.add(geneCompartment.getPrimary_location());
					double maxScore = geneCompartment.getPrimary_score()/100; 
					ql.add(MerlinUtils.round(maxScore,2)+"");

					String secondaryCompartments = "", secondaryScores = "";

					for(String key : geneCompartment.getSecondary_location().keySet()) {

						secondaryCompartments += key + ", ";
						secondaryScores += MerlinUtils.round(geneCompartment.getSecondary_location().get(key)/100, 2)+ ", ";
					}

					if (secondaryCompartments.length() != 0) {

						secondaryCompartments = secondaryCompartments.substring(0, secondaryCompartments.length()-2);
						secondaryScores = secondaryScores.substring(0, secondaryScores.length()-2);
					}

					ql.add(secondaryCompartments);
					ql.add(secondaryScores);

					//				ql.add("");
					qrt.addLine(ql, geneCompartment.getGeneID());

					g+=1;
				}
			}
			statement.close();
		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		return qrt;
	}

	public DataTable[] getRowInfo(String id) {

		DataTable[] results = new DataTable[1];

		List<String> columnsNames = new ArrayList<String>();
		columnsNames.add("compartment");
		columnsNames.add("score");
		results[0] = new DataTable(columnsNames, "compartments");

		Statement stmt;

		try {

			stmt = this.connection.createStatement();

			ArrayList<String[]> data = ProjectAPI.getRowInfo(id, stmt);

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);
				
				ArrayList<String> resultsList = new ArrayList<>();

				resultsList.add(list[0]);
				Double score = Double.parseDouble(list[1])/10;
				resultsList.add(score.toString());

				results[0].addLine(resultsList);
			}
			stmt.close();
		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return results;
	}

	/**
	 * @param project
	 * @param results 
	 * @param threshold
	 * @param cTool
	 * @param statement
	 */
	public static void loadPredictions(Project project, String tool, Map<String, CompartmentResult> results, Statement statement) {

		try {

			boolean type = false;
			String kg = project.getOrganismLineage().split(";")[0];
			KINGDOM kingdom = KINGDOM.valueOf(kg);
			if (kg.contains("Viridiplantae"))
				type = true;
			CompartmentsInterface compartmentsInterface = null;

			boolean go=false;

			if(kingdom.equals(KINGDOM.Eukaryota)) {

				compartmentsInterface = new ReadLocTree();
				((ReadLocTree) compartmentsInterface).setPlant(type);

				if(project.areCompartmentsPredicted())
					go = false;
				else
					go = compartmentsInterface.getCompartments(null);
			}
			else {
				CompartmentsTool compartmentsTool = CompartmentsTool.valueOf(tool);
				if(compartmentsTool.equals(CompartmentsTool.PSort))
					compartmentsInterface = new ReadPSort3();
				if(compartmentsTool.equals(CompartmentsTool.LocTree))
					compartmentsInterface = new ReadLocTree();
				if(compartmentsTool.equals(CompartmentsTool.WoLFPSORT))
					compartmentsInterface = new WoLFPSORT();

				if(project.areCompartmentsPredicted())
					go=false;
				else							
					go = compartmentsInterface.getCompartments(null);
			}
			
			if(go)
				compartmentsInterface.loadCompartmentsInformation(results, project.getProjectID(), statement);

		} 
		catch (Exception e1) {

			Workbench.getInstance().error("An error occurred while loading compartments prediction.");
			e1.printStackTrace();
		}
	}
	
	public Map<String, GeneCompartments> runCompartmentsInterface(Statement statement){
		
		Map<String, GeneCompartments> geneCompartments = null;
		
		try {
			
			HomologyAPI.checkStatement(this.getProject().getDatabase().getDatabaseAccess(), statement);
			
			CompartmentsInterface compartmentsInterface = null;
			String cTool = ProjectAPI.getCompartmentsTool(this.getProject().getProjectID(), statement);

			if(cTool!=null) {
				
				CompartmentsTool compartmentsTool = CompartmentsTool.valueOf(cTool);
				
				if(compartmentsTool.equals(CompartmentsTool.PSort))
					compartmentsInterface = new ReadPSort3();
				if(compartmentsTool.equals(CompartmentsTool.LocTree))
					compartmentsInterface = new ReadLocTree();
				if(compartmentsTool.equals(CompartmentsTool.WoLFPSORT))
					compartmentsInterface = new WoLFPSORT();
				
				geneCompartments = compartmentsInterface.getBestCompartmentsByGene(threshold, this.getProject().getProjectID(), statement);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return geneCompartments;
	}

	/**
	 * @param thold
	 */
	public void setThreshold(Double thold) {

		this.threshold = thold;
	}

	/**
	 * @param 
	 * @return
	 */
	public Double getThreshold() {

		return this.threshold;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getGeneName(String id) {
		
		return this.names.get(new Integer(id));
	}
}
