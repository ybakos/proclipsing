package proclipsing.os;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

public abstract class OSHelper {
    private final String CORE_PATH 		    = "lib" + getFileSeparator();
    protected final String LIBRARY_PATH     = "libraries" + getFileSeparator();
    protected final String LIB_MATCH_STRING	= "%LIBRARY_IDENTIFIER%";
    protected final String END_PATH_TO_LIB  =  LIBRARY_PATH + LIB_MATCH_STRING + getFileSeparator() + "library" + getFileSeparator();
	
	public String getFileSeparator() {
	    String separator =  System.getProperty("file.separator");
	    // extra special precautionary tactics
	    if (separator == null || separator.length() == 0) separator = "/";
	    return separator;
	}
	
	public String getPathToLibrary(String library) {
		return END_PATH_TO_LIB.replaceAll(LIB_MATCH_STRING, library);
	}

	public String getCorePath() {
		return CORE_PATH;
	}

    public String getLibraryPath() {
        return LIBRARY_PATH;
    }

    public Dialog getDialog(Shell shell) {
        return new DirectoryDialog(shell);
    }
	
}
