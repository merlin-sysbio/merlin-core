package pt.uminho.ceb.biosystems.merlin.core.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;
import pt.uminho.ceb.biosystems.merlin.core.utilities.LoadFromConf;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseSchemas;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.ceb.biosystems.merlin.utilities.DatabaseFilesPaths;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

public class NewWorkspaceGUI extends javax.swing.JDialog{
	private static final long serialVersionUID = -1L;
	private JPanel jPanel1, jPanel11;
	private JTextField jTextField1;
	
	/**
	 * @param dataTable
	 * @param windowName
	 * @param name
	 * @param reaction
	 */
	public NewWorkspaceGUI() {
		
		super(Workbench.getInstance().getMainFrame());
		this.initGUI();
		Utilities.centerOnOwner(this);
		this.setVisible(true);		
		this.setAlwaysOnTop(true);
		this.toFront();
	}
	
	/**
	 * @param querydatas
	 * @param windowName
	 * @param name
	 */
	private void initGUI() {
		
	this.setModal(true);
	{
		this.setTitle("new workspace name");
		jPanel1 = new JPanel();
		getContentPane().add(jPanel1, BorderLayout.CENTER);
		GridBagLayout jPanel1Layout = new GridBagLayout();
		jPanel1Layout.columnWeights = new double[] {0.0, 0.1, 0.0};
		jPanel1Layout.columnWidths = new int[] {7, 7, 7};
		jPanel1Layout.rowWeights = new double[] {0.1, 0.0, 0.1};
		jPanel1Layout.rowHeights = new int[] {7, 7, 7};
		jPanel1.setLayout(jPanel1Layout);
	}
	{
		jPanel11 = new JPanel();
		GridBagLayout jPanel11Layout = new GridBagLayout();
		jPanel11.setLayout(jPanel11Layout);
		jPanel11Layout.columnWeights = new double[] {0.0, 0.1, 0.0};
		jPanel11Layout.columnWidths = new int[] {7, 7, 7};
		jPanel11Layout.rowWeights = new double[] {0.0, 0.1, 0.0};
		jPanel11Layout.rowHeights = new int[] {7, 7, 7};

		jPanel1.add(jPanel11, new GridBagConstraints(0, 0, 3, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
	}
	
	jTextField1 = new JTextField();
	jPanel11.add(jTextField1, new GridBagConstraints(1, 0, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

	
	JButton button1 = new JButton("create workspace");
	jPanel11.add(button1, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
	button1.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent evt){

			createWorkspace(jTextField1.getText());
			finish();
		}
	});
	
	this.setSize(250, 100);
	}

	/**
	 * 
	 */
	public void finish() {
		
		this.setVisible(false);
		this.dispose();
	}
	
	public void createWorkspace(String name) {
		
		Map<String, String> credentials = LoadFromConf.loadDatabaseCredentials(FileUtils.getConfFolderPath());
		String username = null, password = null, host = null, port = null;
		
		DatabaseType dbType = DatabaseType.H2;
		if (credentials.get("dbtype").equals("mysql"))
			dbType = DatabaseType.MYSQL;
		
		username = credentials.get("username");
		password = credentials.get("password");
		if (dbType.equals(DatabaseType.MYSQL)) {
			host = credentials.get("host");
			port = credentials.get("port");
		}
		
		long startTime = System.currentTimeMillis();
		
		DatabaseSchemas schemas = new DatabaseSchemas( username, password, host, port, dbType);
		

		boolean databaseT = false;

		if (dbType.equals(DatabaseType.MYSQL))
			databaseT = true;
		
		String[] filePath= DatabaseFilesPaths.getPathsList(databaseT);

		if(schemas.newSchemaAndScript(name, filePath)){
	    	long endTime = System.currentTimeMillis();
			Workbench.getInstance().info("workspace "+name+" successfuly created.");
		}
		else
			Workbench.getInstance().error("There was an error when trying to create the workspace!!");
	}
}
