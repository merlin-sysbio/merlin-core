/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.utilities;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.clipboard.ClipboardItem;
import es.uvigo.ei.aibench.workbench.MainWindow;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.Project;
import pt.uminho.ceb.biosystems.merlin.core.datatypes.metabolic_regulatory.Entity;
import pt.uminho.ceb.biosystems.merlin.core.views.UpdatablePanel;

/**
 * @author ODias
 *
 */
public class AIBenchUtils {

	
	/**
	 * Get project object from name
	 * 
	 * @param projectName
	 * @return
	 */
	public static Project getProject(String projectName){
		
		
		
		List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);

		for(int i=0; i<cl.size(); i++) {

			Project project = (Project) cl.get(i).getUserData();

			if(project.getName().equals(projectName))
				return project;

		}
		return null;
	}
	
	/**
	 * Get project object from name
	 * 
	 * @param projectName
	 * @return
	 */
	public static List<Project> getAllProjects(){
		
		List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);
		
		List<Project> all = new ArrayList<>();

		for(int i=0; i<cl.size(); i++) 
			all.add((Project) cl.get(i).getUserData());
			
		return all;
	}
	
	/**
	 * retrieves all open projects(workspaces) in clipboard
	 * 
	 * @return 
	 */
	public static List<Project> getAllClipboardProjects(){
		
		List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);
		List<Project> projects = new ArrayList<>();

		for(int i=0; i<cl.size(); i++) {
						
			Entity entity = (Entity) cl.get(i).getUserData();
			projects.add(entity.getProject());
		}
		
		return projects;
		
	}
	
	
	/**
	 * @return
	 */
	public static List<String> getProjectNames() {

		List <String> projectNames = new ArrayList<String>();

		List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(Project.class);

		for(int i=0; i<cl.size(); i++) {

			ClipboardItem item = cl.get(i);
			projectNames.add(item.getName());
		}

		return projectNames;
	}


	/**
	 * @param projectName
	 * @param datatype
	 */
	public static void updateView(String projectName, Class<?> datatype) {

		ClipboardItem item = null;

		List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(datatype);

		for(int i=0; i<cl.size(); i++) {

			if(datatype.equals(cl.get(i).getUserData().getClass())) {

				if( cl.get(i).getName().equals(projectName)) {

					item = cl.get(i);
				}
				else {

					if(cl.get(i).getUserData().getClass().getSuperclass().equals(Entity.class) ||
							cl.get(i).getUserData().getClass().equals(Entity.class)) {

						Entity entity = (Entity) cl.get(i).getUserData();

						if(entity.getProject().getName().equals(projectName))
							item = cl.get(i);
					}
				}
			}
			else {

				Entity entity = (Entity) cl.get(i).getUserData();

				if(entity.getProject().getName().equals(projectName))
					item = cl.get(i);
			}
		}

		if(item!=null) {

			MainWindow window = (MainWindow) Workbench.getInstance().getMainFrame();

			List<Component> list = window.getDataViews(item);

			for(Component component : list) {

				if(component instanceof UpdatablePanel){

					UpdatablePanel view = (UpdatablePanel) component;

					if(view.getProjectName().equals(projectName))
						view.updateTableUI();
				}
			}
		}
	}

	/**
	 * Get entity for project
	 * 
	 * @param projectName
	 * @param datatype
	 * @return
	 */
	public static Entity getEntity(String projectName, Class<?> datatype){
		
		List<ClipboardItem> cl = Core.getInstance().getClipboard().getItemsByClass(datatype);

		for(int i=0; i<cl.size(); i++) {

			Entity entity = (Entity) cl.get(i).getUserData();
			
			if(datatype.equals(entity.getClass())) {
				
					if(entity.getProject().getName().equals(projectName))
						return entity;
		}

	}
		return null;
	}

}
