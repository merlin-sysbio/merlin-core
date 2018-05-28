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

@Datatype(structure = Structure.LIST, namingMethod = "getName")
public class TranscriptionUnit extends Entity implements Serializable {

	private static final long serialVersionUID = 9008699561446234481L;

	private HashMap<String, String> names;
	private int sortP;

	public TranscriptionUnit(Table dbt, String name) {
		super(dbt, name);
		this.sortP = 1;
	}

	public String[][] getStats() {
		
		String[][] res = new String[6][];
		try {
			Statement stmt = super.table.getConnection().createStatement();
			
			Integer[] list = ProjectAPI.countTUs(this.table.getName(), stmt);
			
			int num = list[0];
			int noname = list[1];

			res[0] = new String[] { "Number of TUs", "" + num };
			res[1] = new String[] { "Number of TUs with no name associated",
					"" + noname };

			String snumgenes = ProjectAPI.countGenesAssociatedWithTUs(stmt);
			
			res[2] = new String[] { "Number of genes associated with TUs",
					snumgenes };

			String snumtus = ProjectAPI.countTUsWithGenesAssociated(stmt);
			
			res[3] = new String[] { "Number of TUs with genes associated",
					snumtus };

			double promoters_by_tus = ProjectAPI.getAvarageNumberOfPromotersByTU(num, stmt);

			res[4] = new String[] { "Average number of promoters by TU", "" + promoters_by_tus };

			int gens_tu = ProjectAPI.getAvarageNumberOfGenesByTU(stmt);

			res[5] = new String[] {
					"Average number of genes by TU",
					"" + new Double(gens_tu).doubleValue()
							/ new Double(num).doubleValue() };

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return res;
	}

	public void setSort(int sortP) {
		this.sortP = sortP;
	}

	public GenericDataTable getData() {
		this.names = new HashMap<String, String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		ArrayList<String> index = new ArrayList<String>();
		HashMap<String, String[]> qls = new HashMap<String, String[]>();

		columnsNames.add("Names");
		columnsNames.add("Number of genes");
		columnsNames.add("Number of promoters");

		GenericDataTable res = new GenericDataTable(columnsNames, "TUs", "TU");

		try {
			Statement stmt = super.table.getConnection().createStatement();
			
			ArrayList<String[]> result = ProjectAPI.getAllFromTU(this.table.getName(), stmt);

			for(int i=0; i<result.size(); i++) {
				String[] list = result.get(i);
				
				String[] ql = new String[4];
				ql[0] = list[1];
				ql[1] = "0";
				ql[2] = "0";
				ql[3] = list[0];
				index.add(list[0]);
				qls.put(list[0], ql);
			}

			
			
			qls = ModelAPI.getDataFromTU(qls, stmt);

			qls = ModelAPI.getDataFromTU2(qls, stmt);

			SortableData[] sos = new SortableData[index.size()];

			for (int i = 0; i < index.size(); i++) {
				String[] gark = qls.get(index.get(i));
				sos[i] = new SortableData(new Integer(gark[sortP]).intValue(),
						gark);
			}

			Arrays.sort(sos);

			for (int i = 0; i < sos.length; i++) {
				ArrayList<Object> ql = new ArrayList<Object>();
				String[] gark = (String[]) sos[i].getData();
				ql.add(gark[0]);
				ql.add(gark[1]);
				ql.add(gark[2]);
				res.addLine(ql, gark[3]);
				this.names.put(gark[3], gark[0]);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	public HashMap<Integer, Integer[]> getSearchData() {
		HashMap<Integer, Integer[]> res = new HashMap<Integer, Integer[]>();

		res.put(new Integer(0), new Integer[] { new Integer(0) });

		return res;
	}

	public String[] getSearchDataIds() {
		String[] res = new String[] { "Name" };

		return res;
	}

	public boolean hasWindow() {
		return true;
	}

	public DataTable[] getRowInfo(String id) {
		DataTable[] res = new DataTable[3];

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Synonym");

		res[0] = new DataTable(columnsNames, "Synonyms");

		ArrayList<String> columnsNames2 = new ArrayList<String>();

		columnsNames2.add("Genes");

		res[1] = new DataTable(columnsNames2, "Genes");

		ArrayList<String> columnsNames3 = new ArrayList<String>();

		columnsNames3.add("Promoters");

		res[2] = new DataTable(columnsNames3, "Promoters");

		try {
			Statement stmt = super.table.getConnection().createStatement();
			
			ArrayList<String> result = ProjectAPI.getAliasClassTU(id, stmt);

			for(int i=0; i<result.size(); i++) {
				
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(result.get(i));
				res[0].addLine(ql);
			}

			result = ModelAPI.getGeneNameFromTU(id, stmt);

			for(int i=0; i<result.size(); i++){
				
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(result.get(i));
				res[1].addLine(ql);
			}

			result = ModelAPI.getPromoterNameFromTU(id, stmt);

			for(int i=0; i<result.size(); i++){
				
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(result.get(i));
				res[2].addLine(ql);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return res;
	}

	public String getName(String id) {
		return this.names.get(id);
	}

	public String getSingular() {
		return "TU: ";
	}
}