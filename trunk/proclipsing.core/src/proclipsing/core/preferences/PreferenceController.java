package proclipsing.core.preferences;

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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.osgi.service.prefs.BackingStoreException;

import proclipsing.os.OS;
import proclipsing.processingprovider.ProcessingLibrary;
import proclipsing.processingprovider.ProcessingProvider;
import proclipsing.util.LogHelper;

public class PreferenceController {

	public static void saveToProject(IProject project,
			ProjectPreferences projectPreferences) {
		
		saveToProject(project, 
				projectPreferences, new Vector<IClasspathEntry>());	
	}
	
	public static void saveToProject(IProject project,
			ProjectPreferences projectPreferences, Vector<IClasspathEntry> classpathEntries) {
		
		IEclipsePreferences savePrefs = 
			new ProjectScope(project).getNode(ProjectPreferences.PROJECT_PREFS_NODE);
		
		saveAppPath(savePrefs, projectPreferences);
		saveSketchPath(savePrefs, projectPreferences);
		saveLibraries(project, projectPreferences);
		
		classpathEntries.addAll(
				saveLibraries(project, projectPreferences));
		
        try {
            setProjectClassPath(project, classpathEntries);
        } catch (JavaModelException e) {
            LogHelper.LogError(e);
        }	
	}
		

	public static ProjectPreferences loadFromProject(IProject project) {
		IEclipsePreferences preferences = 
			new ProjectScope(project).getNode(ProjectPreferences.PROJECT_PREFS_NODE);
		ProjectPreferences defaults = new ProjectPreferences();
		return new ProjectPreferences(
						project.getName(), 
						preferences.get(ProjectPreferences.APP_PATH_KEY, defaults.getAppPath()), 
						preferences.get(ProjectPreferences.SKETCH_PATH_KEY, defaults.getSketchPath()),
						getLibraries(project));
	}
	
	private static void saveAppPath(
			IEclipsePreferences target, ProjectPreferences prefsToSave) {
		
		String path = prefsToSave.getAppPath();
        if (!path.endsWith(OS.helper().getFileSeparator())) 
        	path +=  OS.helper().getFileSeparator();
        target.put(ProjectPreferences.APP_PATH_KEY, path);
        try {
        	target.flush();
        } catch (BackingStoreException e) {
           LogHelper.LogError(e);
        }				
	}
	
	private static void saveSketchPath(
			IEclipsePreferences target, ProjectPreferences prefsToSave) {
		
		String path = prefsToSave.getSketchPath();
        if (!path.endsWith(OS.helper().getFileSeparator())) 
        	path +=  OS.helper().getFileSeparator();
        target.put(ProjectPreferences.SKETCH_PATH_KEY, path);
        try {
        	target.flush();
        } catch (BackingStoreException e) {
           LogHelper.LogError(e);
        }				
	}		
	
	
	private static Vector<IClasspathEntry> saveLibraries(IProject project, 
					ProjectPreferences projectPreferences) {
        
        IProgressMonitor progMonitor = new NullProgressMonitor();
        IFolder folder = project.getFolder(ProjectPreferences.LIB_DIR);
        ArrayList<String> libs = getLibraries(project);
        for (String l : libs) {
            if (!projectPreferences.getLibraries().contains(l) 
                    && !l.equals(ProcessingProvider.CORE)) {
                IFolder deleteFolder = project.getFolder(ProjectPreferences.LIB_DIR + 
                        OS.helper().getFileSeparator() + l);
                try {
                    deleteFolder.delete(true, progMonitor);
                } catch (CoreException e) {
                    LogHelper.LogError(e);
                }
            }
        };
        
        ProcessingLibrary[] libraries = ProcessingProvider.getLibraries(
                projectPreferences.getAppPath(),
                projectPreferences.getLibraries().toArray(
                        new String[projectPreferences.getLibraries().size()]));
        
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
        return classpathEntries;
    }
	
    private static ArrayList<String> getLibraries(IProject project) {
        ArrayList<String> libs = new ArrayList<String>();
        if (project == null) return libs;
        IFolder folder = project.getFolder((ProjectPreferences.LIB_DIR));
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

    private static void setProjectClassPath(IProject proj,
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
    private static Vector<IClasspathEntry> addProcessingLibs(
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
                if (!libFile.exists() && !OS.helper().isExlcuded(filename))
                    libFile.create(url.openStream(), true, monitor);
                if ((filename.endsWith(".jar") || filename.endsWith(".zip")) && !OS.helper().isExlcuded(filename)){
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


}
