package proclipsing.processingprovider.pub;

import java.util.ArrayList;

public class ProcessingProvider {
    // This is all the libraries the framework currently supports
    // currently these haven't all been tested
    private static final String[] ALL_LIBRARIES = {"core", "dxf", "javascript", "minim", "net", "serial", "video"};

    /**
     * Static method gets all libraries in String[] passed to it
     * 
     * @param libNames
     * @return
     */
    public static ProcessingLibrary[] getLibraries(String[] libNames) {
        ArrayList<ProcessingLibrary> libs = new ArrayList<ProcessingLibrary>();
        for(String libStr : libNames) {
            libs.add(new ProcessingLibrary(libStr));
        }
        return libs.toArray(new ProcessingLibrary[libs.size()]);        
    }
    
    /**
     * Static method to get all processing libraries
     * 
     * @return
     */
    public static ProcessingLibrary[] getLibraries() {
        return getLibraries(ALL_LIBRARIES);
    }
    
    /**
     * Get an array of all the library identifiers
     * 
     * @return
     */
    public static String[] getAllLibraryIdentifiers() {
        return ALL_LIBRARIES;
    }
}
