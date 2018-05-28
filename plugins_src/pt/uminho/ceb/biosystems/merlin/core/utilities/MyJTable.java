package pt.uminho.ceb.biosystems.merlin.core.utilities;

import java.util.Vector;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

public class MyJTable extends JTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MyJTable() {
		super();
		this.setRenderer(Object.class);
		this.showGrid(false);
	}

	public MyJTable(TableModel dm) {
		super(dm);
		this.setRenderer(Object.class);
		this.showGrid(false);
	}

	public MyJTable(TableModel dm, TableColumnModel cm) {
		super(dm, cm);
		this.setRenderer(Object.class);
		this.showGrid(false);
	}

	public MyJTable(int numRows, int numColumns) {
		super(numRows, numColumns);
		this.setRenderer(Object.class);
		this.showGrid(false);
	}

	@SuppressWarnings("rawtypes")
	public MyJTable(Vector rowData, Vector columnNames) {
		super(rowData, columnNames);
		this.setRenderer(Object.class);
		this.showGrid(false);
	}

	public MyJTable(Object[][] rowData, Object[] columnNames) {
		super(rowData, columnNames);
		this.setRenderer(Object.class);
		this.showGrid(false);
	}

	public MyJTable(TableModel dm, TableColumnModel cm, ListSelectionModel sm) {
		super(dm, cm, sm);
		this.setRenderer(Object.class);
		this.showGrid(false);
	}

	private void setRenderer(Class<?> columnClass) {
		
		DefaultTableCellRenderer centerRenderer = (DefaultTableCellRenderer) super.getDefaultRenderer(columnClass);
		
		centerRenderer.setHorizontalAlignment( SwingUtilities.CENTER );
		
		super.setDefaultRenderer(columnClass, centerRenderer);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JTable#setDefaultRenderer(java.lang.Class, javax.swing.table.TableCellRenderer)
	 */
	public void setDefaultRenderer(Class<?> columnClass, TableCellRenderer renderer) {

		super.setDefaultRenderer(columnClass,null);
		super.setDefaultRenderer(columnClass, renderer);
	}

	private void showGrid(boolean show) {

		super.setShowGrid(show);
	}

}
