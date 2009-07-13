package proclipsing.os;

public class MacOSHelper extends OSHelper {
	
	private static final String PATH_TO_JAVA = "Contents/Resources/Java/";
	
	@Override
	public String getCorePath() {
        return PATH_TO_JAVA;
	}

	@Override
	public String getPathToLibrary(String library) {
		return PATH_TO_JAVA + super.getPathToLibrary(library);
	}

}
