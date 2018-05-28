package pt.uminho.ceb.biosystems.merlin.core.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactantsProducts;
import pt.uminho.ceb.biosystems.merlin.core.utilities.CreateImageIcon;

public class InsertEditMetabolite extends javax.swing.JDialog{
	
	@SuppressWarnings("unused")
	private int row = -1;
	private JPanel jPanelMetabolite;
	private JTextField jTextFieldName;
	private JLabel jLabel1;
	private JLabel jLabel2;
	private JTextField jTextFieldFormula;
	private JLabel jLabel3;
	private JLabel jLabel4;
	private JLabel jLabel5;
	private JTextField jTextFieldMolecularW;
	private JTextField jTextFieldCharge;
	private JComboBox<String> jComboBoxEntryType;
	private JPanel jPanelSaveClose;
	private JButton jButtonSave;
	private JButton jButtonCancel;
	private String name = "", entryType = "", formula = "", molecularW = "", charge = "";
	private ReactantsProducts reactantsProducts;
	private String metabolite;
	private boolean insert = true;
	private String originalKEGG;
	
	public InsertEditMetabolite(int row, String metabolite, String keggID, ReactantsProducts reactantsProducts) {
		 
		super(Workbench.getInstance().getMainFrame());
		
		this.metabolite = metabolite;
		this.row = row;
		this.reactantsProducts = reactantsProducts;
		this.originalKEGG = keggID;
		
		if(row == -1)
			this.setTitle("insert metabolite");
	
		else {
			this.setTitle("edit metabolite information");
			insert = false;
		}
		
		this.initGUI();
		Utilities.centerOnOwner(this);
		this.setVisible(true);
		this.setAlwaysOnTop(true);
		this.toFront();
		
	}
	
	private void initGUI(){
		
		try {
			GridBagLayout thisLayout = new GridBagLayout();
			thisLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
			thisLayout.columnWidths = new int[] {7, 7, 7};
			thisLayout.rowWeights = new double[] {0.0, 200.0, 0.0, 0.0, 0.0};
			thisLayout.rowHeights = new int[] {7, 50, 7, 3, 7};
			this.setLayout(thisLayout);
			this.setPreferredSize(new Dimension(875, 585));
			this.setSize(500, 250);
			{

				jPanelMetabolite = new JPanel();
				GridBagLayout jPanel1Layout = new GridBagLayout();
				jPanel1Layout.rowWeights = new double[] {0.1, 0.1, 0.0, 0.0, 0.1};
				jPanel1Layout.rowHeights = new int[] {7, 7, 7, 7, 7};
				jPanel1Layout.columnWeights = new double[] {0.0, 0.0, 0.1, 0.0};
				jPanel1Layout.columnWidths = new int[] {7, 6, 7, 7};
				jPanelMetabolite.setLayout(jPanel1Layout);
				jPanelMetabolite.setBorder(BorderFactory.createTitledBorder("metabolite data"));

				this.add(jPanelMetabolite, new GridBagConstraints(1, 1, 3, 1, 0.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

				{	
					{
						jTextFieldName = new JTextField();
						jTextFieldFormula = new JTextField();
						jTextFieldMolecularW = new JTextField();
						jTextFieldCharge = new JTextField();
						
						jLabel1 = new JLabel("name");
						jLabel2 = new JLabel("entry type");
						jLabel3 = new JLabel("formula");
						jLabel4 = new JLabel("molecular weight");
						jLabel5 = new JLabel("charge");

						jComboBoxEntryType = new JComboBox<String>(new String[] {"COMPOUND", "GLYCAN", "DRUGS", "BIOMASS" });
						
						jPanelMetabolite.add(jTextFieldName, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
						jPanelMetabolite.add(jComboBoxEntryType, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jPanelMetabolite.add(jTextFieldFormula, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jPanelMetabolite.add(jTextFieldMolecularW, new GridBagConstraints(2, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jPanelMetabolite.add(jTextFieldCharge, new GridBagConstraints(2, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

						
						jPanelMetabolite.add(jLabel1, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						jPanelMetabolite.add(jLabel2, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						jPanelMetabolite.add(jLabel3, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						jPanelMetabolite.add(jLabel4, new GridBagConstraints(1, 7, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
						jPanelMetabolite.add(jLabel5, new GridBagConstraints(1, 9, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

						
						jTextFieldName.setBounds(54, 36, 179, 27);
						
						if (!insert){
							ArrayList<String> data = reactantsProducts.getMetaboliteData(metabolite);
							
							int type = 0;
							
							if (data.get(1).equals("GLYCAN"))
								type = 1;
							else if (data.get(1).equals("DRUGS"))
								type = 2;
							else if (data.get(1).equals("BIOMASS"))
								type = 3;
							
							jTextFieldName.setText(data.get(0));
							jComboBoxEntryType.setSelectedIndex(type);
							jTextFieldFormula.setText(data.get(2));
							jTextFieldMolecularW.setText(data.get(3));
							jTextFieldCharge.setText(data.get(4));
						}
					}
				}
				{
					jPanelSaveClose = new JPanel(new GridBagLayout());
					GridBagLayout jPanelSaveCloseLayout = new GridBagLayout();
					jPanelSaveCloseLayout.rowWeights = new double[] {0.1, 0.1, 0.1};
					jPanelSaveCloseLayout.rowHeights = new int[] {5, 10, 5};
					jPanelSaveCloseLayout.columnWeights = new double[] {0.1, 0.1, 0.1, 0.1, 0.1};
					jPanelSaveCloseLayout.columnWidths = new int[] {8, 130, 24, 130, 8};
					jPanelSaveClose.setLayout(jPanelSaveCloseLayout);

					{
						jButtonSave = new JButton();
						jPanelSaveClose.add(jButtonSave, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jButtonSave.setText("save");
						jButtonSave.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Save.png")),0.1).resizeImageIcon());
						jButtonSave.addActionListener(new ActionListener(){

							@Override
							public void actionPerformed(ActionEvent e) {
								
								name = jTextFieldName.getText();
								entryType = jComboBoxEntryType.getSelectedItem().toString();
								formula = jTextFieldFormula.getText();
								molecularW = jTextFieldMolecularW.getText();
								charge = jTextFieldCharge.getText();
								
								if (name.equals(""))
									Workbench.getInstance().warn("please verify the name field.");
								else if (formula.equals(""))
									Workbench.getInstance().warn("please verify the formula field.");
								else if (charge.equals(""))
									Workbench.getInstance().warn("please verify the charge field.");
								else{
									if (insert)
										insert();
									else
										edit();
									
								simpleFinish();
								}
							}
						});
					}
					{
						jButtonCancel = new JButton();
						jPanelSaveClose.add(jButtonCancel, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jButtonCancel.setText("cancel");
						jButtonCancel.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")), 0.1).resizeImageIcon());
						jButtonCancel.addActionListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent e) {
								
								simpleFinish();
								
								}
						});
					}
					this.add(jPanelSaveClose, new GridBagConstraints(1, 2, 3, 1, 0.0, 0.5, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
				}
			}
		
			this.setModal(true);
		}
		catch (Exception e) {

			e.printStackTrace();
		}
	}
	
	private void simpleFinish() {

		this.setVisible(false);
		this.dispose();
	}
	
	private void insert(){
		
		if (reactantsProducts.getMetaboliteOccurrence(name)==0)
			reactantsProducts.insertData(name, entryType, formula, molecularW, charge);
		else
			Workbench.getInstance().warn("the metabolite " + name + " already exists.");
	}
	
	private void edit(){
		
		String keggID = reactantsProducts.getKeggIdOccurence(name);
		
		if (keggID == null)
			Workbench.getInstance().warn("the metabolite " + name + " already exists.");
		
		else if(keggID.isEmpty())
			reactantsProducts.updateData(name, entryType, formula, molecularW, charge, metabolite, originalKEGG);
		
		else if(keggID.equalsIgnoreCase(originalKEGG))
			reactantsProducts.updateData(name, entryType, formula, molecularW, charge, metabolite, originalKEGG);
		
		else
			Workbench.getInstance().warn("the metabolite " + name + " already exists.");
			
			
		}
			
}
