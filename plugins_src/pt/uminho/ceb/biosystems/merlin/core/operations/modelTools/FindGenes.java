package pt.uminho.ceb.biosystems.merlin.core.operations.modelTools;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.biojava.nbio.core.sequence.template.AbstractSequence;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.Enumerators.FileExtensions;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactionsInterface;
import pt.uminho.ceb.biosystems.merlin.core.gui.FillGapReaction;
import pt.uminho.ceb.biosystems.merlin.core.utilities.LoadFromConf;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.HomologyAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.gpr.rules.core.IdentifyGenomeSubunits;
import pt.uminho.ceb.biosystems.merlin.utilities.Enumerators.Method;
import pt.uminho.ceb.biosystems.merlin.utilities.Pair;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.capsules.AlignmentCapsule;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

@Operation(name="find genes", description="Find genes for gaps")
public class FindGenes implements Observer{

	private String reactionID;
	private ReactionsInterface reactionsInterface;
	private Map<String, List<String>> ecNumbers;
//	private AtomicBoolean cancel = new AtomicBoolean();
//	private TimeLeftProgress progress = new TimeLeftProgress();
//	private long startTime;
//	private OperationProgress operation;
	

	@Port(direction=Direction.INPUT, name="reactionID", order=1)
	public void setReactionID(String reactionID){

		this.reactionID = reactionID;
	};

	@Port(direction=Direction.INPUT, name="ReactionsInterface", order=2)
	public void setReactionsInterface(ReactionsInterface reactionInterface) throws Exception{

//		this.startTime = GregorianCalendar.getInstance().getTimeInMillis();

		this.reactionsInterface = reactionInterface;

		long taxonomyID = reactionsInterface.getProject().getTaxonomyID();
		String databaseName = reactionsInterface.getProject().getDatabase().getDatabaseName();

		Map<String, String> credentials = LoadFromConf.loadReactionsThresholds(FileUtils.getConfFolderPath());

		double similarityThreshold = Double.valueOf(credentials.get("similarity_threshold"));
		Double reference_taxo_threshold = Double.valueOf(credentials.get("reference_taxo_threshold"));

		int flag = -1;
		DataTable data = null;


		Pair<Integer, DataTable> pair = FillGapReaction.openFile(databaseName, taxonomyID, this.reactionID, similarityThreshold);
		flag = pair.getA();
		data = pair.getB();


		DatabaseAccess databaseAccess = reactionsInterface.getProject().getDatabase().getDatabaseAccess();
		Connection conn = new Connection(databaseAccess);
		Statement statement = conn.createStatement();

		if(flag== -1){

			this.ecNumbers= HomologyAPI.getEcNumbersList(reactionID, statement);

			Map<String, AbstractSequence<?>> genome = CreateGenomeFile.getGenomeFromID(reactionsInterface.getProject().getDatabase().getDatabaseName(), taxonomyID, FileExtensions.PROTEIN_FAA);

			Method method = Method.SmithWaterman;
			IdentifyGenomeSubunits i = new IdentifyGenomeSubunits(ecNumbers, genome, taxonomyID, databaseAccess, similarityThreshold, 
					reference_taxo_threshold, method , true);

			//			i.setProgress(progress);

			//				boolean identifiedWithoutErros = i.runGapsIdentification(this.alignmentResults);
			boolean identifiedWithoutErros = i.runIdentification(true);

			if(identifiedWithoutErros){
				ConcurrentLinkedQueue<AlignmentCapsule> alignmentResults=i.findGapsResult();

				data = this.createTable(alignmentResults, statement);

				this.writeNewFile(databaseName, taxonomyID, reactionID, similarityThreshold, data);
			}
			else{ 
				Workbench.getInstance().warn("error");
			}

		}

		new FillGapReaction(reactionsInterface, reactionID, similarityThreshold, statement);

	};

	public DataTable createTable(ConcurrentLinkedQueue<AlignmentCapsule> alignmentResults, Statement statement){
		List<String> alignCol = new ArrayList<>();
		alignCol.add("locus tag");
		alignCol.add("sequence id");
		alignCol.add("orthologous");
		alignCol.add("alignment score");
		alignCol.add("ec number");
		alignCol.add("query coverage");
		alignCol.add("target's coverage");

		DataTable data = new GenericDataTable(alignCol, "alignment table","alignment results");

		try {
			for ( AlignmentCapsule alignmentContainer : alignmentResults) {		

				ArrayList<String> line = new ArrayList<>();

				if(alignmentContainer.getQuery() != null || alignmentContainer.getScore()!= -1.0){

					String geneID = ModelAPI.getGeneId(alignmentContainer.getTarget(), statement);
					if(geneID == null)
						line.add(alignmentContainer.getTarget());
					else
						line.add(geneID);
					line.add(alignmentContainer.getTarget());
					line.add(alignmentContainer.getQuery());
					line.add(alignmentContainer.getScore()+"");
					String ec = this.ecNumbers.keySet().toString();
					line.add(ec.replace("[", "").replace("]", "").trim());
					line.add(alignmentContainer.getCoverageQuery()+"");
					line.add(alignmentContainer.getCoverageTarget()+"");
					data.addLine(line);

				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return data;

	}

	private void writeNewFile(String databaseName, long taxonomyID, String reactionID, double similarityThreshold, DataTable data) throws IOException{

		String path = FileUtils.getWorkspaceTaxonomyFolderPath(databaseName, taxonomyID) + "FillGapReactions.txt";

		try {
			File file = new File(path);

			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file,true)));

			if(data.getRowCount() > 0){

				for (int i=0; i < data.getRowCount(); i++){

					out.write(reactionID+"\t"+similarityThreshold+"\t");

					for (int j=0; j < data.getColumnCount(); j++){
						out.write(data.getValueAt(i,j)+"\t");
					}
					out.write("\n");
				}
			}
			else{
				out.write(reactionID+"\t"+similarityThreshold+"\t"+"empty"+"\n");
			}
			out.close();

		} 
		catch (Exception e) {
			e.printStackTrace();
		}

	}

//	
//	/**
//	 * @return the operation status
//	 */
//	@Progress
//	public OperationProgress getOperationStatus() {
//
//		return this.operation;
//	}
//	
//	@Port(direction=Direction.OUTPUT)
//	public void status(){
//		
//	      this.operation.setTask("searching for ortholog");
//	      this.operation.setProgress(0.50f);
//	}
	
//	/**
//	 * @return the progress
//	 */
//	@Progress
//	public TimeLeftProgress getProgress() {
//
//		return progress;
//	}

//	/**
//	 * @param cancel the cancel to set
//	 */
//	@Cancel
//	public void setCancel() {
//
//		progress.setTime(0, 0, 0);
//		this.cancel.set(true);
//	}

	@Override
	public void update(Observable o, Object arg) {

//				progress.setTime(GregorianCalendar.getInstance().getTimeInMillis() - this.startTime, this.geneProcessingCounter.get(), this.querySize.get());
	}

}
