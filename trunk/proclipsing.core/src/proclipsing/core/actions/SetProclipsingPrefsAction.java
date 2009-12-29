package proclipsing.core.actions;

import java.awt.Window;
import java.net.URL;

import javax.swing.WindowConstants;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import proclipsing.core.createproject.ProjectConfiguration;
import proclipsing.core.ui.IValidateListener;
import proclipsing.core.ui.PathAndLibrariesSelectionDrawer;
import proclipsing.processingprovider.ProcessingLibrary;
import proclipsing.processingprovider.ProcessingProvider;
import proclipsing.util.LogHelper;

public class SetProclipsingPrefsAction implements IObjectActionDelegate {

	private Shell shell;
	private ProjectConfiguration project_configuration = null;
	private PathAndLibrariesSelectionDrawer content_drawer = null;
	
	/**
	 * Constructor for Action1.
	 */
	public SetProclipsingPrefsAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		shell = targetPart.getSite().getShell();
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
	    if (project_configuration == null) {
	    	ErrorDialog.openError(shell, "No Project", "A Project was not selected.", null);
	    	return;
	    }
	   
	    
	    new ProjectPrefsDialog(shell, "Processing Processing Preferences", null,
	            "Update the preferences for your processing project.", 
	            MessageDialog.NONE, new String[] {IDialogConstants.OK_LABEL,
                        IDialogConstants.CANCEL_LABEL}, 1).open();
	   
	    
	    /*
		MessageDialog.openInformation(
			shell,
			"Proclipsing Plug-in",
			"Proclipsing Project Prefs was executed.\n" + msg);
			*/
	}

	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof IProject) {
                project_configuration = new ProjectConfiguration((IProject) element);
            }
        }
	}
	
	private class ProjectPrefsDialog extends MessageDialog implements IValidateListener {

		public ProjectPrefsDialog(Shell parentShell, String dialogTitle,
				Image dialogTitleImage, String dialogMessage,
				int dialogImageType, String[] dialogButtonLabels,
				int defaultIndex) {
			
			super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
					dialogImageType, dialogButtonLabels, defaultIndex);
			
		}

        protected Control createCustomArea(Composite parent) {
    		Composite composite = new Composite(parent, SWT.NONE);
    		composite.setLayout(new GridLayout());
    	    
    		content_drawer = 
    	    	new PathAndLibrariesSelectionDrawer(this);
    	    
        	content_drawer.drawPaths(composite,
        	        project_configuration.getProcessingAppPath(),
        	        project_configuration.getProcessingSketchPath(),
        	        project_configuration.getSelectedLibraries());
    		content_drawer.drawLibrarySelector(composite,
    		        project_configuration.getSelectedLibraries());
    		
            return composite;
        }		
		
		public void validate() {
		    //saveConfiguration();
		}
		

		protected void buttonPressed(int buttonId) {
		   saveConfiguration();
		   super.buttonPressed(buttonId);
		}
		
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			validate();
		}

	    protected void saveConfiguration() {
	        project_configuration.setSelectedLibraries(content_drawer.getSelectedLibraries());
	        project_configuration.setProcessingAppPath(content_drawer.getProcessingPath());
	        project_configuration.setProcessingSketchPath(content_drawer.getSketchPath());
	        project_configuration.savePaths();
	        project_configuration.saveLibs();
	    }
	    
	}
	
}
