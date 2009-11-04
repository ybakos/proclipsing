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

import proclipsing.core.createproject.ProjectConfiguration;
import proclipsing.os.OSHelperManager;

public class PathAndLibrariesSelectionDrawer {


	private static String PROCESSING_APP_PATH_LABEL     = "Processing Path";
    private static String PROCESSING_SKETCH_PATH_LABEL     = "Processing Sketch Path";
    private static String DIR_SEARCH_BUTTON_LABEL   = "Browse...";    
    private static String IMPORT_LIBRARIES_LABEL    = "Select Libraries to Import";
    private static int    PATH_TEXT_WIDTH_HINT      = 350;    
    
    private Text processing_app_path_text;
    private Text processing_sketch_path_text;
    private ProjectConfiguration project_configuration;
    private IValidateListener validate_listener;
    private CheckboxTableViewer libraries_viewer;    
    
    
    public PathAndLibrariesSelectionDrawer(
            ProjectConfiguration projectConfiguration, IValidateListener validateListener) {
        project_configuration = projectConfiguration;
        validate_listener = validateListener;
    }
    
    public void drawProcessingAppFinder(Composite parent) {
        
        Label processingPathLabel = new Label(parent, SWT.NONE);
        processingPathLabel.setText(PROCESSING_APP_PATH_LABEL);
        
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setLayout(layout);
        
        processing_app_path_text = new Text(composite, SWT.NONE | SWT.BORDER );
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = PATH_TEXT_WIDTH_HINT;
        processing_app_path_text.setLayoutData(gd);
        processing_app_path_text.setText(project_configuration.getProcessingAppPath());
        processing_app_path_text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
            	validate_listener.validate();
                showDiscoveredLibraries();
                setSelectedLibraries();     
            }
        });
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
    
	public void drawProcessingSketchFinder(Composite parent) {
        
        Label processingPathLabel = new Label(parent, SWT.NONE);
        processingPathLabel.setText(PROCESSING_SKETCH_PATH_LABEL);
        
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setLayout(layout);
        
        processing_sketch_path_text = new Text(composite, SWT.NONE | SWT.BORDER );
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = PATH_TEXT_WIDTH_HINT;
        processing_sketch_path_text.setLayoutData(gd);
        processing_sketch_path_text.setText(project_configuration.getProcessingSketchPath());
        processing_sketch_path_text.addModifyListener(new ModifyListener() {
		    public void modifyText(ModifyEvent e) {
		    	validate_listener.validate();
		        showDiscoveredLibraries();
		        setSelectedLibraries();
            }
		});
        Button button = new Button(composite, SWT.PUSH);
        button.setText(DIR_SEARCH_BUTTON_LABEL);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
            	DirectoryDialog dialog = new DirectoryDialog(composite.getShell());
            	
            	processing_sketch_path_text.setText(((DirectoryDialog)dialog).open());
            	
            }
        });
    }
    
    public void drawLibrarySelector(Composite parent) {
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
                saveConfiguration();
            }
        });
    }
    
    public String getProcessingPathText() {
        return processing_app_path_text.getText();
    }
    
    public boolean validatePathExists() {
        return new File(processing_app_path_text.getText()).exists();
    }
    
    public boolean validatePathIsProcessing() {
        // final check for core.jar
        return new File(processing_app_path_text.getText(),
                OSHelperManager.getHelper().getCorePath() + "core.jar").exists();
        
    }
    
    public void saveConfiguration() {
        project_configuration.setSelectedLibraries(getSelectedLibraries());
        project_configuration.setProcessingAppPath(processing_app_path_text.getText());
        project_configuration.setProcessingSketchPath(processing_sketch_path_text.getText());
    }    
    
    
    private void showDiscoveredLibraries() {
    	
        File librariesDir = new File(project_configuration.getProcessingAppPath(),
                OSHelperManager.getHelper().getLibraryPath());
        List<String> libraries = new ArrayList<String>();
        if (librariesDir.exists()) { 
            String[] files = librariesDir.list();
            for (String file : files) {
                if ((new File(librariesDir, file)).isDirectory())
                    libraries.add(file);
            }
        }
        
        librariesDir = new File(project_configuration.getProcessingSketchPath());
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
    
    private ArrayList<String> getSelectedLibraries() {
        ArrayList<String> libs = new ArrayList<String>();
        for (Object element : libraries_viewer.getCheckedElements()) {
           libs.add((String) element);
        }
        return libs;
    }    
    
    private void setSelectedLibraries() {
        List<String> selectedLibs = project_configuration.getSelectedLibraries();
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
