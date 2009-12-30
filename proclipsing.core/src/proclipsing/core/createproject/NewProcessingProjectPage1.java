package proclipsing.core.createproject;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import proclipsing.core.preferences.ProjectPreferences;
import proclipsing.core.ui.PathAndLibrariesSelectionDrawer;
import proclipsing.core.ui.IValidateListener;

public class NewProcessingProjectPage1 extends WizardPage implements IValidateListener {

	private static String PAGE_NAME                 = "New Processing Project";
	private static String PAGE_TITLE                = "Processing";
	private static String PROJECT_NAME_LABEL        = "Project Name";
	private static int    PROJECT_NAME_MAXSIZE      = 150;
	private static int    LABEL_WIDTH_HINT          = 150;
	private static int    TEXT_WIDTH_HINT           = 350;
	
	private boolean is_drawn = false;
	private Text project_name_text;
	private Button appButton;
	private PathAndLibrariesSelectionDrawer path_and_libraries_drawer;
	
	protected NewProcessingProjectPage1() {
	    super(PAGE_NAME, PAGE_TITLE, null);
	}

	public void createControl(Composite parent) { 
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		Composite titlearea = new Composite(composite, SWT.NONE);
		titlearea.setLayout(new GridLayout(3, false));
		titlearea.setLayoutData(new GridData(SWT.FILL));
		
		Label label = new Label(titlearea, SWT.NONE);
		label.setText(PROJECT_NAME_LABEL);
		GridData gd = new GridData(SWT.FILL);
		gd.widthHint = LABEL_WIDTH_HINT;
		label.setLayoutData(gd);
		 
		project_name_text = new Text(titlearea, SWT.FILL | SWT.BORDER);
		GridData gd2 = new GridData(SWT.FILL);
		gd2.widthHint = TEXT_WIDTH_HINT;
		project_name_text.setLayoutData(gd2);
		project_name_text.addModifyListener(new ModifyListener() {
		    public void modifyText(ModifyEvent e) {
		        // calling this forces isPageComplete() to get called
		        setPageComplete(true);
            }
		});

        path_and_libraries_drawer = 
            new PathAndLibrariesSelectionDrawer(this);
        
        ProjectPreferences prefs = new ProjectPreferences();
        path_and_libraries_drawer.drawPaths(composite, prefs);
		drawAppOption(composite);
		path_and_libraries_drawer.drawLibrarySelector(composite, prefs);
		setControl(composite);
		is_drawn = true;
	}
	
    private void drawAppOption(Composite parent){
	    Composite appOption = new Composite(parent, SWT.NONE);
	    RowLayout rowLayout = new RowLayout(SWT.VERTICAL);
	    appOption.setLayout(rowLayout);
	    appOption.setLayoutData(
	            new GridData(GridData.VERTICAL_ALIGN_BEGINNING));
	    
		Button appletButton = new Button(appOption, SWT.RADIO);
		appButton = new Button(appOption, SWT.RADIO);

		appButton.setText("Application");
		appletButton.setText("Applet");
		
		appButton.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {}

			public void widgetSelected(SelectionEvent arg0) {
	            setPageComplete(true);
			}			
		});
		
		appletButton.setSelection(true);
	}
	
    /**
     * 
     * Call this when you want to force validate
     */
    public void validate() {
        setPageComplete(true);
    }     
    
	/**
	 * invoked any time setPageComplete(true) is called
	 * This then calls saveConfiguration()
	 * So the best thing to do is after any changes by the user,
	 * call setPageComplete(true) to have this run and check the changes
	 * and then save the changes if everything looks ok
	 * 
	 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
	 */
    public boolean isPageComplete() {
    	
        setErrorMessage(null);     
        if (!is_drawn) return false;
        
        String projName = project_name_text.getText();
        char[] cs = projName.toCharArray();
        
        // no project name
        if (cs.length < 1) return false;
        
        // project name too long!
        if (cs.length > PROJECT_NAME_MAXSIZE) {
            setErrorMessage("Project name limit reached!");
            return false;
        }
        
        // invalid project name
        for (int i = 0; i < cs.length; i++) {
            if (cs[i] == ' ' || cs[i] == '_') {
                continue;
            }
            if (!Character.isLetterOrDigit(cs[i])) {
                setErrorMessage("Invalid project name.");
                return false;
            }
        }       
        
        // project already exists
        IProject proj = 
        	ResourcesPlugin.getWorkspace().getRoot().getProject(projName);
        if (proj.exists()) {
            setErrorMessage("Project with name " + projName + " already exists.");
            return false;
        }     
        
        if (!path_and_libraries_drawer.validatePathExists()) {
            setErrorMessage("Processing path (" + 
                    path_and_libraries_drawer.getProcessingPath() + ") does not exist.");
            return false;           
        }
        
        if (!path_and_libraries_drawer.validatePathIsProcessing()) {
            setErrorMessage(
                    path_and_libraries_drawer.getProcessingPath() 
                    + " does not contain the processing libs.");
            return false;           
        }
        
        saveConfiguration();        
        return true;
        
    }

    private void saveConfiguration() {
    	 ((NewProcessingProjectWizard) getWizard()).setConfiguration(
    			 project_name_text.getText(), path_and_libraries_drawer.getProcessingPath(),
    			 path_and_libraries_drawer.getSketchPath(), path_and_libraries_drawer.getSelectedLibraries(),
    			 appButton.getSelection());
    }
    

}
