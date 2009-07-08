package proclipsing.os;

public abstract class OSHelper {
    private   final String CORE_PATH 		= "lib" + getFileSeparator();
    protected final String LIB_MATCH_STRING	= "%LIBRARY_IDENTIFIER%";
    protected final String LIBRARY_END_PATH = "libraries" + getFileSeparator() + LIB_MATCH_STRING + getFileSeparator() + "library" + getFileSeparator();
	
	public String getFileSeparator() {
	    String separator =  System.getProperty("file.separator");
	    // extra special precautionary tactics
	    if (separator == null || separator.length() == 0) separator = "/";
	    return separator;
	}
	
	public String getLibraryPath(String library) {
		return LIBRARY_END_PATH.replaceAll(LIB_MATCH_STRING, library);
	}

	public String getCorePath() {
		return CORE_PATH;
	}
	
}
