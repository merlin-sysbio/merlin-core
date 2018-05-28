package pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic;

import java.io.Serializable;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;

@Datatype(structure=Structure.SIMPLE,namingMethod="getName")
public class CompoundsReactions extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, String> names;
	private Connection connection;

	public CompoundsReactions(Table table, String name) {

		super(table, name);
		this.connection=table.getConnection();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		Set<String> compoundsNumber = new HashSet<String>();
		Set<String> compartmentsNumber = new HashSet<String>();
		Set<String> reactantsNumber = new HashSet<String>();
		Set<String> productsNumber = new HashSet<String>();

		String[][] res = new String[4][];
		Statement stmt;
		try {

			stmt = this.connection.createStatement();
			
			ArrayList<String[]> result = ProjectAPI.getCompoundStats(this.table.getName(), stmt);
			
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				compoundsNumber.add(list[2]);
				compartmentsNumber.add(list[3]);

				if(list[4].startsWith("-")) {

					reactantsNumber.add(list[2]);
				}
				else { 

					productsNumber.add(list[2]);
				}
			}

			res[0] = new String[] {"Number of distinct compounds involved in reactions", ""+compoundsNumber.size()};
			res[1] = new String[] {"              Number of distinct reactants", ""+reactantsNumber.size()};
			res[2] = new String[] {"              Number of distinct products", ""+productsNumber.size()};
			res[3] = new String[] {"Number of compartments", ""+compartmentsNumber.size()};

			stmt.close();

		}
		catch(Exception e){e.printStackTrace();}
		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getData()
	 */
	public GenericDataTable getData() {

		this.names = new HashMap<String, String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("info");
		columnsNames.add("compound name");
		columnsNames.add("formula");
		columnsNames.add("KEGG ID");
		columnsNames.add("number of reactions");

		List<String> index = new ArrayList<>();
		Map<String, String> indexMap = new HashMap<>();
		HashMap<String,String[]> qls = new HashMap<String,String[]>();

		GenericDataTable res = new GenericDataTable(columnsNames, "compound", "compound" ){

			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col){
			
				if (col==0)
					return true;
				else 
					return false;
				}
			};

				try {
					
					Statement stmt = this.connection.createStatement();

					ArrayList<String[]> result = ProjectAPI.getCompoundInformation2(stmt);
					
					for(int i=0; i<result.size(); i++){
						String[] list = result.get(i);

						String[] ql = new String[4];
						ql[0] = list[2];
						ql[1] = list[0];
						ql[2] = list[1];
						ql[3] = list[3];

						//if(rs.getString(1)==(null)) {
							indexMap.put(list[3], list[4]);
							index.add(list[4]);
							qls.put(list[4], ql);
						//}
						//else {
//
						//	index.add(rs.getString("idcompound"));
						//	qls.put(rs.getString("idcompound"), ql);
						//}
					}
					
					result = ProjectAPI.getCompoundWithBiologicalRoles(stmt);

					for(int i=0; i<result.size(); i++){
						String[] list = result.get(i);

						String[] ql = new String[4];
						ql[0] = "0";
						ql[1] = list[1];
						ql[2] = list[5];
						ql[3] = list[3];

						indexMap.put(list[3], list[0]);
						index.add(list[0]);
						qls.put(list[0], ql);
					}
					
					List <String> sorter = new ArrayList<>(indexMap.keySet());
					Collections.sort(sorter);
					
					for(int i=0;i<sorter.size();i++) {

						//String j = index.get(i);
						String j = indexMap.get(sorter.get(i));
						
						List<Object> ql = new ArrayList<Object>();
						String[] cr = qls.get(j);
						ql.add("");
						ql.add(cr[1]); //compound name
						ql.add(cr[2]);// compound formula
						ql.add(cr[3]);// compound formula
						ql.add(cr[0]); //reactions
						res.addLine(ql, j);
						this.names.put(j, cr[1]);
					}
					stmt.close();

				}
				catch(Exception e){e.printStackTrace();}

				return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	public DataTable[] getRowInfo(String id) {

		DataTable[] res = new DataTable[3];
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Entry Type");
		res[0] = new DataTable(columnsNames, "entry type");

		columnsNames = new ArrayList<String>();
		columnsNames.add("Names");
		res[1] = new DataTable(columnsNames, "synonyms");

		try {

			Statement	stmt = this.connection.createStatement();
			ArrayList<String> result = ProjectAPI.getEntryType(id, stmt);

			for(int i=0; i<result.size(); i++)
			{
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(result.get(i));
				res[0].addLine(ql);
			}
			
			result = ProjectAPI.getAliasClassC(id, stmt);

			for(int i=0; i<result.size(); i++)
			{
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(result.get(i));
				res[1].addLine(ql);
			}

			columnsNames = new ArrayList<String>();
			columnsNames.add("reaction ");
			columnsNames.add("equation");
			res[2] = new DataTable(columnsNames, "reactions");

			ArrayList<String[]> data = ProjectAPI.getCompoundReactions(id, stmt);
			
			for(int i=0; i<data.size(); i++)
			{
				String[] list = data.get(i);
				
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[2]);
				ql.add(list[3]);
				res[2].addLine(ql);
			}
			stmt.close();
		}
		catch(Exception e)
		{e.printStackTrace();}

		return res;
	}

	public HashMap<Integer,Integer[]> getSearchData()
	{
		HashMap<Integer,Integer[]> res = new HashMap<Integer,Integer[]>();

		res.put(new Integer(0), new Integer[]{new Integer(0)});

		return res;
	}

	public String[] getSearchDataIds()
	{
		String[] res = new String[]{"name"};


		return res;
	}

	public boolean hasWindow()
	{
		return true;
	}

	public String getName() {
		
		return "compounds";
	}
	
	public String getWindowName() {
		
		return "compound";
	}

}