package dch.eclipse.p5Export;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;

public class P5ExportAction extends Action implements IWorkbenchWindowActionDelegate
{
  P5ExportWizard wizard;
  ISelection selection;

  /** Called when the action is created. */
  public void init(IWorkbenchWindow window) {}

  /** Called when the action is discarded. */
  public void dispose() {}

  /** Called when the action is executed. */
  public void run(IAction action)
  {
    wizard = new P5ExportWizard();
    IWorkbench wb = PlatformUI.getWorkbench();
    wizard.init(wb, null);
    Shell shell = wb.getActiveWorkbenchWindow().getShell();
    WizardDialog wd = new WizardDialog(shell, wizard);
    wd.open();
    // System.err.println("WIZARD="+wizard);
  }

  /** Called when objects in the editor are selected or deselected. */
  public void selectionChanged(IAction action, ISelection selection)
  {
    this.selection = selection;
  }
}