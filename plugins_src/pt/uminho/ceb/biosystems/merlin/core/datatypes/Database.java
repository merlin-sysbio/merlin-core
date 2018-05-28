package pt.uminho.ceb.biosystems.merlin.core.datatypes;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Observable;
import java.util.Set;
import java.util.TreeMap;

import es.uvigo.ei.aibench.core.datatypes.annotation.Clipboard;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseAccess;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;

/**
 * @author adias
 *
 */
/**
 * @author adias
 *
 */
@Datatype(structure = Structure.COMPLEX, namingMethod="getName")
public class Database extends Observable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1L;
	//private Results qr;
	
	private Tables dbt;
	private Entities entities;
	private TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy;
	private LinkedList<String> orphenComplexs;
	private DatabaseAccess databaseAccess;
	private Annotations annotations;

	/**
	 * @param databaseAccess
	 */
	public Database(DatabaseAccess databaseAccess) {
		
		this.databaseAccess = databaseAccess;
		this.ultimlyComplexComposedBy = new TreeMap<String,LinkedList<String>>();
		this.orphenComplexs = new LinkedList<String>();

		try {
			
			this.getComplex();
		}
		catch(Exception e){e.printStackTrace();}
	}

	/**
	 * @return
	 */
	public DatabaseAccess getDatabaseAccess() {

		return this.databaseAccess;
	}

//	@Clipboard(name="Results",order=2)
//	public Results getQr() {
//		return qr;
//	}
//
//	public void setQr(Results qr) {
//		this.qr = qr;
//		setChanged();
//		notifyObservers();
//	}
//
//	public void setQr2(Results qr) {
//		this.qr = qr;
//		//		setChanged();
//		//		notifyObservers();
//	}

//	@Clipboard(name="tables",order=3)
	public Tables getTables() {
		return dbt;
	}

	public void setTables(Tables dbt) {
		this.dbt = dbt;
		setChanged();
		notifyObservers();
	}

	public void setDatabaseCredentials(DatabaseAccess DatabaseAccess){
		this.databaseAccess=DatabaseAccess;
	}

//	@Clipboard(name="model",order=1)
	public Entities getEntities() {
		return entities;
	}

	public void setEntities(Entities entities) {
		this.entities = entities;
	}
	
//	@Clipboard(name="annotation",order=2)
	public Annotations getAnnotations() {
		return annotations;
	}
	
	public void setAnnotations(Annotations annotations) {

		this.annotations = annotations;		
	}
	
	/**
	 * Method to return the name of the database
	 * @return String containing the name of the database.
	 */
	public String getDatabaseName(){
//		if (this.databaseAccess.get_database_type().equals(DatabaseType.H2))
//			return this.databaseAccess.get_database_host();
//		else
			return this.databaseAccess.get_database_name();
	}

	public LinkedList<String> getOrphenComplexs() {
		return orphenComplexs;
	}

	public TreeMap<String, LinkedList<String>> getUltimlyComplexComposedBy() {
		return ultimlyComplexComposedBy;
	}

	protected void getComplex() throws SQLException
	{
		HashMap<String,String[]> complexCodingGeneData = new HashMap<String,String[]>();

		HashMap<String,LinkedList<String>> complexComposedBy = new HashMap<String,LinkedList<String>>();

		HashMap<String,LinkedList<String>> proteinGenes = new HashMap<String,LinkedList<String>>();

		Connection connection = new Connection(databaseAccess);

		Statement stmt = connection.createStatement();

		boolean go = ProjectAPI.checkTables(stmt);

		if(go)
		{
			ArrayList<String[]> result = ProjectAPI.getAllFromProteinComposition(stmt);

			for(int i=0; i<result.size(); i++)
			{
				String[] list = result.get(i);
				addToList(list[1], list[0], complexComposedBy);
			}

			result = ModelAPI.getProteinComposition(stmt);

			for(int i=0; i<result.size(); i++)
			{
				String[] list = result.get(i);
				if(!complexCodingGeneData.containsKey(list[0])) {
					
					complexCodingGeneData.put(
							list[0], 
							new String[]{list[0], list[1], list[2]}
							);
				}

				//addToList(rs.getString(1), rs.getString(4), proteinGenes);        DAVIDE -------> nao existem 4 colunas no resultado da query
				addToList(list[0], "", proteinGenes);
			}

			//        HashMap<String,LinkedList<String>> ultimlyComplexComposedBy = new HashMap<String,LinkedList<String>>();

			getRestOfComplexs(proteinGenes, complexComposedBy, ultimlyComplexComposedBy);
		}
		
		stmt.close();
	}

	public TreeMap<String,LinkedList<String>> getRestOfComplexs(HashMap<String,LinkedList<String>> proteinGenes, 
			HashMap<String,LinkedList<String>> complexComposedBy, 
			TreeMap<String,LinkedList<String>> ultimlyComplexComposedBy)
			{

		int intialUnkonen = complexComposedBy.size();

		Set<String> keys = complexComposedBy.keySet();

		LinkedList<String> found = new LinkedList<String>();

		for (Iterator<String> p_iter = keys.iterator(); p_iter.hasNext(); )
		{
			String key = (String)p_iter.next();

			LinkedList<String> subProts = complexComposedBy.get(key);

			boolean go = true;

			for(int i=0;i<subProts.size() && go;i++)
			{
				String sub = subProts.get(i);

				if(!ultimlyComplexComposedBy.containsKey(sub) && !proteinGenes.containsKey(sub)) go = false;
			}

			if(go) found.add(key);
		}

		for(int i=0;i<found.size();i++)
		{
			String key = found.get(i);

			LinkedList<String> subProts = complexComposedBy.get(key);

			complexComposedBy.remove(key);

			LinkedList<String> comps = new LinkedList<String>();

			for(int e=0;e<subProts.size();e++)
			{
				String sub = subProts.get(e);

				LinkedList<String> genesToAdd;

				if(ultimlyComplexComposedBy.containsKey(sub))
				{
					genesToAdd = ultimlyComplexComposedBy.get(sub);
				}
				else
				{
					genesToAdd = proteinGenes.get(sub);
				}

				for(int g=0;g<genesToAdd.size();g++)
				{
					comps.add(genesToAdd.get(g));
				}
			}

			ultimlyComplexComposedBy.put(key, comps);
		}

		if(complexComposedBy.isEmpty() || intialUnkonen==complexComposedBy.size())
		{

			if(intialUnkonen==complexComposedBy.size())
			{
				Set<String> kezs = complexComposedBy.keySet();

				for (Iterator<String> p_iter = kezs.iterator(); p_iter.hasNext(); )
				{
					String key = (String)p_iter.next();
					this.orphenComplexs.add(key);
				}
			}
			return ultimlyComplexComposedBy;
		}
		else
		{
			return getRestOfComplexs(proteinGenes, complexComposedBy, ultimlyComplexComposedBy);
		}

			}

	protected void addToList(String add, String key, HashMap<String,LinkedList<String>> h)
	{
		if(h.containsKey(key)) h.get(key).add(add);
		else
		{
			LinkedList<String> lis = new LinkedList<String>();
			lis.add(add);
			h.put(key, lis);
		}
	}

	protected void addToList(LinkedList<String> add, String key, HashMap<String,LinkedList<String>> h)
	{
		if(h.containsKey(key))
		{
			for(int i=0;i<add.size();i++)
			{
				h.get(key).add(add.get(i));
			}
		}
		else h.put(key, add);
	}
	
	public DatabaseType getDatabaseType()
	{
		return this.databaseAccess.get_database_type();
	}

}
