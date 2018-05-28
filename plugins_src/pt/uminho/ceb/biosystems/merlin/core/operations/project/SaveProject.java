package pt.uminho.ceb.biosystems.merlin.core.operations.project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import es.uvigo.ei.aibench.core.operation.annotation.Direction;
import es.uvigo.ei.aibench.core.operation.annotation.Operation;
import es.uvigo.ei.aibench.core.operation.annotation.Port;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;

@Operation(description="Save existing project", name="Save Project.")
public class SaveProject {

	private Project project;
	private File file;
	private String fileName;


	/**
	 * 
	 * @param project project to be saved.
	 */
	@Port(name="Workspace",direction=Direction.INPUT, validateMethod="checkProject",description="Save Project",order=1)
	public void saveProject(Project project) {
		
		this.project = project;
	}

	@Port(name="Directory", direction=Direction.INPUT, description="Select Directory", order=2)
	public void save(File file) throws IOException {
		
		this.file = file;
		
	}

	/**
	 * 
	 * @param file file in which to save the project.
	 * @throws IOException
	 */
	@Port(name="File Name", direction=Direction.INPUT, description="Type desired file name", order=3)
	public void save(String filename) throws IOException {
		
		try {
			this.fileName=this.validateName(filename);
			
			String path = this.file.getAbsolutePath();
			if(!new File(path).isDirectory())
				path = this.file.getParent();
			
			path+=File.separator+this.fileName;
			
			FileOutputStream fo = new FileOutputStream(path);
			ObjectOutputStream oo = new ObjectOutputStream(fo);
			this.project.setFileName(path);
			oo.writeObject(this.project);

			oo.flush();
			fo.flush();
			oo.close();
			fo.close();
			Workbench.getInstance().info("Project saved successfully.");
		}
		catch (Exception e) {
			
			e.printStackTrace();
			Workbench.getInstance().error("There was some problem while saving your project! Please close and reopen all the tabs you are currently using before trying to save again.");
		}
	}
	
	/**
	 * @param project
	 */
	public void checkProject(Project project) {
		
		if(project == null) {
			
			throw new IllegalArgumentException("No Project Selected!");
		}
		else {
			
			this.project = project;
		}
	}
	
	public String validateName(String filename) {
		
		if (! filename.endsWith(".mer"))
			filename = filename + ".mer";
		
		return filename;
	}
}
