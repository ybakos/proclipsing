package proclipsing.core.createproject;

import java.util.ArrayList;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import proclipsing.core.Activator;
import proclipsing.os.OSHelperManager;
import proclipsing.util.LogHelper;

/**
 * Simple class to keep track of the project configuration
 * as you go through the wizard
 * 
 * @author brian
 *
 */
public class ProjectConfiguration {

    public static final String PROJECT_PREFS_NODE           = "PROJECT_PREFS_NODE";
    public static final String PROCESSING_APP_PATH_KEY      = "PROCESSING_APP_PATH";
    public static final String PROCESSING_SKETCH_PATH_KEY   = "PROCESSING_SKETCH_PATH";
    
    private ArrayList<String> selected_libraries;
    private String project_name;
    private String app_path     = null;
    private String sketch_path  = null;
    
    private boolean isApp = false;
    private IProject project;
    
    public ProjectConfiguration() {
    }
    
    public ProjectConfiguration(IProject project) {
        this.project = project;
    }
    
    public ProjectConfiguration(
            String projectName, ArrayList<String> selectedLibraries) {
        project_name = projectName;
        selected_libraries = selectedLibraries;
    }

    public ArrayList<String> getSelectedLibraries() {
        return selected_libraries;
    }
    
    public void setSelectedLibraries(
            ArrayList<String> selectedLibraries) {
        selected_libraries = selectedLibraries;
    }

    public void setProjectName(String projectName) {
    	project_name = projectName;
    }
    
    public String getProjectName() {
        return project_name;
    }
    
    public String getProcessingAppPath() {
        if (app_path != null)
            return app_path;
        else if (projectExists())
            return getProjectPreferencesNode().get(
                    PROCESSING_APP_PATH_KEY, getDefaultProcessingAppPath());
        else
            return getDefaultProcessingAppPath();
    }
    
    public void setProcessingAppPath(String path) {
        path = path.trim();
        if (!path.endsWith(OSHelperManager.getHelper().getFileSeparator())) 
        	path +=  OSHelperManager.getHelper().getFileSeparator();
        app_path = path;
    }
     
    private void saveProcessingAppPath() throws IllegalStateException {
        if (project == null) 
            throw new IllegalStateException("Cannot save processing path " +
            		"because target project has not been set");
        
        Preferences preferences = getProjectPreferencesNode();
        preferences.put(PROCESSING_APP_PATH_KEY, app_path);
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
           LogHelper.LogError(e);
        }
    }
    
    public String getProcessingSketchPath() {
        if (sketch_path != null)
            return sketch_path;
        else if (projectExists())
            return getProjectPreferencesNode().get(
                    PROCESSING_SKETCH_PATH_KEY, getDefaultProcessingSketchPath());
        else
            return getDefaultProcessingSketchPath();
    }
    
    public void setProcessingSketchPath(String path) {
        path = path.trim();
        if (!path.endsWith(OSHelperManager.getHelper().getFileSeparator())) 
        	path +=  OSHelperManager.getHelper().getFileSeparator();
        sketch_path = path;
    }
    
    private void saveProcessingSketchPath() {
        if (project == null) 
            throw new IllegalStateException("Cannot save sketch path " +
                    "because target project has not been set");
        
        Preferences preferences = getProjectPreferencesNode();
        preferences.put(PROCESSING_SKETCH_PATH_KEY, sketch_path);
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
           LogHelper.LogError(e);
        }
    }
    
    public void savePreferences(IProject project) {
        this.project = project;
        saveProcessingAppPath();
        saveProcessingSketchPath();
    }
    
    public boolean isApp() {
		return isApp;
	}

	public void setApp(boolean isApp) {
		this.isApp = isApp;
	}
	
	public boolean projectExists() {
	    return project != null;
	}
	
	/**
	 *  this gets the configuration in this project scope for the project's prefs
	 *  
	 * @return
	 */
	private IEclipsePreferences getProjectPreferencesNode() {
	    return new ProjectScope(project).getNode(PROJECT_PREFS_NODE);
	}
	
	private String getDefaultProcessingAppPath() {
        return  new ConfigurationScope().getNode(Activator.PLUGIN_ID).get(
                PROCESSING_APP_PATH_KEY,  OSHelperManager.getHelper().getDefaultAppPath());
	}
	
	private String getDefaultProcessingSketchPath() {
        return  new ConfigurationScope().getNode(Activator.PLUGIN_ID).get(
                PROCESSING_SKETCH_PATH_KEY,  OSHelperManager.getHelper().getDefaultSketchPath());

	}
}
