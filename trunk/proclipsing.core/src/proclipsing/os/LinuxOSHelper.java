package proclipsing.os;

public class LinuxOSHelper extends OSHelper {

    
    public boolean isExlcuded(String jarName){
    	boolean result = false;
    	
//    	if(jarName.toLowerCase().contains("windows") || 
//    			jarName.toLowerCase().contains("macos")){
//    		result = true;
//    	} 
    	
    	return result || super.isExlcuded(jarName);
    }
}
