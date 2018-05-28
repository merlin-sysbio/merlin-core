/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.operations.modelTools;

import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.BiomassMetabolite;
import pt.uminho.ceb.biosystems.merlin.Enumerators.MetaboliteGroups;
import pt.uminho.ceb.biosystems.merlin.Enumerators.ReturnType;
import pt.uminho.ceb.biosystems.merlin.EstimateBiomassContents;
import pt.uminho.ceb.biosystems.merlin.Utilities;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.utilities.Enumerators.FileExtensions;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.utilities.DatabaseLoaders;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.InformationType;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.ebiomassTemplate;
import pt.uminho.ceb.biosystems.merlin.core.utilities.LoadFromConf;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.CompartmentsAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseUtilities;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

/**
 * @author Oscar Dias
 *
 */
@Operation(name="add Biomass equation", description="add biomass components estimated from the genome sequence.")
public class EBiomass {

	private Project project;
	private boolean isProtein;
	private boolean isDNA;
	private double proteinContents;
	private double dnaContents;
	private double rnaContents;
	private double rRNA_Contents;
	private double tRNA_Contents;
	private double mRNA_Contents;
	private boolean isRNA;
	private String separator;
	private File geneExpressionFile;
	private String compartment;
	private boolean isGeneExpression;
	private ebiomassTemplate template;
	
	@Port(direction=Direction.INPUT, name="select contents template",order=1)
	public void setTemplate(ebiomassTemplate template) {

		this.template = template;
	}

	@Port(direction=Direction.INPUT, name="calculate protein contents",validateMethod="useProtein", description= "will use loaded fasta file (protein.faa file)", defaultValue="true",order=2)
	public void isProteinSequences(boolean isProtein) {

		this.isProtein = isProtein;
	}

//	@Port(direction=Direction.INPUT, name="Protein contents",description="Protein contents (gProtein.gDW-1).",validateMethod="checkProteinContents",defaultValue="0.0",order=2)
//	public void setProteinContents(double proteinContents) {
//
//		this.proteinContents = proteinContents;
//	}

	@Port(direction=Direction.INPUT, name="calculate DNA contents",validateMethod="useDna", description= "will use loaded file with whole genome (genomic.fna file)", defaultValue="true",order=3)
	public void isDNA_Contents(boolean isDNA) {

		this.isDNA = isDNA;
	}

//	@Port(direction=Direction.INPUT, name="DNA contents",description="DNA contents (gDNA.gDW-1).",validateMethod="checkDNA_Contents",defaultValue="0.0",order=4)
//	public void setDNA_Contents(double dnaContents) {
//
//		this.dnaContents = dnaContents;
//	}

//	@Port(direction=Direction.INPUT, name="DNA sequence",description="file with complete genome sequence (.fna file).",defaultValue="files path",order=4)
//	public void setDNASequences(File dnaFile) {
//			
//		this.dnaFile = dnaFile;
//	}

	@Port(direction=Direction.INPUT, name="calculate RNA contents",validateMethod="useRna", description= "will use loaded files with tRNA and rRNA sequences (.fna files)", defaultValue="true",order=5)
	public void isRNA_Contents(boolean isRNA) {

		this.isRNA = isRNA;
	}

//	@Port(direction=Direction.INPUT, name="RNA contents",description="Protein contents (gRNA.gDW-1).",validateMethod="checkRNA_Contents",defaultValue="0.0",order=6)
//	public void setRNA_Contents(double rnaContents) {
//
//		this.rnaContents = rnaContents;
//	}

//	@Port(direction=Direction.INPUT, name="mRNA contents",description="mRNA contents (g(mRNA).gRNA-1).",validateMethod="check_mRNA_Contents",defaultValue="0.0",order=7)
//	public void set_mRNA_Contents(double mRNA_Contents) {
//
//		this.mRNA_Contents = mRNA_Contents;
//	}

//	@Port(direction=Direction.INPUT, name="mRNA sequences", description="File with mRNA sequences (.fna file).", defaultValue="files path", order=8)
//	public void set_mRNA_Sequences(File mRNA_File) {
//
//		this.mRNA_File = mRNA_File;
//	}

//	@Port(direction=Direction.INPUT, name="tRNA contents", description="tRNA contents (g(tRNA).gRNA-1).",validateMethod="check_tRNA_Contents",defaultValue="0.0",order=9)
//	public void set_tRNA_Contents(double tRNA_Contents) {
//
//		this.tRNA_Contents = tRNA_Contents;
//	}

//	@Port(direction=Direction.INPUT, name="tRNA sequences", description="file with tRNA sequences (.fna file).", defaultValue="file path", order=10)
//	public void set_tRNA_Sequences(File tRNA_File) {
//
//		this.tRNA_File = tRNA_File;
//	}

//	@Port(direction=Direction.INPUT, name="rRNA contents", description="rRNA contents (g(rRNA).gRNA-1).",validateMethod="check_rRNA_Contents",defaultValue="0.0",order=11)
//	public void set_rRNA_Contents(double rRNA_Contents) {
//
//		this.rRNA_Contents = rRNA_Contents;
//	}

//	@Port(direction=Direction.INPUT, name="rRNA sequences" ,description="file with rRNA sequences (.fna file).", defaultValue="file path", order=12)
//	public void set_rRNA_Sequences(File rRNA_File) {
//
//		this.rRNA_File = rRNA_File;
//	}

	@Port(direction=Direction.INPUT, name="use Gene expression data?",defaultValue="false",order=13)
	public void isGeneExpression(boolean isGeneExpression) {

		this.isGeneExpression = isGeneExpression;
	}

	@Port(direction=Direction.INPUT, name="gene expression",description="gene expression data (optional).", defaultValue="file path", order=14)
	public void setGeneExpression(File file) {

		this.geneExpressionFile = file;
	}

	@Port(direction=Direction.INPUT, name="gene expression data separator",description="gene expression data separator character (Optional).",defaultValue=";",order=15)
	public void setGeneExpressionSeparator(String separator) {

		this.separator = separator;
	}

	/**
	 * @param project
	 */
	@Port(direction=Direction.INPUT, name="Workspace",description="Select Workspace",validateMethod="checkProject",order=16)
	
	public void setProject(Project project) {

		this.project = project;

	}

	@Port(direction=Direction.INPUT, name="biomass compartmentID",description="compartment for allocating the biomass equations.",validateMethod="checkBiomassCompartment",defaultValue="auto",order=17)
	public void setBiomassCompartment(String compartment) {

		Map<String, String> contents = LoadFromConf.loadEbiomassContents(FileUtils.getConfFolderPath(), this.template);
		
		try {
			
			this.proteinContents = Double.parseDouble(contents.get("proteinContents"));
			this.dnaContents = Double.parseDouble(contents.get("dnaContents"));
			this.rnaContents = Double.parseDouble(contents.get("rnaContents"));
			this.mRNA_Contents = Double.parseDouble(contents.get("mRNA_Contents"));
			this.rRNA_Contents = Double.parseDouble(contents.get("rRNA_Contents"));
			this.tRNA_Contents = Double.parseDouble(contents.get("tRNA_Contents"));
			
			
			if(this.isRNA && (this.mRNA_Contents + this.rRNA_Contents + this.tRNA_Contents) != 1)
				throw new IllegalArgumentException("The sum of the RNA contents should be equal to 1.");

			if((this.proteinContents + this.rnaContents+ this.dnaContents) > 1)
				throw new IllegalArgumentException("The sum of the macromolecules contents should lower than 1.");
			
			if (template.equals(ebiomassTemplate.Custom) || contents.get("proteinContents").equals("0")) {
				Workbench.getInstance().warn("set custom content values in the configuration file in merlin directory at /conf/ebiomass_contents.conf");
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			Workbench.getInstance().error("error! check contents configuration file in merlin directory at /conf/ebiomass_contents.conf");
		}

		try {

			//file paths
			String preffix = "/out";
//			String tempPath = FileUtils.getCurrentTempDirectory();
			String exportFilePath = FileUtils.getWorkspaceTaxonomyFolderPath(this.project.getDatabase().getDatabaseAccess().get_database_name(), this.project.getTaxonomyID()).concat("biomass");
			String localGenomefilePath = FileUtils.getWorkspaceTaxonomyFolderPath(this.project.getDatabase().getDatabaseAccess().get_database_name(), this.project.getTaxonomyID());
			
			//create dir
			File f = new File(exportFilePath);
			if(!f.exists())
				f.mkdir();

			//database connections
			Connection connection = new Connection(this.project.getDatabase().getDatabaseAccess());
			Statement statement = connection.createStatement();

			//get e-biomass composition
			Map<String, BiomassMetabolite> biomassMetabolites = Utilities.getBiomassMetabolites();
			biomassMetabolites = DatabaseLoaders.getModelInformationForBiomass(biomassMetabolites, statement);

			String geneExpressionPath = null;
			if(this.isGeneExpression)
				geneExpressionPath = this.geneExpressionFile.getPath();
			
			String proteinPath = null;
			if(this.isProtein)
				proteinPath = localGenomefilePath.concat(FileExtensions.PROTEIN_FAA.getExtension());
			
			String dnaPath = null;
			if(this.isDNA)
				dnaPath = localGenomefilePath.concat("genomic.fna");
			
			String rRnaPath = null, tRnaPath = null, mRnaPath = null;
			if(this.isRNA) {
				
				rRnaPath = localGenomefilePath.concat("rrna_from_genomic.fna");
				mRnaPath = localGenomefilePath.concat("cds_from_genomic.fna");
				tRnaPath = localGenomefilePath.concat("trna_from_genomic.fna");
			}
			
			Map<BiomassMetabolite, Double> averageProtein = EstimateBiomassContents.getProteinsRelativeAbundance(proteinPath, this.proteinContents, true, ReturnType.MMol_GMacromolecule, exportFilePath+preffix+"_Prot.txt", biomassMetabolites, geneExpressionPath, this.separator );
			Map<BiomassMetabolite, Double> averageDNA = EstimateBiomassContents.getNucleotides_RelativeAbundance(dnaPath, this.dnaContents, true, ReturnType.MMol_GMacromolecule, exportFilePath+preffix+"_DNA.txt", biomassMetabolites, false);
			Map<BiomassMetabolite, Double> average_rRNA = EstimateBiomassContents.getNucleotides_RelativeAbundance(rRnaPath, this.rnaContents, true, ReturnType.MMol_GMacromolecule, exportFilePath+preffix+"_rRNA.txt", biomassMetabolites, true);
			Map<BiomassMetabolite, Double> average_tRNA = EstimateBiomassContents.getNucleotides_RelativeAbundance(tRnaPath, this.rnaContents, true, ReturnType.MMol_GMacromolecule, exportFilePath+preffix+"_tRNA.txt", biomassMetabolites, true);
			Map<BiomassMetabolite, Double> average_mRNA = EstimateBiomassContents.getNucleotides_RelativeAbundance(mRnaPath, this.rnaContents, true, ReturnType.MMol_GMacromolecule, exportFilePath+preffix+"_mRNA.txt", biomassMetabolites, true);
			Map<BiomassMetabolite, Double> averageRNA = Utilities.mergeRNAMaps(average_mRNA, this.mRNA_Contents, average_tRNA, this.tRNA_Contents, average_rRNA, this.rRNA_Contents);
			
			Map<BiomassMetabolite, Double> averageCofactor = EstimateBiomassContents.getCofactoresAbundance(biomassMetabolites);

			// biomass pathway ID
			Map<String, Set<String>> pathway  = new HashMap<>();
			pathway.put(ModelAPI.addBiomassPathway(statement), new HashSet<String>());
			
			//insert data to model
			double lowerBound = 0;
			double upperBound = 999999;

			//Biomass equation
			{
				Map<BiomassMetabolite, Double> averageBiomass = new HashMap<>();

				BiomassMetabolite eProtein = Utilities.getElementFromMap("e-Protein", averageProtein); 
				if(eProtein.getModelId() == null)
					eProtein.setModelId(ModelAPI.insertCompoundToDatabase(eProtein.getName(), eProtein.getMolecularWeight(), statement));
				averageBiomass.put(eProtein, this.proteinContents);

				BiomassMetabolite eDNA = Utilities.getElementFromMap("e-DNA", averageDNA);
				if(eDNA.getModelId() == null)
					eDNA.setModelId(ModelAPI.insertCompoundToDatabase(eDNA.getName(), eDNA.getMolecularWeight(), statement));
				averageBiomass.put(eDNA, this.dnaContents);

				BiomassMetabolite eRNA = Utilities.getElementFromMap("e-RNA", averageRNA);
				if(eRNA.getModelId() == null)
					eRNA.setModelId(ModelAPI.insertCompoundToDatabase(eRNA.getName(), eRNA.getMolecularWeight(), statement));
				averageBiomass.put(eRNA, this.rnaContents);

				BiomassMetabolite cofactor = new BiomassMetabolite("C","e-Cofactor","e-Cofactor", MetaboliteGroups.OTHER.toString());
				if(cofactor.getModelId() == null)
					cofactor.setModelId(ModelAPI.insertCompoundToDatabase(cofactor.getName(), cofactor.getMolecularWeight(), statement));
				averageBiomass.put(cofactor, 0.0);
				averageCofactor.put(cofactor, -1.0);
				
				BiomassMetabolite lipid = new BiomassMetabolite("L","e-Lipid","e-Lipid", MetaboliteGroups.OTHER.toString());
				if(lipid.getModelId() == null)
					lipid.setModelId(ModelAPI.insertCompoundToDatabase(lipid.getName(), lipid.getMolecularWeight(), statement));
				averageBiomass.put(lipid, 0.0);
				
				BiomassMetabolite carbohydrate = new BiomassMetabolite("T","e-Carbohydrate","e-Carbohydrate",MetaboliteGroups.OTHER.toString());
				if(carbohydrate.getModelId() == null)
					carbohydrate.setModelId(ModelAPI.insertCompoundToDatabase(carbohydrate.getName(), carbohydrate.getMolecularWeight(), statement));
				averageBiomass.put(carbohydrate, 0.0);
				
				BiomassMetabolite eBiomass = new BiomassMetabolite("B", "e-Biomass", "e-Biomass", MetaboliteGroups.OTHER.toString());
				if(eBiomass.getModelId() == null)
					eBiomass.setModelId(ModelAPI.insertCompoundToDatabase(eBiomass.getName(), eBiomass.getMolecularWeight(), statement));
				//averageBiomass.put(biomassMetabolites.get("e-Biomass"), -1.0);
				averageBiomass.put(eBiomass, -1.0);

				String entity = "e-Biomass";
				String equation = Utilities.getReactionEquation(averageBiomass);
				
				String name =  DatabaseUtilities.databaseStrConverter(entity,connection.getDatabaseType());

				int reactionID = ProjectAPI.getReactionIdByName(name, statement);
				
				if(reactionID>0) 
					Workbench.getInstance().warn("R_"+entity+" already available in model. Skipping reaction.");
				
				else {

					Map<String, String> compartments  = new HashMap<>(), sthoichiometry = new HashMap<String, String>(), chains= new HashMap<String, String>();

					for(BiomassMetabolite bm : averageBiomass.keySet()) {

						compartments.put(bm.getModelId(), this.compartment);
						sthoichiometry.put(bm.getModelId(), (-1*averageBiomass.get(bm))+"");
						chains.put(bm.getModelId(), "0");
					}

					ModelAPI.insertNewReaction(entity, equation, false, chains, compartments, sthoichiometry, true, pathway, this.compartment,
							false, false, false, lowerBound, upperBound, InformationType.EBIOMASS.toString(), null, this.project.isCompartmentalisedModel(), this.project.getDatabaseType(), statement);
				}
			}
			
			// Cofactor
			{

				String entity = "e-Cofactor";
				String equation = Utilities.getReactionEquation(averageCofactor);
				
				String name = DatabaseUtilities.databaseStrConverter("R_"+entity,connection.getDatabaseType());
				
				int reactionID = ProjectAPI.getReactionIdByName(name, statement);
				
				if(reactionID>0)  
					Workbench.getInstance().warn("R_"+entity+" already available in model. Skipping reaction.");
				
				else {

					Map<String, String> compartments  = new HashMap<>(), sthoichiometry = new HashMap<String, String>(), chains= new HashMap<String, String>();

					for(BiomassMetabolite bm : averageCofactor.keySet()) {

						compartments.put(bm.getModelId(), this.compartment);
						sthoichiometry.put(bm.getModelId(), (-1*averageCofactor.get(bm))+"");
						chains.put(bm.getModelId(), "0");
					}

					ModelAPI.insertNewReaction(entity, equation, false, chains, compartments, sthoichiometry, true, pathway, this.compartment,
							false, false, false, lowerBound, upperBound, InformationType.EBIOMASS.toString(), null, this.project.isCompartmentalisedModel(), this.project.getDatabaseType(), statement);
				}
			}
			
			//Protein
			if(this.isProtein) {

				String entity = "e-Protein";
				String equation = Utilities.getReactionEquation(averageProtein);

				String name = DatabaseUtilities.databaseStrConverter("R_"+entity,connection.getDatabaseType());
				
				int reactionID = ProjectAPI.getReactionIdByName(name, statement);
				
				if(reactionID>0) 
					Workbench.getInstance().warn("R_"+entity+" already available in model. Skipping reaction.");
				else {

					Map<String, String> compartments  = new HashMap<>(), metabolites = new HashMap<String, String>(), chains= new HashMap<String, String>();

					for(BiomassMetabolite bm : averageProtein.keySet()) {

						compartments.put(bm.getModelId(), this.compartment);
						metabolites.put(bm.getModelId(), (-1*averageProtein.get(bm))+"");
						chains.put(bm.getModelId(), "0");
					}

					ModelAPI.insertNewReaction(entity, equation, false, chains, compartments, metabolites, true, pathway, this.compartment,
							false, false, false, lowerBound, upperBound, InformationType.EBIOMASS.toString(), null, this.project.isCompartmentalisedModel(), this.project.getDatabaseType(), statement);
				}
			}

			//DNA
			if(this.isDNA) {


				String entity = "e-DNA";
				String equation = Utilities.getReactionEquation(averageDNA);

				String name = DatabaseUtilities.databaseStrConverter("R_"+entity,connection.getDatabaseType());

				int reactionID = ProjectAPI.getReactionIdByName(name, statement);
				
				if(reactionID>0) 
					Workbench.getInstance().warn("R_"+entity+" already available in model. Skipping reaction.");
				else {

					Map<String, String> compartments  = new HashMap<>(), metabolites = new HashMap<String, String>(), chains= new HashMap<String, String>();

					for(BiomassMetabolite bm : averageDNA.keySet()) {

						compartments.put(bm.getModelId(), this.compartment);
						metabolites.put(bm.getModelId(), (-1*averageDNA.get(bm))+"");
						chains.put(bm.getModelId(), "0");
					}

					ModelAPI.insertNewReaction(entity, equation, false, chains, compartments, metabolites, true, pathway, this.compartment,
							false, false, false, lowerBound, upperBound, InformationType.EBIOMASS.toString(), null, this.project.isCompartmentalisedModel(), this.project.getDatabaseType(), statement);
				}
			}

			//RNA
			if(this.isRNA) {


				String entity = "e-RNA";
				String equation = Utilities.getReactionEquation(averageRNA);

				String name = DatabaseUtilities.databaseStrConverter("R_"+entity,connection.getDatabaseType());
				
				int reactionID = ProjectAPI.getReactionIdByName(name, statement);
				
				if(reactionID>0) 
					Workbench.getInstance().warn("R_"+entity+" already available in model. Skipping reaction.");
				else {

					Map<String, String> compartments  = new HashMap<>(), metabolites = new HashMap<String, String>(), chains= new HashMap<String, String>();

					for(BiomassMetabolite bm : averageRNA.keySet()) {

						compartments.put(bm.getModelId(), this.compartment);
						metabolites.put(bm.getModelId(), (-1*averageRNA.get(bm))+"");
						chains.put(bm.getModelId(), "0");
					}

					ModelAPI.insertNewReaction("e-RNA", equation, false, chains, compartments, metabolites, true, pathway, this.compartment,
							false, false, false, lowerBound, upperBound, InformationType.EBIOMASS.toString(), null, this.project.isCompartmentalisedModel(), this.project.getDatabaseType(), statement);
				}
			}
			statement.close();
			connection.closeConnection();
			
			MerlinUtils.updateAllViews(project.getName());
			Workbench.getInstance().info("e-Biomass equations added to the model!");
		} 
		catch (Exception e) {

			Workbench.getInstance().error("Error "+e.getMessage()+" has occured.");
			e.printStackTrace();
		}
	}

	/**
	 * @param project
	 */
	public void checkProject(Project project) {

		this.project = project;
		
		if(project == null) {

			throw new IllegalArgumentException("No Project Selected!");
		}
		else {
			String dbName = project.getDatabase().getDatabaseName();
			Long taxID = project.getTaxonomyID();
			String genomeFilesPath = FileUtils.getWorkspaceTaxonomyFolderPath(dbName, taxID);
			
			if(!this.project.isMetabolicDataAvailable())
				throw new IllegalArgumentException("Please load metabolic data before adding the e-Biomass equations.");

			if(this.isProtein && !((new File(genomeFilesPath.concat(FileExtensions.PROTEIN_FAA.getExtension()))).exists()))
				throw new IllegalArgumentException("Please add 'faa' files to the project for calculating protein contents.");
			
			File tRna = new File(genomeFilesPath.concat("trna_from_genomic.fna"));
			File rRna = new File(genomeFilesPath.concat("rrna_from_genomic.fna"));

			if(this.isRNA && (!tRna.exists() || !rRna.exists()))
				throw new IllegalArgumentException("Please add 'tRNA' and 'rRNA' 'fna' files to the project, for calculating tRNA and rRNA contents.");
			
			if(this.isRNA && !Project.isFnaFiles(dbName,taxID))
				throw new IllegalArgumentException("Please add 'fna' files to the project, for calculating mRNA contents.");
			
		}
	}

	/**
	 * @param contents
	 */
	public void checkProteinContents(double contents) {

		if(contents<0 || contents>1)
			throw new IllegalArgumentException("The contents should be higher than 0 and lower than 1!");
		this.proteinContents = contents;
	}
	public void checkDNA_Contents(double contents) {

		if(contents<0 || contents>1)
			throw new IllegalArgumentException("The contents should be higher than 0 and lower than 1!");
		this.dnaContents = contents;
	}
	public void checkRNA_Contents(double contents) {

		if(contents<0 || contents>1)
			throw new IllegalArgumentException("The contents should be higher than 0 and lower than 1!");
		this.rnaContents = contents;
	}
	public void check_mRNA_Contents(double contents) {

		if(contents<0 || contents>1)
			throw new IllegalArgumentException("The contents should be higher than 0 and lower than 1!");
		this.mRNA_Contents = contents;
	}
	public void check_tRNA_Contents(double contents) {

		if(contents<0 || contents>1)
			throw new IllegalArgumentException("The contents should be higher than 0 and lower than 1!");
		this.tRNA_Contents = contents;
	}
	public void check_rRNA_Contents(double contents) {

		if(contents<0 || contents>1)
			throw new IllegalArgumentException("The contents should be higher than 0 and lower than 1!");
		this.rRNA_Contents = contents;
	}

	/**
	 * @param compartmentID
	 */
	public void checkBiomassCompartment(String compartment) {

		String aux = "";
		if(!compartment.equalsIgnoreCase("auto"))
			aux = " WHERE name='"+compartment+"'";
		
		Connection connection;

		try {

			connection = new Connection(this.project.getDatabase().getDatabaseAccess());

			Statement stmt = connection.createStatement();
			
			ArrayList<String[]> result = CompartmentsAPI.getCompartmentDataByName(aux, stmt);

			for(int i=0; i<result.size(); i++){
				String[] list = result.get(i);

				if(compartment.equalsIgnoreCase("auto")) {

					if(((list[1].equalsIgnoreCase("cytoplasmic") || (list[1].equalsIgnoreCase("cytosol"))) & project.isCompartmentalisedModel()) || 
							(list[1].equalsIgnoreCase("inside") && !project.isCompartmentalisedModel())) {

						this.compartment = list[1];
					}
				}
				else {

					this.compartment = list[1];
				}
			}
			stmt.close();

			if(this.compartment==null) {

				Workbench.getInstance().warn("No external compartmentID defined!");
			}
		} 
		catch (SQLException e) {
			
			throw new IllegalArgumentException("An error occured. "+e);
		}
	}

	/**
	 * Validation parameter for assigning the isDna use to the class field.
	 * 
	 * @param isDNA
	 */
	public void useDna(boolean isDNA) {
		
		this.isDNA = isDNA;
		
	}
	
	/**
	 * Validation parameter for assigning the idProtein use to the class field.
	 * 
	 * @param isDNA
	 */
	public void useProtein(boolean isProtein) {
		
		this.isProtein = isProtein;
		
	}
	
	/**
	 * Validation parameter for assigning the isRNA use to the class field.
	 * 
	 * @param isRNA
	 */
	public void useRna(boolean isRna) {
		
		this.isRNA = isRna;
		
	}
	
}
