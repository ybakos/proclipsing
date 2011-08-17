package dch.eclipse.p5Export;

import org.eclipse.core.runtime.Status;

/**
 * Real simple abstraction for logging until I can figure out how to do it
 * the right way
 * 
 * @author brian
 *
 */
public class LogHelper {
    
    /**
     * Still need to figure out how to hook into debug level in eclipse
     * 
     * @return
     */
    private static boolean isDebug() { return true; }
    
    /**
     * Log an error.
     * This will print the stack trace if in debug mode.
     * 
     * @param e
     * @param message
     */
    public static void LogError(Exception e) {
        LogError(e, e.getMessage());
    }
    
    /**
     * Log an error.
     * This will print the stack trace if in debug mode.
     * 
     * @param e
     * @param message
     */
    public static void LogError(Exception e, String message) {
//        Activator.getDefault().getLog().log(
//                new Status(Status.ERROR, Activator.PLUGIN_ID, 0, message, e));
        if (isDebug()) e.printStackTrace();
    }
    
    /**
     * Log info.
     * This will print the message if in debug mode
     * 
     * @param message
     */
    public static void LogInfo(String message) {
//        Activator.getDefault().getLog().log(
//                new Status(Status.INFO, Activator.PLUGIN_ID, message));
        if (isDebug()) System.out.println("[INFO] " + message);
    }
}
