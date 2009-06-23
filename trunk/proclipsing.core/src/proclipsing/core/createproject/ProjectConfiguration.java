package proclipsing.core.createproject;

import java.util.ArrayList;

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

    public String getProjectName() {
        return project_name;
    }
}
