package proclipsing.core.createproject;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.actions.WorkspaceModifyOperation;

import proclipsing.core.Activator;
import proclipsing.core.ProcessingProjectNature;
import proclipsing.core.preferences.PreferenceController;
import proclipsing.core.preferences.ProjectPreferences;
import proclipsing.util.LogHelper;
import proclipsing.util.Util;


/**
 * This job does the work of creating a Processing Project
 */
public class CreateProcessingProjectJob extends WorkspaceModifyOperation {

	private static final String PROJECT_NAME_PLACEHOLDER = "%project_name%";
	private static final String PACKAGE_NAME_PLACEHOLDER = "%package_name%";
	
	private Vector<IClasspathEntry> classpath_entries;
	private String package_name;
	private ProjectPreferences preferences;
	private boolean is_app;

	public CreateProcessingProjectJob(ProjectPreferences preferences, boolean isApp) {
	    this.preferences = preferences;	
	    classpath_entries = new Vector<IClasspathEntry>();
		is_app = isApp;
	}
	
	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {
		
		IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
		
		IProject project = wsroot.getProject(preferences.getName());
		
		package_name = Util.projNametoPackage(preferences.getName());
		
		project.create(monitor);
		project.open(monitor);
		
		addNatures(project, monitor);
		
		createBinFolder(project, monitor);
		createSrcFolder(project, monitor);
		createDataFolder(project, monitor);
		createProjFolder(project, monitor);
		createLibFolders(project, monitor);
		
		addMyAppletSkeleton(project, monitor);
		PreferenceController.saveToProject(project, preferences, classpath_entries);
		
	}

	private void addMyAppletSkeleton(IProject project, IProgressMonitor monitor) throws CoreException {
		IFolder srcDir = project.getFolder(ProjectPreferences.SRC_DIR + "/" + package_name);
		IFile defaultApplet = srcDir.getFile(Util.strToCamelCase(preferences.getName()) + ".java");
		
		URL url = Activator.getDefault().getBundle().getResource(
				"template/PAppletTemplate.tmpl");
		
		if(is_app){
			url = Activator.getDefault().getBundle().getResource(
				"template/PAppTemplate.tmpl");
		}
		
		try{
			String templateStr = Util.convertStreamToString(url.openStream());

			String newApplet = Util.replaceAllSubString(templateStr, PACKAGE_NAME_PLACEHOLDER, package_name);
			newApplet = Util.replaceAllSubString(newApplet, 
					PROJECT_NAME_PLACEHOLDER, Util.strToCamelCase(preferences.getName()));
			
			byte[] bArray = newApplet.getBytes();
			InputStream bais = new ByteArrayInputStream(bArray);
			
			defaultApplet.create(bais, true, monitor);
		} catch (Exception e) {
			LogHelper.LogError(e);
		}
	}
	
    	
	private void addNatures(IProject project, IProgressMonitor monitor) throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setNatureIds(new String[]{
				JavaCore.NATURE_ID, 					// add java nature
				ProcessingProjectNature.class.getName() // add processing project nature
			});
		project.setDescription(desc, monitor);		
	}
	
	
    private IFolder createSrcFolder(IProject project, IProgressMonitor monitor) throws CoreException {
        IFolder srcDir = project.getFolder(ProjectPreferences.SRC_DIR);
        srcDir.create(true, true, monitor);
        classpath_entries.add(JavaCore.newSourceEntry(srcDir.getFullPath()));
        return srcDir;
    }
	
	
    private IFolder createDataFolder(IProject project, IProgressMonitor monitor) throws CoreException {
        IFolder dataDir = project.getFolder(ProjectPreferences.SRC_DIR + "/" + ProjectPreferences.DATA_DIR);
        dataDir.create(true, true, monitor);
        return dataDir;
    }
	
    private IFolder createProjFolder(IProject project, IProgressMonitor monitor) throws CoreException {
        IFolder projSrcDir = project.getFolder(ProjectPreferences.SRC_DIR + "/" + package_name);
        projSrcDir.create(true, true, monitor);
        return projSrcDir;
    }
	
	private IFolder createLibFolders(IProject project, IProgressMonitor monitor) throws CoreException {
		IFolder libDir = project.getFolder("lib");
		libDir.create(true, true, monitor);
		
		IFolder baselibDir = project.getFolder(ProjectPreferences.BASELIB_DIR);
		baselibDir.create(true, true, monitor);
		
        IFolder userlibDir = project.getFolder(ProjectPreferences.USERLIB_DIR);
        userlibDir.create(true, true, monitor);		
		
		return libDir;
	}

	private IFolder createBinFolder(IProject project, IProgressMonitor monitor) throws CoreException {
		IFolder binDir = project.getFolder(ProjectPreferences.BIN_DIR);
		binDir.create(true, true, monitor);
		JavaCore.create(project).setOutputLocation(binDir.getFullPath(), monitor);
		return binDir;
	}

}
