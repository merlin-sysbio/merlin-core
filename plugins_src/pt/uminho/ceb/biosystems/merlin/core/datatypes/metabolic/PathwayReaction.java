package pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;

/**
 * @author ODias
 *
 */
public class PathwayReaction extends DataTable implements Serializable{

	private static final long serialVersionUID = 1L;
	protected ArrayList<String> ids;
	protected ArrayList<String> pathway;
	protected Map <String, String> pathways;
	protected boolean encodedOnly;

	/**
	 * @param columnsNames
	 * @param name
	 * @param pathways
	 * @param encodedOnly
	 */
	public PathwayReaction(ArrayList<String> columnsNames, String name, Map <String, String> pathways, boolean encodedOnly) {
		
		super(columnsNames, name);
		this.ids = new ArrayList<String>();
		this.pathway = new ArrayList<String>();
		this.pathways = pathways;
		this.encodedOnly=encodedOnly;
	}

//	/**
//	 * @param line
//	 * @param id
//	 * @param pathway
//	 */
//	public void addLine(ArrayList<String> line, String id, String pathway){
//		super.addLine(line);
//		this.ids.add(id);
//		this.pathway.add(pathway);
//	}
	
	/**
	 * @param line
	 * @param id
	 * @param pathway
	 */
	public void addLine(ArrayList<Object> line, String id, String pathway){
		super.addLine(line);
		this.ids.add(id);
		this.pathway.add(pathway);
	}

	/**
	 * @param row
	 * @return
	 */
	public String getRowPathway(int row){
		return pathway.get(row);
	}

	/**
	 * @param row
	 * @return
	 */
	public String getRowId(int row){
		return ids.get(row);
	}

	/**
	 * @param path
	 * @return
	 */
	public GenericDataTable getReactionsData(int path) {
		
		try {
			
			ArrayList<String> columnsNames = new ArrayList<String>();
			columnsNames.add("info");
			columnsNames.add("pathway name");
			columnsNames.add("reaction name");
			columnsNames.add("equation");
			columnsNames.add("source");
			columnsNames.add("notes");
			columnsNames.add("reversible");
			columnsNames.add("in model");

			GenericDataTable qrt = new GenericDataTable(columnsNames, "reactions", "") {
				
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int col) {
					
					if (col==0 || col>4)						
						return true;
					else 
						return false;
				}
			};

			for(int i=0;i<pathway.size();i++) {
				
				if(pathway.get(i).equals(path+""))
					qrt.addLine((Object[])super.getRow(i), this.ids.get(i));
			}
			return qrt;
		}
		catch(Exception e) {
		
			e.printStackTrace();
		}

		return null;
	}
}
