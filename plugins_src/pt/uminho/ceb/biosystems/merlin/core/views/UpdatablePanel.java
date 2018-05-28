/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.views;

import javax.swing.JPanel;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.utilities.UpdateUI;

/**
 * @author ODias
 *
 */
public abstract class UpdatablePanel extends JPanel implements UpdateUI {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Entity entity;

	/**
	 * 
	 */
	public UpdatablePanel() {
		
	}
	
	/**
	 * @param entity
	 */
	public UpdatablePanel(Entity entity){
		
		this.setEntity(entity);
	}
	
	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#updateTableUI()
	 */
	@Override
	public abstract void updateTableUI();

	/* (non-Javadoc)
	 * @see merlin_utilities.UpdateUI#addListenersToGraphicalObjects()
	 */
	@Override
	public abstract void addListenersToGraphicalObjects();
	
	@Override
	public String getProjectName() {

		return this.entity.getProject().getName();
	}

	/**
	 * @return
	 */
	public Entity getEntity() {
		return entity;
	}

	/**
	 * @param entity
	 */
	public void setEntity(Entity entity) {
		this.entity = entity;
	}

}
