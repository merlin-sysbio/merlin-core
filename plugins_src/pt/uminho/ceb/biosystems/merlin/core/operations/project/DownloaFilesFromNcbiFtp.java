package pt.uminho.ceb.biosystems.merlin.core.operations.project;

import java.io.File;
import java.util.ArrayList;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.containers.DocumentSummary;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.Enumerators.FileExtensions;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

@Operation(name="download NCBI files", description="download files from NCBI genBank or refSeq ftp")
public class DownloaFilesFromNcbiFtp {

	private DocumentSummary docSum;
	private boolean isGenBank;
	private String workspace = null;
	private Long taxID;
	
	
	/**
	 * @param database
	 */
	@Port(direction=Direction.INPUT, name="database", order=1)
	public void setDatabase(String workspace) {
		this.workspace = workspace;
	}
	
	
	/**
	 * @param docSummary
	 */
	@Port(direction=Direction.INPUT, name="taxonomyID", order=2)
	public void setTaxonomyID(Long taxonomyID) {
		this.taxID = taxonomyID;
	}
	
	
	/**
	 * @param docSummary
	 */
	@Port(direction=Direction.INPUT, name="docSummary", order=3)
	public void setDocumentSummary(DocumentSummary docSummary) {
		this.docSum = docSummary;
	}

	
	/**
	 * @param isGenBank
	 */
	@Port(direction=Direction.INPUT, name="isGenBank", order=4)
	public void startDownload(boolean isGenBank) {
		this.isGenBank = isGenBank;

		try {
			ArrayList<String> ftpUrlInfo;
			String ftpSource = null;
			
			if(isGenBank)
				ftpSource = "genBank";
			else
				ftpSource = "refSeq";
			
			CreateGenomeFile.saveAssemblyRecordInfo(docSum, workspace);
			
			if(this.isGenBank){
				ftpUrlInfo = CreateGenomeFile.getFtpURLFromAssemblyUID(docSum, true);
				CreateGenomeFile.getFilesFromFtpURL(ftpUrlInfo, workspace);
			}
			else {
				ftpUrlInfo = CreateGenomeFile.getFtpURLFromAssemblyUID(docSum, false);
				CreateGenomeFile.getFilesFromFtpURL(ftpUrlInfo, workspace);
			}
			
			File faafastaFile = new File(FileUtils.getWorkspaceTaxonomyFolderPath(workspace, taxID) + FileExtensions.PROTEIN_FAA.getExtension());
			File fnafastaFile = new File(FileUtils.getWorkspaceTaxonomyFolderPath(workspace, taxID) + FileExtensions.CDS_FROM_GENOMIC.getExtension());
			
			if(!faafastaFile.exists() && !fnafastaFile.exists())
				Workbench.getInstance().warn(ftpSource + " ftp '.faa' and '.fna' files do not exist! please import manually.");
			else if (!fnafastaFile.exists()){
				CreateGenomeFile.createGenomeFileFromFasta(workspace, taxID, faafastaFile, FileExtensions.PROTEIN_FAA);
				Workbench.getInstance().info("only '.faa' " + ftpSource + " ftp file successfuly downloaded");
			}
			else if (!faafastaFile.exists()){
				CreateGenomeFile.createGenomeFileFromFasta(workspace, taxID, fnafastaFile, FileExtensions.CDS_FROM_GENOMIC);
				Workbench.getInstance().info("only '.fna' " + ftpSource + " ftp file successfuly downloaded");
			}
			else {
				CreateGenomeFile.createGenomeFileFromFasta(workspace, taxID, faafastaFile, FileExtensions.PROTEIN_FAA);
				CreateGenomeFile.createGenomeFileFromFasta(workspace, taxID, fnafastaFile, FileExtensions.CDS_FROM_GENOMIC);
				Workbench.getInstance().info(ftpSource + " ftp files successfuly downloaded");
			}
			
			
		}
		catch (Exception e) {

			e.printStackTrace();
		}
	}
}
