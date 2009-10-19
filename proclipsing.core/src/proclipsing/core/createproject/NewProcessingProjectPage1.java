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

import proclipsing.core.ui.PathAndLibrariesSelectionDrawer;
import proclipsing.core.ui.IValidateListener;

public class NewProcessingProjectPage1 extends WizardPage implements IValidateListener {

	private static String PAGE_NAME                 = "New Processing Project";
	private static String PAGE_TITLE                = "Processing";
	private static String PROJECT_NAME_LABEL        = "Project Name";
	private static int    PROJECT_NAME_MAXSIZE      = 150;
	
	private Text project_name_text;
	private Button appButton;
	private PathAndLibrariesSelectionDrawer path_and_libraries_drawer;
	
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

        path_and_libraries_drawer = 
            new PathAndLibrariesSelectionDrawer(getProjectConfiguration(), this);		
		path_and_libraries_drawer.drawProcessingAppFinder(composite);		
		path_and_libraries_drawer.drawProcessingSketchFinder(composite);
		drawAppOption(composite);
		path_and_libraries_drawer.drawLibrarySelector(composite);
		setControl(composite);
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
                    path_and_libraries_drawer.getProcessingPathText() + ") does not exist.");
            return false;           
        }
        
        if (!path_and_libraries_drawer.validatePathIsProcessing()) {
            setErrorMessage(
                    path_and_libraries_drawer.getProcessingPathText() 
                    + " does not contain the processing libs.");
            return false;           
        }
        
        saveConfiguration();        
        return true;
        
    }

    private void saveConfiguration() {
        path_and_libraries_drawer.saveConfiguration();
    	getProjectConfiguration().setProjectName(project_name_text.getText());
    	getProjectConfiguration().setApp(appButton.getSelection());
    }
    
    private ProjectConfiguration getProjectConfiguration() {
        return ((NewProcessingProjectWizard) getWizard()).getProjectConfiguration();
    }	

}
