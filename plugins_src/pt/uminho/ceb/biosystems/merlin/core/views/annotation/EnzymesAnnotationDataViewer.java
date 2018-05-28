package pt.uminho.ceb.biosystems.merlin.core.views.annotation;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation.EnzymesAnnotationDataInterface;
import pt.uminho.ceb.biosystems.merlin.core.gui.CustomGUI;
import pt.uminho.ceb.biosystems.merlin.core.gui.InsertRemoveDataWindow;
import pt.uminho.ceb.biosystems.merlin.core.remote.retriever.alignment.blast.WriteGBFile;
import pt.uminho.ceb.biosystems.merlin.core.utilities.ButtonColumn;
import pt.uminho.ceb.biosystems.merlin.core.utilities.ComboBoxColumn;
import pt.uminho.ceb.biosystems.merlin.core.utilities.CreateImageIcon;
import pt.uminho.ceb.biosystems.merlin.core.utilities.LinkOut;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MyJTable;
import pt.uminho.ceb.biosystems.merlin.core.utilities.SearchInTable;
import pt.uminho.ceb.biosystems.merlin.core.utilities.StarColumn;
import pt.uminho.ceb.biosystems.merlin.core.views.UpdatablePanel;
import pt.uminho.ceb.biosystems.merlin.core.views.windows.GenericDetailWindowBlast;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.HomologyAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import pt.uminho.ceb.biosystems.merlin.utilities.OpenBrowser;


/**
 * @author oDias
 *
 */
public class EnzymesAnnotationDataViewer extends UpdatablePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JScrollPane jScrollPane;
	private JButton jButtonIntegration;
	private JLabel jLabeldAlpha, jLabelAlphaText ,jLabelLowerThresholdText, jLabelLowerThreshold;
	//	/private JButton jButton1;
	//	private JTextField jTextField1;
	private JPanel jPanel3;
	private MyJTable jTable;
	private JButton saveButton, jButton1ExportXLS, jButtonGbk, jButtonAnnotation, jButtonReset;
	private JPanel jPanel1, jPanel2, jPanel21, jPanel23, commitPane;
	private GenericDataTable mainTableData;
	private EnzymesAnnotationDataInterface homologyDataContainer;
	private JFileChooser fc;
	private int selectedModelRow;
	private ComboBoxColumn productsColumn, enzymesColumn;
	private ButtonColumn buttonColumn;
	private StarColumn buttonStarColumn;
	private Map<Integer, List<String>> getUniprotECnumbersTable;
	private int infoColumnNumber, uniprotStarColumnNumber, locus_tagColumnNumber, geneNameColumnNumber, chromosomeColumnNumber = -1, 
			namesColumnNumber, namesScoreColumnNumber, ecnumbersColumnNumber, ecScoreColumnNumber, notesColumnNumber;//, selectColumnNumber
	private ItemListener namesItemListener, enzymesItemListener;
	private MouseAdapter namesMouseAdapter, enzymesMouseAdapter, buttonMouseAdapter, starMouseAdapter;
	private ActionListener buttonActionListener, starActionListener;
	private PopupMenuListener namesPopupMenuListener, enzymesPopupMenuListener;
	private SearchInTable searchInHomology;
	private MouseAdapter tableMouseAdapator;
	private TableModelListener tableModelListener;
	private List<Map<Integer, String>> itemsList;
	private String blastDatabase;  		//empty means ALL databases
	private int currentDatabaseIndex;
	private JLabel jLabelUpperThresholdText;
	private JLabel jLabelUpperThreshold;

	/**
	 * @param homologyDataContainer
	 */
	public EnzymesAnnotationDataViewer(EnzymesAnnotationDataInterface homologyDataContainer) {

		super(homologyDataContainer);

		try {
			
			Connection connection = homologyDataContainer.getConnection();
			Statement statement = connection.createStatement();
			
			statement = HomologyAPI.checkStatement(homologyDataContainer.getProject().getDatabase().getDatabaseAccess(), statement);

			this.blastDatabase = HomologyAPI.getLastestUsedBlastDatabase(statement);
			
			homologyDataContainer.getCommitedScorerData(blastDatabase);

			this.homologyDataContainer = homologyDataContainer;
			this.homologyDataContainer.setIsEukaryote();
			
			if(this.homologyDataContainer.getCommittedAlpha()>-1) 
				updateSettings(false);
			else 
				updateSettings(true);
			
			this.mainTableData = this.homologyDataContainer.getAllGenes(this.blastDatabase, false);

//			if(this.homologyDataContainer.getProject().isInitialiseHomologyData()) {
//
//				this.homologyDataContainer.getProject().setInitialiseHomologyData(false);
//				this.initialiser();
//			}
			
			List<Integer> nameTabs = new ArrayList<>();
			this.searchInHomology= new SearchInTable(nameTabs);

			this.initGUI();

			Rectangle visible = null;

			if(this.selectedModelRow>-1 && jTable.getRowCount()>0 && jTable.getRowCount()> this.selectedModelRow)
				visible = this.jTable.getCellRect(this.selectedModelRow, -1, true);

			this.fillList(visible);

			if(this.selectedModelRow>-1 && jTable.getRowCount()>this.selectedModelRow) {

				this.jTable.setRowSelectionInterval(this.selectedModelRow, this.selectedModelRow);
				this.jTable.scrollRectToVisible(this.jTable.getCellRect(this.selectedModelRow, -1, true));
			}
			
		}
		catch(Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void intitialiseTableColumns() {

		int number=0;
		this.infoColumnNumber = number++;
		this.locus_tagColumnNumber = number++;
		this.uniprotStarColumnNumber = number++;
		this.geneNameColumnNumber = number++;
		if(homologyDataContainer.isEukaryote())
			this.chromosomeColumnNumber = number++;
		this.namesColumnNumber = number++;
		this.namesScoreColumnNumber = number++;
		this.ecnumbersColumnNumber = number++;
		this.ecScoreColumnNumber = number++;
		this.notesColumnNumber = number++;
		//		this.selectColumnNumber = number++;

		List<Integer> nameTabs = new ArrayList<>();
		nameTabs.add(locus_tagColumnNumber);
		nameTabs.add(geneNameColumnNumber);
		this.searchInHomology.setNameTabs(nameTabs);;
	}

	/**
	 * 
	 */
	private void addListeners() {

		this.addMouseListener();
		this.addTableModelListener();
		if(this.namesItemListener==null)
			this.namesItemListener = this.getComboBoxNamesItemListener();
		if(this.enzymesItemListener==null)
			this.enzymesItemListener = this.getComboBoxEnzymesItemListener();
		if(this.namesMouseAdapter==null)
			this.namesMouseAdapter = this.getComboBoxNamesMouseListener();
		if(this.enzymesMouseAdapter==null)
			this.enzymesMouseAdapter = this.getComboBoxEnzymesMouseListener();
		if(this.namesPopupMenuListener==null)
			this.namesPopupMenuListener = this.getComboBoxNamesPopupMenuListener();
		if(this.enzymesPopupMenuListener==null)
			this.enzymesPopupMenuListener = this.getComboBoxEnzymesPopupMenuListener();
		if(this.buttonActionListener==null)
			this.buttonActionListener = this.getButtonActionListener();
		if(this.buttonMouseAdapter==null)
			this.buttonMouseAdapter = this.getButtonMouseAdapter();
		if(this.starActionListener==null)
			this.starActionListener = this.getStarActionListener();
		if(this.starMouseAdapter==null)
			this.starMouseAdapter = this.getStarMouseAdapter();
	}

	/**
	 * initiate graphical user interface
	 */
	private void initGUI() {

		try {

			GridBagLayout thisLayout = new GridBagLayout();
			thisLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			thisLayout.columnWidths = new int[] {7, 7, 7};
			thisLayout.rowWeights = new double[] {0.0, 200.0, 0.0, 0.0, 0.0};
			thisLayout.rowHeights = new int[] {7, 50, 7, 3, 7};
			this.setLayout(thisLayout);
			this.setPreferredSize(new Dimension(875, 585));

			{
				jPanel1 = new JPanel();
				GridBagLayout jPanel1Layout = new GridBagLayout();
				jPanel1Layout.rowWeights = new double[] {0.1};
				jPanel1Layout.rowHeights = new int[] {7};
				jPanel1Layout.columnWeights = new double[] {0.1};
				jPanel1Layout.columnWidths = new int[] {7};
				jPanel1.setLayout(jPanel1Layout);
				this.add(jPanel1, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

				jScrollPane = new JScrollPane();
				jPanel1.add(jScrollPane, new GridBagConstraints(0, 0, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jScrollPane.setPreferredSize(new java.awt.Dimension(7, 7));
				jScrollPane.setSize(900, 420);
			}
			{				
				jPanel2 = new JPanel();
				GridBagLayout jPanel2Layout = new GridBagLayout();
				jPanel2Layout.rowWeights = new double[] {0.0, 0.0};
				jPanel2Layout.rowHeights = new int[] {3, 3};
				jPanel2Layout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1};
				jPanel2Layout.columnWidths = new int[] {7, 7, 7, 7, 7, 7, 7, 7};
				jPanel2.setLayout(jPanel2Layout);
				this.add(jPanel2, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				{
					{
						{
							jPanel21 = new JPanel();
							jPanel2.add(jPanel21, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
							GridBagLayout jPanel21Layout = new GridBagLayout();
							jPanel21.setBounds(14, 41, 294, 63);
							jPanel21.setBorder(BorderFactory.createTitledBorder("export"));
							jPanel21Layout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
							jPanel21Layout.rowHeights = new int[] {7, 20, 7, 20, 7};
							jPanel21Layout.columnWeights = new double[] {0.1};
							jPanel21Layout.columnWidths = new int[] {7};
							jPanel21.setLayout(jPanel21Layout);
							{
								jButton1ExportXLS = new JButton();
								jButton1ExportXLS.setText("export file");
								jButton1ExportXLS.setToolTipText("export to excel file (xls)");
								jPanel21.add(jButton1ExportXLS, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
								jButton1ExportXLS.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Download.png")),0.1).resizeImageIcon());
								jButton1ExportXLS.addActionListener(new ActionListener()
								{
									public void actionPerformed(ActionEvent arg0) {

										fc.setDialogTitle("select directory");
										int returnVal = fc.showOpenDialog(new JTextArea());
										if (returnVal == JFileChooser.APPROVE_OPTION) {

											File file = fc.getSelectedFile();
											String path;
											if(file.isDirectory())
												path = file.getAbsolutePath();
											else
												path = file.getParentFile().getPath();

											exportToXls(exportAllData(),path);
										}
									}	
								});
							}
							{
								jButtonGbk = new JButton();
								jPanel21.add(jButtonGbk, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
								jButtonGbk.setText("genbank file");
								jButtonGbk.setToolTipText("update genbank file");
								jButtonGbk.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Download.png")),0.1).resizeImageIcon());
								jButtonGbk.addActionListener(new ActionListener(){
									public void actionPerformed(ActionEvent arg0) {
										try {

											saveGenbankFile();
										} 
										catch (IOException e) {

											e.printStackTrace();
										}
									}});
							}
						}
					}
				}
				{
					fc = new JFileChooser();
					fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				}
				{
					jPanel23 = new JPanel();
//					GridBagLayout jPanel23Layout = new GridBagLayout();
//					jPanel2.add(jPanel23, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
//					//					jPanel23.setBounds(325, 41, 376, 79);
//					jPanel23.setBorder(BorderFactory.createTitledBorder("gene selection"));
////					jPanel23Layout.columnWidths = new int[] {7, 7};
////					jPanel23Layout.rowHeights = new int[] {7,20, 7, 20};
////					jPanel23Layout.columnWeights = new double[] {0.05, 0.1};
////					jPanel23Layout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
//					
//					jPanel23Layout.columnWidths = new int[] {7, 7, 7};
//					jPanel23Layout.rowHeights = new int[] {7,20, 7, 20};
//					jPanel23Layout.columnWeights = new double[] {0.1, 0.1};
//					jPanel23Layout.rowWeights = new double[] {0.1, 0.0, 0.1};
//					
//					jPanel23.setLayout(jPanel23Layout);
					
					jPanel23 = new JPanel();
					jPanel2.add(jPanel23, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					GridBagLayout jPanel21Layout = new GridBagLayout();
					jPanel23.setBounds(14, 41, 294, 63);
					jPanel23.setBorder(BorderFactory.createTitledBorder("scores parameters"));
					jPanel21Layout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
					jPanel21Layout.rowHeights = new int[] {7, 20, 7, 20, 7};
					jPanel21Layout.columnWeights = new double[] {10, 7, 7, 10};
					jPanel21Layout.columnWidths = new int[] {10, 7, 7, 10};
					jPanel23.setLayout(jPanel21Layout);

					{
						//						buttonGroupGbk = new ButtonGroup();
						//						{
						//							jRadioButtonSelAll = new JRadioButton();
						//							jPanel23.add(jRadioButtonSelAll, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						//							buttonGroupGbk.add(jRadioButtonSelAll);
						//							jRadioButtonSelAll.setText("select all");
						//							jRadioButtonSelAll.setBounds(15, 11, 91, 20);
						//							jRadioButtonSelAll.addActionListener(new ActionListener() {
						//
						//								public void actionPerformed(ActionEvent arg0) {
						//
						//									Rectangle visible = jTable.getVisibleRect();
						//
						//									for(int i=0; i < mainTableData.getTable().size(); i++)
						//										mainTableData.setValueAt(true, i, selectColumnNumber);
						//
						//									for(int i=0; i<jTable.getRowCount();i++)
						//										homologyDataContainer.getSelectedGene().put(Integer.parseInt(homologyDataContainer.getKeys().get(i)), (Boolean)jTable.getValueAt(i, selectColumnNumber));
						//
						//									fillList(visible, true);
						//									jRadioButtonSelAll.setSelected(true);
						//								}
						//							});
						//						}
						//						{
						//							jRadioButtonManSel = new JRadioButton();
						//							jPanel23.add(jRadioButtonManSel, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						//							buttonGroupGbk.add(jRadioButtonManSel);
						//							jRadioButtonManSel.setText("manual selection");
						//							jRadioButtonManSel.setBounds(15, 34, 144, 20);
						//						}
						{
							jLabelLowerThreshold = new JLabel();
							jPanel23.add(jLabelLowerThreshold, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
							jLabelLowerThreshold.setText(homologyDataContainer.getThreshold().toString());
							jLabelLowerThreshold.setToolTipText("this parameter is not editable");
							jLabelLowerThreshold.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED), null));
							jLabelLowerThreshold.setBounds(164, 57, 36, 20);
							jLabelLowerThreshold.setHorizontalAlignment(SwingConstants.CENTER);
							//							jRadioButtonTresh = new JRadioButton();
							//							jPanel23.add(jRadioButtonTresh, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
							//							buttonGroupGbk.add(jRadioButtonTresh);
							//							jRadioButtonTresh.setText("threshold");
							//							jRadioButtonTresh.setBounds(15, 57, 144, 20);
							//							jRadioButtonTresh.setSelected(true);
							//							jRadioButtonTresh.addActionListener(new ActionListener() {
							//
							//								public void actionPerformed(ActionEvent arg0) {
							//
							//									selectThreshold();
							//								}});
						}
						{
							jLabelLowerThresholdText = new JLabel();
							jPanel23.add(jLabelLowerThresholdText, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
							jLabelLowerThresholdText.setText("lower threshold:");
//							jLabelThresholdText.setToolTipText("set threshold");
//							jButtonSetThreshold.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Edit.png")),0.1).resizeImageIcon());
//							jButtonSetThreshold.addActionListener(new ActionListener() {	
//
//								public void actionPerformed(ActionEvent arg0) {
//
//									selectThreshold();
//								}});
						}
						{
							jLabelUpperThreshold = new JLabel();
							jPanel23.add(jLabelUpperThreshold, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
							jLabelUpperThreshold.setText(homologyDataContainer.getUpperThreshold().toString());
							jLabelUpperThreshold.setToolTipText("this parameter is not editable");
							jLabelUpperThreshold.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED), null));
							jLabelUpperThreshold.setBounds(164, 57, 36, 20);
							jLabelUpperThreshold.setHorizontalAlignment(SwingConstants.CENTER);
							//							jRadioButtonTresh = new JRadioButton();
							//							jPanel23.add(jRadioButtonTresh, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
							//							buttonGroupGbk.add(jRadioButtonTresh);
							//							jRadioButtonTresh.setText("threshold");
							//							jRadioButtonTresh.setBounds(15, 57, 144, 20);
							//							jRadioButtonTresh.setSelected(true);
							//							jRadioButtonTresh.addActionListener(new ActionListener() {
							//
							//								public void actionPerformed(ActionEvent arg0) {
							//
							//									selectThreshold();
							//								}});
						}
						{
							jLabelUpperThresholdText = new JLabel();
							jPanel23.add(jLabelUpperThresholdText, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
							jLabelUpperThresholdText.setText("upper threshold:");
//							jLabelThresholdText.setToolTipText("set threshold");
//							jButtonSetThreshold.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Edit.png")),0.1).resizeImageIcon());
//							jButtonSetThreshold.addActionListener(new ActionListener() {	
//
//								public void actionPerformed(ActionEvent arg0) {
//
//									selectThreshold();
//								}});
						}
						
						{
							jLabeldAlpha = new JLabel();
							jPanel23.add(jLabeldAlpha, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
							jLabeldAlpha.setText(this.homologyDataContainer.getAlpha().toString());
							jLabeldAlpha.setToolTipText("this parameter is not editable");
							jLabeldAlpha.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED),null));
							jLabeldAlpha.setBounds(164, 57, 36, 20);
							jLabeldAlpha.setHorizontalAlignment(SwingConstants.CENTER);
							
							jLabelAlphaText = new JLabel();
							jPanel23.add(jLabelAlphaText, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
							jLabelAlphaText.setText("alpha value:");
//							jLabelAlphaText.setToolTipText("enter the alpha value for enzyme selection");
//							jLabelAlphaText.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Edit.png")),0.1).resizeImageIcon());
//							jButtonAlpha.addActionListener(new ActionListener() {
//								public void actionPerformed(ActionEvent arg0) {
//
//									Rectangle visible = jTable.getVisibleRect();
//
//									if(Double.parseDouble(jLabeldAlpha.getText())<0 || Double.parseDouble(jLabeldAlpha.getText())>1) {
//
//										jLabeldAlpha.setText(homologyDataContainer.getAlpha().toString());
//										Workbench.getInstance().warn("the value must be between 0 and 1");
//									}
//									else {
//
//										if(discardData())
//											initialiser();
//
//										homologyDataContainer.setAlpha(Double.parseDouble(jLabeldAlpha.getText()));
//										mainTableData = homologyDataContainer.getAllGenes(blastDatabase, true);
//										jTable.setModel(mainTableData);
//										fillList(visible, true);
//									}
//								}
//							});
						}
						

						//						{
						//							jRadioButtonMETAGENES = new JRadioButton();
						//							jPanel23.add(jRadioButtonMETAGENES, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						//							buttonGroupGbk.add(jRadioButtonMETAGENES);
						//							jRadioButtonMETAGENES.setText("metabolic");
						//							jRadioButtonMETAGENES.setBounds(17, 22, 124, 18);
						//							jRadioButtonMETAGENES.addActionListener(new ActionListener() {
						//
						//								public void actionPerformed(ActionEvent evt) {
						//
						//									Rectangle visible = jTable.getVisibleRect();
						//
						//									for(int row = 0; row < jTable.getRowCount(); row++) {
						//
						//										int key = Integer.parseInt(homologyDataContainer.getKeys().get(row));
						//
						//										boolean is = false;
						//
						//										if(homologyDataContainer.hasCommittedData())
						//											is = homologyDataContainer.getCommittedEcItem().containsKey(key);
						//
						//										if(homologyDataContainer.getInitialEcItem().containsKey(row))
						//											is = homologyDataContainer.getInitialEcItem().containsKey(row) 
						//											&& !homologyDataContainer.getInitialEcItem().get(row).trim().isEmpty();
						//
						//										homologyDataContainer.getSelectedGene().put(key,is);
						//
						//									}
						//									fillList(visible, true);
						//									jRadioButtonMETAGENES.setSelected(true);
						//								}
						//							});
						//						}
					}
				}
				{
					commitPane = new JPanel();
					GridBagLayout commitPaneLayout = new GridBagLayout();
					commitPane.setLayout(commitPaneLayout);
					commitPaneLayout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
					commitPaneLayout.rowHeights = new int[] {7, 7, 7, 20, 7};
					commitPaneLayout.columnWeights = new double[] {0.1};
					commitPaneLayout.columnWidths = new int[] {7};
					jPanel2.add(commitPane, new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					commitPane.setToolTipText("integration");
					commitPane.setBorder(BorderFactory.createTitledBorder("integration"));
					{
						saveButton = new JButton();
						commitPane.add(saveButton, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						saveButton.setText("save");
						saveButton.setToolTipText("save annotation database");
						saveButton.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Save.png")),0.1).resizeImageIcon());
						saveButton.addActionListener(new ActionListener(){

							public void actionPerformed(ActionEvent arg0) {	

								Rectangle visible = jTable.getVisibleRect();

								if(homologyDataContainer.commitToDatabase(blastDatabase)) {
									
									try {
										
										Connection connection = homologyDataContainer.getConnection();
										Statement statement = connection.createStatement();
										
										if(HomologyAPI.hasCommitedData(statement))
											homologyDataContainer.setHasCommittedData();
										
									}
									catch(Exception e) {
										System.out.println("exception message "+e.getMessage());
									}
									
									Workbench.getInstance().info("data successfully loaded into database!");
									fillList(visible);
								}
								else {

									Workbench.getInstance().warn("an error occurred while performing the operation!");
								}
							}});
					}
					{
						jButtonIntegration = new JButton();
						commitPane.add(jButtonIntegration, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jButtonIntegration.setText("integrate to model");
						jButtonIntegration.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Integrate.png")),0.1).resizeImageIcon());
						jButtonIntegration.addActionListener(new ActionListener() {

							public void actionPerformed(ActionEvent arg0) {	

								Rectangle visible = jTable.getVisibleRect();

								if(homologyDataContainer == null || homologyDataContainer.getInitialLocus().size()==0) {

									Workbench.getInstance().error("no homology information on the selected project!");
								}
								else if(!homologyDataContainer.getProject().isMetabolicDataAvailable()) {

									Workbench.getInstance().error("no metabolic information on the selected project!");
								}
								else {

									fillList(visible);

									for (@SuppressWarnings("rawtypes") OperationDefinition def : Core.getInstance().getOperations()){
										if (def.getID().equals("operations.IntegrateHomologyData.ID")){

											Workbench.getInstance().executeOperation(def);
										}
									}

									//									new HomologyIntegrationGui("integrate homology data to database", homologyDataContainer);
								}
							}
						});
					}
				}
				{
					//					jPanel24 = new JPanel();
					//					GridBagLayout jPanel3Layout = new GridBagLayout();
					//					jPanel2.add(jPanel24, new GridBagConstraints(1, 0, 7, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					//					jPanel24.setBorder(BorderFactory.createTitledBorder("Search"));
					//					jPanel3Layout.rowWeights = new double[] {0.0};
					//					jPanel3Layout.rowHeights = new int[] {3};
					//					jPanel3Layout.columnWeights = new double[] {1.1, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.0, 0.1};
					//					jPanel3Layout.columnWidths = new int[] {100, 20, 7, 7, 3, 3, 7, 6, 3, 6};
					//					jPanel24.setLayout(jPanel3Layout);
					//					{
					//						jButtonPrevious = new JButton();
					//						jButtonPrevious .setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Previous.png")),0.05).resizeImageIcon());
					//						jPanel24.add(jButtonPrevious, new GridBagConstraints(4, -1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					//						jButtonPrevious.setToolTipText("Previous");
					//						jButtonPrevious.addActionListener(new ActionListener(){
					//
					//							public void actionPerformed(ActionEvent arg0) {
					//
					//								if(rows.size()>0) {
					//
					//									if(presentRow!=0) {
					//
					//										presentRow-=1;
					//									}
					//									else {
					//
					//										presentRow=rows.size()-1;
					//									}
					//									jTextFieldResult.setText(""+(presentRow+1));
					//									jTable.setRowSelectionInterval(rows.get(presentRow), rows.get(presentRow));
					//									jTable.scrollRectToVisible(jTable.getCellRect(rows.get(presentRow), 0, true));
					//									selectedModelRow = rows.get(presentRow);
					//									homologyDataContainer.setSelectedRow(selectedModelRow);
					//								}
					//							}});
					//					}
					//					{
					//						jButtonNext = new JButton();
					//						jButtonNext .setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Next.png")),0.05).resizeImageIcon());
					//						jPanel24.add(jButtonNext, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					//						jButtonNext.setToolTipText("Next");
					//						jButtonNext.addActionListener(new ActionListener() {
					//
					//							public void actionPerformed(ActionEvent arg0) {
					//
					//								if(rows.size()>0) {
					//
					//									if(presentRow!=rows.size()-1) {
					//
					//										presentRow+=1;
					//									}
					//									else {
					//
					//										Workbench.getInstance().info("The end was reached!\n Starting from the top.");
					//										presentRow=0;
					//									}
					//									jTextFieldResult.setText(""+(presentRow+1));
					//									jTable.setRowSelectionInterval(rows.get(presentRow), rows.get(presentRow));
					//									jTable.scrollRectToVisible(jTable.getCellRect(rows.get(presentRow), 0, true));
					//									selectedModelRow = rows.get(presentRow);
					//									homologyDataContainer.setSelectedRow(selectedModelRow);
					//								}
					//							}});
					//					}
					//					{
					//						jTextFieldResult = new JTextField();
					//						jTextFieldResult.setEditable(false);
					//						jPanel24.add(jTextFieldResult, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					//					}
					//					{
					//						jLabel1 = new JLabel();
					//						jPanel24.add(jLabel1, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					//						jLabel1.setText("of");
					//						jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
					//						jLabel1.setHorizontalTextPosition(SwingConstants.CENTER);
					//					}
					//					{
					//						jTextFieldTotal = new JTextField();
					//						jTextFieldTotal.setEditable(false);
					//						jPanel24.add(jTextFieldTotal, new GridBagConstraints(9, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					//					}
					//					{
					//
					//						searchTextField = new JTextField();
					//						searchTextField.setBounds(14, 12, 604, 20);
					//						searchTextField.addKeyListener(new KeyAdapter() {
					//
					//							@Override
					//							public void keyTyped(KeyEvent evt) {
					//
					//								searchInTable(evt);
					//							}
					//						});
					//
					//						ComboBoxModel<String> searchComboBoxModel = new DefaultComboBoxModel<> (new String[] { "Name", "All" });
					//						searchComboBox = new JComboBox<>();
					//						jPanel24.add(searchComboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					//						jPanel24.add(searchTextField, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					//						//				searchComboBox.setBounds(624, 12, 77, 20);
					//
					//						searchComboBox.setModel(searchComboBoxModel);
					//						searchComboBox.addActionListener(new ActionListener()
					//						{
					//							public void actionPerformed(ActionEvent arg0){
					//								if(searchTextField.getText().compareTo("")!=0)
					//								{
					//									searchInTable(searchTextField.getText());
					//								}
					//							}
					//						});
					//					}
					{
						JPanel searchPanel = searchInHomology.addPanel();
						jPanel2.add(searchPanel, new GridBagConstraints(1, 0, 6, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					}
					{
						JPanel setupPanel = new JPanel();
						GridBagLayout setupPanelPaneLayout = new GridBagLayout();
						setupPanel.setLayout(setupPanelPaneLayout);
						setupPanelPaneLayout.rowWeights = new double[] {0.0, 0.1};
						setupPanelPaneLayout.rowHeights = new int[] {7};
						setupPanelPaneLayout.columnWeights = new double[] {0.1};
						setupPanelPaneLayout.columnWidths = new int[] {7};
						jPanel2.add(setupPanel, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						setupPanel.setBorder(BorderFactory.createTitledBorder("view"));
						{
							String[] databases = null;

							try {

								Connection connection = new Connection(homologyDataContainer.getProject().getDatabase().getDatabaseAccess());

								databases = HomologyAPI.getBlastDatabases(connection.createStatement());

								connection.closeConnection();

							} catch (Exception e) {
								e.printStackTrace();
							}

							JComboBox<String> option = new JComboBox<String>(new DefaultComboBoxModel<>(databases));
							setupPanel.add(option, new GridBagConstraints(0, 0, 0, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
							option.setToolTipText("select database");
							option.setSelectedItem(this.blastDatabase);
							currentDatabaseIndex = option.getSelectedIndex();
							option.addActionListener(new ActionListener() {

								public void actionPerformed(ActionEvent arg0) {

									try {

										if(option.getSelectedIndex() != currentDatabaseIndex) {
											
											currentDatabaseIndex = option.getSelectedIndex();
											
											if(currentDatabaseIndex == 0)
												blastDatabase = "";
											else
												blastDatabase = option.getSelectedItem().toString();

											homologyDataContainer.getCommitedScorerData(blastDatabase);

											if(homologyDataContainer.getCommittedAlpha() > -1) {

												updateSettings(false);

												jLabelLowerThreshold.setText(homologyDataContainer.getThreshold().toString());
												jLabelUpperThreshold.setText(homologyDataContainer.getUpperThreshold().toString());
												jLabeldAlpha.setText(homologyDataContainer.getAlpha().toString());

											}
											else {
												updateSettings(true);
											}

											Connection connection = homologyDataContainer.getConnection();
											Statement statement = connection.createStatement();

											HomologyAPI.setLastestUsedBlastDatabase(statement, blastDatabase);

											updateTableUI();
										}

									} 
									catch (SQLException e) {
										e.printStackTrace();
									}

								}});
						}
					}

				}
				{
					jPanel3 = new JPanel();
					jPanel2.add(jPanel3, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					GridBagLayout jPanel21Layout = new GridBagLayout();
					jPanel3.setBounds(14, 41, 294, 63);
					jPanel3.setBorder(BorderFactory.createTitledBorder("SamPler"));
					jPanel21Layout.rowWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0};
					jPanel21Layout.rowHeights = new int[] {7, 20, 7, 20, 7};
					jPanel21Layout.columnWeights = new double[] {7};
					jPanel21Layout.columnWidths = new int[] {7};
					
//					GridBagLayout jPanel21Layout = new GridBagLayout();
//					jPanel21Layout.rowWeights = new double[] {0.0, 0.0};
//					jPanel21Layout.rowHeights = new int[] {3, 3};
//					jPanel21Layout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1};
//					jPanel21Layout.columnWidths = new int[] {7, 7, 7, 7, 7, 7, 7, 7};
					jPanel3.setLayout(jPanel21Layout);
					//					{
					//						jButton1 = new JButton();
					//						jButton1.setText("blast/hmmer");
					//						jButton1.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Edit.png")),0.1).resizeImageIcon());
					//						jPanel3.add(jButton1, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					//						jButton1.setToolTipText("relative weigth for BLAST and HMMER");
					//						jTextField1 = new JTextField();
					//						jPanel3.add(jTextField1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					//						jTextField1.setText(""+homologyDataContainer.getBlastHmmerWeight());
					//						jTextField1.setToolTipText("relative weigth for BLAST and HMMER");
					//						jTextField1.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED),null));
					//						jTextField1.setBounds(164, 57, 36, 20);
					//						if(homologyDataContainer.isBlastPAvailable() && homologyDataContainer.isHmmerAvailable()) {
					//
					//							jTextField1.setText(""+homologyDataContainer.getBlastHmmerWeight());
					//							jButton1.addActionListener(new ActionListener() {
					//
					//								public void actionPerformed(ActionEvent arg0) {
					//
					//									Rectangle visible = jTable.getVisibleRect();
					//
					//									if(Double.parseDouble(jTextField1.getText())<0 || Double.parseDouble(jTextField1.getText())>1) {
					//										jTextField1.setText(homologyDataContainer.getBlastHmmerWeight().toString());
					//										Workbench.getInstance().warn("the value must be between 0 and 1");
					//									}
					//									else {
					//
					//										if(discardData()){initialiser();}
					//										homologyDataContainer.setBlastHmmerWeight(Double.parseDouble(jTextField1.getText()));
					//										mainTableData = homologyDataContainer.getAllGenes(blastDatabase);
					//										jTable.setModel(mainTableData);
					//										fillList(visible,true);
					//									}
					//								}
					//							});
					//						}
					//						else {
					//
					//							jTextField1.setEnabled(false);
					//							jButton1.setEnabled(false);
					//						}
					//					}
					//					{
					//						jTextField2 = new JTextField();
					//						jPanel3.add(jTextField2, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					//						jTextField2.setText(""+this.homologyDataContainer.getBeta());
					//						jTextField2.setToolTipText("Enter the beta value for enzyme selection");
					//						jTextField2.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED),null));
					//						jTextField2.setBounds(164, 57, 36, 20);
					//						jButtonBeta = new JButton();
					//						jPanel3.add(jButtonBeta, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					//						jButtonBeta.setText("beta value");
					//						jButtonBeta.setToolTipText("Enter the beta value for enzyme selection");
					//						jButtonBeta.addActionListener(new ActionListener() {
					//							public void actionPerformed(ActionEvent arg0) {
					//
					//								Rectangle visible = jTable.getVisibleRect();
					//
					//								if(Double.parseDouble(jTextField2.getText())<0 || Double.parseDouble(jTextField2.getText())>1) {
					//
					//									jTextField2.setText(homologyDataContainer.getBeta().toString());
					//									Workbench.getInstance().warn("The value must be between 0 and 1");
					//								}
					//								else {
					//									if( Double.parseDouble(jTextField2.getText())> new Double(1.0/new Double(homologyDataContainer.getMinimumNumberofHits()))) {
					//										jTextField2.setText(homologyDataContainer.getBeta()+"");
					//										Workbench.getInstance().warn("The maximum beta value for "+homologyDataContainer.getMinimumNumberofHits()+" minimum number of hits cannot be higher than "+new Double(1.0/new Double(homologyDataContainer.getMinimumNumberofHits()))+".");
					//									}
					//									else {
					//										if(discardData()){initialiser();}
					//										homologyDataContainer.setBeta(Double.parseDouble(jTextField2.getText()));
					//										mainTableData = homologyDataContainer.getAllGenes();
					//										jTable.setModel(mainTableData);
					//										fillList(visible, true);
					//									}
					//								}
					//							}
					//						});
					//
					//					}
					//					{
					//						jTextField3 = new JTextField();
					//						jPanel3.add(jTextField3, new GridBagConstraints(4, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					//						jTextField3.setText(""+this.homologyDataContainer.getMinimumNumberofHits());
					//						jTextField3.setToolTipText("Enter the minimum number of required hits for enzyme selection");
					//						jTextField3.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEtchedBorder(BevelBorder.LOWERED),null));
					//						jTextField3.setBounds(164, 57, 36, 20);
					//						jButtonMinHits = new JButton();
					//						jPanel3.add(jButtonMinHits, new GridBagConstraints(5, 3, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					//						jButtonMinHits.setText("Min Homologies");
					//						jButtonMinHits.setToolTipText("Enter the minimum number of required Homologies for enzyme selection");
					//						jButtonMinHits.addActionListener(new ActionListener() {
					//							public void actionPerformed(ActionEvent arg0) {
					//
					//								Rectangle visible = jTable.getVisibleRect();
					//
					//								if(Double.parseDouble(jTextField3.getText())<0 || Double.parseDouble(jTextField3.getText())%1!=0) {
					//									jTextField3.setText(homologyDataContainer.getMinimumNumberofHits()+"");
					//									Workbench.getInstance().warn("The value has to be a positive Integer");
					//								}
					//								else {
					//									if( Double.parseDouble(jTextField3.getText())> (1.0/new Double(homologyDataContainer.getBeta()))) {
					//										jTextField3.setText(homologyDataContainer.getMinimumNumberofHits()+"");
					//										Workbench.getInstance().warn("The minimum number of hits for a beta value of "+homologyDataContainer.getBeta()+" cannot be higher than "+(1.0/new Double(homologyDataContainer.getBeta()))+".");
					//									}
					//									else {
					//
					//										if(discardData()) {
					//
					//											initialiser();
					//										}
					//
					//										homologyDataContainer.setMinimumNumberofHits(Integer.parseInt(jTextField3.getText()));
					//										mainTableData = homologyDataContainer.getAllGenes();
					//										jTable.setModel(mainTableData);
					//										fillList(visible, true);
					//									}
					//								}
					//							}
					//						});
					//					}
					{
						jButtonReset = new JButton();
						jPanel3.add(jButtonReset, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jButtonReset.setText("reset");
						jButtonReset.setToolTipText("resets the scorer configurations");
						jButtonReset.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Synchronize.png")),0.1).resizeImageIcon());
						jButtonReset.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {

								if(resetScorer(blastDatabase)) {
									
									updateSettings(true);
									
									updateTableUI();

									Workbench.getInstance().info("parameters successfully reset!");
								}
								
							}
						});
					}
					
					{
						jButtonAnnotation = new JButton();
						jPanel3.add(jButtonAnnotation, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jButtonAnnotation.setText("selection");
						jButtonAnnotation.setToolTipText("find best parameters for enzymes annotation");
						jButtonAnnotation.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Synchronize.png")),0.1).resizeImageIcon());
						jButtonAnnotation.addActionListener(new ActionListener() {

							public void actionPerformed(ActionEvent arg0) {

								//									double currentAlpha = homologyDataContainer.getAlpha();
								//									double currentThreshold = homologyDataContainer.getThreshold();

								try {
									ParamSpec[] paramsSpec = new ParamSpec[]{
											new ParamSpec("locusTagColumnNumber", Integer.class, locus_tagColumnNumber, null),
											new ParamSpec("homologyDataContainer", EnzymesAnnotationDataInterface.class, homologyDataContainer, null),
											new ParamSpec("ecnumbersColumnNumber", Integer.class, ecnumbersColumnNumber, null),
											new ParamSpec("ecScoreColumnNumber", Integer.class, ecScoreColumnNumber, null),
											new ParamSpec("sampleSize", Integer.class, 50, null),
											new ParamSpec("itemsList", Map.class, itemsList.get(1), null),
											new ParamSpec("blastDatabase", String.class, blastDatabase, null),
											new ParamSpec("searchFile", Boolean.class, true, null),
									};

									for (@SuppressWarnings("rawtypes") OperationDefinition def : Core.getInstance().getOperations()){
										if (def.getID().equals("operations.EnzymesAnnotationParametersSetting.ID")){

											Workbench.getInstance().executeOperation(def, paramsSpec);
										}
									}

								} catch (IllegalArgumentException e) {
									e.printStackTrace();
								}

								//									double bestAlpha = homologyDataContainer.getAlpha();
								//									double bestThreshold = homologyDataContainer.getThreshold();

								//									System.out.println(currentAlpha);
								//									System.out.println(bestAlpha);
								//									
								//									if (currentAlpha != bestAlpha || currentThreshold != bestThreshold){
								//										
								//										System.out.println("yohh");
								//										
								//										Rectangle visible = jTable.getVisibleRect();
								//										jTextFieldThreshold.setText(""+ bestThreshold);
								//										jTextFieldAlpha.setText(" "+bestAlpha);
								//										
								//										mainTableData = homologyDataContainer.getAllGenes(blastDatabase, false);
								//										jTable.setModel(mainTableData);
								//										fillList(visible, true);
								//									}
								//									

							}});
					}
				}
			}
			
			selectedModelRow=this.homologyDataContainer.getSelectedRow();

			jTable = new MyJTable();
			jTable.setModel(mainTableData);
			jScrollPane.setViewportView(jTable);
			//			jTable.setAutoCreateRowSorter(true);

			checkButtonsStatus(null);

		}
		catch(Exception e){

			e.printStackTrace();
		}
	}

	/**
	 *	fill entities lists 
	 * @param visible
	 * @param userInput
	 */
	public void fillList(Rectangle visible) {

		try {

			this.intitialiseTableColumns();

			this.selectedModelRow = this.homologyDataContainer.getSelectedRow();

			int myRow = this.selectedModelRow;

			jTable= new MyJTable();
			jTable.setShowGrid(false);
			jTable.setModel(mainTableData);
			itemsList = this.updateData();
			List<Integer> interProRows = this.homologyDataContainer.getInterProRows();

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			this.addListeners();

			{
				this.buttonColumn = this.buildButtonColumn(this.infoColumnNumber, interProRows);

				//if(this.getUniprotECnumbersTable == null) 
				{

					this.getUniprotECnumbersTable = this.getUniprotECnumbersTable(itemsList.get(2));
				}
				this.buttonStarColumn = this.buildStarColumn(this.uniprotStarColumnNumber, this.compareAnnotations(itemsList.get(1)));
				this.productsColumn = this.buildComboBoxColumn(this.namesColumnNumber, itemsList.get(0));
				this.enzymesColumn = this.buildComboBoxColumn(this.ecnumbersColumnNumber, itemsList.get(1));
			}

			this.jScrollPane.setViewportView(jTable);

			if(visible!=null) {

				//this.selectedRow = this.homologyDataContainer.getSelectedRow();

				this.jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

				if(myRow>-1 && jTable.getRowCount()>myRow)
					this.jTable.setRowSelectionInterval(myRow, myRow);

				scrollToVisible(visible);
			}

			this.setTableColumunModels();

			this.searchInHomology.setMyJTable(jTable);
			this.searchInHomology.setMainTableData(mainTableData);
			this.searchInHomology.setSearchTextField("");

		}
		catch (Exception e) {

			e.printStackTrace();
		}

	}

	//	/**
	//	 * @param evt
	//	 */
	//	public void searchInTable(KeyEvent evt) {
	//
	//		String text;
	//
	//		if(searchTextField.getText().compareTo("")!=0 && evt.getKeyChar() == KeyEvent.VK_BACK_SPACE) {
	//
	//			text = searchTextField.getText();
	//		}
	//		else {
	//
	//			text = searchTextField.getText()+evt.getKeyChar();
	//		}
	//
	//		searchInTable(text);
	//
	//	}

	/**
	 * @param text
	 */
	//	private void searchInTable(String text) {
	//
	//		this.rows = new ArrayList<Integer>();
	//		Set<Integer> rows = new TreeSet<Integer>();
	//		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
	//		int i=0;
	//		this.presentRow = 0;
	//		ArrayList<Object[]> tab = this.mainTableData.getTable();
	//
	//		switch(this.searchComboBox.getSelectedIndex())
	//		{
	//		case 0:
	//		{
	//			for(int z=0;z<tab.size();z++) {
	//
	//				Object[] subtab = tab.get(i);
	//				if(((String)subtab[locus_tagColumnNumber]) != null &&  ((String)subtab[locus_tagColumnNumber]).contains(text)) {
	//
	//					int modelRow = new Integer(z);
	//					rows.add(modelRow);
	//				}
	//
	//				if(((String)subtab[geneNameColumnNumber]) != null && ((String)subtab[geneNameColumnNumber]).contains(text)) {
	//
	//					int modelRow = new Integer(i);
	//					rows.add(modelRow);
	//				}
	//
	//				i++;
	//			}
	//			break;
	//		}
	//		case 1:
	//		{
	//			for(int z=0;z<tab.size();z++) {
	//
	//				Object[] subtab = tab.get(i);
	//
	//				List<String> product = new ArrayList<String>();
	//				List<String> ecnumber = new ArrayList<String>();
	//
	//				if(((String[])subtab[namesColumnNumber]) != null && ((String[])subtab[namesColumnNumber]).length>0) {
	//
	//					product.addAll(Arrays.asList(((String[])subtab[namesColumnNumber])));
	//				}
	//
	//				for(String s: product) {
	//
	//					if(s.contains(text)) {
	//
	//						int modelRow = new Integer(i);
	//						rows.add(modelRow);
	//					}
	//				}
	//
	//				if(((String[])subtab[ecnumbersColumnNumber]) != null && ((String[])subtab[ecnumbersColumnNumber]).length>0) {
	//
	//					ecnumber.addAll(Arrays.asList(((String[])subtab[ecnumbersColumnNumber])));
	//				}
	//
	//				for(String s: ecnumber) {
	//
	//					if(s.contains(text)) {
	//
	//						int modelRow = new Integer(i);
	//						rows.add(modelRow);
	//					}
	//				}
	//
	//				if(((String)subtab[locus_tagColumnNumber]) != null && ((String)subtab[locus_tagColumnNumber]).contains(text)) {
	//
	//					int modelRow = new Integer(i);
	//					rows.add(modelRow);
	//				}
	//
	//				if(((String)subtab[geneNameColumnNumber]) != null && ((String)subtab[geneNameColumnNumber]).contains(text)) {
	//
	//					int modelRow = new Integer(i);
	//					rows.add(modelRow);
	//				}
	//
	//				if(((String)subtab[notesColumnNumber]) != null && ((String)subtab[notesColumnNumber]).contains(text)) {
	//
	//					int modelRow = new Integer(i);
	//					rows.add(modelRow);
	//				}
	//
	//				if(homologyDataContainer.isEukaryote() && ((String)subtab[chromosomeColumnNumber]) != null && ((String)subtab[chromosomeColumnNumber]).contains(text)) {
	//
	//					int modelRow = new Integer(i);
	//					rows.add(modelRow);
	//				}
	//
	//				i++;
	//			}
	//			break;
	//		}
	//		default:
	//		{
	//			for(int z=0;z<tab.size();z++) {
	//
	//				Object[] subtab = tab.get(i);
	//				if(((String)subtab[locus_tagColumnNumber]) != null && ((String) subtab[locus_tagColumnNumber]).contains(text)) {
	//					rows.add(new Integer(i));
	//				}
	//
	//				if(((String)subtab[geneNameColumnNumber]) != null && ((String) subtab[geneNameColumnNumber]).contains(text)) {
	//					rows.add(new Integer(i));
	//				}
	//				i++;
	//			}
	//			break;
	//		}
	//
	//		}
	//		this.rows.addAll(rows);
	//
	//		int row = 0;
	//		for(Integer r: this.rows) {
	//
	//			row = r.intValue();
	//			selectionModel.addSelectionInterval(row, row);
	//		}
	//
	//
	//		this.jTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	//		this.jTable.setSelectionModel(selectionModel);
	//		if(selectionModel.isSelectionEmpty()&& (this.searchTextField.getText().compareTo("")!=0)) {
	//
	//			this.searchTextField.setForeground(new java.awt.Color(255,0,0));
	//			searchTextField.setBackground(new java.awt.Color(174,174,174));
	//			this.jTextFieldResult.setText("");
	//			this.jTextFieldTotal.setText("");
	//			this.rows = new ArrayList<Integer>();
	//		}
	//		else {
	//
	//			this.searchTextField.setForeground(Color.BLACK);
	//			this.searchTextField.setBackground(Color.WHITE);
	//		}
	//
	//		if(this.rows.size()!=0) {
	//
	//			jTextFieldResult.setText(""+1);
	//			jTextFieldTotal.setText(""+this.rows.size());
	//			this.scrollToVisible(this.jTable.getCellRect(this.rows.get(0), 0, true));
	//		}
	//	}

	/**
	 * update data lists 
	 * @return 
	 */
	private List<Map<Integer,String>> updateData() {

		try {

			if(this.homologyDataContainer.hasCommittedData()) {

				this.homologyDataContainer.getCommittedHomologyData();
				//				if(!userInput)
				//					this.jRadioButtonManSel.setSelected(true);
			}


			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			// commited product list
			if(this.homologyDataContainer.getCommittedProductList()!= null)
				for(int row : this.homologyDataContainer.getCommittedProductList().keySet())
					this.jTable.setValueAt(homologyDataContainer.getCommittedProductList().get(row), row, this.namesColumnNumber);

			//container product list
			for(int key : this.homologyDataContainer.getEditedProductData().keySet()) {

				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

					int row = homologyDataContainer.getReverseKeys().get(key);
					this.jTable.setValueAt(homologyDataContainer.getEditedProductData().get(key), row, this.namesColumnNumber);
				}
			}

			///////////////////////////////////////////////////////////////////////////////////////////////////////

			// commited product item
			Map<Integer, String> mappedProdItem = this.homologyDataContainer.getInitialProdItem();

			if(this.homologyDataContainer.getCommittedProdItem()!= null) {

				for(int row : this.homologyDataContainer.getCommittedProdItem().keySet()) {

					if(this.homologyDataContainer.getCommittedProdItem().get(row)!=null && !this.homologyDataContainer.getCommittedProdItem().get(row).equalsIgnoreCase("null")) {

						mappedProdItem.put(row, this.homologyDataContainer.getCommittedProdItem().get(row));

						if(!homologyDataContainer.getProductList().containsKey(Integer.parseInt(this.homologyDataContainer.getKeys().get(row)))) {

							this.homologyDataContainer.getProductList().put(Integer.parseInt(this.homologyDataContainer.getKeys().get(row)),mappedProdItem.get(row));
						}

					}
				}
			}

			// score 
			for(int key : this.homologyDataContainer.getProductList().keySet()) {

				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

					int row = homologyDataContainer.getReverseKeys().get(key);

					mappedProdItem.put(row,this.homologyDataContainer.getProductList().get(key));

					String pdWeigth = this.homologyDataContainer.getProductPercentage(mappedProdItem.get(row), row);

					this.jTable.setValueAt(pdWeigth, row, this.namesScoreColumnNumber);

					this.homologyDataContainer.getProductList().put(key,mappedProdItem.get(row));
				}
			}

			//			for(int row :mappedProdItem.keySet()) {
			//				
			//				if(mappedProdItem.get(row) != null) {
			//					
			//					this.homologyDataContainer.getProductList().put(Integer.parseInt(this.homologyDataContainer.getKeys().get(row)),mappedProdItem.get(row));
			//				}
			//			}

			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			if(this.homologyDataContainer.getCommittedEnzymeList()!= null) {

				for(int row : this.homologyDataContainer.getCommittedEnzymeList().keySet()) {

					this.jTable.setValueAt(this.homologyDataContainer.getCommittedEnzymeList().get(row), row, this.ecnumbersColumnNumber);
				}
			}

			for(int key : this.homologyDataContainer.getEditedEnzymeData().keySet()) {

				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

					int row = homologyDataContainer.getReverseKeys().get(key);
					this.jTable.setValueAt(this.homologyDataContainer.getEditedEnzymeData().get(key), row, this.ecnumbersColumnNumber);

				}
			}


			Map<Integer, String> mappedEcItem = this.homologyDataContainer.getInitialEcItem();

			if(this.homologyDataContainer.getCommittedEcItem()!= null) {

				for(int row : this.homologyDataContainer.getCommittedEcItem().keySet()) {

					if(this.homologyDataContainer.getCommittedEcItem().get(row)!=null && !this.homologyDataContainer.getCommittedEcItem().get(row).equalsIgnoreCase("null")) {

						mappedEcItem.put(row, this.homologyDataContainer.getCommittedEcItem().get(row));

						int key = Integer.parseInt(this.homologyDataContainer.getKeys().get(row));

						if(!homologyDataContainer.getEnzymesList().containsKey(key))
							this.homologyDataContainer.getEnzymesList().put(key,mappedEcItem.get(row));
					}
				}
			}

			for(int key : this.homologyDataContainer.getEnzymesList().keySet()) {
				
				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

					int row = homologyDataContainer.getReverseKeys().get(key);

					mappedEcItem.put(row,this.homologyDataContainer.getEnzymesList().get(key));

					String ecWeigth = this.homologyDataContainer.getECPercentage(mappedEcItem.get(row), row);
					
//					if(!ecWeigth.equalsIgnoreCase("manual") && !ecWeigth.isEmpty() && Double.parseDouble(ecWeigth) < this.homologyDataContainer.getThreshold()) {
//
//						ecWeigth = "<"+this.homologyDataContainer.getThreshold();
//					}
					this.jTable.setValueAt(ecWeigth, row, this.ecScoreColumnNumber);

					this.homologyDataContainer.getEnzymesList().put(key,mappedEcItem.get(row));
				}
			}

			//			for(int row :mappedEcItem.keySet()) {
			//				
			//				if(mappedEcItem.get(row) != null) {
			//					
			//					this.homologyDataContainer.getEnzymesList().put(Integer.parseInt(this.homologyDataContainer.getKeys().get(row)),mappedEcItem.get(row));
			//				}
			//			}


			//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

			Map<Integer, String> mappedLocusList = this.homologyDataContainer.getInitialLocus();

			if(this.homologyDataContainer.getCommittedLocusList() != null && jTable.getRowCount()>0) {

				for(int row : this.homologyDataContainer.getCommittedLocusList().keySet()) {

					if(this.homologyDataContainer.getCommittedLocusList().get(row)!=null && !this.homologyDataContainer.getCommittedLocusList().get(row).equalsIgnoreCase("null")) {

						mappedLocusList.put(row, this.homologyDataContainer.getCommittedLocusList().get(row));
						this.jTable.setValueAt(mappedLocusList.get(row), row, this.locus_tagColumnNumber);
					}
				}
			}

			for(int key : this.homologyDataContainer.getLocusList().keySet()) {

				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

					int row = homologyDataContainer.getReverseKeys().get(key);
					mappedLocusList.put(row,this.homologyDataContainer.getLocusList().get(key));
					this.jTable.setValueAt(mappedLocusList.get(row), row, this.locus_tagColumnNumber);
				}
			}

			Map<Integer, String> mappedNamesList = this.homologyDataContainer.getInitialNames();

			if(this.homologyDataContainer.getCommittedNamesList() != null && jTable.getRowCount()>0) {

				for(int row : this.homologyDataContainer.getCommittedNamesList().keySet()) {

					if(this.homologyDataContainer.getCommittedNamesList().get(row)!=null && !this.homologyDataContainer.getCommittedNamesList().get(row).equalsIgnoreCase("null")) {

						mappedNamesList.put(row, this.homologyDataContainer.getCommittedNamesList().get(row));
						this.jTable.setValueAt(mappedNamesList.get(row), row, this.geneNameColumnNumber);
					}
				}
			}

			for(int key : this.homologyDataContainer.getNamesList().keySet()) {

				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

					int row = homologyDataContainer.getReverseKeys().get(key);

					mappedNamesList.put(row,this.homologyDataContainer.getNamesList().get(key));

					this.jTable.setValueAt(mappedNamesList.get(row), row, this.geneNameColumnNumber);
				}
			}

			Map<Integer, String> mappedNotesMap = new TreeMap<Integer, String>();

			if(this.homologyDataContainer.getCommittedNotesMap() != null && jTable.getRowCount()>0) {

				for(int row : this.homologyDataContainer.getCommittedNotesMap().keySet()) {

					if(this.homologyDataContainer.getCommittedNotesMap().get(row) != null) {

						mappedNotesMap.put(row, this.homologyDataContainer.getCommittedNotesMap().get(row));
						this.jTable.setValueAt(mappedNotesMap.get(row), row, this.notesColumnNumber);
					}
				}
			}

			for(int key : this.homologyDataContainer.getNotesMap().keySet()) {

				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {
					int row = homologyDataContainer.getReverseKeys().get(key);

					mappedNotesMap.put(row,this.homologyDataContainer.getNotesMap().get(key));
					this.jTable.setValueAt(mappedNotesMap.get(row), row, this.notesColumnNumber);
				}
			}

			Map<Integer, String> mappedChromosome = null ;
			if(homologyDataContainer.isEukaryote()) {

				mappedChromosome = this.homologyDataContainer.getInitialChromosome();

				if(this.homologyDataContainer.getCommittedChromosome() != null) {

					for(int row : this.homologyDataContainer.getCommittedChromosome().keySet()) {

						if(this.homologyDataContainer.getCommittedChromosome().get(row) != null) {

							mappedChromosome.put(row, this.homologyDataContainer.getCommittedChromosome().get(row));
							this.jTable.setValueAt(mappedChromosome.get(row), row, this.chromosomeColumnNumber);
						}
					}
				}

				for(int key : this.homologyDataContainer.getChromosome().keySet()) {

					if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {

						int row = homologyDataContainer.getReverseKeys().get(key);
						mappedChromosome.put(row,this.homologyDataContainer.getChromosome().get(key));
						this.jTable.setValueAt(mappedChromosome.get(row), row, this.chromosomeColumnNumber);
					}
				}
			}

			//			Map<Integer, Boolean> mappedSelectedGene = this.homologyDataContainer.getInitialSelectedGene();
			//			if(this.homologyDataContainer.getCommittedSelected() != null && jTable.getRowCount()>0) {
			//
			//				for(int row : this.homologyDataContainer.getCommittedSelected().keySet()) {
			//
			//					if(this.homologyDataContainer.getCommittedSelected().get(row) != null && !userInput) {
			//
			//						mappedSelectedGene.put(row,this.homologyDataContainer.getCommittedSelected().get(row));
			//						this.jTable.setValueAt(mappedSelectedGene.get(row), row, this.selectColumnNumber);
			//					}
			//				}
			//			}

			//			for(int key : this.homologyDataContainer.getSelectedGene().keySet()) {
			//
			//				if(this.homologyDataContainer.getReverseKeys().containsKey(key)) {
			//
			//					int row = homologyDataContainer.getReverseKeys().get(key);
			//					mappedSelectedGene.put(row,this.homologyDataContainer.getSelectedGene().get(key));
			//					this.jTable.setValueAt(mappedSelectedGene.get(row), row, this.selectColumnNumber);
			//				}
			//			}

			// just to be sure that all selected or unselected genes are mapped
			//			for(int row=0; row < jTable.getRowCount(); row++) {
			//
			//				int key =Integer.parseInt(homologyDataContainer.getKeys().get(row)) ;
			//				mappedSelectedGene.put(row,this.homologyDataContainer.getSelectedGene().get(key));
			//
			//				this.homologyDataContainer.getSelectedGene().put(key, (Boolean)this.jTable.getValueAt(row,this.selectColumnNumber));
			//			}

			// prepate items for integration
			{
				this.homologyDataContainer.setIntegrationLocusList(mappedLocusList);
				this.homologyDataContainer.setIntegrationNamesList(mappedNamesList);
				this.homologyDataContainer.setIntegrationProdItem(mappedProdItem);
				this.homologyDataContainer.setIntegrationEcItem(mappedEcItem);
				//				this.homologyDataContainer.setIntegrationSelectedGene(mappedSelectedGene);
				if(homologyDataContainer.isEukaryote())
					this.homologyDataContainer.setIntegrationChromosome(mappedChromosome);
			}

			List<Map<Integer, String>> object = new ArrayList<Map<Integer,String>>();
			object.add(0,mappedProdItem);
			object.add(1,mappedEcItem);
			object.add(2,mappedLocusList);

			return object;
		}
		catch (Exception e) {

			this.jTable.setModel(this.mainTableData);
			this.selectedModelRow=-1;
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 */
	private void addMouseListener() {

		if(this.tableMouseAdapator==null)
			this.tableMouseAdapator = this.getTableMouseAdapator();

		jTable.addMouseListener(this.tableMouseAdapator);

	}

	/**
	 * @return
	 */
	private MouseAdapter getTableMouseAdapator() {

		MouseAdapter mouseAdapter = new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent arg0) {

				selectedModelRow=jTable.getSelectedRow();
				homologyDataContainer.setSelectedRow(selectedModelRow);

				if(jTable.getSelectedRow()>-1 && jTable.getRowCount()>0 && jTable.getRowCount()> jTable.getSelectedRow()) {

					jTable.setRowSelectionInterval(jTable.getSelectedRow(), jTable.getSelectedRow());
					scrollToVisible(jTable.getCellRect(jTable.getSelectedRow(), -1, true));
				}

				//jTextFieldTotal.setText(""); //searchTextField.setText("");jTextFieldResult.setText("");//rows = new ArrayList<Integer>();

				int selectedColumn=-1;
				{
					Point p = arg0.getPoint();
					int  columnNumber = jTable.columnAtPoint(p);
					jTable.setColumnSelectionInterval(columnNumber, columnNumber);
					selectedColumn=columnNumber;
				}

				//EC number popup
				if(selectedColumn==(new Integer(ecScoreColumnNumber))) {

					selectedModelRow=jTable.convertRowIndexToModel(jTable.getSelectedRow());
					homologyDataContainer.setSelectedRow(selectedModelRow);

					new InsertRemoveDataWindow(homologyDataContainer, selectedModelRow ,"Scores description", true) {

						private static final long serialVersionUID = -1;
						public void finishClose() {

							Rectangle visible = jTable.getVisibleRect();

							this.setVisible(false);
							this.dispose();
							//							homologyDataContainer.getSelectedGene().put(Integer.parseInt(homologyDataContainer.getKeys().get(selectedModelRow)), true);
							fillList(visible);
						}						
					};
				}

				// products popup
				if(selectedColumn==namesScoreColumnNumber) {

					selectedModelRow=jTable.convertRowIndexToModel(jTable.getSelectedRow());
					homologyDataContainer.setSelectedRow(selectedModelRow);

					new InsertRemoveDataWindow(homologyDataContainer, selectedModelRow, "Scores description", false) {

						private static final long serialVersionUID = -1;

						public void finishClose() {

							Rectangle visible = jTable.getVisibleRect();

							this.setVisible(false);
							this.dispose();
							//							homologyDataContainer.getSelectedGene().put(Integer.parseInt(homologyDataContainer.getKeys().get(selectedModelRow)), true);
							fillList(visible);
						}						
					};
				}

				//genes Linkout
				if(selectedColumn==locus_tagColumnNumber || selectedColumn==geneNameColumnNumber) {

					if(arg0.getButton()==MouseEvent.BUTTON3 && jTable.getSelectedRow()>0) {

						List<Integer> dbs = new ArrayList<Integer>();
						dbs.add(0);
						dbs.add(1);
						new LinkOut(dbs, (String)jTable.getValueAt(jTable.getSelectedRow(), selectedColumn)).show(arg0.getComponent(),arg0.getX(), arg0.getY());
					}
				}

				//proteins linkout
				if(selectedColumn==namesColumnNumber || selectedColumn==ecnumbersColumnNumber) {

					if(arg0.getButton()==MouseEvent.BUTTON3 && jTable.getSelectedRow()>0) {

						List<Integer> dbs = new ArrayList<Integer>();

						String text=null;
						if(selectedColumn==namesColumnNumber) {

							dbs.add(1);
							dbs.add(2);
							text=productsColumn.getSelectItem(selectedModelRow);
						}

						if(selectedColumn==ecnumbersColumnNumber) {
							dbs.add(1);
							dbs.add(3);
							text=enzymesColumn.getSelectItem(jTable.getSelectedRow());
						}

						if(text!=null) {

							new LinkOut(dbs, text).show(arg0.getComponent(),arg0.getX(), arg0.getY());
						}
					}
				}
			}
		};
		return mouseAdapter;
	}

	/**
	 * 
	 */
	private void addTableModelListener() {

		if(this.tableModelListener == null)
			this.tableModelListener = this.getTableModelListener();

		jTable.getModel().addTableModelListener(this.tableModelListener);

	}

	/**
	 * @return
	 */
	private TableModelListener getTableModelListener() {

		TableModelListener tableModelListener = new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {

				if(jTable.getSelectedRow()>-1) {

					selectedModelRow=jTable.getSelectedRow();
					homologyDataContainer.setSelectedRow(selectedModelRow);
					int key = Integer.parseInt(homologyDataContainer.getKeys().get(jTable.getSelectedRow()));
					homologyDataContainer.getLocusList().put(key , (String) jTable.getValueAt(jTable.getSelectedRow(), locus_tagColumnNumber));
					homologyDataContainer.getNamesList().put(key , (String) jTable.getValueAt(jTable.getSelectedRow() , geneNameColumnNumber));
					homologyDataContainer.getNotesMap().put(key , (String) jTable.getValueAt(jTable.getSelectedRow(), notesColumnNumber));

					if(homologyDataContainer.isEukaryote())
						homologyDataContainer.getChromosome().put(key , (String) jTable.getValueAt(jTable.getSelectedRow(), chromosomeColumnNumber));

					//					if(e.getFirstRow()!= e.getLastRow()) {
					//
					//						for(int i=0;i<jTable.getRowCount();i++)
					//							homologyDataContainer.getSelectedGene().put(Integer.parseInt(homologyDataContainer.getKeys().get(i)), (Boolean) jTable.getValueAt(i, selectColumnNumber));
					//					}
					//					else {
					//
					//						homologyDataContainer.getSelectedGene().put(Integer.parseInt(homologyDataContainer.getKeys().get(e.getFirstRow())), (Boolean) jTable.getValueAt(e.getFirstRow(), selectColumnNumber));
					//					}
				}
			}};
			return tableModelListener;
	}

	/**
	 * 
	 */
	private void setTableColumunModels() {

		TableColumnModel tc = jTable.getColumnModel();
		tc.getColumn(infoColumnNumber).setMaxWidth(35);				
		tc.getColumn(infoColumnNumber).setResizable(false);
		tc.getColumn(infoColumnNumber).setModelIndex(infoColumnNumber);

		tc.getColumn(uniprotStarColumnNumber).setMaxWidth(50);				
		tc.getColumn(uniprotStarColumnNumber).setResizable(false);
		tc.getColumn(uniprotStarColumnNumber).setModelIndex(uniprotStarColumnNumber);

		tc.getColumn(locus_tagColumnNumber).setMinWidth(120);
		tc.getColumn(locus_tagColumnNumber).setModelIndex(locus_tagColumnNumber);

		tc.getColumn(geneNameColumnNumber).setMinWidth(100);
		tc.getColumn(geneNameColumnNumber).setModelIndex(geneNameColumnNumber);

		if(this.homologyDataContainer.isEukaryote()) {

			tc.getColumn(chromosomeColumnNumber).setMinWidth(100);
			tc.getColumn(chromosomeColumnNumber).setModelIndex(chromosomeColumnNumber);
		}

		tc.getColumn(namesColumnNumber).setMinWidth(210);
		tc.getColumn(namesColumnNumber).setModelIndex(namesColumnNumber);

		tc.getColumn(namesScoreColumnNumber).setMinWidth(90);
		tc.getColumn(namesScoreColumnNumber).setMaxWidth(120);
		tc.getColumn(namesScoreColumnNumber).setModelIndex(namesScoreColumnNumber);

		tc.getColumn(ecnumbersColumnNumber).setMinWidth(135);
		tc.getColumn(ecnumbersColumnNumber).setModelIndex(ecnumbersColumnNumber);

		tc.getColumn(ecScoreColumnNumber).setMinWidth(90);
		tc.getColumn(ecScoreColumnNumber).setMaxWidth(120);
		tc.getColumn(ecScoreColumnNumber).setModelIndex(ecScoreColumnNumber);

		tc.getColumn(notesColumnNumber).setResizable(true);
		tc.getColumn(notesColumnNumber).setModelIndex(notesColumnNumber);


		//		tc.getColumn(selectColumnNumber).setPreferredWidth(75);		
		//		tc.getColumn(selectColumnNumber).setResizable(true);
		//		tc.getColumn(selectColumnNumber).setModelIndex(selectColumnNumber);

		jTable.setColumnModel(tc);
		jTable.setRowHeight(20);
		jTable.setAutoResizeMode(MyJTable.AUTO_RESIZE_ALL_COLUMNS);
	}

	/**
	 * @return
	 */
	@SuppressWarnings("unused")
	private boolean discardData() {

		int i =CustomGUI.stopQuestion("Discard manually selected data?",
				"Do you wish to discard all the edited information?" +
						"\n(If you select 'No' edited data will available for later use)",
						new String[]{"Yes", "No", "Info"});
		if(i<2)
		{
			switch (i)
			{
			case 0:return true;
			default:return false;
			}
		}
		else
		{
			Workbench.getInstance().warn(
					"If you discard the edited information, all previously selected genes, enzymes and gene products" +
							"\nwill be returned their default alpha values, as well as edited gene names, identifiers (locus tag)" +
							"\nand chromosomes (if available)."+
							"\nUser inserted data such as ec numbers or product names unavailable on BLAST for a certain gene," +
							"\nwill also be discarded, as well as deleted ec numbers and products."+
							"\nIf you do not discard the edited data, you can revert to your previously reviewed information" +
					"\nwhen selecting the 'Manual Selection'.");
			return discardData();
		}
	}

	/**
	 * @return
	 */
	private boolean resetScorer(String blastDatabase) {

		int i;

		if(blastDatabase.equals("")) {
			i =CustomGUI.stopQuestion("Reset", 
					"Reset score configurations for this database or for all databases?", 
					new String[]{"Yes for all", "Yes", "No"});

		}
		else {
			i =CustomGUI.stopQuestion("Reset", 
					"Reset score just for " + blastDatabase +" or for all databases?",
					new String[]{"Yes for all", "just for "+ blastDatabase, "No"});
		}
		
		try {
			Connection connection = homologyDataContainer.getConnection();
			Statement statement = connection.createStatement();
			
			switch (i)
			{
			case 0:
			{
				HomologyAPI.resetAllScorers(statement);
				return true;
			}
			case 1:
			{
				HomologyAPI.resetDatabaseScorer(statement, blastDatabase);
				return true;
			}
			default:
			{
				return false;
			}
			}
		} 
		catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Method to upate the user commited settings for a specific database
	 */
	private void updateSettings(boolean restore) {
		
		if (restore) {

			this.homologyDataContainer.setThreshold(EnzymesAnnotationDataInterface.THRESHOLD);
			this.homologyDataContainer.setUpperThreshold(EnzymesAnnotationDataInterface.UPPER_THRESHOLD);
//			this.homologyDataContainer.setBlastHmmerWeight(EnzymesAnnotationDataInterface.BLAST_HMMER_WEIGHT);
			this.homologyDataContainer.setBeta(EnzymesAnnotationDataInterface.BETA);
			this.homologyDataContainer.setAlpha(EnzymesAnnotationDataInterface.ALPHA);
			this.homologyDataContainer
			.setMinimumNumberofHits(EnzymesAnnotationDataInterface.MINIMUM_NUMBER_OF_HITS);
		}
		else {

			this.homologyDataContainer.setThreshold(this.homologyDataContainer.getCommittedThreshold());
			this.homologyDataContainer.setUpperThreshold(this.homologyDataContainer.getCommittedUpperThreshold());
//			this.homologyDataContainer.setBlastHmmerWeight(this.homologyDataContainer.getCommittedBalanceBH());
			this.homologyDataContainer.setBeta(this.homologyDataContainer.getCommittedBeta());
			this.homologyDataContainer.setAlpha(this.homologyDataContainer.getCommittedAlpha());
			this.homologyDataContainer
			.setMinimumNumberofHits(this.homologyDataContainer.getCommittedMinHomologies());

		}
		
	}

	/**
	 * 
	 */
	private void initialiser(){

		this.homologyDataContainer.setProductList(new TreeMap<Integer, String>()); 
		this.homologyDataContainer.setEnzymesList(new TreeMap<Integer, String>());  
		this.homologyDataContainer.setNamesList(new TreeMap<Integer, String>()); 
		this.homologyDataContainer.setLocusList(new TreeMap<Integer, String>()); 
		//		this.homologyDataContainer.setSelectedGene(new TreeMap<Integer, Boolean>());
		this.homologyDataContainer.setEditedProductData(new TreeMap<Integer, String[]>());
		this.homologyDataContainer.setEditedEnzymeData(new TreeMap<Integer, String[]>());
		this.homologyDataContainer.setNotesMap(new TreeMap<Integer, String>());
	}

	/**
	 * @param i
	 * @return
	 */
	private ButtonColumn buildButtonColumn(final int i, List<Integer> interProRows) {

		return new ButtonColumn(jTable, i, this.buttonActionListener, this.buttonMouseAdapter, interProRows);
	}

	/**
	 * @return
	 */
	private ActionListener getButtonActionListener() {

		return new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				processButton(arg0);}
		};
	}

	/**
	 * @return
	 */
	private MouseAdapter getButtonMouseAdapter() {

		return new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				Point p = e.getPoint();

				int  columnNumber = jTable.columnAtPoint(p);
				jTable.setColumnSelectionInterval(columnNumber, columnNumber);
				selectedModelRow=jTable.getSelectedRow();
				homologyDataContainer.setSelectedRow(selectedModelRow);
				processButton(e);
			}
		};
	}

	/**
	 * @param i
	 * @param starsColorMap 
	 * @return
	 */
	private StarColumn buildStarColumn(final int i, Map<Integer, Integer> starsColorMap) {

		return new StarColumn(jTable, i, this.starActionListener, this.starMouseAdapter, starsColorMap);
	}

	/**
	 * @return
	 */
	private ActionListener getStarActionListener() {

		return new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {

				processStarButton(arg0);
			}
		};
	}

	/**
	 * @return
	 */
	private MouseAdapter getStarMouseAdapter() {

		return new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				Point p = e.getPoint();
				int  columnNumber = jTable.columnAtPoint(p);
				jTable.setColumnSelectionInterval(columnNumber, columnNumber);
				selectedModelRow=jTable.getSelectedRow();
				homologyDataContainer.setSelectedRow(selectedModelRow);
				processStarButton(e);
			}
		};
	}

	/**
	 * @param column
	 * @param items
	 * @return
	 */
	private ComboBoxColumn buildComboBoxColumn(final int column, Map<Integer,String> items) {


		if(column == this.namesColumnNumber)
			return  new ComboBoxColumn(jTable, column, items , this.namesItemListener, this.namesMouseAdapter, this.namesPopupMenuListener);
		else
			return  new ComboBoxColumn(jTable, column, items , this.enzymesItemListener, this.enzymesMouseAdapter, this.enzymesPopupMenuListener);
	}

	/**
	 * @return
	 */
	private ItemListener getComboBoxNamesItemListener() {

		return new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {

				processProductComboBoxChange(e);
			}
		};
	}

	/**
	 * @return
	 */
	private PopupMenuListener getComboBoxNamesPopupMenuListener() {

		return new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

				processProductComboBoxChange(e);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {

			}
		};
	}

	/**
	 * @return
	 */
	private ItemListener getComboBoxEnzymesItemListener() {

		return new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {

				processEnzymesComboBoxChange(e);
			}
		};
	}

	/**
	 * @return
	 */
	private PopupMenuListener getComboBoxEnzymesPopupMenuListener() {

		return new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

				processEnzymesComboBoxChange(e);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {

			}
		};
	}

	/**
	 * @return
	 */
	private MouseAdapter getComboBoxNamesMouseListener() {

		return new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				Point p = e.getPoint();

				int  columnNumber = jTable.columnAtPoint(p);
				jTable.setColumnSelectionInterval(columnNumber, columnNumber);

				selectedModelRow = jTable.getSelectedRow();
				homologyDataContainer.setSelectedRow(selectedModelRow);

				int myRow = jTable.getSelectedRow();

				if(myRow>-1 && jTable.getRowCount()>0 && jTable.getRowCount()> myRow) {

					jTable.setRowSelectionInterval(myRow, myRow);
					scrollToVisible(jTable.getCellRect(myRow, -1, true));
				}

				processProductComboBoxChange(e);
			}
		};		
	}

	/**
	 * @return
	 */
	private MouseAdapter getComboBoxEnzymesMouseListener() {

		return new MouseAdapter() {

			public void mouseClicked(MouseEvent e) {

				Point p = e.getPoint();

				int  columnNumber = jTable.columnAtPoint(p);
				jTable.setColumnSelectionInterval(columnNumber, columnNumber);

				selectedModelRow = jTable.getSelectedRow();
				homologyDataContainer.setSelectedRow(selectedModelRow);

				int myRow = jTable.getSelectedRow();

				if(myRow>-1 && jTable.getRowCount()>0 && jTable.getRowCount()> myRow) {

					jTable.setRowSelectionInterval(myRow, myRow);
					scrollToVisible(jTable.getCellRect(myRow, -1, true));
				}

				processEnzymesComboBoxChange(e);
			}
		};		
	}

	/**
	 * @param e
	 */
	@SuppressWarnings("unchecked")
	private void processProductComboBoxChange(EventObject e) {

		boolean go = false;
		JComboBox<String> comboBox = null;

		if(e.getClass()==MouseEvent.class) {

			Object obj = ((MouseEvent) e).getSource();

			if(obj instanceof JComboBox) {

				comboBox = (JComboBox<String>) obj;
			}

			ListSelectionModel model = jTable.getSelectionModel();
			model.setSelectionInterval( productsColumn.getSelectIndex(comboBox), productsColumn.getSelectIndex(comboBox));

			if(comboBox != null)
				go = true;

			if(((MouseEvent) e).getButton()==MouseEvent.BUTTON3 ) {

				List<Integer> dbs = new ArrayList<Integer>();

				String text=null;

				dbs.add(1);
				dbs.add(2);
				text=comboBox.getSelectedItem().toString();

				if(text!=null) 
					new LinkOut(dbs, text).show(((MouseEvent) e).getComponent(),((MouseEvent) e).getX(), ((MouseEvent) e).getY());
			}
		}
		else if((e.getClass()==ItemEvent.class && ((ItemEvent) e).getStateChange() == ItemEvent.SELECTED) ) {

			Object obj = ((ItemEvent) e).getSource();

			if(obj instanceof JComboBox)
				comboBox = (JComboBox<String>) obj;

			if(comboBox != null)
				go = true;

		}

		else if(e.getClass() == PopupMenuEvent.class) {

			Object obj = ((PopupMenuEvent) e).getSource();

			if(obj instanceof JComboBox)
				comboBox = (JComboBox<String>) obj;

			if(comboBox != null)
				go = true;

		}

		if(go) {

			this.selectedModelRow = productsColumn.getSelectIndex(comboBox);
			this.homologyDataContainer.setSelectedRow(this.selectedModelRow);

			if(this.selectedModelRow < 0)
				this.selectedModelRow = Integer.parseInt(comboBox.getName());

			String selectedItem = (String) comboBox.getSelectedItem();

			if(this.selectedModelRow>-1 && productsColumn.getValues().containsKey(selectedModelRow) && !selectedItem.trim().equals(productsColumn.getValues().get(selectedModelRow))) {

				this.homologyDataContainer.setSelectedRow(this.selectedModelRow);
				int row = this.homologyDataContainer.getSelectedRow();
				this.updateProductsComboBox(comboBox, selectedItem, row);
				this.selectedModelRow = row;
			}
		}
	}

	/**
	 * @param comboBox
	 * @param selectedItem
	 * @param row
	 */
	private void updateProductsComboBox(JComboBox<String> comboBox, String selectedItem, int row ) {

		comboBox.setToolTipText((String) comboBox.getSelectedItem());
		this.productsColumn.getValues().put(row , selectedItem);

		String pdWeigth = this.homologyDataContainer.getProductPercentage(selectedItem, row );
		this.jTable.setValueAt(pdWeigth, row , namesScoreColumnNumber);
		//		this.jTable.setValueAt(new Boolean(true), row , selectColumnNumber);
		int key = Integer.parseInt(homologyDataContainer.getKeys().get(row));
		this.homologyDataContainer.getProductList().put(key, selectedItem);
		//		this.homologyDataContainer.getSelectedGene().put(key, true);
	}

	/**
	 * @param e
	 */
	@SuppressWarnings("unchecked")
	private void processEnzymesComboBoxChange(EventObject e) {

		boolean go = false;
		JComboBox<String> comboBox = null;

		if(e.getClass()==MouseEvent.class) {

			Object obj = ((MouseEvent) e).getSource();

			if(obj instanceof JComboBox)
				comboBox = (JComboBox<String>) obj;

			ListSelectionModel model = jTable.getSelectionModel();
			model.setSelectionInterval( enzymesColumn.getSelectIndex(comboBox), enzymesColumn.getSelectIndex(comboBox));

			if(((MouseEvent) e).getButton()==MouseEvent.BUTTON3 ) {

				List<Integer> dbs = new ArrayList<Integer>();

				String text=null;

				dbs.add(1);
				dbs.add(3);
				text=comboBox.getSelectedItem().toString();

				if(text!=null) 
					new LinkOut(dbs, text).show(((MouseEvent) e).getComponent(),((MouseEvent) e).getX(), ((MouseEvent) e).getY());
			}
		}

		else if((e.getClass()==ItemEvent.class && ((ItemEvent) e).getStateChange() == ItemEvent.SELECTED) ) {

			Object obj = ((ItemEvent) e).getSource();

			if(obj instanceof JComboBox) 
				comboBox = (JComboBox<String>) obj;

			if(comboBox != null) 
				go = true;
		}

		else if(e.getClass() == PopupMenuEvent.class) {

			Object obj = ((PopupMenuEvent) e).getSource();

			if(obj instanceof JComboBox) 
				comboBox = (JComboBox<String>) obj;

			if(comboBox != null) 
				go = true;
		}

		if(go) {

			this.selectedModelRow = enzymesColumn.getSelectIndex(comboBox);

			if(this.selectedModelRow < 0)
				this.selectedModelRow = Integer.parseInt(comboBox.getName());

			String selectedItem = (String) comboBox.getSelectedItem();

			if(this.selectedModelRow>-1 && enzymesColumn.getValues().containsKey(selectedModelRow) && !selectedItem.trim().equalsIgnoreCase(enzymesColumn.getValues().get(selectedModelRow))) {

				this.homologyDataContainer.setSelectedRow(this.selectedModelRow);
				int row = this.homologyDataContainer.getSelectedRow();
				this.updateEnzymesComboBox(comboBox, selectedItem, row );
				this.selectedModelRow = row;
			}
		}
	}

	/**
	 * @param comboBox
	 * @param selectedItem
	 * @param row
	 */
	private void updateEnzymesComboBox(JComboBox<String> comboBox, String selectedItem, int row ) {

		List<String> merlin_ecs = new ArrayList<String>();
		String[] ecs = ((String) comboBox.getSelectedItem()).split(",");

		for(String ec : ecs)
			merlin_ecs.add(ec.trim());

		int result = -10;
		if(this.getUniprotECnumbersTable.containsKey(row)) {

			List<String> uniprot_ecs = new ArrayList<String>(this.getUniprotECnumbersTable.get(row));
			result = this.compareAnnotationsLists(merlin_ecs, uniprot_ecs);
		}
		else {

			if(!merlin_ecs.get(0).equalsIgnoreCase("null") && !merlin_ecs.get(0).equalsIgnoreCase(""))
				result = 0;
		}

		//this.buttonStarColumn.getValueArray().get(row).setBackground(StarColumn.getBackgroundColor(result));
		boolean reviewed = ((Integer)this.buttonStarColumn.getStarsReviewedMap().get(row))==1;
		String path = StarColumn.getBackgroundColor(reviewed,result);
		this.buttonStarColumn.getValueArray().get(row).setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource(path)),0.08).resizeImageIcon());
		this.buttonStarColumn.getValueArray().get(row).updateUI();
		this.buttonStarColumn.getValueArray().get(row).repaint();
		this.buttonStarColumn.getValueArray().get(row).paintImmediately(this.buttonStarColumn.getValueArray().get(row).getBounds());

		comboBox.setToolTipText((String) comboBox.getSelectedItem());
		this.enzymesColumn.getValues().put(row, selectedItem);

		String ecWeigth = homologyDataContainer.getECPercentage(selectedItem,row);
		if(selectedItem.isEmpty())
			ecWeigth = "";

		jTable.setValueAt(ecWeigth, row, ecScoreColumnNumber);
		//		jTable.setValueAt(new Boolean(true), row, selectColumnNumber);
		//		jRadioButtonManSel.setSelected(true);
		int key = Integer.parseInt(homologyDataContainer.getKeys().get(row));
		homologyDataContainer.getEnzymesList().put(key, selectedItem);
		//		homologyDataContainer.getSelectedGene().put(key , true);
	}

	/**
	 * @param arg0
	 */
	private void processButton(EventObject arg0) {

		JButton button = null;
		if(arg0.getClass()==ActionEvent.class) {

			button = (JButton)((ActionEvent) arg0).getSource();
			ListSelectionModel model = jTable.getSelectionModel();

			int row = buttonColumn.getSelectIndex(button);
			model.setSelectionInterval(row, row);
		}		
		else if(arg0.getClass()==MouseEvent.class) {

			Point p = ((MouseEvent) arg0).getPoint();
			int  columnNumber = jTable.columnAtPoint(p);
			jTable.setColumnSelectionInterval(columnNumber, columnNumber);
			button = (JButton) buttonColumn.getValueArray().get(jTable.getSelectedRow());
			selectedModelRow = jTable.getSelectedRow();
		}
		if(button!=null) {
			int row = buttonColumn.getSelectIndex(button);
			selectedModelRow = jTable.getSelectedRow();

			new GenericDetailWindowBlast(homologyDataContainer.getRowInfo(row), 
					"Homology Data", "Gene: " + homologyDataContainer.getGeneLocus(row));

			if(jTable.isEditing())
				jTable.getCellEditor().stopCellEditing();
		}
	}

	/**
	 * @param arg0
	 */
	private void processStarButton(EventObject arg0) {

		JButton button = null;
		if(arg0.getClass()==ActionEvent.class) {

			button = (JButton)((ActionEvent) arg0).getSource();
			ListSelectionModel model = jTable.getSelectionModel();
			int row = buttonColumn.getSelectIndex(button);
			model.setSelectionInterval(row, row);

		}		
		else if(arg0.getClass()==MouseEvent.class) {

			Point p = ((MouseEvent) arg0).getPoint();
			int  columnNumber = jTable.columnAtPoint(p);
			jTable.setColumnSelectionInterval(columnNumber, columnNumber);
			button = (JButton) buttonStarColumn.getValueArray().get(jTable.getSelectedRow());
			selectedModelRow = jTable.getSelectedRow();
		}

		if(button!=null) {

			OpenBrowser  openUrl = new OpenBrowser();
			openUrl.setUrl("http://www.uniprot.org/uniprot/?query="+(String)jTable.getValueAt(jTable.getSelectedRow(), locus_tagColumnNumber)+"&sort=score");
			openUrl.openURL();
			if(jTable.isEditing())
				jTable.getCellEditor().stopCellEditing();
		}
	}

	/**
	 * @return
	 */
	private boolean exportAllData() {

		int i =CustomGUI.stopQuestion("Export all available data?",
				"Do you wish to export all information, including the data available inside the dropdown boxes?",
				new String[]{"Yes", "No", "Info"});
		if(i<2) {

			switch (i)
			{
			case 0:return true;
			default:return false;
			}
		}
		else {

			Workbench.getInstance().warn("If you select 'No' only data selected on the dropdown boxes will be exported.\n" +
					"If you select yes all homology data, including the one inside dropdown boxes, will be exported.");
			return exportAllData();
		}
	}

	/**
	 * @param path
	 * 
	 * Export Data to xls tabbed files
	 * 
	 */
	public void exportToXls(boolean allData, String path) {

		String excelFileName = System.getProperty("user.home");

		if(!path.equals("")) {

			excelFileName=path;
		}

		Calendar cal = new GregorianCalendar();

		// Get the components of the time
		int hour24 = cal.get(Calendar.HOUR_OF_DAY);     // 0..23
		int min = cal.get(Calendar.MINUTE);             // 0..59
		int day = cal.get(Calendar.DAY_OF_YEAR);		//0..365

		String db = this.blastDatabase+"_";
		
		if(this.blastDatabase.isEmpty())
			db = "all_";
		
		excelFileName+="/homologyData_"+homologyDataContainer.getProject().getName()+"_"+db+hour24+"_"+min+"_"+day+".xls";

		try {

			String sheetName = "Enzymes Annotation";//name of sheet

			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFSheet sheet = wb.createSheet(sheetName) ;

			TableColumnModel tc = jTable.getColumnModel();
			int headerSize = tc.getColumnCount();
			int h = 1; // skip info column

			HSSFRow row = sheet.createRow(0);

			while (h < headerSize) {

				row.createCell(h-1).setCellValue(tc.getColumn(h).getHeaderValue().toString());
				h++;
			}

			int p = 3, pS = 4, e = 5, eS = 6;

			if(homologyDataContainer.isEukaryote()) {

				p++;
				pS++;
				e++;
				eS++;
			}


			int addrow = 1;

			for(int r=0; r < mainTableData.getTable().size(); r++) {

				String[] productsToXLS = null, enzymesToXLS = null;
				String pdWeigth, ecWeigth;
				boolean existsProduct=false, existsEnzyme=false;
				String product=null, enzyme=null;

				row = sheet.createRow(addrow);

				for (int j=1; j < mainTableData.getRow(r).length; j++) {

					if(j==namesColumnNumber && mainTableData.getRow(r)[j].getClass()==String[].class) {

						productsToXLS = (String[])mainTableData.getRow(r)[j]; 

						product = this.productsColumn.getSelectItem(r);

						row.createCell(j-1).setCellValue(product);

						productsToXLS = this.removeElement(productsToXLS, product);
						existsProduct=true;
					}
					else if(j==namesScoreColumnNumber && existsProduct) {

						existsProduct=false;
						row.createCell(j-1).setCellValue(homologyDataContainer.getProductPercentage(product,r));
						product=null;
					}
					else if(j==ecnumbersColumnNumber && mainTableData.getRow(r)[j].getClass()==String[].class) {

						enzymesToXLS = (String[])mainTableData.getRow(r)[j];
						enzyme = this.enzymesColumn.getSelectItem(r);

						if(enzyme!=null) {

							row.createCell(j-1).setCellValue(enzyme);
							enzymesToXLS = this.removeElement(enzymesToXLS, enzyme);
							existsEnzyme=true;
						}
					}
					else if(j==ecScoreColumnNumber && existsEnzyme) {

						existsEnzyme=false;
						row.createCell(j-1).setCellValue(homologyDataContainer.getECPercentage(enzyme,r)+"");
						enzyme=null;
					}
					else {

						if(j==locus_tagColumnNumber) {

							row.createCell(j-1).setCellValue(mainTableData.getRow(r)[j]+"");
						}
						else if(j==notesColumnNumber ) {
							
							row.createCell(j-1).setCellValue(mainTableData.getRow(r)[j]+"");
						}
						else if(j==uniprotStarColumnNumber) {

							String text = mainTableData.getRow(r)[j]+"";
							if(text.equalsIgnoreCase("1")) {

								text = "reviewed";
							}
							if(text.equalsIgnoreCase("0")) {

								text = "unreviewed";
							}
							if(text.equalsIgnoreCase("-1")) {

								text = "unavailable";
							}

							row.createCell(j-1).setCellValue("\t"+text);
						}
						else {
							row.createCell(j-1).setCellValue(mainTableData.getRow(r)[j]+"");
						}
					}
				}

				if(allData) {

					if(productsToXLS!=null && enzymesToXLS!=null) {

						int maxlength=0;

						if(productsToXLS.length>enzymesToXLS.length) {

							maxlength=productsToXLS.length;
						}
						else {

							maxlength=enzymesToXLS.length;
						}

						for(int k=0;k<maxlength;k++) {

							addrow++;
							row = sheet.createRow(addrow);

							if(k<productsToXLS.length&&k<enzymesToXLS.length) {

								pdWeigth = homologyDataContainer.getProductPercentage(productsToXLS[k].trim(),r);
								ecWeigth = homologyDataContainer.getECPercentage(enzymesToXLS[k].trim(),r);
								if(productsToXLS[k].trim()==""){productsToXLS[k]="\t";}
								if(enzymesToXLS[k].trim()==""){enzymesToXLS[k]="\t";}

								row.createCell(p).setCellValue(productsToXLS[k].trim());
								row.createCell(pS).setCellValue(pdWeigth);
								row.createCell(e).setCellValue(enzymesToXLS[k].trim());
								row.createCell(eS).setCellValue(ecWeigth);

							}
							else if(k<productsToXLS.length) {

								pdWeigth = homologyDataContainer.getProductPercentage(productsToXLS[k].trim(),r);
								if(productsToXLS[k].trim()==""){productsToXLS[k]="\t";}

								row.createCell(p).setCellValue(productsToXLS[k].trim());
								row.createCell(pS).setCellValue(pdWeigth);
							}
							else if(k<enzymesToXLS.length) {

								ecWeigth = homologyDataContainer.getECPercentage(enzymesToXLS[k],r);
								if(enzymesToXLS[k].trim()==""){enzymesToXLS[k]="\t";}

								row.createCell(e).setCellValue(enzymesToXLS[k].trim());
								row.createCell(eS).setCellValue(ecWeigth);
							}
						}
					}
					else if(productsToXLS!=null) {

						addrow++;
						row = sheet.createRow(addrow);

						for(int k=1;k<productsToXLS.length;k++) {

							pdWeigth = homologyDataContainer.getProductPercentage(productsToXLS[k].trim(),r);
							if(productsToXLS[k].trim()==""){productsToXLS[k]="\t";}

							row.createCell(p).setCellValue(productsToXLS[k].trim());
							row.createCell(pS).setCellValue(pdWeigth);
						}
					}
					else if(enzymesToXLS!=null) {

						addrow++;
						row = sheet.createRow(addrow);

						for(int k=1;k<enzymesToXLS.length;k++) {

							ecWeigth = homologyDataContainer.getECPercentage(enzymesToXLS[k].trim(),r);
							if(enzymesToXLS[k].trim()=="")
								enzymesToXLS[k]="\t";

							row.createCell(e).setCellValue(enzymesToXLS[k].trim());
							row.createCell(eS).setCellValue(ecWeigth);
						}
					}
				}

				if(allData){
					addrow++;
					row = sheet.createRow(addrow);
				}

				addrow++;	
			}

			FileOutputStream fileOut = new FileOutputStream(excelFileName);

			//write this workbook to an Outputstream.
			wb.write(fileOut);
			fileOut.flush();
			wb.close();
			fileOut.close();

			Workbench.getInstance().info("Data successfully exported.");
		} 
		catch (Exception e) {

			Workbench.getInstance().error("An error occurred while performing this operation. Error "+e.getMessage());
			e.printStackTrace();
		}
	}


	/**
	 * @author ODias
	 * Filter GenBank files
	 */
	class GBKFileFilter extends javax.swing.filechooser.FileFilter {

		public boolean accept(File f) {

			return f.isDirectory() || f.getName().toLowerCase().endsWith(".gbk");
		}

		public String getDescription() {

			return ".gbk files";
		}
	}

	/**
	 * Save re-annotated GenBank file
	 * @throws IOException 
	 */
	private void saveGenbankFile() throws IOException{

		fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fc.setFileFilter(new GBKFileFilter());
		fc.setDialogTitle("Select gbk files directory");
		int returnVal = fc.showOpenDialog(new JTextArea());

		if (returnVal == JFileChooser.APPROVE_OPTION) {

			File[]	files;
			if(fc.getSelectedFile().isDirectory()) {

				files = fc.getSelectedFile().listFiles();
			}
			else {

				files = (new File(fc.getSelectedFile().getParent())).listFiles();
			}

			int countFiles=0;

			for(File file:files) {

				if(file.getAbsolutePath().endsWith(".gbk"))
					countFiles+=1;
			}
			if(countFiles<=0) {

				Workbench.getInstance().error("The selected directory does not contain any Genbank file (*.gbk)");
			}
			else {

				TreeMap<String,String> gene = new TreeMap<String, String>();
				TreeMap<String,String> ecn = new TreeMap<String, String>();
				TreeMap<String,String> prod = new TreeMap<String, String>();

				for(int i=0; i < mainTableData.getTable().size(); i++) {

					//					if((Boolean) mainTableData.getRow(i)[selectColumnNumber]) {

					if(this.productsColumn.getSelectItem(i)!=null) {

						prod.put((String) mainTableData.getRow(i)[locus_tagColumnNumber], this.productsColumn.getSelectItem(i));
					}

					if(jTable.getValueAt(i,geneNameColumnNumber)!=null && !((String) jTable.getValueAt(i,geneNameColumnNumber)).isEmpty()) {

						gene.put((String) mainTableData.getRow(i)[locus_tagColumnNumber], ((String) jTable.getValueAt(i,geneNameColumnNumber)));
					}

					if(this.enzymesColumn.getSelectItem(i)!=null) {

						ecn.put((String) mainTableData.getRow(i)[locus_tagColumnNumber], this.enzymesColumn.getSelectItem(i));
					}

					//						p=""; e=""; g="";
					//						if(!((String) mainTableData.getRow(i)[2]).isEmpty())
					//						{
					//							g = ((String) jTable.getValueAt(i,2));
					//						}
					//						if(((String[]) mainTableData.getRow(i)[namesColumnNumber]).length>0)
					//						{
					//							if(prodItem.containsKey(i))
					//							{
					//								for(String s: (String[]) mainTableData.getRow(i)[namesColumnNumber])
					//								{
					//									if(s==prodItem.get(i))
					//									{
					//										p=s;
					//									}
					//								}
					//							}
					//							else
					//							{
					//								p = ((String[]) mainTableData.getRow(i)[namesColumnNumber])[0];
					//							}
					//						}
					//						if(((String[]) mainTableData.getRow(i)[ecnumbersColumnNumber]).length>0)
					//						{
					//							if(ecItem.containsKey(i))
					//							{
					//								for(String s: (String[]) mainTableData.getRow(i)[ecnumbersColumnNumber])
					//								{
					//									if(s==ecItem.get(i))
					//									{
					//										e=s;
					//									}
					//								}
					//							}
					//							else
					//							{
					//								e = ((String[]) mainTableData.getRow(i)[ecnumbersColumnNumber])[0];
					//							}
					//						}
					//						if(!p.isEmpty()) prod.put((String) mainTableData.getRow(i)[1], p);
					//						if(!g.isEmpty()) gene.put((String) mainTableData.getRow(i)[1], g);
					//						if(!e.isEmpty()) ecn.put((String) mainTableData.getRow(i)[1], e);
				}						                             
				//				}

				for(File f: files)
				{
					if(f.getAbsolutePath().endsWith(".gbk"))
					{
						WriteGBFile wgbf = new WriteGBFile(f, ecn, prod, gene);
						wgbf.writeFile();
					}
				}
			}
		}
	}

//	/**
//	 * 
//	 */
//	private void selectThreshold() {
//
//		//		this.homologyDataContainer.setSelectedGene(new HashMap<Integer, Boolean>());
//		//		this.jRadioButtonTresh.setSelected(true);
//
//		if(Double.parseDouble(this.jLabelLowerThreshold.getText())<0 || Double.parseDouble(this.jLabelLowerThreshold.getText())>1) {
//
//			this.jLabelLowerThreshold.setText(this.homologyDataContainer.getThreshold().toString());
//			Workbench.getInstance().warn("The value must be between 0 and 1");
//		}
//		else {
//
//			Rectangle visible = jTable.getVisibleRect();
//
//			this.homologyDataContainer.setThreshold(Double.parseDouble(this.jLabelLowerThreshold.getText()));
//			this.mainTableData = this.homologyDataContainer.getAllGenes(blastDatabase, false);
//			//this.jTable.setModel(this.mainTableData);
//			this.fillList(visible);
//			//			this.jRadioButtonTresh.setSelected(true);
//		}
//
//	}

	/**
	 * @param locusMap
	 * @return
	 */
	private Map<Integer, List<String>> getUniprotECnumbersTable(Map<Integer, String> locusMap) {

		Map<Integer, List<String>> result = new HashMap<Integer, List<String>>();
		Map<String, List<String>> uniprotData = this.homologyDataContainer.get_uniprot_ecnumbers();

		Map<String, Integer> inv = new HashMap<String, Integer>(); 

		for (Entry<Integer, String> entry : locusMap.entrySet()) {

			inv.put(entry.getValue(), entry.getKey());
		}

		if(uniprotData != null) {

			for(String locus : uniprotData.keySet()) {

				if(inv.containsKey(locus)) {

					result.put(inv.get(locus), uniprotData.get(locus));
				}
			}
		}
		return result;
	}

	/**
	 * @param uniprot
	 * @param merlin
	 * @return
	 * 
	 * 
	 *  0 distinct
	 * -1 partial match uniprot more
	 *  1 match
	 *  2 partial match merlin more
	 * 
	 */
	private Map<Integer, Integer> compareAnnotations(Map<Integer, String> ecItems) {

		Map<Integer, Integer> result = new HashMap<Integer, Integer>();

		Map<Integer, List<String>> merlin = new HashMap<Integer, List<String>>();
		for(int row : ecItems.keySet()) {

			if(ecItems.get(row) != null) {

				List<String> ecnumbers = new ArrayList<String>();
				String[] ecs = ecItems.get(row).split(", ");

				for(String ec : ecs)
					ecnumbers.add(ec.trim());

				merlin.put(row, ecnumbers);
			}
		}

		List<Integer> merlinKeySet = new ArrayList<Integer>(merlin.keySet());
		Map<Integer, List<String>> uniprotECnumbersTable_clone = new HashMap<Integer, List<String>>(this.getUniprotECnumbersTable);

		for(int row : merlinKeySet) {

			List<String> merlin_ecs = new ArrayList<String> (merlin.get(row));

			if(uniprotECnumbersTable_clone.containsKey(row) && !uniprotECnumbersTable_clone.get(row).get(0).equalsIgnoreCase("null") && !uniprotECnumbersTable_clone.get(row).get(0).equalsIgnoreCase("")) {

				List<String> uni_ecs = new ArrayList<String> (uniprotECnumbersTable_clone.get(row));

				result.put(row, this.compareAnnotationsLists(merlin_ecs, uni_ecs));
				uniprotECnumbersTable_clone.remove(row);
			}
			else{

				if(!merlin_ecs.get(0).equalsIgnoreCase("null") && !merlin_ecs.get(0).equalsIgnoreCase(""))
					result.put(row, 0);
			}
			merlin.remove(row);
		}

		List<Integer> uniprotKeySet = new ArrayList<Integer>(uniprotECnumbersTable_clone.keySet());

		for(int row : uniprotKeySet) {

			if(!uniprotECnumbersTable_clone.get(row).get(0).equalsIgnoreCase("null") && !uniprotECnumbersTable_clone.get(row).get(0).equalsIgnoreCase("")) {

				result.put(row, 0);
			}
			uniprotECnumbersTable_clone.remove(row);
		}

		return result;
	}


	/**
	 * @param merlin_ecs
	 * @param uni_ecs
	 * @return
	 */
	private int compareAnnotationsLists(List<String> merlin_ecs, List<String> uniprot_ecs) {

		List<String> uni_ecs = new ArrayList<String>(uniprot_ecs);
		List<String> merlin_ecs_clone = new ArrayList<String> (merlin_ecs);
		List<String> uni_ecs_clone = new ArrayList<String> (uni_ecs);
		int uni_initial_size = uni_ecs_clone.size();
		int merlin_initial_size =  merlin_ecs_clone.size();

		if(merlin_ecs.size() == uni_ecs.size()) {

			for(String ecnumber :  merlin_ecs_clone) {

				merlin_ecs.remove(ecnumber);
				if(uni_ecs.contains(ecnumber)) {

					uni_ecs.remove(ecnumber);
				}
			}

			if(uni_ecs.isEmpty()) {

				return 1;
			}
			else if(!uni_ecs.isEmpty() && !uni_ecs.get(0).equalsIgnoreCase("null") && !uni_ecs.get(0).equalsIgnoreCase("")) {

				return 0;
			}
		}
		else {

			if(merlin_ecs.size() > uni_ecs.size()) {

				for(String ecnumber :  merlin_ecs_clone) {

					merlin_ecs.remove(ecnumber);
					if(uni_ecs.contains(ecnumber)) {

						uni_ecs.remove(ecnumber);
					}
				}

				if(uni_ecs.isEmpty()) {

					return 2;
				}
				else if(!uni_ecs.isEmpty() && !uni_ecs.get(0).equalsIgnoreCase("null") && !uni_ecs.get(0).equalsIgnoreCase("")) {

					if(uni_initial_size == uni_ecs.size()) {

						return 0;
					}
					else {

						return -1;
					}
				}
			}
			else {

				for(String ecnumber :  uni_ecs_clone) {

					uni_ecs.remove(ecnumber);
					if(merlin_ecs.contains(ecnumber)) {

						merlin_ecs.remove(ecnumber);
					}
				}

				if(merlin_ecs.isEmpty()) {

					return -1;
				}
				else if(!merlin_ecs.isEmpty() && !merlin_ecs.get(0).equalsIgnoreCase("null") && !merlin_ecs.get(0).equalsIgnoreCase("")) {

					if(merlin_initial_size == merlin_ecs.size()) {

						return 0;
					}
					else {

						return -1;
					}
				}
			}

		}
		return -10;
	}

	/**
	 * @param array
	 * @param element
	 * @return
	 */
	private String[] removeElement(String[] array, String element) {
		if(Arrays.asList(array).contains(element)) {

			String[] newArray = new String[array.length-1];
			boolean reachedElement = false;

			for(int i=0; i<(array.length-1); i++) {

				if(!array[i].equals(element)) {

					if(reachedElement) {

						newArray[i]=array[i+1];
					}
					else {

						newArray[i]=array[i];
					}
				}
				else {

					reachedElement = true;
					newArray[i]=array[i+1];
				}
			}
			return newArray;
		}
		return array;
	}

	/**
	 * @param visible
	 */
	private void scrollToVisible(final Rectangle visible) {

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				jTable.scrollRectToVisible(visible);
			}
		});
	}

	/**
	 * 
	 * Method to decide when the buttons alpha, threshold and auto select should be available
	 * @throws SQLException 
	 */
	private void checkButtonsStatus(Statement statement){


		ArrayList<String> databases = new ArrayList<>();

		try {

			if(statement == null) {
				Connection connection = homologyDataContainer.getConnection();
				statement = connection.createStatement();
			}

			databases = HomologyAPI.bestAlphasFound(statement);

		} 
		catch (SQLException e) {
			e.printStackTrace();
		}

		if(!databases.contains("") && this.blastDatabase.equalsIgnoreCase("") && databases.size() > 0) {

//			jLabelThresholdText.setEnabled(false);
//			jLabelAlphaText.setEnabled(false);
			jButtonAnnotation.setEnabled(false);

		}
		else if(!databases.contains("") && this.blastDatabase.equalsIgnoreCase("") && databases.size() == 0) {

//			jLabelThresholdText.setEnabled(true);
//			jLabelAlphaText.setEnabled(true);
			jButtonAnnotation.setEnabled(true);

		}
		else if(databases.contains("") && !this.blastDatabase.equalsIgnoreCase("")) {

//			jLabelThresholdText.setEnabled(false);
//			jLabelAlphaText.setEnabled(false);
			jButtonAnnotation.setEnabled(false);

		}
		else if(databases.contains(blastDatabase)) {

//			jLabelThresholdText.setEnabled(false);
//			jLabelAlphaText.setEnabled(false);
			jButtonAnnotation.setEnabled(false);

		}
		else {

//			jLabelThresholdText.setEnabled(true);
//			jLabelAlphaText.setEnabled(true);
			jButtonAnnotation.setEnabled(true);

		}
		
	}

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#updateGraphicalObject()
	 */
	@Override
	public void updateTableUI() {
		
		jLabeldAlpha.setText(homologyDataContainer.getAlpha().toString());
		jLabelLowerThreshold.setText(homologyDataContainer.getThreshold().toString());
		jLabelUpperThreshold.setText(homologyDataContainer.getUpperThreshold().toString());

		Rectangle visible = null;

		if(this.selectedModelRow>-1 && jTable.getRowCount()>0 && jTable.getRowCount()> this.selectedModelRow)
			visible = this.jTable.getCellRect(this.selectedModelRow, -1, true);

		this.mainTableData = this.homologyDataContainer.getAllGenes(blastDatabase, true);
		this.fillList(visible);

		//jButton1.setEnabled(homologyDataContainer.isBlastPAvailable() && homologyDataContainer.isHmmerAvailable());
		//		jTextField1.setEnabled(homologyDataContainer.isBlastPAvailable() && homologyDataContainer.isHmmerAvailable());

		this.updateUI();
		this.revalidate();
		this.repaint();
		
		this.checkButtonsStatus(null);
	}

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#addListenersToGraphicalObjects(javax.swing.JPanel, javax.swing.MyJTable)
	 */
	@Override
	public void addListenersToGraphicalObjects() {}
}
