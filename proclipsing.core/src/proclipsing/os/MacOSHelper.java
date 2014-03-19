package proclipsing.os;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class MacOSHelper extends OSHelper {

	private static final int VERSION_PROCESSING_1_5_MINUS = 0;
	private static final int VERSION_PROCESSING_2_0_BETA = 1;
	private static final int VERSION_PROCESSING_2_1_PLUS = 2;
	
	private int processingVersion = VERSION_PROCESSING_1_5_MINUS;
	
	//private static boolean isProcessing2Path = false;
	private static final String DEFAULT_JAVA = "Contents/Resources/Java/";
	private static final String ALT_PATH_TO_JAVA = "core/library/";
	private static final String PROCESSING_2_1_PLUS_PATH = "Contents/Java/";
	

	private static String PATH_TO_JAVA = DEFAULT_JAVA;
	
	public void resetProcessingPath(){
		processingVersion = 0;
		getCorePath();
	}

	public void tryProcessing2_0bpath(){
		processingVersion++;
		getCorePath();
	}

	@Override
	public String getCorePath() {
		if(processingVersion == VERSION_PROCESSING_1_5_MINUS){
			PATH_TO_JAVA = DEFAULT_JAVA;
			return PATH_TO_JAVA;
		} else if (processingVersion == VERSION_PROCESSING_2_0_BETA){
	        return PATH_TO_JAVA + ALT_PATH_TO_JAVA;
		} else {
			PATH_TO_JAVA = PROCESSING_2_1_PLUS_PATH;
	        return PATH_TO_JAVA + ALT_PATH_TO_JAVA;
		}
		
	}

	@Override
	public String getPathToLibrary(String library) {
		return PATH_TO_JAVA + super.getPathToLibrary(library);
	}
	
	@Override
	public String getPathToP2Library(String library) {
		return PATH_TO_JAVA + super.getPathToP2Library(library);
	}

	@Override
	public String getLibraryPath() {
		return PATH_TO_JAVA + getFileSeparator() + super.getLibraryPath();
	}
	
	@Override
	public String getNewLibraryPath() {
		return PATH_TO_JAVA + getFileSeparator() + super.getNewLibraryPath();
	}

    public Dialog getDialog(Shell shell) {
        return new FileDialog(shell);
    }
    
    public String getDefaultAppPath(){
    	return "/Applications/Processing.app";
    }
    
    public String getDefaultSketchPath(){
    	return System.getProperty("user.home") + "/Documents/Processing/";
    }
    
    public boolean isExlcuded(String jarName){
    	boolean result = false;
    	
//    	if(jarName.toLowerCase().contains("linux") || 
//    			jarName.toLowerCase().contains("windows")){
//    		result = true;
//    	} 
    	
    	return result || super.isExlcuded(jarName);
    }

}
