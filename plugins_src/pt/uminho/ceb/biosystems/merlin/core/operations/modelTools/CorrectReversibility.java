/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.operations.modelTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.univocity.parsers.tsv.TsvParser;
import com.univocity.parsers.tsv.TsvParserSettings;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.reversibilitySource;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.reversibilityTemplate;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.TransportersAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseUtilities;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

/**
 * @author ODias
 *
 */
@Operation(name="correct reactions reversibility", description="correct reactions reversibility")
public class CorrectReversibility {

	private Project project;
	private boolean forceCorrect;
	private reversibilitySource source;
	private reversibilityTemplate template;
	
	@Port(name="source",description="reversibility source", direction = Direction.INPUT, order=1)
	public void setSource(reversibilitySource source){
		
		this.source = source;
	}
	
	@Port(name="template",description="organism template", direction = Direction.INPUT, order=2)
	public void setSource(reversibilityTemplate template){
		
		this.template = template;
	}

	@Port(name="force corrections",description="force reversibility corrections", validateMethod="checkForceCorrect", direction = Direction.INPUT, order=3)
	public void setForceCorrect(boolean forceCorrect){
		
		this.forceCorrect = forceCorrect;
	}
	
	@Port(name="select workspace",description="select workspace", validateMethod="checkProject", direction = Direction.INPUT, order=4)
	public void setProject(Project project){

		try {
			
			Connection connection = new Connection(this.project.getDatabase().getDatabaseAccess());
			Statement stmt = connection.createStatement();
			
			if(source.equals(reversibilitySource.Zeng)) {
			
				File file = new File(FileUtils.getUtilitiesFolderPath() + "irr_reactions.txt");
	
				BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
	
				String text;
				while ((text = bufferedReader.readLine()) != null) {
	
					String[] data = text.split("\t");
	
					String query = "SELECT * FROM reaction " +
							"WHERE name ='"+DatabaseUtilities.databaseStrConverter(data[0],connection.getDatabaseType())+"' " +
									" OR name LIKE '"+DatabaseUtilities.databaseStrConverter(data[0],connection.getDatabaseType())+"%_%' "; 
					
					Map<String, String> reactions = ProjectAPI.getDataFromReaction(query, stmt);
					
					for (String id : reactions.keySet()) {
						
						String equation = reactions.get(id).replace("<=>", "=>");
						
						boolean rever = !DatabaseUtilities.get_boolean_int_to_boolean(DatabaseUtilities.databaseStrConverter(data[1],connection.getDatabaseType()));
						
						
						if(rever)
							equation = reactions.get(id).replace(" => ", " <=> ");
						else						
							equation = reactions.get(id).replace(" <=> ", " => ");
						
						query = "UPDATE reaction SET reversible = "+rever+", equation= '"+DatabaseUtilities.databaseStrConverter(equation,connection.getDatabaseType())+"' " +//, notes='RRC' " +
								" WHERE idreaction ="+id;
						
						ProjectAPI.executeQuery(query, stmt);
					}
				}
				bufferedReader.close();
			}
			
			else {
				TsvParserSettings settings = new TsvParserSettings();
				TsvParser parser = new TsvParser(settings);
				
				URL fileAliases = new URL("https://raw.githubusercontent.com/ModelSEED/ModelSEEDDatabase/master/Biochemistry/Aliases/Reactions_Aliases.tsv");
				InputStream inAliases = fileAliases.openStream();
				List<String[]> rowsAliases = parser.parseAll(inAliases);
				
				Map<String,String> idConversion = new HashMap<String,String>();
				
				for(String[] line : rowsAliases)
					if(line[3].equals("KEGG")) {
						if(!idConversion.containsKey(line[0]))
							idConversion.put(line[0],line[2].substring(0, 6));
						if(!idConversion.containsKey(line[1]))
							idConversion.put(line[1],line[2].substring(0, 6));
						}
				
				inAliases.close();
				
				TsvParserSettings settings2 = new TsvParserSettings();
				settings2.setMaxCharsPerColumn(10000);
				TsvParser parser2 = new TsvParser(settings2);
				
				URL fileReactions = new URL("https://raw.githubusercontent.com/ModelSEED/ModelSEEDDatabase/master/Biochemistry/reactions.tsv");
				InputStream inReactions = fileReactions.openStream();
				List<String[]> rowsReactions = parser2.parseAll(inReactions);
				
				URL fileTemplate = null;
				if(template.equals(reversibilityTemplate.GramNegative))
					fileTemplate = new URL("https://raw.githubusercontent.com/ModelSEED/ModelSEEDDatabase/master/Templates/GramNegative/Reactions.tsv");
				if(template.equals(reversibilityTemplate.GramPositive))
					fileTemplate = new URL("https://raw.githubusercontent.com/ModelSEED/ModelSEEDDatabase/master/Templates/GramPositive/Reactions.tsv");
				if(template.equals(reversibilityTemplate.Microbial))
					fileTemplate = new URL("https://raw.githubusercontent.com/ModelSEED/ModelSEEDDatabase/master/Templates/Microbial/Reactions.tsv");
				if(template.equals(reversibilityTemplate.Mycobacteria))
					fileTemplate = new URL("https://raw.githubusercontent.com/ModelSEED/ModelSEEDDatabase/master/Templates/Mycobacteria/Reactions.tsv");
//				if(template.equals(reversibilityTemplate.Plant))
//					fileTemplate = new URL("https://raw.githubusercontent.com/ModelSEED/ModelSEEDDatabase/master/Templates/Plant/Reactions.tsv");
				if(template.equals(reversibilityTemplate.Human))
					fileTemplate = new URL("https://raw.githubusercontent.com/ModelSEED/ModelSEEDDatabase/master/Templates/Human/Reactions.tsv");
				
				InputStream inTemplate = fileTemplate.openStream();
				List<String[]> rowsTemplate = parser.parseAll(inTemplate);

				for(String[] line : rowsTemplate) {
					String keggID = idConversion.get(line[0]);
					if(keggID != null){
						
						String query = "SELECT * FROM reaction " +
								"WHERE name ='"+DatabaseUtilities.databaseStrConverter(keggID,connection.getDatabaseType())+"' " +
										" OR name LIKE '"+DatabaseUtilities.databaseStrConverter(keggID,connection.getDatabaseType())+"%_%' "; 
						
						Map<String, String> reactions = ProjectAPI.getDataFromReaction(query, stmt);						
						
						for (String id : reactions.keySet()) {
							String equation = reactions.get(id);
							
							boolean rever = false;
							if(line[2].equals("="))
								rever = true;
							
							if(line[2].equals("<"))
								equation = this.reverseEquation(keggID, equation, rowsReactions);
								
							if(rever)
								equation = equation.replace(" => ", " <=> ");
							else						
								equation = equation.replace(" <=> ", " => ");
							
							query = "UPDATE reaction SET reversible = "+rever+", equation= '"+DatabaseUtilities.databaseStrConverter(equation,connection.getDatabaseType())+"' " +//, notes='RRC' " +
									" WHERE idreaction ="+id;
							
							ProjectAPI.executeQuery(query, stmt);
						}
					}
				}
			}


			stmt.close();
			
			Workbench.getInstance().info("Reactions reversibility corrected!");

		} catch (Exception e) {
			
			throw new IllegalArgumentException(e);
		} 
	}
	
	public String reverseEquation(String keggID, String equation, List<String[]> rowsReactions){

		String newEquation = equation;
		
		for(String[] line : rowsReactions)
			if(line[1].equals(keggID)) {
				String[] equationSplit = equation.split(" <=> | => | <= ");
				String[] reactants = equationSplit[0].split(" \\+ ");
				String[] msEqSplit = line[7].split(" <=> | => | <= ");
				
				if(msEqSplit[0].contains(reactants[0]) || msEqSplit[0].contains(reactants[1])){
					newEquation = equationSplit[1] + " => " + equationSplit[0];
				}
				break;
			}
		
		return newEquation;
	}
		
	/**
	 * @param project
	 */
	public void checkForceCorrect(boolean forceCorrect) {
		
		this.forceCorrect = forceCorrect;
	}
	
	/**
	 * @param project
	 */
	public void checkProject(Project project) {
		
		if(project == null) {
			
			throw new IllegalArgumentException("No Project Selected!");
		}
		else {
			
			this.project = project;
			
			Connection connection;
			try {
				connection = new Connection(this.project.getDatabase().getDatabaseAccess());
				Statement stmt = connection.createStatement();
				
				boolean exists = TransportersAPI.checkReactionNotReversible(stmt);
				
				if(exists && !this.forceCorrect)
					throw new IllegalArgumentException("Model already has irriversible reactions!\nPlease check force reversibility corrections to continue.");
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			if(!this.project.isMetabolicDataAvailable())			
				throw new IllegalArgumentException("Please load KEGG Data!");
			
			
		}
	}
}
