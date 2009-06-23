package proclipsing.processingprovider.pub;

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

import proclipsing.processingprovider.Activator;

public class ProcessingLibrary {
    
    private static final String CORE_PATH 		= "processing/";
    private static final String LIBRARY_PATH 	= "processing/libraries/%LIBRARY_IDENTIFIER%/library/";
    private static final String LIB_MATCH_STRING= "%LIBRARY_IDENTIFIER%";
    private static final String EXPORT_FILE 	= "export.txt";
    private static final String CORE 			= "core";
    private static final String LINUX_PLATFORM_STRING 	= "application.linux";
    private static final String WINDOWS_PLATFORM_STRING = "application.windows";
    private static final String MAC_PLATFORM_STRING 	= "application.macosx";
    
    private String identifier;
    
    public ProcessingLibrary(String identifier) {
    	this.identifier = identifier;
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
    	// start with the path to the files for this library
        URL resourceURL = 
        	Activator.getDefault().getBundle().getResource(getResourcePath());
        URL fileURL = null;
        try {
            fileURL = FileLocator.toFileURL(resourceURL);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if (fileURL == null) return null;
        File realPath = new File(fileURL.getPath());
        
        // Try to read out export file.  Export file tells us which files to load
        File exportFile = new File(fileURL.getPath() + EXPORT_FILE);
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
        if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
          return WINDOWS_PLATFORM_STRING;
        } else if (System.getProperty("os.name").toLowerCase().indexOf("linux") > -1) {
          return LINUX_PLATFORM_STRING;
        } else if (System.getProperty("os.name").toLowerCase().indexOf("mac") > -1) {
          return MAC_PLATFORM_STRING;
        }
        // default to linux?
        return LINUX_PLATFORM_STRING;
    }
    
    private String getResourcePath() {
        if (identifier == CORE)
            return CORE_PATH;
        else 
            return LIBRARY_PATH.replaceAll(LIB_MATCH_STRING, identifier);
    }
    
    
}
