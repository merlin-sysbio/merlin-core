package pt.uminho.ceb.biosystems.merlin.core.datatypes.regulatory;

import java.io.Serializable;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.utilities.SortableData;

@Datatype(structure = Structure.LIST,namingMethod="getName")
public class Promoter extends Entity implements Serializable {

	private static final long serialVersionUID = -7544259557599444570L;

	private HashMap<String, String> names;

	private MultipleRi mr;
	private int sortP;
	
	public Promoter(Table dbt, String name)
	{
		super(dbt, name);
		this.mr = new MultipleRi();
		this.mr.loadData(super.table.getConnection());
		this.sortP = 2;
	}
	
	public String[][] getStats(){
		String[][] res = new String[4][];
		
		try{
			Statement stmt = super.table.getConnection().createStatement();

			Integer[] list = ModelAPI.countPromoters(stmt);
			
			int num = list[0];
			int noname = list[1];
			int noap = list[2];
			
			res[0] = new String[] {"Number of promoters", ""+num};
			res[1] = new String[] {"Number of promoters with no name associated", ""+noname};
			res[2] = new String[] {"Number of promoters with no absolut position associated", ""+noap};
			
			//stmt = super.table.getConnection().createStatement();
//			rs = stmt.executeQuery("SELECT count(distinct(promoter_idpromoter)) FROM sigma_promoter");
//			
//			rs.next();
			
			/////////////////////////////////////////////comentado pois nao esta a ser usado e lanca um warning
			//String ssigma = rs.getString(1);
			////////////////////////////////////////////////
			//stmt = super.table.getConnection().createStatement();
			
			
			String value = ModelAPI.countPromoterWithRegulationsByTFs(stmt);
			
			res[3] = new String[] {"Number of promoters with regulations by TFs", value};

			//stmt = super.table.getConnection().createStatement();
//			rs = stmt.executeQuery(
//				"SELECT count(distinct(transcription_unit_promoter.promoter_idpromoter)) " +
//				"FROM transcription_unit_promoter " +
//				"JOIN sigma_promoter " +
//				"ON transcription_unit_promoter.promoter_idpromoter = sigma_promoter.promoter_idpromoter"
//			);
//			
//			rs.next();
			
			stmt.close();
		} catch(Exception e)
		{e.printStackTrace();}
		return res;
	}
	
	public void setSort(int sortP)
	{
		this.sortP = sortP;
	}
	
	public GenericDataTable getData()
	{
		this.names = new HashMap<String, String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		ArrayList<String> index = new ArrayList<String>();
		HashMap<String,String[]> qls = new HashMap<String,String[]>();
		
		columnsNames.add("Names");
		columnsNames.add("Absolute position");
		columnsNames.add("Number of regulations");
		columnsNames.add("Number of TUs");
		
		GenericDataTable res = new GenericDataTable(columnsNames, "Promoters", "Promoter");
		
		try
		{
			Statement stmt = super.table.getConnection().createStatement();
			
			ArrayList<String[]> result = ProjectAPI.getAllFromPromoters(this.table.getName(), stmt);
			
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				String[] ql = new String[6];
				ql[0] = list[1];
				ql[1] = list[2];
				ql[2] = "0";
				ql[3] = "0";
				ql[4] = "0";
				ql[5] = list[0];
				index.add(list[0]);
				qls.put(list[0], ql);
			}
			
			qls = ModelAPI.getDataFromRegulatoryEvent(qls, stmt);
			
			qls = ModelAPI.getDataFromSigmaPromoter(qls, stmt);
			
			qls = ModelAPI.getDataFromTranscriptUnitPromoter(qls, stmt);
			
			SortableData[] sos = new SortableData[index.size()];
			
			for(int i=0;i<index.size();i++)
			{
				String[] gark = qls.get(index.get(i));
				sos[i] = new SortableData(new Integer(gark[sortP]).intValue(), gark);
			}
			
			Arrays.sort(sos);
			
			for(int i=0;i<sos.length;i++)
			{
				ArrayList<Object> ql = new ArrayList<Object>();
				String[] gark = (String[])sos[i].getData();
				ql.add(gark[0]);
				ql.add(gark[1]);
				ql.add(gark[2]);
				ql.add(gark[3]);
				ql.add(gark[4]);
				res.addLine(ql, gark[5]);
				this.names.put(gark[5], gark[0]);
			}
			
		} catch(Exception e)
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

		columnsNames.add("Synonym");

		DataTable[] res = new DataTable[4];
		
		DataTable qrt = new DataTable(columnsNames, "Synonyms");
		
		res[0] = qrt;

		ArrayList<String> columnsNames2 = new ArrayList<String>();
		
		columnsNames2.add("Regulations");

		res[1] = new DataTable(columnsNames2, "Regulations");
		
		ArrayList<String> columnsNames3 = new ArrayList<String>();
		
		columnsNames3.add("Sigmas");

		res[2] = new DataTable(columnsNames3, "Sigmas");
		
		ArrayList<String> columnsNames4 = new ArrayList<String>();
		
		columnsNames4.add("TUs");

		res[3] = new DataTable(columnsNames4, "TUs");
		
		
		try{
			Statement stmt = super.table.getConnection().createStatement();

			ArrayList<String> ql = ProjectAPI.getAliasClassP(id, stmt);
			res[0].addLine(ql);

			
			//stmt = super.table.getConnection().createStatement();
			
			ql = ProjectAPI.getProteinName(id, stmt);
			res[1].addLine(ql);
			
			//stmt = super.table.getConnection().createStatement();
			
			ql = ProjectAPI.getNameFromSigmaPromoterTable(id, stmt);
			
			res[2].addLine(ql);
			
			//stmt = super.table.getConnection().createStatement();
			
			ql = ProjectAPI.getNameFromTranscriptionUnitPromoterTable(id, stmt);
			
			res[3].addLine(ql);
			
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
		return "Promoter: ";
	}

	public MultipleRi getMr() {
		return mr;
	}
	
}
