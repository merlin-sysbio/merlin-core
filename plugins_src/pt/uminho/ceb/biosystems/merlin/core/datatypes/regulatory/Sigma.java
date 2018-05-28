package pt.uminho.ceb.biosystems.merlin.core.datatypes.regulatory;

import java.io.Serializable;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;

public class Sigma extends Entity implements Serializable {

	private static final long serialVersionUID = 7041578259945388261L;

	private HashMap<String, String> names;

	public Sigma(Table dbt, String name)
	{
		super(dbt, name);
	}
	
	public String[][] getStats()
	{
		String[][] res = new String[3][];
		try{
			Statement stmt = super.table.getConnection().createStatement();
			
			Integer[] list = ModelAPI.countSigmaFactors(this.table.getName(), stmt);
			
			int num=list[0];
			int nproteins=list[1];
			int npromoter=list[2];
			
//			res[0] = new String[] {"Number of sigma promoters", ""+num};
//			res[1] = new String[] {"Number of proteins that are sigma promoters",
//				""+npromoter};
//			res[2] = new String[] {"Number of promoters that are regulated by sigma promoters",
//				""+nproteins};
			
			res[0] = new String[] {"Number of sigma factors", ""+nproteins};
			res[1] = new String[] {"Number of promoters regulated by sigma factors", ""+num};
			res[2] = new String[] {"Number of sigma factor regulations", ""+npromoter};
			
			
		} catch(Exception e)
		{e.printStackTrace();}
		return res;
	}
	
	public DataTable getAllSigmass() throws Exception
	{
		ArrayList<String> columnsNames = new ArrayList<String>();
		
		columnsNames.add("Names");
		columnsNames.add("Encoding genes");
		columnsNames.add("Regulated genes");

		DataTable qrt = new DataTable(columnsNames, "Sigma");
		
		Statement stmt = super.table.getConnection().createStatement();
		
		HashMap<String,ArrayList<String[]>> index = ModelAPI.getDataFromSigmaPromoter(stmt);
        
		ArrayList<String[]> result = ModelAPI.getDataFromSigmaPromoter2(stmt);

        ArrayList<String> hashIndex = new ArrayList<String>();
		HashMap<String,String> indexSigmaNames = new HashMap<String,String>();
		HashMap<String,ArrayList<String>> indexSigmaAfectedGenes = new HashMap<String,ArrayList<String>>();
		
		for(int i=0; i<result.size(); i++){
			String[] list = result.get(i);
			
        	if(!indexSigmaNames.containsKey(list[0]))
        	{
        		indexSigmaNames.put(list[0], list[1]);
        	}
        	if(!indexSigmaAfectedGenes.containsKey(list[0]))
        	{
        		ArrayList<String> lis = new ArrayList<String>();
    			lis.add(list[3]);
        		indexSigmaAfectedGenes.put(list[0], lis);
        	}
        	else
        	{
        		indexSigmaAfectedGenes.get(list[0]).add(list[3]);
        	}
        	if(!hashIndex.contains(list[0])) hashIndex.add(list[0]);
        }
        
        stmt.close();
        
        for(int i=0;i<hashIndex.size();i++)
        {
        	String encodGenes = "";
        	String regulatedGenes = "";
        	
        	if(indexSigmaAfectedGenes.containsKey(hashIndex.get(i)))
        	{
        		ArrayList<String> lis = indexSigmaAfectedGenes.get(hashIndex.get(i));
        		for(int r=0;r<lis.size();r++)
        		{
        			if(regulatedGenes.equals("")) regulatedGenes = lis.get(r);
        			else regulatedGenes += " "+lis.get(r);
        		}
        	}
        	
        	if(index.containsKey(hashIndex.get(i)))
        	{
        		ArrayList<String[]> lis = index.get(hashIndex.get(i));
        		for(int r=0;r<lis.size();r++)
        		{
        			if(encodGenes.equals("")) encodGenes = lis.get(r)[2];
        			else encodGenes += " "+lis.get(r)[2];
        		}
        	}
        	ArrayList<String> ql = new ArrayList<String>();
        	ql.add(indexSigmaNames.get(hashIndex.get(i)));
        	ql.add(encodGenes);
        	ql.add(regulatedGenes);
        	qrt.addLine(ql);
        }
        
        return qrt;
	}
	
	public GenericDataTable getData()
	{
		this.names = new HashMap<String, String>();
		ArrayList<String> columnsNames = new ArrayList<String>();
		
		columnsNames.add("Names");
		columnsNames.add("Encoding genes");
		columnsNames.add("Regulated genes");

		GenericDataTable qrt = new GenericDataTable(columnsNames, "Sigma", "Sigma data");
		
		try{
		
			Statement stmt = super.table.getConnection().createStatement();
			
			HashMap<String,ArrayList<String[]>> index = ModelAPI.getDataFromSigmaPromoter(stmt);
		
			ArrayList<String[]> result = ModelAPI.getDataFromSigmaPromoter2(stmt);

			ArrayList<String> hashIndex = new ArrayList<String>();
			HashMap<String,String> indexSigmaNames = new HashMap<String,String>();
			HashMap<String,ArrayList<String>> indexSigmaAfectedGenes = new HashMap<String,ArrayList<String>>();
		
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
	        	if(!indexSigmaNames.containsKey(list[0]))
	        	{
	        		indexSigmaNames.put(list[0], list[1]);
	        	}
	        	if(!indexSigmaAfectedGenes.containsKey(list[0]))
	        	{
	        		ArrayList<String> lis = new ArrayList<String>();
	    			lis.add(list[3]);
	        		indexSigmaAfectedGenes.put(list[0], lis);
	        	}
	        	else
	        	{
	        		indexSigmaAfectedGenes.get(list[0]).add(list[3]);
	        	}
	        	if(!hashIndex.contains(list[0])) hashIndex.add(list[0]);
	        }
        
			stmt.close();
        
			for(int i=0;i<hashIndex.size();i++)
			{
				String encodGenes = "";
				int regulatedGenes = 0;
				
				if(indexSigmaAfectedGenes.containsKey(hashIndex.get(i)))
				{
					ArrayList<String> lis = indexSigmaAfectedGenes.get(hashIndex.get(i));
					
					regulatedGenes = lis.size();
					
//					for(int r=0;r<lis.size();r++)
//					{
//						if(regulatedGenes.equals("")) regulatedGenes = lis.get(r);
//						else regulatedGenes += " "+lis.get(r);
//					}
				}
        	
				if(index.containsKey(hashIndex.get(i)))
				{
					ArrayList<String[]> lis = index.get(hashIndex.get(i));
//					encodGenes = lis.size();
					for(int r=0;r<lis.size();r++)
					{
						if(encodGenes.equals("")) encodGenes = lis.get(r)[2];
						else encodGenes += " "+lis.get(r)[2];
					}
				}
				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add(indexSigmaNames.get(hashIndex.get(i)));
				ql.add(encodGenes);
				ql.add(""+regulatedGenes);
				qrt.addLine(ql, hashIndex.get(i));

				this.names.put(hashIndex.get(i), indexSigmaNames.get(hashIndex.get(i)));
			}
			
		} catch(Exception e)
		{e.printStackTrace();}
        
        return qrt;
		
	}
	
	public HashMap<Integer,Integer[]> getSearchData()
	{
		HashMap<Integer,Integer[]> res = new HashMap<Integer,Integer[]>();
		
		res.put(new Integer(0), new Integer[]{new Integer(0)});
		
		return res;
	}
	
	public String[] getSearchDataIds()
	{
		String[] res = new String[]{"Name"};
		
		return res;
	}
	
	public boolean hasWindow()
	{
		return true;
	}
	
	public DataTable[] getRowInfo(String id)
	{
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Synonyms");

		DataTable[] res = new DataTable[2];
		
		DataTable qrt = new DataTable(columnsNames, "Synonyms");
		
		res[0] = qrt;
		
		ArrayList<String> columnsNames2 = new ArrayList<String>();

		columnsNames2.add("Regulated genes");

		DataTable qrt2 = new DataTable(columnsNames2, "Regulated genes");
		
		res[1] = qrt2;
		
		try
		{
			Statement stmt = super.table.getConnection().createStatement();
		
			ArrayList<String> ql = ProjectAPI.getAliasClassP(id, stmt);
			qrt.addLine(ql);

			//stmt = super.table.getConnection().createStatement();
			ArrayList<String[]> result = ModelAPI.getRowInfoSigmaPromoter(id, stmt);
			
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				ArrayList<String> ql2 = new ArrayList<String>();
				ql.add(list[1]);
				qrt2.addLine(ql2);
			}
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
		return "Sigma: ";
	}
}
