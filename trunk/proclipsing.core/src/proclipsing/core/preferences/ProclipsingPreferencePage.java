package proclipsing.core.preferences;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import proclipsing.core.Activator;
import proclipsing.core.createproject.ProjectConfiguration;
import proclipsing.core.ui.ProcessingPathDrawer;
import proclipsing.os.OSHelperManager;
import proclipsing.util.LogHelper;

public class ProclipsingPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    Preferences preferences;
    
    private ProcessingPathDrawer path_drawer = null;
    
    public ProclipsingPreferencePage() {}

    public ProclipsingPreferencePage(String title) {
        super(title);
    }

    public ProclipsingPreferencePage(String title, ImageDescriptor image) {
        super(title, image);
    }

    protected Control createContents(Composite parent) {
        path_drawer = new ProcessingPathDrawer();
        Composite composite = path_drawer.draw(parent);
        path_drawer.getProcessingPathTextWidget().setText(
                preferences.get(ProjectConfiguration.PROCESSING_APP_PATH_KEY,
                        OSHelperManager.getHelper().getDefaultAppPath()));
        path_drawer.getSketchPathTextWidget().setText(
                preferences.get(ProjectConfiguration.PROCESSING_SKETCH_PATH_KEY,
                        OSHelperManager.getHelper().getDefaultSketchPath()));       
        composite.setLayoutData(new GridData(SWT.FILL));
        return composite;
    }

    public void init(IWorkbench workbench) {
        preferences = new ConfigurationScope().getNode(Activator.PLUGIN_ID);
    }

    protected void performDefaults() {
        if (path_drawer != null) {
            path_drawer.getProcessingPathTextWidget().setText(
                    OSHelperManager.getHelper().getDefaultAppPath());
            path_drawer.getSketchPathTextWidget().setText(
                    OSHelperManager.getHelper().getDefaultSketchPath());
        }
        
        super.performDefaults();
    }
    
    
    public boolean performOk() {
        // this'll never happen, but..
        if (path_drawer == null || preferences == null) return false;
        
        String path = path_drawer.getProcessingPath().trim();
        if (!path.endsWith(OSHelperManager.getHelper().getFileSeparator())) 
            path +=  OSHelperManager.getHelper().getFileSeparator();
        preferences.put(ProjectConfiguration.PROCESSING_APP_PATH_KEY, path);

        path = path_drawer.getSketchPath().trim();
        if (!path.endsWith(OSHelperManager.getHelper().getFileSeparator())) 
            path +=  OSHelperManager.getHelper().getFileSeparator();
        preferences.put(ProjectConfiguration.PROCESSING_SKETCH_PATH_KEY, path);
        
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
           LogHelper.LogError(e);
           return false;
        }
 
        return true;
    }
    
}
