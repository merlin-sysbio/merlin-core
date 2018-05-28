package pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.blast;

import java.io.File;
import java.io.FileInputStream;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.biojava.nbio.core.sequence.template.AbstractSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.remote.loader.alignment.LoadSimilarityResultstoDatabase;
import pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.HomologyDataClient;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.BlastProgram;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.BlastSource;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.HomologySearchServer;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

public class BlastReportsLoader implements Runnable {

	final static Logger logger = LoggerFactory.getLogger(BlastReportsLoader.class);

	private String extension;
	private ConcurrentLinkedQueue<String> existingGenes;
	private String[] organismTaxa;
	private Project project;
	private Map<String, AbstractSequence<?>> sequences;

	private BlastProgram blastProgram;
	private BlastSource blastSource;

	private ConcurrentLinkedQueue<File> outFiles;
	private ConcurrentHashMap<String, String[]> taxonomyMap;
	private ConcurrentHashMap<String, Boolean> uniprotStar;

	private int max;
	private long startTime;
	private AtomicBoolean cancel;
	private AtomicInteger errorCounter, counter;
	private TimeLeftProgress progress;


	/**
	 * Constructor for concurrent BLAST reporst loader. 
	 * 
	 * @param extension
	 * @param existingGenes2
	 * @param organismTaxa
	 * @param project
	 * @param blastProgram
	 * @param blastSource
	 * @param outFiles
	 * @param sequences
	 * @param taxonomyMap
	 * @param uniprotStar
	 * @param max
	 * @param startTime
	 * @param cancel
	 * @param errorCounter
	 * @param counter
	 * @param progress
	 */
	public BlastReportsLoader(String extension, ConcurrentLinkedQueue<String> existingGenes, String[] organismTaxa, Project project,
			BlastProgram blastProgram, BlastSource blastSource, ConcurrentLinkedQueue<File> outFiles,
			Map<String, AbstractSequence<?>> sequences, ConcurrentHashMap<String, String[]> taxonomyMap,
			ConcurrentHashMap<String, Boolean> uniprotStar, int max, long startTime, AtomicBoolean cancel,
			AtomicInteger errorCounter, AtomicInteger counter, TimeLeftProgress progress) {
		super();
		this.extension = extension;
		this.existingGenes = existingGenes;
		this.organismTaxa = organismTaxa;
		this.project = project;
		this.blastProgram = blastProgram;
		this.blastSource = blastSource;
		this.outFiles = outFiles;
		this.sequences = sequences;
		this.taxonomyMap = taxonomyMap;
		this.uniprotStar = uniprotStar;
		this.max = max;
		this.startTime = startTime;
		this.cancel = cancel;
		this.errorCounter = errorCounter;
		this.counter = counter;
		this.progress = progress;
	}

	@Override
	public void run() {

		while(outFiles.size()>0) {

			File outFile = outFiles.poll();
			try {

				if(outFile.getName().endsWith(extension))
					this.runner(outFile);
			} 
			catch (Exception e) {

				e.printStackTrace();
			}
		}
	}

	private void runner(File outFile) throws Exception {

		CheckBlastResult cbr = new CheckBlastResult(new FileInputStream(outFile.getAbsolutePath()));
		boolean go = cbr.isBlastResultOK();
		String query = cbr.getQuery().split(" ")[0];
		HomologyDataClient homologyDataClient;

		logger.debug("Processing "+query);

		if(go) {

			if(cbr.isSimilarityFound()) {

				ReadBlasttoList blastToList = new ReadBlasttoList(cbr);

				if(!blastToList.isReprocessQuery()) {

					if(!existingGenes.contains(blastToList.getQuery())) {

						if(!this.cancel.get()) {

							HomologySearchServer hss = HomologySearchServer.EBI;
							if(blastSource.equals(BlastSource.NCBI))
								hss = HomologySearchServer.NCBI;

							homologyDataClient = new HomologyDataClient(blastToList, this.organismTaxa, this.taxonomyMap, this.uniprotStar, 
									this.cancel, false, hss, this.blastSource, project.getTaxonomyID());

							if(sequences.containsKey(query))
								homologyDataClient.setFastaSequence(sequences.get(query).getSequenceAsString());

							if(homologyDataClient.isDataRetrieved() && !existingGenes.contains(blastToList.getQuery())) {

								homologyDataClient.setDatabaseID(blastToList.getDatabaseId());

								LoadSimilarityResultstoDatabase lbr = new LoadSimilarityResultstoDatabase(homologyDataClient, this.project, this.cancel);
								lbr.loadData();

								if(lbr.isLoaded())		
									logger.debug("Gene\t{}\tprocessed. {} genes left.", homologyDataClient.getLocus_tag(), (max-counter.get()));
								else		
									logger.error("Loading error for query {}\t{}", query, homologyDataClient.getLocus_tag(), query);
							}
							else {

								if(!this.cancel.get())		
									logger.debug("Reprocessing");
							}
						}
					}

					else {

						logger.debug("Gene {} already processed.", blastToList.getQuery());
					}
				}
				else {

					errorCounter.incrementAndGet();
					logger.error("Error processing "+cbr.getQuery());
				}
			}
			else {

				if(!this.cancel.get()) {

					HomologySearchServer hss = HomologySearchServer.EBI;
					if(blastSource.equals(BlastSource.NCBI))
						hss = HomologySearchServer.NCBI;

					homologyDataClient = new HomologyDataClient(query, this.blastProgram.toString(), this.cancel, blastSource.equals(BlastSource.EBI), hss, project.getTaxonomyID());
					homologyDataClient.setFastaSequence(sequences.get(query).getSequenceAsString());
					homologyDataClient.setDatabaseID(cbr.getDatabase());

					LoadSimilarityResultstoDatabase lbr = new LoadSimilarityResultstoDatabase(homologyDataClient, this.project, this.cancel);
					lbr.loadData();
				}
			}
			cbr.getInputSource().getCharacterStream().close();
			existingGenes.add(query);
		}
		else {

			errorCounter.incrementAndGet();
		}

		this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.counter.incrementAndGet(), this.max, "Loading Genes");
	}

}
