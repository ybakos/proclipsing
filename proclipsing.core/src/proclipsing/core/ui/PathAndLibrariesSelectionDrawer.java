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

import proclipsing.os.OSHelperManager;

public class PathAndLibrariesSelectionDrawer {

    private static String IMPORT_LIBRARIES_LABEL            = "Select Libraries to Import";
    private static String PROCESSING_APP_PATH_LABEL         = "Processing Path";
    private static String PROCESSING_SKETCH_PATH_LABEL      = "Processing Sketch Path";
    private static String DIR_SEARCH_BUTTON_LABEL           = "Browse...";    
    private static int    PATH_TEXT_WIDTH_HINT              = 350;
    private static int    LABEL_WIDTH_HINT                  = 150;

    //private ProjectConfiguration project_configuration;
    private IValidateListener validate_listener;
    private CheckboxTableViewer libraries_viewer; 
    private Text processing_app_path_text;
    private Text processing_sketch_path_text;
    
    public PathAndLibrariesSelectionDrawer(IValidateListener validateListener) {
        validate_listener = validateListener;
    }
 

    public void drawPaths(Composite parent, final String processingPath, 
            final String sketchPath, final List<String> selectedLibs) {
        
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));
        
        drawProcessingAppFinder(composite, processingPath, selectedLibs);
        drawSketchPathFinder(composite, sketchPath, selectedLibs);
        
        GridData gd = new GridData(SWT.FILL);
        composite.setLayoutData(gd);
    } 
    
    public void drawLibrarySelector(Composite parent, ArrayList<String> selectedLibs) {
        // group surrounds the box w/ a thin line
        Group projectsGroup = new Group(parent, SWT.NONE);
        projectsGroup.setText(IMPORT_LIBRARIES_LABEL);
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
        viewerData.heightHint = 200;

        // jface component to deal w/ data in table and checkboxes
        libraries_viewer = new CheckboxTableViewer(librariesTable);
        libraries_viewer.getControl().setLayoutData(viewerData);
        libraries_viewer.setContentProvider(new SelectedLibrariesContentProvider());
        libraries_viewer.setLabelProvider(new SelectedLibrariesLabelProvider());
        showDiscoveredLibraries();
        libraries_viewer.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                validate_listener.validate();
                //saveConfiguration();
            }
        });
        libraries_viewer.setCheckedElements(selectedLibs.toArray());
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
        return new File(processing_app_path_text.getText(),
                OSHelperManager.getHelper().getCorePath() + "core.jar").exists();
        
    }   
    
    private Text drawDirFinder(final Composite composite, 
            final String label, String path, final List<String> selectedLibs, final Listener buttonListener) {
        
        Label processingPathLabel = new Label(composite, SWT.NONE);
        processingPathLabel.setText(label);
        GridData gd1 = new GridData();
        gd1.widthHint = LABEL_WIDTH_HINT;
        processingPathLabel.setLayoutData(gd1);
        
        
        Text text = new Text(composite, SWT.NONE | SWT.BORDER );
        text.setText(path);
        text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validate_listener.validate();
                showDiscoveredLibraries();
                setSelectedLibraries(selectedLibs);     
            }
        });           
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = PATH_TEXT_WIDTH_HINT;
        text.setLayoutData(gd);

        Button button = new Button(composite, SWT.PUSH);
        button.setText(DIR_SEARCH_BUTTON_LABEL);
        button.addListener(SWT.Selection, buttonListener);
        return text;
    }
    
    
    private void drawProcessingAppFinder(final Composite composite, 
            final String processingPath, final List<String> selectedLibs ) {

        Listener buttonListener = new Listener() {
            public void handleEvent(Event event) {
                Dialog dialog = OSHelperManager.getHelper().getDialog(composite.getShell());
                
                if(dialog instanceof FileDialog)
                    processing_app_path_text.setText(((FileDialog)dialog).open());
                else if(dialog instanceof DirectoryDialog)
                    processing_app_path_text.setText(((DirectoryDialog)dialog).open());
                    
            }
        };
     
        
        processing_app_path_text = drawDirFinder(composite, PROCESSING_APP_PATH_LABEL, 
                processingPath, selectedLibs, buttonListener);
    }

    
    private void drawSketchPathFinder(final Composite composite, 
            final String sketchPath, final List<String> selectedLibs ) {

        Listener buttonListener = new Listener() {
            public void handleEvent(Event event) {
                DirectoryDialog dialog = new DirectoryDialog(composite.getShell());
                processing_sketch_path_text.setText(((DirectoryDialog)dialog).open());
            }
        };
        
        processing_sketch_path_text = drawDirFinder(composite, PROCESSING_SKETCH_PATH_LABEL, 
                sketchPath, selectedLibs, buttonListener);
    }

    
    private void showDiscoveredLibraries() {
        
        if (processing_app_path_text == null) return;
        File librariesDir = new File(processing_app_path_text.getText(),
                OSHelperManager.getHelper().getLibraryPath());
        List<String> libraries = new ArrayList<String>();
        if (librariesDir.exists()) { 
            String[] files = librariesDir.list();
            for (String file : files) {
                if ((new File(librariesDir, file)).isDirectory())
                    libraries.add(file);
            }
        }
        
        if (processing_sketch_path_text == null) return;
        librariesDir = new File(processing_sketch_path_text.getText());
        if (librariesDir.exists()) { 
            String[] files = librariesDir.list();
            for (String file : files) {
                if ((new File(librariesDir, file)).isDirectory()){
                    libraries.add(file);
                }
            }
        }        
        setLibrariesViewerInput(
                libraries.toArray(new String[libraries.size()]));
    }    
 
    private void setLibrariesViewerInput(String[] allLibraryIdentifiers) {
        libraries_viewer.setInput(allLibraryIdentifiers);
    }
    
    public ArrayList<String> getSelectedLibraries() {
        ArrayList<String> libs = new ArrayList<String>();
        for (Object element : libraries_viewer.getCheckedElements()) {
           libs.add((String) element);
        }
        return libs;
    }    
    
    private void setSelectedLibraries(List<String> selectedLibs) {
        if (selectedLibs != null)
            libraries_viewer.setCheckedElements(selectedLibs.toArray());
        libraries_viewer.refresh();
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
