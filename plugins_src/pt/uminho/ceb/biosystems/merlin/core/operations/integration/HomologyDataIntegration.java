package pt.uminho.ceb.biosystems.merlin.core.operations.integration;

import java.util.Observable;
import java.util.Observer;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;

/**
 * @author Antonio Dias
 *
 */
@Operation(name="Add TRIAGE Data", description="adds TRIAGE data from similarities")
public class HomologyDataIntegration implements Observer{
	
	private Project project;

	@Port(direction=Direction.INPUT, name="Alpha",description="Select Alpha",validateMethod="setAlpha",order=1)
	public void setAlph(double alpha){};
	
	@Port(direction=Direction.INPUT, name="Threshold",description="Select Threshold",validateMethod="setThreshold",order=2)
	public void setThold(double threshold){};
	
	@Port(direction=Direction.INPUT, name="Ignore Metabolites",description="Select Metabolites to Ignore",validateMethod="setIgnoreSymportMetabolites",order=3)
	public void setMetabolites(String metabolite){};
	
	@Port(direction=Direction.INPUT, name="Workspace",description="Select Workspace",//validateMethod="checkProject",
			order=4)
	public void setProject(Project project) {
		this.project = project;
		
	}
	
	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
