package dch.eclipse.p5Export;

import java.io.PrintStream;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.ui.console.*;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/* TODO:
 * 
 * PreferenceController.loadFromProject:
   
   IEclipsePreferences preferences = 
      new ProjectScope(project).getNode(ProjectPreferences.PROJECT_PREFS_NODE);
      
 *   Default to current project, last run-config
 *   
 *   Setup an (error) log for all info/error msgs
 *   
 *   Add docs a la http://fjep.sourceforge.net/
 *  
 * @author dhowe
 */
public class P5ExportPlugin extends AbstractUIPlugin
{
  public static final String ID = "dch.eclipse.EclipseP5Exporter";
  public static final String NAME = "EclipseP5Exporter";
  public static final String VERSION = "0.2.6";
  public static ImageDescriptor LARGE_ICON;
  
  private static MessageConsoleStream stream; 
  private static MessageConsole console;
  
  // The shared instance.
  static P5ExportPlugin plugin;

  // Resource bundle.
  private ResourceBundle resourceBundle;

  /**
   * The constructor.
   */
  public P5ExportPlugin() {
    plugin = this;
    LARGE_ICON = getImageDescriptor("icons/proc48.png");
  }

  /**
   * This method is called upon plug-in activation
   */
  public void start(BundleContext context) throws Exception
  {
    super.start(context);
    System.out.println("[INFO] Loading "+NAME+" v"+VERSION);
  }

  /**
   * This method is called when the plug-in is stopped
   */
  public void stop(BundleContext context) throws Exception
  {
    super.stop(context);
    plugin = null;
  }

  /**
   * Returns the shared instance.
   */
  public static P5ExportPlugin getPlugin()
  {
    return plugin;
  }

  /**
   * Returns an image descriptor for the image file at the given plug-in
   * relative path.
   * 
   * @param path
   *          the path
   * @return the image descriptor
   */
  public static ImageDescriptor getImageDescriptor(String path)
  {
    return AbstractUIPlugin.imageDescriptorFromPlugin(ID, path);
  }
  
  public void log(String msg) 
  {
    log(msg, null);
  }
  
  public void log(String msg, Exception e) 
  {
    getLog().log(new Status(Status.INFO, ID, Status.OK, msg, e));
  }
   
  
  private static void initConsoleStream()
  {
    if (console == null) {
      console = new MessageConsole(P5ExportPlugin.NAME+" Console", LARGE_ICON);
      console.activate();
      ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{ console });
    }
    stream = console.newMessageStream();
    stream.setActivateOnWrite(true);    
  }

  public static void toConsole(String msg)
  {
    toConsole(msg, null);
  }
  
  public static void toConsole(Throwable e)
  {
    toConsole(null, e);
  }
  
  public static void toConsole(String msg, Throwable e)
  {
    if (stream == null || stream.isClosed())
      initConsoleStream();
    
    if (stream != null && !stream.isClosed()) {
      if (msg != null) stream.println(msg); 
      if (e != null)
        e.printStackTrace(new PrintStream(stream));
    }
  }
  
  /**
   * Returns the string from the plugin's resource bundle, or 'key' if not
   * found.
   */
  public static String getResourceString(String key)
  {
    ResourceBundle bundle = P5ExportPlugin.getPlugin().getResourceBundle();
    try
    {
      return (bundle != null) ? bundle.getString(key) : key;
    } 
    catch (MissingResourceException e)
    {
      return key;
    }
  }

  /**
   * Returns the plugin's resource bundle,
   */
  public ResourceBundle getResourceBundle()
  {
    return resourceBundle;
  }
}
