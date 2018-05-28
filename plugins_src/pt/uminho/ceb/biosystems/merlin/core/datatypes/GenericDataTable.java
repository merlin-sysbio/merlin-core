package pt.uminho.ceb.biosystems.merlin.core.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oscar
 *
 */
public class GenericDataTable extends DataTable implements Serializable{

	private static final long serialVersionUID = 1371428859304583691L;

	protected String windowName;
	protected ArrayList<String> metaboliteType;
	private ArrayList<String> ids;
	
	/**
	 * @param columnsNames
	 * @param name
	 * @param windowName
	 */
	public GenericDataTable(List<String> columnsNames, String name, String windowName) {
		
		super(columnsNames, name);
		this.windowName = windowName;
		this.ids = new ArrayList<String>();
		this.metaboliteType = new ArrayList<String>();
    }
    
    /**
     * @param line
     * @param id
     */
    public void addLine(ArrayList<Object> line,  String id) {
    	
    	super.addLine(line);
    	this.ids.add(id);
    }
    
    /**
     * @param line
     * @param id
     */
    public void addLine(List<Object> line, String id ) {
      	super.addLine(line);
    	this.ids.add(id);
	}
        
    /**
     * @param line
     * @param id
     */
    public void addLine(Object[] line,  String id) {
    	super.addLine(line);
    	this.ids.add(id);
    }
    
    /**
     * @param line
     * @param type
     * @param id
     */
    public void addLine(ArrayList<Object> line, String type, String id) {
    	super.addLine(line);
    	this.metaboliteType.add(type);
    	this.ids.add(id);
    }
    
    /**
     * @param line
     * @param type
     * @param id
     */
    public void addLine(List<Object> line, String type, String id) {
    	super.addLine(line);
    	this.metaboliteType.add(type);
    	this.ids.add(id);
    }
    
    
    /**
     * @param line
     * @param type
     * @param id
     */
    public void setLine(int index, List<Object> line, String type, String id) {
    	super.setLine(index, line);
    	this.metaboliteType.set(index, type);
    	this.ids.set(index, id);
    }
    
    /**
     * @param row
     * @return
     */
    public String getRowType(int row) {
    	
    	return metaboliteType.get(row);
    }
    
    /**
     * @return
     */
    public String getWindowName() {
    	
    	return windowName;
    }
    
    /**
     * @param row
     * @return
     */
    public String getRowID(int row) {
    	
    	return ids.get(row);
    }

	/**
	 * @return
	 */
	public ArrayList<String> getIds() {
		return ids;
	}
	
	/**
	 * @param id
	 * @return
	 */
	public int getRowFromID(String id) {

		for(int i =0; ids.size()>i; i++)
			if(this.ids.get(i).equalsIgnoreCase(id))
				return i;
		
		return -1;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "GenericDataTable [windowName=" + windowName + ", ids=" + ids
				+ "]";
	}
	
	
}