/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.remote.loader.alignment;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.blast.org.biojava3.ws.alignment.qblast.NCBIQBlastAlignmentProperties;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.HomologyDataClient;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.HomologyAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseUtilities;
import pt.uminho.ceb.biosystems.merlin.utilities.DatabaseProgressStatus;

/**
 * @author oDias
 *
 */
public class LoadSimilarityResultstoDatabase {

	final static Logger logger = LoggerFactory.getLogger(LoadSimilarityResultstoDatabase.class);

	private NCBIQBlastAlignmentProperties rqb;
	private HomologyDataClient homologyDataClient;
	private Statement statement;
	private int organism_s_key, homologues_s_key, geneHomology_s_key, undo_geneHomology_s_key, homologySetupID;
	private String databaseID, version, program, query; 
	private HashMap<String, List<Integer>> prodOrg;
	private boolean loaded;
	private int maxNumberOfAlignments;
	private Project project;
	private double eVal;
	private AtomicBoolean cancel;


	/**
	 * @param homologyData
	 * @param rqb
	 * @param expectedVal
	 * @param maxNumberOfAlignments
	 * @param project
	 * @param cancel
	 */
	public LoadSimilarityResultstoDatabase(HomologyDataClient homologyData, NCBIQBlastAlignmentProperties rqb, double expectedVal, 
			int maxNumberOfAlignments, Project project, AtomicBoolean cancel) {

		this.maxNumberOfAlignments = maxNumberOfAlignments;
		this.rqb = rqb;
		this.homologyDataClient = homologyData;
		this.program = homologyData.getProgram();
		this.version = homologyData.getVersion();
		this.databaseID = homologyData.getDatabaseID();
		this.query = homologyData.getQuery();
		this.setLoaded(true);
		this.project = project;
		this.cancel = cancel;
		this.eVal = expectedVal;
	}

	/**
	 * @param homologyData
	 * @param project
	 * @param uniprotStatus 
	 */
	public LoadSimilarityResultstoDatabase(HomologyDataClient homologyData, Project project, AtomicBoolean cancel) {

		this.homologyDataClient = homologyData;
		this.program = homologyData.getProgram();
		this.version = homologyData.getVersion();
		this.databaseID = homologyData.getDatabaseID();
		this.query = homologyData.getQuery();
		this.setLoaded(true);
		this.project = project;
		this.cancel = cancel;
	}

	/**
	 * @param hd
	 * @param maxNumberOfAlignments
	 * @param eValue
	 * @param project
	 * @param cancel
	 */
	public LoadSimilarityResultstoDatabase(HomologyDataClient hd, int maxNumberOfAlignments, 
			double eValue, Project project, AtomicBoolean cancel) {

		this.eVal = eValue;
		this.maxNumberOfAlignments = maxNumberOfAlignments;
		this.homologyDataClient = hd;
		this.program = hd.getProgram();
		this.version = hd.getVersion();
		this.databaseID = hd.getDatabaseID();
		this.query = hd.getQuery();
		this.setLoaded(true);
		this.project = project;
		this.cancel = cancel;
	}

	/**
	 * @param locusTag
	 * @param query
	 * @param gene
	 * @param chromosome
	 * @param organelle
	 * @throws SQLException
	 */
	private void loadGene(String locusTag, String query, String gene, String chromosome, String organelle, String uniprot_star) throws SQLException {

		this.undo_geneHomology_s_key = -1;
		int uniprot_star_int = -1;

		if(uniprot_star!= null)
			uniprot_star_int = DatabaseUtilities.get_boolean_int(uniprot_star);

		String uniProtEC = "";
		if(this.homologyDataClient.getUniprot_ecnumber()!=null)
			uniProtEC =	this.homologyDataClient.getUniprot_ecnumber();

		if(this.homologyDataClient.isNoSimilarity()) {

			String aux = DatabaseUtilities.databaseStrConverter(query,this.project.getDatabaseType());

			int sKey = HomologyAPI.getGeneHomologySkey(aux, this.homologySetupID, statement);

			if(sKey<0) {

				String query2 = "INSERT INTO geneHomology (locusTag, query, homologySetup_s_key, status, uniprot_star, uniprot_ecnumber) VALUES "
						+ " ('"+ locusTag +"','"+ DatabaseUtilities.databaseStrConverter(query,this.project.getDatabaseType()) + "','" +this.homologySetupID+ "','"+DatabaseProgressStatus.NO_SIMILARITY+"',"
						+ "'"+uniprot_star_int+"','"+uniProtEC+"');";

				//				ProjectAPI.executeQuery(query2, statement);

				sKey = ProjectAPI.executeAndGetLastInsertID(query2, statement);
			}
			this.geneHomology_s_key = sKey;
		}
		else {

			String aux = DatabaseUtilities.databaseStrConverter(query,this.project.getDatabaseType());

			int sKey = HomologyAPI.getGeneHomologySkey(aux, this.homologySetupID, statement);

			if(sKey>0) {

				this.geneHomology_s_key = sKey;
				this.undo_geneHomology_s_key = this.geneHomology_s_key;
				String query2 = "UPDATE geneHomology " +
						"SET locusTag='"+ locusTag +"', " +
						"homologySetup_s_key = '" +this.homologySetupID+ "'," +
						"gene = '" +DatabaseUtilities.databaseStrConverter(gene,this.project.getDatabaseType())+ "'," +
						"chromosome = '" +chromosome+ "'," +
						"organelle = '" +organelle+ "'," +
						"uniprot_star = '" +uniprot_star_int+ "'," +
						"status = '"+DatabaseProgressStatus.PROCESSING+"'" +
						"WHERE query = '"+DatabaseUtilities.databaseStrConverter(query,this.project.getDatabaseType())+"';";

				ProjectAPI.executeQuery(query2, statement);

			}
			else {

				String query2 = "INSERT INTO geneHomology (locusTag, query, gene, chromosome, organelle, uniprot_star, homologySetup_s_key, status, uniprot_ecnumber) VALUES ('"
						+ locusTag +"','"+ DatabaseUtilities.databaseStrConverter(query,this.project.getDatabaseType()) + "','"+ DatabaseUtilities.databaseStrConverter(gene,this.project.getDatabaseType()) + "','" + chromosome + "','"+ organelle + 
						"'," +uniprot_star_int + ",'" +this.homologySetupID+ "','"+DatabaseProgressStatus.PROCESSING+"','"+uniProtEC+"');";

				sKey = ProjectAPI.executeAndGetLastInsertID(query2, statement);
				this.geneHomology_s_key = sKey;
				this.undo_geneHomology_s_key = this.geneHomology_s_key;
			}
		}
	}

	/**
	 * @param locusTag
	 */
	private void updataGeneStatus(String locusTag) {

		try {

			String query = "UPDATE geneHomology SET status ='"+DatabaseProgressStatus.PROCESSED+
					"' WHERE locusTag ='" + locusTag +"';";

			ProjectAPI.executeQuery(query, statement);
		}
		catch (SQLException e) {

			System.out.println(LoadSimilarityResultstoDatabase.class.toString()+" updateGeneStatus eror.");
		}
	}


	/**
	 * @param organism
	 * @param taxonomy
	 * @param myOrganismTaxonomy
	 * @throws SQLException
	 */
	private void loadOrganism(String organism, String taxonomy, String myOrganismTaxonomy, String locus) throws Exception {

		try {

			if(organism==null)
				organism = "unknown";

			if(taxonomy==null) 
				taxonomy = "unknown";


			String aux = DatabaseUtilities.databaseStrConverter(organism,this.project.getDatabaseType())+"';";

			int sKey = ProjectAPI.getSKeyFromOrganism(aux, statement);

			if(sKey > 0) 
				this.organism_s_key = sKey;

			else {

				String query = "INSERT INTO organism (organism, taxonomy, taxrank) VALUES ('"
						+ DatabaseUtilities.databaseStrConverter(organism,this.project.getDatabaseType()) +"','"
						+ DatabaseUtilities.databaseStrConverter(taxonomy,this.project.getDatabaseType()) 
						+"','"+this.rankTaxonomy(taxonomy.concat("; "+organism), myOrganismTaxonomy)+"');";

				sKey = ProjectAPI.executeAndGetLastInsertID(query, statement);
				this.organism_s_key = sKey;
			}
		}
		catch (NullPointerException e) {

			System.out.println(this.homologyDataClient.getOrganism());
			System.out.println();
			System.out.println(LoadSimilarityResultstoDatabase.class.toString()+" loadOrganism error null pointer locus:"+locus+"\t\torganism:\t"+organism+"\t\ttax:\t"+taxonomy+"\t\tmyOrgTax:\t"+myOrganismTaxonomy);
			//e.printStackTrace();
			throw e;
		}
		catch (SQLException e) {

			System.out.println(this.homologyDataClient.getOrganism());
			System.out.println(LoadSimilarityResultstoDatabase.class.toString()+" loadOrganism error  sql organism "+organism+" tax "+taxonomy+" myOrgTax "+myOrganismTaxonomy);

			throw e;
		}
	}

	/**
	 * @param taxonomy
	 * @param myOrganismTaxonomy
	 * @return
	 */
	private Integer rankTaxonomy(String taxonomy, String myOrganismTaxonomy) {

		if(taxonomy== null || myOrganismTaxonomy == null) {

			return 0;
		}
		else {

			String[] taxonomyArray = taxonomy.split(";");
			String[] myOrganismTaxonomyArray = myOrganismTaxonomy.split(";");

			for(int i = 0; i< myOrganismTaxonomyArray.length; i++) {

				if(taxonomyArray.length>i) {

					if(!myOrganismTaxonomyArray[i].equals(taxonomyArray[i]))
						return i;
				}
				else {

					return taxonomyArray.length;
				}
			}
			return myOrganismTaxonomyArray.length;
		}
	}

	/**
	 * @param locusID
	 * @param calculated_mw
	 * @param productRank
	 * @param referenceID
	 * @return
	 * @throws SQLException
	 */
	private Map<String, Integer> loadHomologues(String locusID, double calculated_mw, Map<String, Integer> productRank,  String referenceID) throws SQLException {

		String definition = this.homologyDataClient.getDefinition().get(referenceID);
		String product = this.homologyDataClient.getProduct().get(referenceID);
		String organelle = this.homologyDataClient.getOrganelles().get(referenceID);

		if(product==null) {

			product=definition;
		}
		else {

			//			product = product.replaceAll("\\","");
			//			product = DatabaseUtilities.databaseStrConverter(product);
		}

		if(organelle!= null && organelle.length()>99)
			organelle = organelle.subSequence(0, 99).toString();

		int sKey;
		String query = "SELECT * FROM homologues WHERE " +
				"organism_s_key='"+ this.organism_s_key +"' "+
				"AND locusID='"+ DatabaseUtilities.databaseStrConverter(locusID,this.project.getDatabaseType()) +"' " +
				"AND (definition='"+ DatabaseUtilities.databaseStrConverter(definition,this.project.getDatabaseType()) +"') " +
				"AND calculated_mw= "+ calculated_mw +" " +
				"AND (product='"+ DatabaseUtilities.databaseStrConverter(product,this.project.getDatabaseType()) +"') " +
				"AND organelle='"+ DatabaseUtilities.databaseStrConverter(organelle,this.project.getDatabaseType()) +"'";

		try {

			sKey = HomologyAPI.getHomologuesSkey(query, statement);
		} 
		catch (SQLException e) {

			System.out.println(LoadSimilarityResultstoDatabase.class.toString()+" load homologues error.");
			System.out.println(query);

			e.printStackTrace();
			throw e;
		}

		if(sKey > 0) {

			this.homologues_s_key = sKey;
		}
		else {

			int uniprot_star_int = -1;


			if(this.homologyDataClient.getUniprotStar().containsKey(referenceID)) {

				String star = this.homologyDataClient.getUniprotStar().get(referenceID).toString();
				uniprot_star_int = DatabaseUtilities.get_boolean_int(star);
			}

			query = "INSERT INTO homologues (" +
					//"homology_s_key, homology_geneHomology_s_key," +
					"organism_s_key, locusID, definition, calculated_mw, product, organelle, uniprot_star) VALUES ("
					//"'"+this.homology_s_key +"','"+ this.geneHomology_s_key +"','"+ 
					+this.organism_s_key +",'"+locusID +"','"+ 
					DatabaseUtilities.databaseStrConverter(definition,this.project.getDatabaseType()) 
					+ "','"+ calculated_mw +"','"+ 
					DatabaseUtilities.databaseStrConverter(product,this.project.getDatabaseType()) 
					+"'," +"'"+ organelle +"',"+uniprot_star_int+")";

			sKey = ProjectAPI.executeAndGetLastInsertID(query, statement);
			this.homologues_s_key = sKey;
		}

		if(productRank.containsKey(product)) {

			productRank.put(product, productRank.get(product)+1);
			List<Integer> orgKey = this.prodOrg.get(product);
			orgKey.add(this.organism_s_key);
			this.prodOrg.put(product, orgKey);
		}
		else {

			productRank.put(product, 1);
			List<Integer> orgKey = new ArrayList<>();
			orgKey.add(this.organism_s_key);
			this.prodOrg.put(product, orgKey);
		}

		return productRank;
	}


	/**
	 * @param ecNumberRank
	 * @param ecOrg
	 * @param locus
	 * @return
	 * @throws SQLException
	 */
	private Map<Set<String>, Integer> loadECNumbers(Map<Set<String>, Integer> ecNumberRank, Map<Set<String>, List<Integer>> ecOrg, String locus) throws SQLException {

		if(this.homologyDataClient.getEcnumber().keySet().contains(locus)) {

			Set<String> ecnumbers = new HashSet<String>();

			for(int e =0; e<this.homologyDataClient.getEcnumber().get(locus).length; e++) {
				
				this.loadECNumbers(this.homologyDataClient.getEcnumber().get(locus)[e]);

				if(!ecnumbers.contains(this.homologyDataClient.getEcnumber().get(locus)[e]))
					ecnumbers.add(this.homologyDataClient.getEcnumber().get(locus)[e]); 
			}

			if(ecNumberRank.containsKey(ecnumbers)) {

				List<Integer> orgKey = ecOrg.get(ecnumbers); 
				ecNumberRank.put(ecnumbers, ecNumberRank.get(ecnumbers)+1);
				orgKey.add(this.organism_s_key);
				ecOrg.put(ecnumbers, orgKey);
			}
			else {

				List<Integer> orgKey = new ArrayList<>();
				ecNumberRank.put(ecnumbers, 1);
				orgKey.add(this.organism_s_key);
				ecOrg.put(ecnumbers, orgKey);
			}
		}
		return ecNumberRank;		
	}

	/**
	 * @param referenceID
	 * @param gene
	 * @param eValue
	 * @param bits
	 * @throws SQLException
	 */
	private void load_geneHomology_has_homologues(String referenceID, String gene, double eValue, String bits) {

		try {

			if(gene!=null) {

				gene=DatabaseUtilities.databaseStrConverter(gene,this.project.getDatabaseType());

				String query = "SELECT * FROM geneHomology_has_homologues WHERE geneHomology_s_key='"+ this.geneHomology_s_key +"' AND homologues_s_key = "+this.homologues_s_key+" " +
						"AND referenceID='"+ DatabaseUtilities.databaseStrConverter(referenceID,this.project.getDatabaseType()) +"' AND gene='"+ gene +"' AND eValue='"+ eValue +"' AND bits='"+ bits +"'";

				boolean exists = HomologyAPI.checkGeneHomologyHasHomologues(query, statement);

				if(!exists) {

					query = "INSERT INTO geneHomology_has_homologues (geneHomology_s_key, referenceID, gene ,eValue, bits, homologues_s_key) " +
							"VALUES("+this.geneHomology_s_key +",'"+ DatabaseUtilities.databaseStrConverter(referenceID,this.project.getDatabaseType()) 
							+"','"+ gene +"','"+ eValue +"','"+ bits +"',"+this.homologues_s_key+" );";

					ProjectAPI.executeQuery(query, statement);
				}
			}
			else {

				String query = "SELECT * FROM geneHomology_has_homologues WHERE geneHomology_s_key='"+ this.geneHomology_s_key +"' AND homologues_s_key = "+this.homologues_s_key+" ;"; // AND  +
				//	"AND referenceID='"+ DatabaseUtilities.databaseStrConverter(referenceID) +"'eValue='"+ eValue +"' AND bits='"+ bits +"'");
				boolean exists = HomologyAPI.checkGeneHomologyHasHomologues(query, statement);

				if(!exists) {

					query = "INSERT INTO geneHomology_has_homologues (geneHomology_s_key, referenceID, eValue, bits, homologues_s_key) " +
							"VALUES("+this.geneHomology_s_key +",'"+ DatabaseUtilities.databaseStrConverter(referenceID,this.project.getDatabaseType()) 
							+"','"+ eValue +"','"+ bits +"',"+this.homologues_s_key+" );";

					ProjectAPI.executeQuery(query, statement);
				}
			}
		}
		catch (SQLException e) {

			e.printStackTrace();
			System.out.println("ERROR!!!! SELECT * FROM geneHomology_has_homologues WHERE geneHomology_s_key='"+ this.geneHomology_s_key +"' AND homologues_s_key = "+this.homologues_s_key+" " +
					"AND referenceID='"+ DatabaseUtilities.databaseStrConverter(referenceID,this.project.getDatabaseType()) +"' AND eValue='"+ eValue +"' AND bits='"+ bits +"'");
			System.out.println(LoadSimilarityResultstoDatabase.class.toString()+" load gene homology error.");
		}
	}

	/**
	 * @param sequence
	 * @throws SQLException
	 */
	private void loadFastaSequence(String sequence) throws SQLException {

		boolean exists = ProjectAPI.checkfastaSequenceBySkey(this.geneHomology_s_key, statement);

		if(!exists) {

			String query = "INSERT INTO fastaSequence (geneHomology_s_key, sequence) VALUES('"+ this.geneHomology_s_key + "','"+ sequence +"');";
			ProjectAPI.executeQuery(query, statement);
		}
	}

	/**
	 * @param ecNumber
	 * @throws SQLException
	 */
	private void loadECNumbers(String ecNumber) throws SQLException {

		//this.statement.execute("LOCK tables ecNumber read,homology_has_ecNumber read;");

		int sKey = ProjectAPI.getecNumberSkey(ecNumber, statement);

		if(sKey<0) {

			String query = "INSERT INTO ecNumber SET ecNumber='"+ ecNumber +"'";
			sKey = ProjectAPI.executeAndGetLastInsertID(query, statement);
		}
		int ecnumber_s_key = sKey;
		
		boolean exists = HomologyAPI.checkHomologuesHasEcNumber(this.homologues_s_key, ecnumber_s_key, statement);
		
		if(!exists) {
			
			String query = "INSERT INTO homologues_has_ecNumber (homologues_s_key, ecNumber_s_key) " +
					"VALUES('"+this.homologues_s_key +"','"+ ecnumber_s_key +"');";
			ProjectAPI.executeQuery(query, statement);
		}
	}

	/**
	 * @param databaseID
	 * @param program
	 * @param version
	 * @throws SQLException
	 */
	private void loadhomologySetup(String databaseID, String program, String version) throws SQLException {

		//this.statement.execute("LOCK tables homologySetup read;");
		String query = "SELECT * FROM homologySetup " +
				"WHERE databaseID = '"+databaseID+"' " +
				"AND program='"+program+"' " +
				"AND eValue='"+this.rqb.getBlastExpect()+"' " +
				"AND matrix='"+this.rqb.getBlastMatrix()+"' " +
				"AND wordSize='"+this.rqb.getBlastWordSize()+"' " +
				"AND gapCosts='"+this.rqb.getBlastGapCosts()+"' " +
				"AND maxNumberOfAlignments='"+this.maxNumberOfAlignments+"' " +
				"AND version='"+version+"'";

		int sKey = HomologyAPI.getHomologuesSkey(query, statement);

		if(sKey<0) {

			query = "INSERT INTO homologySetup (databaseID, program, version, eValue, matrix, wordSize, gapCosts, maxNumberOfAlignments) " +
					"VALUES ('"+ databaseID +"','"+ program +"','"+version +"','"+this.rqb.getBlastExpect() +"','"+this.rqb.getBlastMatrix() +"'," +
					"'"+this.rqb.getBlastWordSize() +"','"+this.rqb.getBlastGapCosts() +"','"+this.maxNumberOfAlignments +"');";

			sKey = ProjectAPI.executeAndGetLastInsertID(query, statement);
		}
		this.homologySetupID = sKey;
		//this.statement.execute("UNLOCK tables;");
	}



	/**
	 * @param databaseID
	 * @param program
	 * @param version
	 * @throws SQLException
	 */
	private void loadHmmerSetup(String databaseID, String program, String version) throws SQLException {

		//this.statement.execute("LOCK tables homologySetup read;");

		String query = "SELECT * FROM homologySetup " +
				"WHERE databaseID = '"+databaseID+"' " +
				"AND program='"+program+"' " +
				"AND eValue='"+this.eVal+"' " +
				"AND maxNumberOfAlignments='"+this.maxNumberOfAlignments+"' " +
				"AND version='"+version+"'";

		int sKey = HomologyAPI.getHomologuesSkey(query, statement);

		if(sKey<0) {

			query = "INSERT INTO homologySetup (databaseID, program, version, eValue, maxNumberOfAlignments) " +
					"VALUES ('"+ databaseID +"','"+ program +"','"+version +"','"+this.eVal +"','"+this.maxNumberOfAlignments +"');";

			sKey = ProjectAPI.executeAndGetLastInsertID(query,statement);
		}
		this.homologySetupID = sKey;
		//this.statement.execute("UNLOCK tables;");
	}

	/**
	 * @param pd
	 * @throws SQLException
	 */
	private void loadProductRank(Map<String, Integer> pd) throws SQLException {

		for(String product : pd.keySet()) {

			String aux = DatabaseUtilities.databaseStrConverter(product,this.project.getDatabaseType());

			int sKey = ProjectAPI.getProductRankSkey(this.geneHomology_s_key, aux, pd.get(product), statement);

			if(sKey<0) {

				String query = "INSERT INTO productRank (geneHomology_s_key, productName, rank) " +
						"VALUES("+ this.geneHomology_s_key +",'"
						+ DatabaseUtilities.databaseStrConverter(product,this.project.getDatabaseType()) + "','" +pd.get(product)+ "');";

				int prodkey = ProjectAPI.executeAndGetLastInsertID(query, statement);

				for(int orgKey : this.prodOrg.get(product)) {

					sKey = ProjectAPI.getProductRankHasOrganismSkey(prodkey, orgKey, statement);

					if(sKey<0)
						query = "INSERT INTO productRank_has_organism (productRank_s_key, organism_s_key) "
								+ "VALUES("+ prodkey+ "," + orgKey+ ");";
					ProjectAPI.executeQuery(query, statement);
				}
			}
		}
		//this.statement.execute("UNLOCK tables;");
	}

	/**
	 * @param ecn
	 * @param ecOrg
	 * @throws SQLException
	 */
	private void loadECNumberRank(Map<Set<String>, Integer> ecn, Map<Set<String>, List<Integer>> ecOrg) throws SQLException {

		//this.statement.execute("LOCK tables ecNumberRank read, ecNumberRank_has_organism read;");

		for(Set<String> ecnumber: ecn.keySet()) {

			String concatEC="";
			for(String s:ecnumber) {

				if(concatEC.isEmpty())
					concatEC = s;
				else
					concatEC += ", " + s;
			}

			int sKey = ProjectAPI.getEcNumberRankSkey(this.geneHomology_s_key, concatEC, ecn.get(ecnumber), statement);

			if(sKey<0) {

				String query = "INSERT INTO ecNumberRank (geneHomology_s_key, ecNumber, rank) " +
						"VALUES('"+ this.geneHomology_s_key +"','"+ concatEC + "','" +ecn.get(ecnumber)+ "');";

				sKey = ProjectAPI.executeAndGetLastInsertID(query, statement);

				if(sKey > 0) {
					int eckey = sKey;

					for(int orgKey : ecOrg.get(ecnumber)) {

						query = "INSERT INTO ecNumberRank_has_organism (ecNumberRank_s_key, organism_s_key) VALUES('"+ eckey + "','" + orgKey+ "');";
						ProjectAPI.executeQuery(query, statement);
					}
				}
			}
		}
		//this.statement.execute("UNLOCK tables;");
	}

	/**
	 */
	public void loadData() {

		try {

			Connection conn = this.project.getDatabase().getDatabaseAccess().openConnection();
			this.statement = conn.createStatement();

			String program = "hmmer";
			if(this.rqb != null)
				program = this.homologyDataClient.getProgram();

			boolean exists = HomologyAPI.loadGeneHomologyData(this.homologyDataClient.getQuery(), program, statement);

			if(exists) 
				logger.warn("Gene {} already processed!", this.homologyDataClient.getLocus_tag());
			else {

				String star;
				if(this.homologyDataClient.getQuery() != null && this.homologyDataClient.getUniprotStar()!=null && 
						this.homologyDataClient.getUniprotStar().containsKey(this.homologyDataClient.getQuery())) {

					star = this.homologyDataClient.getUniprotStar().get(this.homologyDataClient.getQuery()).toString();
				}
				else {

					try {

						star =  this.homologyDataClient.isUniProtStarred()+"";
					}
					catch(NullPointerException e) {

						e.printStackTrace();
						star = null;
					}
				}

				if(this.rqb != null) {

					this.loadhomologySetup(this.databaseID, this.program, this.version);
				}
				else {

					this.loadHmmerSetup(this.databaseID, this.program, this.version);
				}	

				if(this.homologyDataClient.isNoSimilarity()) {

					String locusTag = this.query;
					if(this.homologyDataClient.getLocus_tag() != null) {

						locusTag = this.homologyDataClient.getLocus_tag();
					}

					this.loadGene(locusTag, this.query, null, null, null, star);
					this.loadFastaSequence(this.homologyDataClient.getFastaSequence());
				}
				else {

					String locusTag;

					if(this.homologyDataClient.getLocus_tag()==null) {

						if(this.homologyDataClient.getLocus_gene_note()==null) {

							locusTag=this.homologyDataClient.getLocus_protein_note();	
						}
						else {

							if(this.homologyDataClient.getLocusID().get(0).matches("[A-Za-z]*\\d*\\s+")
									&& !this.homologyDataClient.getLocus_tag().contains(":") )//if the locus tag contains spaces and not: 
							{

								locusTag=this.homologyDataClient.getLocusID().get(0);
							}
							else {

								String[] locus = this.homologyDataClient.getLocus_gene_note().split(":");
								locusTag = locus[locus.length-1];
								locus = locusTag.split(";");
								locusTag = locus[0];
							}
						}
					}
					else {

						locusTag=this.homologyDataClient.getLocus_tag();
					}

					this.loadGene(locusTag, this.query, this.homologyDataClient.getGene(), this.homologyDataClient.getChromossome(), this.homologyDataClient.getOrganelle(), star);
					this.loadFastaSequence(this.homologyDataClient.getFastaSequence());
					Map<String, Integer> productRank = new HashMap<String,Integer>();
					this.prodOrg = new HashMap<>();
					Map<Set<String>, Integer> ecNumberRank = new HashMap<Set<String>,Integer>();
					Map<Set<String>, List<Integer>> ecOrg = new HashMap<>();
					String myOrganismTaxonomy="";

					myOrganismTaxonomy = this.homologyDataClient.getOrganismTaxa()[1].concat("; "+this.homologyDataClient.getOrganismTaxa()[0]);
					this.loadOrganism(this.homologyDataClient.getOrganismTaxa()[0], this.homologyDataClient.getOrganismTaxa()[1],myOrganismTaxonomy,"origin organism");

					for(int l = 0 ; l< this.homologyDataClient.getLocusID().size(); l++) {

						String locus = this.homologyDataClient.getLocusID().get(l);

						if(locus!=null && !locus.trim().isEmpty()){

							if(!this.cancel.get()) {

								double calculateMW=0;
								if(this.homologyDataClient.getCalculated_mol_wt().get(locus)!= null)
									calculateMW = Double.parseDouble(this.homologyDataClient.getCalculated_mol_wt().get(locus));

								String blastLocusTagID = this.homologyDataClient.getBlastLocusTag().get(locus);			

								if (blastLocusTagID==null)
									blastLocusTagID=locus;				

								String organism = this.homologyDataClient.getOrganism().get(locus),
										taxonomy =	this.homologyDataClient.getTaxonomy().get(locus);

								this.loadOrganism(organism,taxonomy,myOrganismTaxonomy,locus);

								double eValue = this.homologyDataClient.getEValue().get(locus);

								if(this.homologyDataClient.getEValue().containsKey(locus) && this.homologyDataClient.getScore().containsKey(locus)) {

									String bits = new String(this.homologyDataClient.getScore().get(locus)+"");
									String gene= this.homologyDataClient.getGenes().get(locus);

									if(this.homologyDataClient.getScore().get(locus)<0) {

										bits = new String("999999");
										eValue=0;
										//this.homologyDataClient.getEcnumber().put(locus, this.homologyDataClient.getUniprot_ecnumber().split(","));
									}

									if(this.eVal >= eValue) {

										productRank = loadHomologues(blastLocusTagID, calculateMW, productRank, locus);
										
										ecNumberRank = loadECNumbers(ecNumberRank, ecOrg, locus);
										
										this.load_geneHomology_has_homologues(locus, gene, eValue, bits);
									}
								}
							}
						}
					}


					if(!this.cancel.get()) {

						this.loadProductRank(productRank);

						if (!ecNumberRank.isEmpty()) {

							this.loadECNumberRank(ecNumberRank,ecOrg);		
						}
						this.updataGeneStatus(locusTag);
					}
				}

				if(this.cancel.get()) {

					this.setLoaded(false);
				}
			}
			this.statement.close();
			conn.close();
		}
		catch (Exception e) {

			e.printStackTrace();
			System.out.println(LoadSimilarityResultstoDatabase.class.toString()+" loadData error.");
			this.deleteEntry();
			this.setLoaded(false);
		}
	}

	//	/**
	//	 * @param rawString
	//	 * @return
	//	 */
	//	private String parseString(String rawString) {
	//		if(rawString==null)
	//			return null;
	//		return rawString.replace("\\'","'").replace("-","\\-").replace("'","\\'").replace("[","\\[").replace("]","\\]");//.replace(".","");		
	//	}

	/**
	 * 
	 */
	public void deleteEntry() {

		try  {

			if(this.undo_geneHomology_s_key>0) {

				String query = "DELETE FROM geneHomology WHERE s_key='"+this.undo_geneHomology_s_key+"'";
				ProjectAPI.executeQuery(query, statement);
			}

			//			if(this.undo_organism_s_key!=null && !this.undo_organism_s_key.isEmpty()) {
			//
			//				this.statement.execute("DELETE FROM organism WHERE s_key='"+this.undo_organism_s_key+"'");
			//			}

		}
		catch (SQLException e) {

			System.out.println(LoadSimilarityResultstoDatabase.class.toString()+" delete entry eror.");
		}
	}

	/**
	 * @param loaded the loaded to set
	 */
	public void setLoaded(boolean loaded) {
		this.loaded = loaded;
	}

	/**
	 * @return the loaded
	 */
	public boolean isLoaded() {
		return loaded;
	}

}
