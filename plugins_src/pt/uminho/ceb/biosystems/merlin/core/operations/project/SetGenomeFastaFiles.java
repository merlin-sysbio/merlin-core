/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.operations.project;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.Enumerators.FileExtensions;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

/**
 * @author ODias
 *
 */
@Operation(description="Set the genome fasta files to perform similarity searches.")
public class SetGenomeFastaFiles {

	private Project project=null;
	private File faaFastaFile, fnaFastaFile, genBankFile;
	private FileExtensions fileExtension;
	private String databaseName;
	private Long taxID;
	

	@Port(direction=Direction.INPUT, name="Select Workspace", validateMethod = "checkProject", order=1)

	public void setProject(Project project) {

	}
	
	/**
	 * @param project
	 */
	public void checkProject(Project project) {

		this.project = project;
		
		if(this.project==null) {

			throw new IllegalArgumentException("Please select a workspace.");
		}
		
		else {
			this.databaseName = project.getDatabase().getDatabaseName();
			this.taxID = this.project.getTaxonomyID();
		}
	}
	
	
	@Port(direction=Direction.INPUT, validateMethod="checkFileExtension", name="file type", description="",order=2)
	public void setExtensions(FileExtensions fileExtension) {
		
	}
	
	
	/**
	 * @param fileExtension
	 */
	public void checkFileExtension(FileExtensions fileExtension){
		this.fileExtension = fileExtension;
	}
	
	
	
	@Port(direction=Direction.INPUT,validateMethod="checkFiles",name="file", description="path to file.",order=3)
	public void setFiles(File file) {
		
		try{
		
			if(faaFastaFile != null) { 
				
				CreateGenomeFile.createGenomeFileFromFasta(this.databaseName, this.taxID, faaFastaFile, this.fileExtension);
	//			if(taxonomyID > 0 )
	//				this.project.setTaxonomyID(this.taxonomyID);
				
				Workbench.getInstance().info("Project 'faa' files successfully added!");
			}
	
			if(fnaFastaFile != null) {
				
				if(fileExtension.equals(FileExtensions.RNA_FROM_GENOMIC)){
					CreateGenomeFile.divideAndBuildRNAGenomicFastaFile(this.databaseName, this.taxID, fnaFastaFile);
					File tRNA_File = new File(FileUtils.getWorkspaceTaxonomyFolderPath(this.databaseName, this.taxID) + "trna_from_genomic.fna");
					File rRNA_File = new File(FileUtils.getWorkspaceTaxonomyFolderPath(this.databaseName, this.taxID) + "rrna_from_genomic.fna");
					CreateGenomeFile.createGenomeFileFromFasta(this.databaseName, this.taxID, tRNA_File, this.fileExtension);
					CreateGenomeFile.createGenomeFileFromFasta(this.databaseName, this.taxID, rRNA_File, this.fileExtension);
				}
				else
				CreateGenomeFile.createGenomeFileFromFasta(this.databaseName, this.taxID, fnaFastaFile, this.fileExtension);
//				if(taxonomyID > 0 )
//					this.project.setTaxonomyID(this.taxonomyID);
			
				Workbench.getInstance().info("Project 'fna' files successfully added!");
			}
			
			if(genBankFile != null) {
				
				Path source, destiny = null;
				
				if(file.getAbsolutePath().endsWith(".gbff")) {
					
					source = file.toPath();
					destiny = new File(FileUtils.getWorkspaceTaxonomyFolderPath(this.databaseName, this.taxID).concat(FileExtensions.CUSTOM_GENBANK_FILE.getExtension()).concat(".gbff")).toPath();
				
					Files.copy(source, destiny, StandardCopyOption.REPLACE_EXISTING);
				}
				if(file.getAbsolutePath().endsWith(".gpff")) {
					
					source = file.toPath();
					destiny = new File(FileUtils.getWorkspaceTaxonomyFolderPath(this.databaseName, this.taxID).concat(FileExtensions.CUSTOM_GENBANK_FILE.getExtension()).concat(".gpff")).toPath();
				
					Files.copy(source, destiny, StandardCopyOption.REPLACE_EXISTING);
				}
				Workbench.getInstance().info("Project genBank files successfully added!");
			}
			
//			else 
//				Workbench.getInstance().warn("Please insert a valid file type");

			MerlinUtils.updateProjectView(project.getName());
		} 
		
		catch (Exception e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Error uploading fasta files!");
		} 
	}
		
	
	/**
	 * @param file
	 */
	public void checkFiles(File file){

		if(file == null || file.toString().isEmpty() || !file.isFile() //|| (!file.getPath().endsWith("faa") && !file.getPath().endsWith("fna"))
				) {

			throw new IllegalArgumentException("Fasta file not set!");
		}
		else {

//			if(!file.isDirectory())//
//				file = new File(file.getParent().toString());

			this.faaFastaFile = null;
			this.fnaFastaFile = null;
			this.genBankFile = null;

		//	for(File f: file.listFiles()) {
			
			if(file.getAbsolutePath().endsWith(".faa")){
				if(fileExtension.equals(FileExtensions.PROTEIN_FAA))
					faaFastaFile = file;
				else
					throw new IllegalArgumentException("File type 'PROTEIN_FAA' does not matches the inserted file");
			}
			
			if(file.getAbsolutePath().endsWith(".fna")){
				if(fileExtension.equals(FileExtensions.CDS_FROM_GENOMIC) || fileExtension.equals(FileExtensions.RNA_FROM_GENOMIC))
					fnaFastaFile = file;
				else
					throw new IllegalArgumentException("File type 'CDS_FROM_GENOMIC' or 'RNA_FROM_GENOMIC' does not matches the inserted file");
			}

			if(file.getAbsolutePath().endsWith(".gbff") || file.getAbsolutePath().endsWith(".gpff")){
				if(fileExtension.equals(FileExtensions.CUSTOM_GENBANK_FILE))
					genBankFile = file;
				else
					throw new IllegalArgumentException("File type 'CUSTOM_GENBANK_FILE' does not matches the inserted file");
			}

			if(faaFastaFile == null && fnaFastaFile == null && genBankFile == null)
				throw new IllegalArgumentException("Please Select one '.faa', '.fna', '.gpff' or '.gbff' file!");
		}
	}

}


//	/**
//	 * @param project
//	 */
//	public void checkProject(Project project) {
//
//		this.project = project;
//		
//		if(this.project==null) {
//
//			throw new IllegalArgumentException("Please select a workspace.");
//		}
//		else {
//
//			try {
//				
//				String dbName = project.getDatabase().getDatabaseName();
//				Long taxID = this.project.getTaxonomyID();
//
//				if(faaFastaFile != null) { 
//					
//					CreateGenomeFile.createGenomeFileFromFasta(dbName, taxID, faaFastaFile, this.extension);
////					if(taxonomyID > 0 )
////						this.project.setTaxonomyID(this.taxonomyID);
//					
//					Workbench.getInstance().info("Project 'faa' files successfully added!");
//				}
//
//				if(fnaFastaFile != null) { 
//
//					CreateGenomeFile.createGenomeFileFromFasta(dbName, taxID, fnaFastaFile, this.extension);
////					if(taxonomyID > 0 )
////						this.project.setTaxonomyID(this.taxonomyID);
//					
//					Workbench.getInstance().info("Project 'fna' files successfully added!");
//				}
//				
////				if(this.taxonomyID>0) {
////					
////					ProjectUtils.getProjectID(project, true);
////					String [] orgData = NcbiAPI.ncbiNewTaxID(this.taxonomyID); 
////					project.setOrganismName(orgData[0]);
////					project.setOrganismLineage(orgData[1]);
////				}
//				
//				MerlinUtils.updateProjectView(project.getName());
//			} 
//			catch (Exception e) {
//				
//				e.printStackTrace();
//				throw new IllegalArgumentException("Error uploading fasta files!");
//			} 
//		}
//
//	}
