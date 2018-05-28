package pt.uminho.ceb.biosystems.merlin.core.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.NumberFormatter;

import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactionsInterface;
import pt.uminho.ceb.biosystems.merlin.core.utilities.CreateImageIcon;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.InformationType;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.MetaboliteContainer;
import pt.uminho.ceb.biosystems.merlin.utilities.containers.model.ReactionContainer;

/**
 * @author ODias
 *
 */
public class InsertEditReaction extends JDialog {

	private static final long serialVersionUID = -1L;
	private ReactionsInterface reactionsInterface;

	private String selectedPathway, defaultCompartment;
	private int rowID;
	private String[] enzymesModel, pathwaysModel, reactants, reactantsStoichiometry, productsStoichiometry,
	products, reactantsChains, productsChains, reactantsCompartments, productsCompartments, 
	metabolitesCompartmentsModel, reactionsCompartmentsModel, genesModel;
	private String[][] metabolitesModel;
	private boolean applyPressed;
	protected boolean inModel;
	private JPanel jPanelDialogReaction, jPanelName, jPanelEquation, jPanelReversible, jPanelSaveClose,
	panelReactants, panelProducts, jPanelCompartmentReaction;
	private JTextField jTextFieldName, jTextFieldEquation;
	private JCheckBox jNonEnzymatic, jSpontaneous, jIsGeneric, jCheckBoxInModel;
	private JComboBox<String> jComboBoxLocalisation;
	private JRadioButton jBackword, jForward, jRadioButtonReversible, jRadioButtonIrreversible;
	private JScrollPane jScrollPaneEnzymes, jScrollPanePathways, jScrollPaneReactants, jScrollPaneProducts, jScrollPaneGeneRule;
	private JButton jApply, jButtonSave, jButtonCancel;
	private List<JComboBox<String>> reactantsField, productsCompartmentsBox, enzymesField, pathwaysField, reactantsCompartmentsBox, productsField;
	private List<List<JComboBox<String>>> genesField;
	private JTextField[] reactantsStoichiometryField, productsStoichiometryField, reactantsChainsField, productsChainsField;
	private JFormattedTextField lowerBoundary, upperBoundary;
	private ButtonGroup reversibility, direction;
	private boolean applied = false;

	private Map<String, MetaboliteContainer> reactionMetabolites;
	protected Map<String, Set<String>> selectedEnzymesAndPathway;
	private List<List<String>> geneRule_AND_OR;

	/**
	 * @param reactionsInterface
	 * @param rowID
	 */
	public InsertEditReaction(ReactionsInterface reactionsInterface, int rowID) {

		super(Workbench.getInstance().getMainFrame());
		this.reactionsInterface = reactionsInterface;
		this.applyPressed=false;
		this.metabolitesModel = reactionsInterface.getAllMetabolites();
		boolean isCompartmentalisedModel = reactionsInterface.getProject().isCompartmentalisedModel();
		this.metabolitesCompartmentsModel = reactionsInterface.getCompartments(true, isCompartmentalisedModel);
		this.reactionsCompartmentsModel = reactionsInterface.getCompartments(false, isCompartmentalisedModel);
		this.pathwaysModel = reactionsInterface.getPathways(false);
		this.enzymesModel = reactionsInterface.getEnzymesModel();
		this.genesModel = reactionsInterface.getGenesModel();
		Set<String> enzymesSet = new TreeSet<String>();
		Set<String> allEnzymes = new TreeSet<String>();
		this.selectedPathway="-1allpathwaysinreaction";
		this.selectedEnzymesAndPathway = new TreeMap<String, Set<String>>();
		this.geneRule_AND_OR = new ArrayList<>();
		this.defaultCompartment = reactionsInterface.getDefaultCompartment();

		this.rowID=rowID;

		if(rowID == -10) {

			this.setTitle("Insert Reaction");
			enzymesSet=new TreeSet<String>();
			this.selectedEnzymesAndPathway.put("-1allpathwaysinreaction", enzymesSet);
			//this.selectedEnzymesAndPathway.put("", new HashSet<String>());
		}
		else {

			this.setTitle("Edit Reaction");
			String[] pathways = reactionsInterface.getPathways(rowID);

			if(pathways == null || pathways.length==0) {

				allEnzymes = reactionsInterface.getEnzymesForReaction(rowID);
			} 
			else {

				for(String pathway : pathways) {

					enzymesSet=new TreeSet<String>();
					enzymesSet.addAll(new TreeSet<String>(Arrays.asList(reactionsInterface.getEnzymes(rowID, reactionsInterface.getPathwayID(pathway)))));
					allEnzymes.addAll(enzymesSet);
					this.selectedEnzymesAndPathway.put(pathway, enzymesSet);
				}
			}

			this.selectedEnzymesAndPathway.put(this.selectedPathway, allEnzymes);
		}

		initGUI();

		if(rowID == -10) {

			this.jRadioButtonReversible.setSelected(true);
			this.jCheckBoxInModel.setSelected(true);
		}
		else {

			this.startFields();
		}

		enzymesSet=new TreeSet<String>();
		Utilities.centerOnOwner(this);
		this.setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
		this.setVisible(true);		
		this.setAlwaysOnTop(true);
		this.toFront();
		Utilities.centerOnOwner(this);
	}

	/**
	 * 
	 */
	private void initGUI() {

		GroupLayout thisLayout = new GroupLayout((JPanel)getContentPane());
		this.getContentPane().setLayout(thisLayout);
		this.jPanelDialogReaction = new JPanel(new GridBagLayout());
		thisLayout.setVerticalGroup(thisLayout.createSequentialGroup()
				.addContainerGap().addComponent(jPanelDialogReaction, 0, 740, Short.MAX_VALUE));
		thisLayout.setHorizontalGroup(thisLayout.createSequentialGroup().addComponent(jPanelDialogReaction, 0, 1000, Short.MAX_VALUE));

		GridBagLayout jPanelDialogReactionLayout = new GridBagLayout();
		jPanelDialogReactionLayout.columnWeights = new double[] {0.0, 0.1, 0.1, 0.1, 0.0, 0.1, 0.0, 0.1, 0.1, 0.0};
		jPanelDialogReactionLayout.columnWidths = new int[] {8, 450, 15, 10, 7, 150, 50, 200, 15, 8};
		jPanelDialogReactionLayout.rowWeights = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.0, 0.1, 0.0, 0.0, 0.1, 0.0, 0.0};
		jPanelDialogReactionLayout.rowHeights = new int[] {7, 10, 10, 7, 20, 7, 15, 7, 12, 145, 12, 170, 7, 12, 170, 10, 15};
		this.jPanelDialogReaction.setLayout(jPanelDialogReactionLayout);

		{
			jPanelName = new JPanel();
			GridBagLayout jPanelNameLayout = new GridBagLayout();
			this.jPanelDialogReaction.add(this.jPanelName, new GridBagConstraints(1, 1, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jPanelNameLayout.rowWeights = new double[] {0.1};
			jPanelNameLayout.rowHeights = new int[] {7};
			jPanelNameLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			jPanelNameLayout.columnWidths = new int[] {7, 7, 7};
			this.jPanelName.setLayout(jPanelNameLayout);
			this.jPanelName.setBorder(BorderFactory.createTitledBorder(null, "Reaction Name", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
			{
				this.jTextFieldName = new JTextField();
				this.jTextFieldName.setPreferredSize(new Dimension(180,26));
				this.jPanelName.add(this.jTextFieldName, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			}
		}
		{
			jScrollPanePathways = new JScrollPane();
			jScrollPanePathways.setViewportView(addPathways());
			jPanelDialogReaction.add(jScrollPanePathways, new GridBagConstraints(1, 8, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jPanelDialogReaction.add(jButtonAddPathways(), new GridBagConstraints(2, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			JPanel boundaries = new JPanel();
			GridBagLayout jPanelBoundariesLayout = new GridBagLayout();
			jPanelBoundariesLayout.rowWeights = new double[] {0.1, 0.0, 0.1};
			jPanelBoundariesLayout.rowHeights = new int[] {20, 7, 20};
			jPanelBoundariesLayout.columnWeights = new double[] {0.1, 0.0, 0.1};
			jPanelBoundariesLayout.columnWidths = new int[] {20, 7, 20};
			boundaries.setLayout(jPanelBoundariesLayout);
			boundaries.setBorder(BorderFactory.createTitledBorder(null, "Flux Boundaries", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
			jPanelDialogReaction.add(boundaries, new GridBagConstraints(5, 5, 1, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			JLabel lowerBoundaries = new JLabel("Lower");
			boundaries.add(lowerBoundaries, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			JLabel upperBoundaries = new JLabel("Upper");
			boundaries.add(upperBoundaries, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
			format.setGroupingUsed(false);
			NumberFormatter formatter = new NumberFormatter(format);
			formatter.setValueClass(Double.class);
			formatter.setMinimum((-Double.MAX_VALUE));
			formatter.setMaximum(Double.MAX_VALUE);
			formatter.setCommitsOnValidEdit(true);

			this.lowerBoundary = new JFormattedTextField(formatter);
			this.lowerBoundary.setText("-10000");
			boundaries.add(this.lowerBoundary, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			this.upperBoundary = new JFormattedTextField(formatter);
			this.upperBoundary.setText("10000");
			boundaries.add(this.upperBoundary, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			jPanelReversible = new JPanel();
			GridBagLayout jPanelReversibleLayout = new GridBagLayout();
			jPanelDialogReaction.add(jPanelReversible, new GridBagConstraints(5, 1, 1, 4, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jPanelReversibleLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
			jPanelReversibleLayout.rowHeights = new int[] {7, 20, 20};
			jPanelReversibleLayout.columnWeights = new double[] {0.0, 0.1};
			jPanelReversibleLayout.columnWidths = new int[] {75, 20};
			jPanelReversible.setLayout(jPanelReversibleLayout);
			jPanelReversible.setBorder(BorderFactory.createTitledBorder(null, "Reversibility", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
			{
				jRadioButtonReversible = new JRadioButton();
				jPanelReversible.add(jRadioButtonReversible, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jRadioButtonReversible.setText("Reversible");
				jRadioButtonReversible.setToolTipText("Reversible");
				jRadioButtonReversible.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {

						if(jRadioButtonReversible.isSelected()) {

							String equation = jTextFieldEquation.getText().replace(" <= ", " <=> ").replace(" => ", " <=> ");
							jTextFieldEquation.setText(equation);
							jTextFieldEquation.setToolTipText(equation);
							lowerBoundary.setText("-10000");
							jForward.setEnabled(false);
							jBackword.setEnabled(false);
							panelReactants.setBorder(BorderFactory.createTitledBorder(null, "Reactants", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
							panelProducts.setBorder(BorderFactory.createTitledBorder(null, "Products", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
						}
					}
				});
			}
			{
				jRadioButtonIrreversible = new JRadioButton();
				jPanelReversible.add(jRadioButtonIrreversible, new GridBagConstraints(0, 1, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jRadioButtonIrreversible.setText("Irreversible");
				jRadioButtonIrreversible.setToolTipText("Irreversible");
				jRadioButtonIrreversible.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {

						if(jRadioButtonIrreversible.isSelected()) {

							jForward.setEnabled(true);
							jForward.setSelected(true);
							String equation  = jTextFieldEquation.getText().replace(" <=> ", " => ").replace(" <= ", " => ");
							jTextFieldEquation.setText(equation);
							jTextFieldEquation.setToolTipText(equation);
							lowerBoundary.setText("0");
							jBackword.setEnabled(true);
							panelReactants.setBorder(BorderFactory.createTitledBorder(null, "Reactants", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
							panelProducts.setBorder(BorderFactory.createTitledBorder(null, "Products", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
						}
					}
				});
			}
			{
				reversibility = new ButtonGroup();
				reversibility.add(jRadioButtonIrreversible);
				reversibility.add(jRadioButtonReversible);
			}

			{
				jForward = new JRadioButton();
				jForward.setEnabled(false);
				jForward.setText("=>");
				jForward.setToolTipText("Forward");
				jPanelReversible.add(jForward, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jForward.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {

						if(jForward.isSelected()) {

							String equation  = jTextFieldEquation.getText().replace(" <=> ", " => ").replace(" <= ", " => ");
							jTextFieldEquation.setText(equation);
							jTextFieldEquation.setToolTipText(equation);
							panelReactants.setBorder(BorderFactory.createTitledBorder(null, "Reactants", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
							panelProducts.setBorder(BorderFactory.createTitledBorder(null, "Products", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
						}
					}
				});

				jBackword = new JRadioButton();
				jBackword.setEnabled(false);
				jBackword.setText("<=");
				jBackword.setToolTipText("Backward");
				jPanelReversible.add(jBackword, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				jBackword.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {

						if(jBackword.isSelected()) {

							String equation = jTextFieldEquation.getText().replace(" <=> ", " <= ").replace(" => ", " <= ");
							jTextFieldEquation.setText(equation);
							jTextFieldEquation.setToolTipText(equation);
							panelReactants.setBorder(BorderFactory.createTitledBorder(null, "Products", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
							panelProducts.setBorder(BorderFactory.createTitledBorder(null, "Reactants", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
						}
					}
				});
			}
			{
				direction = new ButtonGroup();
				direction.add(jForward);
				direction.add(jBackword);
			}
		}

		{
			jPanelEquation = new JPanel();
			GridBagLayout jPanelEquationLayout = new GridBagLayout();
			jPanelDialogReaction.add(jPanelEquation, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			jPanelEquationLayout.rowWeights = new double[] {0.1};
			jPanelEquationLayout.rowHeights = new int[] {7};
			jPanelEquationLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			jPanelEquationLayout.columnWidths = new int[] {7, 7, 7};
			jPanelEquation.setLayout(jPanelEquationLayout);
			jPanelEquation.setBorder(BorderFactory.createTitledBorder(null, "Equation", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
			{
				jTextFieldEquation = new JTextField();
				jPanelEquation.add(jTextFieldEquation, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jTextFieldEquation.setToolTipText(jTextFieldEquation.getText());
			}
		}

		{
			jPanelSaveClose = new JPanel(new GridBagLayout());
			GridBagLayout jPanelSaveCloseLayout = new GridBagLayout();
			jPanelSaveCloseLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
			jPanelSaveCloseLayout.rowHeights = new int[] {5, 10, 5};
			jPanelSaveCloseLayout.columnWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
			jPanelSaveCloseLayout.columnWidths = new int[] {8, 86, 13, 86, 13, 86, 8};
			//set layout
			jPanelSaveClose.setLayout(jPanelSaveCloseLayout);
			{
				jButtonSave = new JButton();
				jPanelSaveClose.add(jButtonSave, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jButtonSave.setText("Save");
				jButtonSave.setToolTipText("Save");
				jButtonSave.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Save.png")),0.1).resizeImageIcon());
				jButtonSave.addActionListener(new ActionListener(){

					@Override
					public void actionPerformed(ActionEvent e) {
						//	boolean metabolites = false;

						if(jTextFieldName.getText().isEmpty()) {

							Workbench.getInstance().warn("Please name the reaction.");
						}
						else {
							if(!applied)
								saveData();
							
							closeAndUpdate();
						}
					}
				});
			}
			{
				jButtonCancel = new JButton();
				jPanelSaveClose.add(jButtonCancel, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jButtonCancel.setText("Close");
				jButtonCancel.setToolTipText("Close");
				jButtonCancel.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")), 0.1).resizeImageIcon());
				jButtonCancel.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent arg0) {
						if(applyPressed) {

							closeAndUpdate();
						}
						else {

							close();
						}
					}
				});			}
			{
				jApply = new JButton();
				jPanelSaveClose.add(jApply, new GridBagConstraints(5, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				jApply.setText("Apply");
				jApply.setToolTipText("Apply");
				jApply.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")), 0.1).resizeImageIcon());
				jApply.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						//						boolean metabolites = false;

						if(jTextFieldName.getText().isEmpty()) {

							Workbench.getInstance().warn("Please name the reaction.");
						}
						else {
							
							saveData();
							rowID = reactionsInterface.getReactionID(jTextFieldName.getText());
							if(rowID<0)
								rowID = reactionsInterface.getReactionID("R_"+jTextFieldName.getText());
							if(rowID<0)
								rowID = reactionsInterface.getReactionID("T_"+jTextFieldName.getText());
							
							applied = true;
						}
					}
				});

			}
			jPanelDialogReaction.add(jPanelSaveClose, new GridBagConstraints(1, 17, 7, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}

		{
			reactants = new String[0]; //reactants = new String[2][0];
			reactantsStoichiometry = new String[0];
			reactantsChains= new String[0]; reactantsCompartments= new String[0];
			products = new String[0]; //products = new String[2][0];
			productsStoichiometry = new String[0];
			productsChains= new String[0];	productsCompartments= new String[0];

			jScrollPaneReactants = new JScrollPane();
			panelReactants = this.addReactantsPanel();
			jScrollPaneReactants.setViewportView(panelReactants);
			jScrollPaneProducts = new JScrollPane();
			panelProducts = this.addProductsPanel();
			jScrollPaneProducts.setViewportView(panelProducts);
			
			jPanelDialogReaction.add(jScrollPaneReactants, new GridBagConstraints(1, 10, 7, 2, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jPanelDialogReaction.add(jScrollPaneProducts, new GridBagConstraints(1, 13, 7, 2, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}

		//enzymes pane
		{
			jScrollPaneEnzymes = new JScrollPane();
			jScrollPaneEnzymes.setViewportView(addEnzymes());
			jPanelDialogReaction.add(jScrollPaneEnzymes, new GridBagConstraints(3, 8, 5, 2, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jPanelDialogReaction.add(jButtonAddEnzyme(), new GridBagConstraints(8, 8, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		//ButtonAddReactant
		{
			jPanelDialogReaction.add(jButtonAddReactant(), new GridBagConstraints(8, 10, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		//ButtonAddProduct
		{
			jPanelDialogReaction.add(jButtonAddProduct(), new GridBagConstraints(8, 13, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		}
		//  Check Box In Model
		{

			GridBagLayout jPanelPropertiesLayout = new GridBagLayout();
			jPanelPropertiesLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
			jPanelPropertiesLayout.rowHeights = new int[] {10, 5, 10, 5, 10, 5, 10};
			jPanelPropertiesLayout.columnWeights = new double[] {0.1, 0.1};
			jPanelPropertiesLayout.columnWidths = new int[] {20, 20};
			JPanel jPanelProperties = new JPanel(jPanelPropertiesLayout);
			jPanelProperties.setBorder(BorderFactory.createTitledBorder(null, "Properties", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
			jPanelDialogReaction.add(jPanelProperties, new GridBagConstraints(3, 1, 2, 6, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

			jCheckBoxInModel = new JCheckBox();
			jCheckBoxInModel.setText("In Model");
			jCheckBoxInModel.setToolTipText("In Model");
			jPanelProperties.add(jCheckBoxInModel, new GridBagConstraints(2, 0, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jSpontaneous = new JCheckBox();
			jSpontaneous.setText("Spontaneous");
			jSpontaneous.setToolTipText("Spontaneous");
			jPanelProperties.add(jSpontaneous, new GridBagConstraints(2, 2, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jNonEnzymatic = new JCheckBox();
			jNonEnzymatic.setText("Non Enzymatic");
			jNonEnzymatic.setToolTipText("Non Enzymatic");
			jPanelProperties.add(jNonEnzymatic, new GridBagConstraints(2, 4, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jIsGeneric = new JCheckBox();
			jIsGeneric.setText("Is Generic");
			jIsGeneric.setToolTipText("Is Generic");
			jPanelProperties.add(jIsGeneric, new GridBagConstraints(2, 6, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		}
		//reactionlocalisation
		{
			ComboBoxModel<String> jComboBoxLocalisationModel = new DefaultComboBoxModel<>(reactionsCompartmentsModel);
			jComboBoxLocalisation = new JComboBox<>();
			jComboBoxLocalisation.setModel(jComboBoxLocalisationModel);
			jPanelCompartmentReaction = new JPanel();
			GridBagLayout jPanelCompartmentReactionLayout = new GridBagLayout();
			jPanelCompartmentReaction.setBorder(BorderFactory.createTitledBorder(BorderFactory.createTitledBorder(""), "Localisation", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
			jPanelCompartmentReactionLayout.rowWeights = new double[] {0.0};
			jPanelCompartmentReactionLayout.rowHeights = new int[] {7};
			jPanelCompartmentReactionLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			jPanelCompartmentReactionLayout.columnWidths = new int[] {7, 7, 7};
			jPanelCompartmentReaction.setLayout(jPanelCompartmentReactionLayout);
			jPanelCompartmentReaction.add(jComboBoxLocalisation, new GridBagConstraints(1, -1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jPanelDialogReaction.add(jPanelCompartmentReaction, new GridBagConstraints(1, 6, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}

		{
			jScrollPaneGeneRule = new JScrollPane();
			jScrollPaneGeneRule.setViewportView(addGeneRules());
			jPanelDialogReaction.add(jScrollPaneGeneRule, new GridBagConstraints(6, 1, 2, 6, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jPanelDialogReaction.add(jButtonAddGeneRule_OR(), new GridBagConstraints(8, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}

		this.setModal(true);
		pack();

	}

	/**
	 * @param row 
	 * @return
	 */
	private JButton jButtonAddGeneRule_AND(int row) {

		JButton jButtonAddGeneRule = new JButton();
		jButtonAddGeneRule.setIcon((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")), 0.04).resizeImageIcon()));
		jButtonAddGeneRule.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {

				List<String> rule = geneRule_AND_OR.get(row);
				rule.add("");
				geneRule_AND_OR.set(row,rule);
				jScrollPaneGeneRule.setViewportView(addGeneRules());
			}
		});
		return jButtonAddGeneRule;
	}

	/**
	 * @return
	 */
	private JButton jButtonAddGeneRule_OR() {

		JButton jButtonAddGeneRule = new JButton();
		jButtonAddGeneRule.setIcon((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")), 0.04).resizeImageIcon()));
		jButtonAddGeneRule.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {

				List<String> rule = new ArrayList<String>();
				rule.add("");
				geneRule_AND_OR.add(rule);
				jScrollPaneGeneRule.setViewportView(addGeneRules());
			}
		});
		return jButtonAddGeneRule;
	}

	/**
	 * @return
	 */
	private JPanel addGeneRules() {

		JPanel jPanelGeneRule;
		jPanelGeneRule = new JPanel();
		GridBagLayout jPanelGeneRuleLayout = new GridBagLayout();
		jPanelGeneRuleLayout.columnWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1};
		jPanelGeneRuleLayout.columnWidths = new int[] {7, 7, 7, 7, 7};
		jPanelGeneRuleLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
		jPanelGeneRuleLayout.rowHeights = new int[] {7, 7, 7};

		jPanelGeneRule.setBorder(BorderFactory.createTitledBorder(null, "Gene Rule", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));

		int rowsSize = geneRule_AND_OR.size();
		int columnsSize = 0;

		for(List<String> list : this.geneRule_AND_OR)
			if(list.size()>columnsSize)
				columnsSize = list.size();

		if(rowsSize>0) {

			int rowsLength = rowsSize*3+rowsSize-1;
			jPanelGeneRuleLayout.rowWeights = new double[rowsLength];
			jPanelGeneRuleLayout.rowHeights  = new int[rowsLength];

			for(int rh=0; rh<rowsLength; rh++) {

				jPanelGeneRuleLayout.rowWeights[rh]=0.1;
				jPanelGeneRuleLayout.rowHeights[rh]=7;
			}
		}

		if(columnsSize>0) {

			int columnsLength = columnsSize*3+columnsSize+4;

			jPanelGeneRuleLayout.columnWeights = new double[columnsLength];
			jPanelGeneRuleLayout.columnWidths  = new int[columnsLength];

			for(int rh=0; rh<columnsLength; rh++) {

				jPanelGeneRuleLayout.columnWeights[rh]=0.1;
				jPanelGeneRuleLayout.columnWidths[rh]=7;
			}
		}

		jPanelGeneRule.setLayout(jPanelGeneRuleLayout);

		this.genesField = new ArrayList<>(rowsSize);	

		for(int row=0; row<rowsSize; row++) {

			List<JComboBox<String>> and = new ArrayList<>();
			this.genesField.add(row, and);
			int rowLocation = (row+1) *3 + row;

			if(geneRule_AND_OR.get(row).size()>0)
				jPanelGeneRule.add(new JLabel("("), new GridBagConstraints(1, (rowLocation-2), 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

			for(int column=0; column<columnsSize; column++) {

				if(geneRule_AND_OR.get(row).size()>column) {

					int columnlocation = (column+1)*3 + column+1;

					JComboBox<String> gene = new JComboBox<>(this.genesModel);
					gene.setSelectedIndex(Arrays.asList(genesModel).indexOf(geneRule_AND_OR.get(row).get(column)));
					if(gene.getSelectedItem()!=null)
						gene.setToolTipText(gene.getSelectedItem().toString());
					final List<JComboBox<String>> geneRule = genesField.get(row);
					gene.addActionListener(new ActionListener() {

						@SuppressWarnings("unchecked")
						@Override
						public void actionPerformed(ActionEvent arg0) {

							comboBoxActionListener(geneRule,(JComboBox<String>) arg0.getSource());
							String selectedItem = ((JComboBox<String>) arg0.getSource()).getSelectedItem().toString();
							((JComponent) arg0.getSource()).setToolTipText(selectedItem);

							geneRule_AND_OR = new ArrayList<>();

							for(int g = 0; g<genesField.size(); g++) {

								List<JComboBox<String>> genes = genesField.get(g);
								List<String> genesList = new ArrayList<>();
								for(int j=0; j<genes.size();j++) {

									JComboBox<String> jcb = genes.get(j);
									genesList.add(j, jcb.getSelectedItem().toString());
								}

								geneRule_AND_OR.add(g, genesList);
							}

						}
					});

					jPanelGeneRule.add(gene, new GridBagConstraints((columnlocation-2), (rowLocation-2), 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

					if(column<geneRule_AND_OR.get(row).size()-1) {

						JLabel andField = new JLabel("AND");
						jPanelGeneRule.add(andField, new GridBagConstraints((columnlocation), (rowLocation-2), 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					}
					else {

						JLabel label = new JLabel(")");
						jPanelGeneRule.add(label, new GridBagConstraints((columnlocation), (rowLocation-2), 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						jPanelGeneRule.add(this.jButtonAddGeneRule_AND(row), new GridBagConstraints((columnlocation+2), (rowLocation-2), 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
					}

					and.add(gene);
				}
			}

			if(row<rowsSize-1) {

				JLabel orField = new JLabel("OR");
				jPanelGeneRule.add(orField, new GridBagConstraints(2, rowLocation, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			}
			this.genesField.set(row,and);
		}

		return jPanelGeneRule;
	}

	/**
	 * 
	 */
	public void close(){}

	/**
	 * 
	 */
	public void closeAndUpdate(){}

	/**
	 * @return reactants panel
	 */
	private JPanel addReactantsPanel() {

		JPanel panelReactants = new JPanel();
		GridBagLayout jPanelReactantsLayout = new GridBagLayout();

		//number of rows (array size) 
		if(reactants.length==0) {

			jPanelReactantsLayout.rowWeights = new double[] {0.1, 0.1, 0.1, 0.1};
			jPanelReactantsLayout.rowHeights = new int[] {7, 7, 7, 7};
		}
		else {

			jPanelReactantsLayout.rowWeights = new double[reactants.length*2+2];
			jPanelReactantsLayout.rowHeights  = new int[reactants.length*2+2];

			for(int rh=0; rh<reactants.length*2+2; rh++) {

				jPanelReactantsLayout.rowWeights[rh]=0.1;
				jPanelReactantsLayout.rowHeights[rh]=7;
			}
		}
		//number of columns (array size)
		jPanelReactantsLayout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0};
		jPanelReactantsLayout.columnWidths = new int[] {1, 7, 1, 7, 1, 7, 1, 7, 1};
		//set layout
		panelReactants.setLayout(jPanelReactantsLayout);
		panelReactants.setBorder(BorderFactory.createTitledBorder(null, "Reactants", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
		panelReactants.add(new JLabel("Metabolite"), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelReactants.add(new JLabel("Stoichiometry"), new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelReactants.add(new JLabel("Chains number"), new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelReactants.add(new JLabel("Localization"), new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelReactants.add(this.addReactantCompartmentButton(), new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		if(reactants.length==0) {

			reactants = new String[1];
			reactants[0]= "";
			reactantsField = new ArrayList<>(1);

			//			JComboBox<String> rbox = reactantsField.get(0);
			//			JComboBox<String> rCompartmentsBox = reactantsCompartmentsBox.get(0);

			JComboBox<String> rbox = null;

			if(metabolitesModel==null)
				rbox=new JComboBox<>();
			else
				rbox=new JComboBox<String>(metabolitesModel[1]);
			rbox.setPreferredSize(new Dimension(580, 26));
			rbox.setSelectedIndex(0);
			if(rbox.getSelectedItem()!=null)
				rbox.setToolTipText(rbox.getSelectedItem().toString());
			rbox.addActionListener(new ActionListener() {

				@SuppressWarnings("unchecked")
				@Override
				public void actionPerformed(ActionEvent arg0) {

					comboBoxActionListener(reactantsField,(JComboBox<String>) arg0.getSource());
					((JComponent) arg0.getSource()).setToolTipText(((JComboBox<String>) arg0.getSource()).getSelectedItem().toString());
				}
			});

			reactantsStoichiometry = new String[1];
			reactantsStoichiometry[0] = "1";
			reactantsStoichiometryField = new JTextField[1];
			reactantsStoichiometryField[0] = new JTextField();
			reactantsStoichiometryField[0].setText(reactantsStoichiometry[0]);

			reactantsChainsField = new JTextField[1];
			reactantsChains = new String[1];
			reactantsChains[0]="1";
			reactantsChainsField[0]= new JTextField();
			reactantsChainsField[0].setText(reactantsChains[0]);

			reactantsCompartments=new String[1];
			reactantsCompartments[0] = this.defaultCompartment;

			reactantsCompartmentsBox = new ArrayList<>(1);


			JComboBox<String> rCompartmentsBox = new JComboBox<String>();

			if(metabolitesCompartmentsModel!=null && metabolitesCompartmentsModel.length>0) {

				rCompartmentsBox = new JComboBox<String>(metabolitesCompartmentsModel); 
				rCompartmentsBox.setSelectedIndex(0);
			}

			panelReactants.add(rbox, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			panelReactants.add(reactantsStoichiometryField[0], new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			panelReactants.add(reactantsChainsField[0], new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			panelReactants.add(rCompartmentsBox, new GridBagConstraints(7, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

			reactantsField.add(0,rbox);
			reactantsCompartmentsBox.add(0,rCompartmentsBox);
		}
		else {
			
			int size = 1;
			if(reactants.length>1)
				size = reactants.length;

			reactantsField = new ArrayList<>(size);
			reactantsStoichiometryField = new JTextField[size];
			reactantsChainsField =  new JTextField[size];
			reactantsCompartmentsBox = new ArrayList<>(size);

			for(int s=0; s<size; s++) {

				//					JComboBox<String> rbox = reactantsField.get(s);
				//					JComboBox<String> rCompartmentsBox = reactantsCompartmentsBox.get(s);
				
				JComboBox<String> rbox = new JComboBox<String>(metabolitesModel[1]);
				rbox.setPreferredSize(new Dimension(580, 26));
				
				if(reactants[s]==null)
					rbox.setSelectedIndex(0);
				else
					rbox.setSelectedIndex(Arrays.asList(metabolitesModel[0]).indexOf(reactants[s]));
				
				if(rbox.getSelectedItem()!=null)
					rbox.setToolTipText(rbox.getSelectedItem().toString());
				rbox.addActionListener(new ActionListener() {

					@SuppressWarnings("unchecked")
					@Override
					public void actionPerformed(ActionEvent arg0) {

						comboBoxActionListener(reactantsField,(JComboBox<String>) arg0.getSource());
						((JComponent) arg0.getSource()).setToolTipText(((JComboBox<String>) arg0.getSource()).getSelectedItem().toString());
					}
				});

				reactantsStoichiometryField[s] = new JTextField();
				reactantsChainsField[s] = new JTextField();;
				if(reactantsStoichiometry.length<=s) {

					reactantsStoichiometry[s]="1";
					reactantsChains[s]="1";
				}
				reactantsStoichiometryField[s].setText(reactantsStoichiometry[s]);
				reactantsChainsField[s].setText(reactantsChains[s]);
				JComboBox<String> rCompartmentsBox = new JComboBox<String>(metabolitesCompartmentsModel);
				if(reactantsCompartments[s]==null)
					rCompartmentsBox.setSelectedIndex(0);
				else
					rCompartmentsBox.setSelectedItem(reactantsCompartments[s]);

				int r =s*2+2;
				panelReactants.add(rbox, new GridBagConstraints(1, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				panelReactants.add(reactantsStoichiometryField[s], new GridBagConstraints(3, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				panelReactants.add(reactantsChainsField[s], new GridBagConstraints(5, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				panelReactants.add(rCompartmentsBox, new GridBagConstraints(7, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

				reactantsField.add(s,rbox);
				reactantsCompartmentsBox.add(s,rCompartmentsBox);
			}
		}

		return panelReactants;
	}

	/**
	 * @return products panel
	 */
	private JPanel addProductsPanel() {

		JPanel panelProducts = new JPanel();
		GridBagLayout jPanelProductsLayout = new GridBagLayout();

		//number of rows (array size)
		if(products.length==0) {

			jPanelProductsLayout.rowWeights = new double[] {0.1, 0.0, 0.1};
			jPanelProductsLayout.rowHeights = new int[] {7, 7, 7};
		}
		else {

			jPanelProductsLayout.rowWeights = new double[products.length*2+1];
			jPanelProductsLayout.rowHeights  = new int[products.length*2+1];

			for(int rh=0; rh<products.length*2+1; rh++) {

				jPanelProductsLayout.rowWeights[rh]=0.1;
				jPanelProductsLayout.rowHeights[rh]=7;
			}
		}
		
		//number of columns (array size)
		jPanelProductsLayout.columnWeights = new double[] {0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0, 0.1, 0.0};
		jPanelProductsLayout.columnWidths = new int[] {1, 7, 1, 7, 1, 7, 1, 7, 1};
		//set layout
		panelProducts.setLayout(jPanelProductsLayout);
		panelProducts.setBorder(BorderFactory.createTitledBorder(null, "Products", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));
		panelProducts.add(new JLabel("Metabolite"), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelProducts.add(new JLabel("Stoichiometry"), new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelProducts.add(new JLabel("Chains number"), new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelProducts.add(new JLabel("Localization"), new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		panelProducts.add(this.addProductCompartmentButton(), new GridBagConstraints(7, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

		if(products.length==0) {

			products = new String[1];
			products[0]= "";
			productsField = new ArrayList<>(1);
			productsStoichiometryField = new JTextField[1];
			productsChainsField = new JTextField[1];
			productsCompartmentsBox = new ArrayList<>(1);

			JComboBox<String> pBox = null;

			if(metabolitesModel==null)
				pBox=new JComboBox<>();
			else
				pBox=new JComboBox<String>(metabolitesModel[1]);
			pBox.setPreferredSize(new Dimension(580, 26));
			pBox.setSelectedIndex(0);
			if(pBox.getSelectedItem()!=null)
				pBox.setToolTipText(pBox.getSelectedItem().toString());
			pBox.addActionListener(new ActionListener() {

				@SuppressWarnings("unchecked")
				@Override
				public void actionPerformed(ActionEvent arg0) {

					JComboBox<String> jCombo = (JComboBox<String>) arg0.getSource();
					
					comboBoxActionListener(productsField, jCombo);
					((JComponent) arg0.getSource()).setToolTipText(jCombo.getSelectedItem().toString());
					
				}});

			productsStoichiometry = new String[1];
			productsStoichiometry[0]= "1";
			productsStoichiometryField = new JTextField[1];
			productsStoichiometryField[0] = new JTextField();
			productsStoichiometryField[0].setText("1");

			productsChains = new String[1];
			productsChains[0]="1";
			productsChainsField = new JTextField[1];
			productsChainsField[0] = new JTextField();
			productsChainsField[0].setText("1");

			productsCompartments=new String[1];
			productsCompartments[0]=this.defaultCompartment;
			JComboBox<String> pCompartmentsBox = new JComboBox<String>();
			if(metabolitesCompartmentsModel!=null && metabolitesCompartmentsModel.length>0) {

				pCompartmentsBox = new JComboBox<String>(metabolitesCompartmentsModel); 
				pCompartmentsBox.setSelectedIndex(0);
			}


			panelProducts.add(pBox, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			panelProducts.add(productsStoichiometryField[0], new GridBagConstraints(3, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			panelProducts.add(productsChainsField[0], new GridBagConstraints(5, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			panelProducts.add(pCompartmentsBox, new GridBagConstraints(7, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			
			productsField.add(0,pBox);
			productsCompartmentsBox.add(0,pCompartmentsBox);

		}
		else {
			
			int size = 1;
			if(products.length>1)
				size = products.length;

			productsField = new ArrayList<>(size);
			productsStoichiometryField = new JTextField[size];
			productsChainsField = new JTextField[size];
			productsCompartmentsBox = new ArrayList<>(size);

			for(int s=0; s<size; s++) {

				//				JComboBox<String> pBox = productsField.get(s);
				//				JComboBox<String> pCompartmentsBox = productsCompartmentsBox.get(s);
				
				JComboBox<String> pBox = new JComboBox<String>(metabolitesModel[1]);
				pBox.setPreferredSize(new Dimension(580, 26));
				if(products[s]==null)
					pBox.setSelectedIndex(0);
				else
					pBox.setSelectedIndex(Arrays.asList(metabolitesModel[0]).indexOf(products[s]));
				
				if(pBox.getSelectedItem()!=null)
					pBox.setToolTipText(pBox.getSelectedItem().toString());
				pBox.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent arg0) {

						@SuppressWarnings("unchecked")
						JComboBox<String> jCombo = (JComboBox<String>) arg0.getSource();
						
						comboBoxActionListener(productsField, jCombo);
						((JComponent) arg0.getSource()).setToolTipText(jCombo.getSelectedItem().toString());
					}});

				productsStoichiometryField[s] = new JTextField();
				productsChainsField[s] = new JTextField();
				if(productsStoichiometry.length<=s) {

					productsStoichiometry[s]="1";
					productsChains[s]="1";
				}
				productsStoichiometryField[s].setText(productsStoichiometry[s]);
				productsChainsField[s].setText(productsChains[s]);
				JComboBox<String> pCompartmentsBox = new JComboBox<String>(metabolitesCompartmentsModel);
				if(productsCompartments[s]==null)
					pCompartmentsBox.setSelectedIndex(0);
				else
					pCompartmentsBox.setSelectedItem(productsCompartments[s]);

				int r =s*2+2;
				panelProducts.add(pBox, new GridBagConstraints(1, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
				panelProducts.add(productsStoichiometryField[s], new GridBagConstraints(3, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				panelProducts.add(productsChainsField[s], new GridBagConstraints(5, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				panelProducts.add(pCompartmentsBox, new GridBagConstraints(7, r, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
				
				productsField.add(s,pBox);
				productsCompartmentsBox.add(s,pCompartmentsBox);
			}
		}

		return panelProducts;
	}

	/**
	 * @param pathway
	 * @return
	 */
	private JPanel addEnzymes() {

		JPanel jPanelEnzyme;
		jPanelEnzyme = new JPanel();
		GridBagLayout jPanelEnzymeLayout = new GridBagLayout();
		boolean noEnzyme=false;

		String[] enzymes = new String[0];

		if(this.selectedEnzymesAndPathway.containsKey(selectedPathway)) {

			enzymes = new String[this.selectedEnzymesAndPathway.get(selectedPathway).size()];

			int i = 0;
			for(String enzyme : this.selectedEnzymesAndPathway.get(selectedPathway)) {

				enzymes[i] = enzyme;
				i++;
			}
		}

		if(enzymes.length==0) {

			jPanelEnzymeLayout.rowWeights = new double[] {0.1, 0.0, 0.1};
			jPanelEnzymeLayout.rowHeights = new int[] {7, 7, 7};
			enzymes=new String[1];
			noEnzyme=true;
		}
		else {

			jPanelEnzymeLayout.rowWeights = new double[enzymes.length*2+1];
			jPanelEnzymeLayout.rowHeights  = new int[enzymes.length*2+1];

			for(int rh=0; rh<enzymes.length*2+1; rh++) {

				jPanelEnzymeLayout.rowWeights[rh]=0.1;
				jPanelEnzymeLayout.rowHeights[rh]=7;
			}
		}
		jPanelEnzymeLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
		jPanelEnzymeLayout.columnWidths = new int[] {7, 7, 7};
		jPanelEnzyme.setLayout(jPanelEnzymeLayout);

		jPanelEnzyme.setBorder(BorderFactory.createTitledBorder(null, "Enzymes in "+selectedPathway, TitledBorder.LEADING, TitledBorder.ABOVE_TOP));

		if(selectedPathway.equals("-1allpathwaysinreaction"))
			jPanelEnzyme.setBorder(BorderFactory.createTitledBorder(null, "Enzymes", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));

		if(noEnzyme) {

			enzymesField = new ArrayList<>(1);
			//JComboBox<String> eBox = enzymesField.get(0);
			JComboBox<String> eBox = new JComboBox<>(enzymesModel);

			if(enzymes[0]!=null && !enzymes[0].equals("")) {

				eBox.setSelectedItem(enzymes[0]);
				eBox.setToolTipText(eBox.getSelectedItem().toString());
			}

			jPanelEnzyme.add(eBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			Set<String> enzymesSet=new TreeSet<String>();

			if(!eBox.getSelectedItem().toString().equals(""))
				enzymesSet.add(eBox.getSelectedItem().toString());

			if(selectedEnzymesAndPathway.size()>0)
				selectedEnzymesAndPathway.put(selectedPathway, enzymesSet);

			eBox.addItemListener(new ItemListener() {

				@Override
				public void itemStateChanged(ItemEvent arg0) {

					enzymesAction(arg0);
				}
			});

			enzymesField.add(0,eBox);
		}
		else {

			Set<String> enzymesSet=new TreeSet<String>();
			enzymesField = new ArrayList<>(enzymes.length);

			for(int e=0; e<enzymes.length;e++) {

				int r =e*2+1;

				//JComboBox<String> eBox = enzymesField.get(e);
				JComboBox<String> eBox = new JComboBox<>(enzymesModel);

				if(enzymes[e]!=null && !enzymes[e].equals("")) {

					eBox.setSelectedItem(enzymes[e]);
					eBox.setToolTipText(eBox.getSelectedItem().toString());
				}

				jPanelEnzyme.add(eBox, new GridBagConstraints(1, r, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

				if(!eBox.getSelectedItem().toString().equals(""))
					enzymesSet.add(eBox.getSelectedItem().toString());

				if(selectedEnzymesAndPathway.size()>0)
					selectedEnzymesAndPathway.put(selectedPathway, enzymesSet);

				eBox.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent arg0) {

						enzymesAction(arg0);

					}
				});

				enzymesField.add(e,eBox);
			}
		}
		return jPanelEnzyme;
	}

	/**
	 * @return
	 */
	private JPanel addPathways() {

		JPanel jPanelPathway;
		GridBagLayout jPanelPathwayLayout = new GridBagLayout();
		boolean noPathway=false;
		String[] pathways = new String[this.selectedEnzymesAndPathway.size()-1];
		int i = 0;
		jPanelPathwayLayout.rowWeights = new double[] {0.1, 0.0, 0.1};
		jPanelPathwayLayout.rowHeights = new int[] {7, 7, 7};
		jPanelPathwayLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
		jPanelPathwayLayout.columnWidths = new int[] {7, 7, 7};

		for(String pathway : this.selectedEnzymesAndPathway.keySet()) {

			if(pathway.equals("-1allpathwaysinreaction")) {

				//pathways[0] = pathway;
			}
			else {

				pathways[i] = pathway;
				i++;
			}
		}

		if(pathways.length == 0) {


			pathways=new String[1];
			noPathway=true;
		}
		else {

			jPanelPathwayLayout.rowWeights = new double[pathways.length*2+1];
			jPanelPathwayLayout.rowHeights  = new int[pathways.length*2+1];

			for(int rh = 0; rh < pathways.length *2+1; rh++) {

				jPanelPathwayLayout.rowWeights[rh]=0.1;
				jPanelPathwayLayout.rowHeights[rh]=7;
			}
		}

		jPanelPathway= new JPanel();
		jPanelPathway.setLayout(jPanelPathwayLayout);
		jPanelPathway.setBorder(BorderFactory.createTitledBorder(null, "Pathways", TitledBorder.LEADING, TitledBorder.ABOVE_TOP));

		if(noPathway) {

			pathwaysField = new ArrayList<>(1);
			// JComboBox<String> pBox = pathwaysField.get(0);
			JComboBox<String> pBox = new JComboBox<String>(pathwaysModel);

			if(pathways[0]!= null && !pathways[0].equals("")) {

				pBox.setSelectedItem(pathways[0]);
				pBox.setToolTipText(pBox.getSelectedItem().toString());
			}

			pBox.setPreferredSize(new Dimension(28, 26));
			jPanelPathway.add(pBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			pBox.addMouseListener(new MouseListener() {
				@Override
				public void mouseReleased(MouseEvent e) {}
				@Override
				public void mousePressed(MouseEvent e) {}
				@Override
				public void mouseExited(MouseEvent e) {}
				@Override
				public void mouseEntered(MouseEvent e) {}
				@SuppressWarnings("unchecked")
				@Override
				public void mouseClicked(MouseEvent e) {

					pathwaysClick(((JComboBox<String>) e.getSource()).getSelectedItem().toString());
				}
			});
			pBox.addItemListener(new ItemListener() {
				@SuppressWarnings("unchecked")
				@Override
				public void itemStateChanged(ItemEvent arg0) {

					List<String> paths = new ArrayList<String>();
					paths.add("-1allpathwaysinreaction");

					for(JComboBox<String> jPathway : pathwaysField) {

						String pathway = jPathway.getSelectedItem().toString();

						if(!pathway.equals(""))
							paths.add(pathway);
					}

					pathwaysAction(paths, ((JComboBox<String>) arg0.getSource()).getSelectedItem().toString());
				}
			});
			pathwaysField.add(0, pBox);
		}
		else {

			pathwaysField = new ArrayList<>(pathways.length);

			for(int p = 0 ; p < pathways.length ; p++) {

				//JComboBox<String> pBox = pathwaysField.get(p);
				JComboBox<String> pBox = new JComboBox<String>(pathwaysModel);

				if(pathways[p]!= null && !pathways[p].equals("")) {

					pBox.setSelectedItem(pathways[p]);
					pBox.setToolTipText(pBox.getSelectedItem().toString());
				}
				pBox.addMouseListener(new MouseListener() {
					@Override
					public void mouseReleased(MouseEvent e) {}
					@Override
					public void mousePressed(MouseEvent e) {}
					@Override
					public void mouseExited(MouseEvent e) {}
					@Override
					public void mouseEntered(MouseEvent e) {}
					@SuppressWarnings("unchecked")
					@Override
					public void mouseClicked(MouseEvent e) {

						pathwaysClick(((JComboBox<String>) e.getSource()).getSelectedItem().toString());
					}
				});
				pBox.addItemListener(new ItemListener() {
					@SuppressWarnings("unchecked")
					@Override
					public void itemStateChanged(ItemEvent arg0) {


						List<String> paths = new ArrayList<String>();
						//paths.add("-1allpathwaysinreaction");

						for(JComboBox<String> jPathway : pathwaysField) {

							String pathway = jPathway.getSelectedItem().toString();

							if(!pathway.equals(""))
								paths.add(pathway);
						}

						pathwaysAction(paths, ((JComboBox<String>) arg0.getSource()).getSelectedItem().toString());
					}
				});

				pBox.setPreferredSize(new Dimension(28, 26));
				int r =p*2+1;
				jPanelPathway.add(pBox, new GridBagConstraints(1, r, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));

				pathwaysField.add(p, pBox);
			}
		}
		jPanelPathway.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {}
			@Override
			public void mousePressed(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseClicked(MouseEvent e) {

				pathwaysPanelClick();
			}
		});
		return jPanelPathway;
	}

	/**
	 * @return
	 */
	private Component addReactantCompartmentButton() {

		JButton add = new JButton();
		add.setIcon((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")), 0.03).resizeImageIcon()));
		add.setText("Compartment");
		add.setToolTipText("add Compartment");
		add.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				new InsertCompartment(reactionsInterface)
				{
					private static final long serialVersionUID = 4850184611256705194L;
					public void finish() {
						this.setVisible(false);
						this.dispose();
						boolean isCompartmentalisedModel = reactionsInterface.getProject().isCompartmentalisedModel();
						metabolitesCompartmentsModel = reactionsInterface.getCompartments(true, isCompartmentalisedModel);
						jScrollPaneReactants.setViewportView(addReactantsPanel());
						jScrollPaneProducts.setViewportView(addProductsPanel());
						System.gc();
					}						
				};

			}});
		return add;
	}

	/**
	 * @return
	 */
	private Component addProductCompartmentButton() {


		JButton add = new JButton();
		add.setIcon((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")), 0.03).resizeImageIcon()));
		add.setText("Compartment");
		add.setToolTipText("add Compartment");
		add.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				new InsertCompartment(reactionsInterface)
				{
					private static final long serialVersionUID = 1L;
					public void finish() {
						this.setVisible(false);
						this.dispose();
						boolean isCompartmentalisedModel = reactionsInterface.getProject().isCompartmentalisedModel();
						metabolitesCompartmentsModel = reactionsInterface.getCompartments(true, isCompartmentalisedModel);
						jScrollPaneProducts.setViewportView(addProductsPanel());
						jScrollPaneReactants.setViewportView(addReactantsPanel());
						System.gc();
					}						
				};

			}});
		return add;
	}

	/**
	 * Save data 
	 */
	private void saveData() {
		
		Map<String, String> metabolites = new TreeMap<String, String>();
		Map<String, String > chains = new TreeMap<String, String >(), compartments = new TreeMap<String, String >();
		boolean go = true;

		for(int i=0; i< reactantsField.size(); i++) {

			int selectedIndex = reactantsField.get(i).getSelectedIndex();

			if(selectedIndex>0) {

				String signal = "-";

				if(jBackword.isSelected())
					signal = "";

				reactants[i]= signal+metabolitesModel[0][selectedIndex];
				String stoich = reactantsStoichiometryField[i].getText();

				if(stoich.startsWith("-")) {

					if(signal.isEmpty())
						stoich=stoich.substring(1);
				}
				else {

					if(signal.equalsIgnoreCase("-"))
						stoich=signal+stoich;
				}

				metabolites.put(reactants[i].trim(), stoich);
				chains.put(reactants[i].trim(), reactantsChainsField[i].getText().trim());
				compartments.put(reactants[i].trim(), reactantsCompartmentsBox.get(i).getSelectedItem().toString().trim());

				if (stoich.length()>18) {
					
					BigDecimal bigDecimal = new BigDecimal(stoich);
					bigDecimal = bigDecimal.setScale(18, RoundingMode.HALF_UP);

//					go = false;
//					Workbench.getInstance().error("Cannot add reaction. Stoichiometry value ("+stoich+") for "+reactantsField.get(i).getSelectedItem()+" to large.");
//					i = reactantsField.size();
					
					stoich = bigDecimal.doubleValue()+"";
				}
			}
		}
		
		if(go)
			for(int i=0; i< productsField.size(); i++) {

				int selectedIndex = productsField.get(i).getSelectedIndex();
				
				if(selectedIndex>0) {

					String signal = "";

					if(jBackword.isSelected())
						signal = "-";

					products[i]= signal+metabolitesModel[0][selectedIndex];
					String stoich = productsStoichiometryField[i].getText();

					if(stoich.startsWith("-")) {

						if(signal.isEmpty())
							stoich=stoich.substring(1);
					}
					else {

						if(signal.equalsIgnoreCase("-"))
							stoich=signal+stoich;
					}

					metabolites.put(products[i].trim(), stoich.trim());
					chains.put(products[i].trim(), productsChainsField[i].getText().trim());
					compartments.put(products[i].trim(), productsCompartmentsBox.get(i).getSelectedItem().toString().trim());

					if (stoich.length()>18) {
						
						BigDecimal bigDecimal = new BigDecimal(stoich);
						bigDecimal = bigDecimal.setScale(18, RoundingMode.HALF_UP);

//						go = false;
//						Workbench.getInstance().error("Cannot add reaction. Stoichiometry value ("+stoich+") for "+productsField.get(i).getSelectedItem()+" to large.");
//						i = productsField.size();
						
						stoich = bigDecimal.doubleValue()+"";
					}
				}
			}

		String boolean_rule = null;
		{
			if(this.genesField.size()>0)
				boolean_rule="";

			for(int l=0;l <this.genesField.size();l++) {

				List<JComboBox<String>> genesAnd = this.genesField.get(l);

				String boolean_rule_row="";
				for(int j=0; j<genesAnd.size();j++) {

					if(genesAnd.get(j).getSelectedItem()!=null) {

						String gene = genesAnd.get(j).getSelectedItem().toString(); 

						if(!gene.isEmpty()) {

							if(j>0)
								boolean_rule_row = boolean_rule_row.concat(" AND ");

							boolean_rule_row = boolean_rule_row.concat(gene);
						}
					}
				}

				if(l>0 && !boolean_rule_row.isEmpty())
					boolean_rule = boolean_rule.concat(" OR ");

				boolean_rule = boolean_rule.concat(boolean_rule_row);
			}
		}
		
		if(go) {
			
			this.updatePathways();
			if(rowID == -10) {

				reactionsInterface.insertNewReaction(jTextFieldName.getText(), jTextFieldEquation.getText(), jRadioButtonReversible.isSelected(),
						chains, compartments, metabolites, jCheckBoxInModel.isSelected(), selectedEnzymesAndPathway, jComboBoxLocalisation.getSelectedItem().toString(),
						jSpontaneous.isSelected(), jNonEnzymatic.isSelected(), jIsGeneric.isSelected(),
						Double.valueOf(lowerBoundary.getText()), Double.valueOf(upperBoundary.getText()), InformationType.MANUAL.toString(), boolean_rule);
			}
			else {

				reactionsInterface.updateReaction(rowID, jTextFieldName.getText(), jTextFieldEquation.getText(), jRadioButtonReversible.isSelected(), //enzymesSet,
						chains, compartments, metabolites, jCheckBoxInModel.isSelected(), selectedEnzymesAndPathway, jComboBoxLocalisation.getSelectedItem().toString(), 
						jSpontaneous.isSelected(), jNonEnzymatic.isSelected(), jIsGeneric.isSelected(),
						Double.valueOf(lowerBoundary.getText()), Double.valueOf(upperBoundary.getText()), boolean_rule);
			}
			this.inModel = jCheckBoxInModel.isSelected();
			applyPressed=true;
		}
	}

	/**
	 * 
	 */
	private void startFields() {

		ReactionContainer data = reactionsInterface.getReaction(rowID);
		jTextFieldName.setText(data.getName());
		jTextFieldName.setToolTipText(data.getName());
		jTextFieldEquation.setText(data.getEquation());
		jTextFieldEquation.setToolTipText(data.getEquation());

		if(data.isReversible()) {

			jRadioButtonReversible.setSelected(true);
		}
		else {

			jRadioButtonIrreversible.setSelected(true);
			jForward.setSelected(true);
			jForward.setEnabled(true);
			jBackword.setEnabled(true);
		}

		jCheckBoxInModel.setSelected(data.isInModel());
		jComboBoxLocalisation.setSelectedItem(data.getLocalisation());
		jSpontaneous.setSelected(data.isSpontaneous());
		jNonEnzymatic.setSelected(data.isNon_enzymatic());
		jIsGeneric.setSelected(data.isGeneric());

		if(data.isReversible())
			this.lowerBoundary.setText("-999999");
		else
			this.lowerBoundary.setText("0");
		if(data.getLowerBound()!=null)
			this.lowerBoundary.setText(data.getLowerBound().toString());

		this.upperBoundary.setText("999999");
		if(data.getUpperBound()!=null)
			this.upperBoundary.setText(data.getUpperBound().toString());

		if(data.getGeneRule()!= null) {

			String[] gene_AND_OR = data.getGeneRule().split(" OR ");

			for(String gene_AND : gene_AND_OR) {

				List<String> geneAnd = new ArrayList<>();

				for(String gene : gene_AND.split(" AND "))
					if(!gene.trim().isEmpty())
						geneAnd.add(gene.trim());

				geneRule_AND_OR.add(geneAnd);
			}
		}

		List<String> r = new ArrayList<String>();
		List<String> p = new ArrayList<String>();
		List<String> rs = new ArrayList<String>();
		List<String> rc = new ArrayList<String>();
		List<String> ps = new ArrayList<String>();
		List<String> pc = new ArrayList<String>();
		List<String> compartmentReactant = new ArrayList<>();
		List<String> compoundID_R = new ArrayList<>();
		List<String> compartmentProduct = new ArrayList<>();
		List<String> compoundID_P = new ArrayList<>();

		this.reactionMetabolites = reactionsInterface.getMetabolites(rowID);

		for(String m : reactionMetabolites.keySet()) {

			if(reactionMetabolites.get(m).getStoichiometric_coefficient().startsWith("-")) {
				
				r.add(reactionMetabolites.get(m).getCompartment_name());
				rs.add(reactionMetabolites.get(m).getStoichiometric_coefficient()+"");
				rc.add(reactionMetabolites.get(m).getNumberofchains());
				compartmentReactant.add(reactionMetabolites.get(m).getCompartment_name());
				compoundID_R.add(reactionMetabolites.get(m).getMetaboliteID()+"");
			}
			else {

				p.add(reactionMetabolites.get(m).getCompartment_name());
				ps.add(reactionMetabolites.get(m).getStoichiometric_coefficient()+"");
				pc.add(reactionMetabolites.get(m).getNumberofchains());
				compartmentProduct.add(reactionMetabolites.get(m).getCompartment_name());
				compoundID_P.add(reactionMetabolites.get(m).getMetaboliteID()+"");
			}
		}

		reactants = compoundID_R.toArray(reactants);
		reactantsStoichiometry = rs.toArray(reactantsStoichiometry);
		reactantsChains = rc.toArray(reactantsChains);
		reactantsCompartments = compartmentReactant.toArray(reactantsCompartments);

		products = compoundID_P.toArray(products);
		productsStoichiometry = ps.toArray(productsStoichiometry);
		productsChains = pc.toArray(productsChains);
		productsCompartments = compartmentProduct.toArray(productsCompartments);

		panelReactants = this.addReactantsPanel();
		jScrollPaneReactants.setViewportView(panelReactants);
		panelProducts = this.addProductsPanel();
		jScrollPaneProducts.setViewportView(panelProducts);

		jScrollPaneGeneRule.setViewportView(this.addGeneRules());

	}

	/**
	 * @return
	 */
	private JButton jButtonAddReactant() {

		JButton jButtonAddReactant = new JButton();
		jButtonAddReactant.setIcon((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")), 0.04).resizeImageIcon()));
		jButtonAddReactant.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {

				String[] newReactants = new String[reactantsField.size()+1];
				String[] newReactantsStoichiometry = new String[reactantsStoichiometryField.length+1];
				String[] newReactantsChains = new String[reactantsChainsField.length+1];
				String[] newReactantsComp = new String[reactantsCompartmentsBox.size()+1];

				for(int i=0; i<reactantsField.size();i++) {

					newReactants[i]=Arrays.asList(metabolitesModel[0]).get(reactantsField.get(i).getSelectedIndex()).toString();
					newReactantsStoichiometry[i] = reactantsStoichiometryField[i].getText().toString();
					newReactantsChains[i] = reactantsChainsField[i].getText().toString();
					newReactantsComp[i] = reactantsCompartmentsBox.get(i).getSelectedItem().toString();
				}

				newReactants[reactantsField.size()]="";
				newReactantsStoichiometry[reactantsStoichiometryField.length]="-1";
				newReactantsChains[reactantsChainsField.length] = "1";
				newReactantsComp[reactantsCompartmentsBox.size()] = defaultCompartment;
				reactants=newReactants;
				reactantsStoichiometry = newReactantsStoichiometry;
				reactantsChains=newReactantsChains;
				reactantsCompartments=newReactantsComp;
				jScrollPaneReactants.setViewportView(addReactantsPanel());
			}
		});
		return jButtonAddReactant;
	}

	/**
	 * @return
	 */
	private JButton jButtonAddProduct() {

		JButton jButtonAddProduct = new JButton();
		jButtonAddProduct.setIcon((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")), 0.04).resizeImageIcon()));
		jButtonAddProduct.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {

				String[] newProduct = new String[productsField.size()+1];
				String[] newProductStoichiometry = new String[productsStoichiometryField.length+1];
				String[] newProductChains = new String[productsChainsField.length+1];
				String[] newProductComp = new String[productsCompartmentsBox.size()+1];
				
				for(int i=0; i<productsField.size();i++) {
					
					newProduct[i]=Arrays.asList(metabolitesModel[0]).get(productsField.get(i).getSelectedIndex()).toString();
					newProductStoichiometry[i] = productsStoichiometryField[i].getText().toString();
					newProductChains[i] = productsChainsField[i].getText().toString();
					newProductComp[i] = productsCompartmentsBox.get(i).getSelectedItem().toString();
				}
				newProduct[productsField.size()]="";
				newProductStoichiometry[productsStoichiometryField.length]="1";
				newProductChains[productsChainsField.length] = "1";
				newProductComp[productsCompartmentsBox.size()] = defaultCompartment;

				products=newProduct;
				productsStoichiometry = newProductStoichiometry;
				productsChains=newProductChains;
				productsCompartments=newProductComp;
				jScrollPaneProducts.setViewportView(addProductsPanel());					
			}
		});

		return jButtonAddProduct ;
	}

	/**
	 * @return
	 */
	private JButton jButtonAddEnzyme() {

		JButton jButtonAddEnzyme = new JButton();
		jButtonAddEnzyme.setIcon((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")), 0.04).resizeImageIcon()));
		jButtonAddEnzyme.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {

				selectedEnzymesAndPathway.get(selectedPathway).add("");
				jScrollPaneEnzymes.setViewportView(addEnzymes());
			}
		});
		return jButtonAddEnzyme;
	}

	/**
	 * @return
	 */
	private JButton jButtonAddPathways() {

		JButton jButtonAddPathways = new JButton();
		jButtonAddPathways.setIcon((new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Add.png")), 0.04).resizeImageIcon()));
		jButtonAddPathways.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {

				selectedEnzymesAndPathway.put("", new HashSet<String>());
				jScrollPanePathways.setViewportView(addPathways());					
			}
		});
		return jButtonAddPathways;
	}

	/**
	 * @param comboArray
	 * @param comboBox
	 */
	private boolean comboBoxActionListener(List<JComboBox<String>> comboArray, JComboBox<String> comboBox) {

		for(JComboBox<String> r:comboArray) {

			if(!comboBox.equals(r)) {

				if(comboBox.getSelectedIndex() == r.getSelectedIndex() && !comboBox.getSelectedItem().toString().equals("")) {

					Workbench.getInstance().warn("Entity already selected!");
					comboBox.setSelectedIndex(0);
					return false;
				}
			}
		}
		return true;
	}


	/**
	 * @param arg0
	 */
	private void pathwaysAction(List<String> paths, String editedPathway) {

		Map<String, Set<String>> newSelectedEnzymesAndPathway = new HashMap<String, Set<String>>();

		for(String pathway : paths) {

			Set<String> enz = new TreeSet<String>();

			if(selectedEnzymesAndPathway.containsKey(pathway))
				enz = selectedEnzymesAndPathway.get(pathway);

			newSelectedEnzymesAndPathway.put(pathway, enz);
		}

		selectedEnzymesAndPathway = newSelectedEnzymesAndPathway;
		pathwaysClick(editedPathway);
	}


	/**
	 * @param pathway
	 */
	private void pathwaysClick(String pathway) {

		if(pathway.equals(""))
			this.selectedPathway="-1allpathwaysinreaction";
		else
			this.selectedPathway=pathway;

		this.jScrollPaneEnzymes.setViewportView(addEnzymes());		
	}


	/**
	 * @param arg0
	 */
	private void enzymesAction(ItemEvent arg0) {

		if(arg0.getStateChange()==ItemEvent.SELECTED) {

			Set<String> enzs = new HashSet<String>();
			for(JComboBox<String> jEnzymes : this.enzymesField) {

				String enzyme = jEnzymes.getSelectedItem().toString();

				if(!enzyme.equals(""))
					enzs.add(enzyme);
			}
			
			@SuppressWarnings("unchecked")
			boolean newECnumber=comboBoxActionListener(this.enzymesField, (JComboBox<String>) arg0.getSource());
			boolean isReallySelected = this.verifyIfIsSelected(this.enzymesField, enzs);

			if(newECnumber && isReallySelected)
				this.selectedEnzymesAndPathway.put(this.selectedPathway,enzs);
		}
	}

	/**
	 * 
	 */
	private void updatePathways () {

		//if all pathways, put new enzyme in all pathways
		if(selectedPathway.equalsIgnoreCase("-1allpathwaysinreaction")) {

			Set<String> allEnzymes = new HashSet<>();					

			if(selectedEnzymesAndPathway.containsKey("-1allpathwaysinreaction"))
				allEnzymes = selectedEnzymesAndPathway.get("-1allpathwaysinreaction");

			for(String pathway : selectedEnzymesAndPathway.keySet()) {

				if(!pathway.equalsIgnoreCase("-1allpathwaysinreaction")) {

					Set<String> enzymes = selectedEnzymesAndPathway.get(pathway);

					enzymes.retainAll(allEnzymes);
					selectedEnzymesAndPathway.put(pathway, enzymes);
				}
			}
		}
		else {

			// collect ec numbers for this reaction
			Set<String> newEnzymes = new HashSet<String> ();
			for(String pathway : selectedEnzymesAndPathway.keySet()) {

				Set<String> enzymes = selectedEnzymesAndPathway.get(pathway);
				newEnzymes.addAll(enzymes);
			}

			selectedEnzymesAndPathway.put("-1allpathwaysinreaction", newEnzymes);
		}
	}

	/**
	 * @param enzymesField2
	 * @param enzs
	 * @return
	 */
	private boolean verifyIfIsSelected(List<JComboBox<String>> comboArray, Set<String> enzs) {

		Set<String> surrogateEnzs = new HashSet<>(enzs);

		for(JComboBox<String> r:comboArray) {

			String ec = r.getSelectedItem().toString();

			if(enzs.contains(ec))
				surrogateEnzs.remove(ec);
		}

		return surrogateEnzs.isEmpty();
	}

	/**
	 * 
	 */
	private void pathwaysPanelClick() {

		this.selectedPathway="-1allpathwaysinreaction";
		this.jScrollPaneEnzymes.setViewportView(addEnzymes());
	}
}
