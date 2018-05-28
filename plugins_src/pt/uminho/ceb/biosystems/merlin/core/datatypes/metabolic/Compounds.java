package pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic;

import java.io.Serializable;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

/**
 * @author ODias
 *
 */
@Datatype(structure=Structure.SIMPLE,namingMethod="getName",removable=true,removeMethod ="remove")
public class Compounds extends Entity implements Serializable {

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
	public Compounds(Table table, String name) {
		
		super(table, name);
		this.connection=table.getConnection();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {
		int num=0;
		int noname=0;
		int noinchi=0;
		//int nocrn=0;
		int noformula=0;
		//int nosmiles=0;
		int glycans=0;
		int compounds=0;

		String[][] res = new String[9][];
		try {
			
			Statement stmt = this.connection.createStatement();
			
			ArrayList<String[]> data = ProjectAPI.getAllFromCompound(this.table.getName(), stmt);

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);
				
				num++;
				if(list[1]==null) noname++;
				if(list[2]==null) noinchi++;
				if(list[3].equals("glycan")){glycans++;}
				else{compounds++;}
				//if(rs.getString(5)==null) nocrn++;
				if(list[4]==null) noformula++;
				//if(rs.getString(11)==null) nosmiles++;
			}

			res[0] = new String[] {"Number of metabolites", ""+num};
			res[1] = new String[] {"Number of metabolites with no name associated", ""+noname};
			//res[2] = new String[] {"Number of compounds with no " +
			//	"CAS registry number associated", 
			//	""+nocrn};
			res[2] = new String[] {"Number of metabolites with no " +
					"formula associated", ""+noformula};
			res[3] = new String[] {"Number of metabolites with no " +
					"InChi associated", ""+noinchi};
			res[4] = new String[] {"Number of compounds", ""+compounds};
			res[5] = new String[] {"Number of glycan", ""+glycans};
			//res[5] = new String[] {"Number of compounds with no " +
			//	"smilies associated", ""+nosmiles};

			data = ProjectAPI.getCompoundIDsFromStoichiometry(stmt);

			LinkedList<String> reagents = new LinkedList<String>();
			LinkedList<String> products = new LinkedList<String>();
			LinkedList<String> metabolitesInReaction = new LinkedList<String>();

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);
				
				if(!metabolitesInReaction.contains(list[0])) 
					metabolitesInReaction.add(list[0]);

				//if((new Double(rs.getString(2))).doubleValue()>0)
				if(list[1].toString().startsWith("-")) {
					
					if(!products.contains(list[0])) products.add(list[0]);
				}
				else {
					
					if(!reagents.contains(list[0])) reagents.add(list[0]);
				}
			}

			res[6] = new String[] {"Number of metabolites that participate in reactions",""+metabolitesInReaction.size()};

			res[7] = new String[] {"Number of metabolites that are consumed in reactions",""+reagents.size()};

			res[8] = new String[] {"Number of metabolites that are produced in reactions",""+products.size()};

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
		columnsNames.add("names");
		columnsNames.add("formula");
		columnsNames.add("KEGG ID");
		columnsNames.add("type");
		//columnsNames.add("InChi");

		GenericDataTable res = new GenericDataTable(columnsNames, "Promoter", "Compound") {
			
			private static final long serialVersionUID = 1153164566285176L;

			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}
		};

		try {
			
			Statement stmt = this.connection.createStatement();
			
			ArrayList<String[]> data = ProjectAPI.getCompoundInformation(this.table.getName(), stmt);

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);
				
				List<Object> ql = new ArrayList<Object>();

				ql.add("");
				ql.add(list[1]);
				ql.add(list[5]);
				ql.add(list[3]);
				ql.add(list[4]);
				res.addLine(ql, list[0]);

				if( list[1]==(null)) {
					
					this.names.put(list[0], list[2]);
				}
				else {
					
					this.names.put( list[0], list[1]);
				}
			}
			stmt.close();
		} 
		catch(Exception e) {
			
			e.printStackTrace();
		}

		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getSearchData()
	 */
	public HashMap<Integer,Integer[]> getSearchData() {
		
		HashMap<Integer,Integer[]> res = new HashMap<Integer,Integer[]>();

		res.put(new Integer(0), new Integer[]{new Integer(0)});
		res.put(new Integer(1), new Integer[]{new Integer(1)});
		res.put(new Integer(2), new Integer[]{new Integer(2)});
		res.put(new Integer(3), new Integer[]{new Integer(3)});
		res.put(new Integer(4), new Integer[]{new Integer(4)});

		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getSearchDataIds()
	 */
	public String[] getSearchDataIds() {
		
		//String[] res = new String[]{"Name" , "CAS registry number", "Formula",
		//	"InChi", "Smilies"};

		String[] res = new String[]{"Name" , "InChi", "Formula",
		};

		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	public DataTable[] getRowInfo(String id) {
		
		DataTable[] res = new DataTable[2];
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("entry Type");
		res[0] = new DataTable(columnsNames, "Entry type");
		
		columnsNames = new ArrayList<String>();
		columnsNames.add("names");
		res[1] = new DataTable(columnsNames, "Synonyms");

		try {
			
			Statement stmt = this.connection.createStatement();

			ArrayList<String> data = ProjectAPI.getAliasClassC(id, stmt);
			
			for(int i=0; i<data.size(); i++){
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(data.get(i));
				res[1].addLine(ql);
			}
			
			data = ProjectAPI.getEntryType(id, stmt);
			
			for(int i=0; i<data.size(); i++){
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(data.get(i));
				res[0].addLine(ql);
			}
			stmt.close();
		}
		catch(Exception e)
		{e.printStackTrace();}
		return res;
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
		
		return this.names.get(id).toLowerCase();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getSingular()
	 */
	public String getSingular() {
		
		return "compound: ";
	}
}
