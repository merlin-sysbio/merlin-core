
package pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic;

import java.io.Serializable;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.TransportersAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;

/**
 * @author ODias
 *
 */
public class TransportersContainer extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HashMap<String, String> names;
	private Connection connection;

	/**
	 * @param table
	 * @param name
	 */
	public TransportersContainer(Table table, String name) {
		
		super(table, name);
		this.connection=table.getConnection();
	}
	
	
	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		String[][] res = new String[3][];
		try {
			
			Statement stmt = this.connection.createStatement();
			
			Integer[] result = TransportersAPI.getStats(this.table.getName(), stmt);
			
			res[0] = new String[] {"number of metabolites", ""+result[0]};
			res[1] = new String[] {"number of metabolites with no name associated", ""+ result[1]};
			res[4] = new String[] {"number of compounds", ""+ result[2]};

			//rs = stmt.executeQuery("SELECT compound_idcompound, stoichiometric_coefficient FROM stoichiometry");

			//LinkedList<String> reagents = new LinkedList<String>();

			//while(rs.next()) {
				
			//}
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

		columnsNames.add("Info");
		columnsNames.add("Genes");
		columnsNames.add("TC family");
		columnsNames.add("Number of encoded proteins");

		GenericDataTable res = new GenericDataTable(columnsNames, "Transporters", "Transport proteins encoding genes") {
			
			private static final long serialVersionUID = 1153164566285176L;

			@Override
			public boolean isCellEditable(int row, int col) {
				
				if (col==0) {
					
					return true;
				}
				else {
					
					return false;
				}
			}
		};

		try {
			
			Statement stmt = this.connection.createStatement();
			
			ArrayList<String[]> result = TransportersAPI.getSwTransportersData(stmt);
			
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				List<Object> ql = new ArrayList<Object>();

				ql.add("");
				ql.add(list[1]);
				ql.add(list[4]);
				ql.add(list[2]);
				ql.add(list[5]);
				res.addLine(ql, list[0]);

				if( list[1]==(null))
				{
					this.names.put(list[0], list[2]);
				}
				else
				{
					this.names.put(list[0], list[1]);
				}
			}
			stmt.close();
		} 
		catch(Exception e) {
			
			e.printStackTrace();
		}

		return res;
	
	}
	
	public HashMap<Integer,Integer[]> getSearchData() {
		
		HashMap<Integer,Integer[]> res = new HashMap<Integer,Integer[]>();

		res.put(new Integer(0), new Integer[]{new Integer(0)});
		res.put(new Integer(1), new Integer[]{new Integer(1)});
		res.put(new Integer(2), new Integer[]{new Integer(2)});
		res.put(new Integer(3), new Integer[]{new Integer(3)});
		res.put(new Integer(4), new Integer[]{new Integer(4)});

		return res;
	}
	
	public String[] getSearchDataIds() {
		
		//String[] res = new String[]{"Name" , "CAS registry number", "Formula",
		//	"InChi", "Smilies"};

		String[] res = new String[]{"name" , "InChi", "formula",
		};

		return res;
	}
	
	
	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	public DataTable[] getRowInfo(String id) {
		
		return null;
	}
	
	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#hasWindow()
	 */
	public boolean hasWindow() {
		
		return true;
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
		
		return "transport protein: ";
	}
}
