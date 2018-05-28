<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
package pt.uminho.sysbio.merlin.core.gui;
=======
package pt.uminho.ceb.biosystems.merlin.core.gui;

>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.NumberFormat;
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
import java.util.ArrayList;
=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.SoftBevelBorder;
import javax.swing.text.NumberFormatter;

<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
import com.mysql.jdbc.exceptions.jdbc4.CommunicationsException;

=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.workbench.InputGUI;
import es.uvigo.ei.aibench.workbench.ParamsReceiver;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
import pt.uminho.ceb.biosystems.mew.utilities.io.FileUtils;
import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.NcbiAPI;
import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.containers.DocumentSummary;
import pt.uminho.sysbio.common.bioapis.externalAPI.ncbi.containers.DocumentSummarySet;
import pt.uminho.sysbio.common.bioapis.externalAPI.utilities.Enumerators.FileExtensions;
import pt.uminho.sysbio.common.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.sysbio.common.database.connector.datatypes.Connection;
import pt.uminho.sysbio.common.database.connector.datatypes.DatabaseSchemas;
import pt.uminho.sysbio.common.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.sysbio.merlin.core.datatypes.Project;
import pt.uminho.sysbio.merlin.core.datatypes.annotation.EnzymesAnnotationDataInterface;
import pt.uminho.sysbio.merlin.core.utilities.AssemblyPanel;
import pt.uminho.sysbio.merlin.core.utilities.CreateImageIcon;
import pt.uminho.sysbio.merlin.core.utilities.LoadFromConf;


=======
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.CreateGenomeFile;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.NcbiAPI;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.containers.DocumentSummary;
import pt.uminho.ceb.biosystems.merlin.bioapis.externalAPI.ncbi.containers.DocumentSummarySet;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.utilities.AssemblyPanel;
import pt.uminho.ceb.biosystems.merlin.core.utilities.CreateImageIcon;
import pt.uminho.ceb.biosystems.merlin.core.utilities.LoadFromConf;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseSchemas;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java

/**
 * @author ODias
 *
 */
public class NewProjectGui extends javax.swing.JDialog implements InputGUI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jPanel1, jPanel3=null, jPanel4=null;
	private JLabel jTaxonomyID;
	private JFormattedTextField jTextField4;
	private JComboBox<String> jComboBox1;
	private JPanel jPanel11;
	private JPanel jPanel12;
	private ParamsReceiver rec = null;
	private JButton btnNewDB;
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
	private boolean connectionNotVerified;
=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
	private boolean isImported=false;
	public static boolean toImport = false;
	private JTextArea jTextArea1, jTextArea2;


	/**
	 * New project Gui constructor
	 */
	public NewProjectGui() {

		super(Workbench.getInstance().getMainFrame());
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
		this.connectionNotVerified = true;
=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
		//TransportersUtilities.centerOnOwner(this);
		initGUI();
		updateComboBox();
		Utilities.centerOnOwner(this);
		//		this.setSize(550,500);
	}

	/**
	 * Initiate gui method
	 */
	private void initGUI() {

		this.setModal(true);
		{

			jPanel1 = new JPanel();
			getContentPane().add(jPanel1, BorderLayout.CENTER);
			GridBagLayout jPanel1Layout = new GridBagLayout();
			jPanel1Layout.columnWeights = new double[] {0.1, 0.0, 0.1};
			jPanel1Layout.columnWidths = new int[] {7, 7, 7};
			jPanel1Layout.rowWeights = new double[] {0.1, 0.0, 0.1, 0.0};
			jPanel1Layout.rowHeights = new int[] {7, 7, 20, 7, 30};
			jPanel1.setLayout(jPanel1Layout);

			jPanel11 = new JPanel();
			GridBagLayout jPanel11Layout = new GridBagLayout();
			jPanel11.setLayout(jPanel11Layout);
			jPanel11Layout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
			jPanel11Layout.columnWidths = new int[] {7, 7, 7, 7, 7};
			jPanel11Layout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0};
			jPanel11Layout.rowHeights = new int[] {7, 7, 7, 7, 7, 7, 7, 7, 7};

			jPanel1.add(jPanel11, new GridBagConstraints(0, 0, 3, 2, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			jPanel12 = new JPanel();
			GridBagLayout jPanel12Layout = new GridBagLayout();
			jPanel12.setLayout(jPanel12Layout);
			jPanel12Layout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.0};
			jPanel12Layout.columnWidths = new int[] {3, 20, 7, 50};
			jPanel12Layout.rowWeights = new double[] {0.1};
			jPanel12Layout.rowHeights = new int[] {7};
			jPanel12.setBorder(new SoftBevelBorder(SoftBevelBorder.RAISED));

			jPanel1.add(jPanel12, new GridBagConstraints(0, 3, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			jPanel4 = new JPanel();
			GridBagLayout jPanel4Layout = new GridBagLayout();
			jPanel4.setLayout(jPanel4Layout);
			jPanel1Layout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
			jPanel1Layout.columnWidths = new int[] {7, 7, 7, 7, 7};
			jPanel1Layout.rowWeights = new double[] {0.1};
			jPanel1Layout.rowHeights = new int[] {7};
			jPanel4.setBorder(BorderFactory.createTitledBorder("Assembly Record Information"));

<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java


			//			jPanel3 = new JPanel();
			//			GridBagLayout jPanel3Layout = new GridBagLayout();
			//			jPanel3.setLayout(jPanel3Layout);
			//			jPanel3Layout.columnWeights = new double[] {0.0, 0.1};
			//			jPanel3Layout.columnWidths = new int[] {7, 7};
			//			jPanel3Layout.rowWeights = new double[] {0.0, 0.1, 0.0};
			//			jPanel3Layout.rowHeights = new int[] {7, 50, 10};
			//			jPanel3.setBorder(BorderFactory.createTitledBorder("Files retriever"));
			//			
			//			jComboBox4 = new JComboBox<>();
			//			jPanel3.add(jComboBox4, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
			//			
			//			AssemblyPanel panel = new AssemblyPanel();
			//			jPanel3 = panel.constructPanel("309800");
			//			jPanel1.add(jPanel3, new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));




=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
			JButton button1 = new JButton("ok");
			button1.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")),0.1).resizeImageIcon());
			jPanel12.add(button1, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			button1.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent evt){

					boolean go=true;

					Map<String, String> credentials = LoadFromConf.loadDatabaseCredentials(FileUtils.getConfFolderPath());
					String username = null, password = null, host = null, port = null;

					DatabaseType databaseType =  DatabaseType.H2;
					if (credentials.get("dbtype").equals("mysql"))
						databaseType = DatabaseType.MYSQL;

					username = credentials.get("username");
					password = credentials.get("password");
					if (databaseType.equals(DatabaseType.MYSQL)) {
						host = credentials.get("host");
						port = credentials.get("port");
					}

					String database = null;
					if(jComboBox1.getSelectedItem()==null) {

						go=false;
						Workbench.getInstance().error("please select a workspace");
					}
					else {

						database = jComboBox1.getSelectedItem().toString();
					}

<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
					//					String name = jTextField5.getText();
					String name = buildName("");
					//					String genomeID = null;


					//					List <String> projectNames = new ArrayList<String>();

					List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);


					//					String assemblyRecord = null;
					//					if(jComboBox3.getSelectedItem()==null) {
					//
					//						go=false;
					//						Workbench.getInstance().error("please select an assembly record");
					//					}
					//					else {
					//
					//						assemblyRecord = jComboBox3.getSelectedItem().toString();
					//					}


					//					for(int i=0; i<cl.size(); i++) {
					//						ClipboardItem item = cl.get(i);
					//						projectNames.add(item.getName());
					//					}
					//
					//					if(name.isEmpty()) {
					//
					//						while(projectNames.contains(name)||name.isEmpty()) {
					//
					//							name = buildName(name);
					//						}
					//					}
					//					else {
					//						if (!name.matches("[a-zA-Z]+")) {
					//							go=false;
					//							Workbench.getInstance().error("project can be named exclusively with letters!\nplease insert another name.");
					//						}
					//
					//						for(int i=0; i<projectNames.size(); i++) {
					//
					//							String itemName = projectNames.get(i);
					//							if(name.equals(itemName)) {
					//
					//								go=false;
					//								Workbench.getInstance().error("project with the same name already exists!\nplease insert another name.");
					//							}
					//						}
					//					}

=======

					List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);

>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
					boolean exists = false;

					for (ClipboardItem item : cl) {

						String host_previous = ((Project)item.getUserData()).getDatabase().getDatabaseAccess().get_database_host();
						String databaseName_previous = ((Project)item.getUserData()).getDatabase().getDatabaseAccess().get_database_name();

						if(!exists) {

							exists = (database!= null && database.equals(databaseName_previous));

							if(exists && databaseType.equals(DatabaseType.MYSQL))
								exists = host.equals(host_previous);
						}
					}

					if(exists) {

						go=false;
						Workbench.getInstance().error("workspace connected to the same data base already exists!\nplease select another workspace.");
					}

<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java

					//					if (!isSetFastaFiles.isSelected()) {
					//						
					//						if(Project.isFaaFiles(database) || Project.isFnaFiles(database))
					//							go=true;
					//						
					//						else
					//							Workbench.getInstance().warn("fasta file not set!");
					//					}


					//					if(file == null && file.toString().isEmpty() && !file.isFile() && (!file.getPath().endsWith("faa") && !file.getPath().endsWith("fna"))) {
					//	
					//							Workbench.getInstance().warn("fasta file not set!");
					//							Workbench.getInstance().info(info);
					//						}
					String taxonomyID = jTextField4.getText();
					
					if(!isImported) {
						//						go=true;

						//						if(Project.isFaaFiles(database))
						//							faafastaFile = new File(CreateGenomeFile.tempPath+database+"/"+ FileExtensions.PROTEIN_FAA.extension());
						//
						//						if(Project.isFnaFiles(database))
						//							fnafastaFile = new File(CreateGenomeFile.tempPath+database+"/"+ FileExtensions.GENOMIC_FNA.extension());
						//					}

						//					else {

						//						if(file.getAbsolutePath().endsWith(".faa"))
						//							faafastaFile = file;
						//
						//						if(file.getAbsolutePath().endsWith(".fna"))
						//							fnafastaFile = file;
						//						
						//						if(faafastaFile == null && fnafastaFile == null) {
						//
						//							go=false;
						//							Workbench.getInstance().error("please Select one '.faa' or '.fna' file!");
						//						}
						//					else {
=======
					String taxonomyID = jTextField4.getText();
					
					if(!isImported) {
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
						
						Long taxIdLong = null;

						if(taxonomyID!= null && !taxonomyID.isEmpty() && Integer.parseInt(taxonomyID)>0) {

							taxIdLong = Long.parseLong(taxonomyID);

							try {

								if(getTaxonomyID(database) == null)
									NcbiAPI.getTaxonomyFromNCBI(taxIdLong, 0);
							} 
							catch (Exception e) {

								throw new IllegalArgumentException("error validating taxonomy identifier, please verify your input and try again.");
							}
						}
						else {

							Workbench.getInstance().error("please insert a valid taxonomy identifier!");
							go = false;
						}

						if(toImport && go) {

							boolean isGenBank = AssemblyPanel.isGenBank;
							DocumentSummary docSummary = AssemblyPanel.selectedDocSummary;
							
							try  {
								
								ParamSpec[] paramsSpec = new ParamSpec[]{
										new ParamSpec("database", String.class, database, null),
										new ParamSpec("taxonomyID", Long.class,taxIdLong,null),
										new ParamSpec("docSummary", DocumentSummary.class, docSummary, null),
										new ParamSpec("isGenBank", Boolean.class, isGenBank, null),
								};

<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
								for (@SuppressWarnings("rawtypes") OperationDefinition def : Core.getInstance().getOperations()){
									if (def.getID().equals("operations.DownloadNcbiFiles.ID")){
=======
								for (@SuppressWarnings("rawtypes") OperationDefinition def : Core.getInstance().getOperations()) {
									
									if (def.getID().equals("operations.DownloadNcbiFiles.ID")) {
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java

										Workbench.getInstance().executeOperation(def, paramsSpec);
									}
								}
							}
							
							catch (Exception e) {

								Workbench.getInstance().error("error downloading files!");
								e.printStackTrace();
							}
							
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
//							try  {
//								CreateGenomeFile.saveAssemblyRecordInfo(docSummary, database);
////								ftpUrlInfo = CreateGenomeFile.getFtpURLFromAssemblyUID(docSummary, true);
////								CreateGenomeFile.getFilesFromFtpURL(ftpUrlInfo, database);
//								faafastaFile = new File(FileUtils.getWorkspaceTaxonomyFolderPath(database, taxIdLong) + FileExtensions.PROTEIN_FAA.getExtension());
//								fnafastaFile = new File(FileUtils.getWorkspaceTaxonomyFolderPath(database, taxIdLong) + FileExtensions.CDS_FROM_GENOMIC.getExtension());
//								if(!faafastaFile.exists() && !fnafastaFile.exists())
//									Workbench.getInstance().warn(ftpSource + " ftp '.faa' and '.fna' files do not exist! please import manually.");
//								else if (!fnafastaFile.exists()){
//									CreateGenomeFile.createGenomeFileFromFasta(database, taxIdLong, faafastaFile, FileExtensions.PROTEIN_FAA);
//									Workbench.getInstance().info("only '.faa' " + ftpSource + " ftp file successfuly downloaded");
//								}
//								else if (!faafastaFile.exists()){
//									CreateGenomeFile.createGenomeFileFromFasta(database, taxIdLong, fnafastaFile, FileExtensions.CDS_FROM_GENOMIC);
//									Workbench.getInstance().info("only '.fna' " + ftpSource + " ftp file successfuly downloaded");
//								}
//								else {
//									CreateGenomeFile.createGenomeFileFromFasta(database, taxIdLong, faafastaFile, FileExtensions.PROTEIN_FAA);
//									CreateGenomeFile.createGenomeFileFromFasta(database, taxIdLong, fnafastaFile, FileExtensions.CDS_FROM_GENOMIC);
//									//genomeID=createGenomeFile.getGenomeID();
//									Workbench.getInstance().info(ftpSource + " ftp files successfuly downloaded");
//								}
//							} 
//							catch (Exception e) {
//								e.printStackTrace();
//							}

//							else {
//
//								try  {
//									CreateGenomeFile.saveAssemblyRecordInfo(AssemblyPanel.selectedDocSummary, database);
//									ftpUrlInfo = CreateGenomeFile.getFtpURLFromAssemblyUID(AssemblyPanel.selectedDocSummary, false);
//									CreateGenomeFile.getFilesFromFtpURL(ftpUrlInfo, database);
//									faafastaFile = new File(FileUtils.getWorkspaceTaxonomyFolderPath(database, taxIdLong) + FileExtensions.PROTEIN_FAA.getExtension());
//									fnafastaFile = new File(FileUtils.getWorkspaceTaxonomyFolderPath(database, taxIdLong) + FileExtensions.CDS_FROM_GENOMIC.getExtension());
//									if(!faafastaFile.exists() && !fnafastaFile.exists())
//										Workbench.getInstance().warn("refSeq ftp '.faa' and '.fna' files do not exist! please import manually.");
//									else if (!fnafastaFile.exists()){
//										CreateGenomeFile.createGenomeFileFromFasta(database, taxIdLong, faafastaFile, FileExtensions.PROTEIN_FAA);
//										Workbench.getInstance().info("only '.faa' refSeq ftp file successfuly downloaded");
//									}
//									else if (!faafastaFile.exists()){
//										CreateGenomeFile.createGenomeFileFromFasta(database, taxIdLong, fnafastaFile, FileExtensions.CDS_FROM_GENOMIC);
//										Workbench.getInstance().info("only '.fna' refSeq ftp file successfuly downloaded");
//									}
//									else {
//										CreateGenomeFile.createGenomeFileFromFasta(database, taxIdLong, faafastaFile, FileExtensions.PROTEIN_FAA);
//										CreateGenomeFile.createGenomeFileFromFasta(database, taxIdLong, fnafastaFile, FileExtensions.CDS_FROM_GENOMIC);
//										//genomeID=createGenomeFile.getGenomeID();
//										Workbench.getInstance().info("refSeq ftp files successfuly downloaded");
//									}
//								}
//								catch (Exception e) {
//
//									Workbench.getInstance().error("error downloading refSeq files!");
//									e.printStackTrace();
//								}
//							}
=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
						}
						else {

							if(go && !Project.isFaaFiles(database, taxIdLong) && !Project.isFaaFiles(database, taxIdLong))
								Workbench.getInstance().warn("fasta file not set!");
						}
					}

					if(go) {

						try {
							Connection conn = new Connection(host, port, database, username, password, databaseType);
							ProjectAPI.updateOrganismID(conn, taxonomyID);
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
=======
							
							CreateGenomeFile.createFolder(database, Long.parseLong(taxonomyID));
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
						} 
						catch (SQLException e) {
							e.printStackTrace();
						}

						rec.paramsIntroduced(
								new ParamSpec[]{
										new ParamSpec("Host",String.class,host,null),
										new ParamSpec("Port",String.class,port,null),
										new ParamSpec("User",String.class,username,null),
										new ParamSpec("Password",String.class,password,null),
										new ParamSpec("Database Type",DatabaseType.class,databaseType,null),
										new ParamSpec("Database",String.class,database,null),
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
										new ParamSpec("New project name",String.class,name,null),
										//										new ParamSpec("PID",String.class,pid,null),
										//										new ParamSpec("oldPID",Map.class,oldPID,null),
										//new ParamSpec("genomeID",String.class,genomeID,null),
										//new ParamSpec("isNCBIGenome",boolean.class,isNCBIGenome.isSelected(),null),
=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
										new ParamSpec("TaxonomyID",long.class,Long.parseLong(taxonomyID),null),
								}
								);
						
						
					}
				}
			});

			JButton button2 = new JButton("cancel");
			button2.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")),0.1).resizeImageIcon());
			jPanel12.add(button2, new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			button2.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent evt) {
					finish();
				}
			});

<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
			//			JLabel jLabel7 = new JLabel();
			//			jLabel7.setText("database type");
			//			jPanel11.add(jLabel7, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));
			//			{
			//				jComboBox2 = new JComboBox<>(DatabaseType.values());
			//				jComboBox2.setSize(232, 33);
			//				jPanel11.add(jComboBox2, new GridBagConstraints(3, 1, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
			//
			//			}

=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
			btnNewDB = new JButton();
			btnNewDB.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")),0.1).resizeImageIcon());
			btnNewDB.setToolTipText("add");
			jPanel11.add(btnNewDB, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
			btnNewDB.setSize(232, 33);
			btnNewDB.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					createNewWorkspace();
					updateComboBox();
				}
			});

<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
			//			btnRefresh = new JButton();
			//			btnRefresh.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Synchronize.png")),0.1).resizeImageIcon());
			//			btnRefresh.setToolTipText("refresh");
			//			jPanel11.add(btnRefresh, new GridBagConstraints(4, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
			//			btnRefresh.setSize(232, 33);
			//			btnRefresh.addActionListener(new ActionListener() {
			//				@Override
			//				public void actionPerformed(ActionEvent e) {
			//					updateComboBox();
			//				}
			//			});

=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
			JLabel jLabel5 = new JLabel();
			jLabel5.setText("workspace");
			jPanel11.add(jLabel5, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));
			{
				jComboBox1 = new JComboBox<>();
				jComboBox1.setSize(232, 33);
				jPanel11.add(jComboBox1, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
			}
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
			//			jComboBox1.addActionListener(new ActionListener() {
			//				
			//				@Override
			//				public void actionPerformed(ActionEvent e) {
			//					String dbName = jComboBox1.getSelectedItem().toString();
			//					
			//					if (dbName != null)
			//						jTextField4.setText(getTaxonomyID(dbName));
			//					
			//					if (Project.isFaaFiles(dbName) || Project.isFnaFiles(dbName)){
			//						jLabel1.setText("(Files already imported)");
			//						isImported = true;
			//					}
			//					
			//						
			////					if (!Project.isFaaFiles(dbName) && !Project.isFnaFiles(dbName)){
			////						jbutton3.setEnabled(true); 
			////					}
			//						
			//					
			//				}
			//			});


=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java

			JButton jbutton3 = new JButton();
			jPanel11.add(jbutton3, new GridBagConstraints(4, 6, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));
			jbutton3.setText("search");
			jbutton3.setToolTipText("search assembly records");
			jbutton3.setEnabled(true);


			jTextArea1 = new JTextArea();
			jTextArea1.setEditable(false);
			jTextArea2 = new JTextArea();
			jTextArea2.setEditable(false);
			jPanel4.add(jTextArea1, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));
			jPanel4.add(jTextArea2, new GridBagConstraints(2, 0, 2, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));


			jComboBox1.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent e) {

					if (e.getStateChange() == 1) {

						if(jPanel3 != null) {
							removePanel(jPanel3);
							toImport = false;
						}

						if(jPanel4 != null)
							removePanel(jPanel4);

						String databaseName = jComboBox1.getSelectedItem().toString();
						
						if (databaseName != null){

							String taxID = getTaxonomyID(databaseName);
							Long taxIDLong = null;
							
							if(taxID != null){
								taxIDLong = Long.parseLong(taxID);
								
								jTextField4.setText(taxID);

								if (Project.isFaaFiles(databaseName, taxIDLong) || Project.isFnaFiles(databaseName, taxIDLong)){
									
									jbutton3.setEnabled(false);
									isImported = true;
									File assemblyRecord = new File(FileUtils.getWorkspaceTaxonomyFolderPath(databaseName, taxIDLong) + "assemblyRecordInfo.txt");
									if(assemblyRecord.exists()) { 
										updateTextArea(databaseName);
										jPanel1.add(jPanel4, new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
										Workbench.getInstance().info("Files already imported");
									}
									else
										Workbench.getInstance().info("Files already manually imported");
								}
								else{
									jbutton3.setEnabled(true);
									isImported = false;
								}
							}
							
							else{
								jTextField4.setText("");
								jbutton3.setEnabled(true);
								isImported = false;
							}

						}
					}

				}
			}); 

			jbutton3.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0) {
					
					String taxID = jTextField4.getText();

					if(taxID != null && !jTextField4.getText().isEmpty() && Long.parseLong(taxID)>0) {

						if(jPanel3 != null) {
							removePanel(jPanel3);
							toImport = false;
						}

						if(jPanel4 != null)
							removePanel(jPanel4);

						String dbName = jComboBox1.getSelectedItem().toString();
						
						Long taxIDlong = Long.parseLong(taxID);

						CreateGenomeFile.createFolder(dbName, taxIDlong);

						if (Project.isFaaFiles(dbName, taxIDlong) || Project.isFnaFiles(dbName,taxIDlong)){
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
							//							Workbench.getInstance().info("Files already imported");
							//						jLabel1.setText("(Files already imported)");
							//							jbutton3.setEnabled(false);
=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
							isImported = true;
							File assemblyRecord = new File(FileUtils.getWorkspaceTaxonomyFolderPath(dbName, taxIDlong) +"assemblyRecordInfo.txt");
							if(assemblyRecord.exists()) { 
								updateTextArea(dbName);
								jPanel1.add(jPanel4, new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
								
								Workbench.getInstance().info("Files already imported");
							}
							else
								Workbench.getInstance().info("Files already manually imported");
						}
						else {
							AssemblyPanel panel = new AssemblyPanel();
							jPanel3 = panel.constructPanel(taxID);
							jPanel1.add(jPanel3, new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
							jPanel3.setBorder(BorderFactory.createTitledBorder("Download from NCBI ftp"));
=======
							jPanel3.setBorder(BorderFactory.createTitledBorder("download from NCBI ftp"));
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
							setComboBoxAssembly(taxID);
							
							if(!AssemblyPanel.jComboBox1.getSelectedItem().toString().isEmpty()){
								panel.updateTextArea(AssemblyPanel.jComboBox1.getSelectedItem().toString());
								toImport = true;
							}
						}

					}
					else 
						Workbench.getInstance().error("please insert a valid taxonomy identifier!");

				}});

<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
			//			JLabel jLabel6 = new JLabel();
			//			jLabel6.setText("Assembly Record");
			//			jPanel11.add(jLabel6, new GridBagConstraints(1, 12, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));

			//			JLabel jLabel6 = new JLabel();
			//			jLabel6.setText("new project name");
			//			jPanel11.add(jLabel6, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));

			//			JTextField jTextField5 = new JTextField();
			//			jTextField5.setSize(232, 33);
			//			jPanel11.add(jTextField5, new GridBagConstraints(3, 7, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));

			//			isSetFastaFiles = new JCheckBox();
			//			jPanel11.add(isSetFastaFiles, new GridBagConstraints(3, 12, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
			//			isSetFastaFiles.setText("set single fasta file");
			//			isSetFastaFiles.addActionListener(new ActionListener() {
			//				@Override
			//				public void actionPerformed(ActionEvent arg0) {
			//
			//					jTextField6.setEnabled(((JCheckBox)arg0.getSource()).isSelected());
			//					jTextField6.setEditable(true);
			////					jbutton.setEnabled(((JCheckBox)arg0.getSource()).isSelected());
			//				}
			//			});

			//			jbutton= new JButton();
			//			jPanel11.add(jbutton, new GridBagConstraints(1, 13, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(6, 5, 6, 5), 0, 0));
			//			jbutton.setText("browse fasta files");
			//			jbutton.setToolTipText("browse fasta files");
			//			jbutton.setEnabled(false);
			//			jbutton.addActionListener(new ActionListener()
			//			{
			//				public void actionPerformed(ActionEvent arg0) {
			//					if(jbutton.isEnabled())
			//						openFileChooser();
			//				}});
			//			jFileChooser = new JFileChooser();
			//			jFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
			//			jFileChooser.setFileFilter(new FileFilter() {
			//				@Override
			//				public String getDescription() {
			//
			//					return "fasta files";
			//				}
			//				@Override
			//				public boolean accept(File f) {return f.isDirectory() || f.getName().toLowerCase().endsWith("fna") || f.getName().toLowerCase().endsWith("faa");}
			//			});
			//			jTextField6= new JTextField();
			//			jTextField6.setEnabled(false);
			//			jTextField6.setEditable(false);
			//			jPanel11.add(jTextField6, new GridBagConstraints(3, 13, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
			//			jTextField6.setSize(232, 33);
			//			jTextField6.addMouseListener(new MouseListener() {
			//				@Override
			//				public void mouseReleased(MouseEvent arg0) {}
			//				@Override
			//				public void mousePressed(MouseEvent arg0) {}
			//				@Override
			//				public void mouseExited(MouseEvent arg0) {}
			//				@Override
			//				public void mouseEntered(MouseEvent arg0) {}
			//				@Override
			//				public void mouseClicked(MouseEvent arg0) { 
			//					if(jTextField6.isEditable())
			//						openFileChooser();}
			//			});

			//			{
			//				jLabel1 = new JLabel();
			//				jPanel11.add(jLabel1, new GridBagConstraints(3, 10, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			//				jLabel1.setText("");
			//				jLabel1.setLabelFor(jTextField4);
			//			}
=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java

			{
				NumberFormat format = NumberFormat.getInstance();
				format.setMinimumIntegerDigits(1);
				format.setMaximumIntegerDigits(15);
				format.setGroupingUsed(false);
				format.setParseIntegerOnly(true);
				NumberFormatter formatter = new NumberFormatter(format);
				formatter.setValueClass(Integer.class);
				formatter.setMinimum(0);
				formatter.setMaximum(Integer.MAX_VALUE);
				formatter.setCommitsOnValidEdit(true);

				jTextField4 = new JFormattedTextField(formatter);
				//jTextField4.setFocusLostBehavior(JFormattedTextField.PERSIST);
				jPanel11.add(jTextField4, new GridBagConstraints(3, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 5, 5), 0, 0));
				//jTextField4.setEnabled(false);
				jTextField4.setEditable(true);
				jTextField4.addKeyListener(new KeyListener() {

					@Override
					public void keyTyped(KeyEvent e) {

						jbutton3.setEnabled(true);
						if(jPanel3 != null) {
							removePanel(jPanel3);
							toImport = false;
						}

						if(jPanel4 != null)
							removePanel(jPanel4);
					}

					@Override
					public void keyPressed(KeyEvent e) {
						// TODO Auto-generated method stub
					}

					@Override
					public void keyReleased(KeyEvent e) {
						// TODO Auto-generated method stub
					}

				});

				jTaxonomyID = new JLabel();
				jPanel11.add(jTaxonomyID, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 5, 5), 0, 0));
				jTaxonomyID.setText("taxonomy ID");
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
				//jTaxonomyID.setEnabled(false);
=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
			}
		}
		this.setSize(600, 200);
		Utilities.centerOnOwner(this);
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
		//} catch (Exception e) {e.printStackTrace();}
	}

	//	/**
	//	 * @return
	//	 */
	//	private void openFileChooser(){
	//
	//		jFileChooser.setDialogTitle("select the genome coding sequences fasta file.");
	//		int returnVal = jFileChooser.showOpenDialog(new JTextArea());
	//
	//		if (returnVal == JFileChooser.APPROVE_OPTION) {
	//
	//			file = jFileChooser.getSelectedFile();
	//			jTextField6.setText(jFileChooser.getSelectedFile().getPath());
	//		}
	//	}


=======
	}

>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.InputGUI#finish()
	 */
	public void finish() {

		this.setVisible(false);
		this.dispose();
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.InputGUI#init(es.uvigo.ei.aibench.workbench.ParamsReceiver, es.uvigo.ei.aibench.core.operation.OperationDefinition)
	 */
	public void init(ParamsReceiver arg0, OperationDefinition<?> arg1) {

		this.rec = arg0;
		this.setTitle(arg1.getName());
		this.setVisible(true);
	}

	/* (non-Javadoc)
	 * @see es.uvigo.ei.aibench.workbench.InputGUI#onValidationError(java.lang.Throwable)
	 */
	public void onValidationError(Throwable arg0) {

		Workbench.getInstance().error(arg0);
	}

	/**
	 * 
	 */
	private void updateComboBox() {

		try {

			setComboBox();
			jTextField4.setText(getTaxonomyID(jComboBox1.getSelectedItem().toString()));
		} 
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
		catch (CommunicationsException c ) {

			if(this.connectionNotVerified) {

				String os_name = System.getProperty("os.name");
				if(os_name.contains("Windows")) {

					try {

						verifydbProcess();
					} 
					catch (SQLException e) {
					}

					this.connectionNotVerified = false;
					updateComboBox();
				}
				else {
				}
			}
		}
=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
		catch (SQLException e1) {

			Workbench.getInstance().error("no connection! Cause: " +e1.getMessage());
			e1.printStackTrace();
		}
	}

<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
	//	/**
	//	 * @param field
	//	 */
	//	private void addListener(JTextField field) {
	//
	//		field.getDocument().addDocumentListener(new DocumentListener() {
	//
	//			@Override
	//			public void insertUpdate(DocumentEvent e) {
	//
	//				DefaultComboBoxModel<String> sch = new DefaultComboBoxModel<>();
	//				jComboBox1.setModel(sch);
	//				jComboBox1.updateUI();
	//			}
	//
	//			@Override
	//			public void changedUpdate(DocumentEvent e) {
	//			}
	//
	//			@Override
	//			public void removeUpdate(DocumentEvent e) {
	//
	//				DefaultComboBoxModel<String> sch = new DefaultComboBoxModel<>();
	//				jComboBox1.setModel(sch);
	//				jComboBox1.updateUI();
	//			}
	//
	//		});
	//	}
=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java

	/**
	 * @throws SQLException 
	 * 
	 */
	private void setComboBox() throws SQLException {

		DefaultComboBoxModel<String> sch = new DefaultComboBoxModel<>();

		Map<String, String> credentials = LoadFromConf.loadDatabaseCredentials(FileUtils.getConfFolderPath());
		String username = null, password = null, host = null, port = null;

		DatabaseType databaseType =  DatabaseType.H2;
		if (credentials.get("dbtype").equals("mysql"))
			databaseType = DatabaseType.MYSQL;

		username = credentials.get("username");
		password = credentials.get("password");
		if (databaseType.equals(DatabaseType.MYSQL)) {
			host = credentials.get("host");
			port = credentials.get("port");
		}

		DatabaseSchemas mSchemas = new DatabaseSchemas(username, password, host, port, databaseType);

		if(mSchemas.isConnected()) {

			List<String> schemas = mSchemas.getSchemas();

			//get list of databases already used by projects, and remove those from schemas

			if(schemas.isEmpty()) {
				if(createDatabase()) {
					createNewWorkspace();
					schemas = mSchemas.getSchemas();
				}
				else {

					Workbench.getInstance().warn("no workspace available for merlin.");
				}
			}
			sch = new DefaultComboBoxModel<>(schemas.toArray(new String[schemas.size()]));
		}
		else {
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
			Workbench.getInstance().error("error! check your database configuration file in merlin directory at /utilities/"+databaseType.toString()+"_settings.conf");
=======
			Workbench.getInstance().error("error! check your database configuration file in merlin directory at \\conf\\database_settings.conf");
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
		}		

		jComboBox1.setModel(sch);
		jComboBox1.updateUI();

	}

	/**
	 * Get TaxonomyId for the given database
	 * 
	 * @param dbname
	 * @return
	 */
	private String getTaxonomyID(String dbname){

		try {

			Map<String, String> credentials = LoadFromConf.loadDatabaseCredentials(FileUtils.getConfFolderPath());
			String username = null, password = null, host = null, port = null;

			Connection conn = null;

			DatabaseType databaseType =  DatabaseType.H2;
			if (credentials.get("dbtype").equals("mysql"))
				databaseType = DatabaseType.MYSQL;

			username = credentials.get("username");
			password = credentials.get("password");
			if (databaseType.equals(DatabaseType.MYSQL)) {
				host = credentials.get("host");
				port = credentials.get("port");
			}

			conn = new Connection(host, port, dbname, username, password, databaseType);

			Statement statement = conn.createStatement();

			String taxID = ProjectAPI.getOrganismID(statement);
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java

=======
			
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
			return taxID;

		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @param unAnnotated
	 * @param initialSize
	 * @param location
	 * @return
	 */
	private static boolean createDatabase(){

		int i =CustomGUI.stopQuestion("create new workspace?",
				"merlin could not detect any compatible workspace, do you wish to create one?",
				new String[]{"yes", "no"});


		switch (i) {

		case 0:return true;
		case 1: return false;
		}
		return false;
	}

	private static void createNewWorkspace() {

		new NewWorkspaceGUI();
	}

<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
	/**
	 * @param name
	 * @return
	 */
	private String buildName(String name) {

		Project.setCounter(Project.getCounter()+1);
		name="MyProject_"+Project.getCounter();
		return name;
	}

	/**
	 * @throws SQLException 
	 * 
	 */
	private void verifydbProcess() throws SQLException {

		//this.oldPID=
		//DatabaseProcess.listDBProcess();
		//this.pid=
		//DatabaseProcess.startDBProcess(jTextField3.getText(), new String(jPasswordField1.getPassword()), jTextField1.getText(), jTextField2.getText());
	}
=======
//	/**
//	 * @param name
//	 * @return
//	 */
//	private String buildName(String name) {
//
//		Project.setCounter(Project.getCounter()+1);
//		name="MyProject_"+Project.getCounter();
//		return name;
//	}

>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java

	private void setComboBoxAssembly(String taxID) {

		this.setSize(550, 400);
		Utilities.centerOnOwner(this);

		try{
			DefaultComboBoxModel<String> aNames = new DefaultComboBoxModel<>();

			DocumentSummarySet docSummaryset = CreateGenomeFile.getESummaryFromNCBI(taxID);
			List<String> assemblyNames = CreateGenomeFile.getAssemblyNames(docSummaryset);
			assemblyNames.add("don't download");

			aNames = new DefaultComboBoxModel<>(assemblyNames.toArray(new String[assemblyNames.size()]));

			AssemblyPanel.jComboBox1.setModel(aNames);
			AssemblyPanel.jComboBox1.updateUI();

		}

		catch(Exception e){
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
			Workbench.getInstance().warn("Assembly records not found for given taxonomyID.");
=======
			Workbench.getInstance().warn("assembly records not found for provided taxonomy identifier.");
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
			e.printStackTrace();
		}
	}

	private void updateTextArea(String dbName) {

		String taxonomyID = jTextField4.getText();

		List<String> assemblyInfo = CreateGenomeFile.getAssemblyRecordInfo(dbName, taxonomyID);
		String text1 = assemblyInfo.get(0);
		String text2 = assemblyInfo.get(4);

		for(int i=1; i<4; i++) {
			text1 += "\n" + assemblyInfo.get(i);
			text2 += "\n" + assemblyInfo.get(i+4);
		}

		jTextArea1.setText(text1);
		jTextArea2.setText(text2);
		this.setSize(550, 350);
		Utilities.centerOnOwner(this);
	}

	private void removePanel(Component panel){
		this.jPanel1.remove(panel);
		this.setSize(600, 200);
		Utilities.centerOnOwner(this);
	}
<<<<<<< HEAD:plugins_src/pt/uminho/sysbio/merlin/core/gui/NewProjectGui.java
	
	//	private void removeTextArea(Component textArea){
	//		this.jPanel4.remove(textArea);
	//		this.setSize(400, 350);
	//	}
=======
>>>>>>> f84611906e8065eca7c69ba007972198710cb344:plugins_src/pt/uminho/ceb/biosystems/merlin/core/gui/NewProjectGui.java
}
