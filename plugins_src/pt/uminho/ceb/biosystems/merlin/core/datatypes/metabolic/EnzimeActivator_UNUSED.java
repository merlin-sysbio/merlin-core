package pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic;

import java.io.Serializable;

import pt.uminho.ceb.biosystems.merlin.core.datatypes.Table;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;

public class EnzimeActivator_UNUSED extends Entity implements Serializable {

	private static final long serialVersionUID = 7041578259945388261L;

	public EnzimeActivator_UNUSED(Table dbt, String name)
	{
		super(dbt, name);
	}
	
	public String[][] getStats()
	{
		String[][] res = new String[0][];
		
		return res;
	}
}
