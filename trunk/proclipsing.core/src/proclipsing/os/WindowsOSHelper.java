package proclipsing.os;

public class WindowsOSHelper extends OSHelper {

    
    public String getDefaultAppPath(){
    	return "C:\\Program Files\\Processing";
    }
    
    public String getDefaultSketchPath(){
    	return System.getProperty("user.home") + "\\My Documents\\Processing\\libraries\\";
    }
    
    public boolean isExlcuded(String jarName){
    	boolean result = false;
    	
//    	if(jarName.toLowerCase().contains("linux") || 
//    			jarName.toLowerCase().contains("macos")){
//    		result = true;
//    	} 
    	
    	return result || super.isExlcuded(jarName);
    }

}
