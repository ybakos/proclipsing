package dch.eclipse.p5Export;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;

public class P5ExportWizard extends Wizard implements IExportWizard
{ 
  private P5ExportPage p5MainPage;
  
  public P5ExportWizard()
  {
    super();
    setNeedsProgressMonitor(true);  
/*    setHelpAvailable(true);
    WizardDialog.setDialogHelpAvailable(true);
    WorkbenchHelpSystem help = (WorkbenchHelpSystem)PlatformUI.getWorkbench().getHelpSystem();
    System.err.println("HELP: "+help);
    help.setHelp(
        getShell(),
        "P5Exporter.wizard1.HelpSystem_help"
    );  */  
    setDefaultPageImageDescriptor(P5ExportPlugin.LARGE_ICON);
    setWindowTitle(P5ExportPlugin.NAME+"  v"+P5ExportPlugin.VERSION+""); //$NON-NLS-1$ //$NON-NLS-2$
  }  
  
  public void init(IWorkbench workbench, IStructuredSelection selection)
  {
    p5MainPage = new P5ExportPage(Messages.getString("P5ExportWizard.2")); //$NON-NLS-1$
    p5MainPage.setDescription(Messages.getString("P5ExportWizard.3"));     //$NON-NLS-1$
    addPage(p5MainPage);
  }    
  
  public boolean performFinish()
  {
    IRunnableWithProgress runThread = new IRunnableWithProgress()  {
      public void run(IProgressMonitor monitor) throws InvocationTargetException {           
        try
        {
          doFinish(monitor);
        } 
        catch (Throwable e)
        {
          throw new InvocationTargetException(e);
        }
        finally {
          monitor.done();
        }
      }
    }; 
    
    try  {
      getContainer().run(true, false, runThread);
    }
    catch (InterruptedException e) {
      return false;
    }
    catch (InvocationTargetException e) {      
      Throwable re = e.getTargetException();
      P5ExportUtils.errorDialog(getShell(), re.getMessage(), e);
      return false;
    }
    return true;
  }
    

  protected void doFinish(IProgressMonitor monitor) throws Exception
  {
    P5ExportComp comp = p5MainPage.getComposite();
    IJavaProject project = comp.getSelectedJavaProject();
    P5ExportType exportType = comp.getSelectedExportType();
    P5ExportBuilder builder = new P5ExportBuilder(getShell(), project, exportType);    

    // check appropriate options for export type
    if (exportType.isApplet) {     
      exportType.setForceMultipleJars(comp.getForceMultipleJars());
    }
    else {      
      exportType.setUsePresentMode(comp.getUsePresentMode());
      exportType.setAddStopButton(comp.getAddStopButton());
    }
    
    monitor.beginTask(Messages.getString("P5ExportWizard.4"), 2);      //$NON-NLS-1$
    monitor.worked(1);
    monitor.setTaskName(Messages.getString("P5ExportWizard.5")); //$NON-NLS-1$
    
    final List createdDirs = new ArrayList();    
    final List dirsToOpen = p5MainPage.createExports
      (project, exportType, builder, createdDirs);       
    
    monitor.worked(4);
    monitor.setTaskName(Messages.getString("P5ExportWizard.6")); //$NON-NLS-1$
    
    StringBuffer sb = new StringBuffer();
    sb.append(Messages.getString("P5ExportWizard.7"));    //$NON-NLS-1$
    for (Iterator j = createdDirs.iterator(); j.hasNext();) {
      sb.append(((File)j.next()));
      if (j.hasNext()) sb.append("\n\n"); //$NON-NLS-1$
    }      
    
    final String message = sb.toString();
    getShell().getDisplay().syncExec(new Runnable() 
    {
      public void run() {
        if (dirsToOpen == null) 
          throw new RuntimeException(Messages.getString("P5ExportWizard.9")); //$NON-NLS-1$
        File f = (File)dirsToOpen.get(0);
        if (MessageDialog.openConfirm(getShell(), Messages.getString("P5ExportWizard.10"), message))  //$NON-NLS-1$
        {
          try
          {
            P5ExportUtils.openFolder(f);
          }
          catch (P5ExportException e)
          {
            System.err.println("[WARN] Unable to open folder: "+e.getMessage());
          }
        }
        else {
          for (Iterator j = createdDirs.iterator(); j.hasNext();) {
            File toRemove = (File)j.next();
            //System.err.println("Removing: "+toRemove);
            P5ExportUtils.deleteDir(toRemove);  // remove all dirs created
          }
        }
    }});
    monitor.worked(1);
    monitor.setTaskName(Messages.getString("P5ExportWizard.11")); //$NON-NLS-1$
    project.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
    monitor.worked(1);
  }     

}// end
