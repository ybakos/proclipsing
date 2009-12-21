package proclipsing.core.preferences;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import proclipsing.core.Activator;
import proclipsing.core.createproject.ProjectConfiguration;
import proclipsing.os.OSHelperManager;
import proclipsing.util.LogHelper;

public class ProclipsingPreferencePage extends PreferencePage implements
        IWorkbenchPreferencePage {

    private static String PROCESSING_APP_PATH_LABEL         = "Processing Path";
    private static String PROCESSING_SKETCH_PATH_LABEL      = "Processing Sketch Path";
    private static String DIR_SEARCH_BUTTON_LABEL           = "Browse...";    
    private static int    PATH_TEXT_WIDTH_HINT              = 350;
    private static int    LABEL_WIDTH_HINT                  = 150;
    
    Preferences preferences;
    
    private Text processing_app_path_text;
    private Text processing_sketch_path_text;
    
    public ProclipsingPreferencePage() {}

    public ProclipsingPreferencePage(String title) {
        super(title);
    }

    public ProclipsingPreferencePage(String title, ImageDescriptor image) {
        super(title, image);
    }

    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, false));
        drawProcessingAppFinder(composite);
        drawProcessingSketchFinder(composite);
        prefill();
       
        return composite;
    }

    private void prefill() {
        processing_app_path_text.setText(
                preferences.get(ProjectConfiguration.PROCESSING_APP_PATH_KEY,
                        OSHelperManager.getHelper().getDefaultAppPath()));
       processing_sketch_path_text.setText(
                preferences.get(ProjectConfiguration.PROCESSING_SKETCH_PATH_KEY,
                        OSHelperManager.getHelper().getDefaultSketchPath())); 
    }

    public void drawProcessingAppFinder(final Composite composite) {

        Label processingPathLabel = new Label(composite, SWT.NONE);
        processingPathLabel.setText(PROCESSING_APP_PATH_LABEL);    

        GridData gd1 = new GridData();
        gd1.widthHint = LABEL_WIDTH_HINT;
        gd1.horizontalSpan = 2;
        gd1.horizontalAlignment = SWT.BEGINNING;
        processingPathLabel.setLayoutData(gd1);
        
        processing_app_path_text = new Text(composite, SWT.NONE | SWT.BORDER );
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = PATH_TEXT_WIDTH_HINT;
        processing_app_path_text.setLayoutData(gd);        
        
        Button button = new Button(composite, SWT.PUSH);
        button.setText(DIR_SEARCH_BUTTON_LABEL);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
            Dialog dialog = OSHelperManager.getHelper().getDialog(composite.getShell());
            
            if(dialog instanceof FileDialog)
                processing_app_path_text.setText(((FileDialog)dialog).open());
            else if(dialog instanceof DirectoryDialog)
                processing_app_path_text.setText(((DirectoryDialog)dialog).open());
                
            }
        });        
    }
    
    public void drawProcessingSketchFinder(final Composite composite) {
        
        Label processingPathLabel = new Label(composite, SWT.NONE);
        processingPathLabel.setText(PROCESSING_SKETCH_PATH_LABEL);
        GridData gd1 = new GridData();
        gd1.widthHint = LABEL_WIDTH_HINT;
        gd1.horizontalSpan = 2;
        gd1.horizontalAlignment = SWT.BEGINNING;
        processingPathLabel.setLayoutData(gd1);
        
        processing_sketch_path_text = new Text(composite, SWT.NONE | SWT.BORDER );
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = PATH_TEXT_WIDTH_HINT;
        processing_sketch_path_text.setLayoutData(gd);

        Button button = new Button(composite, SWT.PUSH);
        button.setText(DIR_SEARCH_BUTTON_LABEL);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                DirectoryDialog dialog = new DirectoryDialog(composite.getShell());
                processing_sketch_path_text.setText(((DirectoryDialog)dialog).open());
            }
        });
    }        
    
    public void init(IWorkbench workbench) {
        preferences = new ConfigurationScope().getNode(Activator.PLUGIN_ID);
    }

    protected void performDefaults() {       
        processing_app_path_text.setText(
                OSHelperManager.getHelper().getDefaultAppPath());
        processing_sketch_path_text.setText(
                OSHelperManager.getHelper().getDefaultSketchPath());

        super.performDefaults();
    }
    
    
    public boolean performOk() {
        // this'll never happen, but..
        if (preferences == null) return false;
        
        String path = processing_app_path_text.getText().trim();
        if (!path.endsWith(OSHelperManager.getHelper().getFileSeparator())) 
            path +=  OSHelperManager.getHelper().getFileSeparator();
        preferences.put(ProjectConfiguration.PROCESSING_APP_PATH_KEY, path);

        path = processing_sketch_path_text.getText().trim();
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
