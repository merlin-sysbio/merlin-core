package pt.uminho.ceb.biosystems.merlin.core.views.annotation;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation.CompartmentsAnnotationDataContainer;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation.TransportersAnnotationDataContainer;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.core.utilities.ButtonColumn;
import pt.uminho.ceb.biosystems.merlin.core.utilities.CreateImageIcon;
import pt.uminho.ceb.biosystems.merlin.core.utilities.ExportToXLS;
import pt.uminho.ceb.biosystems.merlin.core.utilities.LoadFromConf;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MyJTable;
import pt.uminho.ceb.biosystems.merlin.core.utilities.SearchInTable;
import pt.uminho.ceb.biosystems.merlin.core.views.UpdatablePanel;
import pt.uminho.ceb.biosystems.merlin.core.views.windows.GenericDetailWindow;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.TransportersAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.GeneCompartments;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

public class TransportersAnnotationDataViewer extends UpdatablePanel {

	private static final long serialVersionUID = -1;
	private JScrollPane jScrollPane1;
	private JPanel jPanelExport, jPanelIntegration, jPanelReactions, jPanelDatabase;
	private JButton jButtonExportXLS, jButtonIntegration, jButtonReactions, jButtonDatabase;
//	private JComboBox<String> jIntegrateBox;
	private JPanel jPanel1, jPanel2;
//	private JTextField jTextField1, jTextField2, jTextField3;
//	private JLabel jLabel1, jLabel2, jLabel3;
	private MyJTable jTable;
	private TransportersAnnotationDataContainer transportersContainer;
	private GenericDataTable mainTableData;
	private String selectedRowID;
	private SearchInTable searchInGenes;
	private ButtonColumn buttonColumn;
	private Project project;
	private JButton jButtonCleanIntegration;
	private Map<String, String> settings;
	private double threshold, alpha; 


	/**
	 * @param genes
	 */
	public TransportersAnnotationDataViewer(TransportersAnnotationDataContainer transportersContainer) {

		super(transportersContainer);
		this.project = transportersContainer.getProject();
		this.transportersContainer = transportersContainer;
		List<Integer> nameTabs = new ArrayList<>();
		nameTabs.add(1);
		nameTabs.add(3);
		this.settings = LoadFromConf.loadTransportReactionsSettings(FileUtils.getConfFolderPath()); // configuration file containing information about alpha, threshold and symport currency metabolites
		
		alpha = Double.parseDouble(settings.get("alphaValue"));
		
		if(alpha < 0 || alpha > 1)
			Workbench.getInstance().warn("The alpha value must be between 0 and 1. \n \nCheck configurantion file /conf/transp_reactions_settings.conf");
		else
			transportersContainer.setAlpha(alpha);
		
		threshold = Double.parseDouble(settings.get("cut-offThreshold"));
		
		if( threshold < 0 || threshold > 1)
			Workbench.getInstance().warn("please set a valid threshold value (0 < threshold < 1).  \n \nCheck configurantion file /conf/transp_reactions_settings.conf");
		else 
			transportersContainer.setThreshold(threshold);
		
		String[] searchParams = new String[] { "name", "all", "metabolite" };
		this.searchInGenes = new SearchInTable(nameTabs, searchParams, project);
		
		initGUI();
		fillList();
	}


	/**
	 * 
	 */
	private void initGUI() {

		try  {

			GridBagLayout jPanelLayout = new GridBagLayout();
			jPanelLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			jPanelLayout.columnWidths = new int[] {7, 7, 7};
			jPanelLayout.rowWeights = new double[] {0.0, 200.0, 0.0, 0.0, 0.0};
			jPanelLayout.rowHeights = new int[] {7, 50, 7, 3, 7};

			this.setLayout(jPanelLayout);
			jPanel2 = new JPanel();
			GridBagLayout jPanel2Layout = new GridBagLayout();
			jPanel2Layout.columnWeights = new double[] {0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1};
			jPanel2Layout.columnWidths = new int[] {7, 7, 7, 7, 7, 7, 7};
			jPanel2Layout.rowWeights = new double[] {0.1, 0.0};
			jPanel2Layout.rowHeights = new int[] {7, 7};
			jPanel2.setLayout(jPanel2Layout);
			this.add(jPanel2, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			{
				jPanelDatabase = new JPanel();
				GridBagLayout jPanelDatabaseLayout = new GridBagLayout();
				jPanelDatabaseLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
				jPanelDatabaseLayout.columnWidths = new int[] {7, 7, 7};
				jPanelDatabaseLayout.rowWeights = new double[] {0.0};
				jPanelDatabaseLayout.rowHeights = new int[] {5};
				jPanelDatabase.setLayout(jPanelDatabaseLayout);
				jPanel2.add(jPanelDatabase, new GridBagConstraints(0, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelDatabase.setBounds(567, 56, 139, 61);
				jPanelDatabase.setBorder(BorderFactory.createTitledBorder("TRIAGE"));
				{
					jButtonDatabase = new JButton();
					jPanelDatabase.add(jButtonDatabase, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonDatabase.setText("add TRIAGE data");
					jButtonDatabase.setToolTipText("integrate transporters information from TRIAGE's database");
					jButtonDatabase.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Synchronize.png")),0.1).resizeImageIcon());
					jButtonDatabase.setPreferredSize(new Dimension(200, 40));
					jButtonDatabase.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0)  {
							
							try {
								
								if(project.isSW_TransportersSearch()) {

									ParamSpec[] paramsSpec = new ParamSpec[]{
										new ParamSpec("Workspace", Project.class, transportersContainer.getProject(), null)};
									
									for (@SuppressWarnings("rawtypes") OperationDefinition def : Core.getInstance().getOperations()){
										if (def.getID().equals("operations.AddTRIAGEData.ID")){
											
											Workbench.getInstance().executeOperation(def, paramsSpec);
										}
									}

									MerlinUtils.updateTransportersAnnotationView(project.getName());
									MerlinUtils.updateProjectView(project.getName());
									
								}
								else {
									
									Workbench.getInstance().error("Please perform transporters identification first!");
								}
								
							}
							catch(Exception ex){
								ex.printStackTrace();
							}
						}
							
					});
				}
			}
			{
				jPanelReactions = new JPanel();
				GridBagLayout jPanelReactionsLayout = new GridBagLayout();
				jPanel2.add(jPanelReactions, new GridBagConstraints(2, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelReactions.setBounds(325, 41, 376, 79);
				jPanelReactions.setBorder(BorderFactory.createTitledBorder("reactions"));
				jPanelReactionsLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
				jPanelReactionsLayout.columnWidths = new int[] {7, 7, 7};
				jPanelReactionsLayout.rowWeights = new double[] {0.0};
				jPanelReactionsLayout.rowHeights = new int[] {5};
				
				jPanelReactions.setLayout(jPanelReactionsLayout);
				{
//					jTextField1 = new JTextField();
//					jPanelReactions.add(jTextField1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
//					jTextField1.setText("0.3");
//					
//					if(this.transportersContainer.getProject().getTransportContainer()!= null)
//						jTextField1.setText(this.transportersContainer.getProject().getTransportContainer().getAlpha()+"");
//					else if(this.transportersContainer.getAlpha()>0 && this.transportersContainer.getAlpha()!=new Double(jTextField1.getText()))
//						jTextField1.setText(this.transportersContainer.getAlpha()+"");
//					else
//						this.transportersContainer.setAlpha(Double.parseDouble(jTextField1.getText()));
//					
//					jTextField1.addActionListener(new ActionListener() {
//						public void actionPerformed(ActionEvent arg0) {
//
//							if(Double.parseDouble(jTextField1.getText())<0 || Double.parseDouble(jTextField1.getText())>1) {
//
//								jTextField1.setText(transportersContainer.getAlpha()+"");
//								Workbench.getInstance().warn("The value must be between 0 and 1");
//							}
//							else {
//
//								transportersContainer.setAlpha(Double.parseDouble(jTextField1.getText()));
//							}
//						}
//					} );
//					jTextField1.setToolTipText("frequency and taxonomy scores leverage");
//					jTextField1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED),null));
//					jTextField1.setBounds(164, 57, 36, 20);
//					jLabel1 = new JLabel();
//					jPanelReactions.add(jLabel1, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
//					jLabel1.setText(" alpha value");
//					jLabel1.setToolTipText("frequency and taxonomy scores leverage");
//					jLabel1.setFocusable(false);
//					
//					jTextField2 = new JTextField();
//					jPanelReactions.add(jTextField2, new GridBagConstraints(1, 3, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
//					jTextField2.setText("0.2");
//				
//					if(this.transportersContainer.getProject().getTransportContainer()!= null)
//						jTextField2.setText(this.transportersContainer.getProject().getTransportContainer().getThreshold()+"");
//					else if(this.transportersContainer.getThreshold()>0 && this.transportersContainer.getThreshold()!=new Double(jTextField2.getText()))
//						jTextField2.setText(this.transportersContainer.getThreshold()+"");
//					else
//						this.transportersContainer.setThreshold(Double.parseDouble(jTextField2.getText()));
//					jTextField2.addActionListener(new ActionListener() {
//							public void actionPerformed(ActionEvent arg0) {
//
//								if(Double.parseDouble(jTextField2.getText())<0 || Double.parseDouble(jTextField2.getText())>1) {
//
//									jTextField2.setText(transportersContainer.getThreshold()+"");
//									Workbench.getInstance().warn("The value must be between 0 and 1");
//								}
//								else {
//
//									transportersContainer.setThreshold(Double.parseDouble(jTextField2.getText()));
//								}
//							}
//						} );
//					
//					jTextField2.setToolTipText("cut-off threshold for metabolites selection");
//					jTextField2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED),null));
//					jTextField2.setBounds(164, 57, 36, 20);
//					jLabel2 = new JLabel();
//					jPanelReactions.add(jLabel2, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
//					jLabel2.setText(" cut-off threshold");
//					jLabel2.setToolTipText("cut-off threshold for metabolites selection");
//					jLabel2.setFocusable(false);
//					
//					jTextField3 = new JTextField();
//					jPanelReactions.add(jTextField3, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
//					jTextField3.setText("C00080; C01330");
//					jTextField3.setToolTipText("use KEGG ids separated by semicolon (default value: H+; Na+)");
//					jTextField3.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED),null));
//					jTextField3.setBounds(164, 57, 36, 20);
//					jLabel3 = new JLabel();
//					jPanelReactions.add(jLabel3, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
//					jLabel3.setText(" symport currency metabolites");
//					jLabel3.setToolTipText("use KEGG ids separated by semicolon (default value: H+; Na+)");
//					jLabel3.setFocusable(false);
					
//					ComboBoxModel<String> jComboBoxReportsModel = new DefaultComboBoxModel<>(new String[] { "Reports", "Integration" });
//					jIntegrateBox = new JComboBox<>();
//					jPanelReactions.add(jIntegrateBox, new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
//					jIntegrateBox.setModel(jComboBoxReportsModel);
//					jIntegrateBox.setToolTipText("Generate integration reports or Integrate database");
					
					jButtonReactions = new JButton();
					jPanelReactions.add(jButtonReactions, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonReactions.setPreferredSize(new Dimension(300, 40));
					jButtonReactions.setText("create transport reactions");
					jButtonReactions.setToolTipText("generates transport reactions report");
					jButtonReactions.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Synchronize.png")),0.1).resizeImageIcon());
					jButtonReactions.addActionListener(new ActionListener(){
						public void actionPerformed(ActionEvent arg0) {
							
							if(transportersContainer == null) {
								Workbench.getInstance().error("no transporters information on this project!");
							}
							
							settings = LoadFromConf.loadTransportReactionsSettings(FileUtils.getConfFolderPath());	// refresh the initial reading
							
							try{
								if(project.isSW_TransportersSearch()) {
									
									alpha = Double.parseDouble(settings.get("alphaValue"));
									threshold = Double.parseDouble(settings.get("cut-offThreshold"));
									String meta = settings.get("symportCurrencyMetabolites");
									
									if( alpha < 0 || alpha > 1) {
										Workbench.getInstance().warn("please set a valid alpha value ( 0 < alpha < 1). \n \nCheck configurantion file /conf/transp_reactions_settings.conf");
									}
									else if(threshold<0 || threshold>1) {
										Workbench.getInstance().warn("please set a valid threshold value (0 < threshold < 1).  \n \nCheck configurantion file /conf/transp_reactions_settings.conf");
									}
									
									ParamSpec[] paramsSpec = new ParamSpec[]{
										new ParamSpec("alpha", double.class, alpha, null),
										new ParamSpec("threshold", double.class, threshold, null),
										new ParamSpec("metabolites", String.class, meta, null),
										new ParamSpec("workspace", Project.class, transportersContainer.getProject(), null)
									};
									
									for (@SuppressWarnings("rawtypes") OperationDefinition def : Core.getInstance().getOperations()){
										if (def.getID().equals("operations.CreateTransportReactions.ID")){
											
											Workbench.getInstance().executeOperation(def, paramsSpec);
										}
									}
									
									if(project.getTransportContainer()!=null)
										try {
											
											project.getTransportContainer().verifyDepBetweenClass();
										} 
									catch (IOException e) {

										e.printStackTrace();
									}
									MerlinUtils.updateTransportersAnnotationView(project.getName());
									MerlinUtils.updateProjectView(project.getName());
								}
								else{
									Workbench.getInstance().error("please perform transporters identification first!");
								}
							}
							catch(Exception ex){
								ex.printStackTrace();
							}
						}
					});
				}
			}
			{
				jPanelExport = new JPanel();
				GridBagLayout jPanelExportLayout = new GridBagLayout();
				jPanelExportLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
				jPanelExportLayout.columnWidths = new int[] {7, 7, 7};
				jPanelExportLayout.rowWeights = new double[] {0.0};
				jPanelExportLayout.rowHeights = new int[] {5};
				jPanelExport.setLayout(jPanelExportLayout);
				jPanel2.add(jPanelExport, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelExport.setBounds(567, 56, 139, 61);
				jPanelExport.setBorder(BorderFactory.createTitledBorder("export"));
				{
					jButtonExportXLS = new JButton();
					jPanelExport.add(jButtonExportXLS, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonExportXLS.setText("export file");
					jButtonExportXLS.setToolTipText("export to excel file (xls)");
					jButtonExportXLS.setIcon(new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Download.png"))),0.1).resizeImageIcon());
					jButtonExportXLS.setPreferredSize(new Dimension(200, 40));
					jButtonExportXLS.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0)  {

							try {

								JFileChooser fc = new JFileChooser();
								fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
								fc.setDialogTitle("Select directory");
								int returnVal = fc.showOpenDialog(new JTextArea());

								if (returnVal == JFileChooser.APPROVE_OPTION) {


									File file = fc.getSelectedFile();
									String filePath = file.getAbsolutePath();
									Calendar cal = new GregorianCalendar();

									// Get the components of the time
									int hour24 = cal.get(Calendar.HOUR_OF_DAY);     // 0..23
									int min = cal.get(Calendar.MINUTE);             // 0..59
									int day = cal.get(Calendar.DAY_OF_YEAR);		//0..365

									filePath += "/"+transportersContainer.getName()+"_"+transportersContainer.getProject().getName()+"_"+hour24+"_"+min+"_"+day+".xls";
									
									ExportToXLS.exportToXLS(filePath, mainTableData, jTable);

									Workbench.getInstance().info("data successfully exported.");
								}
							} catch (Exception e) {

								Workbench.getInstance().error("an error occurred while performing this operation. error "+e.getMessage());
								e.printStackTrace();
							}
						}
					});
				}
			}
			{
				jPanelIntegration = new JPanel();
				GridBagLayout jPanelIntegrationLayout = new GridBagLayout();
				jPanelIntegrationLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
				jPanelIntegrationLayout.columnWidths = new int[] {7, 7, 7};
				jPanelIntegrationLayout.rowWeights = new double[] {0.0};
				jPanelIntegrationLayout.rowHeights = new int[] {5};
				jPanelIntegration.setLayout(jPanelIntegrationLayout);
				jPanel2.add(jPanelIntegration, new GridBagConstraints(5, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelIntegration.setBounds(567, 56, 139, 61);
				jPanelIntegration.setBorder(BorderFactory.createTitledBorder("integration"));
				{
					jButtonIntegration = new JButton();
					jPanelIntegration.add(jButtonIntegration, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonIntegration.setText("integrate to model");
					jButtonIntegration.setToolTipText("integrates the generated transport reactions with the model reactions");
					jButtonIntegration.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Integrate.png")),0.1).resizeImageIcon());
					jButtonIntegration.setPreferredSize(new Dimension(200, 40));
					jButtonIntegration.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0)  {

							if(transportersContainer == null) {
								Workbench.getInstance().error("no transporters information on this Project!");
							}

							try {
								
								Connection conn = transportersContainer.getConnection();
								Statement statement = conn.createStatement();
								
								Map<String, GeneCompartments> geneCompartment = null;
								
								CompartmentsAnnotationDataContainer c = null;
								
								for(Entity e: project.getDatabase().getAnnotations().getEntitiesList())
									if(e.getName().equalsIgnoreCase("compartments"))
										c=(CompartmentsAnnotationDataContainer) e;
								
								if(ProjectAPI.isCompartmentalisedModel(statement))
									geneCompartment = c.runCompartmentsInterface(statement);
								
								ParamSpec[] paramsSpec = new ParamSpec[]{
										new ParamSpec("compartments", Map.class, geneCompartment, null),
										new ParamSpec("workspace", Project.class, transportersContainer.getProject(), null)
									};
									
									for (@SuppressWarnings("rawtypes") OperationDefinition def : Core.getInstance().getOperations()){
										if (def.getID().equals("operations.IntegrateTransporterstoDatabase.ID")){
											
											Workbench.getInstance().executeOperation(def, paramsSpec);
										}
									}

									checkButtonsStatus();
							}
							catch(Exception ex){
								ex.printStackTrace();
							}
						}
							
					});
				}
				{
					jButtonCleanIntegration = new JButton();
					jPanelIntegration.add(jButtonCleanIntegration, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonCleanIntegration.setText("clean integration");
					jButtonCleanIntegration.setToolTipText("clean the integrated transport reactions");
					jButtonCleanIntegration.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Delete.png")),0.1).resizeImageIcon());
					jButtonCleanIntegration.setPreferredSize(new Dimension(200, 40));
					jButtonCleanIntegration.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0)  {

							try {
								
								TransportersAPI.cleanIntegration(getStatement());
								
								Workbench.getInstance().info("Integration successfully cleaned!");
								
								checkButtonsStatus();
								
							} catch (Exception e) {
								
								Workbench.getInstance().error("error while cleaning integration!");
								e.printStackTrace();
							}
						}
							
					});
				}
			}
			{
				jPanel2.add(searchInGenes.addPanel(), new GridBagConstraints(0, 1, 7, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			}

			jPanel1 = new JPanel();
			GridBagLayout thisLayout = new GridBagLayout();
			jPanel1.setLayout(thisLayout);
			this.add(jPanel1, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			jScrollPane1 = new JScrollPane();
			jTable = new MyJTable();
			jTable.setShowGrid(false);
			jScrollPane1.setViewportView(jTable);
			jPanel1.add(jScrollPane1,new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			checkButtonsStatus();
			
			this.setPreferredSize(new java.awt.Dimension(887, 713));

		} 
		catch (Exception e) {e.printStackTrace();}
	}
	
	/**
	 * Method to check which buttons should be active.
	 */
	private void checkButtonsStatus(){
		
		try {
			boolean integrated = TransportersAPI.checkTransporters(getStatement());
			
			if(integrated){
				jButtonIntegration.setEnabled(false);
				jButtonCleanIntegration.setEnabled(true);
			}
			else{
				jButtonIntegration.setEnabled(true);
				jButtonCleanIntegration.setEnabled(false);
			}
			
			MerlinUtils.updateTransportersAnnotationView(transportersContainer.getProject().getName());
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void fillList() {
		
		mainTableData = this.transportersContainer.getInfo();
		
		jTable.setModel(mainTableData);
		jTable.setAutoCreateRowSorter(true);
		
		buttonColumn =  new ButtonColumn(jTable,0, new ActionListener(){

			public void actionPerformed(ActionEvent arg0){
				processButton(arg0);
			}},
			new MouseAdapter(){
				public void mouseClicked(MouseEvent e) {
					// {
					// get the coordinates of the mouse click
					Point p = e.getPoint();

					// get the row index that contains that coordinate
					int rowNumber = jTable.rowAtPoint(p);
					int  columnNumber = jTable.columnAtPoint(p);
					jTable.setColumnSelectionInterval(columnNumber, columnNumber);
					// Get the ListSelectionModel of the MyJTable
					ListSelectionModel model = jTable.getSelectionModel();
					// set the selected interval of rows. Using the "rowNumber"
					// variable for the beginning and end selects only that one row.
					model.setSelectionInterval( rowNumber, rowNumber );
					processButton(e);
				}
			}, new ArrayList<>());
		TableColumnModel tc = jTable.getColumnModel();
		tc.getColumn(0).setMaxWidth(35);				//button
		tc.getColumn(0).setResizable(false);
		tc.getColumn(0).setModelIndex(0);
		
		this.searchInGenes.setMyJTable(jTable);
		this.searchInGenes.setMainTableData(mainTableData);
		this.searchInGenes.setSearchTextField("");
		
	}


	/**
	 * @param arg0
	 */
	private void processButton(EventObject arg0) {
		
		JButton button = null;
		if(arg0.getClass()==ActionEvent.class) {

			button = (JButton)((ActionEvent) arg0).getSource();
		}

		if(arg0.getClass()==MouseEvent.class) {

			button = (JButton)((MouseEvent) arg0).getSource();
		}

		ListSelectionModel model = jTable.getSelectionModel();
		model.setSelectionInterval( buttonColumn.getSelectIndex(button), buttonColumn.getSelectIndex(button));

		selectedRowID = mainTableData.getRowID(jTable.convertRowIndexToModel(jTable.getSelectedRow()));

		DataTable[] informationTable = transportersContainer.getRowInfo(selectedRowID);
		
		if(this.transportersContainer.getProject().getTransportContainer()!=null)
			new GenericDetailWindow(informationTable, "Gene data", "Gene: "+transportersContainer.getGeneName(selectedRowID)+"   user alpha: "+this.transportersContainer.getAlpha()+"   reactions alpha: "+this.transportersContainer.getProject().getTransportContainer().getAlpha());
		else
			new GenericDetailWindow(informationTable, "Gene data", "Gene: "+transportersContainer.getGeneName(selectedRowID)+"   user alpha: "+this.transportersContainer.getAlpha());
	}
	
	/**
	 * Method to get the statement.
	 * 
	 * @return statement
	 */
	private Statement getStatement() {
		
		Statement statement = null;
		
		try {
			Connection conn = transportersContainer.getConnection();
			
			statement = conn.createStatement();
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return statement;
	}

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#updateGraphicalObject()
	 */
	@Override
	public void updateTableUI() {

		this.fillList();
		this.updateUI();
		this.revalidate();
		this.repaint();
	}

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#addListenersToGraphicalObjects(javax.swing.JPanel, javax.swing.MyJTable)
	 */
	@Override
	public void addListenersToGraphicalObjects() {

		jTable.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {

				updateTableUI();
			}
		});

		this.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent arg0) {}

			@Override
			public void focusGained(FocusEvent arg0) {

				updateTableUI();
			}
		});
	}
}
