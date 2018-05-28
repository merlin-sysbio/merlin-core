package pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.TransportersAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.transporters.core.transport.reactions.containerAssembly.PopulateTransportContainer;
import pt.uminho.ceb.biosystems.merlin.transporters.core.transport.reactions.containerAssembly.TransportContainer;
import pt.uminho.ceb.biosystems.merlin.transporters.core.transport.reactions.containerAssembly.TransportReactionCI;
import pt.uminho.ceb.biosystems.merlin.utilities.External.ExternalRefSource;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.ContainerUtils;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.StoichiometryValueCI;

/**
 * @author Antonio Dias
 *
 */
@Datatype(structure= Structure.SIMPLE, namingMethod="getName",removable=true,removeMethod ="remove")
public class TransportersAnnotationDataContainer extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Connection connection;
	private TreeMap<String,String> names;
	private TreeMap<Integer,String> ids;
	private double alpha;
	private double threshold;

	/**
	 * @param dbt
	 * @param name
	 */
	public TransportersAnnotationDataContainer(Table dbt, String name) {
		
		super(dbt, name);
		this.connection=dbt.getConnection();
		this.alpha = -1;
		this.threshold = -1;
		
//		if(this.getProject().getTransportContainer()!=null)
//			try {
//				this.getProject().getTransportContainer().verifyDepBetweenClass();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getName()
	 */
	public String getName() {

		return "transporters";
	}


	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		String[][] res = new String[4][];

		try {

			Statement stmt = this.connection.createStatement();
			String genes = null,metabolites = null,uniprotID = null,tcNumbers = null;
			
//			String aux = "gene_to_metabolite_direction";
//			if(this.connection.getDatabaseType().equals(DatabaseType.H2)) { aux = "\"gene_to_metabolite_direction\""; }

			genes = TransportersAPI.getGenesCount(stmt);
			
//			rs = stmt.executeQuery("SELECT count(distinct(metabolite_id)) met FROM transporters_annotation_genes_has_metabolites");

			metabolites = TransportersAPI.getMetabolitesCount(stmt);
			
//			rs = stmt.executeQuery("SELECT count(distinct(uniprot_id)) uid FROM transporters_annotation_genes_has_tcdb_registries");

			uniprotID = TransportersAPI.getUniprotIDsCount(stmt);
			
//			rs = stmt.executeQuery("SELECT count(distinct(tr.tc_number)) tc FROM transporters_annotation_tcdb_registries tr "
//					+ "INNER JOIN transporters_annotation_genes_has_tcdb_registries gtr ON tr.uniprot_id=gtr.uniprot_id "
//					+ "AND tr.version=gtr.version");

			tcNumbers = TransportersAPI.getTcNumbersCount(stmt);

			res[0] = new String[] {"Number of genes", genes};
			res[1] = new String[] {"Number of metabolites", metabolites};
			res[2] = new String[] {"Number of UniProt Entries", uniprotID};
			res[3] = new String[] {"Number of tc_numbers", tcNumbers};

			stmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return res;
	}

	public GenericDataTable getInfo() {
		
		this.names = new TreeMap<String,String>();
		this.ids = new TreeMap<Integer,String>();

		ArrayList<String> columnsNames = new ArrayList<String>();
		
		columnsNames.add("info");
		columnsNames.add("genes");
		columnsNames.add("nº transmembrane domains");
		columnsNames.add("TC number");
		columnsNames.add("nº metabolites");
		columnsNames.add("nº transport reactions");
		columnsNames.add("select");
		
		GenericDataTable qrt = new GenericDataTable(columnsNames, "genes", "gene data"){
			private static final long serialVersionUID = 1L;
			@Override
			public boolean isCellEditable(int row, int col) {

				if (col==0 || col==6) {

					return true;
				}
				else return false;
			}
		};

		try {

			List<Object> ql = null;
			Map<String, String> tcr = new HashMap<>(), tcn = new HashMap<>(), mets = new HashMap<>();// tcn = new ArrayList<>(), tcn = new ArrayList<>();
			Statement stmt = this.connection.createStatement();

//			String aux = "gene_to_metabolite_direction";
//			if(this.connection.getDatabaseType().equals(DatabaseType.H2)) {
//
//				aux = "\"gene_to_metabolite_direction\"";
//			}
			
			mets = TransportersAPI.getMetabolites(stmt);
			
			tcn = TransportersAPI.getTcFamily(stmt);

			if(this.getProject().getTransportContainer()!=null) {
				for (String geneKey : this.getProject().getTransportContainer().getGenes().keySet()) {
//					int counter = 0;
//					
//					for(String reaction: this.getProject().getTransportContainer().getGene(geneKey).getReactionIds()) {
//
//						TransportReactionCI trci = this.getProject().getTransportContainer().getReaction(reaction).clone();
//
//						boolean go = true;
//
//						for(String key : new HashSet<>(trci.getReactants().keySet())) {
//
//							StoichiometryValueCI svci = trci.getReactants().get(key);
//							String met_id = this.getProject().getTransportContainer().getMetabolite(key).getName();
//							String kegg = ExternalRefSource.KEGG_CPD.getSourceId(this.getProject().getTransportContainer().getKeggMiriam().get(key));
//							if(kegg==null || kegg=="null")
//								go = false;
//
//							svci.setMetaboliteId(met_id);
//							trci.getReactants().put(met_id, svci);
//							trci.getReactants().remove(key);
//						}
//
//						for(String key : new HashSet<>(trci.getProducts().keySet())) {
//							
//							StoichiometryValueCI svci = trci.getProducts().get(key);
//							String met_id = this.getProject().getTransportContainer().getMetabolite(key).getName();
//							String kegg = ExternalRefSource.KEGG_CPD.getSourceId(this.getProject().getTransportContainer().getKeggMiriam().get(key));
//							if(kegg==null || kegg=="null")
//								go = false;
//
//							svci.setMetaboliteId(met_id);
//							trci.getProducts().put(met_id, svci);
//							trci.getProducts().remove(key);
//						}
//
//						if(go)							
//							counter++;
//				}
					
					tcr.put(geneKey, Integer.toString(this.getProject().getTransportContainer().getGene(geneKey).getReactionIds().size()));
					//tcr.put(geneKey, counter+"");
				};
			}
			
			ArrayList<String[]> result = TransportersAPI.getTransportersInfo(stmt);
			
			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);
				
				String locus = list[1];
				ql = new ArrayList<Object>();
				ql.add("");
				this.ids.put(i, list[0]);
				ql.add(locus);
				ql.add(list[2]);
				ql.add( tcn.get(locus));
				ql.add(mets.get(locus));
				ql.add( tcr.get(locus));
				ql.add(new Boolean(true));
				qrt.addLine(ql, list[0]);
				this.names.put(list[0], locus);
			}
			stmt.close();
		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		return qrt;
	}

	/* (non-Javadoc)
	 * @see pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity#getRowInfo(java.lang.String)
	 */
	public DataTable[] getRowInfo(String id) {
		
		DataTable[] results = new DataTable[2];
		if(this.getProject().getTransportContainer()!=null){
			results = new DataTable[results.length+1];
		}

		List<String> columnsNames = new ArrayList<String>();
		columnsNames.add("accession");
		columnsNames.add("TCDB id");
		columnsNames.add("similarity");
		results[0] = new DataTable(columnsNames, "Alignment");

		columnsNames = new ArrayList<String>();
		columnsNames.add("metabolite");
		columnsNames.add("kegg ID");
		columnsNames.add("similarity score");
		columnsNames.add("taxonomy score");
		columnsNames.add("final score");
		columnsNames.add("direction");
		columnsNames.add("reversible");
		columnsNames.add("transport type");
		columnsNames.add("type score");
		results[1] = new DataTable(columnsNames, "Metabolites");

		if(this.getProject().getTransportContainer()!=null){

			columnsNames = new ArrayList<String>();
			columnsNames.add("name");
			columnsNames.add("original");
			columnsNames.add("ontology");
			columnsNames.add("equation");
			results[2] = new DataTable(columnsNames, "Reactions");
		}

		Statement stmt;

		try {
			
			stmt = this.connection.createStatement();

			ArrayList<String[]> data = TransportersAPI.getTransportersData(id, stmt);

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);
				
				ArrayList<String> resultsList = new ArrayList<String>();
				resultsList.add(list[0]);
				resultsList.add(list[1]);
				resultsList.add(list[2]);

				results[0].addLine(resultsList);
			}
			
			double beta = 0.05;
			int minimalFrequency = 2;
			int originTaxonomy = this.getProject().getOrganismLineage().split(";").length+1;
			
			double totalScore = TransportersAPI.getGeneTotalScore(this.names.get(id), stmt);

			
			Map<String, Double> metaboliteScore = new HashMap<>(),  frequencyScores = new HashMap<>(),  taxonomyScores = new HashMap<>();
			
			data = TransportersAPI.getMetaboliteFrequencyScore(this.names.get(id), stmt);
			
			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);
				
				String metaboliteID = list[0];
				
				double similarity_score_sum = Double.parseDouble(list[2]), 
						taxonomy_score_sum = Double.parseDouble(list[3]);
				int frequency = Integer.parseInt(list[4]);
				
				double frequencyScore = similarity_score_sum/totalScore;
				double taxonomyScore = (taxonomy_score_sum*(1-(minimalFrequency-PopulateTransportContainer.func_getFrequency(frequency, minimalFrequency))*beta)/(originTaxonomy*frequency));
				double final_score = frequencyScore*alpha+(1-alpha)*taxonomyScore;
				
				metaboliteScore.put(metaboliteID, final_score);
				frequencyScores.put(metaboliteID, frequencyScore);
				taxonomyScores.put(metaboliteID, taxonomyScore);
			}

			Map<String, Double> metaboliteTotalScore = TransportersAPI.getMetaboliteTotalScore(this.names.get(id), stmt);

			Map<String, Map<String, Double>> transportTypeScore = new HashMap<>();
			
			data = TransportersAPI.getTransportTypeScore(this.names.get(id), stmt);
			
			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);
				
				double transport_type_score_sum = Double.parseDouble(list[3]), 
						taxonomy_score_sum = Double.parseDouble(list[4]);
				int frequency = Integer.parseInt(list[5]);
				String metabolite_id = list[0];
				String transport_type_id = list[6];
				
				Map<String, Double> typeScore = new HashMap<>();
				
				if(transportTypeScore.containsKey(metabolite_id))
					typeScore = transportTypeScore.get(metabolite_id);
				
				double frequencyScore = transport_type_score_sum/metaboliteTotalScore.get(metabolite_id);
				double taxonomyScore = (taxonomy_score_sum*(1-(minimalFrequency-PopulateTransportContainer.func_getFrequency(frequency, minimalFrequency))*beta)/(originTaxonomy*frequency));
				double final_score = frequencyScore*alpha+(1-alpha)*taxonomyScore;
				
				typeScore.put(transport_type_id, final_score);
				transportTypeScore.put(metabolite_id, typeScore);
			}
			
			data = TransportersAPI.getMetabolitesDirectionAndReversibility(this.names.get(id), stmt);
			
			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);
				
				ArrayList<String> resultsList = new ArrayList<String>();
				
				resultsList.add(list[3]);
				if(list[7].equals("null") || list[7].isEmpty())
					resultsList.add("");
				else
					resultsList.add(list[7].split(":")[3]);
				
				String frequencyScore = MerlinUtils.round(frequencyScores.get(list[2]),4)+"";
				if(list[4].equalsIgnoreCase("reactant") ||list[4].equalsIgnoreCase("product"))
					frequencyScore = "";
				resultsList.add(frequencyScore);
				
				String taxonomyScore = MerlinUtils.round(taxonomyScores.get(list[2]),4)+"";
				if(list[4].equalsIgnoreCase("reactant") || list[4].equalsIgnoreCase("product"))
					taxonomyScore = "";
				resultsList.add(taxonomyScore);
				
				String score = MerlinUtils.round(metaboliteScore.get(list[2]),4)+"";
				if(list[4].equalsIgnoreCase("reactant") || list[4].equalsIgnoreCase("product"))
					score = "";
				resultsList.add(score);
				
				resultsList.add(list[4]);
				resultsList.add(list[5]);
				resultsList.add(list[1]);

				String typeScore = MerlinUtils.round(transportTypeScore.get(list[2]).get(list[1]), 4)+"";
				if(list[4].equalsIgnoreCase("reactant") || list[4].equalsIgnoreCase("product"))
					typeScore = "";
				resultsList.add(typeScore+"");

				results[1].addLine(resultsList);
			}
			
			if(this.getProject().getTransportContainer()!=null) {
				TransportContainer tc = this.getProject().getTransportContainer();
				
				if(tc.getGenes().containsKey(this.names.get(id))) {

					Set<String> reactions = tc.getGene(this.names.get(id)).getReactionIds();

					if(tc!=null) {

						for(String reaction: reactions) {

							TransportReactionCI trci = tc.getReaction(reaction).clone();

							ArrayList<String> resultsList = new ArrayList<String>();

							boolean go = true;

							for(String key : new HashSet<>(trci.getReactants().keySet())) {

								StoichiometryValueCI svci = trci.getReactants().get(key);
								String met_id = tc.getMetabolite(key).getName();
								String kegg = ExternalRefSource.KEGG_CPD.getSourceId(tc.getKeggMiriam().get(key));
								if(kegg==null || kegg=="null")
									go = false;

								svci.setMetaboliteId(met_id);
								trci.getReactants().put(met_id, svci);
								trci.getReactants().remove(key);
							}

							for(String key : new HashSet<>(trci.getProducts().keySet())) {
								
								StoichiometryValueCI svci = trci.getProducts().get(key);
								String met_id = tc.getMetabolite(key).getName();
								String kegg = ExternalRefSource.KEGG_CPD.getSourceId(tc.getKeggMiriam().get(key));
								if(kegg==null || kegg=="null")
									go = false;

								svci.setMetaboliteId(met_id);
								trci.getProducts().put(met_id, svci);
								trci.getProducts().remove(key);
							}

							if(go) {
								
								String name = trci.getName();
								
								Set<String> original = new HashSet<>(), ontology = new HashSet<>();
								for(String gene:trci.getIsOriginalReaction_byGene().keySet())
									if(trci.getIsOriginalReaction_byGene().get(gene))
										original.add(gene);
									else
										ontology.add(gene);
								
								String equation = ContainerUtils.getReactionToString(trci);

								resultsList.add(name);
								resultsList.add(original.toString().replaceAll("\\[","").replaceAll("\\]",""));
								resultsList.add(ontology.toString().replaceAll("\\[","").replaceAll("\\]",""));
								resultsList.add(equation);

								results[2].addLine(resultsList);
							}
						}
					}
				}
			}
			stmt.close();
		} 
		catch (SQLException ex) {

			ex.printStackTrace();
		}

		return results;
	}

	/**
	 * @param id
	 * @return
	 */
	public String getGeneName(String id) {

		return this.names.get(id);
	}

	/**
	 * @return the alpha
	 */
	public double getAlpha() {
		return alpha;
	}

	/**
	 * @param alpha the alpha to set
	 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	/**
	 * @return the threshold
	 */
	public double getThreshold() {
		return threshold;
	}

	/**
	 * @param threshold the threshold to set
	 */
	public void setThreshold(double threshold) {
		this.threshold = threshold;
	}
}