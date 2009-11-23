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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import proclipsing.core.createproject.ProjectConfiguration;
import proclipsing.os.OSHelperManager;

public class PathAndLibrariesSelectionDrawer {

    private static String IMPORT_LIBRARIES_LABEL    = "Select Libraries to Import";    

    private ProjectConfiguration project_configuration;
    private IValidateListener validate_listener;
    private CheckboxTableViewer libraries_viewer;   
    ProcessingPathDrawer drawer;
    
    
    public PathAndLibrariesSelectionDrawer(
            ProjectConfiguration projectConfiguration, IValidateListener validateListener) {
        project_configuration = projectConfiguration;
        validate_listener = validateListener;
        drawer = new ProcessingPathDrawer();
    }
 

    public void drawPaths(Composite parent) {
        Composite composite = drawer.draw(parent);
        
        drawer.getProcessingPathTextWidget().setText(project_configuration.getProcessingAppPath());
        drawer.getProcessingPathTextWidget().addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                validate_listener.validate();
                showDiscoveredLibraries();
                setSelectedLibraries();     
            }
        });
        
        drawer.getSketchPathTextWidget().setText(project_configuration.getProcessingSketchPath());
        drawer.getSketchPathTextWidget().addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                System.out.println("ooooh");
                validate_listener.validate();
                showDiscoveredLibraries();
                setSelectedLibraries();
            }
        });
        
        GridData gd = new GridData(SWT.FILL);
        composite.setLayoutData(gd);
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
        return drawer.getProcessingPath();
    }
    
    public boolean validatePathExists() {
        return new File(drawer.getProcessingPath()).exists();
    }
    
    public boolean validatePathIsProcessing() {
        // final check for core.jar
        return new File(drawer.getProcessingPath(),
                OSHelperManager.getHelper().getCorePath() + "core.jar").exists();
        
    }
    
    public void saveConfiguration() {
        project_configuration.setSelectedLibraries(getSelectedLibraries());
        project_configuration.setProcessingAppPath(drawer.getProcessingPath());
        project_configuration.setProcessingSketchPath(drawer.getSketchPath());
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
