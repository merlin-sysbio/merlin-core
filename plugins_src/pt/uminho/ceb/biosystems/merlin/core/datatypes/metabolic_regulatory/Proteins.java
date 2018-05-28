package pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseUtilities;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

public class Proteins extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<Integer,String> namesIndex;
	private HashMap<Integer,Integer> ids =  new HashMap<>();;
	//private TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy;
	private Connection connection;

	/**
	 * @param dbt
	 * @param name
	 * @param ultimlyComplexComposedBy
	 */
	public Proteins(Table dbt, String name, TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy) {

		super(dbt, name);
		//this.ultimlyComplexComposedBy = ultimlyComplexComposedBy;
		this.connection=dbt.getConnection();
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		Statement stmt;
		String[][] res = new String[8][];
		
		try {
			
			stmt = this.connection.createStatement();
			
			int[] result = ProjectAPI.countProteins(stmt);
			
			int num = result[0];
			int noname = result[1];

			res[0] = new String[] {"Number of proteins", ""+num};
			
			res[1] = new String[] {"Number of proteins with no name associated", ""+noname};

			double snumproteins = ProjectAPI.countProteinsSynonyms(stmt);
			res[2] = new String[] {"Number of proteins synonyms", snumproteins+""};

			double synmed = Math.round((snumproteins/num)*1000.0)/1000.0;
			res[3] = new String[] {"Average number synonyms by protein", ""+synmed};

			String value = ProjectAPI.countProteinsEnzymes(stmt);
			res[4] = new String[] {"Number of proteins that are enzymes", value};

			value = ProjectAPI.countProteinsTransporters(stmt);
			res[5] = new String[] {"Number of proteins that are transporters", value};
			
		
//			rs = stmt.executeQuery("SELECT count(distinct(protein_idprotein)) FROM regulatory_event");
//			rs.next();
//			res[8] = new String[] {"Number of proteins that are transcription factors", rs.getString(1)};

		
			value = ProjectAPI.countProteinsComplexes(stmt);
			res[6] = new String[] {"Number of proteins that are complexes", value};

			int p_g = ProjectAPI.countProteinsAssociatedToGenes(stmt);
			
			res[7] = new String[] {"Number of proteins associated to genes",p_g+""};

			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @param encoded
	 * @return
	 */
	public GenericDataTable getAllProteins(boolean encoded) {

		this.namesIndex = new HashMap<Integer, String>();
		List<String> columnsNames = new ArrayList<String>();

		columnsNames.add("info");
		columnsNames.add("names");
		columnsNames.add("identifier");
		columnsNames.add("number of reactions");
		columnsNames.add("encoding genes");
		columnsNames.add("encoded in Genome");
		columnsNames.add("catalysing reactions in Model");


		GenericDataTable enzymeDataTable = new GenericDataTable(columnsNames, "Enzymes",""){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0){return true;}
				else {return false;}
			}
		};


		try {
			Statement stmt = this.connection.createStatement();
			
			HashMap<String,Integer> proteins = ProjectAPI.getProteins(stmt);
			
			ArrayList<String[]> result = ModelAPI.getAllEnzymes(this.getProject().isCompartmentalisedModel(), encoded, stmt);

			for(int index=0; index<result.size(); index++){
				String[] list = result.get(index);

				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add(""); //info

				for(int i=0;i<6;i++) {

					if(i>3 && i<6) {

						if(i==5) {

							if(Boolean.valueOf(list[i])==false && Integer.parseInt(list[7])==1) {

								ql.add(false); 
							}
							else {

								ql.add(true); 
							}

						}
						else {
							ql.add(Boolean.valueOf(list[i]));
						}
					}
					else {
						if (i==3){
							if(proteins.containsKey(list[6]))
								ql.add(proteins.get(list[6]).intValue()+"");
							else
								ql.add("0");
						}
						else{
							String aux = list[i];
	
							if(aux!=null) 
								ql.add(aux);
							else 
								ql.add("");	
						}
					}
				}
				enzymeDataTable.addLine(ql,list[6]);
				
				this.namesIndex.put(index, list[0]);
				this.ids.put(index, Integer.parseInt(list[6]));
			}
			stmt.close();
		}
		catch (SQLException e) {

			e.printStackTrace();
		}

		return enzymeDataTable;
	}


//	/**
//	 * @return
//	 */
//	public GenericDataTable getEnzymes() {
//
//		namesIndex = new HashMap<String,String>();
//		ids = new TreeMap<Integer,String>();
//		ArrayList<String> columnsNames = new ArrayList<String>();
//
//		columnsNames.add("Info");
//		columnsNames.add("Name");
//		columnsNames.add("InChi");
//		columnsNames.add("Encoding genes");
//
//		GenericDataTable qrt = new GenericDataTable(columnsNames, "Proteins", ""){
//			private static final long serialVersionUID = 6629060675011336218L;
//			@Override
//			public boolean isCellEditable(int row, int col){
//				if (col==0)
//				{
//					return true;
//				}
//				else return false;
//			}
//		};
//
//		try {
//
//			Statement stmt = this.connection.createStatement();
//
//			HashMap<String,Integer> index = ProjectAPI.getProteins(stmt);
//			
//			//	stmt = dsa.createStatement();
//
//			ArrayList<String[]> result = ModelAPI.getEnzymes(stmt);
//
//			int p=0;
//			for(int i=0; i<result.size(); i++){
//				String[] list = result.get(i);
//				
//				ArrayList<Object> ql = new ArrayList<Object>();
//				ql.add("");
//				ql.add(list[1]);
//				ql.add(list[2]);
////				ql.add(list[3]);		nao existem as entradas 3 e 4.
////				ql.add(list[4]);		as duas de baixo substituem estas
//				ql.add("");
//				ql.add("");
//
//				String idp = list[0];
//
//				if(index.containsKey(idp)) ql.add(index.get(idp).intValue()+"");
//				else ql.add("0");
//
//				this.namesIndex.put(idp, list[1]);
//				this.ids.put(p, idp);
//				p++;
//				qrt.addLine(ql, idp);
//			}
//			stmt.close();
//		}
//		catch (SQLException ex) {
//
//			ex.printStackTrace();
//		}
//		return qrt;
//	}
//
//	/**
//	 * @return
//	 */
//	public GenericDataTable getTFs() {
//
//		namesIndex = new HashMap<String,String>();
//		ids = new TreeMap<Integer,String>();
//		ArrayList<String> columnsNames = new ArrayList<String>();
//
//		columnsNames.add("Info");
//		columnsNames.add("Name");
//		columnsNames.add("InChi");
//		columnsNames.add("Number of encoding genes");
//
//		GenericDataTable qrt = new GenericDataTable(columnsNames, "Proteins", ""){
//			private static final long serialVersionUID = 6629060675011336218L;
//			@Override
//			public boolean isCellEditable(int row, int col){
//				if (col==0)
//				{
//					return true;
//				}
//				else return false;
//			}
//		};
//
//		try {
//
//			Statement stmt = this.connection.createStatement();
//
//			HashMap<String,Integer> index = ProjectAPI.getProteins(stmt);
//			
//			//stmt = dsa.createStatement();
//
//			ArrayList<String[]> result = ModelAPI.getTFs(stmt);
//
//			int p=0;
//			for(int i=0; i<result.size(); i++){
//				String[] list = result.get(i);
//				
//				ArrayList<Object> ql = new ArrayList<Object>();
//				ql.add("");
//				ql.add(list[1]);
//				ql.add(list[2]);
////				ql.add(list[3]);		nao existem as entradas 3 e 4.
////				ql.add(list[4]);		as duas de baixo substituem estas
//				ql.add("");
//				ql.add("");
//				String idp = list[0];
//
//				if(index.containsKey(idp)) ql.add(index.get(idp).intValue()+"");
//				else ql.add("0");
//
//				ids.put(p, idp);
//				p++;
//
//				qrt.addLine(ql, list[0]);
//
//				this.namesIndex.put(list[0], list[1]);
//			}
//			stmt.close();
//		}
//		catch (SQLException ex) {
//
//			ex.printStackTrace();
//		}
//		return qrt;
//	}
//
//	/**
//	 * @return
//	 */
//	public GenericDataTable getSigmas() {
//
//		namesIndex = new HashMap<String,String>();
//		ids = new TreeMap<Integer,String>();
//		ArrayList<String> columnsNames = new ArrayList<String>();
//
//		columnsNames.add("Info");
//		columnsNames.add("Name");
//		columnsNames.add("InChi");
//		columnsNames.add("Number of encoding genes");
//
//		GenericDataTable qrt = new GenericDataTable(columnsNames, "Proteins", ""){
//			private static final long serialVersionUID = 6629060675011336218L;
//			@Override
//			public boolean isCellEditable(int row, int col){
//				if (col==0)
//				{
//					return true;
//				}
//				else return false;
//			}
//		};
//
//		try
//		{
//			//MySQLMultiThread dsa =  new MySQLMultiThread( this.host,this.port, this.dbName, this.user, this.pass);
//			Statement stmt = this.connection.createStatement();
//			
//			HashMap<String,Integer> index = ProjectAPI.getProteins(stmt);
//
//			ArrayList<String[]> result = ModelAPI.getSigmas(stmt);
//
//			int p=0;
//			for(int i=0; i<result.size(); i++){
//				String[] list = result.get(i);
//				
//				ArrayList<Object> ql = new ArrayList<Object>();
//				ql.add("");
//				ql.add(list[1]);
//				ql.add(list[2]);
//				ql.add(list[3]);		
//				ql.add(list[4]);		
//
//				String idp = list[0];
//
//				if(index.containsKey(idp)) ql.add(index.get(idp).intValue()+"");
//				else ql.add("0");
//
//				ids.put(p, idp);
//				p++;
//
//				qrt.addLine(ql, list[0]);
//
//				this.namesIndex.put(list[0], list[1]);
//			}
//			stmt.close();
//		}
//		catch (SQLException ex) {
//
//			ex.printStackTrace();
//		}
//		return qrt;
//	}

	/**
	 * @param id
	 * @return
	 */
	public String getProteinName(int id) {

		return this.namesIndex.get(id);
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	/**
	 * @param ecnumber
	 * @param id
	 * @return
	 */
	public DataTable[] getRowInfo(String ecnumber, String id) {

		//String id = this.index.get(Integer.parseInt(row));
		DataTable[] datatables = new DataTable[6];
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("reaction");
		columnsNames.add("equation");
		columnsNames.add("source");
		columnsNames.add("in model");
		columnsNames.add("reversible");
		datatables[0] = new DataTable(columnsNames, "Encoded Reactions");

		columnsNames = new ArrayList<String>();
		columnsNames.add("name");
		columnsNames.add("locus tag");
		columnsNames.add("KO");
		columnsNames.add("origin");
//		columnsNames.add("notes");
		columnsNames.add("similarity");
		columnsNames.add("orthologue");
		datatables[1] = new DataTable(columnsNames, "Encoding genes");	

		columnsNames = new ArrayList<String>();
		columnsNames.add("gpr status");
		columnsNames.add("reaction");
		columnsNames.add("rule");
		columnsNames.add("module name");
		datatables[2] = new DataTable(columnsNames, "Gene-Protein-Reaction");	

		columnsNames = new ArrayList<String>();
		columnsNames.add("pathway ID");
		columnsNames.add("pathway name");
		datatables[3] = new DataTable(columnsNames, "Pathways");	

		columnsNames = new ArrayList<String>();
		columnsNames.add("synonyms");
		datatables[4] = new DataTable(columnsNames, "Synonyms");

		columnsNames = new ArrayList<String>();
		columnsNames.add("locus tag");
		columnsNames.add("compartment");
		columnsNames.add("score");
		columnsNames.add("primary location");
		datatables[5] = new DataTable(columnsNames, "Compartments");

		try {

			Statement stmt = this.connection.createStatement();
			String aux = "";
			if(this.getProject().isCompartmentalisedModel())
				aux = aux.concat(" AND NOT originalReaction ");
			else
				aux = aux.concat(" AND originalReaction ");

			ArrayList<String[]> result = ModelAPI.getReactionsData(ecnumber, aux, id, stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[0]);
				ql.add(list[1]);
				ql.add(list[2]);

				if(Boolean.valueOf(list[3]))					
					ql.add("true");
				else					
					ql.add("-");

				if(Boolean.valueOf(list[4]))					
					ql.add("true");
				else					
					ql.add("-");

				datatables[0].addLine(ql);
			}

			result = ModelAPI.getGeneData(ecnumber, id, stmt);
			
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[0]);
				ql.add(list[1]);
				ql.add(list[2]);
				ql.add(list[3]);
				ql.add(list[4]);
				ql.add(list[5]);
				
				datatables[1].addLine(ql);
			}

			result = ModelAPI.getPathways(ecnumber, id, stmt);
			
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[0]);
				ql.add(list[1]);
				datatables[3].addLine(ql);
			}

			result = ModelAPI.getDataFromSubunit(ecnumber, id, stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[0]);
				ql.add(list[1]);
				ql.add(list[2]);
				ql.add(list[3]);
				
				datatables[2].addLine(ql);
			}

			ArrayList<String> data = ProjectAPI.getAliasClassP(id, stmt);

			for(int i=0; i<data.size(); i++){

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(data.get(i));
				datatables[4].addLine(ql);
			}

			result = ModelAPI.getGeneData2(ecnumber, id, stmt);


			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[4]);
				ql.add(list[1]);
				ql.add(list[3]);

				if(Boolean.valueOf(list[2]))
					ql.add(list[2]);
				else
					ql.add("");

				datatables[5].addLine(ql);
			}

			stmt.close();

		}
		catch(Exception e) {

			e.printStackTrace();
		}

		return datatables;
	}

	/**
	 * @param row
	 */
	public void RemoveProtein(int row, boolean encodedOnly) {
		
		Statement stmt;
		try {

			stmt= this.connection.createStatement();

//			if(row==-1) {
//
//				String aux ="";
//				if(encodedOnly) {
//					
//					aux = " RIGHT JOIN enzyme ON (protein.idprotein = protein_idprotein) WHERE inModel";
//				}
//
//				Set<String> proteins = ModelAPI.getAllFromProtein(aux, stmt);
//
//				for(String proteinID : proteins) {
//
//					Pair<List<String>, Boolean[]> res = ModelAPI.getECnumber(proteinID, stmt);
//					
//					List<String> enzymesIDs = res.getA();
//					Boolean[] inModel = res.getB();
//
//					for(String enz : enzymesIDs) {
//
//						Proteins.removeEnzymesAssignmensts(enz, enzymesIDs, inModel, stmt, proteinID, true, this.connection.getDatabaseType());
//					}
//					String query = "DELETE FROM protein where idprotein = "+proteinID;
//					ProjectAPI.executeQuery(query, stmt);
//				}
//			}
//			else {
			Pair<List<String>, Boolean[]> res = ModelAPI.getECnumber(ids.get(row), stmt);
			List<String> enzymesIDs = res.getA();
			Boolean[] inModel = res.getB();

			for(String enz : enzymesIDs) {
				
				Proteins.removeEnzymesAssignmensts(enz, enzymesIDs, inModel, stmt, ids.get(row), true, this.connection.getDatabaseType());
			}
			String query = "DELETE FROM protein where idprotein = '"+ids.get(row)+"'";
			ProjectAPI.executeQuery(query, stmt);
//			}
			stmt.close();
		}
		catch (SQLException ex) {
			
			Workbench.getInstance().warn("An error occurred while deleting this protein!");
			ex.printStackTrace();
		}
		Workbench.getInstance().info("Protein successfully removed.");
	}

	/**
	 * @param selectedRow
	 * @return
	 */
	public String[] getProteinData(int selectedRow) {

		Statement stmt;
		String[][] res = null;
		String[] data = new String[11];

		try {

			stmt = this.connection.createStatement();
			
			res = ModelAPI.getProteinData(ids.get(selectedRow), stmt);
			
			for(int i=0; i<res[0].length;i++)
			{
				data[i]=res[0][i];
			}

			String[] result = ModelAPI.getECnumber2(ids.get(selectedRow), stmt);
			
			data[9]= result[0];
			data[10]= result[1];
			
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		return data;
	}

	/**
	 * @param selectedRow
	 * @return
	 */
	public String[] getSynonyms(int selectedRow) {

		Statement stmt;
		String[][] res = null; 
		try 
		{
			stmt = this.connection.createStatement();
			
			res = ModelAPI.getSynonyms(ids.get(selectedRow), stmt);

			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		String[] data = new String[res.length];

		int i=0;
		while(i<res.length)
		{
			data[i]=res[i][0];
			i++;
		}

		return data;
	}

	/**
	 * @param name
	 * @param clas
	 * @param inchi
	 * @param molecular_weight
	 * @param molecular_weight_kd
	 * @param molecular_weight_exp
	 * @param molecular_weight_seq
	 * @param pi
	 * @param selectedRow
	 * @param synonyms
	 * @param oldSynonyms
	 * @param enzymes
	 * @param oldEnzymes
	 * @param inModel
	 * @param oldInModel
	 */
	public boolean updateProtein(String name, String clas, String inchi, String molecular_weight, String molecular_weight_kd,
			String molecular_weight_exp, String molecular_weight_seq, String pi, int selectedRow, String[] synonyms,
			String[] oldSynonyms, String[] enzymes, String[] oldEnzymes, Boolean[] inModel, Boolean[] oldInModel) {

		Statement stmt;

		try {
			int proteinID = ids.get(selectedRow);
			stmt = this.connection.createStatement();
			stmt.execute("UPDATE protein SET name = '" + DatabaseUtilities.databaseStrConverter(name, this.connection.getDatabaseType())+ "'" +
					//", inchi = '" + inchi + "', " +
					//					"molecular_weight ='" + molecular_weight +  "', " +
					//					"molecular_weight_exp='" + molecular_weight_exp + "', "+ 
					//					"molecular_weight_kd = '" + molecular_weight_kd + "', " +
					//					"molecular_weight_seq = '" + molecular_weight_seq + "', " +
					//					"pi = '" + pi + "' " +
					"WHERE  idprotein = "+proteinID);
			
			String query;
			
			if(!clas.equals("")){
				query = "UPDATE protein SET class = '" + clas + "' WHERE idprotein = "+proteinID;
				ProjectAPI.executeQuery(query, stmt);
			}
			if(!inchi.equals("")){
				query = "UPDATE protein SET inchi = '" + inchi + "' WHERE idprotein = "+proteinID;
				ProjectAPI.executeQuery(query, stmt);
			}
			if(!molecular_weight.equals("")){
				query = "UPDATE protein SET molecular_weight ='" + molecular_weight +  "' WHERE idprotein = "+proteinID;
				ProjectAPI.executeQuery(query, stmt);
			}
			if(!molecular_weight_exp.equals("")){
				query = "UPDATE protein SET molecular_weight_exp='" + molecular_weight_exp + "' WHERE idprotein = "+proteinID;
				ProjectAPI.executeQuery(query, stmt);
			}
			if(!molecular_weight_kd.equals("")){
				query = "UPDATE protein SET molecular_weight_kd='" + molecular_weight_kd + "' WHERE idprotein = "+proteinID;
				ProjectAPI.executeQuery(query, stmt);
			}
			if(!molecular_weight_seq.equals("")){
				query = "UPDATE protein SET molecular_weight_seq='" + molecular_weight_seq + "' WHERE idprotein = "+proteinID;
				ProjectAPI.executeQuery(query, stmt);
			}
			if(!molecular_weight_seq.equals("")){
				query = "UPDATE protein SET pi = '" + pi + "' WHERE  idprotein = "+proteinID;
				ProjectAPI.executeQuery(query, stmt);
			}
			
			for(int s=0; s<synonyms.length; s++) {

				if(oldSynonyms.length>s) {

					if(!synonyms[s].equals("")) {

						if(!synonyms[s].equals(oldSynonyms[s])) {

							query = "UPDATE aliases SET alias ='" + DatabaseUtilities.databaseStrConverter(synonyms[s], this.connection.getDatabaseType()) +"' " +
									"WHERE class='p' entity = "+proteinID+" AND alias ='" + DatabaseUtilities.databaseStrConverter(oldSynonyms[s], this.connection.getDatabaseType()) + "'";
							
							ProjectAPI.executeQuery(query, stmt);
						}
					}
					else {
						query = "Delete from aliases WHERE entity = "+proteinID+" AND alias ='" + DatabaseUtilities.databaseStrConverter(oldSynonyms[s], this.connection.getDatabaseType()) + "'";
						ProjectAPI.executeQuery(query, stmt);
					}

				}			
				else {

					if(!synonyms[s].equals("")) {

						query = "INSERT INTO aliases (class, alias, entity) VALUES('p','" 
						+ DatabaseUtilities.databaseStrConverter(synonyms[s], this.connection.getDatabaseType()) +"', "+proteinID+")";
						
						ProjectAPI.executeQuery(query, stmt);
					}
				}
			}

			List<String> old_enzymes_ids = new ArrayList<String>();
			List<String> enzymes_ids = new ArrayList<String>();

			int i = 0;
			for(String id : oldEnzymes) {

				old_enzymes_ids.add(i,id);
				i++;
			}

			i = 0;
			for(String id : enzymes) {

				enzymes_ids.add(i,id);
				i++;
			}

			List<String> enzymes_ids_add = new ArrayList<String>();

			for(String id : enzymes_ids) {

				if(!id.equals("dummy") && !id.isEmpty()) {

					if(old_enzymes_ids.contains(id)) {

						old_enzymes_ids.remove(id);
					}
					else {

						enzymes_ids_add.add(id);
					}
				}
			}

			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			for(String id : old_enzymes_ids) {

				Proteins.removeEnzymesAssignmensts(id, enzymes_ids_add, inModel, stmt, proteinID, false, this.connection.getDatabaseType());
			}

			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			for(String id : enzymes_ids) {

				if(enzymes_ids_add.contains(id)) {

					Proteins.insertEnzymes(ids.get(selectedRow), id, stmt, false);
				}
				else {

					if(inModel[enzymes_ids.indexOf(id)]) {
						
						Proteins.insertEnzymes(ids.get(selectedRow), id, stmt, true);
					}
					else {
						
						Proteins.removeEnzymesAssignmensts(id, enzymes_ids_add, inModel, stmt, proteinID, false, this.connection.getDatabaseType());
					}
				}
			}

			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			//			for(int s=0; s<enzymes.length; s++) {
			//
			//				if(oldEnzymes.length>s) {
			//
			//					if(!enzymes[s].equals("")) {
			//
			//						if(!enzymes[s].equals(oldEnzymes[s])) {
			//
			//							stmt.execute("UPDATE enzyme SET ecnumber ='" +Connection.mysqlStrConverter(enzymes[s]) +"' " +
			//									"WHERE protein_idprotein='"+ids.get(selectedRow)+"' AND ecnumber ='" + Connection.mysqlStrConverter(oldEnzymes[s]) + "'");
			//						}
			//					}
			//					else {
			//
			//						stmt.execute("DELETE FROM enzyme WHERE protein_idprotein='"+ids.get(selectedRow)+"' AND ecnumber ='" + Connection.mysqlStrConverter(oldEnzymes[s]) + "'");
			//					}
			//
			//				}
			//				else {
			//
			//					if(!enzymes[s].equals("")) {
			//
			//						stmt.execute("INSERT INTO enzyme (protein_idprotein, ecnumber, inModel) VALUES("+ids.get(selectedRow)+",'"+Connection.mysqlStrConverter(enzymes[s])+"',"+inModel[s]+")");
			//					}
			//				}
			//			}
			//
			//			for(int s=0; s<inModel.length; s++) {
			//
			//				stmt.execute("UPDATE enzyme SET inModel = " + inModel[s]+" WHERE protein_idprotein= "+ids.get(selectedRow)+" AND ecnumber ='" + enzymes[s] + "',");
			//
			//				ResultSet rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reaction " +
			//						"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = idreaction " +
			//						"INNER JOIN pathway_has_enzyme ON pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein  " +
			//						"INNER JOIN pathway_has_reaction ON pathway_has_enzyme.pathway_idpathway = pathway_has_reaction.pathway_idpathway  " +
			//						"WHERE pathway_has_reaction.reaction_idreaction = idreaction " +
			//						"AND reaction_has_enzyme.enzyme_protein_idprotein = '"+ids.get(selectedRow)+"' " +
			//						"AND reaction_has_enzyme.enzyme_ecnumber = '"+enzymes[s]+"'");
			//
			//				List<String> reactions_ids = new ArrayList<String>();
			//
			//				while(rs.next()) {
			//
			//					reactions_ids.add(rs.getString(1));
			//				}
			//
			//				rs= stmt.executeQuery("SELECT idreaction FROM reactions_view_noPath_or_noEC " +
			//						"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction=idreaction " +
			//						"WHERE enzyme_protein_idprotein = '"+ids.get(selectedRow)+"' AND enzyme_ecnumber = '"+enzymes[s]+"'");
			//
			//				while(rs.next()) {
			//
			//					reactions_ids.add(rs.getString(1));
			//				}
			//
			//				for(String idreaction: reactions_ids) {
			//
			//					stmt.execute("UPDATE reaction SET inModel = true, source = 'MANUAL' WHERE idreaction = '"+idreaction+"'");
			//				}
			//			}

			stmt.close();

		}
		catch (SQLException ex) {
			Workbench.getInstance().warn("An error occurred while inserting this protein!");
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @param name
	 * @param class_string
	 * @param inchi
	 * @param molecular_weight
	 * @param molecular_weight_kd
	 * @param molecular_weight_exp
	 * @param molecular_weight_seq
	 * @param pi
	 * @param synonyms
	 * @param enzymes
	 * @param inModel
	 */
	public boolean insertProtein(String name, String classString, String inchi, String molecular_weight,
			String molecular_weight_kd, String molecular_weight_exp, String molecular_weight_seq, String pi,
			String[] synonyms, String[] enzymes, Boolean[] inModel) {

		Statement stmt;

		try {

			stmt = this.connection.createStatement();
			
			int idNewProtein = ModelAPI.getProteinID(classString, name, stmt);
			String query;

			if(idNewProtein<0) {
				
				query = "INSERT INTO protein (name,class) VALUES('" + DatabaseUtilities.databaseStrConverter(name, this.connection.getDatabaseType()) + "','" + classString + "')";
				
				idNewProtein = ProjectAPI.executeAndGetLastInsertID(query, stmt);
//				idNewProtein = (this.select("SELECT LAST_INSERT_ID()"))[0][0];
			}
			
			//if(!class_string.equals("")){stmt.execute("UPDATE protein SET class = '" + class_string + "' WHERE  idprotein ='"+idNewprotein+"'");}
			if(!inchi.equals("")){
				query = "UPDATE protein SET inchi = '" + inchi + "' WHERE  idprotein ='"+idNewProtein+"';";
				ProjectAPI.executeQuery(query, stmt);
			}
			if(!molecular_weight.equals("")){
				query = "UPDATE protein SET molecular_weight ='" + molecular_weight +  "' WHERE  idprotein ='"+idNewProtein+"';";
				ProjectAPI.executeQuery(query, stmt);
			}
			if(!molecular_weight_exp.equals("")){
				query = "UPDATE protein SET molecular_weight_exp='" + molecular_weight_exp + "' WHERE  idprotein ='"+idNewProtein+"';";		
				ProjectAPI.executeQuery(query, stmt);
			}
			if(!molecular_weight_kd.equals("")){
				query = "UPDATE protein SET molecular_weight_kd='" + molecular_weight_kd + "' WHERE  idprotein ='"+idNewProtein+"';";
				ProjectAPI.executeQuery(query, stmt);
			}
			if(!molecular_weight_seq.equals("")){
				query = "UPDATE protein SET molecular_weight_seq='" + molecular_weight_seq + "' WHERE  idprotein ='"+idNewProtein+"';";
				ProjectAPI.executeQuery(query, stmt);
			}
			if(!molecular_weight_seq.equals("")){
				query = "UPDATE protein SET pi = '" + pi + "' WHERE  idprotein ='"+idNewProtein+"';";
				ProjectAPI.executeQuery(query, stmt);
			}

			for(int s=0; s<synonyms.length; s++) {

				if(!synonyms[s].equals("")) {
					
					boolean exists = ModelAPI.checkAliasExistence(idNewProtein, name, stmt);

					if(!exists) {
						
						query = "INSERT INTO aliases (class, alias, entity) VALUES('p','" + DatabaseUtilities.databaseStrConverter(synonyms[s], this.connection.getDatabaseType()) +"', "+idNewProtein+")";
						ProjectAPI.executeQuery(query, stmt);
					}
				}
			}

			List<String> enzymes_ids = new ArrayList<>();
			int i = 0;
			for(String id : enzymes) {

				enzymes_ids.add(i,id);
				i++;
			}

			for(String id : enzymes_ids) {
				
				boolean exists = ModelAPI.checkEnzymeInModelExistence(idNewProtein, id, stmt);

				if(!exists) {
				
					query = "INSERT INTO enzyme (inModel, source, ecnumber, protein_idprotein) " +
							"VALUES (true, 'MANUAL', '"+id+"', '"+idNewProtein+"') ";
					ProjectAPI.executeQuery(query, stmt);
				}
				
				Proteins.insertEnzymes(idNewProtein, id, stmt, false);
			}

			//			for(int s=0; s<enzymes.length; s++) {
			//
			//				if(!enzymes[s].equals("")) {
			//					
			//					stmt.execute("INSERT INTO enzyme (protein_idprotein, ecnumber, inModel, source) VALUES('"+idNewProtein+"','" + enzymes[s] +"', "+inModel[s]+",'MANUAL')");
			//					
			//					ResultSet rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reaction " +
			//							"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = idreaction " +
			//							"INNER JOIN pathway_has_enzyme ON pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein  " +
			//							"INNER JOIN pathway_has_reaction ON pathway_has_enzyme.pathway_idpathway = pathway_has_reaction.pathway_idpathway  " +
			//							"WHERE pathway_has_reaction.reaction_idreaction = idreaction " +
			//							"AND reaction_has_enzyme.enzyme_protein_idprotein = "+idNewProtein+ " " +
			//							"AND reaction_has_enzyme.enzyme_ecnumber = '"+enzymes[s]+"'");
			//
			//					Set<String> reactions_ids = new HashSet<String>();
			//
			//					while(rs.next()) {
			//
			//						reactions_ids.add(rs.getString(1));
			//					}
			//
			//					rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reactions_view_noPath_or_noEC " +
			//							"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction=idreaction " +
			//							"WHERE enzyme_protein_idprotein = "+idNewProtein+" AND enzyme_ecnumber = '"+enzymes[s]+"'");
			//
			//					while(rs.next()) {
			//
			//						reactions_ids.add(rs.getString(1));
			//					}
			//
			//					for(String idreaction: reactions_ids) {
			//
			//						stmt.execute("UPDATE reaction SET inModel = true, source = 'MANUAL' WHERE idreaction = "+idreaction);
			//					}
			//					
			//				}
			//			}
			stmt.close();
			
		}
		catch (SQLException ex) {
			
			Workbench.getInstance().warn("An error occurred while inserting this protein!");
			ex.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * @param selectedRow
	 * @param ecnumber
	 * @param enzymes_ids
	 * @param inModel
	 * @param stmt
	 * @throws SQLException 
	 */
	public static void removeEnzymesAssignmensts(String ecnumber, List<String> enzymes_ids, Boolean[] inModel, Statement stmt, int proteinID, boolean removeReaction, DatabaseType databaseType) throws SQLException {

		String ec = DatabaseUtilities.databaseStrConverter(ecnumber, databaseType);
		
		if(removeReaction) {
			
			String query = "DELETE FROM enzyme WHERE protein_idprotein = "+proteinID+" AND ecnumber ='" + ec + "'";
			ProjectAPI.executeQuery(query, stmt);
		}
		else {
			
			String query = "UPDATE enzyme SET inModel=false WHERE protein_idprotein = "+proteinID+" AND ecnumber ='" + ec + "'";
			ProjectAPI.executeQuery(query, stmt);
		}

		
		
		Set<Integer> reactionsIDs = ModelAPI.getReactionsIDs(proteinID, ec, stmt);

		reactionsIDs = ModelAPI.getReactionsIDs2(reactionsIDs, proteinID, ec, stmt);
		
		for(int idreaction: reactionsIDs) {

			List<String[]> proteins_array = new ArrayList<String[]>();

			ArrayList<String[]> result = ModelAPI.getReactionHasEnzymeData2(idreaction, stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				if(Integer.parseInt(list[0])==proteinID && ecnumber.equalsIgnoreCase(list[1])) {}
				else {

					if(Integer.parseInt(list[0])==proteinID && enzymes_ids.contains(list[1])) {

						if(inModel[enzymes_ids.indexOf(list[1])]) {

							proteins_array.add(new String[] {list[0],list[1]});
						}
					}
					else {

						proteins_array.add(new String[] {list[0],list[1]});
					}
				}
			}

			if(proteins_array.isEmpty()) {

				String query = "UPDATE reaction SET inModel = false, source = 'MANUAL' WHERE idreaction = "+idreaction;
				ProjectAPI.executeQuery(query, stmt);
			}
		}
	}
	
	/**
	 * @param idProtein
	 * @param ecnumber
	 * @param stmt
	 * @throws SQLException
	 */
	public static void insertEnzymes(int idProtein, String ecnumber, Statement stmt, boolean editedReaction) throws SQLException {

		String aux = "";
		
		
		if(editedReaction) {
			
			aux =", source = MANUAL " ;
		}
		
		String query = "UPDATE enzyme SET inModel = true, source = 'MANUAL' WHERE ecnumber = '"+ecnumber+"' AND protein_idprotein = " + idProtein;
		ProjectAPI.executeQuery(query, stmt);
		
		if(editedReaction) {
			
			aux ="AND reaction_has_enzyme.enzyme_protein_idprotein = "+idProtein + " " ;
		}

		Set<String> reactionsIDs = ModelAPI.getDataFromReaction(aux, ecnumber, stmt);

		if(editedReaction) {
			
			aux =" AND enzyme_protein_idprotein = "+idProtein ;
		}
		
		reactionsIDs = ModelAPI.getDataFromReactionsViewNoPathOrNoEc(reactionsIDs, aux, ecnumber, stmt);

		for(String idreaction: reactionsIDs) {
			
			if(!editedReaction) {
				
				boolean exists = ModelAPI.checkEnzyme(idProtein, ecnumber, stmt);
				
				if(!exists) {

					query = "INSERT INTO enzyme (inModel, source, protein_idprotein, ecnumber) VALUES (true,'MANUAL',"+idProtein+",'"+ecnumber+"') ";
					ProjectAPI.executeQuery(query, stmt);
				}
				
				query = "INSERT INTO reaction_has_enzyme (reaction_idreaction, enzyme_protein_idprotein, enzyme_ecnumber) VALUES ("+idreaction+","+idProtein+",'"+ecnumber+"') ";
				ProjectAPI.executeQuery(query, stmt);
			}
			query = "UPDATE reaction SET inModel = true, source = 'MANUAL' WHERE idreaction = "+idreaction;
			ProjectAPI.executeQuery(query, stmt);
		}
		stmt.close();
	}

	/**
	 * @return
	 */
	public boolean existGenes() {

		Statement stmt;
		try {

			stmt = this.connection.createStatement();
			
			boolean result = ProjectAPI.checkGenes(stmt);
			
			stmt.close();
			return result;
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return false;
	}
}
