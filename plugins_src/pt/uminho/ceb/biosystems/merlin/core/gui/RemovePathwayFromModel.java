package pt.uminho.ceb.biosystems.merlin.core.gui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.TitledBorder;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic.ReactionsInterface;
import pt.uminho.ceb.biosystems.merlin.core.utilities.CreateImageIcon;
import pt.uminho.ceb.biosystems.merlin.database.connector.databaseAPI.ProjectAPI;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.Connection;
import es.uvigo.ei.aibench.workbench.Workbench;
import es.uvigo.ei.aibench.workbench.utilities.Utilities;

/**
 * @author ODias
 *
 */
public class RemovePathwayFromModel  extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jPanel1;
	private JComboBox<String> pathways;
	private JButton jButton1;
	private JButton jButton2;
	private String[] paths;
	private boolean removed;
	private Connection connection;

	/**
	 * @param reactionsInterface
	 */
	public RemovePathwayFromModel(ReactionsInterface reactionsInterface) {
		
		super(Workbench.getInstance().getMainFrame());
		this.connection=reactionsInterface.getConnection();
		this.removed=false;
		this.paths = reactionsInterface.getPathways(true);
		this.initGUI("Remove Pathway");
		Utilities.centerOnOwner(this);
		this.setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
		this.setVisible(true);		
		this.setAlwaysOnTop(true);
		this.toFront();
		this.setModal(false);
	}

	private void initGUI(String windowName){
		{
			{
				this.setTitle(windowName);
				jPanel1 = new JPanel();
				getContentPane().add(jPanel1, BorderLayout.NORTH);
				GridBagLayout jPanel1Layout = new GridBagLayout();
				jPanel1Layout.columnWeights = new double[] {0.0, 0.1, 0.2, 0.0, 0.2, 0.1, 0.0};
				jPanel1Layout.columnWidths = new int[] {7, 20, 20, 7, 20, 20, 7};
				jPanel1Layout.rowWeights = new double[] {0.0, 0.3, 0.1, 0.0};
				jPanel1Layout.rowHeights = new int[] {7, 7, 20, 7};
				jPanel1.setLayout(jPanel1Layout);
				jPanel1.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
				jPanel1.setPreferredSize(new java.awt.Dimension(431, 156));
				{
					pathways = new JComboBox<>(this.paths);
					jPanel1.add(pathways, new GridBagConstraints(2, 1, 3, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
					pathways.setBorder(BorderFactory.createTitledBorder(new SoftBevelBorder(BevelBorder.LOWERED, null, null, null, null), "Remove pathway from model", TitledBorder.LEADING, TitledBorder.DEFAULT_POSITION));
				}
				{
					jButton1 = new JButton();
					jPanel1.add(jButton1, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jButton1.setText("Save");
					jButton1.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Save.png")),0.1).resizeImageIcon());
					jButton1.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent evt) {
						insertData();removed=true;}
					});
				}
				{
					jButton2 = new JButton();
					jPanel1.add(jButton2, new GridBagConstraints(4, 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					jButton2.setText("Cancel");
					jButton2.setIcon(new CreateImageIcon(new ImageIcon(getClass().getClassLoader().getResource("icons/Cancel.png")),0.1).resizeImageIcon());			
					jButton2.addActionListener(new ActionListener() {public void actionPerformed(ActionEvent evt) {if(removed){finish();}else{simpleFinish();}}});
				}
			}
		}
		this.setModal(true);
		this.pack();
	}

	public void finish(){}
	
	public void simpleFinish(){}

	public void insertData(){
		//MySQLMultiThread dsa =  new MySQLMultiThread( this.host,this.port, this.dbName, this.user, this.pass);
		Statement stmt;
		try {
			
			stmt = this.connection.createStatement();
			
			int pathwayID = ProjectAPI.getPathwayID(pathways.getSelectedItem()+"", stmt);
			
			if(pathwayID>0) {
				
				int idPathway = pathwayID;
				
				Set<String> ecnumbers = ProjectAPI.getEcNumberByPathwayID(idPathway, stmt);
				Set<String> reactions = ProjectAPI.getReactionIdByPathwayID(idPathway, stmt);
				
				String query = "DELETE FROM pathway_has_enzyme WHERE pathway_idpathway =('" + idPathway + "')";
				ProjectAPI.executeQuery(query, stmt);
				
				query = "DELETE FROM pathway_has_reaction WHERE pathway_idpathway =('" + idPathway + "')";
				ProjectAPI.executeQuery(query, stmt);
				
				for(String ecnumber:new TreeSet<String>(ecnumbers)){
					boolean exists = ProjectAPI.checkPathwayHasEnzymeEntryByECnumber(ecnumber, stmt);
					if(exists)
						ecnumbers.remove(ecnumber);
					else{
						query = "DELETE FROM pathway_has_enzyme WHERE enzyme_ecnumber =('"+ecnumber+"')";
						ProjectAPI.executeQuery(query, stmt);
					}
				}
				
				for(String reaction:new TreeSet<String>(reactions))
				{
					boolean exists = ProjectAPI.checkPathwayHasEnzymeEntryByReactionID(reaction, stmt);
					if(exists)
		
						reactions.remove(reaction);
					else
					{
						query = "DELETE FROM pathway_has_reaction WHERE reaction_idreaction='"+reaction+"'";
						ProjectAPI.executeQuery(query, stmt);
					}
				}
				stmt.close();
				finish();
			}
		}
		catch (SQLException e){e.printStackTrace();}
	}
}