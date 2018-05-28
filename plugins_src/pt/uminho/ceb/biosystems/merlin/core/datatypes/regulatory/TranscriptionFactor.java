package pt.uminho.ceb.biosystems.merlin.core.datatypes.regulatory;

import java.io.Serializable;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;

public class TranscriptionFactor extends Entity implements Serializable {

	private static final long serialVersionUID = -5115896854054366023L;

	private HashMap<String, String> names;

	public TranscriptionFactor(Table dbt, String name)
	{
		super(dbt, name);
	}
	
	public String[][] getStats()
	{
		int num=0;
		int npromoter=0;
		LinkedList<String> numl = new LinkedList<String>();
		LinkedList<String> promotersids = new LinkedList<String>();
		
		String[][] res = new String[5][];
		try{
			Statement stmt = super.table.getConnection().createStatement();
			
			ArrayList<String[]> result = ProjectAPI.getAllFromTF(this.table.getName(), stmt);
			
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				if(!numl.contains(list[0]))
				{
					num++;
					numl.add(list[0]);
				}
				if(!promotersids.contains(list[1]))
				{
					npromoter++;
					promotersids.add(list[1]);
				}
	        }
			
			res[0] = new String[] {"Number of transcription factors", ""+num};
			res[1] = new String[] {"Number of regulated promoters", ""+npromoter};
			res[2] = new String[] {"Average number of TFs for promoters",
				""+(new Double(num).doubleValue())/(new Double(npromoter).doubleValue())};
			
			String value = ModelAPI.countNumberOfRegulatedGenes(stmt);
			
			res[3] = new String[] {"Number of regulated genes", value};
			
			int numultiplegene = ModelAPI.countTFsEncodedByGenes(stmt);
			
			res[4] = new String[] {"Number of TFs encoded by multiple genes", ""+numultiplegene};
			
		} catch(Exception e)
		{e.printStackTrace();}
		return res;
	}
	
	public DataTable getAllTFs() throws Exception
	{
		ArrayList<String> columnsNames = new ArrayList<String>();
		
		columnsNames.add("Names");
		columnsNames.add("Encoding genes");
		columnsNames.add("Number of regulated genes");

		Statement stmt = super.table.getConnection().createStatement();
        
		HashMap<String,ArrayList<String[]>> index = ModelAPI.getDataFromRegulatoryEvent(stmt);
        
        ArrayList<String[]> result = ModelAPI.getDataFromRegulatoryEvent2(stmt);
        
		DataTable qrt = new DataTable(columnsNames, "EnzymesContainer");
        
		for(int i=0; i<result.size(); i++){
			String[] list = result.get(i);
			
    		ArrayList<String> ql = new ArrayList<String>();
        	if(index.containsKey(list[1]))
        	{
        		ArrayList<String[]> lis = index.get(list[1]);
        		String egenes = "";
        		for(int r=0;r<lis.size();r++)
        		{
        			if(egenes.equals("")) egenes = lis.get(r)[2];
        			else egenes += " "+lis.get(r)[2];
        		}
        		ql.add(list[0]);
        		ql.add(egenes);
        		ql.add(list[2]);
        	}
        	else
        	{
        		ql.add(list[0]);
        		ql.add("");
        		ql.add(list[2]);
        	}
        	qrt.addLine(ql);
        }
		stmt.close();
        return qrt;
	}
	
	public GenericDataTable getData()
	{
		this.names = new HashMap<String, String>();
		GenericDataTable qrt = null;
		
		ArrayList<String> columnsNames = new ArrayList<String>();
		
		columnsNames.add("Names");
		columnsNames.add("Number of encoding genes");
		columnsNames.add("Number of regulated genes");
		
		try{
		
			Statement stmt = super.table.getConnection().createStatement();
        
			HashMap<String,ArrayList<String[]>> index = ModelAPI.getDataFromRegulatoryEvent(stmt);
        
			ArrayList<String[]> result = ModelAPI.getDataFromRegulatoryEvent2(stmt);

			qrt = new GenericDataTable(columnsNames, "TFs", "TU data");
			
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				ArrayList<Object> ql = new ArrayList<Object>();
				if(index.containsKey(list[1]))
				{
					ArrayList<String[]> lis = index.get(list[1]);
					int egenes = 0;
					for(int r=0;r<lis.size();r++)
					{
						egenes++;
					}
					ql.add(list[0]);
					ql.add(egenes+"");
					ql.add(list[2]);
				}
				else
				{
					ql.add(list[0]);
					ql.add("0");
					ql.add(list[2]);
				}
				qrt.addLine(ql, list[1]);

				this.names.put(list[1], list[0]);
			}
		} catch(Exception e)
		{e.printStackTrace();}
        return qrt;
	}
	
	public HashMap<Integer,Integer[]> getSearchData()
	{
		HashMap<Integer,Integer[]> res = new HashMap<Integer,Integer[]>();
		
		res.put(new Integer(0), new Integer[]{new Integer(0)});
		res.put(new Integer(1), new Integer[]{new Integer(1)});
		res.put(new Integer(2), new Integer[]{new Integer(0), new Integer(1)});
		
		return res;
	}
	
	public String[] getSearchDataIds()
	{
		String[] res = new String[]{"Name", "Encoding genes", "All"};
		
		return res;
	}
	
	public boolean hasWindow()
	{
		return true;
	}
	
	public DataTable[] getRowInfo(String id)
	{
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Encoding genes");

		DataTable[] res = new DataTable[2];
		
		DataTable qrt = new DataTable(columnsNames, "Encoding genes");
		
		res[0] = qrt;
		
		ArrayList<String> columnsNames2 = new ArrayList<String>();

		columnsNames2.add("Regulated genes");

		DataTable qrt2 = new DataTable(columnsNames2, "Regulated genes");
		
		res[1] = qrt2;
		
		try{
			Statement stmt = super.table.getConnection().createStatement();
			
			ArrayList<String[]> result = ModelAPI.getRowInfoTFs(id, stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[1]);
				qrt.addLine(ql);
			}

			result = ModelAPI.getTFsData(id, stmt);
			
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[1]);
				qrt2.addLine(ql);
			}
			stmt.close();
		} catch(Exception e)
		{e.printStackTrace();}
		
		return res;
	}

	public String getName(String id)
	{
		return this.names.get(id);
	}
	
	public String getSingular()
	{
		return "TF: ";
	}
}
