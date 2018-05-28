package pt.uminho.ceb.biosystems.merlin.core.datatypes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Observable;

import es.uvigo.ei.aibench.core.datatypes.annotation.Datatype;
import es.uvigo.ei.aibench.core.datatypes.annotation.ListElements;
import es.uvigo.ei.aibench.core.datatypes.annotation.Structure;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;

@Datatype(structure = Structure.LIST,namingMethod="getName")
public class Entities extends Observable implements Serializable {

	private static final long serialVersionUID = 1L;

	private ArrayList<Entity> entities = null;
	
	public ArrayList<Entity> getEntities() {
		return entities;
	}

	public Entities()
	{
		this.entities = new ArrayList<Entity>();
	}
	
	@ListElements
	public ArrayList<Entity> getEntitiesList() {
		return entities;
	}

	public void setEntities(ArrayList<Entity> enties) {
		this.entities = enties;
		setChanged();
		notifyObservers();
	}
	
	public String getName() {
		return "model";
	}
}