package pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.HomologyAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseUtilities;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

@Datatype(structure = Structure.LIST,namingMethod="getName")
public class Genes extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private TreeMap<String,String> names;
	private TreeMap<Integer,String> ids;
	//private TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy;
	private Connection connection;

	/**
	 * @param dbt
	 * @param name
	 * @param ultimlyComplexComposedBy
	 */
	public Genes(Table dbt, String name, TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy) {

		super(dbt, name);
		//this.ultimlyComplexComposedBy = ultimlyComplexComposedBy;
		this.connection=dbt.getConnection();
	}

	/**
	 * @param row
	 */
	public void removeGene(int row, boolean encodedOnly) {

		Statement stmt;

		try  {

			stmt = this.connection.createStatement();

			if(row==-1) {

				String aux ="";
				if(encodedOnly) {

					aux = "INNER JOIN subunit ON (gene.idgene = gene_idgene) "+
							"INNER JOIN enzyme ON subunit.enzyme_protein_idprotein = enzyme.protein_idprotein;";
				}

				Set<String> genes = HomologyAPI.getGenesID(aux, stmt);

				for(String geneID : genes) {

					Set<String> proteins = HomologyAPI.getProteinIdAndEcNumber(geneID, stmt);

					for(String id : proteins) {

						this.removeGeneAssignemensts(geneID, id, stmt);
					}

					String query = "DELETE FROM gene where idgene = "+geneID;
					
					ProjectAPI.executeQuery(query, stmt);
				}
			}
			else {

				String geneID = ids.get(row);
				
				Set<String> proteins = HomologyAPI.getProteinIdAndEcNumber(geneID, stmt);

				for(String id : proteins)
					this.removeGeneAssignemensts(geneID, id, stmt);
					
				String query = "DELETE FROM gene where idgene = "+geneID;
				
				ProjectAPI.executeQuery(query, stmt);
			}
			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
	} 

	/**
	 * @return
	 */
	public String[][] getChromosomes() {

		Statement stmt;
		String[][] res = null;

		try {

			stmt = this.connection.createStatement();

			res = HomologyAPI.getChromosomes(stmt);
			

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		String[][] ch = new String[2][1];

		if(res!= null) {

			ch = new String[2][res.length+1];
			ch[0][0]="dummy";
			ch[1][0]="";
			for(int i = 0; i<res.length;i++) {

				ch[0][i+1]= res[i][0];
				ch[1][i+1]= res[i][1];
			}
		}

		return ch;
	} 

	/**
	 * @return
	 */
	public String[][] getProteins() {

		Statement stmt;
		String[][] res = null;

		try  {

			stmt = this.connection.createStatement();
			
			res = HomologyAPI.getProteins(stmt);

			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		String[][] prt = new String[2][res.length+1];
		prt[0][0]="dummy";
		prt[1][0]="";

		for(int i = 0; i<res.length;i++) {

			prt[0][i+1]= res[i][0];
			prt[1][i+1]= res[i][1];
		}
		return prt;
	} 

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		int num=0;
		//int noseq=0;
		int noname=0;
		//int nboolean_rule=0;

		Statement stmt;
		String[][] res = new String[9][];

		try {

			stmt = this.connection.createStatement();
			
			ArrayList<String> result = ProjectAPI.getLocusTagAndName(this.table.getName(), stmt);

			for(int i=0; i<result.size(); i++) {

				num++;
				//				if(rs.getString(2)==null)
				//					noseq++;
				if(result.get(i)==null || result.get(i).isEmpty())
					noname++;
				//if(rs.getString(4)==null) nboolean_rule++;
			}

			int position = 0;

			res[position++] = new String[] {"Number of genes", ""+num};
			res[position++] = new String[] {"Number of genes with no name associated", ""+noname};
			//res[position++] = new String[] {"Number of genes with no chromosome associated", ""+noseq};
			//res[position++] = new String[] {"Number of genes with no boolean rule associated", ""+nboolean_rule};

			String snumgenes = ProjectAPI.countGenesSynonyms(stmt);

			res[position++] = new String[] {"Number of genes' synonyms", snumgenes};

			double synmed = (new Double(snumgenes)).intValue()/(new Double(num));

			NumberFormat formatter = NumberFormat.getNumberInstance();
			formatter.setMaximumFractionDigits(5);

			String synmedFormatted = formatter.format(synmed);
			res[position++] = new String[] {"Average synonym number by gene", synmedFormatted};

			int prot=ProjectAPI.countGenesEncodingProteins(stmt);

			Pair<Integer, Integer> pair = ProjectAPI.countGenesEncodingEnzymesAndTransporters(stmt);
			
			int enz = pair.getA();
			int trp = pair.getB();

			res[position++] = new String[] {"Number of genes that encode proteins", prot+""};
			int both = enz + trp - prot;

			res[position++] = new String[] {"       Number of genes that only encode enzymes", enz-both+""};
			res[position++] = new String[] {"       Number of genes that only encode transporters", trp-both+""};
			res[position++] = new String[] {"       Number of genes that encode both", both+""};
			
			int inModel= ProjectAPI.countGenesInModel(stmt);
			
			res[position++] = new String[] {"      Number of genes in model", inModel+""};

			//			rs = stmt.executeQuery("SELECT count(distinct(gene_idgene)) FROM transcription_unit_gene");
			//
			//			rs.next();
			//
			//			res[9] = new String[] {"			Number of genes that belong to transcription units", rs.getString(1)};
			//
			//			rs = stmt.executeQuery("SELECT distinct(gene_idgene) " +
			//					"FROM subunit JOIN gene ON gene_idgene=idgene " +
			//					"WHERE enzyme_protein_idprotein IN (SELECT protein_idprotein " +
			//					"FROM regulatory_event)"
			//					);
			//
			//			LinkedList<String> tempGenes = new LinkedList<String>();
			//
			//			while(rs.next()) {
			//				
			//				tempGenes.add(rs.getString(1));
			//			}
			//
			//			rs = stmt.executeQuery("SELECT protein_idprotein " +
			//					"FROM regulatory_event"
			//					);
			//
			//			while(rs.next()) {
			//				
			//				if(this.ultimlyComplexComposedBy.containsKey(rs.getString(1))) {
			//					
			//					LinkedList<String> ghenes = 
			//							this.ultimlyComplexComposedBy.get(rs.getString(1));
			//					for(int i=0;i<ghenes.size();i++) 
			//						if(!tempGenes.contains(ghenes.get(i))) 
			//							tempGenes.add(ghenes.get(i));
			//				}
			//			}
			//
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(gene_idgene) " +
			//							"FROM subunit JOIN sigma_promoter ON subunit.enzyme_protein_idprotein = sigma_promoter.protein_idprotein"
			//					);
			//
			//			while(rs.next()) {
			//				
			//				if(!tempGenes.contains(rs.getString(1))) tempGenes.add(rs.getString(1));
			//			}
			//
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(protein_composition.protein_idprotein) " +
			//							"FROM sigma_promoter " +
			//							"JOIN protein_composition ON " +
			//							"protein_composition.protein_idprotein = sigma_promoter.protein_idprotein"
			//					);
			//
			//			while(rs.next()) {
			//				
			//				if(this.ultimlyComplexComposedBy.containsKey(rs.getString(1)))
			//				{
			//					LinkedList<String> ghenes = 
			//							this.ultimlyComplexComposedBy.get(rs.getString(1));
			//					for(int i=0;i<ghenes.size();i++) 
			//						if(!tempGenes.contains(ghenes.get(i))) tempGenes.add(ghenes.get(i));
			//				}
			//			}
			//
			//			res[10] = new String[] {"Number of regulatory genes", ""+tempGenes.size()};
			//
			//			rs = stmt.executeQuery("SELECT count(distinct(gene.idgene)) " +
			//					"FROM regulatory_event as event, transcription_unit, " +
			//					"transcription_unit_gene AS tug, transcription_unit_promoter as tup, " +
			//					"promoter,gene WHERE event.promoter_idpromoter=idpromoter AND " +
			//					"tup.promoter_idpromoter=idpromoter AND " +
			//					"tup.transcription_unit_idtranscription_unit=idtranscription_unit AND " +
			//					"tug.transcription_unit_idtranscription_unit=idtranscription_unit AND gene_idgene=idgene"
			//					);
			//
			//			rs.next();
			//
			//			res[11] = new String[] {"Number of regulated genes", rs.getString(1)};
			//
			//			//stmt = dsa.createStatement();
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(gene_idgene) FROM subunit " +
			//							"JOIN enzyme ON subunit.enzyme_protein_idprotein = enzyme.protein_idprotein"
			//					);
			//
			//			LinkedList<String> enzyme_genes = new LinkedList<String>();
			//
			//			while(rs.next())
			//			{
			//				enzyme_genes.add(rs.getString(1));
			//			}
			//
			//			//stmt = dsa.createStatement();
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(protein_composition.protein_idprotein) " +
			//							"FROM enzyme " +
			//							"JOIN protein_composition ON protein_composition.protein_idprotein = enzyme.protein_idprotein"
			//					);
			//
			//			while(rs.next())
			//			{
			//				if(this.ultimlyComplexComposedBy.containsKey(rs.getString(1)))
			//				{
			//					LinkedList<String> ghenes = this.ultimlyComplexComposedBy.get(rs.getString(1));
			//					for(int i=0;i<ghenes.size();i++) 
			//						if(!enzyme_genes.contains(ghenes.get(i))) enzyme_genes.add(ghenes.get(i));
			//				}
			//			}
			//
			//			res[12] = new String[] {"Number of genes that encode enzymes", ""+enzyme_genes.size()};
			//
			//			//stmt = dsa.createStatement();
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(gene_idgene) FROM subunit " +
			//							"JOIN regulatory_event ON subunit.enzyme_protein_idprotein = regulatory_event.protein_idprotein"
			//					);
			//
			//			LinkedList<String> tf_genes = new LinkedList<String>();
			//
			//			while(rs.next())
			//			{
			//				tf_genes.add(rs.getString(1));
			//			}
			//
			//			//stmt = dsa.createStatement();
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(protein_composition.protein_idprotein) " +
			//							"FROM regulatory_event " +
			//							"JOIN protein_composition ON " +
			//							"protein_composition.protein_idprotein = regulatory_event.protein_idprotein"
			//					);
			//
			//			while(rs.next())
			//			{
			//				if(this.ultimlyComplexComposedBy.containsKey(rs.getString(1)))
			//				{
			//					LinkedList<String> ghenes = this.ultimlyComplexComposedBy.get(rs.getString(1));
			//					for(int i=0;i<ghenes.size();i++) 
			//						if(!tf_genes.contains(ghenes.get(i))) tf_genes.add(ghenes.get(i));
			//				}
			//			}
			//
			//			res[13] = new String[] {"Number of genes that encode TFs", ""+tf_genes.size()};
			//
			//			//stmt = dsa.createStatement();
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(gene_idgene) " +
			//							"FROM subunit JOIN sigma_promoter ON subunit.enzyme_protein_idprotein = sigma_promoter.protein_idprotein"
			//					);
			//
			//			LinkedList<String> sigma_genes = new LinkedList<String>();
			//
			//			while(rs.next())
			//			{
			//				sigma_genes.add(rs.getString(1));
			//			}
			//
			//			//stmt = dsa.createStatement();
			//			rs = stmt.executeQuery(
			//					"SELECT distinct(protein_composition.protein_idprotein) " +
			//							"FROM sigma_promoter " +
			//							"JOIN protein_composition ON " +
			//							"protein_composition.protein_idprotein = sigma_promoter.protein_idprotein"
			//					);
			//
			//			while(rs.next())
			//			{
			//				if(this.ultimlyComplexComposedBy.containsKey(rs.getString(1)))
			//				{
			//					LinkedList<String> ghenes = this.ultimlyComplexComposedBy.get(rs.getString(1));
			//					for(int i=0;i<ghenes.size();i++) 
			//						if(!sigma_genes.contains(ghenes.get(i))) sigma_genes.add(ghenes.get(i));
			//				}
			//			}
			//
			//			LinkedList<String> snork_genes = new LinkedList<String>();
			//
			//			for(int i=0;i<enzyme_genes.size();i++) 
			//				if(!snork_genes.contains(enzyme_genes.get(i))) snork_genes.add(enzyme_genes.get(i));
			//
			//			for(int i=0;i<tf_genes.size();i++) 
			//				if(!snork_genes.contains(tf_genes.get(i))) snork_genes.add(tf_genes.get(i));
			//
			//			for(int i=0;i<sigma_genes.size();i++) 
			//				if(!snork_genes.contains(sigma_genes.get(i))) snork_genes.add(sigma_genes.get(i));
			//
			//			res[14] = new String[] {"Non coding genes", ""+(num-snork_genes.size())};
			//
			//			rs = stmt.executeQuery("SELECT count(*) FROM transcription_unit");
			//			rs.next();
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return res;
	}

	/**
	 * @return
	 */
	public GenericDataTable getAllGenes() {
		
		names = new TreeMap<String,String>();
		ids = new TreeMap<Integer,String>();

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("info");
		columnsNames.add("locus tag");
		columnsNames.add("names");
		columnsNames.add("number of encoding subunits");
		columnsNames.add("number of encoded proteins");
		
		GenericDataTable qrt = new GenericDataTable(columnsNames, "genes", "gene data"){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {

				if (col==0) {

					return true;
				}
				else return false;
			}
		};

		try {
			Statement stmt = this.connection.createStatement();

			ArrayList<String[]> result = ModelAPI.getAllGenes(stmt);
			int g = 0;

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");
				ids.put(g, list[0]);
				ql.add(list[1]);
				ql.add(list[2]);
				ql.add(list[3]);
				ql.add(list[4]);
				qrt.addLine(ql, list[0]);
				names.put(list[0], list[1]);
				g+=1;
			}
			stmt.close();

		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return qrt;
	}

	/**
	 * @return
	 */
	public GenericDataTable getRegulatoryGenes() {

		names = new TreeMap<String,String>();

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("info");
		columnsNames.add("names");
		columnsNames.add("numbers");

		GenericDataTable qrt = new GenericDataTable(columnsNames, "genes", "gene data"){
			private static final long serialVersionUID = 6629060675011336218L;
			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}
		};

		try {

			Statement stmt = this.connection.createStatement();
			
			ArrayList<String[]> result = ModelAPI.getRegulatoryGenes(stmt);
			
			int g = 0;

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");
				ids.put(g, list[0]);
				ql.add(list[1]);
				ql.add(list[2]);
				qrt.addLine(ql, list[0]);
				names.put(list[0], list[1]);
				g+=1;
			}
			stmt.close();

		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return qrt;
	}

	/**
	 * @return
	 */
	public GenericDataTable getEncodingGenes() {

		names = new TreeMap<String,String>();
		ids = new TreeMap<Integer,String>();

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("info");
		columnsNames.add("locus tag");
		columnsNames.add("names");
		columnsNames.add("number of encoding subunits");
		columnsNames.add("number of encoded proteins");

		GenericDataTable qrt = new GenericDataTable(columnsNames, "genes", "gene data"){
			private static final long serialVersionUID = 6629060675011336218L;
			@Override
			public boolean isCellEditable(int row, int col) {

				if (col==0) {

					return true;
				}
				else return false;
			}
		};

		try {

			Statement stmt = this.connection.createStatement();

			ArrayList<String[]> result = ModelAPI.getEncodingGenes(stmt);

			int g = 0;
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");
				ids.put(g, list[0]);
				ql.add(list[1]);
				ql.add(list[2]);
				ql.add(list[3]);
				ql.add(list[4]);
				qrt.addLine(ql, list[0]);
				names.put(list[0], list[1]);
				g+=1;
			}
			stmt.close();
		} 
		catch (SQLException ex) {
			ex.printStackTrace();
		}
		return qrt;
	}

	/**
	 * @return
	 */
	public GenericDataTable getRegulatedGenes() {

		names = new TreeMap<String,String>();

		ArrayList<String> columnsNames = new ArrayList<String>();

		columnsNames.add("Info");
		columnsNames.add("Names");
		columnsNames.add("Bnumbers");

		GenericDataTable qrt = new GenericDataTable(columnsNames, "Genes", "Gene data"){
			private static final long serialVersionUID = 6629060675011336218L;
			@Override
			public boolean isCellEditable(int row, int col){
				if (col==0)
				{
					return true;
				}
				else return false;
			}
		};
		try
		{
			//MySQLMultiThread dsa =  new MySQLMultiThread( this.host,this.port, this.dbName, this.user, this.pass);
			Statement stmt;

			stmt = this.connection.createStatement();

			ArrayList<String[]> result = ModelAPI.getRegulatedGenes(stmt);

			//        int ncols = rs.getMetaData().getColumnCount();

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				ArrayList<Object> ql = new ArrayList<Object>();
				ql.add("");
				for(int j=0;j<2;j++)
				{
					String in = list[j];

					if(in!=null) ql.add(in);
					else ql.add("");
				}
				qrt.addLine(ql, list[2]);
				names.put(list[2], list[0]);
			}
			stmt.close();
		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		return qrt;
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	public DataTable[] getRowInfo(String id) {

		boolean regulation = false, eprotein = false, ecompartment = false, orthology = false;

		DataTable[] results = new DataTable[5];

		DataTable[] results2;

		int tabs=1;

		List<String> columnsNames = new ArrayList<String>();
		columnsNames.add("Synonyms");
		results[0] = new DataTable(columnsNames, "Synonyms");


		columnsNames = new ArrayList<String>();
		columnsNames.add("Regulations");
		results[1] = new DataTable(columnsNames, "Regulations");


		columnsNames = new ArrayList<String>();
		columnsNames.add("Ortholog");
		columnsNames.add("Homologue ID");
		columnsNames.add("Similarity");
		results[2] = new DataTable(columnsNames, "Orthologs");


		columnsNames = new ArrayList<String>();
		columnsNames.add("encoded proteins");
		columnsNames.add("class");
		columnsNames.add("identifier");
		results[3] = new DataTable(columnsNames, "encoded proteins");

		columnsNames = new ArrayList<String>();
		columnsNames.add("Compartment");
		columnsNames.add("Score");
		columnsNames.add("Primary Location");
		results[4] = new DataTable(columnsNames, "Compartments");

		Statement stmt;

		try {

			stmt = this.connection.createStatement();
			
			ArrayList<String> result = ProjectAPI.getAliasClassG(id, stmt);

			for(int i=0; i<result.size(); i++){
	
				ArrayList<String> resultsList = new ArrayList<String>();
				resultsList.add(result.get(i));
				results[0].addLine(resultsList);
			}

			result = ProjectAPI.getProteinIDs(id, stmt);

			for(int i=0; i<result.size(); i++){

				regulation = true;
				ArrayList<String> resultsList = new ArrayList<String>();
				resultsList.add(result.get(i));
				results[1].addLine(resultsList);
			}

			ArrayList<String[]> data = ProjectAPI.getDataFromSubunit(id, stmt);

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);

				eprotein = true;
				ArrayList<String> resultsList = new ArrayList<String>();
				resultsList.add(list[0]);
				resultsList.add(list[1]);
				resultsList.add(list[2]);
				results[3].addLine(resultsList);
			}

			data = ProjectAPI.getDataFromGeneHasOrthology(id, stmt);

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);

				orthology = true;
				ArrayList<String> resultsList = new ArrayList<String>();
				resultsList.add(list[0]);
				resultsList.add(list[1]);
				resultsList.add(list[2]);
				results[2].addLine(resultsList);
			}

			data = ProjectAPI.getDataFromGene(id, stmt);

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);
				
				ecompartment=true;
				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[1]);
				ql.add(list[3]);
				
				if(Boolean.valueOf(list[2]))
					ql.add(list[2]);
				else
					ql.add("");

				results[4].addLine(ql);
			}
			stmt.close();

		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		if(regulation) 
			tabs++;
		if(orthology)
			tabs++;
		if(eprotein)
			tabs++;
		if(ecompartment)
			tabs++;

		results2 = new DataTable[tabs];

		tabs = 0;

		if(eprotein) {

			results2[tabs] = results[3];
			tabs++;
		}

		if(orthology) {

			results2[tabs] = results[2];
			tabs++;
		}

		results2[tabs] = results[0];

		if(regulation) {
			tabs++;
			results2[tabs] = results[1];
		}
		
		if(ecompartment) {
			tabs++;
			results2[tabs] = results[4];
		}
		
		return results2;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getGeneName(String id) {

		return this.names.get(id);
	}

	/**
	 * @param selectedRow
	 * @return
	 */
	public String[] getGeneData(int selectedRow) {

		Statement stmt;
		String[] data = new String[8];

		try {

			stmt = this.connection.createStatement();

			data = ProjectAPI.getGeneData(ids.get(selectedRow), stmt);

			stmt.close();
		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		//		res = this.getDbt().getDsa().select( "SELECT idProtein FROM Protein JOIN subunit ON (idProtein = protein_idProtein) WHERE gene_idgene ="+ids.get(selectedRow));
		//
		//		if(res.length>0)
		//		{
		//			data[6]=res[0][0];
		//		}
		return data;
	}

	/**
	 * @param selectedRow
	 * @return
	 */
	public String[] getSubunits(int selectedRow) {

		Statement stmt;
		String[][] res = null;

		try {

			stmt = this.connection.createStatement();
			
			res = ProjectAPI.getSubunits(ids.get(selectedRow), stmt);

			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
		String[] sub;

		if(res.length>0) {

			sub = new String[res.length];

			for(int i=0; i<res.length;i++) {

				sub[i]=res[i][0];
			}
		}
		else 
			sub = new String[0];
		
		return sub;
	}

	/**
	 * @param idChromosome
	 * @param name
	 * @param transcription_direction
	 * @param left_end_position
	 * @param right_end_position
	 * @param boolean_rule
	 * @param subunits
	 * @param locusTag
	 */
	public void insertNewGene(String idChromosome, String name,
			String transcription_direction, String left_end_position,
			String right_end_position, String[] subunits, String locusTag) {

		Statement stmt; 
		
		try {

			stmt = this.connection.createStatement();

			boolean next = ProjectAPI.checkChromosomeData(idChromosome, stmt);

			if(!next) {
				String query = "INSERT INTO chromosome (name) VALUES('null')";
				ProjectAPI.executeQuery(query, stmt);
			}
			
			String query;
				
			if(left_end_position.equals("")) {

//				sequence id does not have default value when inserting new gene.
//				has id or search it in the homology table or in the fasta Files.class
//				when annotation is loaded all genes are loaded
//				so inserting a new gene will only be present in the enzyme annotation data
//				if gene is not in the other table set  its compartment to cytosol compartmentalization
				
				query = "INSERT INTO gene (chromosome_idchromosome, sequence_id, name, locusTag, origin) VALUES('"+idChromosome+"', +'" + locusTag + "', '"+name+"', '"+locusTag+"','MANUAL')";
				
			}			
			else {

				query = "INSERT INTO gene (chromosome_idchromosome, name, transcription_direction, left_end_position, right_end_position, locusTag,origin) " +
						"VALUES('"+idChromosome+"', '"+DatabaseUtilities.databaseStrConverter(name, this.connection.getDatabaseType())+"', '"+transcription_direction+"', '"+
						DatabaseUtilities.databaseStrConverter(left_end_position, this.connection.getDatabaseType())+"', '"+
						DatabaseUtilities.databaseStrConverter(right_end_position, this.connection.getDatabaseType())+"',  '"+locusTag+"','MANUAL')";
				
			}
			//			String idNewGene = (this.select("SELECT LAST_INSERT_ID()"))[0][0];
			
			int idNewGene = ProjectAPI.executeAndGetLastInsertID(query, stmt);
			
			for(int s=0; s<subunits.length;s++) {

				if(!subunits[s].equals("dummy") && !subunits[s].isEmpty()) {

					//					rs = stmt.executeQuery("SELECT ecnumber FROM enzyme WHERE protein_idprotein = "+subunits[s]);
					//					List<String> ecn = new ArrayList<String>();
					//
					//					while(rs.next()) {
					//
					//						ecn.add(rs.getString(1));
					//					}
					//
					//					for(String e:ecn) {

					String proteinID=subunits[s].split("__")[0];
					String e=subunits[s].split("__")[1];

					query = "INSERT INTO subunit (enzyme_protein_idprotein, gene_idgene, enzyme_ecnumber) VALUES("+"'" + proteinID +"', '"+idNewGene+"', '"+e+"')";
					
					ProjectAPI.executeQuery(query, stmt);

					Set<String> reactionsIDs = ProjectAPI.getReactionID(proteinID, e, stmt);

					reactionsIDs = ProjectAPI.getReactionID2(reactionsIDs, proteinID, query, stmt);

					for(String idreaction: reactionsIDs) {

						query = "UPDATE reaction SET inModel = true, source = 'MANUAL' WHERE idreaction = '"+idreaction+"'";
						
						ProjectAPI.executeQuery(query, stmt);
					}
					//}
				}
			}
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();

		}
	}

	/**
	 * @param idChromosome
	 * @param name
	 * @param transcription_direction
	 * @param left_end_position
	 * @param right_end_position
	 * @param subunits
	 * @param selectedRow
	 * @param oldSubunits
	 * @param locusTag
	 */
	public void updateGene(String idChromosome, String name,
			String transcription_direction, String left_end_position,
			String right_end_position, String[] subunits, int selectedRow, String[] oldSubunits, String locusTag ) {

		Statement stmt; 
		try {

			stmt = this.connection.createStatement();

			if(left_end_position.equals("")) {

				String query = "UPDATE gene SET " +
						"chromosome_idchromosome =" + idChromosome + ", " +
						"name = '" + DatabaseUtilities.databaseStrConverter(name, this.connection.getDatabaseType()) + "', " +
						//"boolean_rule ='" + boolean_rule + "', " +
						"locusTag = '"+locusTag+"' "+
						"WHERE  idgene ="+ids.get(selectedRow);
				
				ProjectAPI.executeQuery(query, stmt);
			}			
			else {

				String query = "UPDATE gene SET "+
						"chromosome_idchromosome ='" + idChromosome + "', " +
						"name = '" +  DatabaseUtilities.databaseStrConverter(name, this.connection.getDatabaseType())+ "', " +
						"transcription_direction = '" + transcription_direction + "', " +
						"left_end_position ='" +  DatabaseUtilities.databaseStrConverter(left_end_position, this.connection.getDatabaseType()) +  "', " +
						"right_end_position='" + DatabaseUtilities.databaseStrConverter(right_end_position, this.connection.getDatabaseType()) + "', "+ 
						//"boolean_rule = '" + boolean_rule + "', " +
						"locusTag = '"+locusTag+"' "+
						"WHERE idgene =" + ids.get(selectedRow);
				
				ProjectAPI.executeQuery(query, stmt);
			}

			List<String> old_protein_ids = new ArrayList<String>();
			List<String> protein_ids = new ArrayList<String>();

			int i = 0;
			for(String id : oldSubunits) {

				old_protein_ids.add(i,id);
				i++;
			}

			i = 0;
			for(String id : subunits) {

				protein_ids.add(i,id);
				i++;
			}

			List<String> subunit_protein_id_add = new ArrayList<String>();

			for(String id : protein_ids) {

				if(!id.contains("dummy") && !id.isEmpty()) {

					if(old_protein_ids.contains(id)) {

						old_protein_ids.remove(id);
					}
					else {

						subunit_protein_id_add.add(id);
					}
				}
			}

			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			for(String id : old_protein_ids) {

				this.removeGeneAssignemensts(ids.get(selectedRow), id, stmt);
			}

			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			for(String protein_id_ec : protein_ids) {

				if(subunit_protein_id_add.contains(protein_id_ec)) {

					//					ResultSet rs = stmt.executeQuery("SELECT ecnumber FROM enzyme WHERE protein_idprotein = "+protein_id);
					//					List<String> ecn = new ArrayList<String>();
					//
					//					while(rs.next()) {
					//
					//						ecn.add(rs.getString(1));
					//					}
					//
					//					for(String ecnumber:ecn) {

					int protein_id = Integer.parseInt(protein_id_ec.split("__")[0]);
					String ecnumber=protein_id_ec.split("__")[1];

					String query = "INSERT INTO subunit (enzyme_protein_idprotein, gene_idgene, enzyme_ecnumber) VALUES(" + protein_id +", '"+ids.get(selectedRow)+"', '"+ecnumber+"')";
					
					ProjectAPI.executeQuery(query, stmt);

					Proteins.insertEnzymes(protein_id,ecnumber,stmt,true);

					//						stmt.execute("UPDATE enzyme SET inModel = true, source = 'MANUAL' WHERE ecnumber = '"+ecnumber+"' AND protein_idprotein = " + protein_id);
					//
					//						rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reaction " +
					//								"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = idreaction " +
					//								"INNER JOIN pathway_has_enzyme ON pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein  " +
					//								"INNER JOIN pathway_has_reaction ON pathway_has_enzyme.pathway_idpathway = pathway_has_reaction.pathway_idpathway  " +
					//								"WHERE pathway_has_reaction.reaction_idreaction = idreaction " +
					//								"AND reaction_has_enzyme.enzyme_protein_idprotein = "+protein_id + " " +
					//								"AND reaction_has_enzyme.enzyme_ecnumber = '"+ecnumber+"'");
					//
					//						Set<String> reactions_ids = new HashSet<String>();
					//
					//						while(rs.next()) {
					//
					//							reactions_ids.add(rs.getString(1));
					//						}
					//
					//						rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reactions_view_noPath_or_noEC " +
					//								"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction=idreaction " +
					//								"WHERE enzyme_protein_idprotein = "+protein_id+" AND enzyme_ecnumber = '"+ecnumber+"'");
					//
					//						while(rs.next()) {
					//
					//							reactions_ids.add(rs.getString(1));
					//						}
					//
					//						for(String idreaction: reactions_ids) {
					//
					//							stmt.execute("UPDATE reaction SET inModel = true, source = 'MANUAL' WHERE idreaction = "+idreaction);
					//						}
					//}
				}

				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			}
			stmt.close();

		}
		catch (SQLException ex) {

			ex.printStackTrace();
		}
	}

	/**
	 * @param selectedRow
	 * @param id
	 * @param stmt 
	 * @throws SQLException
	 */
	private void removeGeneAssignemensts(String gene_id, String id, Statement stmt) throws SQLException {

		String query = "DELETE FROM subunit WHERE gene_idgene = "+gene_id+" AND enzyme_protein_idprotein = " + id.split("__")[0]+" AND enzyme_ecNumber = '" + id.split("__")[1]+"'" ;
		
		ProjectAPI.executeQuery(query, stmt);

		boolean exists = ProjectAPI.checkSubunitData(id, stmt);

		if(!exists) {

			//rs = stmt.executeQuery("SELECT ecnumber FROM enzyme WHERE protein_idprotein = "+id);
			List<String> enzymes_ids = new ArrayList<>();
			enzymes_ids.add(id.split("__")[1]);

			Boolean[] inModel = new Boolean[enzymes_ids.size()];
			for(int i= 0; i< inModel.length; i++) {

				inModel[i]=false;
			}


			for(String e:enzymes_ids) {

				Proteins.removeEnzymesAssignmensts(e, enzymes_ids, inModel, stmt,  Integer.parseInt(id.split("__")[0]), false, this.connection.getDatabaseType());

				//				stmt.execute("UPDATE enzyme SET inModel = false, source = 'MANUAL' WHERE ecnumber = '"+e+"' AND protein_idprotein = " + id);
				//
				//				rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reaction " +
				//						"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction = idreaction " +
				//						"INNER JOIN pathway_has_enzyme ON pathway_has_enzyme.enzyme_protein_idprotein = reaction_has_enzyme.enzyme_protein_idprotein  " +
				//						"INNER JOIN pathway_has_reaction ON pathway_has_enzyme.pathway_idpathway = pathway_has_reaction.pathway_idpathway  " +
				//						"WHERE pathway_has_reaction.reaction_idreaction = idreaction " +
				//						"AND reaction_has_enzyme.enzyme_protein_idprotein = "+id +" " +
				//						"AND reaction_has_enzyme.enzyme_ecnumber = '"+e+"'");
				//
				//				Set<String> reactions_ids = new HashSet<String>();
				//
				//				while(rs.next()) {
				//
				//					reactions_ids.add(rs.getString(1));
				//				}
				//
				//				rs= stmt.executeQuery("SELECT DISTINCT idreaction FROM reactions_view_noPath_or_noEC " +
				//						"INNER JOIN reaction_has_enzyme ON reaction_has_enzyme.reaction_idreaction=idreaction " +
				//						"WHERE enzyme_protein_idprotein = "+id+" AND enzyme_ecnumber = '"+e+"'");
				//
				//				while(rs.next()) {
				//
				//					reactions_ids.add(rs.getString(1));
				//				}
				//
				//
				//				for(String idreaction: reactions_ids) {
				//
				//					List<String[]> proteins_array = new ArrayList<String[]>();
				//
				//					rs= stmt.executeQuery("SELECT enzyme_protein_idprotein, enzyme_ecnumber FROM reaction_has_enzyme " +
				//							"INNER JOIN enzyme ON (enzyme_protein_idprotein = enzyme.protein_idprotein AND enzyme_ecnumber = enzyme.ecnumber)"+
				//							"WHERE inModel AND reaction_idreaction = "+idreaction);
				//
				//					while(rs.next()) {
				//
				//						if(rs.getString(1).equalsIgnoreCase(id) && ecn.contains(rs.getString(2))) {}
				//						else {
				//
				//							proteins_array.add(new String[] {rs.getString(1),rs.getString(2)});
				//						}
				//					}
				//
				//					if(proteins_array.isEmpty()) {
				//
				//						stmt.execute("UPDATE reaction SET inModel = false, source = 'MANUAL' WHERE idreaction = "+idreaction);
				//					}
				//				}
			}
		}
	}
}