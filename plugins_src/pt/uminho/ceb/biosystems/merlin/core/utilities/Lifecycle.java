/**
 * 
 */
package pt.uminho.ceb.biosystems.merlin.core.utilities;

import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.WindowConstants;
import javax.swing.plaf.ColorUIResource;

import org.platonos.pluginengine.PluginLifecycle;

import es.uvigo.ei.aibench.core.Core;
import es.uvigo.ei.aibench.core.operation.OperationDefinition;
import es.uvigo.ei.aibench.workbench.Workbench;
import pt.uminho.ceb.biosystems.merlin.database.connector.datatypes.DatabaseUtilities;

/**
 * @author paulo maia, 09/05/2007
 *
 */
public class Lifecycle extends PluginLifecycle {
	@Override
	public void start(){

		try {
			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			String theme = "Large-Font";
			com.jtattoo.plaf.mint.MintLookAndFeel.setTheme(theme);

			String laf = "com.jtattoo.plaf.mint.MintLookAndFeel";
			//String laf = "com.jtattoo.plaf.fast.FastLookAndFeel";
			
			String os_name = System.getProperty("os.name");

			if(os_name.contains("Windows"))
				UIManager.setLookAndFeel(laf);
			
			DatabaseUtilities.h2CleanDatabaseFiles();
			
		} 
		catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {

			e1.printStackTrace();
		} 
		
//		UIManager.getLookAndFeelDefaults().put("ButtonUI", "com.jtattoo.plaf.fast.FastButtonUI");
		
//	    UIManager.LookAndFeelInfo looks[] = UIManager.getInstalledLookAndFeels();
//
//	    for (UIManager.LookAndFeelInfo info : looks) {
//
//	      UIDefaults defaults = UIManager.getDefaults();
//	      Enumeration newKeys = defaults.keys();
//
//	      while (newKeys.hasMoreElements()) {
//	        Object obj = newKeys.nextElement();
//	        System.out.printf("%50s : %s\n", obj, UIManager.get(obj));
//	      }
//	     }
		
		UIManager.getLookAndFeelDefaults().put("Table:\"Table.cellRenderer\".background",new ColorUIResource(Color.WHITE));
		UIManager.getLookAndFeelDefaults().put("Table:\"Table.showGrid", false);
		
		UIManager.put("Table.alternateRowColor", new Color (242, 242, 242));
		
		UIManager.put("ComboBox.background", new Color (242, 242, 242));
		UIManager.put("ComboBox.selectionBackground", new ColorUIResource(Color.WHITE));
		
		UIManager.getDefaults().put("TableHeader.cellBorder" , BorderFactory.createEmptyBorder(0,0,0,0));
		UIManager.getDefaults().put("Table.showGrid", false);
		
		Workbench.getInstance().getMainFrame().setIconImage((new ImageIcon(getClass().getClassLoader().getResource("icons/merlin.png"))).getImage());
		
		Workbench.getInstance().getMainFrame().addWindowListener(new WindowListener(){

			public void windowActivated(WindowEvent e) {

			}

			public void windowClosed(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			public void windowClosing(WindowEvent e) {
				
				Workbench.getInstance().getMainFrame().setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
				//find quit
				for (OperationDefinition<?> def : Core.getInstance().getOperations()){

					if (def.getID().equals("operations.QuitOperation.ID")) {

						Workbench.getInstance().executeOperation(def);

						return;
					}
				}

			}

			public void windowDeactivated(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			public void windowDeiconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			public void windowIconified(WindowEvent e) {
				// TODO Auto-generated method stub

			}

			public void windowOpened(WindowEvent e) {
				// TODO Auto-generated method stub

			}

		});
	}
}
