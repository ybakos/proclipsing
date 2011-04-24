package proclipsing.processingprovider;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import proclipsing.os.OS;
import proclipsing.util.LogHelper;

public class ProcessingLibrary {
    
    private String identifier;
    private String processing_path;
    
    public ProcessingLibrary(String processingPath, String identifier) {
    	this.identifier = identifier;
    	this.processing_path = processingPath;
    }
    
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     * Gets urls to all the library files (.jar, .zip, .dll, .so)
     * 
     * @return
     */
    public URL[] getUrls(boolean isContributed) {

        File realPath = new File(getResourcePath(isContributed));
        
        if (!isContributed && !isCore() && !realPath.exists()) {
        	realPath = new File(processing_path + OS.helper().getNewPathToLibrary(identifier));
        }
        
        if (!realPath.exists()) return null;

        URL[] urls = getUrls(realPath.listFiles());        
        return urls;
    }

    /**
     * given a set of files in the filesystem, 
     * return an array with urls for all the files
     * 
     * @param files
     * @return
     */
    
    private static final String[] newNativeLibDirs = new String[]{
    	"linux32",
    	"linux64",
    	"macosx",
    	"opengl.jar",
    	"windows32",
    	"windows64"};
    
    private URL[] getUrls(File[] files) {
        ArrayList<URL> urls = new ArrayList<URL>();
        ArrayList<URL> nativeLibs = new ArrayList<URL>();
        for(File file : files) {
            if (file.isDirectory()){
            	boolean isNativeLib = false;
            	for(String nativeLibDir: newNativeLibDirs){
            		isNativeLib = isNativeLib || file.getAbsolutePath().endsWith(nativeLibDir);
            	}
            	
            	if(isNativeLib){
            		for(File nativeLib: file.listFiles()){
            			try{
            				nativeLibs.add(nativeLib.toURI().toURL());	
            			} catch (MalformedURLException e) {
                            LogHelper.LogError(e);
                        }
            		}
            	} else {
            		continue;
            	}
            }
            try {
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                LogHelper.LogError(e);
            }
        }
        
        urls.addAll(nativeLibs);
        
        return urls.toArray(new URL[urls.size()]);
    }
    
    /**
     * Gets the 
     * 
     * @return
     *  the path to the current library.
     */
    private String getResourcePath(boolean isContributed) {
        if (isCore())
            return processing_path + OS.helper().getCorePath();
        else if(isContributed)
            return processing_path + OS.helper().getSketchPathToLibrary(identifier);
        else
            return processing_path + OS.helper().getPathToLibrary(identifier);
    }
    
    /**
     * Are we the processing core?
     * 
     * @return
     */
    private boolean isCore() {
        return identifier == ProcessingProvider.CORE;
    }
    
}
