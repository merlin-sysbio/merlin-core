package pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation;

import java.io.File;
import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.core.operations.project.SaveProject;
import pt.uminho.ceb.biosystems.merlin.core.utilities.BlastScorer;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.HomologyAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseUtilities;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.ceb.biosystems.merlin.utilities.Pair;
import pt.uminho.ceb.biosystems.merlin.utilities.PairComparator;

/**
 * @author ODias
 *
 */
@Datatype(structure= Structure.SIMPLE, namingMethod="getName",removable=true,removeMethod ="remove")
public class EnzymesAnnotationDataInterface extends Entity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final double ALPHA = 0.5;
	public static final double BLAST_HMMER_WEIGHT = 0.5;
	public static final double THRESHOLD = 0.0;
	public static final double BETA = 0.15;
	public static final int MINIMUM_NUMBER_OF_HITS = 3;
	public static final double UPPER_THRESHOLD = 1.0;

	private String[][] ecnPercent, prodPercent, product, enzyme;
	private Map<Integer, String> originalKeys, keys, prodItem, ecItem, initialProdItem, initialChromosome, initialEcItem, 
	namesList, locusList, initialLocus, initialNames, chromosome, notesMap;
	private Map<Integer, String> committedProdItem, committedEcItem, committedNamesList, 
	committedLocusList, committedChromosome, committedNotesMap;
	private Map<Integer, Boolean> committedSelected;
	private Map<String, String> score, score1, score2, scoreP, scoreP1, scoreP2;
	//	private Map<Integer, Boolean> initialSelectedGene, selectedGene;
	private Map<String, Integer> tableRowIndex;
	private Map<Integer, String[]> editedProductData, editedEnzymeData;
	private Double alpha, beta, threshold, upperThreshold;
	private int selectedRow;
	private boolean isEukaryote;
	private boolean blastPAvailable, stats_blastXAvailable, hmmerAvailable, stats_hmmerAvailable, stats_blastPAvailable;
	private int minimumNumberofHits;
	private Map<Integer, String> blastPGeneDataEntries;
	private Map<String, Integer> hmmerGeneDataEntries;
	private Connection connection;
	//private boolean firstRun;
	private Map<Integer, String[]> committedProductList, committedEnzymeList;
	private boolean hasCommittedData;
	private Map<Integer, String> integrationLocusList, integrationChromosome, 
	integrationNamesList, integrationProdItem, integrationEcItem;
	//	private Map<Integer, Boolean> integrationSelectedGene;
	private Map<Integer, Integer> reverseKeys;
	private double committedThreshold;	//lowerThreshold
	private double committedBalanceBH;
	private double committedAlpha;
	private double committedBeta;
	private int committedMinHomologies;
	private double committedUpperThreshold;
	private boolean firstCall;
	private String taxonomyRank, maxTaxRank;

	private DecimalFormat format;
	private Map<Integer,List<Object>> geneData;
	private Map<String,String> prodName;
	private Map<String,List<String>> prodKeys;
	private Map<String,String> ecName;
	private Map<String,List<String>> ecKeys;
	private Map<String, Set<Integer>> blastGeneDatabase;
	private TreeMap<Integer, String> initialOriginalLocus;
	private TreeMap<Integer, String> initialOriginalNames;
	private ArrayList<String[]>  dataFromEcNumber, ecRank, dataFromProduct, taxRank;
	private Map<String, Double> homologuesCountEcNumber, homologuesCountProduct;

	/**
	 * @param dbt
	 * @param name
	 */
	public EnzymesAnnotationDataInterface(Table dbt, String name) {

		super(dbt, name);
		this.connection = dbt.getConnection();

		this.prodItem = new TreeMap<Integer,String>();
		this.ecItem = new TreeMap<Integer,String>();
		//		this.selectedGene = new TreeMap<Integer,Boolean>();
		this.locusList =  new TreeMap<Integer, String>();
		this.namesList = new TreeMap<Integer, String>();
		this.setNotesMap(new TreeMap<Integer, String>()); 
		this.editedEnzymeData = new TreeMap<Integer, String[]>();
		this.editedProductData=new TreeMap<Integer, String[]>();
		this.setScore1(new TreeMap<String, String>());
		this.setScore2(new TreeMap<String, String>());
		this.setScore(new TreeMap<String, String>());
		this.setScoreP1(new TreeMap<String, String>());
		this.setScoreP2(new TreeMap<String, String>());
		this.setScoreP(new TreeMap<String, String>());
		this.chromosome = new TreeMap<Integer, String>();
		this.tableRowIndex = new TreeMap<String, Integer>();
		this.dataFromEcNumber = new ArrayList<>();
		this.ecRank = new ArrayList<>();
		this.dataFromProduct = new ArrayList<>();
		this.taxRank = new ArrayList<>();
		this.homologuesCountEcNumber = new HashMap<>();
		this.homologuesCountProduct = new HashMap<>();

		this.taxonomyRank = "";
		this.maxTaxRank = "";

		this.alpha= ALPHA;
		this.threshold = THRESHOLD;
		this.beta = BETA;
		this.minimumNumberofHits = MINIMUM_NUMBER_OF_HITS;

//		this.setFirstCall(true);

		this.setSelectedRow(-1);
		this.setBlastType();
		this.getCommitedScorerData("");
		
//		getDataFromDatabase();
	}


	/**
	 * Method to save in cache the information about all enzymes in the database.
	 * 
	 * @param stmt
	 * @throws SQLException
	 */
	private void getDataFromDatabase(){

		try {

			Statement statement = this.connection.createStatement();

			this.blastGeneDatabase = HomologyAPI.getGenesPerDatabase(statement);

			DecimalFormatSymbols separator = new DecimalFormatSymbols();
			separator.setDecimalSeparator('.');
			this.format = new DecimalFormat("##.##",separator);

			//GENE |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
			this.geneData = this.getGeneInformation(statement);

			//PROD |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||

			Pair<Map<String, String>, Map<String, List<String>>> pairProd = this.getProductRank();

			this.dataFromProduct = new ArrayList<>(HomologyAPI.getProductRank(statement));

			this.taxRank = new ArrayList<>(HomologyAPI.getTaxRank(statement));

			this.maxTaxRank = HomologyAPI.getMaxTaxRank(statement);

			this.homologuesCountProduct = HomologyAPI.getHomologuesCountByProductRank(statement);

			this.prodName = pairProd.getA();
			this.prodKeys = pairProd.getB();

			//EC |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||

			Pair<Map<String, String>, Map<String, List<String>>> pairEC = this.getECRank();

			this.dataFromEcNumber = new ArrayList<>(HomologyAPI.getDataFromecNumberRank(statement));

			this.ecRank = new ArrayList<>(HomologyAPI.getEcRank(statement));

			this.taxonomyRank = HomologyAPI.getMaxTaxRank(statement);

			this.homologuesCountEcNumber = HomologyAPI.getHomologuesCountByEcNumber(statement);

			this.ecName = pairEC.getA();
			this.ecKeys = pairEC.getB();

		} 
		catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 */
	private void setBlastType() {

		Statement stmt;

		try {

			this.hmmerAvailable = false;
			this.blastPAvailable = false;
			this.stats_blastXAvailable = false;
			this.stats_hmmerAvailable = false;
			this.stats_blastPAvailable = false;

			stmt = this.connection.createStatement();

			ArrayList<String> data = HomologyAPI.getProgramFromHomologySetup(stmt);

			int ncbip_count = 0;
			//int ncbix_count = 0;
			int hmmer_count = 0;

			for(int i=0; i<data.size(); i++){

				if(data.get(i).equalsIgnoreCase("hmmer")) {

					this.hmmerAvailable = true;
					this.stats_hmmerAvailable = true;
					hmmer_count ++;
				}

				if(data.get(i).equalsIgnoreCase("ncbi-blastp")
						|| data.get(i).equalsIgnoreCase("blastp")
						|| data.get(i).equalsIgnoreCase("ebi-blastp")) {

					this.blastPAvailable = true;
					this.stats_blastPAvailable = true;
					ncbip_count ++;
				}

				if(data.get(i).equalsIgnoreCase("ncbi-blastx")
						|| data.get(i).equalsIgnoreCase("blastx")
						|| data.get(i).equalsIgnoreCase("ebi-blastx")) {

					this.stats_blastXAvailable = true;
				}
			}

			if(ncbip_count != hmmer_count) {

				if(ncbip_count> hmmer_count) {

					this.hmmerAvailable = false;
				}
				else {

					this.blastPAvailable = false;
				}
			}
			stmt.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#getStats()
	 */
	public String[][] getStats() {

		this.setBlastType();

		String[][] res_bp = null, res_hm = null, res_bx = null;
		int res_length = 0;
		int countStats = 0;

		if(this.stats_blastPAvailable) {

			res_bp = this.getSpecificStats("'ncbi-blastp' OR LOWER(program) LIKE 'blastp' OR LOWER(program) LIKE 'ebi-blastp'");
			countStats++;
			res_length += res_bp.length;
		}

		if(this.stats_hmmerAvailable) {

			if (countStats>0)
				res_length+=3;

			res_hm = this.getSpecificStats("'hmmer'");
			countStats++;
			res_length += res_hm.length;
		}

		if(this.stats_blastXAvailable) {

			if (countStats>0)
				res_length+=3;

			res_bx = this.getSpecificStats("'ncbi-blastx' OR LOWER(program) LIKE 'blastx' OR LOWER(program) LIKE 'ebi-blastpx'");
			countStats++;
			res_length += res_bx.length;
		}

		String[][] res = new String[res_length][];

		if(countStats>1) {

			int index = 0;

			if(this.stats_blastPAvailable) {

				res [index] = new String[]{"BLASTP"};

				for(int i = 0 ; i< res_bp.length; i++) {

					index++;
					res[index] = res_bp [i];
				}
			}

			if(this.stats_hmmerAvailable) {

				if(index>0) {

					index++;
					res[index] = new String[]{};
					index++;
				}

				res[index] = new String[]{"HMMER"};

				for(int i = 0 ; i< res_hm.length; i++) {

					index++;
					res[index] = res_hm [i];
				}
			}

			if(this.stats_blastXAvailable) {

				if(index>0) {

					index++;
					res[index] = new String[]{};
					index++;
				}

				//index++;
				res[index] = new String[]{"BLASTX"};

				for(int i = 0 ; i< res_bx.length; i++) {

					index++;
					res[index] = res_bx [i];
				}
			}

			return res;
		}
		else if(this.blastPAvailable) {

			return res_bp;
		}
		if(this.hmmerAvailable) {

			return res_hm;
		}
		else {

			return res_bx;
		}
	}

	/**
	 * @param program
	 * @return
	 */
	public String[][] getSpecificStats(String program) {

		int num=0, noLocusTag=0, noQuery=0, noGene=0, noChromosome=0, noOrganelle=0, no_similarity=0;

		String[][] res = new String[15][];
		try {

			Statement stmt = this.connection.createStatement();

			ArrayList<String[]> data = HomologyAPI.getSpecificStats(program, stmt);

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);

				num++;
				if(list[1]==null) noLocusTag++;
				if(list[2]==null) noQuery++;
				if(list[3]==null || list[3].isEmpty()) noGene++;
				if(list[4]==null) noChromosome++;
				if(list[5]==null || list[5].isEmpty()) noOrganelle++;
			}

			ArrayList<String> result = HomologyAPI.getAllFromGeneHomology(program, stmt);

			for(int i=0; i<result.size(); i++){
				no_similarity++;
				num++;
			}

			res[0] = new String[] {"Number of Genes", ""+num};
			res[1] = new String[] {"Number of Genes without similarities", ""+no_similarity};
			res[2] = new String[] {"Number of Genes with unavailable locus tag", ""+noLocusTag};
			res[3] = new String[] {"Number of Genes with unavailable query", ""+noQuery};
			res[4] = new String[] {"Number of Genes with unavailable gene name", ""+noGene};
			res[5] = new String[] {"Number of Genes with unavailable chromosome identifier", ""+noChromosome};
			res[6] = new String[] {"Number of Genes with unavailable organelle alocation", ""+noOrganelle};

			Integer rs1 = HomologyAPI.getNumberOfHomologueGenes(program, stmt);

			res[7] = new String[] {"Number of homologue genes", rs1.toString()};

			double homologueAv = (rs1/(Double.valueOf(num)));

			res[8] = new String[] {"Average homologues per gene", ""+homologueAv};

			ArrayList<String> list = HomologyAPI.getTaxonomy(program, stmt);

			int orgNum=0, eukaryota=0, bacteria=0, archea=0, virus=0, other=0;

			for(int i=0; i<list.size(); i++){
				orgNum++;
				if(list.get(i)!=null) {

					if(list.get(i).startsWith("Eukaryota")) eukaryota++;
					if(list.get(i).startsWith("Bacteria")) bacteria++;
					if(list.get(i).startsWith("Archaea")) archea++;
					if(list.get(i).startsWith("Viruses")) virus++;
					if(list.get(i).startsWith("other sequences")) other++;
				}
			}

			res[9] = new String[] {"Number of organisms with at least one homologue gene", ""+orgNum};
			res[10] = new String[] {"\t Eukaryota:\t", ""+eukaryota};
			res[11] = new String[] {"\t Bacteria:\t", ""+bacteria};
			res[12] = new String[] {"\t Archaea:\t", ""+archea};
			res[13] = new String[] {"\t Viruses:\t", ""+virus};
			res[14] = new String[] {"\t other sequences:\t", ""+other};

			stmt.close();
		}
		catch(Exception e){e.printStackTrace();}

		return res;
	}

	/**
	 * @param alfa
	 * @return
	 */
	public GenericDataTable getAllGenes(String database, boolean useCache) {

		try {

			boolean thresholdBool = false;

			this.tableRowIndex = new TreeMap<String, Integer>();
			this.keys = new TreeMap<Integer, String>();
			this.reverseKeys = new TreeMap<Integer, Integer>();

			this.initialLocus = new TreeMap<Integer, String>();
			this.initialNames = new TreeMap<Integer, String>();

			this.initialProdItem = new TreeMap<Integer,String>();
			this.initialEcItem = new TreeMap<Integer,String>();

			this.getProject().setInitialiseHomologyData(false);

			this.setBlastType();

			ArrayList<String> columnsNames = new ArrayList<String>();
			columnsNames.add("info");
			columnsNames.add("genes");
			columnsNames.add("status");
			columnsNames.add("name");

			//			boolean hasChromosome = isEukaryote();
			//
			//			if(hasChromosome)
			//				columnsNames.add("chromosome");

			columnsNames.add("product");
			columnsNames.add("score");
			columnsNames.add("EC number(s)");
			columnsNames.add("score");
			columnsNames.add("notes");
			//			columnsNames.add("Select");

			GenericDataTable qrt = new GenericDataTable(columnsNames, "EC numbers", "") {

				private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int col) {

					if (col<4 //|| (hasChromosome && col == 4) 
							|| this.getColumnClass(col).equals(Boolean.class) 
							|| this.getColumnClass(col).equals(String[].class) || this.getColumnName(col).equals("notes"))  {
						return true;
					}
					else return false;
				}
			};
			
			if(!useCache || this.score.size() == 0)
				getDataFromDatabase();

//			this.getProductRank();
			this.getECRank();

			//PROD |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
			Pair<Map<String, String>, Map<String, List<String>>> pairProd = this.getProductRank();
			this.prodName = pairProd.getA();
			this.prodKeys = pairProd.getB();

			//EC |||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||||
			Pair<Map<String, String>, Map<String, List<String>>> pairEC = this.getECRank();
			this.ecName = pairEC.getA();
			this.ecKeys = pairEC.getB();
			
			Set<Integer> specificDatabase = null;

			Map<Integer, List<Object>> data;

			data = new TreeMap<>();

			if(!database.isEmpty()) {

				specificDatabase = this.blastGeneDatabase.get(database);

				int position  = 0;

				for(Integer index : this.geneData.keySet()) {

					if(specificDatabase.contains(Integer.parseInt(this.originalKeys.get(index)))) {
						data.put(position, this.geneData.get(index));

						this.keys.put(position, this.originalKeys.get(index));//genes list
						this.tableRowIndex.put(this.originalKeys.get(index), position);

						this.initialLocus.put(position, initialOriginalLocus.get(index));
						this.initialNames.put(position, initialOriginalNames.get(index));

						this.reverseKeys.put(Integer.parseInt(this.originalKeys.get(index)), position);//reverse genes list

						position++;
					}
				}
			}
			else {

				for(Integer index : this.geneData.keySet()) {

					data.put(index, this.geneData.get(index));

					this.keys.put(index, this.originalKeys.get(index));//genes list
					this.tableRowIndex.put(this.originalKeys.get(index), index);

					this.initialLocus.put(index, initialOriginalLocus.get(index));
					this.initialNames.put(index, initialOriginalNames.get(index));

					this.reverseKeys.put(Integer.parseInt(this.originalKeys.get(index)), index);//reverse genes list

				}

			}

			int size = this.getArraySize(this.keys.values());

			this.enzyme = new String[(size)+1][];
			this.ecnPercent = new String[(size)+1][];
			this.prodPercent = new String[(size)+1][];
			this.product = new String[(size)+1][];

			for(Integer index  : data.keySet()) {

				List<Object> dataList = new ArrayList<>(data.get(index));

				dataList = this.processProductNamesData(index, dataList);

				dataList = this.processECNumberData(index, dataList,  thresholdBool);

				data.put(index, dataList);
			}


			//			for(Integer index  : data.keySet()) {
			//				
			//				if(specificDatabase == null) {
			//
			//					List<Object> dataList = data.get(index);
			//
			//					dataList = this.processProductNamesData(index, dataList);
			//
			//					dataList = this.processECNumberData(index, dataList,  thresholdBool);
			//
			//					data.put(index, dataList);
			//				}
			//				else {
			//					
			//					if(specificDatabase.contains(index)) {
			//
			//						List<Object> dataList = data.get(index);
			//
			//						dataList = this.processProductNamesData(index, dataList);
			//
			//						dataList = this.processECNumberData(index, dataList,  thresholdBool);
			//
			//						data.put(index, dataList);
			//
			//					}
			//
			//				}
			//
			//			}

			//			if(this.blastPAvailable && this.hmmerAvailable) 
			//				geneData = this.processBlastHmmerSimilarities(geneData, size);

			for(Integer key : this.keys.keySet()) {

				if(data.containsKey(key))
					qrt.addLine(data.get(key), this.keys.get(key));
			}

			return qrt;
		}
		catch (Exception e) {
			Workbench.getInstance().error(e);
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param geneData
	 * @param size
	 * @return
	 */
	//	private Map<Integer, List<Object>> processBlastHmmerSimilarities(Map<Integer,List<Object>> geneData, int size) {
	//
	//		Map<Integer,List<Object>> newGeneData = new TreeMap<Integer,List<Object>>();
	//		Map<String,Integer> newtableRowIndex = new TreeMap<String, Integer>();
	//		Map<Integer, String> newKeys = new TreeMap<Integer, String>(), 
	//				newName = new TreeMap<Integer, String>(), 	newInitialNames = new TreeMap<Integer, String>(),
	//				newLocus = new TreeMap<Integer, String>(), newInitialLocus = new TreeMap<Integer, String>(), 
	//				newChromosome = new TreeMap<Integer, String>(), newinitialChromosome = new TreeMap<Integer, String>(),
	//				newNotesMap = new TreeMap<Integer, String>();
	//
	//		int counter = 0;
	//		String[][] jointProducts = new String[size][];
	//		String[][] jointProductsPercent = new String[size][];
	//		String[][] jointEnzymes = new String[size][];
	//		String[][] jointEnzymesPercent = new String[size][];
	//
	//		this.initialProdItem = new TreeMap<Integer, String>();
	//		this.initialEcItem = new TreeMap<Integer, String>();
	////		this.initialSelectedGene = new TreeMap<Integer, Boolean>();
	//		this.reverseKeys =  new TreeMap<Integer, Integer>();
	//
	//		for(Integer index  : this.blastPGeneDataEntries.keySet()) {
	//
	//			Integer key = Integer.parseInt(this.keys.get(index)); 
	//			newKeys.put(counter , key+"");
	//			this.reverseKeys.put(key, counter);
	//			newtableRowIndex.put(key+"",counter);
	//
	//			if(this.initialLocus.containsKey(key))
	//				newInitialLocus.put(counter , this.initialLocus.get(index));
	//			else if(this.initialLocus.containsKey(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))))
	//				newInitialLocus.put(counter , this.initialLocus.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))));
	//
	//			if(this.initialNames.containsKey(key))
	//				newInitialNames.put(counter , this.initialNames.get(index));
	//
	//			if(this.initialChromosome.containsKey(key))
	//				newinitialChromosome.put(counter , this.initialChromosome.get(index));
	//
	//			//////////////////////////////////////////////////////////////////////////////
	//
	//			if(this.namesList.containsKey(key))
	//				newName.put(key , this.namesList.get(key));
	//
	//			if(this.locusList.containsKey(key))
	//				newLocus.put(key , this.locusList.get(key));
	//
	//			if(this.chromosome.containsKey(key))
	//				newChromosome.put(key , this.chromosome.get(key));
	//
	//			if(this.notesMap.containsKey(key))
	//				newNotesMap.put(key , this.notesMap.get(key));
	//
	//			List<String> products = new ArrayList<String>(),productsPercentage = new ArrayList<String>(),
	//					ec = new ArrayList<String>(), ecPercentage = new ArrayList<String>();
	//
	//			List<Object> dataList = this.joinBlastAndHmmer(geneData.get(index), geneData.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))),
	//					products, productsPercentage,
	//					ec, ecPercentage, 
	//					index, counter);
	//
	//			jointProducts[key] = products.toArray(new String[products.size()]);
	//			jointProductsPercent[key] =  productsPercentage.toArray(new String[productsPercentage.size()]);
	//			jointEnzymes[key] = ec.toArray(new String[ec.size()]);
	//			jointEnzymesPercent[key] = ecPercentage.toArray(new String[ecPercentage.size()]);
	//
	//			newGeneData.put(counter, dataList);
	//			counter++;
	//		}
	//
	//		this.product = jointProducts;
	//		this.prodPercent =  jointProductsPercent;
	//		this.enzyme = jointEnzymes;
	//		this.ecnPercent = jointEnzymesPercent;
	//		this.keys = newKeys;
	//		this.tableRowIndex=newtableRowIndex;
	//		this.namesList = newName;
	//		this.initialNames = newInitialNames;
	//		this.locusList = newLocus;
	//		this.initialLocus = newInitialLocus;
	//		this.chromosome = newChromosome;
	//		this.initialChromosome = newinitialChromosome;
	//		this.notesMap = newNotesMap;
	//
	//		return newGeneData;
	//	}

	/**
	 * @param data_blast
	 * @param data_hmmer
	 * @param ecPercentage 
	 * @param ec 
	 * @param productsPercentage 
	 * @param products 
	 * @param index
	 * @param row 
	 * @return
	 */
	//	private List<Object> joinBlastAndHmmer(List<Object> data_blast, List<Object> data_hmmer, List<String> products, List<String> productsPercentage, List<String> ec, List<String> ecPercentage, int index, int row) {
	//
	//		List<Object> ql = new ArrayList<Object>();
	//
	//		boolean selected = false, processNext=true;
	//
	//		for(int i = 0; i < data_blast.size() ; i++) {
	//
	//			if(processNext) {
	//
	//				Object data = data_blast.get(i);
	//
	//				if (data == null) {
	//
	//					ql.add(data);
	//				}
	//				else if(data.getClass().isArray()) {
	//
	//					int name = i;
	//					List<Object> scoresList = this.processArrays((String[]) data, (String[]) data_hmmer.get(name),true,index, products, productsPercentage, row); 
	//
	//					for(Object obj:scoresList)
	//						ql.add(obj);
	//
	//					i++;
	//					i++;
	//					int ecs = i;
	//
	//					scoresList = this.processArrays((String[]) data_blast.get(ecs), (String[]) data_hmmer.get(ecs), false,index, ec, ecPercentage, row); 
	//					
	//					if(scoresList.size()>0 && !((String) scoresList.get(1)).isEmpty()) {
	//
	//						if(((String) scoresList.get(1)).equals("manual")) {
	//
	//							selected = true;
	//						}
	//						else {
	//
	//							if(Double.parseDouble(((String) scoresList.get(1))) >= this.threshold) {
	//
	//								selected = true;
	//							}
	//							else {
	//
	//								selected = false;
	//								scoresList.set(1,"<"+this.threshold);
	//							}
	//						}
	//					}
	//
	//					for(Object obj : scoresList) {
	//
	//						ql.add(obj);
	//					}
	//					processNext=false;
	//				}
	//				else if(data.getClass().equals(Boolean.class)) {
	//
	//					ql.add(selected);
	////					this.initialSelectedGene.put(row, selected);
	//					processNext = true;
	//				}
	//				else {
	//
	//					ql.add(data);
	//					processNext = true;
	//				}
	//			}
	//			else {
	//
	//				processNext=true;
	//			}
	//		}
	//
	//		return ql;
	//	}

	/**
	 * @param blast_data
	 * @param hmmer_data
	 * @param product
	 * @param index
	 * @param dataList
	 * @param dataPercentage
	 * @param row 
	 * @return
	 */
	//	private List<Object> processArrays(String[] blast_data, String[] hmmer_data, boolean product, int index, List<String> dataList, List<String> dataPercentage, int row) {
	//
	//		List<Object> array = new ArrayList<Object>();
	//
	//		Set<String> data = new TreeSet<String>();
	//
	//		for(int i = 0; i < blast_data.length; i++) 
	//			data.add(blast_data[i]);
	//
	//		for(int i = 0; i < hmmer_data.length; i++) 
	//			data.add(hmmer_data[i]);
	//
	//		@SuppressWarnings("unchecked")
	//		Pair<String,Double> pairs[] = new Pair[data.size()];
	//
	//		int j=0;
	//		for(String data_name:data) {
	//
	//			Double blast_score=0.0, hmmer_score=0.0, final_score = 0.0;
	//
	//			for(int i = 0; i < blast_data.length; i++) {
	//
	//				if(product) {
	//
	//					if(this.product[Integer.parseInt(keys.get(index))][i].equals(data_name)) {
	//
	//						if(prodPercent[Integer.parseInt(keys.get(index))][i].isEmpty())
	//							blast_score = 0.0;
	//						else
	//							blast_score = Double.parseDouble(prodPercent[Integer.parseInt(keys.get(index))][i]);
	//					}
	//				}
	//				else {
	//
	//					if(this.enzyme[Integer.parseInt(keys.get(index))][i].equals(data_name)) {
	//						
	//						String score = ecnPercent[Integer.parseInt(keys.get(index))][i];
	//
	//						if(score.isEmpty())
	//							blast_score = 0.0;
	//						else {
	//							blast_score = Double.parseDouble(score);
	//					}}
	//						
	//				}
	//
	//			}
	//
	//			for(int i = 0; i < hmmer_data.length; i++) {
	//
	//				if(product) {
	//
	//					if(this.product[Integer.parseInt(keys.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))))][i].equals(data_name)) {
	//
	//
	//						if(prodPercent[Integer.parseInt(keys.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))))][i].isEmpty())
	//							hmmer_score = 0.0;
	//						else
	//							hmmer_score = Double.parseDouble(prodPercent[Integer.parseInt(keys.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))))][i]);
	//					}
	//				}
	//				else {
	//
	//					if(this.enzyme[Integer.parseInt(keys.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))))][i].equals(data_name)) {
	//						
	//						String score = ecnPercent[Integer.parseInt(keys.get(this.hmmerGeneDataEntries.get(this.blastPGeneDataEntries.get(index))))][i];
	//
	//						if(score.isEmpty())
	//							hmmer_score = 0.0;
	//						else {
	//							hmmer_score = Double.parseDouble(score);
	//					}}
	//				}
	//			}
	//			
	//			final_score = (blast_score*this.blastHmmerWeight + hmmer_score*(1-this.blastHmmerWeight));
	//
	//			if(blast_score>0 && hmmer_score>0) {
	//
	//				//final_score = (blast_score*this.blastHmmerWeight + hmmer_score*(1-this.blastHmmerWeight));
	//			}
	//			else if(blast_score>0) {
	//
	//				//				if (this.hmmerAvailable && this.blastHmmerWeight!=0) {
	//				//
	//				//					final_score = (blast_score*0.75);
	//				//				}
	//				//				else {
	//				//
	//				//					final_score = (blast_score);
	//				//				}
	//			}
	//			else if(hmmer_score>0) {
	//
	//				//				if (this.blastPAvailable && this.blastHmmerWeight!=1) {
	//				//
	//				//					final_score = (hmmer_score*0.75);
	//				//				}
	//				//				else {
	//				//
	//				//					final_score = (hmmer_score);
	//				//				}
	//			}
	//			Pair<String,Double> pair = new Pair<String,Double>(data_name,final_score);
	//			
	//			pairs[j] = pair;
	//			j++;
	//		}
	//
	//		Arrays.sort(pairs, new PairComparator<Double>());
	//
	//		String[] results_data = new String[data.size()];
	//		String[] results_score = new String[data.size()];
	//		DecimalFormatSymbols separator = new DecimalFormatSymbols();
	//		separator.setDecimalSeparator('.');
	//		DecimalFormat format = new DecimalFormat("##.##",separator);
	//
	//		j=0;
	//		while (j < pairs.length) {
	//
	//			String data_name = pairs[j].getA();
	//			Double final_score = pairs[j].getB();
	//			results_data[j] = data_name;
	//
	//			if(final_score>=0) {
	//
	//				results_score[j] = format.format(final_score);
	//			}
	//			else {
	//
	//				results_score[j]="manual";
	//
	//			}
	//
	//			dataList.add(j,data_name);
	//			dataPercentage.add(j,results_score[j]);
	//
	//			j++;
	//		}
	//
	//		if(product) {
	//
	//			if(results_data.length>0) {
	//
	//				this.initialProdItem.put(row, results_data[0]);
	//			}
	//			else{
	//
	//				this.initialProdItem.put(row, "");
	//			}
	//		}
	//		else {
	//
	//			//			if(results_score.length>0 && !results_data[0].isEmpty()) {
	//			//
	//			//				if(results_score[0].equalsIgnoreCase("manual")) {
	//			//
	//			//					this.initialEcItem.put(row, results_data[0]);
	//			//				} 
	//			//				else if(Double.parseDouble(results_score[0]) >= this.threshold) {
	//			//
	//			//					this.initialEcItem.put(row, results_data[0]);
	//			//				}
	//			//				else {
	//			//
	//			//					this.initialEcItem.put(row, "");
	//			//				}
	//			//			}
	//
	//			if(results_score.length>0 ){
	//
	//				this.initialEcItem.put(row, results_data[0]);
	//			} 
	//
	//			else {
	//				this.initialEcItem.put(row, "");
	//			}
	//		}
	//
	//		if(results_data.length>0) {
	//
	//			array.add(0,results_data);
	//			array.add(1,results_score[0]);
	//		}
	//		else {
	//
	//			array.add(0,new String[0]);
	//			array.add(1,"");
	//		}
	//		return array;
	//	}

	/**
	 * @param id
	 * @return
	 */
	public String getGeneLocus(Integer id) {

		return this.initialLocus.get(id);
	}

	/**
	 * @param row
	 * @return
	 */
	public DataTable[] getRowInfo(int row) {

		int key = Integer.parseInt(this.getKeys().get(row));

		DataTable[] res = null;

		try {

			Statement statement = this.connection.createStatement();

			Map<String, Boolean> availabilities = HomologyAPI.getHomologyAvailabilities(key, statement); 
			boolean gene_blastXAvailable = availabilities.get("gene_blastXAvailable");
			boolean gene_blastPAvailable = availabilities.get("gene_blastPAvailable");
			boolean gene_hmmerAvailable = availabilities.get("gene_hmmerAvailable");

			int statsCounter =  0;

			if(gene_hmmerAvailable)
				statsCounter++;

			if(gene_blastPAvailable)
				statsCounter++;

			if(gene_blastXAvailable)
				statsCounter++;

			res = new DataTable[statsCounter*2+2];

			boolean interProAvailable = HomologyAPI.getInterproAvailability(key, statement);

			if(interProAvailable)
				res = new DataTable[res.length+1];

			///////////////////////////////////////////////////

			int datatableCounter = 0;
			ArrayList<String> columnsNames = new ArrayList<String>();

			if(gene_blastPAvailable) {

				columnsNames.add("reference ID");
				columnsNames.add("locus ID");
				columnsNames.add("status");
				columnsNames.add("organism");
				columnsNames.add("e Value");
				columnsNames.add("score (bits)");
				columnsNames.add("product");
				columnsNames.add("EC number");
				res[datatableCounter] = new DataTable(columnsNames, "Homology Data - BLASTP");

				for(ArrayList<String> lists : HomologyAPI.getHomologyResults(key, statement, "ncbi-blastp", "blastp", "ebi-blastp" ))
					res[datatableCounter].addLine(lists);

				datatableCounter++;

				columnsNames = new ArrayList<String>();
				columnsNames.add("organism");
				columnsNames.add("phylogenetic tree");

				res[datatableCounter] = new DataTable(columnsNames, "Taxonomy - BLASTP");

				for(ArrayList<String> lists : HomologyAPI.getHomologyTaxonomy(key, statement, "ncbi-blastp", "blastp", "ebi-blastp" ))
					res[datatableCounter].addLine(lists);

				datatableCounter++;
			}


			if(gene_blastXAvailable) {

				columnsNames.add("reference ID");
				columnsNames.add("locus ID");
				columnsNames.add("status");
				columnsNames.add("organism");
				columnsNames.add("e Value");
				columnsNames.add("score (bits)");
				columnsNames.add("product");
				columnsNames.add("EC number");
				res[datatableCounter] = new DataTable(columnsNames, "Homology Data - BLASTX");

				for(ArrayList<String> lists : HomologyAPI.getHomologyResults(key, statement, "ncbi-blastx", "blastx", "ebi-blastx" ))
					res[datatableCounter].addLine(lists);


				datatableCounter++;

				columnsNames = new ArrayList<String>();
				columnsNames.add("organism");
				columnsNames.add("phylogenetic tree");

				res[datatableCounter] = new DataTable(columnsNames, "Taxonomy - BLASTX");
				for(ArrayList<String> lists : HomologyAPI.getHomologyTaxonomy(key, statement, "ncbi-blastx", "blastx", "ebi-blastx" ))
					res[datatableCounter].addLine(lists);

				datatableCounter++;
			}

			if(gene_hmmerAvailable) {

				columnsNames = new ArrayList<String>();
				columnsNames.add("reference ID");
				columnsNames.add("locus ID");
				columnsNames.add("status");
				columnsNames.add("organism");
				columnsNames.add("E Value");
				columnsNames.add("score (bits)");
				columnsNames.add("product");
				columnsNames.add("EC Number");
				res[datatableCounter] = new DataTable(columnsNames, "Homology Data - HMMER");

				for(ArrayList<String> lists : HomologyAPI.getHomologyResults(key, statement, "hmmer", "hmmer", "hmmer" ))
					res[datatableCounter].addLine(lists);


				datatableCounter++;

				columnsNames = new ArrayList<String>();
				columnsNames.add("organism");
				columnsNames.add("phylogenetic tree");

				res[datatableCounter] = new DataTable(columnsNames, "Taxonomy - HMMER");

				for(ArrayList<String> lists : HomologyAPI.getHomologyTaxonomy(key, statement, "hmmer", "hmmer", "hmmer" ))
					res[datatableCounter].addLine(lists);

				datatableCounter++;
			}

			if(interProAvailable) {

				columnsNames = new ArrayList<String>();
				columnsNames.add("database");
				columnsNames.add("domain accession");
				columnsNames.add("name");
				columnsNames.add("E Value");
				columnsNames.add("EC Number");
				columnsNames.add("product");
				columnsNames.add("GO name");
				columnsNames.add("location");
				columnsNames.add("entry accession");
				columnsNames.add("entry name");
				columnsNames.add("entry description");
				columnsNames.add("start");
				columnsNames.add("end");
				res[datatableCounter] = new DataTable(columnsNames, "Homology Data - InterPro");

				for(ArrayList<String> lists : HomologyAPI.getInterProResult(key, statement))					
					res[datatableCounter].addLine(lists);

				datatableCounter++;
			}

			{

				columnsNames = new ArrayList<String>();
				columnsNames.add("FASTA a.a. Sequence");
				res[datatableCounter] = new DataTable(columnsNames, "Sequence");

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(this.setSequenceView(HomologyAPI.getHomologySequence(key, statement)));
				res[datatableCounter].addLine(ql);

				datatableCounter++;
			}


			{
				columnsNames = new ArrayList<String>();
				columnsNames.add("program");
				columnsNames.add("version");
				columnsNames.add("databaseID");
				columnsNames.add("eValue");
				columnsNames.add("matrix");
				columnsNames.add("wordSize");
				columnsNames.add("gapCosts");
				columnsNames.add("max number Of alignments");
				res[datatableCounter] = new DataTable(columnsNames, "Setup Parameters");

				for(ArrayList<String> lists : HomologyAPI.getHomologySetup(key, statement))
					res[datatableCounter].addLine(lists);

				datatableCounter++;
			}

			statement.close();

		}
		catch(SQLException e) {

			e.printStackTrace();
		}

		return res;
	}

	/**
	 * @param id
	 * @return
	 * @throws SQLException 
	 */
	public DataTable getECBlastSelectionPane(String locus) {

		ArrayList<String> columnsNames = new ArrayList<String>();
		DecimalFormatSymbols separator = new DecimalFormatSymbols();
		separator.setDecimalSeparator('.');
		DecimalFormat format = new DecimalFormat("##.##",separator);

		columnsNames.add("products");
		columnsNames.add("frequency (%)");
		columnsNames.add("occurrences");
		columnsNames.add("frequency score");
		columnsNames.add("taxonomy score");
		columnsNames.add("final score");
		columnsNames.add("program");

		DataTable q = new DataTable(columnsNames, "EC Number(s) Selection");

		try {

			Statement stmt = this.connection.createStatement();

			String query = locus;

			query = HomologyAPI.geneHomologyHasHomologues(locus, stmt);

			double blastTotal=0;
			double hmmerTotal=0;

			ArrayList<String[]> data = HomologyAPI.getProgram(query, stmt);

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);

				if(list[3].equals("hmmer"))
					hmmerTotal+=Integer.parseInt(list[2]);
				else
					blastTotal+=Integer.parseInt(list[2]);
			}

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[1]);
				if(list[3].equals("hmmer"))
					ql.add(format.format(Integer.parseInt(list[2])/hmmerTotal*100)+" %");
				else
					ql.add(format.format(Integer.parseInt(list[2])/blastTotal*100)+" %");

				ql.add(list[2]);
				ql.add(this.getScore1().get(list[0]));
				ql.add(this.getScore2().get(list[0]));
				ql.add(this.getScore().get(list[0]));
				ql.add(list[3]);
				q.addLine(ql);
			}

		}
		catch (SQLException e) {e.printStackTrace();}
		return q;
	}

	/**
	 * @param id
	 * @return
	 */
	public DataTable getProductSelectionPane(String locus){

		ArrayList<String> columnsNames = new ArrayList<String>();
		DecimalFormatSymbols separator = new DecimalFormatSymbols();
		separator.setDecimalSeparator('.');
		DecimalFormat format = new DecimalFormat("##.##",separator);

		columnsNames.add("Products");
		columnsNames.add("frequency (%)");
		columnsNames.add("occurrences");
		columnsNames.add("frequency score");
		columnsNames.add("taxonomy score");
		columnsNames.add("final score");
		columnsNames.add("program");

		DataTable q = new DataTable(columnsNames, "Product Selection");

		try {

			Statement stmt = this.connection.createStatement();

			//			ResultSet rs1=stmt.executeQuery("SELECT count(*) FROM geneHomology_has_homologues "+
			//					"INNER JOIN geneHomology ON (geneHomology_s_key = geneHomology.s_key) "+
			//					"WHERE locusTag = '" + locus+"' ");
			//
			//			rs1.next();
			//			int count = rs1.getInt(1);

			//			int r=0;
			double blastTotal=0;
			double hmmerTotal=0;

			ArrayList<String[]> data = HomologyAPI.getProductRankData(locus, stmt);

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);
				if(list[3].equals("hmmer"))
					hmmerTotal+=Integer.parseInt(list[2]);
				else
					blastTotal+=Integer.parseInt(list[2]);
			}

			for(int i=0; i<data.size(); i++){
				String[] list = data.get(i);

				ArrayList<String> ql = new ArrayList<String>();
				ql.add(list[1]);
				if(list[3].equals("hmmer"))
					ql.add(format.format(Integer.parseInt(list[2])/hmmerTotal*100)+" %");
				else
					ql.add(format.format(Integer.parseInt(list[2])/blastTotal*100)+" %");

				ql.add(list[2]);
				ql.add(this.getScoreP1().get(list[0]));
				ql.add(this.getScoreP2().get(list[0]));
				ql.add(this.getScoreP().get(list[0]));
				ql.add(list[3]);
				q.addLine(ql);
			}
		}


		catch(Exception e){e.printStackTrace();}
		return q;
	}

	/**
	 * @param selectedItem
	 * @param row
	 * @return
	 */
	public String getECPercentage(String selectedItem, int row) {

		for(int i = 0; i < this.enzyme[Integer.parseInt(keys.get(row))].length; i++) {

			if(this.enzyme[Integer.parseInt(keys.get(row))][i].trim().equals(selectedItem.trim()))
				return this.ecnPercent[Integer.parseInt(keys.get(row))][i];
		}
		return "manual";
	}

	/**
	 * @param selectedItem
	 * @param row
	 * @return
	 */
	public String getProductPercentage(String selectedItem, int row) {

		for(int i = 0; i < this.product[Integer.parseInt(keys.get(row))].length; i++) {

			if(this.product[Integer.parseInt(keys.get(row))][i].equals(selectedItem)) {

				if(this.prodPercent[Integer.parseInt(keys.get(row))][i]=="0") {

					return "manual";
				}
				else{ 
					return this.prodPercent[Integer.parseInt(keys.get(row))][i];
				}
			}
		}
		return "manual";
	}

	/* (non-Javadoc)
	 * @see datatypes.metabolic_regulatory.Entity#		()
	 */
	public String getName() {

		return "enzymes";
	}


	/**
	 * Remove the Enzymes annotation instance from AIBench 
	 */
	public void remove() {

		List<ClipboardItem> items = Core.getInstance().getClipboard().getItemsByClass(EnzymesAnnotationDataInterface.class);
		ClipboardItem torem = null;
		for(ClipboardItem item : items){
			if(item.getUserData().equals(this)){
				torem = item;
				break;
			}
		}
		Core.getInstance().getClipboard().removeClipboardItem(torem);
		System.gc();
	}

	/**
	 * @return
	 */
	public Map<String, List<String>> get_uniprot_ecnumbers() {

		try {
			Statement stmt = this.connection.createStatement();

			Map<String, List<String>> result = HomologyAPI.getUniprotEcNumbers(stmt);

			stmt.close();
			return result;
		}
		catch(Exception e) {

			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param row
	 * @param data
	 * 
	 * Sets a Map of user edited Product Lists
	 * 
	 */
	public void setEditedProductData(int row, String[] data) {

		this.editedProductData.put(row, data);
	}

	/**
	 * @return a Map of user edited Product Lists
	 */
	public Map<Integer, String[]> getEditedProductData() {

		return editedProductData;
	}

	/**
	 * @param data
	 * 
	 * Creates a new user edited product list
	 */
	public void setEditedProductData(Map<Integer, String[]> data) {
		this.editedProductData = data;
	}

	/**
	 * @param row
	 * @param data
	 * 
	 * Sets a Map of user edited Enzyme List
	 * 
	 */
	public void setEditedEnzymeData(int row, String[] data) {
		this.editedEnzymeData.put(row, data);
	}

	/**
	 * @return a Map of user edited Enzyme List
	 */
	public Map<Integer, String[]> getEditedEnzymeData() {
		return editedEnzymeData;
	}

	public void setEditedEnzymeData(Map<Integer, String[]> data) {
		this.editedEnzymeData = data;
	}

	//	/**
	//	 * @param selectedGene
	//	 * 
	//	 * Sets a Map of user edited Selected Gene
	//	 * 
	//	 */
	//	public void setSelectedGene(Map<Integer, Boolean> selectedGene) {
	//		this.selectedGene = selectedGene;
	//	}
	//
	//	/**
	//	 * @return a Map of user edited Selected Gene
	//	 */
	//	public Map<Integer, Boolean> getSelectedGene() {
	//		return selectedGene;
	//	}

	/**
	 * Get previously committed scorer parameter values
	 * 
	 */
	public void getCommitedScorerData(String blastDatabase){

		this.setCommittedThreshold(-1);
		this.setCommittedUpperThreshold(-1);
		this.setCommittedBalanceBH(-1);
		this.setCommittedAlpha(-1);
		this.setCommittedBeta(-1);
		this.setCommittedMinHomologies(-1);

		Statement stmt;

		try {

			stmt = this.connection.createStatement();

			ArrayList<String> result = HomologyAPI.getCommitedScorerData(stmt, blastDatabase);

			for(int i=0; i<result.size(); i++){

				this.setCommittedThreshold(Double.parseDouble(result.get(0)));
				this.setCommittedUpperThreshold(Double.parseDouble(result.get(1)));
				this.setCommittedBalanceBH(Double.parseDouble(result.get(2)));
				this.setCommittedAlpha(Double.parseDouble(result.get(3)));
				this.setCommittedBeta(Double.parseDouble(result.get(4)));
				this.setCommittedMinHomologies(Integer.parseInt(result.get(5)));

			}

			this.hasCommittedData=false;

			if(HomologyAPI.hasCommitedData(stmt))
				this.hasCommittedData=true;

			stmt.close();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get previously committed Homology Data
	 * 
	 */
	public void getCommittedHomologyData() {

		this.committedLocusList = new TreeMap<Integer, String>();
		this.committedNamesList = new TreeMap<Integer, String>();
		this.committedProdItem = new TreeMap<Integer, String>();
		this.committedEcItem = new TreeMap<Integer, String>();
		this.committedChromosome = new TreeMap<Integer, String>();
		this.committedNotesMap = new TreeMap<Integer, String>();
		this.committedSelected = new TreeMap<Integer, Boolean>();
		this.committedProductList = new TreeMap<Integer, String[]>();
		this.committedEnzymeList = new TreeMap<Integer, String[]>();


		Statement stmt;

		try {

			stmt = this.connection.createStatement();

			ArrayList<String[]> result = HomologyAPI.getCommittedHomologyData(stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				if(this.tableRowIndex.containsKey(list[1])) {

					int row = this.tableRowIndex.get(list[1]);
					if(list[2] != null && !list[2].equalsIgnoreCase("null") && !list[2].isEmpty())
						this.committedLocusList.put(row, list[2]);

					if(list[3] != null && !list[3].equalsIgnoreCase("null") && !list[3].isEmpty())
						this.committedNamesList.put(row, list[3]);

					if(list[4] != null && !list[4].equalsIgnoreCase("null"))
						this.committedProdItem.put(row, list[4]);

					if(list[5] != null && !list[5].equalsIgnoreCase("null"))
						this.committedEcItem.put(row, list[5]);

					if(list[6] != null && !list[6].equalsIgnoreCase("null") && !list[6].isEmpty())
						this.committedSelected.put(row, Boolean.valueOf(list[6]));

					if(list[7] != null && !list[7].equalsIgnoreCase("null") && !list[7].isEmpty())
						this.committedChromosome.put(row, list[7]);

					if(list[8] != null && !list[8].equalsIgnoreCase("null") && !list[8].isEmpty())
						this.committedNotesMap.put(row, list[8]);
				}
			}

			Set<String> dataSet = new HashSet<String>();
			int dataKey=-1;

			result = HomologyAPI.getCommittedHomologyData2(stmt);

			for(int i=0; i<result.size(); i++){

				String[] list = result.get(i);
				if(dataSet.isEmpty()) { 

					dataKey = this.tableRowIndex.get(list[0]);
					dataSet.add(list[1]);
				}
				else {
					if (Integer.parseInt(list[0])==dataKey) {
						dataSet.add(list[1]);
					}
					else {
						this.committedProductList.put(dataKey,dataSet.toArray(new String[dataSet.size()]));
						dataKey = this.tableRowIndex.get(list[0]);
						dataSet = new HashSet<String>();
						dataSet.add(list[1]);
					}

					if(i == result.size()) {
						this.committedProductList.put(dataKey,dataSet.toArray(new String[dataSet.size()]));
					}
				}
			}

			dataSet = new HashSet<String>();
			dataKey = -1;
			result = HomologyAPI.getCommittedHomologyData3(stmt);

			for(int i=0; i<result.size(); i++){

				String[] list = result.get(i);

				if(dataSet.isEmpty()) { 

					dataKey = this.tableRowIndex.get(list[0]);
					dataSet.add(list[1]);
				}
				else {
					if (Integer.parseInt(list[0])==dataKey) {
						dataSet.add(list[1]);
					}
					else {
						this.committedEnzymeList.put(dataKey,dataSet.toArray(new String[dataSet.size()]));
						dataKey = this.tableRowIndex.get(list[0]);
						dataSet = new HashSet<String>();
						dataSet.add(list[1]);
					}
					if(i == result.size()) {
						this.committedEnzymeList.put(dataKey,dataSet.toArray(new String[dataSet.size()]));
					}
				}
			}
			stmt.close();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Save user changes to the local database
	 * 
	 */
	public boolean commitToDatabase(String blastDatabase) {

		boolean result = true;

		Statement stmt;

		try {

			stmt = this.connection.createStatement();
			DatabaseType databaseType = this.connection.getDatabaseType();

			ArrayList<String> data = HomologyAPI.getCommitedScorerData(stmt, blastDatabase);

			if(data.size()>0){
				String query = "UPDATE scorerConfig SET threshold = "+this.getThreshold()
				+ ", upperThreshold= "+ this.getUpperThreshold()
				+ ", balanceBH= "+0.0
				+ ", alpha= "+this.getAlpha()
				+ ", beta= "+this.getBeta()
				+ ", minHomologies= "+this.getMinimumNumberofHits()
				+ " WHERE blastDB = '" + blastDatabase + "';";

				HomologyAPI.executeQuery(query, stmt);
			}
			else{
				String query = "INSERT INTO scorerConfig VALUES("+this.getThreshold()+","
						+ ""+ this.getUpperThreshold() +","
						+ ""+0.0+","
						+ ""+this.getAlpha()+","
						+ ""+this.getBeta()+","
						+ ""+this.getMinimumNumberofHits()+","
						+ "'" + blastDatabase + "',"
						+ "false,"
						+ "false);";

				HomologyAPI.executeQuery(query, stmt);
			}

			HomologyAPI.setLastestUsedBlastDatabase(stmt, blastDatabase);

			Map<Integer, String> database_locus = HomologyAPI.getDatabaseLocus(stmt);

			for(Integer key : this.getLocusList().keySet()) {

				boolean exists = HomologyAPI.homologyDataHasKey(key, stmt);

				if(exists){
					String query = "UPDATE homologyData SET locusTag='" 
							+ DatabaseUtilities.databaseStrConverter(this.getLocusList().get(key), databaseType)+"' WHERE geneHomology_s_key='"+key+"' ;";

					HomologyAPI.executeQuery(query, stmt);
				}
				else{
					String query = "INSERT INTO homologyData (geneHomology_s_key, locusTag, product, ecNumber) " +
							"VALUES ('"+key+"','"+DatabaseUtilities.databaseStrConverter(this.getLocusList().get(key), databaseType)+"','null','null')";

					HomologyAPI.executeQuery(query, stmt);
				}
			}

			for(Integer key : this.getNamesList().keySet()) {

				boolean exists = HomologyAPI.homologyDataHasKey(key, stmt);

				if(this.getNamesList().get(key)!=null && !this.getNamesList().get(key).equalsIgnoreCase("null") && database_locus.containsKey(key)) {

					if(exists){
						String query = "UPDATE homologyData SET geneName='"
								+DatabaseUtilities.databaseStrConverter(this.getNamesList().get(key), databaseType)+"' WHERE geneHomology_s_key='"+key+"' ;";

						HomologyAPI.executeQuery(query, stmt);
					}
					else{
						String query = "INSERT INTO homologyData (geneHomology_s_key, geneName, locusTag, product, ecNumber) " 
								+ "VALUES ('"+key+"', '"+DatabaseUtilities.databaseStrConverter(this.getNamesList().get(key), databaseType)
								+ "','"+ database_locus.get(key)+"','null','null')";

						HomologyAPI.executeQuery(query, stmt);
					}
				}
			}

			for(Integer key : this.getChromosome().keySet()) {

				if(this.getChromosome().get(key)!=null && !this.getChromosome().get(key).equalsIgnoreCase("null") && database_locus.containsKey(key)) {

					boolean exists = HomologyAPI.homologyDataHasKey(key, stmt);

					if(exists){
						String query = "UPDATE homologyData SET chromosome='"
								+ DatabaseUtilities.databaseStrConverter(this.getChromosome().get(key), databaseType)+"' WHERE geneHomology_s_key='"+key+"' ;";

						HomologyAPI.executeQuery(query, stmt);
					}
					else{
						String query = "INSERT INTO homologyData (geneHomology_s_key,chromosome, locusTag, product, ecNumber) "
								+ "VALUES ('"+key+"','"+ DatabaseUtilities.databaseStrConverter(this.getChromosome().get(key), databaseType)
								+ "','"+ database_locus.get(key)+"', 'null', 'null')";

						HomologyAPI.executeQuery(query, stmt);
					}
				}
			}

			for(Integer key : this.getNotesMap().keySet()) {

				if(this.getNotesMap().get(key)!=null && !this.getNotesMap().get(key).equalsIgnoreCase("null") && //!this.getNotesMap().get(key).isEmpty()
						database_locus.containsKey(key)) {

					boolean exists = HomologyAPI.homologyDataHasKey(key, stmt);

					if(exists){
						String query = "UPDATE homologyData SET notes='"+DatabaseUtilities.databaseStrConverter(this.getNotesMap().get(key), databaseType)
						+"' WHERE geneHomology_s_key='"+key+"' ;";

						HomologyAPI.executeQuery(query, stmt);
					}
					else{
						String query = "INSERT INTO homologyData (geneHomology_s_key, notes, locusTag, product, ecNumber) "
								+ "VALUES ('"+key+"','"+DatabaseUtilities.databaseStrConverter(this.getNotesMap().get(key), databaseType)+"','"
								+ database_locus.get(key)+"', 'null', 'null')";

						HomologyAPI.executeQuery(query, stmt);
					}
				}
			}

			//			for(Integer key : this.getSelectedGene().keySet()) {
			//
			//				if(this.getSelectedGene().get(key)!=null && database_locus.containsKey(key)) {
			//
			//					boolean exists = HomologyAPI.homologyDataHasKey(key, stmt);
			//					
			//					if(exists){
			//						String query = "UPDATE homologyData SET selected="+this.getSelectedGene().get(key)+" WHERE geneHomology_s_key='"+key+"' ;";
			//						
			//						HomologyAPI.executeQuery(query, stmt);
			//
			//					}
			//					else {
			//						String query = "INSERT INTO homologyData (geneHomology_s_key, selected, locusTag, product, ecNumber) " +
			//								"VALUES ('"+key+"',"+this.getSelectedGene().get(key)+",'"+ database_locus.get(key)+"', 'null', 'null')";
			//						
			//						HomologyAPI.executeQuery(query, stmt);
			//					}
			//				}
			//			}

			for(Integer key : this.getProductList().keySet()) {

				if(this.getProductList().get(key)!=null && !this.getProductList().get(key).equalsIgnoreCase("null") && database_locus.containsKey(key)) {

					boolean exists = HomologyAPI.homologyDataHasKey(key, stmt);

					if(exists){
						String query = "UPDATE homologyData SET product='"+DatabaseUtilities.databaseStrConverter(this.getProductList().get(key),this.connection.getDatabaseType())
						+ "' WHERE geneHomology_s_key='"+key+"' ;";

						HomologyAPI.executeQuery(query, stmt);
					}
					else {
						String query ="INSERT INTO homologyData (geneHomology_s_key, product, locusTag, ecNumber) " +
								"VALUES ('"+key+"', '"+DatabaseUtilities.databaseStrConverter(this.getProductList().get(key), databaseType)+"','"
								+ database_locus.get(key)+"', 'null')";

						HomologyAPI.executeQuery(query, stmt);
					}
				}
			}

			for(Integer key : this.getEditedProductData().keySet()) {

				if(this.getEditedProductData().get(key)!=null && database_locus.containsKey(key)) {

					boolean exists = HomologyAPI.homologyDataHasKey(key, stmt);
					String s_key;

					if(exists){

						s_key = HomologyAPI.getHomologyDataKey(key, stmt);
						String query = "UPDATE homologyData SET product='"
								+DatabaseUtilities.databaseStrConverter(this.getProductList().get(key),this.connection.getDatabaseType())
								+"' WHERE geneHomology_s_key='"+key+"' ;";

						HomologyAPI.executeQuery(query, stmt);

					}
					else {

						String query = "INSERT INTO homologyData (geneHomology_s_key, product, locusTag, ecNumber) " +
								"VALUES ('"+key+"', '"+DatabaseUtilities.databaseStrConverter(this.getProductList().get(key), databaseType)+
								"','"+ database_locus.get(key)+"', 'null')";

						s_key = HomologyAPI.insertIntoHomologyData(query, stmt);
					}

					boolean exists2 = HomologyAPI.productListHasKey(s_key, stmt);

					if(exists2) {
						String query = "DELETE FROM productList WHERE homologyData_s_key=\'"+s_key+"\'";

						HomologyAPI.executeQuery(query, stmt);
					}

					String [] products = this.getEditedProductData().get(key);

					for(String product : products) {

						String query = "INSERT INTO productList (homologyData_s_key, otherNames) VALUES ('"+s_key+"',\'"
								+DatabaseUtilities.databaseStrConverter(product,this.connection.getDatabaseType())+"')";

						HomologyAPI.executeQuery(query, stmt);
					}
				}
			}

			for(Integer key : this.getEnzymesList().keySet()) {

				if(this.getEnzymesList().get(key)!=null && !this.getEnzymesList().get(key).equalsIgnoreCase("null") && database_locus.containsKey(key)) {

					boolean exists = HomologyAPI.homologyDataHasKey(key, stmt);

					if(exists) {
						String query = "UPDATE homologyData SET ecNumber='"
								+DatabaseUtilities.databaseStrConverter(this.getEnzymesList().get(key),this.connection.getDatabaseType())
								+"' WHERE geneHomology_s_key='"+key+"' ;";

						HomologyAPI.executeQuery(query, stmt);
					}
					else {
						String query = "INSERT INTO homologyData (geneHomology_s_key, ecNumber, locusTag, product) " +
								"VALUES ('"+key+"', '"+DatabaseUtilities.databaseStrConverter(this.getEnzymesList().get(key), databaseType)
								+"','"+ database_locus.get(key)+"', 'null')";

						HomologyAPI.executeQuery(query, stmt);
					}
				}
			}

			for(Integer key : this.getEditedEnzymeData().keySet()) {

				if(this.getEditedEnzymeData().get(key)!=null && database_locus.containsKey(key)) {

					boolean exists = HomologyAPI.homologyDataHasKey(key, stmt);
					String s_key;

					if(exists) {
						s_key = HomologyAPI.getHomologyDataKey(key, stmt);
						String query = "UPDATE homologyData SET ecNumber='"+DatabaseUtilities.databaseStrConverter(this.getEnzymesList().get(key), databaseType)
						+"' WHERE geneHomology_s_key='"+key+"' ;";

						HomologyAPI.executeQuery(query, stmt);
					}
					else {

						String query = "INSERT INTO homologyData (geneHomology_s_key, ecNumber, locusTag, product) " +
								"VALUES ('"+key+"', '"+DatabaseUtilities.databaseStrConverter(this.getEnzymesList().get(key), databaseType)
								+"','"+ database_locus.get(key)+"', 'null')";

						s_key = HomologyAPI.insertIntoHomologyData(query, stmt);
					}


					boolean exists2 = HomologyAPI.ecNumberListHasKey(s_key, stmt);
					if(exists2){
						String query = "DELETE FROM ecNumberList WHERE homologyData_s_key=\'"+s_key+"\'";
						HomologyAPI.executeQuery(query, stmt);
					}

					String [] ecs = this.getEditedEnzymeData().get(key);

					for(String ec : ecs){
						String query = "INSERT INTO ecNumberList (homologyData_s_key, otherECNumbers) VALUES ('"+s_key+"',\'"+ec+"')";				
						HomologyAPI.executeQuery(query, stmt);
					}
				}
			}

			this.setEditedProductData(new TreeMap<Integer, String[]>());
			this.setEditedEnzymeData(new TreeMap<Integer, String[]>());
			this.setLocusList(new TreeMap<Integer, String>());
			this.setNamesList(new TreeMap<Integer, String>());
			//			this.setSelectedGene(new TreeMap<Integer, Boolean>());
			this.setChromosome(new TreeMap<Integer, String>());
			this.setNotesMap(new TreeMap<Integer, String>());
			this.setProductList(new TreeMap<Integer, String>());
			this.setEnzymesList(new TreeMap<Integer, String>());
			//			this.setSelectedGene(new TreeMap<Integer, Boolean>());

			if(this.getProject().getFileName()!=null && !this.getProject().getFileName().isEmpty() && new File(this.getProject().getFileName()).exists()) {

				SaveProject saveProject = new SaveProject();
				saveProject.saveProject(this.getProject());
				File f = new File(this.getProject().getFileName().replace("\\", "/"));
				saveProject.save(f);
			}
			stmt.close();
		} catch (Exception e) {

			result = false;
			e.printStackTrace();
		}
		return result;
	}


	/**
	 * @param stmt
	 * @param ecRank
	 * @param ecName
	 * @param format
	 * @return
	 */
	public Pair<Map<String,String>, Map<String,List<String>>> getECRank(){//, double alpha){

		Pair<Map<String,String>, Map<String,List<String>>> pair = new Pair<Map<String,String>, Map<String,List<String>>>(null, null);

		Map<String,String> ecName = new TreeMap<String,String>();
		Map<String,List<String>> ecKeys = new TreeMap<String, List<String>>();

		try {

			//			ArrayList<String[]> result = HomologyAPI.getDataFromecNumberRank(stmt);

			Map<String, Double> ecNumberRank = new TreeMap<String, Double>();

			for (int i = 0; i<dataFromEcNumber.size(); i++){	
				String[] list = dataFromEcNumber.get(i);

				//number of eckeys for each gene
				Set<String> s_key = new TreeSet<String>();
				if(ecKeys.containsKey(list[1]))					
					s_key = new TreeSet<String>(ecKeys.get(list[1]));

				s_key.add(list[0]);
				ecKeys.put(list[1],new ArrayList<String>(s_key));

				double productEcScore = Double.parseDouble(list[3]);
				String formatedProductEcScore = this.format.format(productEcScore);

				ecNumberRank.put(list[0], Double.parseDouble(formatedProductEcScore));
				ecName.put(list[0], list[2]);
				//ecRank.put(rs.getString(1), rs.getString(4));
			}

			//			result = HomologyAPI.getEcRank(stmt);

			Map<String,List<Integer>> orgRank = new TreeMap<String,List<Integer>>();

			for (int i = 0; i<ecRank.size(); i++){	
				String[] list = ecRank.get(i);

				// organism rank for each ecnumber
				List<Integer> orgTax = new ArrayList<Integer>();
				if(orgRank.containsKey(list[0]))
					orgTax = orgRank.get(list[0]);

				orgTax.add(Integer.parseInt(list[1]));
				orgRank.put(list[0], orgTax);
			}

			int maxRank=0;

			//			String result2 = HomologyAPI.getMaxTaxRank(stmt);

			String thisRank =null;
			if(taxonomyRank!=null && !taxonomyRank.equals("")){
				if((thisRank = taxonomyRank)!=null){
					maxRank=Integer.parseInt(thisRank);
				}
			}

			//			Map<String,Double> homologuesCount = HomologyAPI.getHomologuesCountByEcNumber(stmt);

			for(String key : orgRank.keySet()) {

				double frequency = ecNumberRank.get(key)/this.homologuesCountEcNumber.get(key);

				if(frequency>1)
					frequency=1;

				BlastScorer bs = new BlastScorer(frequency, orgRank.get(key), maxRank, this.alpha, this.beta, minimumNumberofHits);

				this.score1.put(key,this.format.format(bs.getS1()));
				this.score2.put(key, this.format.format(bs.getS2()));
				this.score.put(key, this.format.format(bs.getS()));

			}
		}
		catch (Exception e) {e.printStackTrace();}

		pair.setA(ecName);
		pair.setB(ecKeys);

		return pair;
	}

	/**
	 * @param stmt
	 * @param prodRank
	 * @param prodName
	 * @param format
	 * @return
	 */
	public Pair<Map<String,String>, Map<String,List<String>>> getProductRank(){


		Pair<Map<String,String>, Map<String,List<String>>> pair = new Pair<Map<String,String>, Map<String,List<String>>>(null, null);

		Map<String,String> prodName = new TreeMap<String,String>();
		Map<String,List<String>> prodKeys = new TreeMap<String, List<String>>();

		try {

			//			ArrayList<String[]> result = HomologyAPI.getProductRank(stmt);
			Map<String, Double> productRank = new TreeMap<>();
			
			for (int i = 0; i<this.dataFromProduct.size(); i++){	
				String[] list = this.dataFromProduct.get(i);

				//number of productKeys for each gene
				
				Set<String> s_key = new TreeSet<String>();
				if(prodKeys.containsKey(list[1]))
					s_key = new TreeSet<String>(prodKeys.get(list[1]));

				s_key.add(list[0]);
				prodKeys.put(list[1],new ArrayList<String>(s_key));

				double productRankScore = Double.parseDouble(list[3]);
				String formatedProductRankScore = this.format.format(productRankScore);

				productRank.put(list[0], Double.parseDouble(formatedProductRankScore));
				prodName.put(list[0], list[2]);
			}

			//			result = HomologyAPI.getTaxRank(stmt);

			Map<String,List<Integer>> orgRank = new TreeMap<>();

			for (int i = 0; i<this.taxRank.size(); i++){	
				String[] list = this.taxRank.get(i);

				// organism rank for each product
				List<Integer> orgTax = new ArrayList<Integer>();

				if(orgRank.containsKey(list[0]))
					orgTax = orgRank.get(list[0]);

				orgTax.add(Integer.parseInt(list[1]));
				orgRank.put(list[0], orgTax);
			}

			int maxRank=0;

			//			String result2 = HomologyAPI.getMaxTaxRank(stmt);
			String thisRank =null;

			if(this.maxTaxRank!=null && !this.maxTaxRank.equals("")){
				if((thisRank = this.maxTaxRank)!=null){
					maxRank=Integer.parseInt(thisRank);
				}
			}

			//			Map<String,Double> homologuesCount = HomologyAPI.getHomologuesCountByProductRank(stmt);

			for(String key : orgRank.keySet()) {

				if(productRank.containsKey(key) && this.homologuesCountProduct.containsKey(key)) {

					double frequency = productRank.get(key)/this.homologuesCountProduct.get(key);
					if(frequency>1)
						frequency=1;

					BlastScorer bs = new BlastScorer(frequency, orgRank.get(key), maxRank, this.alpha, this.beta, minimumNumberofHits);

					Map<String, String> s1 = this.getScoreP1();
					if(s1==null){s1 = new TreeMap<String, String>();}
					s1.put(key,this.format.format(bs.getS1()));
					this.setScoreP1(s1);

					Map<String, String> s2 = this.getScoreP2();
					if(s2==null){s2 = new TreeMap<String, String>();}
					s2.put(key, this.format.format(bs.getS2()));
					this.setScoreP2(s2);

					Map<String, String> s = this.getScoreP();
					if(s==null){s = new TreeMap<String, String>();}
					s.put(key, this.format.format(bs.getS()));
					
					this.setScoreP(s);

				}
			}
		}
		catch (Exception e) {e.printStackTrace();}

		pair.setA(prodName);
		pair.setB(prodKeys);

		return pair;
	}

	/**
	 * @param stmt
	 * @param prodRank
	 * @param prodName
	 * @return
	 */
	public Map<String,List<String>> getProductRank(Statement stmt, Map<String,String> prodName) {
		Map<String,List<String>> prodKeys = new TreeMap<String,List<String>>();
		try
		{
			ArrayList<String[]> result = HomologyAPI.getProductRank2(stmt);

			String gene = "";
			for (int i = 0; i<result.size(); i++){	
				String[] list = result.get(i);

				if(list[1].equals(gene))
				{
					List<String> s_key=prodKeys.get(gene);
					s_key.add(list[0]);
					prodKeys.put(gene,s_key);
				}
				else
				{
					gene = list[1];
					List<String> s_key = new ArrayList<String>();
					s_key.add(list[0]);
					prodKeys.put(list[1], s_key);		
				}
				prodName.put(list[0], list[2]);
				this.scoreP.put(list[0], list[3]);
			}

		}
		catch (Exception e) {e.printStackTrace();}
		return prodKeys;
	}

	/**
	 * @param stmt
	 * @return
	 * @throws SQLException
	 */
	public Map<Integer,List<Object>> getGeneInformation(Statement stmt) throws SQLException{

		this.hmmerGeneDataEntries = new  TreeMap<String, Integer>();
		this.blastPGeneDataEntries = new  TreeMap<Integer, String>();
		this.initialChromosome = new TreeMap<Integer,String>();
		//		this.initialSelectedGene = new TreeMap<Integer,Boolean>();
		this.initialOriginalLocus = new TreeMap<Integer, String>();
		this.initialOriginalNames = new TreeMap<Integer, String>();
		//this.namesList = new TreeMap<Integer, String>(); limpa nomes gravados no objeto
		this.originalKeys = new TreeMap<Integer, String>();


		TreeMap<Integer,List<Object>> geneData = new TreeMap<Integer,List<Object>>();
		//ResultSet rs = stmt.executeQuery("SELECT geneHomology.s_key, locusTag, gene, chromosome, organelle FROM geneHomology order by geneHomology.s_key;");

		ArrayList<String[]> result = HomologyAPI.getGenesInformation(stmt);

		int tableIndex = 0;
		boolean hasChromosome = isEukaryote();

		for (int i = 0; i<result.size(); i++){	
			String[] list = result.get(i);

			List<Object> ql = new ArrayList<Object>();
			ql.add("");

			ql.add(list[1]);
			ql.add(list[5]);
			ql.add(list[2]);

			if( (this.blastPAvailable && (list[6].equalsIgnoreCase("ncbi-blastp") || list[6].equalsIgnoreCase("blastp") || list[6].equalsIgnoreCase("ebi-blastp"))) ||
					(this.stats_blastXAvailable && (list[6].equalsIgnoreCase("ncbi-blastx") || list[6].equalsIgnoreCase("blastx") || list[6].equalsIgnoreCase("ebi-blastx"))) ||
					(this.hmmerAvailable && list[6].equalsIgnoreCase("hmmer"))) {

				geneData.put(tableIndex, ql);
				this.originalKeys.put(tableIndex, list[0]);//genes list
				//				this.tableRowIndex.put(list[0],tableIndex);
				//				this.reverseKeys.put(Integer.parseInt(list[0]), tableIndex);//reverse genes list


				if(hasChromosome) {

					ql.add(list[3]);
					this.initialChromosome.put(tableIndex, list[3]);
				}

				if(list[6].equalsIgnoreCase("hmmer"))
					this.hmmerGeneDataEntries.put(list[7], tableIndex);

				//				System.out.println(this.blastPGeneDataEntries.size());

				if(list[6].equalsIgnoreCase("ncbi-blastp") || list[6].equalsIgnoreCase("blastp") || list[6].equalsIgnoreCase("ebi-blastp"))
					this.blastPGeneDataEntries.put(tableIndex, list[7]);


				//				this.initialLocus.put(tableIndex, list[1]);
				//				this.initialNames.put(tableIndex, list[2]);

				this.initialOriginalLocus.put(tableIndex, list[1]);
				this.initialOriginalNames.put(tableIndex, list[2]);

				tableIndex++;
			}

		}

		return geneData;
	}

	/**
	 * @param index
	 * @param dataList
	 * @param prodKeys
	 * @param prodRank
	 * @param prodName
	 * @param format
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object> processProductNamesData(int index, List<Object> dataList){

		String[][] prod = new String[2][0];
		Pair<String,Double> products[] = null;

		String keyString = this.keys.get(index);

		int key = Integer.parseInt(keyString);

		this.product[key]= new String[0];
		this.prodPercent[key] = new String[0];

		if(prodKeys.containsKey(keyString)) {

			List<String> keyList = prodKeys.get(keyString);

			products = new Pair[keyList.size()];
			int j=0;

			while(j<keyList.size()) {

				String listKey = keyList.get(j);
				String prodRankScore = this.getScoreP().get(listKey);
				Double score = Double.parseDouble(prodRankScore);
				products[j]=new Pair<String, Double>(this.prodName.get(listKey), score);
				j++;
			}

			Arrays.sort(products, new PairComparator<Double>());

			prod = new String[2][keyList.size()+1];

			this.product[key] = new String[keyList.size()+1];

			this.prodPercent[key] = new String[keyList.size()+1];

			this.product[key][0]="";

			this.prodPercent[key][0]="";

			prod[0][0]="";
			prod[1][0]="";
			j = 0;

			while (j < products.length) {

				Double score = products[j].getB();
				prod[0][j+1] = products[j].getA();
				this.product[key][j+1] = products[j].getA();

				if(score>=0) {

					prod[1][j+1]= format.format(score);
				}
				else {

					prod[1][j+1]="manual";
					this.product[key][j+1]=prod[0][j+1];
				}
				this.prodPercent[key][j+1] = prod[1][j+1];
				j++;
			}

			if(prod[0].length>0) {

				dataList.add(prod[0]);
			}
			else {

				dataList.add(new String[0]);
			}

			if(prod[1].length>0) {

				dataList.add(prod[1][1]);
			}
			else {

				dataList.add(prod[1][0]);
			}

			this.initialProdItem.put(index, prod[0][1]);
		}
		else
		{
			dataList.add(new String[0]);
			dataList.add("");
		}

		return dataList;
	}

	/**
	 * @param index
	 * @param dataList
	 * @param ecKeys
	 * @param ecRank
	 * @param ecName
	 * @param format
	 * @param thresholdBool
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Object> processECNumberData(int index, List<Object> dataList, boolean thresholdBool){

		String[][] ecn = new String[2][0];
		Pair<String,Double> ecnumber[] = null;

		String keyString = this.keys.get(index);

		int key = Integer.parseInt(keyString);

		this.enzyme[key] = new String[0];

		this.ecnPercent[key] = new String[0];

		if (ecKeys.containsKey(keyString)) {

			List<String> keyList = ecKeys.get(keyString);

			ecnumber = new Pair[keyList.size()];
			int j=0;
			while(j<keyList.size()) {
				String listKey = keyList.get(j);

				String ecRankScore = this.getScore().get(listKey);
				Double score = Double.parseDouble(ecRankScore);
				ecnumber[j]=new Pair<String, Double>(ecName.get(listKey), score);

				j++;
			}


			Arrays.sort(ecnumber, new PairComparator<Double>()); 

			ecn = new String[2][keyList.size()+1];

			this.enzyme[key]= new String[keyList.size()+1];

			this.ecnPercent[key]=	new String[keyList.size()+1];

			this.enzyme[key][0]="";

			this.ecnPercent[key][0]="";

			ecn[0][0]="";
			ecn[1][0]="";
			j = 0;
			while (j < ecnumber.length) {

				Double score = ecnumber[j].getB();
				ecn[0][j+1] = ecnumber[j].getA();
				this.enzyme[key][j+1]=ecnumber[j].getA();

				if(score>=0) {

					ecn[1][j+1]= format.format(score);
				}
				else {
					ecn[1][j+1]=" ";
				}
				this.ecnPercent[key][j+1] = ecn[1][j+1];
				j++;
			}

			if(ecn[1].length>0 && ecnumber[0].getB()>=threshold)
				this.initialEcItem.put(index, ecn[0][1]);
			else
				this.initialEcItem.put(index, "");
		}



		dataList.add(ecn[0]);
		//boolean selected;
		String ec_score = "";
		String note = "";

		if(ecn[1].length>0 && ecnumber[0].getB()>=this.threshold) {

			ec_score = ecn[1][1];
			//selected = new Boolean(true);
		}
		else {

			if(ecn[1].length>0) {

				ec_score = "<"+this.threshold;
			}
			//			selected = new Boolean(false);
		}

		dataList.add(ec_score);
		dataList.add(note);
		//		dataList.add(selected);
		//		this.initialSelectedGene.put(index, selected);

		return dataList;
	}


	/**
	 * @return
	 */
	public boolean isEukaryote() {

		return this.isEukaryote;
	}


	/**
	 * @param chromosome
	 */
	public void setChromosome(Map<Integer, String> chromosome) {
		this.chromosome = chromosome;
	}

	/**
	 * @param dataSet
	 * @return
	 */
	public int getArraySize(Collection <String> dataSet){
		int max = 0;

		for(String key: dataSet)
			if(Integer.parseInt(key)>max)
				max=Integer.parseInt(key);

		return max+1;
	}



	/**
	 * @return the initial locus Tag list
	 */
	public Map<Integer, String> getInitialLocus() {
		return initialLocus;
	}

	/**
	 * @return the initial names List
	 */
	public Map<Integer, String> getInitialNames() {
		return initialNames;
	}

	//		/**
	//		 * @return the initial selected product (after the pair comparator selection)
	//		 */
	//		public Map<Integer, String[]> getInitialProduct() {
	//			return initialProduct;
	//		}
	//
	//		/**
	//		 * @return the initial selected enzyme (after the pair comparator selection)
	//		 */
	//		public Map<Integer, String> getInitialEnzyme() {
	//			return initialEnzyme;
	//		}

	/**
	 * @return
	 */
	public Map<Integer, String> getChromosome() {
		return chromosome;
	}

	/**
	 * @param score1 the score1 to set
	 */
	public void setScore1(Map<String, String> score1) {
		this.score1 = score1;
	}

	/**
	 * @return the score1
	 */
	public Map<String, String> getScore1() {
		return score1;
	}

	/**
	 * @param score2 the score2 to set
	 */
	public void setScore2(Map<String, String> score2) {
		this.score2 = score2;
	}

	/**
	 * @return the score2
	 */
	public Map<String, String> getScore2() {
		return score2;
	}

	/**
	 * @param score the score to set
	 */
	public void setScore(Map<String, String> score) {
		this.score = score;
	}

	/**
	 * @return the score
	 */
	public Map<String, String> getScore() {
		return score;
	}

	/**
	 * @param score1 the score1 to set
	 */
	public void setScoreP1(Map<String, String> scoreP1) {
		this.scoreP1 = scoreP1;
	}

	/**
	 * @return the score1
	 */
	public Map<String, String> getScoreP1() {
		return scoreP1;
	}

	/**
	 * @param score2 the score2 to set
	 */
	public void setScoreP2(Map<String, String> scoreP2) {
		this.scoreP2 = scoreP2;
	}

	/**
	 * @return the score2
	 */
	public Map<String, String> getScoreP2() {
		return scoreP2;
	}

	/**
	 * @param score the score to set
	 */
	public void setScoreP(Map<String, String> scoreP) {
		this.scoreP = scoreP;
	}

	/**
	 * @return the score
	 */
	public Map<String, String> getScoreP() {
		return scoreP;
	}

	/**
	 * @return the alpha
	 */
	public Double getAlpha() {
		return alpha;
	}

	/**
	 * @param alpha the alpha to set
	 */
	public void setAlpha(Double alpha) {
		this.alpha = alpha;
	}

	/**
	 * @param sequence
	 * @return
	 */
	private String setSequenceView(String sequence){
		String seq = new String();
		for(int i=0;i<sequence.toCharArray().length;i++)
		{
			if(i!=0 && (i%70)==0){seq+="\n";}
			seq+=sequence.charAt(i);			
		}
		return seq;
	}

	/**
	 * @return
	 */
	public int getSelectedRow() {
		return selectedRow;
	}

	/**
	 * @param selectedRow
	 */
	public void setSelectedRow(int selectedRow) {
		this.selectedRow = selectedRow;
	}

	/**
	 * @return a list of user selected products
	 */
	public Map<Integer, String> getProductList() {
		return prodItem;
	}

	/**
	 * @return a list of user selected ec numbers
	 */
	public Map<Integer, String> getEnzymesList() {

		return ecItem;
	}

	/**
	 * @return a list of user selected gene names
	 */
	public Map<Integer, String> getNamesList() {

		return namesList ;
	}

	/**
	 * @param prodItem
	 * 
	 * Sets a list of user selected products ec numbers
	 * 
	 */
	public void setProductList(Map<Integer, String> prodItem) {
		this.prodItem = prodItem;		
	}


	/**
	 * @param ecItem
	 * 
	 * Sets a list of user selected ec numbers
	 * 
	 */
	public void setEnzymesList(Map<Integer, String> ecItem) {
		this.ecItem = ecItem;

	}

	/**
	 * @param namesList
	 * 
	 * Sets a list of gene names
	 * 
	 */
	public void setNamesList(Map<Integer, String> namesList) {
		this.namesList = namesList ;
	}


	/**
	 * @param locusList
	 * 
	 * Sets a list of user selected gene Locus Tags
	 * 
	 */
	public void setLocusList(Map<Integer, String> locusList) {
		this.locusList=locusList;

	}

	/**
	 * @return a list of user selected locus Tags
	 */
	public Map<Integer, String> getLocusList() {
		return locusList;
	}

	/**
	 * @return the notesMap
	 */
	public Map<Integer, String> getNotesMap() {
		return notesMap;
	}

	/**
	 * @param notesMap the notesMap to set
	 */
	public void setNotesMap(Map<Integer, String> notesMap) {
		this.notesMap = notesMap;
	}

	/**
	 * @return the threshold
	 */
	public Double getThreshold() {
		return this.threshold;
	}

	/**
	 * @param threshold the threshold to set
	 */
	public void setThreshold(Double threshold) {
		this.threshold = threshold;
	}

	/**
	 * @return the beta
	 */
	public Double getBeta() {
		return beta;
	}

	/**
	 * @param beta the beta to set
	 */
	public void setBeta(Double beta) {
		this.beta = beta;
	}

	/**
	 * @return the minimumNumberofHits
	 */
	public int getMinimumNumberofHits() {
		return minimumNumberofHits;
	}

	/**
	 * @param minimumNumberofHits the minimumNumberofHits to set
	 */
	public void setMinimumNumberofHits(int minimumNumberofHits) {
		this.minimumNumberofHits = minimumNumberofHits;
	}

	/**
	 * @return the blastPAvailable
	 */
	public boolean isBlastPAvailable() {
		return blastPAvailable;
	}

	/**
	 * @param blastPAvailable the blastPAvailable to set
	 */
	public void setBlastPAvailable(boolean blastPAvailable) {
		this.blastPAvailable = blastPAvailable;
	}

	/**
	 * @return the blastXAvailable
	 */
	public boolean isBlastXAvailable() {
		return stats_blastXAvailable;
	}

	/**
	 * @param blastXAvailable the blastNAvailable to set
	 */
	public void setBlastXAvailable(boolean blastXAvailable) {
		this.stats_blastXAvailable = blastXAvailable;
	}

	/**
	 * @return the blastPGeneDataEntries
	 */
	public Map<Integer, String> getBlastPGeneDataEntries() {
		return blastPGeneDataEntries;
	}

	/**
	 * @param blastPGeneDataEntries the blastPGeneDataEntries to set
	 */
	public void setBlastPGeneDataEntries(Map<Integer, String> blastPGeneDataEntries) {
		this.blastPGeneDataEntries = blastPGeneDataEntries;
	}

	/**
	 * @return the hmmerAvailable
	 */
	public boolean isHmmerAvailable() {
		return hmmerAvailable;
	}

	/**
	 * @param hmmerAvailable the hmmerAvailable to set
	 */
	public void setHmmerAvailable(boolean hmmerAvailable) {
		this.hmmerAvailable = hmmerAvailable;
	}

	/**
	 * @return the committedProdItem
	 */
	public Map<Integer, String> getCommittedProdItem() {
		return committedProdItem;
	}

	/**
	 * @param committedProdItem the committedProdItem to set
	 */
	public void setCommittedProdItem(Map<Integer, String> committedProdItem) {
		this.committedProdItem = committedProdItem;
	}

	/**
	 * @return the committedEcItem
	 */
	public Map<Integer, String> getCommittedEcItem() {
		return committedEcItem;
	}

	/**
	 * @param committedEcItem the committedEcItem to set
	 */
	public void setCommittedEcItem(Map<Integer, String> committedEcItem) {
		this.committedEcItem = committedEcItem;
	}

	/**
	 * @return the committedNamesList
	 */
	public Map<Integer, String> getCommittedNamesList() {
		return committedNamesList;
	}

	/**
	 * @param committedNamesList the committedNamesList to set
	 */
	public void setCommittedNamesList(Map<Integer, String> committedNamesList) {
		this.committedNamesList = committedNamesList;
	}

	/**
	 * @return the committedLocusList
	 */
	public Map<Integer, String> getCommittedLocusList() {
		return committedLocusList;
	}

	/**
	 * @param committedLocusList the committedLocusList to set
	 */
	public void setCommittedLocusList(Map<Integer, String> committedLocusList) {
		this.committedLocusList = committedLocusList;
	}

	/**
	 * @return the committedChromosome
	 */
	public Map<Integer, String> getCommittedChromosome() {
		return committedChromosome;
	}

	/**
	 * @param committedChromosome the committedChromosome to set
	 */
	public void setCommittedChromosome(Map<Integer, String> committedChromosome) {
		this.committedChromosome = committedChromosome;
	}

	/**
	 * @return the committedNotesMap
	 */
	public Map<Integer, String> getCommittedNotesMap() {
		return committedNotesMap;
	}

	/**
	 * @param committedNotesMap the committedNotesMap to set
	 */
	public void setCommittedNotesMap(Map<Integer, String> committedNotesMap) {
		this.committedNotesMap = committedNotesMap;
	}

	/**
	 * @return the committedSelected
	 */
	public Map<Integer, Boolean> getCommittedSelected() {
		return committedSelected;
	}

	/**
	 * @param committedSelected the committedSelected to set
	 */
	public void setCommittedSelected(Map<Integer, Boolean> committedSelected) {
		this.committedSelected = committedSelected;
	}

	/**
	 * @return
	 */
	public boolean hasCommittedData() {
		return hasCommittedData;
	}

	/**
	 * @return
	 */
	public boolean setHasCommittedData() {
		return hasCommittedData = true;
	}

	/**
	 * @return the committedProductList
	 */
	public Map<Integer, String[]> getCommittedProductList() {
		return committedProductList;
	}

	/**
	 * @param committedProductList the committedProductList to set
	 */
	public void setCommittedProductList(Map<Integer, String[]> committedProductList) {
		this.committedProductList = committedProductList;
	}

	/**
	 * @return the committedEnzymeList
	 */
	public Map<Integer, String[]> getCommittedEnzymeList() {
		return committedEnzymeList;
	}

	/**
	 * @param committedEnzymeList the committedEnzymeList to set
	 */
	public void setCommittedEnzymeList(Map<Integer, String[]> committedEnzymeList) {
		this.committedEnzymeList = committedEnzymeList;
	}

	/**
	 * @return the initialChromosome
	 */
	public Map<Integer, String> getInitialChromosome() {
		return initialChromosome;
	}

	/**
	 * @param initialChromosome the initialChromosome to set
	 */
	public void setInitialChromosome(Map<Integer, String> initialChromosome) {
		this.initialChromosome = initialChromosome;
	}

	/**
	 * @return the initialProdItem
	 */
	public Map<Integer, String> getInitialProdItem() {
		return initialProdItem;
	}

	/**
	 * @param initialProdItem the initialProdItem to set
	 */
	public void setInitialProdItem(Map<Integer, String> initialProdItem) {
		this.initialProdItem = initialProdItem;
	}

	/**
	 * @return the initialEcItem
	 */
	public Map<Integer, String> getInitialEcItem() {

		return initialEcItem;
	}

	/**
	 * @param initialEcItem the initialEcItem to set
	 */
	public void setInitialEcItem(Map<Integer, String> initialEcItem) {

		this.initialEcItem = initialEcItem;
	}
	//
	//	/**
	//	 * @return the initialSelectedGene
	//	 */
	//	public Map<Integer, Boolean> getInitialSelectedGene() {
	//		return initialSelectedGene;
	//	}
	//
	//	/**
	//	 * @param initialSelectedGene the initialSelectedGene to set
	//	 */
	//	public void setInitialSelectedGene(Map<Integer, Boolean> initialSelectedGene) {
	//		this.initialSelectedGene = initialSelectedGene;
	//	}

	/**
	 * @return the keys
	 */
	public Map<Integer, String> getKeys() {
		return keys;
	}

	/**
	 * @param keys the keys to set
	 */
	public void setKeys(Map<Integer, String> keys) {
		this.keys = keys;
	}

	/**
	 * @param mappedLocusList
	 */
	public void setIntegrationLocusList(Map<Integer, String> mappedLocusList) {

		this.integrationLocusList = mappedLocusList;
	}

	/**
	 * @param mappedNamesList
	 */
	public void setIntegrationNamesList(Map<Integer, String> mappedNamesList) {

		this.integrationNamesList = mappedNamesList;
	}

	/**
	 * @param mappedProdItem
	 */
	public void setIntegrationProdItem(Map<Integer, String> mappedProdItem) {

		this.integrationProdItem = mappedProdItem;
	}

	/**
	 * @param mappedEcItem
	 */
	public void setIntegrationEcItem(Map<Integer, String> mappedEcItem) {

		this.integrationEcItem = mappedEcItem;
	}

	/**
	 * @return the integrationProdItem
	 */
	public void setIntegrationChromosome(Map<Integer, String> integrationChromosome) {

		this.integrationChromosome = integrationChromosome;
	}

	//	/**
	//	 * @param mappedSelectedGene
	//	 */
	//	public void setIntegrationSelectedGene(Map<Integer, Boolean> mappedSelectedGene) {
	//
	//		this.integrationSelectedGene = mappedSelectedGene;
	//	}

	/**
	 * @return the integrationLocusList
	 */
	public Map<Integer, String> getIntegrationLocusList() {
		return integrationLocusList;
	}

	/**
	 * @return the integrationNamesList
	 */
	public Map<Integer, String> getIntegrationNamesList() {
		return integrationNamesList;
	}

	/**
	 * @return the integrationProdItem
	 */
	public Map<Integer, String> getIntegrationProdItem() {
		return integrationProdItem;
	}

	/**
	 * @return the integrationNamesList
	 */
	public Map<Integer, String> getIntegrationChromosome() {
		return integrationChromosome;
	}

	/**
	 * @return the integrationEcItem
	 */
	public Map<Integer, String> getIntegrationEcItem() {
		return integrationEcItem;
	}

	//	/**
	//	 * @return the integrationSelectedGene
	//	 */
	//	public Map<Integer, Boolean> getIntegrationSelectedGene() {
	//		return integrationSelectedGene;
	//	}

	/**
	 * @return the reverseKeys
	 */
	public Map<Integer, Integer> getReverseKeys() {
		return reverseKeys;
	}

	/**
	 * @param reverseKeys the reverseKeys to set
	 */
	public void setReverseKeys(Map<Integer, Integer> reverseKeys) {
		this.reverseKeys = reverseKeys;
	}

	/**
	 * @return the committedThreshold
	 */
	public double getCommittedThreshold() {
		return committedThreshold;
	}

	/**
	 * @param committedThreshold the committedThreshold to set
	 */
	public void setCommittedThreshold(double committedThreshold) {
		this.committedThreshold = committedThreshold;
	}

	/**
	 * @return the committedBalanceBH
	 */
	public double getCommittedBalanceBH() {
		return committedBalanceBH;
	}

	/**
	 * @param committedBalanceBH the committedBalanceBH to set
	 */
	public void setCommittedBalanceBH(double committedBalanceBH) {
		this.committedBalanceBH = committedBalanceBH;
	}

	/**
	 * @return the committedAlpha
	 */
	public double getCommittedAlpha() {
		return committedAlpha;
	}

	/**
	 * @param committedAlpha the committedAlpha to set
	 */
	public void setCommittedAlpha(double committedAlpha) {
		this.committedAlpha = committedAlpha;
	}

	/**
	 * @return the committedBeta
	 */
	public double getCommittedBeta() {
		return committedBeta;
	}

	/**
	 * @param committedBeta the committedBeta to set
	 */
	public void setCommittedBeta(double committedBeta) {
		this.committedBeta = committedBeta;
	}

	/**
	 * @return the committedMinHomologies
	 */
	public int getCommittedMinHomologies() {
		return committedMinHomologies;
	}

	/**
	 * @param committedMinHomologies the committedMinHomologies to set
	 */
	public void setCommittedMinHomologies(int committedMinHomologies) {
		this.committedMinHomologies = committedMinHomologies;
	}

//	/**
//	 * @return the firstCall
//	 */
//	public boolean isFirstCall() {
//		return firstCall;
//	}
//
//	/**
//	 * @param firstCall the firstCall to set
//	 */
//	public void setFirstCall(boolean firstCall) {
//		this.firstCall = firstCall;
//	}



	public void setIsEukaryote() {

		if(this.getProject() == null || this.getProject().getOrganismName() == null || this.getProject().getOrganismName().isEmpty())
			Workbench.getInstance().warn("Please set organism taxonomy id.");

		if(this.getProject() == null || this.getProject().getOrganismLineage()== null || this.getProject().getOrganismLineage().startsWith("Eukaryota"))
			this.isEukaryote = true;

		this.isEukaryote =  false;
	}

	/**
	 * Retrieve all genes between lower and upper threshold.
	 * 
	 * @param lowerThreshold
	 * @param upperThreshold
	 * @param usedManual 
	 * @return
	 * @throws SQLException 
	 */
	public Set<String> getGenesInThreshold(double lowerThreshold, double upperThreshold, boolean usedManual) throws SQLException {

		Set<String> genes = new HashSet<>();

		Map<Integer, String> mappedEcItem = this.getInitialEcItem();

		if(this.getCommittedEcItem()!= null)
			for(int row : this.getCommittedEcItem().keySet())
				if(this.getCommittedEcItem().get(row)!=null && !this.getCommittedEcItem().get(row).equalsIgnoreCase("null"))
					mappedEcItem.put(row, this.getCommittedEcItem().get(row));


		for(int row : this.getInitialProdItem().keySet()) {

			String key = this.keys.get(row);

			//mappedEcItem.put(row,this.getEnzymesList().get(key));
			String selectedItem = mappedEcItem.get(row);
			//String selectedItem = this.getIntegrationEcItem().get(key);

			String ecWeigth = "";

			if(this.getIntegrationEcItem().containsKey(row) || this.getEnzymesList().containsKey(row))
				ecWeigth = this.getECPercentage(selectedItem, row);

			if(ecWeigth.equalsIgnoreCase("manual")) {

				if(usedManual)
					genes.add(key);
			}
			else if(ecWeigth.isEmpty()) {

				if(lowerThreshold == 0)					
					genes.add(key);
			}
			else {

				double score = Double.parseDouble(ecWeigth);

				if( score < upperThreshold && score > lowerThreshold)
					genes.add(key);
			}
		}

		Statement statement = this.connection.createStatement();

		Set<String> ret = HomologyAPI.getQueriesFromKeys(genes, statement );

		statement.close();

		return ret;
	}

	/**
	 * Get rows for genes with InterPro entries.
	 * 
	 * @return
	 */
	public List<Integer> getInterProRows() {

		List<Integer> ret = new ArrayList<>();

		try {

			Statement statement = this.connection.createStatement();

			List<String> interProGenes = HomologyAPI.getInterProGenes(statement);

			Map<String, Integer> locusKeys = HomologyAPI.getLocusKeys(statement);;

			for(String locus : locusKeys.keySet())
				if(interProGenes.contains(locus))
					ret.add(this.reverseKeys.get(locusKeys.get(locus)));

			statement.close();
		}
		catch (SQLException e) {

			e.printStackTrace();
		}

		return ret;
	}


	/**
	 * @return the committedUpperThreshold
	 */
	public double getCommittedUpperThreshold() {
		return committedUpperThreshold;
	}


	/**
	 * @param committedUpperThreshold the committedUpperThreshold to set
	 */
	public void setCommittedUpperThreshold(double committedUpperThreshold) {
		this.committedUpperThreshold = committedUpperThreshold;
	}


	/**
	 * @return the upperThreshold
	 */
	public Double getUpperThreshold() {
		return upperThreshold;
	}


	/**
	 * @param upperThreshold the upperThreshold to set
	 */
	public void setUpperThreshold(Double upperThreshold) {
		this.upperThreshold = upperThreshold;
	}


	/**
	public void commitToDatabase() {

		if(this.dsa==null)
		{
			this.dsa=super.getDbt().getMySqlCredentials();
		}
		Statement stmt;
		Map<String,String> homologyNewEntryKey = new TreeMap<String, String>();
		ResultSet rs;
		try 
		{
			stmt = this.dsa.createStatement();
			//products
			Set<Integer> prodKeys = new TreeSet<Integer>(this.getEditedProductData().keySet());
			for(Integer row : prodKeys)
			{
				//Map<String, String> toDeleteKey = new TreeMap<String,String>();
				Set<String> toDeleteName = new TreeSet<String>();
				Set<String> existsName = new TreeSet<String>();
				existsName.add("");
				Set<String> products = new TreeSet<String>(Arrays.asList(this.getEditedProductData().get(row)));
				rs = stmt.executeQuery("SELECT homologyData.s_key, product FROM homologyData WHERE homology_geneHomology_s_key=\'"+keys.get(row)+"\'");
				while(rs.next())
				{
					String name = rs.getString(2);
					toDeleteName.add(name);
					for(String pd: products)
					{
						if(pd.trim().equals(name.trim()))
						{
							existsName.add(pd);
							//toDeleteKey.remove(rs.getString(1));
							//toDeleteName.remove(pd);
							break;
						}
						else
						{
							//toDeleteKey.put(rs.getString(1), name);

						}
					}
				}

				products.removeAll(existsName);
				toDeleteName.removeAll(existsName);
				//				for(String key:toDeleteKey.keySet())
				//				{
				//					stmt.execute("DELETE FROM homologyData where s_key = \'" + key + "\'");
				//				}
				List<String> toDeletePR = new ArrayList<String>();
				rs = stmt.executeQuery("SELECT productRank.s_key, productName FROM productRank WHERE geneHomology_s_key = \'"+keys.get(row)+ "\'");
				while(rs.next())
				{
					for(String tdn:toDeleteName)
					{
						if(tdn.equals(rs.getString(2)))
						{
							toDeletePR.add(rs.getString(1));
						}
					}
				}
				for(String key:toDeletePR)
				{
					stmt.execute("DELETE FROM productRank WHERE s_key=\'"+key+"\'" );
				}
				for(String pd: products)
				{

					rs=stmt.executeQuery("SELECT homology.s_key FROM homology WHERE geneHomology_s_key='"+keys.get(row)+"' AND organism_s_key=1 AND referenceID=\'0\'");
					if(!rs.next())
					{
						stmt.execute("INSERT INTO homology (geneHomology_s_key, organism_s_key, referenceID, gene, eValue, bits) VALUES (\'"+keys.get(row)+"\',\'1\',\'0\',\'null\',\'0.0\',\'0.0\')");
						rs=stmt.executeQuery("SELECT last_insert_id()");
					}
					rs.next();
					String homology_s_key=rs.getString(1);
					homologyNewEntryKey.put(keys.get(row),homology_s_key);

					rs=stmt.executeQuery("SELECT homologyData.s_key FROM homologyData WHERE homology_geneHomology_s_key='"+keys.get(row)+"' AND homology_s_key="+homology_s_key);
					if(!rs.next())
					{	
						stmt.execute("INSERT INTO homologyData (homology_geneHomology_s_key,homology_s_key,definition,calculated_mw,product) " +
								"VALUES(\'"+keys.get(row)+"\',\'"+homology_s_key+"\',\'"+DatabaseUtilities.databaseStrConverter(pd)+"\',0,\'"+DatabaseUtilities.databaseStrConverter(pd)+"\');");
						stmt.execute("INSERT INTO productRank (geneHomology_s_key, productName, rank) VALUES(\'"+keys.get(row)+"\',\'"+DatabaseUtilities.databaseStrConverter(pd)+"\', \'0\');");
					}
					else
					{
						stmt.execute("UPDATE homologyData definition=\'"+DatabaseUtilities.databaseStrConverter(pd)+"\', product=\'"+DatabaseUtilities.databaseStrConverter(pd)+"\' " +
								"WHERE homology_geneHomology_s_key="+keys.get(row)+" AND homology_s_key="+homology_s_key+"");
					}

					String rank="1";
					//if(this.getProductList().get(row).equals(pd)){rank="0";}
					//else{rank="1";}
					rs=stmt.executeQuery("SELECT productrank.s_key FROM productrank WHERE geneHomology_s_key='"+keys.get(row)+"' AND productName=\'"+DatabaseUtilities.databaseStrConverter(pd)+"\'");
					if(!rs.next())
					{
						stmt.execute("INSERT INTO productRank (geneHomology_s_key, productName, rank) VALUES(\'"+keys.get(row)+"\',\'"+DatabaseUtilities.databaseStrConverter(pd)+"\',"+rank+");");
						//rs=stmt.executeQuery("SELECT last_insert_id()");
						stmt.execute("INSERT INTO productRank_has_organism (productRank_s_key, organism_s_key) VALUES(last_insert_id(),1);");
					}
					//					else
					//					{
					//						stmt.execute("UPDATE productRank SET rank="+rank+" WHERE geneHomology_s_key=\'"+keys.get(row)+"\' AND productName=\'"+DatabaseUtilities.databaseStrConverter(pd)+"\'");
					//					}
					//					rs.next();
					//					String productRank_s_key=rs.getString(1);
					//
					//					rs=stmt.executeQuery("SELECT * FROM productRank_has_organism WHERE organism_s_key=1 AND productRank_s_key=\'"+productRank_s_key+"\'");
					//					if(rs.getFetchSize()==0)
					//					{
					//						stmt.execute("INSERT INTO productRank_has_organism (productRank_s_key, organism_s_key) VALUES("+productRank_s_key+",1);");
					//					}
				}
				this.getEditedProductData().remove(row);
			}
			for(Integer row : new TreeSet<Integer>(this.getProductList().keySet()))
			{
				stmt.execute("UPDATE productRank SET rank=0 WHERE geneHomology_s_key=\'"+keys.get(row)+"\' AND productName=\'"+DatabaseUtilities.databaseStrConverter(this.getProductList().get(row))+"\'");
				this.getProductList().remove(row);
			}

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			//enzymes
			Set<Integer> ecKeys = new TreeSet<Integer>(this.getEditedEnzymeData().keySet());
			//int z=0;
			for(Integer row : ecKeys)
			{
				//Map<String, Set<String>> toDeleteKey = new TreeMap<String, Set<String>>();
				Set<String> toDeleteEnzyme = new TreeSet<String>();
				Set<String> existsEC = new TreeSet<String>();
				existsEC.add("");
				String previous_homology_s_key = "", ecnumber="";
				Set<String> enzymes = new TreeSet<String>(Arrays.asList(this.getEditedEnzymeData().get(row)));
				Set<String> eckeys=new TreeSet<String>();
				rs = stmt.executeQuery("SELECT homology.s_key, ecnumber.s_key ,ecnumber FROM homology " +
						"INNER JOIN homology_has_ecnumber on (homology.s_key = homology_has_ecnumber.homology_s_key) " +
						"JOIN ecnumber on (homology_has_ecnumber.ecnumber_s_key = ecnumber.s_key) " +
						"WHERE homology.geneHomology_s_key =" + keys.get(row) + ";");
				while(rs.next())
				{
					if(previous_homology_s_key.equals(rs.getString(1)))
					{
						ecnumber+=", "+rs.getString(3);
						eckeys.add(rs.getString(2));
					}
					else
					{
						previous_homology_s_key=rs.getString(1);
						if(!ecnumber.isEmpty())
						{
							toDeleteEnzyme.add(ecnumber);
							for(String en: enzymes)
							{
								if(en.equals(ecnumber))
								{
									existsEC.add(en);
									//toDeleteKey.remove(rs.getString(1));
									//toDeleteEnzyme.remove(ecnumber);	
									break;
								}
								else
								{
									//toDeleteKey.put(rs.getString(1),eckeys);
								}
							}
						}
						if(rs.getString(3)!=null)
						{
							ecnumber=rs.getString(3);
							eckeys.add(rs.getString(2));
						}
						else
						{
							ecnumber="";
							eckeys=new TreeSet<String>();
						}
					}
				}
				enzymes.removeAll(existsEC);
				toDeleteEnzyme.removeAll(existsEC);

				//				for(String key:toDeleteKey.keySet())
				//				{
				//					stmt.executeUpdate("DELETE FROM homology_has_ecNumber WHERE( homology_s_key = \'" + key + "\' AND ecNumber_s_key =\'" + toDeleteKey.get(key) + "\');");
				//				}

				List<String> toDeleteER = new ArrayList<String>();
				rs = stmt.executeQuery("SELECT ecNumberRank.s_key, ecNumber FROM ecNumberRank WHERE geneHomology_s_key =\'"+keys.get(row)+"\'");
				while(rs.next())
				{
					for(String tde:toDeleteEnzyme)
					{
						if(tde.equals(rs.getString(2)))
						{
							toDeleteER.add(rs.getString(1));
						}
					}
				}
				for(String key:toDeleteER){stmt.execute("DELETE FROM ecNumberRank WHERE s_key = \'" +key + "\'");}
				int entry=0;
				for(String en: enzymes)
				{
					String homology_s_key;
					if(homologyNewEntryKey.containsKey(keys.get(row)))
					{
						homology_s_key = homologyNewEntryKey.get(keys.get(row));
					}
					else{
						rs=stmt.executeQuery("SELECT homology.s_key FROM homology WHERE geneHomology_s_key='"+keys.get(row)+"' AND organism_s_key=1 AND referenceID=\'"+entry+"\'");
						if(!rs.next())
						{
							stmt.execute("INSERT INTO homology (geneHomology_s_key, organism_s_key, referenceID, gene, eValue, bits) VALUES (\'"+keys.get(row)+"\',\'1\',\'"+entry+"\',\'null\',\'0.0\',\'0.0\')");
							rs=stmt.executeQuery("SELECT last_insert_id()");
						}
						rs.next();
						homology_s_key=rs.getString(1);
					}
					String rank="1";
					//if(this.getEnzymesList().get(row).equals(en)){rank="0";}
					//else{rank="1";}
					rs=stmt.executeQuery("SELECT ecNumberRank.s_key FROM ecNumberRank WHERE geneHomology_s_key='"+keys.get(row)+"' AND ecnumber=\'"+en+"\'");
					if(!rs.next())
					{
						stmt.execute("INSERT INTO ecNumberRank (geneHomology_s_key, ecNumber, rank) VALUES(\'"+keys.get(row)+"\',\'"+DatabaseUtilities.databaseStrConverter(en)+"\',"+rank+");");
						stmt.execute("INSERT INTO ecnumberrank_has_organism (ecNumberRank_s_key, organism_s_key) VALUES(last_insert_id(),1);");
						//						rs=stmt.executeQuery("SELECT last_insert_id()");
					}

					StringTokenizer st= new StringTokenizer(en, ", ");
					while(st.hasMoreElements())
					{
						en=st.nextToken();
						rs = stmt.executeQuery("SELECT s_key FROM ecNumber WHERE ecNumber = \'" + DatabaseUtilities.databaseStrConverter(en) + "\'");
						if(!rs.next())
						{
							stmt.execute("INSERT INTO ecNumber (ecNumber) VALUES (\'" + DatabaseUtilities.databaseStrConverter(en) + "\');");
							rs = stmt.executeQuery("SELECT last_insert_id()");
						}
						rs.next();
						String ec_skey=rs.getString(1);

						rs=stmt.executeQuery("SELECT * FROM homology_has_ecNumber WHERE homology_s_key="+homology_s_key+" AND homology_geneHomology_s_key='"+keys.get(row)+"' AND ecNumber_s_key=\'"+ec_skey+"\'");
						if(!rs.next())
						{
							stmt.execute("INSERT INTO homology_has_ecNumber (homology_s_key, ecNumber_s_key, homology_geneHomology_s_key) VALUES (\'"+homology_s_key+"\',\'"+ec_skey+"\',\'"+keys.get(row)+"\');");
						}
					}
					entry++;
					//					else
					//					{
					//						stmt.execute("UPDATE ecNumberRank SET rank="+rank+" WHERE geneHomology_s_key=\'"+keys.get(row)+"\' AND ecnumber=\'"+DatabaseUtilities.databaseStrConverter(en)+"\'");
					//					}
					//					rs.next();
					//					String ecNumberRank_s_key=rs.getString(1);
					//
					//					rs=stmt.executeQuery("SELECT * FROM ecnumberrank_has_organism WHERE organism_s_key=1 AND ecNumberRank_s_key=\'"+ecNumberRank_s_key+"\'");
					//					if(!rs.next())
					//					{
					//						stmt.execute("INSERT INTO ecnumberrank_has_organism (ecNumberRank_s_key, organism_s_key) VALUES("+ecNumberRank_s_key+",1);");
					//					}
				}

				this.getEditedEnzymeData().remove(row);
			}
			for(Integer row : new TreeSet<Integer>(this.getEnzymesList().keySet()))
			{
				stmt.execute("UPDATE ecNumberRank SET rank=0 WHERE geneHomology_s_key=\'"+keys.get(row)+"\' AND ecnumber=\'"+DatabaseUtilities.databaseStrConverter(this.getEnzymesList().get(row))+"\'");
				this.getEnzymesList().remove(row);
			}

			/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//locustags
			for(Integer row : new TreeSet<Integer>(this.getLocusList().keySet()))
			{
				stmt.execute("UPDATE geneHomology SET locusTag=\'"+mysqlStrConverter(getLocusList().get(row))+"\' WHERE s_key = '" +keys.get(row)+"'");
				this.getLocusList().remove(row);
			}
			//names
			for(Integer row : new TreeSet<Integer>(this.getNamesList().keySet()))
			{
				stmt.execute("UPDATE geneHomology SET gene=\'"+ mysqlStrConverter(getNamesList().get(row))+"\' WHERE s_key = '" +keys.get(row)+"'");
				this.getNamesList().remove(row);
			}
			//chromosomes - organelles
			//for(Integer row : this.getEditedProductData().keySet())
			//{
			//	rs = stmt.executeQuery("SELECT geneHomology.s_key, chromosome, organelle, FROM geneHomology where geneHomology.s_key=" + keys.get(row) + ";");
			//	while(rs. next())
			//	{
			//		
			//	}
			//}
			stmt.close();


			Workbench.getInstance().warn("Data successfully loaded into database! \n" +
			"You sould save your project now...");

		}
		catch (SQLException e) {
			Workbench.getInstance().error("There was an error while loading data into database!");
			e.printStackTrace();}
	}
	 */

}
