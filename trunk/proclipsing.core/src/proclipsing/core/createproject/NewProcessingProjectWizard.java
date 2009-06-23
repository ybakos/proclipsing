package proclipsing.core.createproject;

import java.util.ArrayList;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


public class NewProcessingProjectWizard extends Wizard implements INewWizard {

    private NewProcessingProjectPage1 page1;
    private ProjectConfiguration configuration;
    
	@Override
	public boolean performFinish() {
	    configuration = new ProjectConfiguration(getProjectName(), getSelectedLibraries());
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
	}

	@Override
	public void addPages() {
	    page1 = new NewProcessingProjectPage1();
		addPage(page1);
		super.addPages();
	}
	
	private String getProjectName() {
	    return page1.getProjectName();
	}

	private ArrayList<String> getSelectedLibraries() {
	    return page1.getSelectedLibraries();
	}
}
