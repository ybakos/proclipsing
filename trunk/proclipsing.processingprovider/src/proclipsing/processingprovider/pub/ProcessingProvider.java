package proclipsing.processingprovider.pub;

import java.util.ArrayList;

public class ProcessingProvider {
    private static final String[] ALL_LIBRARIES = {"core", "serial", "pdf", "net"};

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
        return libs.toArray(new ProcessingLibrary[0]);        
    }
    
    /**
     * Static method to get all processing libraries
     * 
     * @return
     */
    public static ProcessingLibrary[] getLibraries() {
        return getLibraries(ALL_LIBRARIES);
    }
}
