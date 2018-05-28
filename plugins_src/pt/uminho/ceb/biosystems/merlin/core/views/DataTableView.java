package pt.uminho.ceb.biosystems.merlin.core.views;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

import es.uvigo.ei.aibench.workbench.utilities.Utilities;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.DataTable;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MyJTable;


public class DataTableView extends javax.swing.JPanel {

	private static final long serialVersionUID = 7348937284724896584L;
	private JScrollPane jScrollPane1;
	private MyJTable jTable1;


	public DataTableView(DataTable d) {
		super();
		initGUI(d);
		Utilities.centerOnOwner(this);
	}
	
	private void initGUI(DataTable d) {
		try {
			BorderLayout thisLayout = new BorderLayout();
			this.setLayout(thisLayout);
			setPreferredSize(new Dimension(400, 300));
			{
				jScrollPane1 = new JScrollPane();
				this.add(jScrollPane1, BorderLayout.CENTER);
				jScrollPane1.setBorder(BorderFactory.createTitledBorder(d.getName()));
				{
					jTable1 = new MyJTable();
					jTable1.setShowGrid(false);
					jScrollPane1.setViewportView(jTable1);
					jTable1.setModel(d);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
