package proclipsing.core.createproject;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;


public class NewProcessingProjectWizard extends Wizard implements INewWizard {

    private NewProcessingProjectPage1 page1;
    
	@Override
	public boolean performFinish() {
		IRunnableWithProgress job = new CreateProcessingProjectJob(getProjectName());
		try {
			getContainer().run(false, false, job);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		// TODO Auto-generated method stub

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

}
