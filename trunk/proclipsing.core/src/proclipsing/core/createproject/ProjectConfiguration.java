package proclipsing.core.createproject;

import java.util.ArrayList;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
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

    private static final String PROCESSING_APP_PATH_KEY = "PROCESSING_APP_PATH";
    private static final String PROCESSING_SKETCH_PATH_KEY = "PROCESSING_SKETCH_PATH";
    private ArrayList<String> selected_libraries;
    private String project_name;
    private boolean isApp = false;
    
    public ProjectConfiguration() {}
    
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
        Preferences preferences = new ConfigurationScope().getNode(Activator.PLUGIN_ID);
        return preferences.get(PROCESSING_APP_PATH_KEY, getDefaultProcessingAppPath());
    }
    
    public void setProcessingAppPath(String path) {
        path = path.trim();
        if (!path.endsWith(OSHelperManager.getHelper().getFileSeparator())) 
        	path +=  OSHelperManager.getHelper().getFileSeparator();
        Preferences preferences = new ConfigurationScope().getNode(Activator.PLUGIN_ID);

        preferences.put(PROCESSING_APP_PATH_KEY, path);

        try {
            preferences.flush();
        } catch (BackingStoreException e) {
           LogHelper.LogError(e);
        }
    }
    
    public String getProcessingSketchPath() {
        Preferences preferences = new ConfigurationScope().getNode(Activator.PLUGIN_ID);
        return preferences.get(PROCESSING_SKETCH_PATH_KEY, getDefaultProcessingSketchPath());
    }
    
    public void setProcessingSketchPath(String path) {
        path = path.trim();
        if (!path.endsWith(OSHelperManager.getHelper().getFileSeparator())) 
        	path +=  OSHelperManager.getHelper().getFileSeparator();
        Preferences preferences = new ConfigurationScope().getNode(Activator.PLUGIN_ID);

        preferences.put(PROCESSING_SKETCH_PATH_KEY, path);

        try {
            preferences.flush();
        } catch (BackingStoreException e) {
           LogHelper.LogError(e);
        }
    }
    
    public boolean isApp() {
		return isApp;
	}

	public void setApp(boolean isApp) {
		this.isApp = isApp;
	}
	
	private String getDefaultProcessingAppPath() {
		return OSHelperManager.getHelper().getDefaultAppPath();
	}
	
	private String getDefaultProcessingSketchPath() {
		return OSHelperManager.getHelper().getDefaultSketchPath();
	}
}
