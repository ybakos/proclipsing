package proclipsing.processingprovider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Filter;

import javax.imageio.stream.FileImageInputStream;

import org.eclipse.core.runtime.FileLocator;

import proclipsing.util.Util;


public class ProcessingLibrary {
    private static final String[] ALL_LIBRARIES = {"core", "dxf", "javascript", "minim", "net", "serial", "video"};
    
    private static final String CORE_PATH 		= "lib" + Util.getFileSeparator();
    private static final String LIB_MATCH_STRING= "%LIBRARY_IDENTIFIER%";
    // LIBRARY_PATH looks like libraries/%LIBRARY_IDENTIFIER%/library/
    private static final String LIBRARY_PATH 	= "libraries" + Util.getFileSeparator() + 
                                                    LIB_MATCH_STRING + Util.getFileSeparator() + 
                                                    "library" + Util.getFileSeparator();
    private static final String EXPORT_FILE 	= "export.txt";
    private static final String CORE 			= "core";
    private static final String LINUX_PLATFORM_STRING 	= "application.linux";
    private static final String WINDOWS_PLATFORM_STRING = "application.windows";
    private static final String MAC_PLATFORM_STRING 	= "application.macosx";
    
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
    public URL[] getUrls() {

        File realPath = new File(getResourcePath());
        if (!realPath.exists()) return null;
        
        // Try to read out export file.  Export file tells us which files to load
        // they don't seem to be using the export file anymore... need to check on it
        File exportFile = null; //new File(getResourcePath() + EXPORT_FILE);
        URL[] urls = null;
        if (exportFile != null && exportFile.exists()) {
        	urls = getUrls(realPath, exportFile);
        } else {
            urls = getUrls(realPath.listFiles()); 
        }
        
        return urls;
    }

    /**
     * Given a root path to the files and a line from the export file
     * get urls for all the files listed in that line
     * 
     * @param realPath
     * @param line
     * @return
     */
    private URL[] getUrls(File realPath, String line) {
        String[] keyval = line.split("=");
        if (keyval.length < 2) return null;
        String[] filenames = keyval[1].split(",");
        File file = null;
        ArrayList<URL> urls = new ArrayList<URL>();
        for (String filename : filenames) {
            file = new File(realPath, filename.trim());
            try {
                if (file != null) urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }
        
    /**
     * Given a root path to the files and an export file,
     * get the urls for the stuff the export file says to get
     * 
     * @param realPath
     * @param exportFile
     * @return
     */
    private URL[] getUrls(File realPath, File exportFile) {
    	//exportFile.get
    	URL[] urls = null;
        try {
            BufferedReader reader = 
                new BufferedReader(new FileReader(exportFile));

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith(getPlatformString()))
                    urls = getUrls(realPath, line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return urls;
    }
    
    /**
     * given a set of files in the filesystem, 
     * return an array with urls for all the files
     * 
     * @param files
     * @return
     */
    private URL[] getUrls(File[] files) {
        ArrayList<URL> urls = new ArrayList<URL>();
        for(File file : files) {
            if (file.isDirectory()) continue;
            try {
                urls.add(file.toURI().toURL());
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return urls.toArray(new URL[urls.size()]);
    }

   
    private String getPlatformString() {
        if (Util.isPC()) {
          return WINDOWS_PLATFORM_STRING;
        } else if (Util.isLinux()) {
          return LINUX_PLATFORM_STRING;
        } else if (Util.isMac()) {
          return MAC_PLATFORM_STRING;
        }
        // default to linux?
        return LINUX_PLATFORM_STRING;
    }
    
    private String getResourcePath() {
        if (identifier == CORE)
            return processing_path + CORE_PATH;
        else 
            return processing_path + LIBRARY_PATH.replaceAll(LIB_MATCH_STRING, identifier);
    }
    
    
}
