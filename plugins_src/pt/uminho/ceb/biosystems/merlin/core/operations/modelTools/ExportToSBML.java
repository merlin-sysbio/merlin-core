package pt.uminho.ceb.biosystems.merlin.core.operations.modelTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.sbml.jsbml.SBMLError;
import org.sbml.jsbml.SBMLException;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.SBMLLevelVersion;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.readers.ContainerBuilder;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.writers.SBMLLevel3Writer;
import pt.uminho.ceb.biosystems.merlin.biocomponents.io.writers.SBMLWriter;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactionsInterface;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.capsules.ReactionsCapsule;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.Container;

@Operation(description="Export the local database to SBML",name="SBML exporter")
public class ExportToSBML {

	private static final boolean addAllNotes = true;
	private String filename;
	private File directory;
	private String sbmlFileID;
	private DatabaseAccess msqlmt;
	private boolean isCompartmentalisedModel;
	private boolean generateFormulae;
	private boolean validateSBML;
	private String biomassEquationID;
	private ReactionsInterface reaction;
	private SBMLLevelVersion levelAndVersion;
	private Set<String> undefinedStoichiometry;
	private String taxonomyID;

	/**
	 * @param project
	 */
	@Port(name="Workspace",description="Select Workspace",direction=Direction.INPUT,order=1, validateMethod="checkProject")
	public void setProject(Project project) {

		this.msqlmt = project.getDatabase().getDatabaseAccess();
		this.sbmlFileID = project.getName();
		this.isCompartmentalisedModel = project.isCompartmentalisedModel();
		this.taxonomyID = Long.toString(project.getTaxonomyID());
	}
	/**
	 * @param project
	 */
	public void checkProject(Project project) {

		if(project == null) {

			throw new IllegalArgumentException("No Project Selected!");
		}
		else {

			for(Entity ent : project.getDatabase().getEntities().getEntities())
				if(ent.getName().equalsIgnoreCase("Reactions"))
					reaction = (ReactionsInterface) ent;
			
			if(this.reaction.getActiveReactions() == null) {
				
				ReactionsCapsule capsule = null;
				try {
					capsule = ModelAPI.getActiveReactions(project.getDatabase().getDatabaseAccess().openConnection().createStatement(), true, project.isCompartmentalisedModel(), project.getDatabaseType());
				} catch (SQLException e) {e.printStackTrace();}

				this.reaction.setActiveReactions(capsule.getActiveReactions());
				
//				throw new IllegalArgumentException("Reactions view unavailable!");
			}
				
		}
	}
	
	@Port(name="Generate Formulae:",description="Generate formulae file for OptFlux.",direction=Direction.INPUT,defaultValue="false",order=2)
	public void generateFormulae(boolean generateFormulae) {
		
		this.generateFormulae = generateFormulae;
	}
	
	@Port(name="Validate SBML:",description="Validate SBML files online.",direction=Direction.INPUT,defaultValue="false",order=3)
	public void validateSBML(boolean validateSBML) {
		
		this.validateSBML = validateSBML;
	}
	

	@Port(name="Biomass reaction name",description="(optional)",direction=Direction.INPUT,order=4, validateMethod="checkBiomassEquation")
	public void biomassReaction(String biomassEquation) {

	}
	
	/**
	 * @param biomassEquation
	 */
	public void checkBiomassEquation(String biomassEquation) {

		this.biomassEquationID = null;

		if(biomassEquation == null) {

			throw new IllegalArgumentException("No biomass equation!");
		}
		else {

			for(String rid : this.reaction.getActiveReactions()) {
				
				if(rid.equalsIgnoreCase(biomassEquation))
					this.biomassEquationID = rid;		
			}

			if(!biomassEquation.isEmpty() && this.biomassEquationID == null)
				throw new IllegalArgumentException("The selected project does not contain the required biomass equation!");
		}
	}
	
	@Port(name="SBML level and version",description="set SBML level and version",defaultValue="SBML Level 3 version 2",direction=Direction.INPUT,order=5)
	public void setLevelAndVersion (SBMLLevelVersion levelAndVersion) {

		this.levelAndVersion = levelAndVersion;
	}

	/**
	 * @param directory
	 */
	@Port(name="Directory:",description="Directory to place the SBML file",direction=Direction.INPUT,validateMethod="checkDirectory",order=6)
	public void selectDirectory(File directory) {

	}

	/**
	 * @param directory
	 */
	public void checkDirectory(File directory) {

		if(directory == null || directory.toString().isEmpty()) {

			throw new IllegalArgumentException("Please select a directory!");
		}
		else {

			if(directory.isDirectory())
				this.directory = directory;
			else
				this.directory = directory.getParentFile();	
		}
	}

	/**
	 * @param filename
	 */
	@Port(name="File name:",defaultValue="",description="Name of the SBML file",direction=Direction.INPUT,validateMethod="checkFilename",order=7)
	public void setFileName(String filename) {

		try {
			this.validateStoichiometry();
			if(this.undefinedStoichiometry.size()>0)
				throw new IllegalArgumentException("please check the stoichiometry on the following reactions\n" + StringUtils.join(this.undefinedStoichiometry, ','));
			
			if(this.levelAndVersion.equals(SBMLLevelVersion.L3V2) || this.levelAndVersion.equals(SBMLLevelVersion.L3V1)){
				
				Container container = new Container(new ContainerBuilder(this.msqlmt,"model_".concat(sbmlFileID),
						true,sbmlFileID,this.biomassEquationID));
				
				boolean writeObjectives = false;
				if(this.biomassEquationID != null && !this.biomassEquationID.isEmpty())
					writeObjectives = true;
				
				SBMLLevel3Writer merlinSBML3Writer = new SBMLLevel3Writer(this.directory.toString().concat("/").concat(this.filename), 
						container, this.taxonomyID, writeObjectives, null, true, this.levelAndVersion, true);
				
				merlinSBML3Writer.writeToFile();
			}
			else{
				SBMLWriter sBMLWriter = new SBMLWriter(this.msqlmt, this.directory.toString().concat("/").concat(this.filename),
						this.sbmlFileID, this.isCompartmentalisedModel, generateFormulae,
						this.biomassEquationID, this.levelAndVersion);
					
				if(validateSBML)
					if(sBMLWriter.getDocument().checkConsistency()>0) {

						Workbench.getInstance().warn(sBMLWriter.getDocument().getListOfErrors().getErrorCount() + " warnings were thrown while writing the sbml."
								+ "\n Please find them in the jsbml.log at merlin's root dir.");
						for(SBMLError sbmlError : sBMLWriter.getDocument().getListOfErrors().getValidationErrors())
							System.out.println(sbmlError.getMessage());
					}
				
				sBMLWriter.getDataFromDatabase();
				sBMLWriter.toSBML(addAllNotes);
			}
			
			Workbench.getInstance().info("SBML file generated!");
			
		}
		catch (FileNotFoundException e) {Workbench.getInstance().error("File not found."); e.printStackTrace();}
		catch (XMLStreamException e) {Workbench.getInstance().error(e.getMessage());	e.printStackTrace();}
		catch (SBMLException e) {Workbench.getInstance().error(e.getShortMessage()); 	e.printStackTrace();} 
		catch (Exception e) {Workbench.getInstance().error(e.getMessage());	e.printStackTrace();}
	}

	/**
	 * @param filename
	 */
	public void checkFilename(String filename) {

		if(filename.isEmpty()) {

			throw new IllegalArgumentException("Please enter name for the SBML file!");
		}
		else {
			
			if(filename.isEmpty())
				filename=sbmlFileID;

			if(filename.toLowerCase().endsWith(".xml"))
				this.filename= filename;
			else
				this.filename= filename.concat(".xml");			
		}
	}
	
	public void validateStoichiometry() throws SQLException {
		
		this.undefinedStoichiometry = ModelAPI.checkUndefinedStoichiometry(msqlmt.openConnection().createStatement());
	}
}
