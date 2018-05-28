package pt.uminho.ceb.biosystems.merlin.core.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Observable;

import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.ListElements;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;

/**
 * @author Oscar Dias
 *
 */
@Datatype(structure = Structure.LIST,namingMethod="getName")
public class Annotations extends Observable implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ArrayList<Entity> annotations = null;

	/**
	 * 
	 */
	public Annotations() {

		this.annotations = new ArrayList<Entity>();
	}


	public ArrayList<Entity> getEntities() {
		return annotations;
	}


	@ListElements
	public ArrayList<Entity> getEntitiesList() {
		return annotations;
	}

	public void setAnnotations(ArrayList<Entity> enties) {
		this.annotations = enties;
		setChanged();
		notifyObservers();
	}

	public String getName() {
		return "annotation";
	}
}
