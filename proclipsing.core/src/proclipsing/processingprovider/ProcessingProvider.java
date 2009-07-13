package proclipsing.processingprovider;

import java.util.ArrayList;

public class ProcessingProvider {
    
    // core should always be gotten.. will handle this better later
    public static String CORE = "core";

    /**
     * Static method gets all libraries in String[] passed to it
     * 
     * @param libNames
     * @return
     */
    public static ProcessingLibrary[] getLibraries(String processingPath, String[] libNames) {
        ArrayList<ProcessingLibrary> libs = new ArrayList<ProcessingLibrary>();
        // first add core
        libs.add(new ProcessingLibrary(processingPath, CORE));
        for(String libStr : libNames) {
            libs.add(new ProcessingLibrary(processingPath, libStr));
        }
        return libs.toArray(new ProcessingLibrary[libs.size()]);        
    }
    
    
    /**
     * Static method to get all processing libraries
     * 
     * @return
     */
    //public static ProcessingLibrary[] getLibraries(String processingPath) {
    //    return getLibraries(processingPath, ALL_LIBRARIES);
    //}
    
    /**
     * Get an array of all the library identifiers
     * 
     * @return
     */
    //public static String[] getAllLibraryIdentifiers() {
    //    return ALL_LIBRARIES;
    //}
}
