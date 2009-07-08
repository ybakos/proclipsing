package proclipsing.os;

public class OSHelperManager {
	
	private static OSHelper helper;
	
	/**
	 * Get a platform specific version of OSHelper
	 * Keep the instance around and re-use it after it's created
	 * 
	 * @return
	 */
	public static synchronized OSHelper getHelper() {
		if (helper == null) {
			synchronized (helper) {
				if (helper == null)
					helper = createHelper();
			}
		}
		return helper;
	}
	
	private static OSHelper createHelper() {
		if (isWindows())
			return new WindowsOSHelper();
		if (isMac())
			return new MacOSHelper();
		if (isLinux())
			return new LinuxOSHelper();
		
		throw new RuntimeException(
				"Your Platform " + System.getProperty("os.name") + " is not supported.");
	}
	
	private static boolean isWindows() {
	    return System.getProperty("os.name").toLowerCase().indexOf("windows") > -1;
	}
	
	private static boolean isMac() {
	    return System.getProperty("os.name").toLowerCase().indexOf("mac") > -1;
	}
	
	private static boolean isLinux () {
	    return System.getProperty("os.name").toLowerCase().indexOf("linux") > -1;
	}
}
