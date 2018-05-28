package pt.uminho.ceb.biosystems.merlin.core.operations.loaders;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.biojava.bio.BioException;
import org.biojava.bio.seq.Feature;
import org.biojava.bio.seq.FeatureFilter;
import org.biojava.bio.seq.FeatureHolder;
import org.biojavax.Note;
import org.biojavax.RichAnnotation;
import org.biojavax.RichObjectFactory;
import org.biojavax.bio.seq.RichFeature;
import org.biojavax.bio.seq.RichSequence;
import org.biojavax.ontology.ComparableTerm;

import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.Enumerators.GenBankFiles;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.utilities.DatabaseLoaders;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.InformationType;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.TransportersAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

/**
 * @author Antonio Dias
 *
 */
@Operation(name="Load GenBbank",description="Load GenBank information")
public class LoadGenBank {
	
	private Project project;
	private GenBankFiles genBankFile;
	private String filePath;
	private ArrayList<String[]> genes;
	private TimeLeftProgress progress = new TimeLeftProgress();
	private long startTime;
	private AtomicInteger datum;
	private AtomicBoolean cancel;
	private ArrayList<String[]> proteins;
	
	
	/**
	 * @param project
	 * @throws Exception
	 */
	@Port(direction=Direction.INPUT, name="Workspace", validateMethod="checkProject", description = "Select Workspace", order=1)
	public void setProject(Project project) throws Exception{
	}

	
	/**
	 * @param project
	 */
	public void checkProject(Project project){
		this.project = project;
	}

	
	/**
	 * @param file
	 * @throws Exception 
	 */
	@Port(direction=Direction.INPUT, name="GenBank File", validateMethod="checkFiles", description = "Select File", order=2)
	public void setGenBankFile(GenBankFiles genBankFile) throws Exception{
		
		Connection connection;
		connection = new Connection(this.project.getDatabase().getDatabaseAccess());
		
//		if(this.genBankFile.equals(GenBankFiles.GENOMIC_GBFF))
//			this.filePath = FileUtils.getWorkspaceTaxonomyFolderPath(this.project.getDatabase().getDatabaseName(), this.project.getTaxonomyID()).concat(genBankFile.extension());
//		
//		if(this.genBankFile.equals(GenBankFiles.PROTEIN_GPFF))
//			this.filePath = FileUtils.getWorkspaceTaxonomyFolderPath(this.project.getDatabase().getDatabaseName(), this.project.getTaxonomyID()).concat(genBankFile.extension());
//		
//		if(this.genBankFile.equals(GenBankFiles.CUSTOM_FILE)) {
//			
//			if(new File(FileUtils.getWorkspaceTaxonomyFolderPath(this.project.getDatabase().getDatabaseName(), this.project.getTaxonomyID()).concat(genBankFile.extension()).concat(".gbff")).exists())
//				this.filePath = FileUtils.getWorkspaceTaxonomyFolderPath(this.project.getDatabase().getDatabaseName(), this.project.getTaxonomyID()).concat(genBankFile.extension()).concat(".gbff");
//			
//			if(new File(FileUtils.getWorkspaceTaxonomyFolderPath(this.project.getDatabase().getDatabaseName(), this.project.getTaxonomyID()).concat(genBankFile.extension()).concat(".gpff")).exists())
//				this.filePath = FileUtils.getWorkspaceTaxonomyFolderPath(this.project.getDatabase().getDatabaseName(), this.project.getTaxonomyID()).concat(genBankFile.extension()).concat(".gbff");
//			
//			else
//				Workbench.getInstance().warn("The CUSTOM GenBank file doesn't exists. Please import one!");
//		}
		
		if(TransportersAPI.checkReactionData(connection)) {
			this.loadGene();
			
			this.startTime = System.currentTimeMillis();
			this.datum = new AtomicInteger(0);
			this.saveGeneToDB(genes, connection);
		}
		else{
			Workbench.getInstance().warn("Please load metabolic data!");
		}
	}
	
	/**
	 * @param file
	 */
	public void checkFiles(GenBankFiles genBankFile){
		
		this.genBankFile = genBankFile;
		this.filePath = null;
		String path = null;
		
		String dbName = this.project.getDatabase().getDatabaseName();
		Long taxonomyID = this.project.getTaxonomyID();
		
		if(this.genBankFile.equals(GenBankFiles.GENOMIC_GBFF)){
			
			path = FileUtils.getWorkspaceTaxonomyFolderPath(dbName, taxonomyID).concat(genBankFile.extension());
			if(FileUtils.existsPath(path))
				this.filePath = path;
		}
		
//		if(this.genBankFile.equals(GenBankFiles.PROTEIN_GPFF)){
//			
//			path = FileUtils.getWorkspaceTaxonomyFolderPath(dbName, taxonomyID).concat(genBankFile.extension());
//			if(FileUtils.existsPath(path))
//				this.filePath = path;
//		}
		
		if(this.genBankFile.equals(GenBankFiles.CUSTOM_FILE)) {
			
			path = FileUtils.getWorkspaceTaxonomyFolderPath(dbName, taxonomyID).concat(genBankFile.extension()).concat(".gbff");
			if(FileUtils.existsPath(path))
				this.filePath = path;
			
			path = FileUtils.getWorkspaceTaxonomyFolderPath(dbName, taxonomyID).concat(genBankFile.extension()).concat(".gpff");
			if(FileUtils.existsPath(path))
				this.filePath = path;
		}
		
		if(this.filePath.equals(null))
			throw new IllegalArgumentException("The " + genBankFile + " file doesn't exists. Please import one!");
	}
	
	
	/**
	 * @throws IOException
	 * @throws BioException 
	 * @throws NoSuchElementException 
	 */
	public void loadGene() throws IOException, NoSuchElementException, BioException	{
		
		genes = new ArrayList<String[]>();
		
		RichSequence richSeq = RichSequence.IOTools.readGenbankDNA(new BufferedReader(new FileReader(this.filePath)),null).nextRichSequence();
				
		FeatureFilter ff = new FeatureFilter.ByType("CDS");
		FeatureHolder fh = richSeq.filter(ff);
		
		//temp file locus_tag - ec number
		String databaseName = this.project.getDatabase().getDatabaseName();
		Long taxID = this.project.getTaxonomyID();
		String path = FileUtils.getWorkspaceTaxonomyFolderPath(databaseName, taxID);
		PrintWriter writer = new PrintWriter(path+richSeq.getAccession()+"_gene-ec.txt", "UTF-8");
	    writer.println("gene - ec number");
	    writer.println("\n");
		
		for (Iterator<Feature> i = fh.features(); i.hasNext();){
			
			String[] gene = new String[7];
			
			RichFeature rf = (RichFeature)i.next();
			RichAnnotation ra = (RichAnnotation)rf.getAnnotation();
			
			String[] location = rf.getLocation().toString().split("\\.\\.");
			gene[5] = location[0];
			gene[6] = location[1];
			
		    //Create the required additional ComparableTerms
		    ComparableTerm locusTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm("locus_tag");
		    ComparableTerm geneTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm("gene");
		    ComparableTerm ecTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm("EC_number");
		    ComparableTerm proteinIDTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm("protein_id");
		    ComparableTerm proteinNameTerm = RichObjectFactory.getDefaultOntology().getOrCreateTerm("product");
		    
		    //Iterate through the notes in the annotation
		    for (Iterator<Note> it = ra.getNoteSet().iterator(); it.hasNext();){
		    	
		    	Note note = (Note) it.next();
		    	
		    	//Check each note to see if it matches one of the required ComparableTerms
		    	if(note.getTerm().equals(locusTerm))
		    		gene[0] = note.getValue().toString(); //locusTag
		    	if(note.getTerm().equals(geneTerm))
		    		gene[1] = note.getValue().toString(); //geneName
		    	if(note.getTerm().equals(ecTerm))
		    		gene[2] = note.getValue().toString(); //ecNumber
		    	if(note.getTerm().equals(proteinIDTerm))
		    		gene[3] = note.getValue().toString(); //proteinID
		    	if(note.getTerm().equals(proteinNameTerm))
		    		gene[4] = note.getValue().toString(); //proteinName
		    }
		    genes.add(gene);
		    writer.println(gene[0]+" - "+gene[2]);
		}
	    writer.close();
	}

	/**
	 * @param entries
	 * @throws Exception
	 */
	public void saveGeneToDB(ArrayList<String[]> genes, Connection connection) throws Exception {
		
		this.cancel = new AtomicBoolean(false);
		
		Statement stmt = connection.createStatement();
		String temp = "";

		for (int i = 0; i < genes.size(); i++){
			
			if (cancel.get()) {
				
				Workbench.getInstance().warn("GenBank loading cancelled!");
				break;
			}
			
			Set<String> ecNumber = new HashSet<String>();
			if (genes.get(i)[2] != null){
				ecNumber.add(genes.get(i)[2]);
			}
			DatabaseLoaders.loadGeneAnnotation(genes.get(i)[0], genes.get(i)[3], genes.get(i)[1], temp, temp, genes.get(i)[5], genes.get(i)[6], ecNumber, genes.get(i)[4], stmt, project, InformationType.GENBANK);
			
			this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-this.startTime), this.datum.incrementAndGet(), genes.size(), "Loading Genes");
		}
		
		stmt.close();
		connection.closeConnection();
		
		if (!cancel.get())
			Workbench.getInstance().info("GenBank successfully loaded.");
		
	}
	
	/**
	 * @return
	 */
	@Progress
	public TimeLeftProgress getProgress() {
		
		return progress;
	}
	
	/**
	 * 
	 */
	@Cancel
	public void cancel() {
		
		this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-GregorianCalendar.getInstance().getTimeInMillis()),1,1);
		this.cancel.set(true);
	}

}