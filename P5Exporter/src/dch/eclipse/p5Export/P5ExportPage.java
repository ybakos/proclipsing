package dch.eclipse.p5Export;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * This is the main page of the plugin.
 */
public class P5ExportPage extends WizardPage
{
  private P5ExportComp composite; 
  
  public P5ExportPage(String pageName)
  {
    super(pageName);
  }

  public P5ExportPage(String pageName, String title, ImageDescriptor titleImage)
  {
    super(pageName, title, titleImage);
  }

  public void createControl(Composite parent)
  {   
    composite = new P5ExportComp(this, parent, SWT.NONE);
    setControl(composite);
    setPageComplete(false);
  }
  
  public List createExports(IJavaProject project, P5ExportType exportType, 
    P5ExportBuilder builder, List createdFiles) throws Exception
  {    
    ILaunchConfiguration runConfig = composite.getConfiguration();
    File openDir = builder.doExport(runConfig, createdFiles);
    List files = new ArrayList();
    files.add(openDir);
    return files;
  }

  public P5ExportComp getComposite()
  {
    return this.composite;
  }
}
