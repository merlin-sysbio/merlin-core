
package pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.biojava.bio.search.SeqSimilaritySearchHit;
import org.biojava.bio.search.SeqSimilaritySearchResult;

import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.datatypes.EntryData;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.datatypes.HomologuesData;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ebi.uniprot.UniProtAPI;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.NcbiAPI;
import pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.blast.ReadBlasttoList;
import pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.hmmer.ReadHmmertoList;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.BlastSource;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.HmmerRemoteDatabasesEnum;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.HomologySearchServer;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;

/**
 * This class retrieves homology data for the results list provided by the Sequence 
 * Similarity Search Results from the Entrez Protein Database
 * 
 * @author oDias
 */
public class HomologyDataClient {

	/**
	 * homology data variables  
	 */
	private String 
	organismID, blastSetupID, databaseID, version, program, query; 
	private AtomicBoolean cancel;
	private boolean dataRetrieved;
	private int numberOfHits;
	private boolean noSimilarity;
	private HomologuesData homologuesData;
	private long taxonomyID;

	/**
	 * @param project
	 * @param query
	 * @param program
	 * @param cancel
	 * @param uniprotStatus 
	 * @param taxonomyID 
	 * @throws Exception
	 */
	public HomologyDataClient(String query, String program, AtomicBoolean cancel, boolean uniprotStatus, HomologySearchServer homologySearchServer, long taxonomyID) throws Exception {
		
		this.setTaxonomyID(taxonomyID);
		this.setCancel(cancel);
		this.setQuery(query);
		this.setProgram(program);
		if(homologySearchServer.equals(HomologySearchServer.EBI))
			this.setProgram("ebi-blastp");
		this.setNoSimilarity(true);
		this.homologuesData = new HomologuesData();

		try {

			String locusTag=null;

			this.processQueryInformation(query);
			if(homologySearchServer.equals(HomologySearchServer.NCBI))
				locusTag = NcbiAPI.getLocusTag(this.homologuesData.getRefSeqGI());

			if(locusTag==null && this.homologuesData.getUniprotLocusTag()!=null)				
				locusTag = this.homologuesData.getUniprotLocusTag();

			if(locusTag==null)
				locusTag=query;

			this.homologuesData.setLocusTag(locusTag);
		}
		catch(Exception e) {

			e.printStackTrace();
		}
	}


	/**
	 * @param blastList
	 * @param project
	 * @param organismTaxa
	 * @param taxonomyMap
	 * @param uniprotStar
	 * @param cancel
	 * @param blastServer 
	 * @param hitListSize 
	 * @param uniprotStatus 
	 * @param taxonomyID 
	 * @throws Exception
	 */
	public HomologyDataClient(ReadBlasttoList blastList, String[] organismTaxa, ConcurrentHashMap<String, String[]> taxonomyMap, 
			ConcurrentHashMap<String, Boolean> uniprotStar, AtomicBoolean cancel, HomologySearchServer homologySearchServer, int hitListSize, boolean uniprotStatus, long taxonomyID
			) throws Exception {

		this.setTaxonomyID(taxonomyID);
		this.setNumberOfHits(hitListSize);
		this.setNoSimilarity(false);
		this.setCancel(cancel);

		this.homologuesData = new HomologuesData();
		this.homologuesData.setTaxonomyMap(taxonomyMap);
		this.homologuesData.setUniprotStatus(uniprotStar);
		this.homologuesData.setOrganismTaxa(organismTaxa);
		this.homologuesData.setSequenceCode(blastList.getQuery());
		this.homologuesData.setQuery(blastList.getQuery());

		this.processQueryInformation(blastList.getQuery());

		List<Pair<String, String>> resultsList = this.initialiseClass(blastList.getQuery(),blastList.getDatabaseId(), blastList.getVersion(), blastList.getProgram(), this.parseResults(blastList.getResults(), homologySearchServer), homologySearchServer);
		
		if(homologySearchServer.equals(HomologySearchServer.NCBI))
			this.homologuesData = NcbiAPI.getNcbiData(this.homologuesData, resultsList, 1, 
					this.cancel, uniprotStatus, String.valueOf(this.taxonomyID));
		
		if(homologySearchServer.equals(HomologySearchServer.EBI))
			this.homologuesData = UniProtAPI.getUniprotData(homologuesData, resultsList, cancel, uniprotStatus, 
					String.valueOf(this.taxonomyID));
	}

	/**
	 * @param hmmerList
	 * @param organismTaxa
	 * @param taxonomyMap
	 * @param uniprotStar
	 * @param cancel
	 * @param uniprotStatus
	 * @param homologySearchServer
	 * @param taxonomyID 
	 * @throws Exception
	 */
	public HomologyDataClient(ReadHmmertoList hmmerList, String[] organismTaxa, ConcurrentHashMap<String, String[]> taxonomyMap,
			ConcurrentHashMap<String, Boolean> uniprotStar, AtomicBoolean cancel, boolean uniprotStatus
			, HomologySearchServer homologySearchServer, long taxonomyID) throws Exception {

		this.setTaxonomyID(taxonomyID);
		this.setCancel(cancel);
		this.setNoSimilarity(false);	

		this.homologuesData = new HomologuesData();
		this.homologuesData.setTaxonomyMap(taxonomyMap);
		this.homologuesData.setUniprotStatus(uniprotStar);
		this.homologuesData.setBits(hmmerList.getScores());
		this.homologuesData.setEValue(hmmerList.getEValues());
		this.homologuesData.setOrganismTaxa(organismTaxa);
		this.homologuesData.setSequenceCode(hmmerList.getQuery());
		this.homologuesData.setQuery(hmmerList.getQuery());

		this.processQueryInformation(hmmerList.getQuery());

		List<Pair<String, String>> resultsList = this.initialiseClass(hmmerList.getQuery(), hmmerList.getDatabaseId().toString(), hmmerList.getVersion(), hmmerList.getProgram(), hmmerList.getResults(), homologySearchServer);		

		if(hmmerList.getDatabase().equals(HmmerRemoteDatabasesEnum.pdb) 
//				|| hmmerList.getDatabase().equals(HmmerRemoteDatabasesEnum.ensembl) ||
//				hmmerList.getDatabase().equals(HmmerRemoteDatabasesEnum.ensemblgenomes)
				)
			this.homologuesData = NcbiAPI.getNcbiData(homologuesData, resultsList, 1, 
					this.cancel, uniprotStatus, String.valueOf(this.taxonomyID));
		else
			this.homologuesData = UniProtAPI.getUniprotData(this.homologuesData, resultsList, cancel, true,  
					String.valueOf(this.taxonomyID));
	}

	/**
	 * 
	 * @param blastList
	 * @param organismTaxa
	 * @param taxonomyMap
	 * @param uniprotStar
	 * @param cancel
	 * @param uniprotStatus
	 * @param homologySearchServer
	 * @param blastSource
	 * @param taxonomyID
	 * @throws Exception
	 */
	public HomologyDataClient(ReadBlasttoList blastList, String[] organismTaxa, ConcurrentHashMap<String, String[]> taxonomyMap,
			ConcurrentHashMap<String, Boolean> uniprotStar, AtomicBoolean cancel, boolean uniprotStatus
			, HomologySearchServer homologySearchServer, BlastSource blastSource, long taxonomyID) throws Exception {

		this.setTaxonomyID(taxonomyID);
		this.setCancel(cancel);
		this.setNoSimilarity(false);	

		this.homologuesData = new HomologuesData();
		this.homologuesData.setTaxonomyMap(taxonomyMap);
		this.homologuesData.setUniprotStatus(uniprotStar);
		this.homologuesData.setOrganismTaxa(organismTaxa);
		this.homologuesData.setSequenceCode(blastList.getQuery());
		this.homologuesData.setQuery(blastList.getQuery());

		this.processQueryInformation(blastList.getQuery());

		List<Pair<String, String>> resultsList = this.initialiseClass(blastList.getQuery(),blastList.getDatabaseId(), blastList.getVersion(), blastList.getProgram(), 
				this.parseResults(blastList.getResults(), homologySearchServer), homologySearchServer);

		if(blastSource.equals(BlastSource.NCBI))
			this.homologuesData = NcbiAPI.getNcbiData(this.homologuesData, resultsList, 1, this.cancel, uniprotStatus, String.valueOf(this.taxonomyID));
		else
			this.homologuesData = UniProtAPI.getUniprotData(this.homologuesData, resultsList, cancel, true, String.valueOf(this.taxonomyID));
	}

	/**
	 * @param query
	 * @throws Exception
	 */
	private void processQueryInformation(String query) throws Exception {

		EntryData entryData = UniProtAPI.getEntryData(query, this.taxonomyID);

		String ecNumbers = null;
		Set<String> ecnumbers = entryData.getEcNumbers();
		if(ecnumbers!= null) {

			ecNumbers = "";

			for(String ecnumber : ecnumbers)
				ecNumbers += ecnumber+", ";

			if(ecNumbers.contains(", "))
				ecNumbers = ecNumbers.substring(0, ecNumbers.lastIndexOf(", "));
		}
		this.setUniprot_ecnumber(ecNumbers);
		this.setUniProtStarred(entryData.getUniprotReviewStatus());
		this.homologuesData.setUniProtEntryID(entryData.getEntryID());
		this.homologuesData.setUniprotLocusTag(entryData.getLocusTag());
		this.homologuesData.setRefSeqGI(NcbiAPI.getNcbiGI(query)); 
	}


	/**
	 * @param query
	 * @param databaseID
	 * @param version
	 * @param program
	 * @param rawResultsList
	 * @param homologySearchServer
	 * @return
	 * @throws Exception
	 */
	private List<Pair<String, String>> initialiseClass(String query, String databaseID, String version, String program, List<String> rawResultsList, HomologySearchServer homologySearchServer) throws Exception {

		this.setQuery(query);
		this.setDataRetrieved(true);
		this.setDatabaseID(databaseID);
		this.setVersion(version);
		this.setProgram(program);
		if(homologySearchServer.equals(HomologySearchServer.EBI))
			this.setProgram("ebi-blastp");

		List<Pair<String, String>>  resultsList = new ArrayList<>();
//		if(this.homologySearchServer.equals(HomologySearchServer.NCBI))
//			resultsList = this.getProteinDatabaseIDS(rawResultsList);
//		else
			for(int i=0;i<rawResultsList.size();i++)
				resultsList.add(i, new Pair<String, String> (rawResultsList.get(i), rawResultsList.get(i)));
			
		return resultsList;
	}

//	/**
//	 * @param resultsList 
//	 * @param trialNumber
//	 * @return 
//	 * @throws Exception 
//	 */
//	private List<Pair<String, String>> getProteinDatabaseIDS(List<String> resultsList) throws Exception {
//
//		return NcbiAPI.getProteinDatabaseIDS(resultsList, 0, 100);
//	} 

	/**
	 * @param list
	 * @return 
	 */
	private List<String> parseResults(List<SeqSimilaritySearchResult> list, HomologySearchServer homologySearchServer) {

		this.homologuesData.setBits(new HashMap<String, Double>());
		this.homologuesData.setEValue(new HashMap<String, Double>());
		List<String> resultsList = new ArrayList<String>();

		for (SeqSimilaritySearchResult result : list) {

			@SuppressWarnings("unchecked")
			List<SeqSimilaritySearchHit> hits = (List<SeqSimilaritySearchHit>) result.getHits();

			for (int i = 0; i<hits.size();i++ ){

				SeqSimilaritySearchHit hit = hits.get(i);
				String id = hit.getSubjectID();

				if(homologySearchServer.equals(HomologySearchServer.EBI))
					id = this.parseUniProtIds(hit);

				if(id!=null) {

					resultsList.add(i, id);
					this.homologuesData.addBits(id, hit.getScore());
					this.homologuesData.addEValue(id, hit.getEValue());
				}
			}
		}
		return resultsList;
	}


	/**
	 * @param id
	 * @return
	 */
	private String parseUniProtIds(SeqSimilaritySearchHit hit) {

		String id = hit.getAnnotation().getProperty("subjectDescription").toString();
		String[] xrefs = id.split("\\s");
		String uni = xrefs[0];

		return uni;
	}

	/**getLocus_tag
	 * @return the identification locus
	 */
	public List<String> getLocusID(){
		return homologuesData.getLocusIDs();
	}

	/**
	 * @return the locus of all the blasted entities
	 */
	public Map<String,String> getBlastLocusTag(){
		return homologuesData.getBlastLocusTag();
	}

	/**
	 * @return the organism
	 */
	public Map<String, String> getOrganism() {
		return homologuesData.getOrganism();
	}

	/**
	 * @return taxonomy
	 */
	public Map<String, String> getTaxonomy() {
		return homologuesData.getTaxonomy();
	}

	/**
	 * @return the product
	 */
	public Map<String, String> getProduct() {
		return homologuesData.getProduct();
	}

	/**
	 * @return the molecular weight
	 */
	public Map<String, String> getCalculated_mol_wt() {
		return homologuesData.getCalculated_mol_wt();
	}

	/**
	 * @return the definition
	 */
	public Map<String, String> getDefinition() {
		return homologuesData.getDefinition();
	}

	/**
	 * @return the enzyme comission number
	 */
	public Map<String, String[]> getEcnumber() {
		return homologuesData.getEcnumber();
	}

	/**
	 * @return the eValue
	 */
	public Map<String, Double> getEValue() {
		return this.homologuesData.getEValue();
	}

	/**
	 * @return the score
	 */
	public Map<String, Double> getScore() {
		return this.homologuesData.getBits();
	}

	/**
	 * @return the gene name
	 */
	public  String getGene() {
		return homologuesData.getGene();
	}

	/**
	 * @return the genes names
	 */
	public Map<String, String> getGenes() {
		return homologuesData.getGenes();
	}

	/**
	 * @return the chromosome where the gene is coded
	 */
	public String getChromossome() {
		return homologuesData.getChromosome();
	}

	/**
	 * @return the organelle where the gene is from
	 */
	public String getOrganelle() {
		return homologuesData.getOrganelle();
	}

	/**
	 * @return the organelle where the gene is from
	 */
	public Map<String, String> getOrganelles() {
		return homologuesData.getOrganelles();
	}

	/**
	 * @return the blast'ed organism name
	 */
	public String getOrganismID(){
		return this.organismID;		
	}

	/**
	 * @return the locus_tag
	 */
	public String getLocus_tag() {

		if(homologuesData.getLocusTag()==null)
			return this.query;

		return homologuesData.getLocusTag();
	}

	/**
	 * @return the locus_note
	 */
	public String getLocus_protein_note() {
		return homologuesData.getLocus_protein_note();
	}

	/**
	 * @return the locus_gene_note
	 */
	public String getLocus_gene_note() {
		return homologuesData.getLocus_gene_note();
	}

	/**
	 * @return the readBlast instance
	 */
	public String getFastaSequence() {
		return homologuesData.getFastaSequence();
	}

	/**
	 * @param fastaSequence the fastaSequence to set
	 */
	public void setFastaSequence(String fastaSequence) {
		homologuesData.setFastaSequence(fastaSequence);
	}

	/**
	 * @param cancel
	 */
	public void setCancel(AtomicBoolean cancel) {

		this.cancel = cancel;
	}

	/**
	 * @param dataRetrieved the dataRetrieved to set
	 */
	public void setDataRetrieved(boolean dataRetrieved) {
		this.dataRetrieved = dataRetrieved;
	}

	/**
	 * @return the dataRetrieved
	 */
	public boolean isDataRetrieved() {
		return dataRetrieved;
	}

	/**
	 * @return the databaseID
	 */
	public String getDatabaseID() {
		return databaseID;
	}

	/**
	 * @param databaseID the databaseID to set
	 */
	public void setDatabaseID(String databaseID) {
		this.databaseID = databaseID;
	}

	/**
	 * @return the blastSetupID
	 */
	public String getBlastSetupID() {
		return blastSetupID;
	}

	/**
	 * @param blastSetupID the blastSetupID to set
	 */
	public void setBlastSetupID(String blastSetupID) {
		this.blastSetupID = blastSetupID;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the program
	 */
	public String getProgram() {
		return program;
	}

	/**
	 * @param program the program to set
	 */
	public void setProgram(String program) {
		this.program = program;
	}

	/**
	 * @return the query
	 */
	public String getQuery() {
		return query;
	}

	/**
	 * @param query the query to set
	 */
	public void setQuery(String query) {
		this.query = query;
	}

	public String[] getOrganismTaxa() {
		return homologuesData.getOrganismTaxa();
	}

	public void setOrganismTaxa(String[] taxID) {
		this.homologuesData.setOrganismTaxa(taxID);
	}

	/**
	 * @return the uniProtStarred
	 */
	public String isUniProtStarred() {
		return this.homologuesData.getEntryUniProtStarred();
	}

	/**
	 * @param uniProtStarred the uniProtStarred to set
	 */
	public void setUniProtStarred(String uniProtStarred) {
		this.homologuesData.setEntryUniProtStarred(uniProtStarred);
	}

	/**
	 * @return the noSimilarity
	 */
	public boolean isNoSimilarity() {
		return noSimilarity;
	}

	/**
	 * @param noSimilarity the noSimilarity to set
	 */
	public void setNoSimilarity(boolean noSimilarity) {
		this.noSimilarity = noSimilarity;
	}

	/**
	 * @return the uniprotStar
	 */
	public ConcurrentHashMap<String, Boolean> getUniprotStar() {
		return homologuesData.getUniprotStatus();
	}

	/**
	 * @param uniprotStar the uniprotStar to set
	 */
	public void setUniprotStar(ConcurrentHashMap<String, Boolean> uniprotStar) {
		this.homologuesData.setUniprotStatus(uniprotStar);;
	}

	/**
	 * @return the uniprot_ecnumber
	 */
	public String getUniprot_ecnumber() {
		return this.homologuesData.getEntryUniprotECnumbers();
	}

	/**
	 * @param uniprot_ecnumber the uniprot_ecnumber to set
	 */
	public void setUniprot_ecnumber(String uniprot_ecnumber) {
		this.homologuesData.setEntryUniprotECnumbers(uniprot_ecnumber);
	}

//	/**
//	 * @return the blastServer
//	 */
//	public HomologySearchServer getBlastServer() {
//		return homologySearchServer;
//	}


//	/**
//	 * @param blastServer the blastServer to set
//	 */
//	public void setBlastServer(HomologySearchServer blastServer) {
//		this.homologySearchServer = blastServer;
//	}

	/**
	 * @return the numberOfHits
	 */
	public int getNumberOfHits() {
		return numberOfHits;
	}

	/**
	 * @param numberOfHits the numberOfHits to set
	 */
	public void setNumberOfHits(int numberOfHits) {
		this.numberOfHits = numberOfHits;
	}

	/**
	 * @return the taxonomyID
	 */
	public long getTaxonomyID() {
		return taxonomyID;
	}

	/**
	 * @param taxonomyID the taxonomyID to set
	 */
	public void setTaxonomyID(long taxonomyID) {
		this.taxonomyID = taxonomyID;
	}

	

}

