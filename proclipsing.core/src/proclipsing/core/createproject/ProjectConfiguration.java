package proclipsing.core.createproject;

import java.util.ArrayList;

/**
 * Simple class to keep track of the project configuration
 * as you go through the wizard
 * 
 * @author brian
 *
 */
public class ProjectConfiguration {
    private ArrayList<String> selected_libraries;
    private String project_name;
    
    
    public ProjectConfiguration() {}
    
    public ProjectConfiguration(
            String projectName, ArrayList<String> selectedLibraries) {
        project_name = projectName;
        selected_libraries = selectedLibraries;
    }

    public ArrayList<String> getSelectedLibraries() {
        return selected_libraries;
    }
    
    public void setSelectedLibraries(
            ArrayList<String> selectedLibraries) {
        selected_libraries = selectedLibraries;
    }

    public void setProjectName(String projectName) {
    	project_name = projectName;
    }
    
    public String getProjectName() {
        return project_name;
    }
}
