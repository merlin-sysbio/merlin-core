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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumnModel;

import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation.CompartmentsAnnotationDataContainer;
import pt.uminho.ceb.biosystems.merlin.core.gui.CustomGUI;
import pt.uminho.ceb.biosystems.merlin.core.gui.IgnoreCompartments;
import pt.uminho.ceb.biosystems.merlin.core.utilities.ButtonColumn;
import pt.uminho.ceb.biosystems.merlin.core.utilities.CreateImageIcon;
import pt.uminho.ceb.biosystems.merlin.core.utilities.ExportToXLS;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MyJTable;
import pt.uminho.ceb.biosystems.merlin.core.utilities.SearchInTable;
import pt.uminho.ceb.biosystems.merlin.core.views.UpdatablePanel;
import pt.uminho.ceb.biosystems.merlin.core.views.windows.GenericDetailWindow;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.CompartmentsAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.HomologyAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;

public class CompartmentsAnnotationDataViewer extends UpdatablePanel {

	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane1;
	private JPanel jPanelExport, jPanelSecComp, jPanelIntegration;
	private JButton  jButtonExportTxt, jButtonIntegration;
	private JPanel jPanel1, jPanel2;
	private JRadioButton jRadioButtonChemReact, jRadioButtonTranspReact;
	private MyJTable jTable;
	private CompartmentsAnnotationDataContainer compartmentsContainer;
	private GenericDataTable mainTableData;
	private String selectedRowID;
	private SearchInTable searchInGenes;
	private ButtonColumn buttonColumn;
	private JComboBox<String> jThreshold;
	private double threshold;
	private JButton jButtonCleanIntegration;
	private JLabel jLabelComp, jLabelDistance;


	/**
	 * @param genes
	 */
	public CompartmentsAnnotationDataViewer(CompartmentsAnnotationDataContainer compartmentsContainer) {

		super(compartmentsContainer);

		this.compartmentsContainer = compartmentsContainer;
		this.compartmentsContainer.setThreshold(10.0);
		List<Integer> nameTabs = new ArrayList<>();
		nameTabs.add(1);
		nameTabs.add(2);

		this.searchInGenes= new SearchInTable(nameTabs);
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
				jPanelSecComp = new JPanel();
				GridBagLayout jPanelPredictionLayout = new GridBagLayout();
				jPanel2.add(jPanelSecComp, new GridBagConstraints(0, 2, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelSecComp.setBounds(325, 41, 376, 79);
				jPanelSecComp.setBorder(BorderFactory.createTitledBorder("secondary compartments"));
				jPanelPredictionLayout.columnWidths = new int[] {7, 7, 7, 7, 7, 7, 7};
				jPanelPredictionLayout.rowHeights = new int[] {7,20, 7, 20, 7};
				jPanelPredictionLayout.columnWeights = new double[] {0.0, 0.1, 0.1, 0.0, 0.1, 0.1, 0.0};
				jPanelPredictionLayout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
				jPanelSecComp.setLayout(jPanelPredictionLayout);
				//				{
				//					jTextField1 = new JTextField();
				//					jPanelSecComp.add(jTextField1, new GridBagConstraints(1, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				//					jTextField1.setText(compartmentsContainer.getThreshold().toString());
				//					jTextField1.setToolTipText("select difference to primary compartment");
				//					jButtonSetThreshold = new JButton();
				//					jPanelSecComp.add(jButtonSetThreshold, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				//					jButtonSetThreshold.setText("difference %");
				//					jButtonSetThreshold.setToolTipText("select difference to primary compartment");
				//					jButtonSetThreshold.setBounds(199, 57, 42, 20);
				//					jButtonSetThreshold.addActionListener(new ActionListener() {
				//
				//						public void actionPerformed(ActionEvent arg0) {
				//
				//							selectThreshold();
				//						}});
				//				}
				{
					jLabelDistance = new JLabel("allowed difference = ");
					jPanelSecComp.add(jLabelDistance, new GridBagConstraints(1, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

				}
				{
					jThreshold = new JComboBox<String>(new DefaultComboBoxModel<>(new String[] {" 0.0", " 0.1" , " 0.2" , " 0.3", " 0.4", " 0.5"}));
					jPanelSecComp.add(jThreshold, new GridBagConstraints(2, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jThreshold.setSelectedIndex(1);
					jThreshold.setToolTipText("display compartments with difference below this percentage");

					jThreshold.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0) {

							selectThreshold();

						}});
				}
				{
					jLabelComp = new JLabel("where, difference = primary score - secondary score");
					jPanelSecComp.add(jLabelComp, new GridBagConstraints(1, 3, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

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
				jPanel2.add(jPanelExport, new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelExport.setBounds(567, 56, 139, 61);
				jPanelExport.setBorder(BorderFactory.createTitledBorder("export"));
				{
					jButtonExportTxt = new JButton();
					jPanelExport.add(jButtonExportTxt, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonExportTxt.setText("export file");
					jButtonExportTxt.setToolTipText("export to excel file (xls)");
					jButtonExportTxt.setIcon(new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Download.png"))),0.1).resizeImageIcon());
					jButtonExportTxt.setPreferredSize(new Dimension(200, 40));
					jButtonExportTxt.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0)  {

							try {

								JFileChooser fc = new JFileChooser();
								fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
								fc.setDialogTitle("Select directory");
								int returnVal = fc.showOpenDialog(new JTextArea());

								if (returnVal == JFileChooser.APPROVE_OPTION) {


									File file = fc.getSelectedFile();
									String excelFileName = file.getAbsolutePath();
									Calendar cal = new GregorianCalendar();

									// Get the components of the time
									int hour24 = cal.get(Calendar.HOUR_OF_DAY);     // 0..23
									int min = cal.get(Calendar.MINUTE);             // 0..59
									int day = cal.get(Calendar.DAY_OF_YEAR);		//0..365

									excelFileName += "/"+compartmentsContainer.getName()+"_"+compartmentsContainer.getProject().getName()+"_"+hour24+"_"+min+"_"+day+".xls";

									ExportToXLS.exportToXLS(excelFileName, mainTableData, jTable);

									Workbench.getInstance().info("data successfully exported.");
								}
							} catch (Exception e) {

								Workbench.getInstance().error("an error occurred while performing this operation. Error "+e.getMessage());
								e.printStackTrace();
							}
						}
					});
				}
			}
			{
				jPanelIntegration = new JPanel();
				GridBagLayout jPanelPredictionLayout = new GridBagLayout();
				jPanel2.add(jPanelIntegration, new GridBagConstraints(4, 2, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelIntegration.setBounds(325, 41, 376, 79);
				jPanelIntegration.setBorder(BorderFactory.createTitledBorder("integration"));
				jPanelPredictionLayout.columnWidths = new int[] {7, 7, 7, 7, 7, 7, 7};
				jPanelPredictionLayout.rowHeights = new int[] {7,20, 7, 20, 7};
				jPanelPredictionLayout.columnWeights = new double[] {0.0, 0.1, 0.1, 0.0, 0.1, 0.1, 0.0};
				jPanelPredictionLayout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
				jPanelIntegration.setLayout(jPanelPredictionLayout);
				{				
					jRadioButtonChemReact = new JRadioButton();
					jPanelIntegration.add(jRadioButtonChemReact, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					jRadioButtonChemReact.setText("biochemical reactions");
					jRadioButtonChemReact.setToolTipText("compartmentalise biochemical reactions");
					jRadioButtonChemReact.setBounds(15, 34, 144, 20);

					jRadioButtonTranspReact = new JRadioButton();
					jPanelIntegration.add(jRadioButtonTranspReact, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					jRadioButtonTranspReact.setText("transport reactions");
					jRadioButtonTranspReact.setToolTipText("compartmentalise transport reactions");
					jRadioButtonTranspReact.setBounds(15, 34, 144, 20);

					jButtonIntegration = new JButton();
					jPanelIntegration.add(jButtonIntegration, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonIntegration.setPreferredSize(new Dimension(300, 40));
					jButtonIntegration.setText("integrate to model");
					jButtonIntegration.setToolTipText("this operation assigns the model reactions (including the transport reactions) to the predicted compartments");
					jButtonIntegration.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Integrate.png")),0.1).resizeImageIcon());
					jButtonIntegration.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0) {

							if(compartmentsContainer == null) {

								Workbench.getInstance().error("no transporters information on this project!");
							}

							try {

								Connection conn = compartmentsContainer.getConnection();

								Statement statement = conn.createStatement();

								new IgnoreCompartments(jRadioButtonChemReact.isSelected(), jRadioButtonTranspReact.isSelected(), compartmentsContainer, statement);

								//CompartmentsPrediction Predictions = new CompartmentsPrediction(compartmentsContainer.getProject(),threshold);
								//								IntegrateCompartmentstoDatabase integrate = new IntegrateCompartmentstoDatabase(jRadioButtonChemReact.isSelected(), jRadioButtonTranspReact.isSelected(), ignore, threshold, compartmentsContainer.getProject(), compartmentsContainer.getGeneCompartments());

								//								ParamSpec[] paramsSpec = new ParamSpec[]{
								//										new ParamSpec("biochemical", Boolean.class, jRadioButtonChemReact.isSelected(), null),
								//										new ParamSpec("transporters", Boolean.class, jRadioButtonTranspReact.isSelected(), null),
								//										new ParamSpec("ignore", String.class, ignore, null),
								//										new ParamSpec("project", Project.class, compartmentsContainer.getProject(), null),
								//										new ParamSpec("geneCompartments", Map.class, compartmentsContainer.getGeneCompartments(), null)
								//								};
								//
								//								for (@SuppressWarnings("rawtypes") OperationDefinition def : Core.getInstance().getOperations()){
								//									if (def.getID().equals("operations.IntegrateCompartmentstoDatabase.ID")){
								//
								//										Workbench.getInstance().executeOperation(def, paramsSpec);
								//									}
								//								}
								//								
								//checkButtonsStatus();

							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});

					jButtonCleanIntegration = new JButton();
					jPanelIntegration.add(jButtonCleanIntegration, new GridBagConstraints(3, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonCleanIntegration.setPreferredSize(new Dimension(300, 40));
					jButtonCleanIntegration.setText("clean integration");
					jButtonCleanIntegration.setToolTipText("this operation removes the reactions (including transport reactions) assigned to the model");
					jButtonCleanIntegration.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Delete.png")),0.1).resizeImageIcon());
					jButtonCleanIntegration.setEnabled(false);

					jButtonCleanIntegration.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0) {

							try {
								
									Connection conn = compartmentsContainer.getConnection();
						
									Statement statement = conn.createStatement();
						
								boolean transport = CompartmentsAPI.checkTransportersReactions(statement);
								boolean biochemical = CompartmentsAPI.checkBiochemicalReactions(statement);

								if(transport && biochemical) {

									if(confirmClean(statement))
										Workbench.getInstance().info("reactions assigned to the model removed!");

									else
										Workbench.getInstance().warn("cleaning operation canceled!");

								}
								else {

									if(confirmSimpleClean(statement))
										Workbench.getInstance().info("all reactions assigned to the model removed!");

									else
										Workbench.getInstance().warn("cleaning operation canceled!");
								}

//								checkButtonsStatus();
								updateTableUI();
								conn.closeConnection();
								
							} catch (Exception e) {
								Workbench.getInstance().error("error while removing the reactions!");
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

			this.checkButtonsStatus();
			this.setPreferredSize(new java.awt.Dimension(887, 713));

		} 
		catch (Exception e) {e.printStackTrace();}
	}

	/**
	 * Method to check which buttons should be active.
	 */
	private void checkButtonsStatus(){

		try {
			
			Connection conn = compartmentsContainer.getConnection();
			
			Statement statement = conn.createStatement();

			// check radio buttons

			boolean biochemical = CompartmentsAPI.checkBiochemicalReactions(statement);

			if(biochemical){
				jRadioButtonChemReact.setEnabled(false);
				jRadioButtonChemReact.setSelected(false);
			}
			else{
				jRadioButtonChemReact.setEnabled(true);
				jRadioButtonChemReact.setSelected(true);
			}

			boolean transport = CompartmentsAPI.checkTransportersReactions(statement);

			if(transport){
				jRadioButtonTranspReact.setEnabled(false);
				jRadioButtonTranspReact.setSelected(false);
			}
			else{
				jRadioButtonTranspReact.setEnabled(true);
				jRadioButtonTranspReact.setSelected(true);
			}

			// check buttons

			if(biochemical && transport){
				jButtonIntegration.setEnabled(false);
				jButtonCleanIntegration.setEnabled(true);
			}
			else if(biochemical || transport){
				jButtonCleanIntegration.setEnabled(true);
				jButtonCleanIntegration.setEnabled(true);
			}
			else{
				jButtonIntegration.setEnabled(true);
				jButtonCleanIntegration.setEnabled(false);
			}

//			MerlinUtils.updateCompartmentsAnnotationView(compartmentsContainer.getProject().getName());
			
			conn.closeConnection();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}


	/**
	 * 
	 */
	private void selectThreshold() {

		this.threshold = jThreshold.getSelectedIndex()*10;

		this.compartmentsContainer.setThreshold(this.threshold);
		this.fillList();
	}

	/**
	 * 
	 */
	public void fillList() {

		mainTableData = this.compartmentsContainer.getInfo();

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
	 * Method to confirm cleaning integration.
	 * 
	 * @return boolean
	 * @throws SQLException 
	 */
	private boolean confirmSimpleClean(Statement statement) throws SQLException {

		int i;

		i =CustomGUI.stopQuestion("clean integration", 
				"continue cleaning operation?", 
				new String[]{"Yes", "No"});

			switch (i)
			{
			case 0:
			{

				CompartmentsAPI.removeNotOriginalReactions(statement);
				return true;

			}
			case 1:
			{
				return false;
			}
			default:
			{
				return false;
			}
			}

	}

	/**
	 * Method to select which integration to clean.
	 * 
	 * @return boolean
	 * @throws SQLException 
	 */
	private boolean confirmClean(Statement statement) throws SQLException {

		int i;

		i =CustomGUI.stopQuestion("Clean integration", 
				"which integration do you want to clean?",
				new String[]{"all", "biochemical reactions", "transporters reactions", "cancel"});

			switch (i)
			{
			case 0:
			{
				CompartmentsAPI.removeNotOriginalReactions(statement);
				return true;
			}
			case 1:
			{
				CompartmentsAPI.removeNotOriginalBiochemicalReactions(statement);
				return true;
			}
			case 2:
			{
				CompartmentsAPI.removeNotOriginalTransportersReactions(statement);
				return true;
			}
			default:
			{
				return false;
			}
			}
			
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

		DataTable[] informationTable = compartmentsContainer.getRowInfo(selectedRowID);

		new GenericDetailWindow(informationTable, "Gene data", "Gene: "+compartmentsContainer.getGeneName(selectedRowID));
	}

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#updateGraphicalObject()
	 */
	@Override
	public void updateTableUI() {

		this.fillList();
		this.updateUI();
		this.checkButtonsStatus();
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
