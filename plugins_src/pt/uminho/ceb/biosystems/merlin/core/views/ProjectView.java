package pt.uminho.ceb.biosystems.merlin.core.views;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;

/**
 * @author ODias
 *
 */
public class ProjectView extends UpdatablePanel{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JPanel jPanel1;
	private JLabel jLabel1;
	private JLabel jLabel2;
	private JLabel jLabel3;
	private JLabel jLabel4;
	private Project project;
	private int counter;


	/**
	 * @param p
	 */
	public ProjectView(Project project) {

		this.project = project;
		initGUI();
		this.addListenersToGraphicalObjects();
	}

	/**
	 * 
	 */
	private void initGUI() {

		try  {

			BorderLayout thisLayout = new BorderLayout();
			this.setLayout(thisLayout);
			this.jPanel1 = new JPanel();
			this.add(jPanel1, BorderLayout.CENTER);
			jPanel1.setBorder(BorderFactory.createTitledBorder(project.getName()+" data"));
			jPanel1.setLayout(null);

			jLabel1 = new JLabel();
			jPanel1.add(jLabel1);
			jLabel1.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel2 = new JLabel();
			jPanel1.add(jLabel2);
			jLabel2.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel3 = new JLabel();
			jPanel1.add(jLabel3);
			jLabel3.setBounds(24, this.increaseCounter(35), 489, 20);

			jLabel4 = new JLabel();
			jPanel1.add(jLabel4);
			jLabel4.setBounds(24, this.increaseCounter(35), 489, 20);
			
			this.fillList();

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	public void fillList() {

		this.setToolTipText("click to refresh");
		
//		jLabel1.setText("Database");
//		
//		jLabel2.setText("type: "+project.getDatabase().getDatabaseAccess().get_database_type());
//		
//		jLabel3.setText("name: "+project.getDatabase().getDatabaseAccess().get_database_name());
//
//		if(project.getDatabase().getDatabaseAccess().get_database_type().equals(DatabaseType.H2)) {
//			jLabel4.setText("host: localhost");
//
//			jLabel5.setText("port: ");
//		}
//		else {
//			jLabel4.setText("host: "+project.getDatabase().getDatabaseAccess().get_database_host());
//
//			jLabel5.setText("port: "+project.getDatabase().getDatabaseAccess().get_database_port());
//		}
//		
//		jLabel6.setText("user: "+project.getDatabase().getDatabaseAccess().get_database_user());
//		
		jLabel1.setText("Organism");
		
		if(project.getTaxonomyID()>0) {

			jLabel2.setText("name: "+project.getOrganismName());

			jLabel3.setText("lineage: "+project.getOrganismLineage());
			jLabel3.setBounds(new Double(jLabel3.getBounds().getX()).intValue(),
					new Double(jLabel3.getBounds().getY()).intValue(),
					project.getOrganismLineage().length()*8,
					new Double(jLabel3.getBounds().getHeight()).intValue()); 

			jLabel4.setText("taxonomy ID: "+project.getTaxonomyID());
		}

		
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

		this.addMouseListener(new MouseAdapter() {

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

	@Override
	public String getProjectName() {

		return this.project.getName();
	}

	/**
	 * @return the counter
	 */
	private int increaseCounter(int step) {
		counter = counter+step;
		return counter;
	}
}
