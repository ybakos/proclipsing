package proclipsing.core.createproject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;

import proclipsing.core.Activator;
import proclipsing.os.OSHelperManager;
import proclipsing.processingprovider.ProcessingLibrary;
import proclipsing.processingprovider.ProcessingProvider;
import proclipsing.util.Util;


/**
 * This job does the work of creating a Processing Project
 */
public class CreateProcessingProjectJob extends WorkspaceModifyOperation {

	private static final String BIN_DIR                  = "bin";
	private static final String SRC_DIR                  = "src";
	private static final String LIB_DIR                  = "lib";
	private static final String PROJECT_NAME_PLACEHOLDER = "%project_name%";
	private static final String PACKAGE_NAME_PLACEHOLDER = "%package_name%";
	
	private Vector<IClasspathEntry> classpath_entries;
	private String package_name;
	private ProjectConfiguration configuration;
	private boolean is_app;
	
	public CreateProcessingProjectJob(ProjectConfiguration configuration) {
	    this.configuration = configuration;
	    classpath_entries = new Vector<IClasspathEntry>();
		is_app = configuration.isApp();
	}
	
	@Override
	protected void execute(IProgressMonitor monitor) throws CoreException,
			InvocationTargetException, InterruptedException {
		
		IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = wsroot.getProject(configuration.getProjectName());
		
		package_name = Util.projNametoPackage(configuration.getProjectName());
		
		project.create(monitor);
		project.open(monitor);
		
		addJavaNature(project, monitor);
		
		createBinFolder(project, monitor);
		createSrcFolder(project, monitor);
		createProjFolder(project, monitor);
		
		addProcessing(createLibFolder(project, monitor), monitor);
		addMyAppletSkeleton(project, monitor);
		setProjectClassPath(project, monitor);
		
	}

    private void addMyAppletSkeleton(IProject project, IProgressMonitor monitor) throws CoreException {
		IFolder srcDir = project.getFolder(SRC_DIR + "/" + package_name);
		IFile defaultApplet = srcDir.getFile(Util.strToCamelCase(configuration.getProjectName()) + ".java");
		
		URL url = Activator.getDefault().getBundle().getResource(
				"template/PAppletTemplate.java");
		
		if(is_app){
			url = Activator.getDefault().getBundle().getResource(
				"template/PAppTemplate.java");
		}
		
		try{
			String templateStr = Util.convertStreamToString(url.openStream());

			String newApplet = Util.replaceAllSubString(templateStr, PACKAGE_NAME_PLACEHOLDER, package_name);
			newApplet = Util.replaceAllSubString(newApplet, PROJECT_NAME_PLACEHOLDER, Util.strToCamelCase(configuration.getProjectName()));
			
			byte[] bArray = newApplet.getBytes();
			InputStream bais = new ByteArrayInputStream(bArray);
			
			defaultApplet.create(bais, true, monitor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    
    /**
     * Add processing libs to the project & set up classpath
     * 
     * @param lib - IFolder where to move the files to
     * @param monitor
     */
    private void addProcessing(IFolder lib, IProgressMonitor monitor) {
        ProcessingLibrary[] libraries = ProcessingProvider.getLibraries(
                configuration.getProcessingPath(),
    	        configuration.getSelectedLibraries().toArray(
    	                new String[configuration.getSelectedLibraries().size()]));
        
        for(ProcessingLibrary library : libraries) {
            // create folder containing lib based on identifier
            IFolder libFolder = 
            	lib.getFolder(library.getIdentifier());
            try {
            	if (!libFolder.exists())
					libFolder.create(true, true, monitor);
			} catch (CoreException e1) {
				e1.printStackTrace();
				continue;
			}

        	// get urls to all jar, zip, + native lib files
            URL[] urls = library.getUrls();
            if (urls == null) continue;
			addProcessingLibs(libFolder, urls, monitor);
        }
        
    }
    
    /**
     * Given urls to the libraries, move them to the right spot
     * then add the jars and zip files to the classpath
     * 
     * @param libFolder
     * @param libUrls
     * @param monitor
     */
    private void addProcessingLibs(
    		IFolder libFolder, URL[] libUrls, IProgressMonitor monitor){
        //vars used in url loop
        String filename; IFile libFile;            
        // go through urls, moving files into project and adding jars and zips to classpath
        for (URL url : libUrls) {
            filename = url.getPath().substring(url.getPath().lastIndexOf(OSHelperManager.getHelper().getFileSeparator()) + 1);
            try {
                libFile = libFolder.getFile(filename);
                // extra check to prevent error
                if (!libFile.exists())
                	libFile.create(url.openStream(), true, monitor);
                if (filename.endsWith(".jar") || filename.endsWith(".zip")){
                    classpath_entries.add(
                            JavaCore.newLibraryEntry(libFile.getFullPath(), null, null, new IAccessRule[0], 
                            		new IClasspathAttribute[]{
                                			JavaCore.newClasspathAttribute(
                                					JavaRuntime.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY, 
                                					libFolder.getFullPath().toPortableString().substring(1))}, false));
                }
            } catch (CoreException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }    	
    }
	
	private void addJavaNature(IProject project, IProgressMonitor monitor) throws CoreException {
		IProjectDescription desc = project.getDescription();
		desc.setNatureIds(new String[]{JavaCore.NATURE_ID});
		project.setDescription(desc, monitor);		
	}
	
	/**
	 * Adds the default Java classpath and then adds 
	 * everything in classpath_entries to the project classpath
	 * 
	 * @param proj
	 * @param monitor
	 * @throws JavaModelException
	 */
	private void setProjectClassPath(IProject proj, IProgressMonitor monitor) throws JavaModelException {
		IPath path  = JavaRuntime.newDefaultJREContainerPath();
		classpath_entries.add(JavaCore.newContainerEntry(path));
		
		// convert classpath_entries to IClasspathEntry[] and set classpath on java project
		JavaCore.create(proj).setRawClasspath(classpath_entries.toArray(
						new IClasspathEntry[classpath_entries.size()]), null);
	}
	
    private IFolder createSrcFolder(IProject project, IProgressMonitor monitor) throws CoreException {
        IFolder srcDir = project.getFolder(SRC_DIR);
        srcDir.create(true, true, monitor);
        classpath_entries.add(JavaCore.newSourceEntry(srcDir.getFullPath()));
        return srcDir;
    }
	
    private IFolder createProjFolder(IProject project, IProgressMonitor monitor) throws CoreException {
        IFolder projSrcDir = project.getFolder(SRC_DIR + "/" + package_name);
        projSrcDir.create(true, true, monitor);
        return projSrcDir;
    }
	
	private IFolder createLibFolder(IProject project, IProgressMonitor monitor) throws CoreException {
		IFolder libDir = project.getFolder(LIB_DIR);
		libDir.create(true, true, monitor);
		return libDir;
	}

	private IFolder createBinFolder(IProject project, IProgressMonitor monitor) throws CoreException {
		IFolder binDir = project.getFolder(BIN_DIR);
		binDir.create(true, true, monitor);
		JavaCore.create(project).setOutputLocation(binDir.getFullPath(), monitor);
		return binDir;
	}

}
