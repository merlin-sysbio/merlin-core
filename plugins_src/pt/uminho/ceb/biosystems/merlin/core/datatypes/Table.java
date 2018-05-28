package pt.uminho.ceb.biosystems.merlin.core.datatypes;

import java.io.Serializable;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Observable;

import org.h2.jdbc.JdbcSQLException;

import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.mew.utilities.datastructures.pair.Pair;
import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;

@Datatype(structure = Structure.COMPLEX,namingMethod="getName")
public class Table extends Observable implements Serializable {

	private static final long serialVersionUID = 1L;

	private String name;
	private String[] columms;
	//private Results results;
	private Connection connection;
	//private String host, port, dbName, usr, pwd;
	
//	/**
//	 * @param name
//	 * @param columms
//	 * @param results
//	 * @param connection
//	 */
//	public Table(String name, String[] columms, Results results, Connection connection) {
//		
//		this.name = name;
//		this.columms = columms;
//		//this.results = results;
//		//this.host = mySQLMultiThread.get_database_host(); port = mySQLMultiThread.get_database_port(); dbName = mySQLMultiThread.get_database_name(); usr = mySQLMultiThread.get_database_user(); pwd=mySQLMultiThread.get_database_password();
//		this.connection=connection;
//	}
	
	/**
	 * @param name
	 * @param columms
	 * @param results
	 * @param connection
	 */
	public Table(String name, String[] columms, Connection connection) {
		
		this.name = name;
		this.columms = columms;
		this.connection=connection;
	}
	
	public String getName() {
		return name.toLowerCase();
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String[] getColumms() {
		return columms;
	}

	public void setColumms(String[] columms) {
		this.columms = columms;
	}

	public String toString() {
		return name;
	}

//	public MySQLMultiThread getMySqlCredentials() {
//	//return new MySQLMultiThread( host, port, dbName, usr, pwd);
//	if(this.mySQLMultiThread == null )
//	{
//		this.mySQLMultiThread=new MySQLMultiThread( this.host, this.port, this.dbName, this.usr, this.pwd);
//	}
//	return this.mySQLMultiThread;
//}
//}
	public void setConnection(Connection connection) {
		
		this.connection = connection;
	}
	/**
	 * @return
	 */
	public Connection getConnection() {
	return this.connection;
}

//	/**
//	 * @return
//	 */
//	public Results getResults() {
//		return results;
//	}
//
//	/**
//	 * @param results
//	 */
//	public void setResults(Results results) {
//		this.results = results;
//	}
	
	public DataTable getValues() throws SQLException {
		
		ArrayList<String> columnsNames = new ArrayList<String>();
		
		for(int i=0;i<columms.length;i++) columnsNames.add(columms[i]);

		DataTable qrt = new DataTable(columnsNames, this.name);
		
		Statement stmt = this.connection.createStatement();

		Pair<ArrayList<String[]>, Integer> pair = ProjectAPI.getAllFromTable(this.name, stmt);
        
        ArrayList<String[]> result = pair.getA();
        int ncols = pair.getB();
        
        for(int j=0; j<result.size(); j++){
        	String[] list = result.get(j);

        	ArrayList<String> ql = new ArrayList<String>();
        	for(int i=0;i<ncols;i++){
        		String in = list[i];

				if(in!=null) 
					ql.add(in);
				else ql.add("");
        	}
        	qrt.addLine(ql);
        }
        
        stmt.close();
        //this.mySQLMultiThread.closeConnection();
        return qrt;
	}
	
	public String getSize() throws SQLException {
		
//		System.out.println(this.connection.getMetaData());
//		Statement statement=null;
//	    statement = connection.createStatement();
//		ResultSet query1=statement.executeQuery( "SHOW TABLES");
//		while (query1.next())
//			System.out.println(query1.getString(1));
		
		//DatabaseAccess dsa =  new DatabaseAccess( host, port, dbName, usr, pwd);
		try {
			
			Statement stmt = this.connection.createStatement();
			
			String result = ProjectAPI.countTableEntries(this.name, stmt);
			
			stmt.close();
		//this.DatabaseAccess.closeConnection();
			return result;
		
		} catch (JdbcSQLException e) {
			

			return null;
		}
	}
}
