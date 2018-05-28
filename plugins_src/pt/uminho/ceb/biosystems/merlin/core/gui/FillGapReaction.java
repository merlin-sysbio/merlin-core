package pt.uminho.ceb.biosystems.merlin.core.gui;


import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactionsInterface;
import pt.uminho.ceb.biosystems.merlin.core.utilities.CreateImageIcon;
import pt.uminho.ceb.biosystems.merlin.core.utilities.DatabaseLoaders;
import pt.uminho.ceb.biosystems.merlin.core.utilities.Enumerators.InformationType;
import pt.uminho.ceb.biosystems.merlin.core.utilities.LoadFromConf;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ModelAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Enumerators.DatabaseType;
import pt.uminho.ceb.biosystems.merlin.utilities.Pair;
import pt.uminho.ceb.biosystems.merlin.utilities.io.FileUtils;

public class FillGapReaction extends JDialog {

	private static final long serialVersionUID = -1L;
	private long reference_organism_id;
	private ReactionsInterface reactionInterface;

	private DataTable data;
	private String reactionID;
	private JButton jButtonOK, jButtonCancel;

	/**
	 * @param reactionName
	 * @param row
	 * @param informationTable
	 */
	public FillGapReaction(ReactionsInterface reactionInterface, String reactionID, double similarityThreshold, Statement statement) {

		super(Workbench.getInstance().getMainFrame());
		this.reactionID=reactionID;
		this.reactionInterface = reactionInterface;
		Utilities.centerOnOwner(this);
		this.reference_organism_id = reactionInterface.getProject().getTaxonomyID();

		String databaseName = reactionInterface.getProject().getDatabase().getDatabaseName();

		int flag = -1;

			Pair<Integer, DataTable> pair = openFile(databaseName, reference_organism_id, this.reactionID, similarityThreshold);
			flag = pair.getA();
			this.data = pair.getB();
		
		if(flag==1){
			initGui(statement);
			Utilities.centerOnOwner(this);
			this.setAlwaysOnTop(true);
			this.toFront(); 
			this.setVisible(true);

		}
		else { 
			Workbench.getInstance().warn("nothing found");
		} 
	}

	/**
	 * @throws SQLException 
	 * 
	 */
	private void apply(List<String> entries, Statement statement) throws SQLException {


		DatabaseType databaseType = reactionInterface.getProject().getDatabase().getDatabaseAccess().get_database_type();

		Map<String, List<String>> reactions  = new HashMap<>();
		for(int i = 0; i < entries.size(); i++) {

			String geneID = DatabaseLoaders.loadGeneLocusFromHomologyData(entries.get(i), statement, databaseType, InformationType.KO);

			String ecs = ((String) data.getValueAt(i, 3));
			String[] ecNumbers = ecs.split(",");

			Set<String> ec = new HashSet<String>();


			for(String ecNumber : ecNumbers) {
				ec.add(ecNumber.trim());				
			}
			reactions.putAll(ModelAPI.loadEnzymeGetReactions(geneID, ec, null, statement, true, true, false, databaseType));	

		}

		if (reactions.keySet().isEmpty()){
			Workbench.getInstance().warn("no reactions to be included in model");
		}
		else {
			Set<String> react  = new HashSet<String>();

			for(String key : reactions.keySet()){

				List<String> names = reactions.get(key);

				try {

					react = ProjectAPI.getReactionName(names, statement);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}

			windowToBack();

			if(react.size() > 0){

				String reacts = react.toString();
				Workbench.getInstance().info("reactions applied to the model "+reacts.replace("[", "").replace("]", ""));
				String projectName = reactionInterface.getProject().getName();
				MerlinUtils.updateMetabolicViews(projectName);

			}
			else	
				Workbench.getInstance().warn("no reactions to be included in model");

		}
	}

	public static Pair<Integer,DataTable> openFile(String databaseName, long taxonomyID, String reactionID, double similarityThreshold) {

		List<String> alignCol = new ArrayList<>();
		alignCol.add("locus tag");
		alignCol.add("sequence id");
		alignCol.add("orthologous");
		alignCol.add("alignment score");
		alignCol.add("ec number");
		alignCol.add("query coverage");
		alignCol.add("target's coverage");
		alignCol.add("select");

		GenericDataTable data = new GenericDataTable(alignCol, "alignment table", "alignment results");

		Pair<Integer,DataTable> pair = new Pair<Integer, DataTable>(null, null);

		String path = FileUtils.getWorkspaceTaxonomyFolderPath(databaseName, taxonomyID) + "FillGapReactions.txt";
		File file = new File(path);

		try {

			Scanner input = new Scanner(file);
			Map<String, String> credentials = LoadFromConf.loadReactionsThresholds(FileUtils.getConfFolderPath());
			similarityThreshold = Double.valueOf(credentials.get("similarity_threshold"));



			boolean empty = false;
			while (input.hasNextLine()){

				List<Object> line = new ArrayList<>();
				String[] str = input.nextLine().trim().split("\t");
				double simas = Double.parseDouble(str[1]);

				if(str[0].equals(reactionID) && simas<=similarityThreshold && !str[2].equals("empty")){

					for(int i=2; i < str.length; i++)
						line.add(str[i]);

					line.add(new Boolean(true));
					data.addLine(line);
				}
				else if(str[0].equals(reactionID) && simas<=similarityThreshold && str[2].equals("empty")) {
					empty = true;
				}
			}

			pair.setB(data);

			input.close();
			if(data.getRowCount()!=0){
				pair.setA(1);
				return pair;

			}
			else if(data.getRowCount()==0 && empty==true) {
				pair.setA(0);
				return pair;
			}


		} catch (NumberFormatException e) {
			e.printStackTrace();
			pair.setA(-1);
			return pair;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		pair.setA(-1);
		return pair;

	}



	public void initGui(Statement statement){

		//JPanel jPanel = new JPanel();
		GridBagLayout thisLayout = new GridBagLayout();
		thisLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
		thisLayout.columnWidths = new int[] {7, 7, 7};
		thisLayout.rowWeights = new double[] {0.0, 200.0, 0.0, 0.0, 0.0};
		thisLayout.rowHeights = new int[] {7, 50, 7, 3, 7};
		this.setLayout(thisLayout);
		this.setPreferredSize(new Dimension(900, 300));
		this.setSize(900, 300);
		this.setLayout(thisLayout);


		JScrollPane jScrollPane = new JScrollPane();
		this.add(jScrollPane,new GridBagConstraints(1,1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		JTable jTable = new JTable();
		jTable.setAutoCreateRowSorter(true);
		jTable.setModel(data);
		DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		jTable.setDefaultRenderer(String.class, centerRenderer);
		this.setTitle("select the row(s) to apply to the model");
		jScrollPane.setViewportView(jTable);
		this.setModal(true);


		JPanel jPanel = new JPanel();
		jPanel.setBorder(BorderFactory.createTitledBorder("options"));
		this.add(jPanel,new GridBagConstraints(1,3, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));



		jButtonOK = new JButton();
		jPanel.add(jButtonOK, new GridBagConstraints(2, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jButtonOK.setText("ok");
		jButtonOK.setToolTipText("apply to the model");
		jButtonOK.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")),0.08).resizeImageIcon());
		jButtonOK.setPreferredSize(new Dimension(100, 30));
		jButtonOK.setSize(100, 30);
		jButtonOK.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				List<String> entries = readSelectedEntries(jTable);

				try {

					if(entries.size() > 0){
						apply(entries, statement);
						simpleFinish();
					}
					else{
						windowToBack();
						Workbench.getInstance().warn("no rows selected to apply to the model!");
					}

				} catch (Exception e1) {
					windowToBack();
					Workbench.getInstance().warn("error while apllying!");
					e1.printStackTrace();
				}
			}
		});



		jButtonCancel = new JButton();
		jPanel.add(jButtonCancel, new GridBagConstraints(4, 2, 2, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
		jButtonCancel.setText("cancel");
		jButtonCancel.setToolTipText("close the tabel");
		jButtonCancel.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")),0.08).resizeImageIcon());
		jButtonCancel.setPreferredSize(new Dimension(100, 30));
		jButtonCancel.setSize(100, 30);
		jButtonCancel.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				simpleFinish();
			}
		});

	}

	/**
	 * Reads the selected lines.
	 * 
	 */
	private List<String> readSelectedEntries(JTable jTable){

		List<String> toApply =new ArrayList<>();

		for(int i = 0; i < jTable.getRowCount(); i++){

			Boolean selected = new Boolean((boolean) jTable.getValueAt(i, 7));

			if( selected == true)
				toApply.add((String) jTable.getValueAt(i, 1));

		}
		return toApply;
	}

	private void windowToBack(){

		this.toBack();

	}

	private void simpleFinish() {
		this.setVisible(false);
		this.dispose();
	}
}










