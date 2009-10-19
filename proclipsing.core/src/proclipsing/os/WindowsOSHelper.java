package proclipsing.os;

public class WindowsOSHelper extends OSHelper {

    
    public String getDefaultAppPath(){
    	return "C:\\Program Files\\Processing";
    }
    
    public String getDefaultSketchPath(){
    	return System.getProperty("user.home") + "\\My Documents\\Processing\\libraries\\";
    }

}
