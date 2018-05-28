package pt.uminho.ceb.biosystems.merlin.core.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.ParamSpec;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.GenericDataTable;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.annotation.CompartmentsAnnotationDataContainer;
import pt.uminho.ceb.biosystems.merlin.core.utilities.CreateImageIcon;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MyJTable;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.CompartmentsAPI;


public class IgnoreCompartments extends javax.swing.JDialog{

		private static final long serialVersionUID = -1L;
		private boolean biochemical, transporters;
		private JPanel jPanel1, jPanel2;
		private JScrollPane jScrollPane;
		private MyJTable newjTable = new MyJTable();
		@SuppressWarnings("unused")
		private int selectedModelRow;
		private Statement statement;
		private Map<String, String> compartments;
		CompartmentsAnnotationDataContainer compartmentsContainer;

		/**
		 * @param sampleSize
		 * @param ecnumbersColumnNumber
		 * @param ecScoreColumnNumber
		 * @param values
		 * @param itemsList
		 * @param locus_tagColumnNumber
		 * @param data
		 * @param homologyDataContainer
		 */
		public IgnoreCompartments(Boolean biochemical, Boolean transporters, CompartmentsAnnotationDataContainer compartmentsContainer, Statement statement) {

			super(Workbench.getInstance().getMainFrame());
			this.biochemical = biochemical;
			this.transporters = transporters;
			this.statement = statement;
			this.compartmentsContainer = compartmentsContainer;
			this.compartments = new HashMap<>();

			initGUI();
			Utilities.centerOnOwner(this);
			this.setVisible(true);		
			this.setAlwaysOnTop(true);
			this.toFront();
		}

		private void initGUI() {
				
				GridBagLayout thisLayout = new GridBagLayout();
				thisLayout.columnWeights = new double[] {0.0, 0.1, 0.0};
				thisLayout.columnWidths = new int[] {7, 7, 7};
				thisLayout.rowWeights = new double[] {0.0, 200.0, 0.0, 0.0, 0.0};
				thisLayout.rowHeights = new int[] {7, 50, 7, 3, 7};
				this.setLayout(thisLayout);
				this.setPreferredSize(new Dimension(875, 585));
				this.setSize(500, 500);
				{
					this.setTitle("choose the compartments to ignore");

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
					jScrollPane.setPreferredSize(new java.awt.Dimension(700, 700));
					jScrollPane.setSize(500, 420);
					{
						jPanel2 = new JPanel();
						jPanel1.add(jPanel2, new GridBagConstraints(0, 10, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
						jPanel2.setBorder(BorderFactory.createTitledBorder("options"));
						GridBagLayout jPanel12Layout = new GridBagLayout();
						jPanel2.setLayout(jPanel12Layout);
						jPanel12Layout.columnWeights = new double[] { 0.0, 0.1, 0.0, 0.0 };
						jPanel12Layout.columnWidths = new int[] { 3, 20, 7, 50 };
						jPanel12Layout.rowWeights = new double[] { 0.1 };
						jPanel12Layout.rowHeights = new int[] { 7 };
						
						{
							JButton jButtonSave = new JButton();
							jButtonSave.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Ok.png")),0.1).resizeImageIcon());
							jPanel2.add(jButtonSave, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
									GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
							jButtonSave.setText("ok");
							jButtonSave.addActionListener(new ActionListener() {

								public void actionPerformed(ActionEvent arg0) {
									
									List<String> ignore = readTable();
									
									ParamSpec[] paramsSpec = new ParamSpec[]{
											new ParamSpec("biochemical", Boolean.class, biochemical, null),
											new ParamSpec("transporters", Boolean.class, transporters, null),
											new ParamSpec("ignore", List.class, ignore, null),
											new ParamSpec("project", Project.class, compartmentsContainer.getProject(), null),
											new ParamSpec("geneCompartments", Map.class, compartmentsContainer.runCompartmentsInterface(statement), null)
									};
	
									for (@SuppressWarnings("rawtypes") OperationDefinition def : Core.getInstance().getOperations()){
										if (def.getID().equals("operations.IntegrateCompartmentstoDatabase.ID")){
	
											Workbench.getInstance().executeOperation(def, paramsSpec);
										}
									}
									
									simpleFinish();
								}});
						}
						{
							JButton jButtonClose = new JButton();
							jButtonClose.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")),0.1).resizeImageIcon());
							jPanel2.add(jButtonClose, new GridBagConstraints(3, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER,
									GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
							jButtonClose.setText("cancel");
							jButtonClose.setToolTipText("close window without continue");
							jButtonClose.setBounds(1, 1, 40, 20);
							jButtonClose.addActionListener(new ActionListener() {

								public void actionPerformed(ActionEvent arg0) {
									
									simpleFinish();
								}});
						}
					}
				
				}
				newjTable.setModel(buildTable());
				
				newjTable.setRowHeight(30);
						
				jScrollPane.setViewportView(newjTable);

				this.setModal(true);
		}

		
		public void simpleFinish() {

			this.setVisible(false);
			this.dispose();
		}

		/**
		 * Method to build the table containing the compartments and the respective checkbox.
		 * 
		 * @return
		 */
		private DataTable buildTable(){
			
			ArrayList<String> columnsNames = new ArrayList<String>();

			columnsNames.add("compartment");
			columnsNames.add("ignore");
			
			GenericDataTable data = new GenericDataTable(columnsNames, "genes", "gene data"){
				private static final long serialVersionUID = 1L;
				@Override
				public boolean isCellEditable(int row, int col) {

					if (col==1) {

						return true;
					}
					else return false;
				}
			};
			
			try {
			
			Map<String, String> compartments = CompartmentsAPI.getCompartments(statement);
			
			setCompartments(compartments);
			
			List<Object> ql = null;
			
			for(String name: compartments.keySet()){

				ql = new ArrayList<Object>();
				
				ql.add(name);
				ql.add(new Boolean(false));
				
				data.addLine(ql, "");
			}
			
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return data;
		}
		
		/**
		 * Reads the compartments selected to ignore.
		 * 
		 */
		private List<String> readTable(){
			
			List<String> toIgnore =new ArrayList<>();
			
			Map<String, String> comp = getCompartments();
 			
			for(int i = 0; i < newjTable.getRowCount(); i++){
				
				if(new Boolean((boolean) newjTable.getValueAt(i, 1)) == true){
					
					String compartment = comp.get((String) newjTable.getValueAt(i, 0));
					
					toIgnore.add(compartment);
				}
			}
			return toIgnore;
		}

		/**
		 * @return the compartments
		 */
		public Map<String, String> getCompartments() {
			return compartments;
		}

		/**
		 * @param compartments the compartments to set
		 */
		private void setCompartments(Map<String, String> compartments) {
			this.compartments = compartments;
		}

}
