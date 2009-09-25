package proclipsing.os;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class MacOSHelper extends OSHelper {
	
	private static final String PATH_TO_JAVA = "Contents/Resources/Java/";
	
	@Override
	public String getCorePath() {
        return PATH_TO_JAVA;
	}

	@Override
	public String getPathToLibrary(String library) {
		return PATH_TO_JAVA + super.getPathToLibrary(library);
	}

	@Override
	public String getLibraryPath() {
		return PATH_TO_JAVA + super.getLibraryPath();
	}

    public Dialog getDialog(Shell shell) {
        return new FileDialog(shell);
    }

}
