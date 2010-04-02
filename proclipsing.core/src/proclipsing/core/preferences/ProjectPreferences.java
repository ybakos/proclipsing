package proclipsing.core.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.ConfigurationScope;

import proclipsing.core.Activator;
import proclipsing.os.OS;

public class ProjectPreferences {
	
    public static final String PROJECT_PREFS_NODE           = "PROJECT_PREFS_NODE";
    public static final String APP_PATH_KEY      			= "PROCESSING_APP_PATH";
    public static final String SKETCH_PATH_KEY   			= "PROCESSING_SKETCH_PATH";
    public static final String BIN_DIR                  	= "bin";
    public static final String SRC_DIR                  	= "src";
    public static final String DATA_DIR                  	= "data";
    public static final String BASELIB_DIR                  = "lib/base";  
    public static final String USERLIB_DIR                  = "lib/user";                 
	
    //private List<String> libraries;
    private List<String> baselibs;
    private List<String> userlibs;
    private String name;
    private String app_path;
    private String sketch_path;

    
    public ProjectPreferences() {
    	this(null, getDefaultAppPath(), getDefaultSketchPath(), 
    	        new ArrayList<String>(), new ArrayList<String>());
    }
    
    public ProjectPreferences(ProjectPreferences prefs) {
    	this(prefs.getName(), prefs.getAppPath(), prefs.getSketchPath(), 
    	        new ArrayList<String>(prefs.getBaselibs()),  
    	        new ArrayList<String>(prefs.getUserlibs()));
    }
    
    public ProjectPreferences(String name, 
    		String appPath, String sketchPath, 
    		List<String> baselibs, List<String> userlibs) {
    	this.name = name;
    	this.app_path = appPath;
    	this.sketch_path = sketchPath;
    	this.baselibs = baselibs;
    	this.userlibs = userlibs;
    }

    public List<String> getBaselibs() {
        return baselibs;
    }    
    
    public List<String> getUserlibs() {
    	return userlibs;
    }
    
    public String getName() {
    	return name;
    }
    
    public String getSketchPath() {
    	return sketch_path;
    }
    
    public String getAppPath() {
    	return app_path;
    }
    
	private static String getDefaultAppPath() {
        return  new ConfigurationScope().getNode(Activator.PLUGIN_ID).get(
                APP_PATH_KEY,  OS.helper().getDefaultAppPath());
	}
	
	private static String getDefaultSketchPath() {
        return  new ConfigurationScope().getNode(Activator.PLUGIN_ID).get(
                SKETCH_PATH_KEY,  OS.helper().getDefaultSketchPath());

	}
}
