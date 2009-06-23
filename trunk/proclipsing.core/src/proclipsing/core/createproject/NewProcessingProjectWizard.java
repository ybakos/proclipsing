package proclipsing.core.createproject;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

import proclipsing.processingprovider.pub.ProcessingProvider;


public class NewProcessingProjectWizard extends Wizard implements INewWizard {

    private NewProcessingProjectPage1 page1;
    private ProjectConfiguration configuration;
    
	@Override
	public boolean performFinish() {
		IRunnableWithProgress job = new CreateProcessingProjectJob(configuration);
		try {
			getContainer().run(false, false, job);
		} catch (Exception e) {
			e.printStackTrace();
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
