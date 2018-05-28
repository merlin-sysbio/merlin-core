package pt.uminho.ceb.biosystems.merlin.core.views.metabolic;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.EventListener;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.auxiliary.ReactionsInterfaceAux;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.PathwayReaction;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactionsInterface;
import pt.uminho.ceb.biosystems.merlin.core.gui.CustomGUI;
import pt.uminho.ceb.biosystems.merlin.core.gui.InsertEditReaction;
import pt.uminho.ceb.biosystems.merlin.core.gui.InsertPathway;
import pt.uminho.ceb.biosystems.merlin.core.gui.RemovePathwayFromModel;
import pt.uminho.ceb.biosystems.merlin.core.utilities.ButtonColumn;
import pt.uminho.ceb.biosystems.merlin.core.utilities.CreateImageIcon;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MyJTable;
import pt.uminho.ceb.biosystems.merlin.core.utilities.SearchInTable;
import pt.uminho.ceb.biosystems.merlin.core.views.UpdatablePanel;
import pt.uminho.ceb.biosystems.merlin.core.views.windows.GenericDetailWindow;
import pt.uminho.ceb.biosystems.merlin.utilities.OpenBrowser;
import pt.uminho.ceb.biosystems.mew.biocomponents.validation.chemestry.BalanceValidator;


/**
 * @author ODias
 *
 */
public class ReactionsView extends UpdatablePanel {

	private static final long serialVersionUID = 1L;
	private static final int REACTION_NAME_COLUMN = 2;
	private static final int IN_MODEL_COLUMN = 7;
	//private static final int IS_GENERIC_COLUMN = 7;
	private static final int IS_REVERSIBLE_COLUMN = 6;
	//private static final int NOTES_COLUMN = 5;
	private JScrollPane jScrollPane1;
	private JPanel jPanel1, jPanel2;
	private MyJTable jTable;
	private ReactionsInterface reactionsInterface;
	private JComboBox<String> pathsComboBox;
	private JPanel jPanelReactions;
	private JButton jButtonColor;
	private JRadioButton jRadioAllReactions, jRadioButtonEncoded;
	private DataTable mainTableData;
	private GenericDataTable specificPathwayData;
	private JPanel jPanelPathways;
	private JButton jButtonDuplicate, jButtonRemovePath;
	private JPanel jPanelReactionsEdition;
	private JButton jButtonInsert, jButtonRemove, jButtonEdit, jButtonGaps;
	private JPanel jPanelExport;
	private JButton jButtonExportTxt;
	private JPanel jPanelPaths;
	private ButtonGroup buttonGroup;
	private SearchInTable searchInReaction;
	//private int selectedRow;
	private JRadioButton jRadioButtonReactionsOnly, jRadioButtonByPathway;
	private ButtonColumn buttonColumn;
	private ButtonGroup buttonGroup3;
	private JButton jButtonNewPathway;
	private boolean encodedSelected;
	protected String previousSelectedPathway;
	private JButton jViewInBrowser;
	private JRadioButton jRadioButtonGapsOnly;
	private JRadioButton jRadioButtonUnbalancedOnly;
	private TableModelListener tableModelListener;
	private boolean myKeepRow;
	private ItemListener comboBoxItemListener;
	private MouseAdapter buttonMouseAdapter;
	private EventListener buttonActionListener;




	/**
	 * @param reactionsInterface
	 */
	public ReactionsView(ReactionsInterface reactionsInterface) {

		super(reactionsInterface);
		this.reactionsInterface = reactionsInterface;
		this.encodedSelected=this.reactionsInterface.existGenes();
		this.mainTableData = this.reactionsInterface.getReactionsData(this.encodedSelected);
		List<Integer> nameTabs = new ArrayList<>();
//		nameTabs.add(1);
		nameTabs.add(2);
		nameTabs.add(3);
		this.searchInReaction = new SearchInTable(nameTabs);
		this.jTable = new MyJTable();
		jTable.setShowGrid(false);
		this.initGUI();
		this.tableModelListener = this.getTableModelListener();
		this.comboBoxItemListener = this.getComboBoxItemListener();
		this.buttonActionListener = this.getButtonActionListener();
		this.buttonMouseAdapter = this.getButtonMouseAdapter();
		this.myKeepRow = false;
		if(mainTableData.getTable().size()>0)
			this.fillList(true, false);
	}

	/**
	 * intitiate gui
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
			jPanel2Layout.rowWeights = new double[] {0.1, 0.1};
			jPanel2Layout.rowHeights = new int[] {7, 7};
			jPanel2.setLayout(jPanel2Layout);
			this.add(jPanel2, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			jPanelPaths = new JPanel();
			GridBagLayout jPanelGenesLayout = new GridBagLayout();
			jPanelGenesLayout.columnWeights = new double[] {0.1, 0.0, 0.0};
			jPanelGenesLayout.columnWidths = new int[] {30, 7, 7};
			jPanelGenesLayout.rowWeights = new double[] {0.1};
			jPanelGenesLayout.rowHeights = new int[] {5};
			jPanelPaths.setLayout(jPanelGenesLayout);
			jPanel2.add(jPanelPaths, new GridBagConstraints(4, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			jPanelPaths.setBounds(718, 6, 157, 115);
			jPanelPaths.setBorder(BorderFactory.createTitledBorder("view pathway"));
			{
				jPanelPathways = new JPanel();
				GridBagLayout jPanelRemoveLayout = new GridBagLayout();
				jPanelRemoveLayout.columnWeights = new double[] {0.1, 0.0, 0.1};
				jPanelRemoveLayout.columnWidths = new int[] {20, 7, 20};
				jPanelRemoveLayout.rowWeights = new double[] {0.1};
				jPanelRemoveLayout.rowHeights = new int[] {5};
				jPanelPathways.setLayout(jPanelRemoveLayout);
				jPanel2.add(jPanelPathways, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelPathways.setBounds(297, 56, 100, 61);
				jPanelPathways.setBorder(BorderFactory.createTitledBorder("pathway"));
				{
					jButtonRemovePath = new JButton();
					jPanelPathways.add(jButtonRemovePath, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonRemovePath.setText("remove");
					jButtonRemovePath.setToolTipText("remove pathway");
					jButtonRemovePath.setPreferredSize(new Dimension(90, 40));
					jButtonRemovePath.setIcon( new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Remove.png"))),0.08).resizeImageIcon());
					jButtonRemovePath.addActionListener( new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {
							//							RemovePathwayFromModel inst = 
							new RemovePathwayFromModel(reactionsInterface){
								private static final long serialVersionUID = 7268015065845897254L;
								public void simpleFinish() {

									this.setVisible(false);
									this.dispose();
								}
								public void finish() {

									this.setVisible(false);
									this.dispose();
									fillList(true, false);
								}
							};
						}
					}
							);
				}
				{
					jButtonColor = new JButton();
					jPanelPathways.add(jButtonColor, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonColor.setText("colors");
					jButtonColor.setToolTipText("change pathways colors");
					jButtonColor.setPreferredSize(new java.awt.Dimension(90, 40));
					jButtonColor.setIcon(new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Synchronize.png"))),0.08).resizeImageIcon());
					jButtonColor.addActionListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {

							reactionsInterface.colorPaths();
							fillList(true, true);
						}});
				}
				{
					jButtonNewPathway = new JButton();
					jPanelPathways.add(jButtonNewPathway, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonNewPathway.setText("new");
					jButtonNewPathway.setToolTipText("add new pathway");
					jButtonNewPathway.setPreferredSize(new java.awt.Dimension(90, 40));
					jButtonNewPathway.setIcon(new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Add.png"))),0.08).resizeImageIcon());
					jButtonNewPathway.addActionListener( new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {

							new InsertPathway(reactionsInterface) {

								private static final long serialVersionUID = -1L;
								public void finish() {

									reactionsInterface.getPathsBoolean(jRadioButtonEncoded.isSelected());
									setPathwaysComboBox();
									this.setVisible(false);
									this.dispose();
								}
							};
						}
					}
							);
				}
			}
			{
				jPanelReactionsEdition = new JPanel();
				GridBagLayout jPanelInsertEditLayout = new GridBagLayout();
				jPanelInsertEditLayout.columnWeights = new double[] {0.1, 0.0, 0.1, 0.0, 0.1};
				jPanelInsertEditLayout.columnWidths = new int[] {20, 7, 20, 40, 20};
				jPanelInsertEditLayout.rowWeights = new double[] {0.1};
				jPanelInsertEditLayout.rowHeights = new int[] {5};
				jPanelReactionsEdition.setLayout(jPanelInsertEditLayout);
				jPanel2.add(jPanelReactionsEdition, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelReactionsEdition.setBounds(7, 56, 277, 61);
				jPanelReactionsEdition.setBorder(BorderFactory.createTitledBorder("reactions"));
				{
					jButtonInsert = new JButton();
					jPanelReactionsEdition.add(jButtonInsert, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonInsert.setIcon( new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Upload.png"))),0.08).resizeImageIcon());
					jButtonInsert.setText("insert");
					jButtonInsert.setToolTipText("insert");
					jButtonInsert.setPreferredSize(new Dimension(90, 40));
					jButtonInsert.addActionListener( new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent e) {
							//							InsertEditReaction inst =
							new InsertEditReaction(reactionsInterface, -10) {

								private static final long serialVersionUID = -1;
								public void close(){
									this.setVisible(false);
									this.dispose();
								}
								public void closeAndUpdate() {

									removeGapLabelling();

									myKeepRow = false;
									MerlinUtils.updateMetabolicViews(reactionsInterface.getProject().getName());
									//TODO
									//find row of the new reaction and go there

									//									fillList(true,false);
									this.setVisible(false);
									this.dispose();
								}
							};
						}
					}
							);
				}
				
				{
					jButtonRemove = new JButton();
					jPanelReactionsEdition.add(jButtonRemove, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonRemove.setIcon( new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Remove.png"))),0.08).resizeImageIcon());
					jButtonRemove.setText("remove");
					jButtonRemove.setToolTipText("remove");
					jButtonRemove.setPreferredSize(new Dimension(90, 40));
					jButtonRemove.addActionListener( new ActionListener(){

						public void actionPerformed(ActionEvent arg0) {

							try {

								int rowID;

								if(jTable.getSelectedRow()!=-1) {

									if(pathsComboBox.getSelectedIndex()>0)
										rowID= Integer.parseInt((specificPathwayData.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow()))));
									else
										rowID= Integer.parseInt(reactionsInterface.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow())));
									
									if(removeReactionConfirmation()) {
									
										reactionsInterface.removeReaction(rowID);
										fillList(true, true);
									}
								}
								else {

									Workbench.getInstance().warn("please select a reaction to remove!");	
								}
							}
							catch (Exception e){e.printStackTrace();}
						}
					});
				}

				{
					jButtonGaps = new JButton();
					jPanelReactionsEdition.add(jButtonGaps, new GridBagConstraints(4, 0, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonGaps.setText("find gene");
					jButtonGaps.setToolTipText("Please, select a row");
					jButtonGaps.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Find.png")),0.08).resizeImageIcon());
					jButtonGaps.setPreferredSize(new Dimension(90, 40));
					jButtonGaps.addActionListener( new ActionListener(){


						public void actionPerformed(ActionEvent e) {

							boolean go = false;
							
							Project project = reactionsInterface.getProject();

							go = Project.isFaaFiles(project.getDatabase().getDatabaseName(), project.getTaxonomyID());

							if(go){

								int	row = jTable.getSelectedRow();
								if(row>-1) {

//									String	id = reactionsInterface.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow()));
									String id;
									if(pathsComboBox.getSelectedIndex()>0)
										id=specificPathwayData.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow()));
									else
										id=reactionsInterface.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow()));
									
									ParamSpec[] paramsSpec = new ParamSpec[]{
											new ParamSpec("reactionID", String.class, id, null),
											new ParamSpec("ReactionsInterface", ReactionsInterface.class, reactionsInterface, null)
										};
									
									for (@SuppressWarnings("rawtypes") OperationDefinition def : Core.getInstance().getOperations()){
										if (def.getID().equals("operations.FindGenes.ID")){
											
											Workbench.getInstance().executeOperation(def, paramsSpec);
										}
									}
									
									MerlinUtils.updateMetabolicViews(project.getName());
								}
								else {

									Workbench.getInstance().warn("please select a row.");
								}

							}
							else {
								Workbench.getInstance().warn("please select a .faa file");
							}

						}
					});



			}
				{
					jButtonEdit = new JButton();
					jPanelReactionsEdition.add(jButtonEdit, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonEdit.setText("edit");
					jButtonEdit.setToolTipText("edit");
					jButtonEdit.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Edit.png")),0.08).resizeImageIcon());
					jButtonEdit.setPreferredSize(new Dimension(90, 40));
					jButtonEdit.addActionListener( new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {

							if(jTable.getSelectedRow()>-1) {

								//int selected = jTable.convertRowIndexToModel(jTable.getSelectedRow());
								int rowID;

								if(pathsComboBox.getSelectedIndex()>0)
									rowID = Integer.parseInt(specificPathwayData.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow())));
								else
									rowID = Integer.parseInt(reactionsInterface.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow())));

								new InsertEditReaction(reactionsInterface, rowID) {

									private static final long serialVersionUID = -1;
									public void close()  {

										this.setVisible(false);
										this.dispose();
									}

									public void closeAndUpdate() {

										removeGapLabelling();
										myKeepRow = this.inModel;

										if(jRadioAllReactions.isSelected())
											myKeepRow = true;

										this.setVisible(false);
										this.dispose();

										MerlinUtils.updateMetabolicViews(reactionsInterface.getProject().getName());
									}
								};
							}
							else {

								Workbench.getInstance().warn("please select a row!");
							}
						}
					}
							);
				}
				{
					jButtonDuplicate = new JButton();
					jPanelReactionsEdition.add(jButtonDuplicate, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonDuplicate.setText("duplicate");
					jButtonDuplicate.setToolTipText("duplicate");
					jButtonDuplicate.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Duplicate.png")),0.08).resizeImageIcon());
					jButtonDuplicate.setPreferredSize(new Dimension(90, 40));
					jButtonDuplicate.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0) {

							try {

								int rowID;

								if(jTable.getSelectedRow()!=-1) {

									if(pathsComboBox.getSelectedIndex()>0)
										rowID = Integer.parseInt(specificPathwayData.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow())));
									else
										rowID = Integer.parseInt(reactionsInterface.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow())));

									reactionsInterface.duplicateReaction(rowID);
									fillList(true, true);
								}
								else {

									Workbench.getInstance().warn("please select a reaction to duplicate!");	
								}
							}
							catch (Exception e){e.printStackTrace();}
						}
					});
				}
			}
			{
				jPanelExport = new JPanel();
				GridBagLayout jPanelExportLayout = new GridBagLayout();
				jPanelExportLayout.columnWeights = new double[] {0.1, 0.0, 0.1};
				jPanelExportLayout.columnWidths = new int[] {7, 7, 7};
				jPanelExportLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1};
				jPanelExportLayout.rowHeights = new int[] {20, 20, 20, 20};
				jPanelExport.setLayout(jPanelExportLayout);
				jPanel2.add(jPanelExport, new GridBagConstraints(4, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelExport.setBorder(BorderFactory.createTitledBorder("export"));
				{
					jButtonExportTxt = new JButton();
					jPanelExport.add(jButtonExportTxt, new GridBagConstraints(2, 0, 1, 3, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					jButtonExportTxt.setText("export file");
					jButtonExportTxt.setToolTipText("Export to text file (xls)");
					//					jButtonExportTxt.setBounds(11, 8, 118, 48);
					jButtonExportTxt.setIcon(new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Download.png"))),0.08).resizeImageIcon());
					jButtonExportTxt.setPreferredSize(new Dimension(120, 40));
					jButtonExportTxt.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent arg0) {

							JFileChooser fc = new JFileChooser();
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							fc.setDialogTitle("Select directory");
							int returnVal = fc.showOpenDialog(new JTextArea());

							if (returnVal == JFileChooser.APPROVE_OPTION) {

								File file = fc.getSelectedFile();
								exportToXls2(file.getAbsolutePath());
							}
						}});

				}
				{
					jRadioButtonByPathway = new JRadioButton();
					jRadioButtonByPathway.setSelected(true);
					jPanelExport.add(jRadioButtonByPathway, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jRadioButtonByPathway.setText("by pathway");
					jRadioButtonByPathway.setToolTipText("by pathway");
				}
				{
					jRadioButtonReactionsOnly = new JRadioButton();
					jPanelExport.add(jRadioButtonReactionsOnly, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jRadioButtonReactionsOnly.setText("reactions only");
					jRadioButtonReactionsOnly.setToolTipText("reactions only");
				}
				{
					jRadioButtonGapsOnly = new JRadioButton();
					jRadioButtonGapsOnly.setEnabled(false);
					jPanelExport.add(jRadioButtonGapsOnly, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jRadioButtonGapsOnly.setText("gap reactions");
					jRadioButtonGapsOnly.setToolTipText("gap reactions");
				}

				{
					jRadioButtonUnbalancedOnly = new JRadioButton();
					jRadioButtonUnbalancedOnly.setEnabled(false);
					jPanelExport.add(jRadioButtonUnbalancedOnly, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jRadioButtonUnbalancedOnly.setText("unbalanced reactions");
					jRadioButtonUnbalancedOnly.setToolTipText("unbalanced reactions");
				}
			}
			{
				jPanelReactions = new JPanel();
				jPanel2.add(jPanelReactions, new GridBagConstraints(6, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jPanelReactions.setBorder(BorderFactory.createTitledBorder("reaction"));
				GridBagLayout jPanelReactionsLayout = new GridBagLayout();
				jPanelReactions.setLayout(jPanelReactionsLayout);
				//jPanelReactions.setBounds(718, 6, 157, 115);
				jPanelReactionsLayout.columnWidths = new int[] {7, 7};
				jPanelReactionsLayout.rowHeights = new int[] {7, 7};
				jPanelReactionsLayout.columnWeights = new double[] {0.1, 0.1};
				jPanelReactionsLayout.rowWeights = new double[] {0.1, 0.1};
				{
					jRadioButtonEncoded = new JRadioButton();
					jPanelReactions.add(jRadioButtonEncoded, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jRadioButtonEncoded.setText("in model");
					jRadioButtonEncoded.setToolTipText("in model");
					jRadioButtonEncoded.setSelected(this.reactionsInterface.existGenes());
					jRadioButtonEncoded.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent evt) {

							if(!encodedSelected) {

								previousSelectedPathway = pathsComboBox.getSelectedItem().toString();
								reactionsInterface.getPathsBoolean(jRadioButtonEncoded.isSelected());
								setPathwaysComboBox();
								fillList(true,false);
								previousSelectedPathway = null;
								encodedSelected=true;
							}
						}		
					});
				}
				{
					jRadioAllReactions = new JRadioButton();
					jPanelReactions.add(jRadioAllReactions, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jRadioAllReactions.setText("all reactions");
					jRadioAllReactions.setToolTipText("all reactions");
					jRadioAllReactions.setSelected(!this.reactionsInterface.existGenes());

					jRadioAllReactions.addActionListener(new ActionListener() {

						public void actionPerformed(ActionEvent evt) {

							if(encodedSelected) {

								previousSelectedPathway = pathsComboBox.getSelectedItem().toString();
								reactionsInterface.getPathsBoolean(jRadioButtonEncoded.isSelected());
								setPathwaysComboBox();
								fillList(true,false);
								previousSelectedPathway = null;
								encodedSelected=false;
							}
						}
					});
				}
			}
			this.pathsComboBox = new JComboBox<>(); 
			this.jPanelPaths.add(pathsComboBox, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			this.jViewInBrowser = new JButton();
			this.jViewInBrowser.setIcon(new CreateImageIcon(new ImageIcon((getClass().getClassLoader().getResource("icons/Web _256.png"))),0.1).resizeImageIcon());
			this.jViewInBrowser.setText("draw in browser");
			this.jViewInBrowser.setEnabled(false);
			this.jViewInBrowser.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent evt) {

					if(pathsComboBox.getSelectedIndex()>0 
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Transporters Pathway")
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Drains Pathway")
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Biomass Pathway")
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Non enzymatic")
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Spontaneous")) {

						String pathway_id = "map"+reactionsInterface.getPathwayCode(pathsComboBox.getSelectedItem().toString());
						String buildQuery = pathway_id;

						ReactionsInterfaceAux enzymes = reactionsInterface.get_enzymes_id_list(reactionsInterface.getPathwayID(pathsComboBox.getSelectedItem().toString()));

						if(enzymes.getEnzymes()!= null)
							for(String enzymes_id : enzymes.getEnzymes())
								buildQuery  = buildQuery.concat("/"+enzymes_id.split("___")[0]+"%09,green");


						if(enzymes.getReactions()!= null)
							for(String pathwayReaction : enzymes.getReactions()) {

								if(!pathwayReaction.startsWith("R_") && (pathwayReaction.startsWith("R") || pathwayReaction.startsWith("K")) && pathwayReaction.length()==6) {

									boolean red = false, cyan=false;

									if(reactionsInterface.getGapReactions()!=null && 
											reactionsInterface.getGapReactions().getNeighbourReactionsOriginal().contains(pathwayReaction))
										cyan=true;
									if(reactionsInterface.getGapReactions()!=null && 
											reactionsInterface.getGapReactions().getReactionsOriginal().containsKey(pathwayReaction))
										red=true;


									if(red)
										buildQuery  = buildQuery.concat("/"+pathwayReaction+"%09,red");
									else if (cyan)
										buildQuery  = buildQuery.concat("/"+pathwayReaction+"%09,cyan");
									else
										buildQuery  = buildQuery.concat("/"+pathwayReaction+"%09,blue");
								}
							}

						boolean noEnzymes = enzymes.getEnzymes().size()==0;

						ReactionsInterfaceAux reactions = reactionsInterface.getReactionsList(noEnzymes, reactionsInterface.getPathwayID(pathsComboBox.getSelectedItem().toString()));

						if(reactions.getReactions()!= null)
							for(String pathwayReaction : reactions.getReactions()) {

								if(!pathwayReaction.startsWith("R_") && (pathwayReaction.startsWith("R") || pathwayReaction.startsWith("K")) && pathwayReaction.length()==6) {

									boolean red = false, cyan=false;

									if(reactionsInterface.getGapReactions()!=null && 
											reactionsInterface.getGapReactions().getNeighbourReactionsOriginal().contains(pathwayReaction))
										cyan=true;
									if(reactionsInterface.getGapReactions()!=null && 
											reactionsInterface.getGapReactions().getReactionsOriginal().containsKey(pathwayReaction))
										red=true;

									if(red)
										buildQuery  = buildQuery.concat("/"+pathwayReaction+"%09,red");
									else if (cyan)
										buildQuery  = buildQuery.concat("/"+pathwayReaction+"%09,cyan");
									else
										buildQuery  = buildQuery.concat("/"+pathwayReaction+"%09,blue");
								}
							}


						if(reactionsInterface.getGapReactions()!=null) {

							Set<String> compounds = new HashSet<>();
							compounds.addAll(enzymes.getCompounds());
							compounds.addAll(reactions.getCompounds());

							for(String gapID : compounds)
								if(reactionsInterface.getGapReactions().getCompounds().contains(gapID))
									buildQuery  = buildQuery.concat("/"+gapID+"%09,red");
						}

						if(enzymes.getEnzymes().isEmpty() && reactions.getReactions().isEmpty()) {

							Workbench.getInstance().info("unfortunately the reactions listed as present in this pathway are being catalysed by enzymes\n" +
									"that although being encoded in the genome are not linked to this pathway.\n" +
									"therefore the KEGG pathway cannot be drawn.");
						}
						else {

							String url="http://www.kegg.jp/pathway/"+buildQuery;
							OpenBrowser  openUrl = new OpenBrowser();
							openUrl.setUrl(url);
							openUrl.openURL();
						}
					}
				}
			});
			this.jPanelPaths.add(jViewInBrowser, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

			this.buttonGroup = new ButtonGroup();
			this.buttonGroup.add(this.jRadioButtonEncoded);
			this.buttonGroup.add(this.jRadioAllReactions);

			this.buttonGroup3 = new ButtonGroup();
			this.buttonGroup3.add(this.jRadioButtonByPathway);
			this.buttonGroup3.add(this.jRadioButtonReactionsOnly);
			this.buttonGroup3.add(this.jRadioButtonGapsOnly);
			this.buttonGroup3.add(this.jRadioButtonUnbalancedOnly);

			this.jPanel2.add(searchInReaction.addPanel(), new GridBagConstraints(0, 0, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			this.jPanel1 = new JPanel();
			GridBagLayout thisLayout = new GridBagLayout();
			this.jPanel1.setLayout(thisLayout);
			this.add(this.jPanel1, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			this.jScrollPane1 = new JScrollPane();
			this.jScrollPane1.setViewportView(this.jTable);
			this.jPanel1.add(this.jScrollPane1,new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			this.setPreferredSize(new java.awt.Dimension(887, 713));
			this.setPathwaysComboBox();
			this.setTableModel(-1, null, -1);
		}
		catch (Exception e){e.printStackTrace();}
	}

	/**
	 * @param update
	 * @param keepRow
	 */
	public void fillList(boolean update, boolean keepRow) {

		try {

			int selectedRowFill = -1;
			int selectedRow = -1;
			String rowID = null;
			int newRow = -1;

			if(keepRow && jTable.getSelectedRow()>-1) {

				selectedRow = jTable.getSelectedRow();
				selectedRowFill = this.jTable.convertRowIndexToModel(selectedRow);

				if(specificPathwayData!=null)
					rowID=specificPathwayData.getRowID(selectedRowFill);
				else
					rowID=reactionsInterface.getRowID(selectedRowFill);
			}

			this.jTable.setAutoCreateRowSorter(true);
			this.searchInReaction.setMyJTable(jTable);
			this.searchInReaction.setSearchTextField("");

			if(update) {

				this.mainTableData = this.reactionsInterface.getReactionsData(this.jRadioButtonEncoded.isSelected());//, jComplete.isSelected());
				String selectedPathway = (String) this.pathsComboBox.getSelectedItem();
				this.setPathwaysComboBox();
				this.pathsComboBox.setSelectedItem(selectedPathway);
			}

			if(reactionsInterface.getGapReactions()!= null && reactionsInterface.getGapReactions().getReactions().size()>0)
				this.jRadioButtonGapsOnly.setEnabled(true);

			if(reactionsInterface.getBalanceValidator()!=null)
				this.jRadioButtonUnbalancedOnly.setEnabled(true);

			this.setTableModel(newRow, rowID, selectedRowFill);
			this.setTableColumnRatio();
			this.setButtonColumn();
		}
		catch(Exception e) {

			e.printStackTrace();
		}
	}

	private void setButtonColumn () {

		this.buttonColumn =  new ButtonColumn(jTable,0, this.buttonActionListener,this.buttonMouseAdapter, new ArrayList<>());
	}

	/**
	 * @return
	 */
	private ActionListener getButtonActionListener() {

		ActionListener actionListener = new ActionListener() {

			public void actionPerformed(ActionEvent arg0){
				processButton(arg0);
			}
		};
		return actionListener;
	}


	private MouseAdapter getButtonMouseAdapter() {

		MouseAdapter mouseAdapter =	new MouseAdapter() {

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
		};

		return mouseAdapter;
	}


	private void setTableColumnRatio() {

		Integer[] tableColumnsSize = this.reactionsInterface.getTableColumnsSize();
		
		TableColumnModel tc = jTable.getColumnModel();
		tc.getColumn(0).setMaxWidth(35);				//button
		tc.getColumn(0).setResizable(true);
		tc.getColumn(0).setModelIndex(0);
		
		if(tableColumnsSize!=null) {

			for(int i = 0 ; i < tableColumnsSize.length ; i++) {

				int j = tableColumnsSize[i];
				tc.getColumn(i+1).setPreferredWidth(j);
				tc.getColumn(i+1).setResizable(true);
				tc.getColumn(i+1).setMaxWidth(j);
			}
			jTable.setColumnModel(tc);
		}
	}


	/**
	 * @param newRow
	 * @param rowID
	 * @param selectedRowFill 
	 */
	private void setTableModel(int newRow, String rowID, int selectedRowFill) {

		if(this.pathsComboBox.getSelectedIndex()==0) {

			this.specificPathwayData = null;
			this.jTable.setModel(this.mainTableData);
			this.searchInReaction.setMainTableData(this.mainTableData);
			this.jTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
				private static final long serialVersionUID = 1L;

				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

					this.setHorizontalAlignment( SwingUtilities.CENTER );

					super.getTableCellRendererComponent(table, value, 
							isSelected, hasFocus, row, column);
					PathwayReaction qd = (PathwayReaction)mainTableData;

					if(isSelected) {

						setBackground(new Color(237, 240, 242));
						setForeground(Color.BLACK);
					}
					else {

						try {

							setBackground(reactionsInterface.getPathwayColors().get(new Integer(qd.getRowPathway(jTable.convertRowIndexToModel(row))).intValue()));

						} catch (IndexOutOfBoundsException e) {

							reactionsInterface.colorPaths();
						}
					}
					

					boolean red = false, cyan= false;

					if(jTable.getModel().getRowCount()>row) {

						String reactionName = (String) jTable.getValueAt(row, REACTION_NAME_COLUMN);
						if(reactionsInterface.getGapReactions()!=null && reactionsInterface.getGapReactions().getReactions().keySet().contains(reactionName))
							red=true;
						if(reactionsInterface.getGapReactions()!=null && reactionsInterface.getGapReactions().getNeighbourReactions().contains(reactionName))
							cyan=true;
					}

					if(red)
						setForeground(Color.red);
					else if(cyan)
						setForeground(Color.cyan);
					else
						setForeground(Color.BLACK);

					if(column==REACTION_NAME_COLUMN && reactionsInterface.getBalanceValidator()!= null &&
							!BalanceValidator.CORRECT_TAG_REACTION.equals(reactionsInterface.getBalanceValidator().getReactionTags().get(reactionsInterface.getExternalModelIds().get(jTable.getValueAt(row, REACTION_NAME_COLUMN))))
							&&
							!BalanceValidator.BALANCED_TAG_REACTION.equals(reactionsInterface.getBalanceValidator().getReactionTags().get(reactionsInterface.getExternalModelIds().get(jTable.getValueAt(row, REACTION_NAME_COLUMN))))
							) {
						setFont(new Font(getFont().getFontName(), Font.BOLD + Font.ITALIC, getFont().getSize()));
					}

					return this;             
				}
			});	

			newRow = reactionsInterface.getRowFromID(rowID);
			if(newRow>-1)
				selectedRowFill = this.jTable.convertRowIndexToModel(newRow);
			else
				selectedRowFill=-1;
		}
		else {

			PathwayReaction spd = (PathwayReaction)mainTableData;
			this.specificPathwayData = spd.getReactionsData(reactionsInterface.getSelectedPathIndexID().get(pathsComboBox.getSelectedIndex()));
			this.jTable.setModel(this.specificPathwayData);
			this.searchInReaction.setMainTableData(this.specificPathwayData);
			this.jTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
				private static final long serialVersionUID = 1L;
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column );

					this.setHorizontalAlignment( SwingUtilities.CENTER );

					if(isSelected) {

						setBackground(new Color(237, 240, 242));
						setForeground(Color.BLACK);
					}
					else {

						setBackground(reactionsInterface.getPathwayColors().get(reactionsInterface.getSelectedPathIndexID().get(pathsComboBox.getSelectedIndex())));
					}

					boolean red = false, cyan= false;
					
					if(jTable.getModel().getRowCount()>row) {
						
						String reactionName = (String) jTable.getValueAt(row, REACTION_NAME_COLUMN).toString();
						if(reactionsInterface.getGapReactions()!=null && reactionsInterface.getGapReactions().getReactions().keySet().contains(reactionName))
							red=true;
						if(reactionsInterface.getGapReactions()!=null && reactionsInterface.getGapReactions().getNeighbourReactions().contains(reactionName))
							cyan=true;
					} 

					if(red)
						setForeground(Color.red);
					else if(cyan)
						setForeground(Color.cyan);
					else
						setForeground(Color.BLACK);

					if(column==REACTION_NAME_COLUMN && reactionsInterface.getBalanceValidator()!= null &&
							!BalanceValidator.CORRECT_TAG_REACTION.equals(reactionsInterface.getBalanceValidator().getReactionTags().get(reactionsInterface.getExternalModelIds()
									.get(jTable.getValueAt(row, REACTION_NAME_COLUMN))))
							&&
							!BalanceValidator.BALANCED_TAG_REACTION.equals(reactionsInterface.getBalanceValidator().getReactionTags().get(reactionsInterface.getExternalModelIds()
									.get(jTable.getValueAt(row, REACTION_NAME_COLUMN))))
							) {
						setFont(new Font(getFont().getFontName(), Font.BOLD + Font.ITALIC, getFont().getSize()));
					}


					return this;             
				}
			});

			newRow = specificPathwayData.getRowFromID(rowID);
			if(newRow>-1)
				selectedRowFill = this.jTable.convertRowIndexToModel(newRow);
			else
				selectedRowFill=-1;
		}
		jTable.getModel().addTableModelListener(this.tableModelListener);
		this.jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.jTable.setRowSelectionAllowed(true);
		this.jScrollPane1.setViewportView(this.jTable);

		if(selectedRowFill>-1 && selectedRowFill<this.jTable.getRowCount()) {

			//this.jTable.scrollRectToVisible(this.jTable.getCellRect(selectedRowFill, -1, true));
			MerlinUtils.scrollToCenter(jTable,selectedRowFill, -1);
			DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
			selectionModel.setSelectionInterval(selectedRowFill, selectedRowFill);
			this.jTable.setSelectionModel(selectionModel);
		}
	}


	/**
	 * @return
	 */
	private TableModelListener getTableModelListener() {

		TableModelListener tableModelListener = new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {

				if(e.getType() == TableModelEvent.UPDATE) {

					if(jTable.getSelectedRow()>-1) {

						if (e.getFirstRow() == e.getLastRow()) {

							int rowNumber = e.getFirstRow();
							int columnNumber = e.getColumn();

							String id;

							if(pathsComboBox.getSelectedIndex()==0)
								id = ((PathwayReaction)mainTableData).getRowId(rowNumber);
							else
								id = specificPathwayData.getRowID(rowNumber);

							reactionsInterface.updateReactionProperties(id, columnNumber, jTable.getValueAt(rowNumber, columnNumber));
							
							if(//columnNumber==NOTES_COLUMN || 
									columnNumber==IS_REVERSIBLE_COLUMN) {

								fillList(true,true);

								if(columnNumber==IS_REVERSIBLE_COLUMN  && (Boolean)jTable.getValueAt(rowNumber, columnNumber))
									removeGapLabelling();
							}

							if(columnNumber>0 && columnNumber<IS_REVERSIBLE_COLUMN)
								searchInReaction.setSearchTextField("");

							if(columnNumber==IN_MODEL_COLUMN) { 

								MerlinUtils.updateReagentProductsView(reactionsInterface.getProject().getName());

								if(((Boolean) jTable.getValueAt(rowNumber, columnNumber))) 
									removeGapLabelling();
							}
						}
					}
				}
			}};
			return tableModelListener;
	}

	/**
	 * set the pathways selection box 
	 */
	private void setPathwaysComboBox() {
		
		this.reactionsInterface.getPathsBoolean(jRadioButtonEncoded.isSelected());
		
		this.pathsComboBox.setModel(new DefaultComboBoxModel<String>(this.reactionsInterface.getPaths()));

		if(this.previousSelectedPathway != null) {

			if(Arrays.asList(reactionsInterface.getPaths()).contains(previousSelectedPathway))
				pathsComboBox.setSelectedItem(previousSelectedPathway);
			else
				pathsComboBox.setSelectedIndex(0);
		}

		int width = (pathsComboBox.getSelectedItem().toString()).length()*7;

		if(width<200)
			pathsComboBox.setPreferredSize(new Dimension(200, 26));
		else
			pathsComboBox.setPreferredSize(new Dimension(width,26));

		this.pathsComboBox.setToolTipText(pathsComboBox.getSelectedItem().toString());

		this.pathsComboBox.addItemListener(comboBoxItemListener);


	}

	/**
	 * @return
	 */
	private ItemListener getComboBoxItemListener() {

		ItemListener itemListener = new ItemListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void itemStateChanged(ItemEvent arg0) {

				if(arg0.getStateChange()==ItemEvent.DESELECTED) {

					pathsComboBox.setToolTipText(((JComboBox<String>) arg0.getSource()).getSelectedItem().toString());
					int width = (pathsComboBox.getSelectedItem().toString()).length()*7;

					if(width<200)
						pathsComboBox.setPreferredSize(new Dimension(200, 26));
					else
						pathsComboBox.setPreferredSize(new Dimension(width,26));

					pathsComboBox.setToolTipText(pathsComboBox.getSelectedItem().toString());
					fillList(false,true);

					if(pathsComboBox.getSelectedIndex()>0
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Transporters Pathway")
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Drains Pathway")
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Biomass Pathway")
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Non enzymatic")
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("Spontaneous")
							&& !pathsComboBox.getSelectedItem().toString().equalsIgnoreCase("")) {

						jViewInBrowser.setEnabled(true);
					}
					else {

						jViewInBrowser.setEnabled(false);
					}
				}
			}
		};

		return itemListener;
	}

	/**
	 * @param arg0
	 */
	private void processButton(EventObject arg0) {

		JButton button = null;
		if(arg0.getClass()==ActionEvent.class)
			button = (JButton)((ActionEvent) arg0).getSource();

		if(arg0.getClass()==MouseEvent.class)
			button = (JButton)((MouseEvent) arg0).getSource();

		ListSelectionModel model = jTable.getSelectionModel();
		model.setSelectionInterval( buttonColumn.getSelectIndex(button), buttonColumn.getSelectIndex(button));

		jButtonClicked();
	}

	/**
	 * 
	 */
	private void jButtonClicked() {

		int id;

		if(pathsComboBox.getSelectedIndex()>0)
			id=Integer.parseInt(specificPathwayData.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow())));
		else
			id=Integer.parseInt(reactionsInterface.getIds().get(jTable.convertRowIndexToModel(jTable.getSelectedRow())));

		DataTable[] informationTable = reactionsInterface.getRowInfo(id);

		//getECsData(informationTable);
		//System.out.println("reaction:"+reactionsInterface.getReactionName(id)+"--->"+getECsData(informationTable));
		new GenericDetailWindow(informationTable, "reaction data", "reaction: "+reactionsInterface.getReactionName(id), reactionsInterface.getFormula(id));
	}



	/**
	 * 
	 */
	public void removeGapLabelling() {

		this.jTable.setForeground(Color.BLACK);
		this.jRadioButtonGapsOnly.setEnabled(false);
		this.jRadioButtonUnbalancedOnly.setEnabled(false);
		this.reactionsInterface.setExternalModelIds(new HashMap<String,String>());
		this.reactionsInterface.setBalanceValidator(null);
		this.reactionsInterface.setGapReactions(null);
	}

	/**
	 * 
	 */
	//	private void tableSizes() {
	//
	//		for(int i = 0 ; i < tableColumnsSize.length ; i++) {
	//
	//			tableColumnsSize[i]=jTable.getColumnModel().getColumn(i+1).getWidth();
	//		}
	//
	//		this.selectedRow = this.jTable.convertRowIndexToModel(jTable.getSelectedRow());
	//		this.reactionsContainer.setTableColumnsSize(this.tableColumnsSize);
	//	}

	/**
	 * @param path
	 * 
	 * Export Data to xls tabbed files
	 * 
	 */
	public void exportToXls(String path) {

		String file = System.getProperty("user.home");

		if(!path.equals(""))
			file=path;

		Calendar cal = new GregorianCalendar();

		// Get the components of the time
		int hour24 = cal.get(Calendar.HOUR_OF_DAY);     // 0..23
		int min = cal.get(Calendar.MINUTE);             // 0..59
		int day = cal.get(Calendar.DAY_OF_YEAR);		//0..365

		String auxString = "";

		if(jRadioButtonGapsOnly.isSelected())
			auxString = "gaps_";

		if(jRadioButtonUnbalancedOnly.isSelected())
			auxString = "unbalanced_";

		file+="/reactions_"+reactionsInterface.getProject().getName()+"_"+auxString+hour24+"_"+min+"_"+day+".xls";


		try {

			FileWriter fw = new FileWriter (file);
			BufferedWriter bw = new BufferedWriter(fw);
			Set<String> aux = new HashSet<String>();

			String header ="";
			TableColumnModel tc = jTable.getColumnModel();
			int headerSize = tc.getColumnCount();
			DataTable export;

			int h;
			if(pathsComboBox.getSelectedIndex()==0) {

				export=this.mainTableData;

				if(jRadioButtonByPathway.isSelected())
					h = 1; // skip info column
				else {

					h = 2;
					header="\t";
				}

			}
			else {

				export=this.specificPathwayData;
				h = 2; // skip info column
				header="\t";
			}

			while (h < headerSize) {

				header+=tc.getColumn(h).getHeaderValue().toString()+"\t";
				h++;
			}
			bw.write(header);
			bw.newLine();

			for(int i=0; i < export.getTable().size(); i++) {			

				int j=1;// j=1 because the first column (j=0) is the info column

				String stringBuffer = new String();

				boolean newLine = false;
				while (j < export.getRow(i).length) {

					if(jRadioButtonByPathway.isSelected()) {

						stringBuffer = stringBuffer.concat((export.getRow(i)[j])+"").concat("\t");
						newLine = true;
					}
					else {

						if(j+1 < export.getRow(i).length && !aux.contains(export.getRow(i)[2].toString())) {

							stringBuffer = stringBuffer.concat((export.getRow(i)[j+1])+"").concat("\t");
							newLine = true;
						}
						//	if(j+1 == export.getRow(i).length){bw.write("\n");}
					}
					j++;
				}

				if(newLine) {

					if(this.jRadioButtonGapsOnly.isSelected()) {

						if(reactionsInterface.getGapReactions()!= null && 
								(this.reactionsInterface.getGapReactions().getReactions().containsKey(export.getRow(i)[2])
										|| this.reactionsInterface.getGapReactions().getNeighbourReactions().contains(export.getRow(i)[2]))) {

							bw.write(stringBuffer);
							bw.newLine();
						}
					}
					else if(this.jRadioButtonUnbalancedOnly.isSelected()) {

						if(reactionsInterface.getBalanceValidator()!= null &&
								!BalanceValidator.CORRECT_TAG_REACTION.equals(reactionsInterface.getBalanceValidator().getReactionTags().get(reactionsInterface.getExternalModelIds().get(export.getRow(i)[2].toString())))
								&&
								!BalanceValidator.BALANCED_TAG_REACTION.equals(reactionsInterface.getBalanceValidator().getReactionTags().get(reactionsInterface.getExternalModelIds().get(export.getRow(i)[2].toString())))
								) {

							bw.write(stringBuffer);
							bw.newLine();
						}
					}
					else {

						bw.write(stringBuffer);
						bw.newLine();
					}
				}
				aux.add(export.getRow(i)[2].toString());
			}
			bw.flush();
			bw.close();

			Workbench.getInstance().info("Data successfully exported.");
		} 
		catch (Exception e)  {

			Workbench.getInstance().error("An error occurred while performing this operation. Error "+e.getMessage());
			e.printStackTrace();
		}

	}
	
	/**
	 * @param path
	 * 
	 * Export Data to xls tabbed files
	 * 
	 */
	public void exportToXls2(String path) {

		String file = System.getProperty("user.home");

		if(!path.equals(""))
			file=path;

		Calendar cal = new GregorianCalendar();

		// Get the components of the time
		int hour24 = cal.get(Calendar.HOUR_OF_DAY);     // 0..23
		int min = cal.get(Calendar.MINUTE);             // 0..59
		int day = cal.get(Calendar.DAY_OF_YEAR);		//0..365

		String auxString = "";

		if(jRadioButtonGapsOnly.isSelected())
			auxString = "gaps_";

		if(jRadioButtonUnbalancedOnly.isSelected())
			auxString = "unbalanced_";

		file+="/reactions_"+reactionsInterface.getProject().getName()+"_"+auxString+hour24+"_"+min+"_"+day+".xls";

		String sheetName = "Sheet1";//name of sheet

		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet(sheetName) ;

		HSSFRow row = sheet.createRow(0);
		
		try {

			Set<String> aux = new HashSet<String>();

			TableColumnModel tc = jTable.getColumnModel();
			int headerSize = tc.getColumnCount();
			DataTable export;

			int h;
			if(pathsComboBox.getSelectedIndex()==0) {

				export=this.mainTableData;

				if(jRadioButtonByPathway.isSelected())
					h = 1; // skip info column
				else {

					h = 2;
				}

			}
			else {

				export=this.specificPathwayData;
				h = 2; // skip info column
			}

			int col = 0;
			
			while (h < headerSize) {

				row.createCell(col).setCellValue(tc.getColumn(h).getHeaderValue().toString());
				h++;
				col++;
			}
			
			boolean newLine = false;
			
			int addLine = 2;
			
			for(int i=0; i < export.getTable().size(); i++) {
				
				row = sheet.createRow(addLine);
				
				int j=1;// j=1 because the first column (j=0) is the info column

				String stringBuffer = new String();
				
				while (j < export.getRow(i).length) {
					
					if(jRadioButtonByPathway.isSelected()) {

						stringBuffer = ((export.getRow(i)[j])+"");
						
						if(!stringBuffer.isEmpty()){
							
							row.createCell(j-1).setCellValue(stringBuffer);
							newLine = true;
						}
						
					}
					
					else {

						if(j+1 < export.getRow(i).length && !aux.contains(export.getRow(i)[2].toString())) {

							stringBuffer = ((export.getRow(i)[j+1])+"");
							
							if(!stringBuffer.isEmpty()){
								
								if(this.jRadioButtonGapsOnly.isSelected() && j < export.getRow(i).length-1) {

									if(reactionsInterface.getGapReactions()!= null && 
											(this.reactionsInterface.getGapReactions().getReactions().containsKey(export.getRow(i)[2])
													|| this.reactionsInterface.getGapReactions().getNeighbourReactions().contains(export.getRow(i)[2]))) {
										
										stringBuffer = ((export.getRow(i)[j+1])+"");

										if(!stringBuffer.isEmpty()){
											row.createCell(j-1).setCellValue(stringBuffer);
											newLine = true;
										}
									}
								}
								else if(this.jRadioButtonUnbalancedOnly.isSelected() && j < export.getRow(i).length-1) {

									if(reactionsInterface.getBalanceValidator()!= null &&
											!BalanceValidator.CORRECT_TAG_REACTION.equals(reactionsInterface.getBalanceValidator().getReactionTags().get(reactionsInterface.getExternalModelIds().get(export.getRow(i)[2].toString())))
											&&
											!BalanceValidator.BALANCED_TAG_REACTION.equals(reactionsInterface.getBalanceValidator().getReactionTags().get(reactionsInterface.getExternalModelIds().get(export.getRow(i)[2].toString())))
											) {
										
										stringBuffer = ((export.getRow(i)[j+1])+"");

										if(!stringBuffer.isEmpty()){
											row.createCell(j-1).setCellValue(stringBuffer);
											newLine = true;							
											}
									}
								}
								else{
									row.createCell(j-1).setCellValue(stringBuffer);
									newLine = true;	
								}
							}
						}
					}
					j++;
					
				}
				
				if(newLine){
					newLine = false;
					addLine++;
				}

				aux.add(export.getRow(i)[2].toString());
			}
			
			FileOutputStream fileOut = new FileOutputStream(file);
			
			//write this workbook to an Outputstream.
			wb.write(fileOut);
			fileOut.flush();
			wb.close();
			fileOut.close();

			Workbench.getInstance().info("Data successfully exported.");
		} 
		catch (Exception e)  {

			Workbench.getInstance().error("An error occurred while performing this operation. Error "+e.getMessage());
			e.printStackTrace();
		}

	}
	

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#updateGraphicalObject()
	 */
	@Override
	public void updateTableUI() {
		
		if(!this.reactionsInterface.isNewGaps()) {

			this.removeGapLabelling();
			this.reactionsInterface.setNewGaps(false);
		}
		
		if(this.reactionsInterface.getGapReactions() != null)
			jRadioButtonGapsOnly.setEnabled(true);
		
		if(reactionsInterface.getBalanceValidator()!=null)
			this.jRadioButtonUnbalancedOnly.setEnabled(true);

		this.reactionsInterface.setPaths(reactionsInterface.getPathsBoolean(jRadioButtonEncoded.isSelected()));
		this.updateUI();
		this.revalidate();
		this.repaint();
		this.fillList(true, this.myKeepRow);
		
	}

	public void internalUpdateTableUI() {

		this.removeGapLabelling();
		this.reactionsInterface.setPaths(reactionsInterface.getPathsBoolean(jRadioButtonEncoded.isSelected()));
		this.fillList(true, false);
		this.updateUI();
		this.revalidate();
		this.repaint();
	}

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#addListenersToGraphicalObjects(javax.swing.JPanel, javax.swing.MyJTable)
	 */
	@Override
	public void addListenersToGraphicalObjects() {}
	
	private static boolean removeReactionConfirmation(){

		int i =CustomGUI.stopQuestion("remove reaction?",
				"are you sure you want to remove the selected reaction?",
				new String[]{"yes", "no"});


		switch (i) {

			case 0:return true;
			case 1:return false;
		}
		
		return false;
	}

}
