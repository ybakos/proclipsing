package proclipsing.core.createproject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import proclipsing.core.Activator;
import proclipsing.os.OSHelperManager;
import proclipsing.processingprovider.ProcessingLibrary;
import proclipsing.processingprovider.ProcessingProvider;
import proclipsing.util.LogHelper;

/**
 * Simple class to keep track of the project configuration
 * as you go through the wizard
 * 
 * @author brian
 *
 */
public class ProjectConfiguration {

    public static final String PROJECT_PREFS_NODE           = "PROJECT_PREFS_NODE";
    public static final String PROCESSING_APP_PATH_KEY      = "PROCESSING_APP_PATH";
    public static final String PROCESSING_SKETCH_PATH_KEY   = "PROCESSING_SKETCH_PATH";
    
    private static final String LIB_DIR                     = "lib";
    
    private ArrayList<String> selected_libraries;
    private String project_name;
    private String app_path     = null;
    private String sketch_path  = null;
    
    private boolean isApp = false;
    private IProject project;
    
    public ProjectConfiguration() {
    }
    
    public ProjectConfiguration(IProject project) {
        this.project = project;
    }
    
    public ProjectConfiguration(
            String projectName, ArrayList<String> selectedLibraries) {
        project_name = projectName;
        selected_libraries = selectedLibraries;
    }

    public ArrayList<String> getSelectedLibraries() {
        if (selected_libraries != null)
            return selected_libraries;
        if (projectExists())  
            return getLibrariesFromProject();
        return new ArrayList<String>();
    }

    public void setSelectedLibraries(
            ArrayList<String> selectedLibraries) {
        selected_libraries = selectedLibraries;
    }

    public void setProjectName(String projectName) {
    	project_name = projectName;
    }
    
    public String getProjectName() {
        return project_name;
    }
    
    public String getProcessingAppPath() {
        if (app_path != null)
            return app_path;
        else if (projectExists())
            return getProjectPreferencesNode().get(
                    PROCESSING_APP_PATH_KEY, getDefaultProcessingAppPath());
        else
            return getDefaultProcessingAppPath();
    }
    
    public void setProcessingAppPath(String path) {
        path = path.trim();
        if (!path.endsWith(OSHelperManager.getHelper().getFileSeparator())) 
        	path +=  OSHelperManager.getHelper().getFileSeparator();
        app_path = path;
    }
     
    private void saveProcessingAppPath() throws IllegalStateException {
        if (project == null) 
            throw new IllegalStateException("Cannot save processing path " +
            		"because target project has not been set");
        
        Preferences preferences = getProjectPreferencesNode();
        preferences.put(PROCESSING_APP_PATH_KEY, app_path);
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
           LogHelper.LogError(e);
        }
    }
    
    public String getProcessingSketchPath() {
        if (sketch_path != null)
            return sketch_path;
        else if (projectExists())
            return getProjectPreferencesNode().get(
                    PROCESSING_SKETCH_PATH_KEY, getDefaultProcessingSketchPath());
        else
            return getDefaultProcessingSketchPath();
    }
    
    public void setProcessingSketchPath(String path) {
        path = path.trim();
        if (!path.endsWith(OSHelperManager.getHelper().getFileSeparator())) 
        	path +=  OSHelperManager.getHelper().getFileSeparator();
        sketch_path = path;
    }
    
    private void saveProcessingSketchPath() {
        if (project == null) 
            throw new IllegalStateException("Cannot save sketch path " +
                    "because target project has not been set");
        
        Preferences preferences = getProjectPreferencesNode();
        preferences.put(PROCESSING_SKETCH_PATH_KEY, sketch_path);
        try {
            preferences.flush();
        } catch (BackingStoreException e) {
           LogHelper.LogError(e);
        }
    }
    
    public void savePreferences(IProject project) {
        this.project = project;
        saveProcessingAppPath();
        saveProcessingSketchPath();
    }
    
    public void savePaths() {
        if (!projectExists())
            throw new IllegalStateException("Cannot save sketch path " +
            "because target project has not been set");
        saveProcessingAppPath();
        saveProcessingSketchPath();
    }
    
    public boolean isApp() {
		return isApp;
	}

	public void setApp(boolean isApp) {
		this.isApp = isApp;
	}
	
	public boolean projectExists() {
	    return project != null;
	}
	
    
    private ArrayList<String> getLibrariesFromProject() {
        ArrayList<String> libs = new ArrayList<String>();
        if (project == null) return libs;
        IFolder folder = project.getFolder(LIB_DIR);
        try {
            IResource[] members = folder.members();
            for (IResource member : members) {
                if (member instanceof IFolder) {
                    libs.add(((IFolder) member).getName());
                }
            }
        } catch (CoreException e) {
            LogHelper.LogError(e);
        }
        return libs;
    }
    
    /**
     * Add processing libs to the project & set up classpath
     * 
     * @param lib - IFolder where to move the files to
     * @param monitor
     */
    public void saveLibs() {
        if (project == null) 
            throw new IllegalStateException("Cannot save sketch path " +
                    "because target project has not been set");
        
        IProgressMonitor progMonitor = new NullProgressMonitor();
        IFolder folder = project.getFolder(LIB_DIR);
        ArrayList<String> libs = getLibrariesFromProject();
        for (String l : libs) {
            if (!selected_libraries.contains(l) 
                    && !l.equals(ProcessingProvider.CORE)) {
                IFolder deleteFolder = project.getFolder(LIB_DIR + 
                        OSHelperManager.getHelper().getFileSeparator() + l);
                try {
                    deleteFolder.delete(true, progMonitor);
                } catch (CoreException e) {
                    LogHelper.LogError(e);
                }
            }
        };
        
        ProcessingLibrary[] libraries = ProcessingProvider.getLibraries(
                getProcessingAppPath(),
                getSelectedLibraries().toArray(
                        new String[getSelectedLibraries().size()]));
        
        Vector<IClasspathEntry> classpathEntries = new Vector<IClasspathEntry>();
        for(ProcessingLibrary library : libraries) {
            // create folder containing lib based on identifier
            IFolder libFolder = 
                folder.getFolder(library.getIdentifier());
            try {
                if (!libFolder.exists())
                    libFolder.create(true, true, progMonitor);
            } catch (CoreException e1) {
                LogHelper.LogError(e1);
                continue;
            }

            // get urls to all jar, zip, + native lib files
            URL[] urls = library.getUrls();
            if (urls == null) continue;
            classpathEntries.addAll(addProcessingLibs(libFolder, urls, progMonitor));
        }
        
        try {
            setProjectClassPath(project, progMonitor, classpathEntries);
        } catch (JavaModelException e) {
            LogHelper.LogError(e);
        }
    }
    
    private void setProjectClassPath(IProject proj, IProgressMonitor monitor, 
            Vector<IClasspathEntry> classpathEntries) throws JavaModelException {
        
        IPath path  = JavaRuntime.newDefaultJREContainerPath();
        classpathEntries.add(JavaCore.newContainerEntry(path));
        
        // convert classpath_entries to IClasspathEntry[] and set classpath on java project
        JavaCore.create(proj).setRawClasspath(classpathEntries.toArray(
                        new IClasspathEntry[classpathEntries.size()]), null);
    }
    
    /**
     * Given urls to the libraries, move them to the right spot
     * then add the jars and zip files to the classpath
     * 
     * @param libFolder
     * @param libUrls
     * @param monitor
     */
    private Vector<IClasspathEntry> addProcessingLibs(
            IFolder libFolder, URL[] libUrls, IProgressMonitor monitor){
        //vars used in url loop
        String filename; IFile libFile; 
        Vector<IClasspathEntry> classpathEntries = new Vector<IClasspathEntry>();
        // go through urls, moving files into project and adding jars and zips to classpath
        for (URL url : libUrls) {
//            filename = url.getPath().substring(url.getPath().lastIndexOf(OSHelperManager.getHelper().getFileSeparator()) + 1);
//          apprently, Java uses "/" instead of the OS file seperator, even on windows.
            filename = url.getPath().substring(url.getPath().lastIndexOf('/') + 1);
            try {
                libFile = libFolder.getFile(filename);
                // extra check to prevent error
                if (!libFile.exists() && !OSHelperManager.getHelper().isExlcuded(filename))
                    libFile.create(url.openStream(), true, monitor);
                if ((filename.endsWith(".jar") || filename.endsWith(".zip")) && !OSHelperManager.getHelper().isExlcuded(filename)){
                    classpathEntries.add(
                            JavaCore.newLibraryEntry(libFile.getFullPath(), null, null, new IAccessRule[0], 
                                    new IClasspathAttribute[]{
                                            JavaCore.newClasspathAttribute(
                                                    JavaRuntime.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY, 
                                                    libFolder.getFullPath().toPortableString().substring(1))}, false));
                }
            } catch (CoreException e) {
                LogHelper.LogError(e);
            } catch (IOException e) {
                LogHelper.LogError(e);
            }
        }
        return classpathEntries;
    }
    
	
	/**
	 *  this gets the configuration in this project scope for the project's prefs
	 *  
	 * @return
	 */
	private IEclipsePreferences getProjectPreferencesNode() {
	    return new ProjectScope(project).getNode(PROJECT_PREFS_NODE);
	}
	
	private String getDefaultProcessingAppPath() {
        return  new ConfigurationScope().getNode(Activator.PLUGIN_ID).get(
                PROCESSING_APP_PATH_KEY,  OSHelperManager.getHelper().getDefaultAppPath());
	}
	
	private String getDefaultProcessingSketchPath() {
        return  new ConfigurationScope().getNode(Activator.PLUGIN_ID).get(
                PROCESSING_SKETCH_PATH_KEY,  OSHelperManager.getHelper().getDefaultSketchPath());

	}
}
