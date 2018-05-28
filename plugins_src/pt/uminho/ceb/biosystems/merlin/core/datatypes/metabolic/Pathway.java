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
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;


/**
 * @author ODias
 *
 */
@Datatype(structure= Structure.SIMPLE, namingMethod="getName",removable=true,removeMethod ="remove")
public class Pathway extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, String> names;
	private Connection connection;
	
	/**
	 * @param dbt
	 * @param name
	 */
	public Pathway(Table dbt, String name) {
		
		super(dbt, name);
		this.connection=dbt.getConnection();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {
		
		int num=0;
		int noname=0;
		int nosbml=0;

		String[][] res = new String[3][];
		
		try {

			Statement stmt = this.connection.createStatement();
			
			ArrayList<String[]> result = ProjectAPI.getAllFromPathWay(this.table.getName(), stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
						
				num++;
				if(list[1]==null) noname++;
				if(list[2]==null) nosbml++;
			}

			res[0] = new String[] {"Number of pathways", ""+num};
			res[1] = new String[] {"Number of pathways with no name associated", ""+noname};
			res[2] = new String[] {"Number of pathways with no SBML file associated",
					""+nosbml};
			stmt.close();

		} 
		catch(Exception e) {
			e.printStackTrace();}
		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getData()
	 */
	public GenericDataTable getData() {

		this.names = new HashMap<String, String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("info");
		columnsNames.add("code");
		columnsNames.add("name");
		columnsNames.add("number of reactions");
		columnsNames.add("number of enzymes");

		ArrayList<String> index = new ArrayList<String>();
		HashMap<String,String[]> qls = new HashMap<String,String[]>();

		GenericDataTable res = new GenericDataTable(columnsNames, "Promoter", "Pathway"){
			private static final long serialVersionUID = 1236477181642906433L;

			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}};

			try
			{
				//MySQLMultiThread dsa =  new MySQLMultiThread( host, port, dbName, usr, pwd);
				Statement stmt = this.connection.createStatement();
				
				ArrayList<String[]> result = ProjectAPI.getPathwayID(stmt);

				for(int i=0; i<result.size(); i++){
					String[] list = result.get(i);
					
					String[] ql = new String[4];
					ql[0] = list[1];
					ql[1] = list[2];
					ql[2] = "0";
					ql[3] = "0";
					index.add(list[0]);
					qls.put(list[0], ql);
				}

				qls = ProjectAPI.countReactionsByPathwayID(qls, stmt);

				qls = ProjectAPI.countProteinIdByPathwayID(qls, stmt);

				for(int i=0;i<index.size();i++) {
					
					List<Object> ql = new ArrayList<Object>();
					String[] gark = qls.get(index.get(i));
					ql.add("");
					ql.add(gark[0]);
					ql.add(gark[1]);
					ql.add(gark[2]);
					ql.add(gark[3]);
					res.addLine(ql, index.get(i));
					this.names.put(index.get(i), gark[0]);
				}
				stmt.close();
			
			}
			catch(Exception e)
			{e.printStackTrace();}

			return res;
	}

	public HashMap<Integer,Integer[]> getSearchData() {
		
		HashMap<Integer,Integer[]> res = new HashMap<Integer,Integer[]>();

		res.put(Integer.valueOf(0), new Integer[]{Integer.valueOf(0)});

		return res;
	}

	public String[] getSearchDataIds() {
		
		String[] res = new String[]{"Name"};


		return res;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#hasWindow()
	 */
	public boolean hasWindow() {
		return true;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	public DataTable[] getRowInfo(String id) {
		
		DataTable[] res = new DataTable[2];
		ArrayList<String> columnsNames = new ArrayList<String>();
		columnsNames.add("Reactions");
		columnsNames.add("Equation");
		DataTable qrt = new DataTable(columnsNames, "Reactions");
		columnsNames = new ArrayList<String>();
		columnsNames.add("Enzymes");
		columnsNames.add("Protein name");
		columnsNames.add("Class");
		DataTable qrt2 = new DataTable(columnsNames, "Enzymes");
		res[0] = qrt;
		res[1] = qrt2;
		try
		{
			//MySQLMultiThread this.connection =  new MySQLMultiThread( host, port, dbName, usr, pwd);
			Statement stmt = this.connection.createStatement();
			
			ArrayList<String[]> result = ProjectAPI.countReactions(id, stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[1]);
				ql.add(list[2]);
				qrt.addLine(ql);
			}

			//stmt = dsa.createStatement();
	
			result = ProjectAPI.getDataFromEnzyme(id, stmt);
			
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[0]);
				ql.add(list[1]);
				ql.add(list[2]);
				ql.add(list[3]);
				
				qrt2.addLine(ql);
			}
			stmt.close();
		
		}
		catch(Exception e)
		{e.printStackTrace();}

		return res;
	}

	public String getName(String id)
	{
		return this.names.get(id);
	}

	public String getSingular()
	{
		return "pathway: ";
	}
}
