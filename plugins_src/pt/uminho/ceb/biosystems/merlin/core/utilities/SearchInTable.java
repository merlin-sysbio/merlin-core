package pt.uminho.ceb.biosystems.merlin.core.utilities;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.transporters.core.transport.reactions.containerAssembly.TransportContainer;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.MetaboliteCI;
import pt.uminho.ceb.biosystems.mew.biocomponents.container.components.ReactionCI;

/**
 * @author ODias
 *
 */
public class SearchInTable {

	private JComboBox<String> searchComboBox;
	private JTextField jTextFieldTotal, jTextFieldResult, searchTextField;
	private MyJTable jTable;
	private DataTable mainTableData;
	private int presentRow;
	private List<Integer> rowsList;
	private JButton jButtonPrevious;
	private JButton jButtonNext;
	private JLabel jLabel1;
	private List<Integer> nameTabs;
	private Project project;
	private String[] searchParams;

	/**
	 * 
	 */
	public SearchInTable (List<Integer> nameTabs) {

		searchTextField = new JTextField();
		this.nameTabs = nameTabs;
		this.searchParams = new String[] { "name", "all" };
	}

	/**
	 * @param nameTabs
	 * @param searchParams
	 * @param project
	 */
	public SearchInTable (List<Integer> nameTabs, String[] searchParams, Project project) {

		searchTextField = new JTextField();
		this.nameTabs = nameTabs;
		this.searchParams = searchParams;
		this.project = project;
	}

	/**
	 * @param text
	 */
	public void searchInTable(String text) {

		text=text.toLowerCase();
		rowsList = new ArrayList<Integer>();
		Set<Integer> rows = new TreeSet<Integer>();

		DefaultListSelectionModel selectionModel = new DefaultListSelectionModel();
		//		int i=0;
		presentRow = 0;
		ArrayList<Object[]> tab = mainTableData.getTable();
		
//		jTable.setAutoCreateRowSorter(true);
		
		switch(searchComboBox.getSelectedIndex())
		{
		case 0:
		{
			for(int z=0;z<tab.size();z++) {

				Object[] subtab = tab.get(z);

				for(int t : this.nameTabs) {

					boolean go = subtab!=null;
					go = go && subtab.length>0;
					go = go && subtab[t]!=null;

					if(go && ((String)subtab[t]).toLowerCase().contains(text)) {
						
//						int modelRow = jTable.getRowSorter().convertRowIndexToView(new Integer(z));
						int modelRow = z;
						rows.add(modelRow);
					}
				}
			}
			break;
		}
		case 1:
		{
			for(int z=0;z<tab.size();z++) {

				Object[] subtab = tab.get(z);

				for(Object obj:subtab) {

					if(obj != null && obj.getClass().equals(String.class)) {

						if(((String)obj).toLowerCase().contains(text)) {
							
//							int modelRow = jTable.getRowSorter().convertRowIndexToView(new Integer(z));
							int modelRow = z;
							rows.add(modelRow);
						}
					}

					List<String> found = new ArrayList<String>();

					if(obj != null && obj.getClass().equals(String[].class)) {

						found.addAll(Arrays.asList(((String[])obj)));

						for(String s: found) {

							if(s.toLowerCase().contains(text)) {
								
//								int modelRow = jTable.getRowSorter().convertRowIndexToView(new Integer(z));
								int modelRow = z;
								rows.add(modelRow);
							}
						}
					}
				}
			}
			break;
		}
		case 2:
		{	
			TransportContainer transportContainer = this.project.getTransportContainer();

			if (transportContainer != null && !text.trim().isEmpty()){
				Map<String,Integer> genes= new HashMap<>();
				for(int z=0;z<tab.size();z++) {
					
					Object[] subtab = tab.get(z);
//					int modelRow = jTable.getRowSorter().convertRowIndexToView(new Integer(z));
					int modelRow = z;
					genes.put((String)subtab[1], modelRow);
				}

				Map<String, MetaboliteCI> metabolites = transportContainer.getMetabolites();
				Map<String, ReactionCI> reactions = transportContainer.getReactions();
				Set<String> reactionIDs = new HashSet<>();
				Set<String> geneIDs = new HashSet<>();

				for(String met:metabolites.keySet()){
					//					System.out.println(metabolites.get(met).getName());
					//					System.out.println(metabolites.get(met).getReactionsId());
					if (metabolites.get(met).getName().toLowerCase().contains(text)){
						reactionIDs.addAll(metabolites.get(met).getReactionsId());
					}
				}

				for(String reaction:reactionIDs){
					geneIDs.addAll(reactions.get(reaction).getGenesIDs());
				}

				for(String gene:geneIDs){
					rows.add(genes.get(gene));
				}
			}
			else {

				rows = new HashSet<>();
			}
			break;
		}
		default:
		{
			for(int z=0;z<tab.size();z++) {

				Object[] subtab = tab.get(z);

				for(int t :this.nameTabs) {

					if(((String) subtab[t]).toLowerCase().contains(text)) {
						
						int modelRow = jTable.getRowSorter().convertRowIndexToView(new Integer(z));
						rows.add(modelRow);
					}
				}
			}
			break;
		}

		}
		rowsList.addAll(rows);

		int row = 0;
		for(Integer r: rowsList) {

			row = r.intValue();
			selectionModel.addSelectionInterval(row, row);
		}

		jTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		jTable.setSelectionModel(selectionModel);

		if(selectionModel.isSelectionEmpty() && (searchTextField.getText().compareTo("")!=0)) {

			searchTextField.setForeground(new java.awt.Color(255,0,0));
			searchTextField.setBackground(new java.awt.Color(174,174,174));
			jTextFieldResult.setText("");
			jTextFieldTotal.setText("");
			rowsList = new ArrayList<Integer>();
		}
		else {
			searchTextField.setForeground(Color.BLACK);
			searchTextField.setBackground(Color.WHITE);
		}

		if(rowsList.size()!=0) {

			jTextFieldResult.setText(""+1);
			jTextFieldTotal.setText(""+rowsList.size());
			//jTable.scrollRectToVisible(jTable.getCellRect(rowsList.get(0), 0, true));
			MerlinUtils.scrollToCenter(jTable, rowsList.get(0), 0);
		}
		else {

			//this.setSearchTextField("");
		}
		
//		jTable.setRowSorter(null);
	}

	/**
	 * @return
	 */
	public JPanel addPanel() {

		JPanel jPanelSearchBox = new JPanel();
		GridBagLayout jPanel3Layout = new GridBagLayout();
		jPanelSearchBox.setBorder(BorderFactory.createTitledBorder("search"));
		jPanel3Layout.rowWeights = new double[] {0.0};
		jPanel3Layout.rowHeights = new int[] {3};
		jPanel3Layout.columnWeights = new double[] {1.1, 0.1, 0.0, 0.0, 0.0, 0.0, 0.0, 0.1, 0.0, 0.1};
		jPanel3Layout.columnWidths = new int[] {100, 20, 7, 7, 3, 3, 7, 6, 3, 6};
		jPanelSearchBox.setLayout(jPanel3Layout);
		{
			jButtonPrevious = new JButton();
			jButtonPrevious .setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Previous.png")),0.05).resizeImageIcon());
			jPanelSearchBox.add(jButtonPrevious, new GridBagConstraints(4, -1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jButtonPrevious.setToolTipText("Previous");
			jButtonPrevious.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0){

					if(rowsList.size()>0) {

						if(presentRow!=0) {

							presentRow-=1;
						}
						else {

							presentRow = rowsList.size()-1;
						}
						jTextFieldResult.setText(""+(presentRow+1));
						jTable.setRowSelectionInterval(rowsList.get(presentRow), rowsList.get(presentRow));
						//jTable.scrollRectToVisible(jTable.getCellRect(rowsList.get(presentRow), 0, true));
						MerlinUtils.scrollToCenter(jTable, rowsList.get(presentRow), 0);
					}
				}});
		}
		{
			jButtonNext = new JButton();
			jButtonNext .setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Next.png")),0.05).resizeImageIcon());
			jPanelSearchBox.add(jButtonNext, new GridBagConstraints(5, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			jButtonNext.setToolTipText("Next");
			jButtonNext.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent arg0) {

					if(rowsList.size()>0) {

						if(presentRow!=rowsList.size()-1) {

							presentRow+=1;
							jTextFieldResult.setText(""+(presentRow+1));
							jTable.setRowSelectionInterval(rowsList.get(presentRow), rowsList.get(presentRow));
							//jTable.scrollRectToVisible(jTable.getCellRect(rowsList.get(presentRow), 0, true));
							MerlinUtils.scrollToCenter(jTable, rowsList.get(presentRow), 0);
						}
						else {

							if(rowsList.size()>1) {

								presentRow=0;
								jTextFieldResult.setText(""+(presentRow+1));
								jTable.setRowSelectionInterval(rowsList.get(presentRow), rowsList.get(presentRow));
								//jTable.scrollRectToVisible(jTable.getCellRect(rowsList.get(presentRow), 0, true));
								MerlinUtils.scrollToCenter(jTable, rowsList.get(presentRow), 0);
								Workbench.getInstance().info("The end was reached!\n Starting from the top.");
							}
						}
					}
				}});
		}
		{
			jTextFieldResult = new JTextField();
			jTextFieldResult.setEditable(false);
			jPanelSearchBox.add(jTextFieldResult, new GridBagConstraints(7, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		{
			jLabel1 = new JLabel();
			jPanelSearchBox.add(jLabel1, new GridBagConstraints(8, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
			jLabel1.setText("of");
			jLabel1.setHorizontalAlignment(SwingConstants.CENTER);
			jLabel1.setHorizontalTextPosition(SwingConstants.CENTER);
		}
		{
			jTextFieldTotal = new JTextField();
			jTextFieldTotal.setEditable(false);
			jPanelSearchBox.add(jTextFieldTotal, new GridBagConstraints(9, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		}
		{

			searchTextField.setBounds(14, 12, 604, 20);
			searchTextField.setBorder(BorderFactory.createCompoundBorder(
					BorderFactory.createEtchedBorder(BevelBorder.LOWERED), null)
					);

			searchTextField.getDocument().addDocumentListener(new MyDocumentListener());

			//			searchTextField.addKeyListener(new KeyAdapter() {
			//				@Override
			//				public void keyTyped(KeyEvent evt) {
			//					
			//					JTextField textField = (JTextField) evt.getSource();
			//					searchInTable(textField.getText());
			//				}
			//				
			//			});


			ComboBoxModel<String> searchComboBoxModel = new DefaultComboBoxModel<>(searchParams);

			searchComboBox = new JComboBox<>();
			jPanelSearchBox.add(searchComboBox, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			jPanelSearchBox.add(searchTextField, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
			//				searchComboBox.setBounds(624, 12, 77, 20);

			searchComboBox.setModel(searchComboBoxModel);
			searchComboBox.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent arg0) {

					if(searchTextField.getText().compareTo("")!=0)
						searchInTable(searchTextField.getText());
					else
						resetSearch();

				}
			});
		}
		return jPanelSearchBox;
	}

	/**
	 * @return the searchTextField
	 */
	public JTextField getSearchTextField() {

		return searchTextField;
	}

	/**
	 * 
	 */
	public void refreshSearch() {

		if(searchTextField.getText().compareTo("")!=0){
			searchInTable(searchTextField.getText());
		}
		else{
			resetSearch();
		}
	}

	/**
	 * @param searchTextField the string to set
	 */
	public void setSearchTextField(String searchTextField) {

		this.searchTextField.setText(searchTextField);
		this.jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		if(this.jTable.getSelectedRow()>-1) {

			this.jTable.setRowSelectionInterval(jTable.getSelectedRow(), jTable.getSelectedRow());
			//this.jTable.scrollRectToVisible(this.jTable.getCellRect(jTable.getSelectedRow(), -1, true));
			MerlinUtils.scrollToCenter(jTable, rowsList.get(presentRow), -1);
		}


	}

	public void setMainTableData(DataTable mainTableData) {
		this.mainTableData = mainTableData;

	}

	public void setMyJTable(MyJTable jTable) {
		this.jTable = jTable;

	}

	class MyDocumentListener implements DocumentListener {

		public void insertUpdate(DocumentEvent event) {

			try {
				updateLog(event);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		public void removeUpdate(DocumentEvent event) {

			try {
				updateLog(event);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		public void changedUpdate(DocumentEvent event) {

			try {
				updateLog(event);
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

		public void updateLog(DocumentEvent event) throws BadLocationException {

			Document doc = (Document)event.getDocument();
			if(doc.getLength()>0)
				searchInTable(doc.getText(0, doc.getLength()));
			else
				resetSearch();
		}
	}

	public void resetSearch(){

		jTextFieldResult.setText("");
		jTextFieldTotal.setText("");
		jTextFieldResult.setToolTipText("");
		jTextFieldTotal.setToolTipText("");
		rowsList = new ArrayList<Integer>();
		jTable.setSelectionModel(new DefaultListSelectionModel());

	}

	/**
	 * @return the nameTabs
	 */
	public List<Integer> getNameTabs() {
		return nameTabs;
	}

	/**
	 * @param nameTabs the nameTabs to set
	 */
	public void setNameTabs(List<Integer> nameTabs) {
		this.nameTabs = nameTabs;
	}
}
