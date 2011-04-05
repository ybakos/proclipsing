package proclipsing.os;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

public abstract class OSHelper {
    private final String CORE_PATH 		    = "lib" + getFileSeparator();
    protected final String LIBRARY_PATH     = "libraries" + getFileSeparator();
    protected final String NEW_LIBRARY_PATH = "modes" + getFileSeparator() + "java" + getFileSeparator() + LIBRARY_PATH;
    protected final String LIB_MATCH_STRING	= "%LIBRARY_IDENTIFIER%";
    protected final String END_PATH_TO_LIB  =  LIBRARY_PATH + LIB_MATCH_STRING + getFileSeparator() + "library" + getFileSeparator();
    protected final String NEW_END_PATH_TO_LIB  = NEW_LIBRARY_PATH + LIB_MATCH_STRING + getFileSeparator() + "library" + getFileSeparator();
    
    protected static String[] ignoreJars = {"antlr.jar","ecj.jar","jna.jar", "pde.jar", "quaqua.jar"};
	
	public String getFileSeparator() {
	    String separator =  System.getProperty("file.separator");
	    // extra special precautionary tactics
	    if (separator == null || separator.length() == 0) separator = "/";
	    return separator;
	}
	
	public String getPathToLibrary(String library) {
		return END_PATH_TO_LIB.replaceAll(LIB_MATCH_STRING, library);
	}
	
	public String getNewPathToLibrary(String library) {
		return NEW_END_PATH_TO_LIB.replaceAll(LIB_MATCH_STRING, library);
	}
	
	public String getSketchPathToLibrary(String library) {
		return END_PATH_TO_LIB.replaceAll(LIB_MATCH_STRING, library);
	}

	public String getCorePath() {
		return CORE_PATH;
	}

    public String getLibraryPath() {
        return LIBRARY_PATH;
    }
    
    public String getNewLibraryPath() {
        return NEW_LIBRARY_PATH;
    }

    public String getSketchPath() {
        return LIBRARY_PATH;
    }

    public Dialog getDialog(Shell shell) {
    	System.out.println("OS FILE DIALOG");
//        return new FileDialog(shell);
        return new DirectoryDialog(shell);
    }
    
    public String getDefaultAppPath(){
    	return System.getProperty("user.home") + "/processing-1.0.5/";
    }
    
    public String getDefaultSketchPath(){
    	return System.getProperty("user.home") + "/processing-1.0.5/";
    }
    
    public boolean isExlcuded(String jarName){
    	boolean result = false;
    	
    	for(int i = 0; i < ignoreJars.length; i++){
    		if(ignoreJars[i].equals(jarName)){
    			result = true;
    			break;
    		}
    	}
    	
    	return result;
    }
	
}
