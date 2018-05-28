package pt.uminho.ceb.biosystems.merlin.core.datatypes.regulatory;

import java.io.Serializable;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;

public class MultipleRi implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5491683363173651717L;
	private HashMap<String,LinkedList<String>> pairs;
	private LinkedList<String> pairsIndex;
	private HashMap<String, LinkedList<String[]>> ris;
	private HashMap<String, String> promoterNames;
	private HashMap<String, String> proteinNames;
	
	public MultipleRi()
	{
		this.pairs = new HashMap<String,LinkedList<String>>();
		this.pairsIndex = new LinkedList<String>();
		this.ris = new HashMap<String, LinkedList<String[]>>();
		this.promoterNames = new HashMap<String, String>();
		this.proteinNames = new HashMap<String, String>();
	}
	
	public void loadData(Connection connection)
	{
		try{
			Statement stmt = connection.createStatement();
			
			ArrayList<String[]> result = ProjectAPI.loadData(stmt);
			
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
						
				if(!pairsIndex.contains(list[0]+"@"+list[1]))
				{
					if(!pairs.containsKey(list[0]))
					{
						LinkedList<String> ll = new LinkedList<String>();
						ll.add(list[1]);
						pairs.put(list[0], ll);
					}
					else
					{
						pairs.get(list[0]).add(list[1]);
					}
					
					LinkedList<String[]> ll = new LinkedList<String[]>();
					ll.add(new String[]{list[6], list[3]});
					ris.put(list[0]+"@"+list[1], ll);
					pairsIndex.add(list[0]+"@"+list[1]);
				}
				else
				{
					ris.get(list[0]+"@"+list[1]).add(new String[]{list[6], list[3]});
				}
				
				if(!proteinNames.containsKey(list[0]))
				{
					proteinNames.put(list[0], list[4]);
				}
				
				if(!promoterNames.containsKey(list[1]))
				{
					promoterNames.put(list[1], list[5]);
				}
			}
			stmt.close();
		} catch(Exception e)
		{e.printStackTrace();}
	}
	
	public DataTable getData()
	{
		ArrayList<String> columnsNames = new ArrayList<String>();
		
		columnsNames.add("Protein");
		columnsNames.add("Promoter");
		columnsNames.add("Ri");
		columnsNames.add("Binding site position");
		
		DataTable res = new DataTable(columnsNames, "");
		
		
		Set<String> zam = pairs.keySet();
		
		
		for (Iterator<String> iter = zam.iterator(); iter.hasNext(); )
		{
			String idprot = iter.next();
			
			LinkedList<String> idpromoters = pairs.get(idprot);
			
			for(int i=0;i<idpromoters.size();i++)
			{
				LinkedList<String[]> data = this.ris.get(idprot+"@"+idpromoters.get(i));
				
				if(data!=null && data.size()>1)
				{
					for(int x=0;x<data.size();x++)
					{
						ArrayList<String> ql = new ArrayList<String>();
						ql.add(this.proteinNames.get(idprot));
						ql.add(this.promoterNames.get(idpromoters.get(i)));
						ql.add(data.get(x)[0]);
						ql.add(data.get(x)[1]);
						
						res.addLine(ql);
					}
				}
			}
		}
		
		
		return res;
	}
}
