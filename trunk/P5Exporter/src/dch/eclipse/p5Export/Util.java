package dch.eclipse.p5Export;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;




public class Util {


	public static String strToCamelCase(String projName){
		
		
		String result = "";
		
		char[] chars = projName.toCharArray();
		
		boolean capThis = true;
		
		for(int x = 0; x < chars.length; x++){
			char c = chars[x];
			if(((c >='a') && (c <= 'z'))|| ((c >='A') && (c <= 'Z')) ||  ((c >='0') && (c <= '9'))){
				
				if(capThis){
					result += new String(c + "").toUpperCase();
					capThis = false;
				}else
					result += c;
			} else {
				capThis = true;
			}
			
		}
		
		return result;
	}
	
	public static String projNametoPackage(String projName){
		
		return strToCamelCase(projName).toLowerCase();
	}
	
	public static String replaceAllSubString(String str, String find, String replace){
		
		int i = str.indexOf(find);
		
		while(i > 0){
			 str =  str.substring(0, i) + replace + str.substring(i + find.length());
			 i = str.indexOf(find);
		}
		
		return str;
	}

	public static String replaceSubString(String str, String find, String replace){
		
		int i = str.indexOf(find);
		
		if(i > 0){
			 return str.substring(0, i) + replace + str.substring(i + find.length());
		}
		
		return str;
	}
	
	public static String convertFileToString(String fileName) {
	    File file = new File(fileName);
	    
	    try{
	    	FileInputStream fis = new FileInputStream(file);
	    	
	    	return convertStreamToString(fis);
	    } catch (Exception e) {
	    	LogHelper.LogError(e);
		}
	    
	    return null;
	}
	
	public static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the
		 * BufferedReader.readLine() method. We iterate until the BufferedReader
		 * return null which means there's no more data to read. Each line will
		 * appended to a StringBuilder and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			LogHelper.LogError(e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				LogHelper.LogError(e);
			}
		}

		return sb.toString();
	}
	
}
