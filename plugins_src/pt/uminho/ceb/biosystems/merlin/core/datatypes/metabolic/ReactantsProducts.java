package pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.CompartmentsAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;

@Datatype(structure= Structure.LIST, namingMethod="getName",removable=true)//,removeMethod ="remove")
public class ReactantsProducts extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Connection connection;

	/**
	 * @param dbt
	 * @param name
	 */
	public ReactantsProducts(Table dbt, String name) {
		super(dbt, name);
		this.connection=table.getConnection();
	}

//	/* (non-Javadoc)
//	 * @see datatypes.metabolic_regulatory.Entity#getName()
//	 */
//	public String getName() {
//		return "Reactants/Products";
//	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		List<String[]> res = new ArrayList<String[]>();
		Statement stmt;

		try {

			stmt = this.connection.createStatement();

			String aux = "";
			if(this.getProject().isCompartmentalisedModel())
				aux = aux.concat(" WHERE NOT originalReaction");
			else
				aux = aux.concat(" WHERE originalReaction");

			ArrayList<Set<String>> result = ModelAPI.getCompoundsReactions(aux, stmt);

			Set<String> reactants = result.get(0);
			Set<String> products = result.get(1);
			Set<String> reactionsReactants = result.get(2);
			Set<String> productsReactants = result.get(3);

			res.add(new String[] {"Number of reactants", ""+reactants.size()});
			res.add(new String[] {"Number of products", ""+products.size()});
			res.add(new String[] {"Number of reactions with reactants associated", ""+reactionsReactants.size()});
			res.add(new String[] {"Number of reactions with products associated", ""+productsReactants.size()});

			ArrayList<String[]> data = CompartmentsAPI.getReactantsInCompartment(aux, stmt);
			
			res.add(new String[] {});
			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);
				
				res.add(new String[] {"Number of reactants in compartment "+list[0], list[1]});
			}

			data = CompartmentsAPI.getProductsInCompartment(aux, stmt);

			res.add(new String[] {});
			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);

				res.add(new String[] {"Number of products in compartment "+list[0], list[1]});
			}

			Set<String> metabolites = new HashSet<String>(products);
			metabolites.retainAll(reactants);

			res.add(new String[] {});
			res.add(new String[] {"Metabolites that are reactants and products", ""+metabolites.size()});

			stmt.close();
		}
		
		catch(Exception e) {
			
			e.printStackTrace();
		}

		String[][] newRes = new String[res.size()][];

		for(int i=0; i<res.size() ;i++)
			newRes[i] = res.get(i);

		return newRes;
	}

	/**
	 * @param selection
	 * @return
	 */
	public GenericDataTable getDataReagentProduct(int selection, boolean encoded, ArrayList<Integer> types) {

		ArrayList<String> index = new ArrayList<String>();
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("info");
		columnsNames.add("metabolite");
		columnsNames.add("compartment");
		columnsNames.add("formula");
		columnsNames.add("KEGG id");
		columnsNames.add("biochemical reactions");
		columnsNames.add("transport reactions");

		GenericDataTable qrt = new GenericDataTable(columnsNames, this.name, "metabolites") {

			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {

				if (col==0) {

					return true;
				}
				else return false;
			}};

			Statement stmt;

			try {

				stmt = this.connection.createStatement();

				String aux = "";

				if(this.getProject().isCompartmentalisedModel()) 
					aux = aux.concat(" WHERE NOT originalReaction ");
				else
					aux = aux.concat(" WHERE originalReaction ");
				
				if(encoded)
					aux = aux.concat(" AND inModel ");
//				else
//					aux = aux.concat(" AND NOT inModel ");  we want "In model + not In Model", not just "not in model"

				String aux2 = " CASEWHEN (compound.name is NULL, 1, 0), ";
				if(this.connection.getDatabaseType().equals(DatabaseType.MYSQL)) 
				 aux2 = " IF(ISNULL(compound.name),1,0), ";
				
				//array structure
				// types = [Glycan , Compound , Drugs , All]
				
				String aux3 = "AND (";
				
				if (types.get(3) != 1){
					if (types.get(0) == 1)
						aux3 = aux3.concat("entry_type = 'GLYCAN' ");
					if (types.get(1) == 1){
						if (types.get(0) == 1)
							aux3 = aux3.concat("OR entry_type = 'COMPOUND' ");
						else 
							aux3 = aux3.concat("entry_type = 'COMPOUND' ");
					}
					if (types.get(2) == 1){
						if (types.get(0) == 1 || types.get(1) == 1)
							aux3 = aux3.concat("OR entry_type = 'DRUGS' ");
						else
							aux3 = aux3.concat("entry_type = 'DRUGS' ");
					}
					aux3 = aux3.concat(")");
				}
				else
					aux3 = "";
						
				ArrayList<String[]> data = ModelAPI.countNotTransport(aux, aux2, aux3, stmt);
					
				Map<String, Integer> sum_not_transport = new HashMap<>();
				
				for(int i=0; i<data.size(); i++){
					String[] list = data.get(i);
					
					String speciesId = list[0] +"_"+ list[1];
					sum_not_transport.put(speciesId, Integer.parseInt(list[2]));
				}
				
				data = ModelAPI.countTransport(aux, aux2, aux3, stmt);

				
				Map<String, Integer> sum_transport = new HashMap<>();
				
				for(int i=0; i<data.size(); i++){
					String[] list = data.get(i);
					
					String speciesId = list[0]+"_"+ list[1];
					sum_transport.put(speciesId, Integer.parseInt(list[2]));
				}
					
				//calculate which metabolites have both properties and add to table 
					
				Set<Integer> reversibleCompounds = ModelAPI.getReversibilities(aux, stmt);
						
				Set<String> both = new HashSet<>();
				
				data = ModelAPI.getMetabolitesWithBothProperties(aux, aux2, aux3, stmt);
				
				for(int i=0; i<data.size(); i++){
					String[] list = data.get(i);
					
					String speciesId = list[3]+"_"+list[7];
					
					if(!index.contains(speciesId)) {
						
						List<Object> ql = new ArrayList<Object>();

							ql.add("");
							ql.add(list[0]);
							ql.add(list[6]);
							ql.add(list[1]);
							ql.add(list[4]);
							
							int sumNT = 0;
							if(sum_not_transport.containsKey(speciesId))
								sumNT = sum_not_transport.get(speciesId);
							ql.add(sumNT+"");
							
							int sumT = 0;
							if(sum_transport.containsKey(speciesId))
								sumT = sum_transport.get(speciesId);
							ql.add(sumT+"");
							
							index.add(speciesId); // marker for species identifier
							
							if(Integer.parseInt(list[2])>1 || reversibleCompounds.contains(Integer.parseInt(list[3]))) {
						
							both.add(speciesId);
							
							qrt.addLine(ql, "both", speciesId);
						}
						else {
							
							qrt.addLine(ql, "other", speciesId);
						}
					}
				}
				
				// add metabolites have one property
				
				data = ModelAPI.getMetabolitesProperties(aux, aux2, aux3, stmt);
				
				for(int i=0; i<data.size(); i++){
					String[] list = data.get(i);
					
					String speciesId = list[3] + "_" + list[7];
					
					if(!both.contains(speciesId)) {
						
						int indexer = index.indexOf(speciesId);		
						 
						//if(!index.contains(speciesId)) 
						{

							List<Object> ql = new ArrayList<Object>();
							ql.add("");
							ql.add(list[0]);
							ql.add(list[6]);
							ql.add(list[1]);
							ql.add(list[4]);
							
							int sumNT = 0;
							if(sum_not_transport.containsKey(speciesId))
								sumNT = sum_not_transport.get(speciesId);
							ql.add(sumNT);
							
							int sumT = 0;
							if(sum_transport.containsKey(speciesId))
								sumT = sum_transport.get(speciesId);
							ql.add(sumT);
							
							String type = "both";
						
							if(Double.parseDouble(list[2])>0 && selection != 2)
								type = "product";
							else if(Double.parseDouble(list[2])<0 && selection != 3)
								type = "reactant";
							
//							query = "SELECT COUNT(reaction_idreaction) AS numR"+
//									" FROM compound " +
//									" INNER JOIN stoichiometry ON (compound_idcompound=idcompound) "+
//									" WHERE compound.idcompound="+groupSigns.getString("compound.idcompound");
//							
//							ResultSet reactionsCount2 = stmt.executeQuery(query);
//							
//							while(reactionsCount2.next())
//								ql.add(reactionsCount2.getString("numR"));
							
							if(indexer>0) 
								qrt.setLine(indexer, ql, type, speciesId);
							else
								qrt.addLine(ql, type, speciesId);
						}
					}
					
				}
				
				if(!encoded) {
					
						data = ModelAPI.getMetabolitesNotInModel(aux3, stmt);
						
						for(int i=0; i<data.size(); i++){
							String[] list = data.get(i);
							
							List<Object> ql = new ArrayList<Object>();
							
							ql.add("");
							ql.add(list[1]);
							ql.add("-");
							ql.add(list[3]);
							ql.add(list[2]);
							ql.add("0");
							ql.add("0");
							
							String speciesId = list[0];
							
							speciesId = speciesId.concat("_0"); //to avoid error
							
							qrt.addLine(ql, "", speciesId);
						}
					}
					stmt.close();
				//} 
			}
			catch (SQLException e) {e.printStackTrace();}

			return qrt;
	}	
	
	/**
	 * @param rec
	 * @return
	 */
	public DataTable[] getReaction(String rec, String compartment) {
		
		DataTable[] res = new DataTable[3];
		
		rec = rec.split("_")[0];
		
		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Entry Type");
		res[1] = new DataTable(columnsNames, "entry type");

		columnsNames = new ArrayList<String>();
		columnsNames.add("Names");
		res[2] = new DataTable(columnsNames, "synonyms");
		
		try {

			Statement	stmt = this.connection.createStatement();
			
			res[1].addLine(ProjectAPI.getEntryType(rec, stmt));
			
			res[2].addLine(ProjectAPI.getAliasClassC(rec, stmt));
			
			columnsNames = new ArrayList<String>();
			columnsNames.add("reaction name");
			columnsNames.add("equations");
			columnsNames.add("source");
			columnsNames.add("in model");
			columnsNames.add("reversible");
			
			res[0] = new DataTable(columnsNames, "reactions");

			String aux = "";
			if(this.getProject().isCompartmentalisedModel()) {

				aux = aux.concat(" WHERE NOT originalReaction ");
			}
			else {

				aux = aux.concat(" WHERE originalReaction ");
			}
			
			List<ArrayList<String>> lines = ModelAPI.getReactions(aux, rec, compartment, stmt);
			
			for(ArrayList<String> line : lines)
				res[0].addLine(line);
			
			stmt.close();
		} 
		catch (SQLException e) {e.printStackTrace();}
		return res;
		
	}
	
	/* (non-Javadoc)
	 * @see pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity#getName(java.lang.String)
	 */
	public String getName(String id) {
		
		return "metabolites";
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getSingular()
	 */
	public String getSingular() {
		
		return "metabolite: ";
	}
	
	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#hasWindow()
	 */
	public boolean hasWindow() {
		
		return true;
	}
	
	public void updateData(String name, String entryType, String formula, String molecularW, String charge, String metabolite, String keggID){
		
		try {
			Statement	stmt = this.connection.createStatement();
			String query = "UPDATE compound SET compound.name = '" + name + "', entry_type = '" + entryType + "', formula = '" + 
					formula + "', molecular_weight = '" + molecularW + "' , charge ='" + charge + "' WHERE kegg_id = '"+ keggID +"';";
			
			ProjectAPI.executeQuery(query, stmt);

			stmt.close();
			
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public void insertData(String name, String entryType, String formula, String molecularW, String charge){
		
		try {
			Statement stmt = this.connection.createStatement();
			
			String dbName = this.getProject().getDatabase().getDatabaseName();
			int newCompoundID = ModelAPI.getAutoIncrementValue(stmt, dbName, "compound");
			String newCompoundKeggID = "M".concat(Integer.toString(newCompoundID));
			
			String query = "INSERT INTO compound (compound.name, compound.kegg_id, entry_type, formula, molecular_weight, charge)" 
			+ "VALUES ('" + name + "', '" + newCompoundKeggID + "', '" + entryType + "', '" + formula + "' , '" + molecularW + "' , '" + charge + "');";

			ProjectAPI.executeQuery(query, stmt);
			
			stmt.close();
			
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
	}
	
	public int getMetaboliteOccurrence (String name){
		
		int res = 0;
		
		try {

			Statement	stmt = this.connection.createStatement();
			
			res = ProjectAPI.countCompoundsByName(name, stmt);
			
			stmt.close();
			
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public String getKeggIdOccurence (String name){
		
		String res = "";
		
		try {

			Statement	stmt = this.connection.createStatement();
			
			res = ProjectAPI.isMetaboliteEditable(name, stmt);
			
			stmt.close();
			
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}
	
	public void deleteMetabolite (String metabolite){
		
		try {

			Statement	stmt = this.connection.createStatement();
			String query = "DELETE FROM compound WHERE kegg_id = '" + metabolite + "';";
			
			ProjectAPI.executeQuery(query, stmt);

			stmt.close();
			
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	public ArrayList<String> getMetaboliteData (String metabolite) {
		
		ArrayList<String> res = new ArrayList<String>();
		
		try {
			
			Statement	stmt = this.connection.createStatement();
			
			res = ProjectAPI.getMetaboliteData(metabolite, stmt);
			
			stmt.close();
			
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	public ArrayList<String> getRelatedReactions(String name){
		
		ArrayList<String> reactions = new ArrayList<>();
			
		try {
			
			Statement	stmt = this.connection.createStatement();
			
			reactions = ProjectAPI.getRelatedReactions(name, stmt);
			
			stmt.close();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return reactions;
	}
}
