package proclipsing.core.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import proclipsing.core.preferences.ProjectPreferences;
import proclipsing.os.MacOSHelper;
import proclipsing.os.OS;

public class PathAndLibrariesSelectionDrawer {

    private static String IMPORT_LIBRARIES_LABEL            = "Select Libraries to Import";
    private static String PROCESSING_APP_PATH_LABEL         = "Processing Path";
    private static String PROCESSING_SKETCH_PATH_LABEL      = "Processing Sketch Path";
    private static String DIR_SEARCH_BUTTON_LABEL           = "Browse...";    
    private static int    PATH_TEXT_WIDTH_HINT              = 350;
    private static int    LABEL_WIDTH_HINT                  = 150;
    private static int	  VIEWER_HEIGHT_HINT				= 150;

    //private ProjectConfiguration project_configuration;
    private IValidateListener validate_listener;
    //private CheckboxTableViewer libraries_viewer; 
    private CheckboxTableViewer baselibs_viewer;
    private CheckboxTableViewer userlibs_viewer;
    private Text processing_app_path_text;
    private Text processing_sketch_path_text;
    
    public PathAndLibrariesSelectionDrawer(IValidateListener validateListener) {
        validate_listener = validateListener;
    }
    
    public void drawEverything(Composite parent, ProjectPreferences prefs) {
        Composite c1 = new Composite(parent, SWT.NONE);
        c1.setLayout(new GridLayout(3, false));
        drawProcessingAppFinder(c1, prefs.getAppPath(), prefs.getBaselibs());
        GridData gd3 = new GridData(SWT.FILL);
        c1.setLayoutData(gd3);
        drawBaseLibrarySelector(parent, prefs);
        
        Composite c2 = new Composite(parent, SWT.NONE);
        c2.setLayout(new GridLayout(3, false));
        drawSketchPathFinder(c2, prefs.getSketchPath(), prefs.getBaselibs());
        c2.setLayoutData(gd3);
        drawUserLibrarySelector(parent, prefs); 	
    }
    
    public void drawBaseLibrarySelector(Composite parent, ProjectPreferences prefs) {
        baselibs_viewer = drawLibrarySelector(parent, prefs, IMPORT_LIBRARIES_LABEL);
        baselibs_viewer.setCheckedElements(prefs.getBaselibs().toArray());
        showDiscoveredLibraries(processing_app_path_text, baselibs_viewer, true);
        setSelectedLibs(baselibs_viewer, prefs.getBaselibs());
    }   
    
    public void drawUserLibrarySelector(Composite parent, ProjectPreferences prefs) {
        userlibs_viewer = drawLibrarySelector(parent, prefs, IMPORT_LIBRARIES_LABEL);
        userlibs_viewer.setCheckedElements(prefs.getUserlibs().toArray());
        showDiscoveredLibraries(processing_sketch_path_text, userlibs_viewer, false);
        setSelectedLibs(userlibs_viewer, prefs.getUserlibs());
    }

    private CheckboxTableViewer drawLibrarySelector(Composite parent, 
                ProjectPreferences prefs, String label) {
        // group surrounds the box w/ a thin line
        Group projectsGroup = new Group(parent, SWT.NONE);
        projectsGroup.setText(label);
        GridData gdProjects = new GridData(GridData.FILL_BOTH);
        //gdProjects.horizontalSpan = 2;
        projectsGroup.setLayoutData(gdProjects);
        projectsGroup.setLayout(new GridLayout(1, false));
        
        // main table to hold the library entries
        Table librariesTable = new Table(projectsGroup ,SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
        librariesTable.setHeaderVisible(false);
        

        TableColumn col1 = new TableColumn(librariesTable, SWT.NONE);
        col1.setWidth(200);
        //col1.setText("Processing Library");

        TableLayout tableLayout = new TableLayout();
        librariesTable.setLayout(tableLayout);

        GridData viewerData = new GridData(GridData.FILL_BOTH);
        viewerData.horizontalSpan = 2;
        viewerData.heightHint = VIEWER_HEIGHT_HINT;

        // jface component to deal w/ data in table and checkboxes
        CheckboxTableViewer viewer = new CheckboxTableViewer(librariesTable);
        viewer.getControl().setLayoutData(viewerData);
        viewer.setContentProvider(new SelectedLibrariesContentProvider());
        viewer.setLabelProvider(new SelectedLibrariesLabelProvider());
        //showDiscoveredLibraries();
        viewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                validate_listener.validate();
                //saveConfiguration();
            }
        });
        return viewer;
    }
    
    public String getProcessingPath() {
        return processing_app_path_text.getText();
    }
    
    public String getSketchPath() {
        return processing_sketch_path_text.getText();
    }
    
    
    public boolean validatePathExists() {
        return processing_app_path_text != null 
                && processing_app_path_text.getText().length() > 0
                && new File(processing_app_path_text.getText()).exists();
    }
    
    public boolean validatePathIsProcessing() {
        // final check for core.jar
    	
    	if(OS.helper() instanceof MacOSHelper){
    		((MacOSHelper)OS.helper()).resetProcessingPath();
    	}
    	
    	
    	if(!new File(processing_app_path_text.getText(),
                OS.helper().getCorePath() + "core.jar").exists()){
    			OS.helper().tryProcessing2_0bpath();
    		if(!new File(processing_app_path_text.getText(),
                    OS.helper().getCorePath() + "core.jar").exists()){
        		OS.helper().tryProcessing2_0bpath();
        		
        	}
    	}
    	
        return new File(processing_app_path_text.getText(),
                OS.helper().getCorePath() + "core.jar").exists();
        
    }   
    
    private Text drawDirFinder(Composite composite, 
            String label, String path,
            Listener buttonListener,
            ModifyListener textModifyListener) {
        
        Label processingPathLabel = new Label(composite, SWT.NONE);
        processingPathLabel.setText(label);
        GridData gd1 = new GridData();
        gd1.widthHint = LABEL_WIDTH_HINT;
        processingPathLabel.setLayoutData(gd1);
        
        
        Text text = new Text(composite, SWT.NONE | SWT.BORDER );
        text.setText(path);
        text.addModifyListener(textModifyListener);          
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = PATH_TEXT_WIDTH_HINT;
        text.setLayoutData(gd);

        Button button = new Button(composite, SWT.PUSH);
        button.setText(DIR_SEARCH_BUTTON_LABEL);
        button.addListener(SWT.Selection, buttonListener);
        return text;
    }
    
    
    public void drawProcessingAppFinder(final Composite composite, 
            final String processingPath, final List<String> selectedLibs ) {

        Listener buttonListener = new Listener() {
            public void handleEvent(Event event) {
                Dialog dialog = OS.helper().getDialog(composite.getShell());
                
                if(dialog instanceof FileDialog)
                    processing_app_path_text.setText(((FileDialog)dialog).open());
                else if(dialog instanceof DirectoryDialog)
                    processing_app_path_text.setText(((DirectoryDialog)dialog).open());
                    
            }
        };
     
        ModifyListener textModifyListener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validate_listener.validate();
                showDiscoveredLibraries((Text)e.getSource(), baselibs_viewer, true);
                setSelectedLibs(baselibs_viewer, selectedLibs);
            }
        };
        
        processing_app_path_text = drawDirFinder(composite, 
        		PROCESSING_APP_PATH_LABEL, processingPath, buttonListener, textModifyListener);
    }

    
    public void drawSketchPathFinder(final Composite composite, 
            final String sketchPath, final List<String> selectedLibs ) {
        
        Listener buttonListener = new Listener() {
            public void handleEvent(Event event) {
                DirectoryDialog dialog = new DirectoryDialog(composite.getShell());
                processing_sketch_path_text.setText(((DirectoryDialog)dialog).open());
            }
        };
        
        ModifyListener textModifyListener = new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validate_listener.validate();
                showDiscoveredLibraries((Text)e.getSource(), userlibs_viewer, false);
                setSelectedLibs(userlibs_viewer, selectedLibs);
            }
        };        
        
        processing_sketch_path_text = drawDirFinder(composite, 
        		PROCESSING_SKETCH_PATH_LABEL, sketchPath, buttonListener, textModifyListener);
    }

    
    private void showDiscoveredLibraries(Text pathText, CheckboxTableViewer libViewer, boolean isCore) {
        if (pathText == null) return;
        
        System.out.println("OS.helper().getNewLibraryPath(): " + OS.helper().getNewLibraryPath());
        
        String test = pathText.getText();
        
        if(OS.helper() instanceof MacOSHelper){
        	((MacOSHelper)OS.helper()).resetProcessingPath();
        }
        
        File librariesDir = new File(pathText.getText(),
                OS.helper().getLibraryPath());
        
        if (!librariesDir.exists()) {
        	librariesDir = new File(pathText.getText(),
                OS.helper().getNewLibraryPath());
            if (!librariesDir.exists() && OS.helper() instanceof MacOSHelper) {
    			OS.helper().tryProcessing2_0bpath();
    			OS.helper().tryProcessing2_0bpath();
    			librariesDir = new File(pathText.getText(), OS.helper().getNewLibraryPath());
            }
        }
        
        if(!isCore){
        	librariesDir = new File(pathText.getText(),
                    OS.helper().getSketchPath());
        }
        
        List<String> libraries = new ArrayList<String>();
        if (librariesDir.exists()) { 
            String[] files = librariesDir.list();
            for (String file : files) {
                if ((new File(librariesDir, file)).isDirectory())
                    libraries.add(file);
            }
        }
        libViewer.setInput(libraries.toArray(new String[libraries.size()]));
    }
    
    public ArrayList<String> getSelectedBaseLibs() {
        ArrayList<String> libs = new ArrayList<String>();
        for (Object element : baselibs_viewer.getCheckedElements()) {
           libs.add((String) element);
        }
        return libs;
    }     
    
    public ArrayList<String> getSelectedUserLibs() {
        ArrayList<String> libs = new ArrayList<String>();
        for (Object element : userlibs_viewer.getCheckedElements()) {
           libs.add((String) element);
        }
        return libs;
    }    
    
    private void setSelectedLibs(CheckboxTableViewer libViewer, List<String> selectedLibs) {
        if (selectedLibs != null)
            libViewer.setCheckedElements(selectedLibs.toArray());
        libViewer.refresh();
    }
    
    /* INNER CLASSES */
    
    
    class SelectedLibrariesContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object input) {
            if (input instanceof String[]) {
                return (String[]) input;
            }
            return null;
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        public void dispose() {
        }
    }

    class SelectedLibrariesLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            String selectedLib = (String) element;
            if (columnIndex == 0) return selectedLib;
            else return "";
        }
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }
    }

    
}
