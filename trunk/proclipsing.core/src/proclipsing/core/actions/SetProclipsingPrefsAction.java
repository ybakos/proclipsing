package proclipsing.core.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

import proclipsing.core.createproject.ProjectConfiguration;

public class SetProclipsingPrefsAction implements IObjectActionDelegate {

	private Shell shell;
	private ProjectConfiguration project_configuration = null;
	
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
	    
	    
	    String msg = "";
	    if (project_configuration != null)
	        msg = 
	            "PROCESSING PATH: " + project_configuration.getProcessingAppPath() + "\n" + 
	            "SKETCH PATH:     " + project_configuration.getProcessingSketchPath();
	    
	    new MessageDialog(shell, "Processing Processing Preferences", null,
	            "Update the preferences for your processing project " + "\n\n" + msg, 
	            MessageDialog.NONE, new String[] {IDialogConstants.OK_LABEL,
                        IDialogConstants.CANCEL_LABEL}, 1) {
	        
	        protected Control createCustomArea(Composite parent) {
	            // TODO - do this!
	            
	            return null;
	        }
	        
	    }.open();
	    
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

}
