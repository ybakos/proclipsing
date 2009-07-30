package proclipsing.core.createproject;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import proclipsing.util.LogHelper;


public class NewProcessingProjectWizard extends Wizard implements INewWizard {

    private NewProcessingProjectPage1 page1;
    private ProjectConfiguration configuration;
    
	@Override
	public boolean performFinish() {
		IRunnableWithProgress job = new CreateProcessingProjectJob(configuration);
		try {
			getContainer().run(false, false, job);
		} catch (Exception e) {
			LogHelper.LogError(e);
			return false;
		}
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// configuration values are set in the pages
	    configuration = new ProjectConfiguration();	
	}

	@Override
	public void addPages() {
	    page1 = new NewProcessingProjectPage1();
		addPage(page1);
		super.addPages();
	}
	
	public ProjectConfiguration getProjectConfiguration() {
		return configuration;
	}

}
