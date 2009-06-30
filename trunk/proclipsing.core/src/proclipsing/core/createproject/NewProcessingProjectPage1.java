package proclipsing.core.createproject;
import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

import proclipsing.processingprovider.pub.ProcessingProvider;

public class NewProcessingProjectPage1 extends WizardPage {

	public static String 	PAGE_NAME 			= "New Processing Project";
	public static String 	PAGE_TITLE 			= "Processing";
	public static String 	PROJECT_NAME_LABEL 	= "Project Name";
	public static int 		PROJECT_NAME_MAXSIZE= 150;
	
	private Text project_name_text;
	private CheckboxTableViewer libraries_viewer;
	private Button appButton;
	
	protected NewProcessingProjectPage1() {
	    super(PAGE_NAME, PAGE_TITLE, null);
	}

	public void createControl(Composite parent) { 
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		 
		Label label = new Label(composite, SWT.NONE);
		label.setText(PROJECT_NAME_LABEL);
		label.setLayoutData(new GridData());
		 
		project_name_text = new Text(composite, SWT.NONE | SWT.BORDER);
		project_name_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		project_name_text.addModifyListener(new ModifyListener() {
		    public void modifyText(ModifyEvent e) {
		        // calling this forces isPageComplete() to get called
		        setPageComplete(true);
            }
		});
		
		drawAppOption(composite);
        drawLibrarySelector(composite);
		setControl(composite);
	}
	
	public void drawAppOption(Composite parent){

		Button appletButton = new Button(parent, SWT.RADIO);
		appButton = new Button(parent, SWT.RADIO);

		appButton.setText("Application");
		appletButton.setText("Applet");
		
		appButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {}

			public void widgetSelected(SelectionEvent arg0) {
	            saveConfiguration();
			}			
		});
		
		appletButton.setSelection(true);
	}
	
	public void drawLibrarySelector(Composite parent) {
        // group surrounds the box w/ a thin line
		Group projectsGroup = new Group(parent, SWT.NONE);
        projectsGroup.setText("Select Libraries to Import");
        GridData gdProjects = new GridData(GridData.FILL_BOTH);
        gdProjects.horizontalSpan = 2;
        projectsGroup.setLayoutData(gdProjects);
        projectsGroup.setLayout(new GridLayout(1, false));
        
        // main table to hold the library entries
        Table librariesTable = new Table(projectsGroup ,SWT.CHECK | SWT.BORDER | SWT.V_SCROLL);
        librariesTable.setHeaderVisible(false);

        TableColumn col1 = new TableColumn(librariesTable, SWT.NONE);
        col1.setWidth(200);
        col1.setText("Processing Library");

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
        libraries_viewer.setInput(ProcessingProvider.getAllLibraryIdentifiers());
        libraries_viewer.setAllChecked(true);
	}
	
	
	public ArrayList<String> getSelectedLibraries() {
	    ArrayList<String> libs = new ArrayList<String>();
	    for (Object element : libraries_viewer.getCheckedElements()) {
	       libs.add((String) element);
	    }
	    return libs;
	}
	
    public boolean isPageComplete() {
        setErrorMessage(null);
        String projName = project_name_text.getText();
        char[] cs = projName.toCharArray();
        if (cs.length < 1) return false;
        else {
            if (cs.length > PROJECT_NAME_MAXSIZE) {
                setErrorMessage("Project name limit reached!");
                return false;
            }
            for (int i = 0; i < cs.length; i++) {
                if (cs[i] == ' ' || cs[i] == '_') {
                    continue;
                }
                if (!Character.isLetterOrDigit(cs[i])) {
                    setErrorMessage("Invalid project name.");
                    return false;
                }
            }

            IProject proj = 
            	ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
            if (proj.exists()) {
                setErrorMessage("Project with name " + projName + " already exists.");
                return false;
            }
            
            saveConfiguration();
            return true;
        }
    }	
	
    private void saveConfiguration() {
    	ProjectConfiguration config = ((NewProcessingProjectWizard)getWizard()).getProjectConfiguration();
    	config.setSelectedLibraries(getSelectedLibraries());
    	config.setProjectName(project_name_text.getText());
    	config.setApp(appButton.getSelection());
    }
    
    
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
