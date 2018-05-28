package pt.uminho.ceb.biosystems.merlin.core.operations.integration;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import es.uvigo.ei.aibench.core.operation.annotation.Cancel;
import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.core.operation.annotation.Progress;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.IntegrateCompartmentsData;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.IntegrateTransportersData;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.utilities.MerlinUtils;
import pt.uminho.ceb.biosystems.merlin.transporters.core.compartments.GeneCompartments;
import pt.uminho.ceb.biosystems.merlin.utilities.TimeLeftProgress;

@Operation(name="IntegrateTransporterstoDatabase", description="Integrates the generated transporters data into the model")
public class IntegrateTransporterstoDatabase {

	private IntegrateTransportersData transportersData;
	private Map<String, GeneCompartments> geneCompartments;
	private TimeLeftProgress progress = new TimeLeftProgress();
	
	@Port(direction=Direction.INPUT, name="compartments",description="select compartments", order=1)
	public void setIntegration(Map<String, GeneCompartments> geneCompartments) throws Exception {

		this.geneCompartments = geneCompartments;
	}

	@Port(direction=Direction.INPUT, name="workspace",description="select workspace", order=2)
	public void setProject(Project project) throws Exception {

		boolean isCompartmentalisedModel = project.isCompartmentalisedModel();
		//if(project.isGeneDataAvailable()) {

		if(!isCompartmentalisedModel || (isCompartmentalisedModel && this.geneCompartments!= null)) {

			if(project.isMetabolicDataAvailable()) {

				if(project.getTransportContainer()!= null) {

					this.transportersData = new IntegrateTransportersData(project);
					this.transportersData.setTimeLeftProgress(this.progress);

					boolean result = this.transportersData.performIntegration();

					if(result) {

						if(!transportersData.isCancel().get()) {

							if(project.isCompartmentalisedModel()) {

								TimeLeftProgress progress = new TimeLeftProgress();
								AtomicInteger size = new AtomicInteger(), position = new AtomicInteger();

								IntegrateCompartmentsData integration = new IntegrateCompartmentsData(project, geneCompartments);
								integration.setQuerySize(size);
								integration.setProcessingCounter(position);
								integration.setTimeLeftProgress(progress);
								integration.performIntegration();
								result = integration.assignCompartmentsToTransportReactions(new ArrayList<String>());
							}

							Workbench.getInstance().info("Transporters integration complete!");
						}
						else {

							Workbench.getInstance().warn("Transporters integration cancelled!");
						}
					}
					else{

						Workbench.getInstance().info("An error occurred while performing the operation.");
					}
				}
				else {

					if(project.isSW_TransportersSearch()) {

						Workbench.getInstance().info("Please generate the transport reactions!");

					}
					else {

						if(project.isTransporterLoaded())
							Workbench.getInstance().info("Please perform the transporters identification and transport reactions annotation!");
						else
							Workbench.getInstance().info("Please annotate the transport reactions!");
					}
				}
			}
			else {

				Workbench.getInstance().warn("Metabolic data for integration unavailable!");
			}
		}
		else {
			Workbench.getInstance().warn("Please perform the compartments prediction to assign compartments to transport reactions!");
		}	
		//		else {
		//
		//			Workbench.getInstance().error("Gene data for integration unavailable!");
		//		}

		MerlinUtils.updateReactionsView(project.getName());
		MerlinUtils.updateTransportersAnnotationView(project.getName());
	}


	/**
	 * @return
	 */
	@Progress
	public TimeLeftProgress getProgress() {

		return this.progress;
	}

	/**
	 * 
	 */
	@Cancel
	public void cancel(){
		this.progress.setTime((GregorianCalendar.getInstance().getTimeInMillis()-GregorianCalendar.getInstance().getTimeInMillis()),1,1);
		this.transportersData.setCancel();
	}

	/**
	 * @param project
	 */
	public void checkProject(Project project){

		if(project == null) {

			throw new IllegalArgumentException("No Project Selected!");
		}
		else {

			if(project.getTransportContainer()==null) {

				throw new IllegalArgumentException("Please perform the Transport Reactions Annotation operation before integrating transporters data.");
			}
		}
	}
}