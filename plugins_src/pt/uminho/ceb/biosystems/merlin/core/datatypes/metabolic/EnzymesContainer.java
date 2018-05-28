package pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

/**
 * @author ODias
 *
 */
@Datatype(structure= Structure.SIMPLE, namingMethod="getName",removable=true,removeMethod ="remove")
public class EnzymesContainer extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, String> names;
	private Connection connection;

	/**
	 * @param table
	 * @param name
	 * @param ultimlyComplexComposedBy
	 */
	public EnzymesContainer(Table table, String name, TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy) {

		super(table, name);
		this.connection=table.getConnection();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		String[][] res = new String[14][];

		try {

			Statement stmt = this.connection.createStatement();

			String originalReaction = "";
			if(this.getProject().isCompartmentalisedModel()) {

				originalReaction = originalReaction.concat(" WHERE NOT originalReaction ");
			}
			else {

				originalReaction = originalReaction.concat(" WHERE originalReaction ");
			}

			ArrayList<Integer> result = ModelAPI.getEnzymesStats(originalReaction, stmt);
			
			//array structure: [enz_num, hom_num, kegg_num, man_num, trans_num, encoded_enz, encoded_hom, encoded_kegg, encoded_man, encoded_trans]

			res[0] = new String[] {"Total number of enzymes", ""+result.get(0)};
			res[1] = new String[] {"      From homology", ""+result.get(1)};
			res[2] = new String[] {"      From KEGG", ""+result.get(2)};
			res[3] = new String[] {"      Added manually", ""+result.get(3)};
			res[4] = new String[] {"Total number of encoded enzymes", ""+result.get(5)};
			res[5] = new String[] {"      From homology", ""+result.get(6)};
			res[6] = new String[] {"      From KEGG", ""+result.get(7)};
			res[7] = new String[] {"       Added manually", ""+result.get(8)};
			res[9] = new String[] {"Total number of transporters", ""+result.get(4)};
			res[10] = new String[] {"Total number of encoded transporters", ""+result.get(9)};
			res[12] = new String[] {"Total number of proteins", ""+(result.get(0)+result.get(4))};
			res[13] = new String[] {"Total number of encoded proteins", ""+(result.get(5)+result.get(9))};

			stmt.close();
		}
		catch(Exception e){e.printStackTrace();}
		return res;
	}

	/**
	 * @param encoded
	 * @return
	 */
	public GenericDataTable getAllEnzymes(boolean encoded) {

		this.names = new HashMap<String, String>();
		List<String> columnsNames = new ArrayList<String>();

		columnsNames.add("info");
		columnsNames.add("names");
		columnsNames.add("identifier");
		columnsNames.add("number of reactions");
		columnsNames.add("source");
		columnsNames.add("encoded in Genome");
		columnsNames.add("catalysing reactions in model");


		GenericDataTable enzymeDataTable = new GenericDataTable(columnsNames, "Enzymes",""){
			private static final long serialVersionUID = 8668268767599264758L;
			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0){return true;}
				else {return false;}
			}
		};


		try {

			Statement stmt = this.connection.createStatement();
			
			ArrayList<String[]> result = ModelAPI.getAllEnzymes(this.getProject().isCompartmentalisedModel(), encoded, stmt);

			for(int index=0; index<result.size(); index++){
				String[] list = result.get(index);

				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");

				for(int i=0;i<6;i++) {

					if(i>3 && i<6) {

						if(i==5) {

							if(Boolean.valueOf(list[i])==false && Integer.parseInt(list[7])==1) {

								ql.add(false);
							}
							else {

								ql.add(true);
							}

						}
						else {

							ql.add(Boolean.valueOf(list[i]));
						}
					}
					else {

						String aux = list[i];

						if(aux!=null) 
							ql.add(aux);
						else 
							ql.add("");	
					}
				}
				enzymeDataTable.addLine(ql,list[6]);
				this.names.put(list[6], list[0]);
			}
			stmt.close();
		}
		catch (SQLException e) {

			e.printStackTrace();
		}

		return enzymeDataTable;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getData()
	 */
	public GenericDataTable getData() {

		this.names = new HashMap<String, String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		ArrayList<String> index = new ArrayList<String>();
		HashMap<String,String[]> qls = new HashMap<String,String[]>();

		columnsNames.add("Names");
		columnsNames.add("ECnumber");
		columnsNames.add("Optimal pH");
		columnsNames.add("Post translational modification");
		columnsNames.add("Number of coding genes");

		GenericDataTable res = new GenericDataTable(columnsNames, "TUs", "TU");

		try {

			Statement stmt = this.connection.createStatement();
			
			ArrayList<String[]> result = ModelAPI.getProteinsData(stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				String[] ql = new String[5];
				if(list[1]!=null) ql[0] = list[1];
				else ql[0] = "";
				if(list[2]!=null) ql[1] = list[2];
				else ql[1] = "";
				if(list[3]!=null) ql[2] = list[3];
				else ql[2] = "";
				if(list[4]!=null) ql[3] = list[4];
				else ql[3] = "";
				ql[4] = "0";
				index.add(list[0]);
				qls.put(list[0], ql);
			}
			
			result = ModelAPI.getProteinsData2(stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				qls.get(list[0])[4] = list[1];
			}

			for(int i=0;i<index.size();i++) {

				List<Object> ql = new ArrayList<Object>();
				String[] enzymeData = qls.get(index.get(i));
				ql.add(enzymeData[0]);
				ql.add(enzymeData[1]);
				ql.add(enzymeData[2]);
				ql.add(enzymeData[3]);
				ql.add(enzymeData[4]);
				res.addLine(ql, index.get(i));
				this.names.put(index.get(i), enzymeData[0]);
			}
			stmt.close();

		}
		catch(Exception e){e.printStackTrace();}

		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getSearchData()
	 */
	public HashMap<Integer,Integer[]> getSearchData() {

		HashMap<Integer,Integer[]> res = new HashMap<Integer,Integer[]>();
		res.put(new Integer(0), new Integer[]{new Integer(0)});
		res.put(new Integer(1), new Integer[]{new Integer(1)});

		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getSearchDataIds()
	 */
	public String[] getSearchDataIds() {

		String[] res = new String[]{"Name", "ECnumber"};

		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#hasWindow()
	 */
	public boolean hasWindow() {

		return true;
	}

	/**
	 * @param ecnumber
	 * @param id
	 * @return
	 */
	public DataTable[] getRowInfo(String ecnumber, String id) {

		//String id = this.index.get(Integer.parseInt(row));
		DataTable[] datatables = new DataTable[6];
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Reaction");
		columnsNames.add("Equation");
		columnsNames.add("Source");
		columnsNames.add("in Model");
		columnsNames.add("Reversible");
		datatables[0] = new DataTable(columnsNames, "Encoded Reactions");

		columnsNames = new ArrayList<String>();
		columnsNames.add("Name");
		columnsNames.add("Locus tag");
		columnsNames.add("KO");
		columnsNames.add("Origin");
		columnsNames.add("Notes");
		columnsNames.add("Similarity");
		columnsNames.add("Orthologue");
		datatables[1] = new DataTable(columnsNames, "Encoding genes");	

		columnsNames = new ArrayList<String>();
		columnsNames.add("GPR status");
		columnsNames.add("Reaction");
		columnsNames.add("Rule");
		columnsNames.add("Module Name");
		datatables[2] = new DataTable(columnsNames, "Gene-Protein-Reaction");	

		columnsNames = new ArrayList<String>();
		columnsNames.add("Pathway ID");
		columnsNames.add("Pathway Name");
		datatables[3] = new DataTable(columnsNames, "Pathways");	

		columnsNames = new ArrayList<String>();
		columnsNames.add("Synonyms");
		datatables[4] = new DataTable(columnsNames, "Synonyms");

		columnsNames = new ArrayList<String>();
		columnsNames.add("Locus tag");
		columnsNames.add("Compartment");
		columnsNames.add("Score");
		columnsNames.add("Primary Location");
		datatables[5] = new DataTable(columnsNames, "Compartments");

		try {

			Statement stmt = this.connection.createStatement();
			String aux = "";
			if(this.getProject().isCompartmentalisedModel())
				aux = aux.concat(" AND NOT originalReaction ");
			else
				aux = aux.concat(" AND originalReaction ");

			ArrayList<String[]> result = ModelAPI.getReactionsData(ecnumber, aux, id, stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[0]);
				ql.add(list[1]);
				ql.add(list[2]);

				if(Boolean.valueOf(list[3]))					
					ql.add("true");
				else					
					ql.add("-");

				if(Boolean.valueOf(list[4]))					
					ql.add("true");
				else					
					ql.add("-");

				datatables[0].addLine(ql);
			}

			result = ModelAPI.getGeneData(ecnumber, id, stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[0]);
				ql.add(list[1]);
				ql.add(list[2]);
				ql.add(list[3]);
				ql.add(list[4]);
				ql.add(list[5]);
				ql.add(list[6]);
				datatables[1].addLine(ql);
			}

			result = ModelAPI.getPathways(ecnumber, id, stmt);
			
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[0]);
				ql.add(list[1]);
				datatables[3].addLine(ql);
			}

			result = ModelAPI.getDataFromSubunit(ecnumber, id, stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[0]);
				ql.add(list[1]);
				ql.add(list[2]);
				ql.add(list[3]);
				
				datatables[2].addLine(ql);
			}

			ArrayList<String> data = ProjectAPI.getAliasClassP(id, stmt);

			for(int i=0; i<data.size(); i++){

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(data.get(i));
				datatables[4].addLine(ql);
			}

			result = ModelAPI.getGeneData2(ecnumber, id, stmt);


			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[4]);
				ql.add(list[1]);
				ql.add(list[3]);

				if(Boolean.valueOf(list[2]))
					ql.add(list[2]);
				else
					ql.add("");

				datatables[5].addLine(ql);
			}

			stmt.close();

		}
		catch(Exception e) {

			e.printStackTrace();
		}

		return datatables;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getName(java.lang.String)
	 */
	public String getName(String id) {

		return this.names.get(id);
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getSingular()
	 */
	public String getSingular() {

		return "enzyme: ";
	}
}
