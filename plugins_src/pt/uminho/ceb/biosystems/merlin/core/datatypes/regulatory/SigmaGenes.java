package pt.uminho.ceb.biosystems.merlin.core.datatypes.regulatory;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.TreeMap;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Genes;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

@Datatype(structure = Structure.LIST,namingMethod="getName")
public class SigmaGenes extends Genes implements Serializable {

	private static final long serialVersionUID = 9008699561446234481L;
	private TreeMap<String,String> names;

	public SigmaGenes(Table dbt, String name, TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy)
	{
		super(dbt, name, ultimlyComplexComposedBy);
	}

	public String[][] getStats()
	{

		String[][] res = new String[5][];
		try{
			Statement stmt = super.table.getConnection().createStatement();
			
			Integer[] list = ProjectAPI.getSigmaGenesStats(stmt);
			
			int num=list[0];
			int noseq=list[1];
			int noname=list[2];
			int nobnumber=list[3];
			int nboolean_rule=list[4];

			res[0] = new String[] {"Number of sigma genes", ""+num};
			res[1] = new String[] {"Number of sigma genes with no name associated", ""+noname};
			res[2] = new String[] {"Number of sigma genes with no sequence associated", ""+noseq};
			res[3] = new String[] {"Number of sigma genes with no bnumber associated", ""+nobnumber};
			res[4] = new String[] {"Number of sigma genes with no boolean rule associated", ""+nboolean_rule};

		} catch(Exception e)
		{e.printStackTrace();}
		return res;
	}

	public GenericDataTable getAllGenes()
	{
		names = new TreeMap<String,String>();

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Names");
		columnsNames.add("Bnumbers");

		GenericDataTable qrt = new GenericDataTable(columnsNames, "Genes", "Gene data");

		try
		{
			Statement stmt = super.table.getConnection().createStatement();

			ArrayList<String[]> result = ProjectAPI.getAllGenes(stmt);

			/* bnumber, */

			for (int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				ArrayList<Object> ql = new ArrayList<Object>();
				for(int j=1;j<=3;j++)
				{
					String in = list[j];

					if(in!=null) ql.add(in);
					else ql.add("");
				}
				qrt.addLine(ql, list[0]);
				names.put(list[0], list[1]);
			}
			stmt.close();
		} 
		catch (SQLException ex)
		{
			// handle any errors
			System.out.println("SQLException: " + ex.getMessage());
			System.out.println("SQLState: " + ex.getSQLState());
			System.out.println("VendorError: " + ex.getErrorCode());
		}
		return qrt;
	}
}