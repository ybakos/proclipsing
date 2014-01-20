package dch.eclipse.p5Export;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;

public class P5ClasspathBuilder
{
  private String separator;
  private IJavaProject project;
  
  private final static String CLASSPATH_ATTR_LIBRARY_PATH_ENTRY = "org.eclipse.jdt.launching.CLASSPATH_ATTR_LIBRARY_PATH_ENTRY";
  
  public P5ClasspathBuilder(String pSeparator, IJavaProject pProject)
  {
    separator = pSeparator;
    project = pProject;
  }

  /**
   * Builds the classpath for the given project
   * @throws JavaModelException 
   */
  public String buildClasspath(IClasspathEntry[] entries, StringBuilder sClasspath) throws JavaModelException 
  {
    P5ExportBuilder.nativeLibDirs = new ArrayList<String>();
    
    IPath pTransformedPath = null;
    P5ExportUtils sUtil = P5ExportUtils.getInstance();
    if (sClasspath == null)
      sClasspath = new StringBuilder();    
    if (entries == null)
      entries = project.getRawClasspath();
    
    // Loop over the classpath entries & extract the path information.
    for(IClasspathEntry i:entries)
    {
      pTransformedPath = i.getPath();
      //System.out.println(i+"    CP_ENTRY: "+i.getEntryKind());
      switch(i.getEntryKind())
      {
        
        case IClasspathEntry.CPE_SOURCE:
        {
          pTransformedPath = i.getOutputLocation();
          // Null path means to use the project default path instead of the source folder output path.
          if (pTransformedPath == null)          
            pTransformedPath = project.getOutputLocation();
          break;
        }
        // Project references (i.e. referencing the output of another project).
        case IClasspathEntry.CPE_PROJECT:
        {
          IJavaProject jTemp = sUtil.getJavaProject(i.getPath().toString());
          pTransformedPath = jTemp.getOutputLocation(); 
          break;
        }
        // User-Libs
        case IClasspathEntry.CPE_CONTAINER:
        {
          IPath path = i.getPath();                
          if (path.toString().endsWith("JRE_CONTAINER")) continue;
          IClasspathContainer icc = JavaCore.getClasspathContainer(path ,project);
          buildClasspath(icc.getClasspathEntries(),sClasspath);
          break;
        }
      }
      
      //MATT Get dirs for all Native Libs
      if(pTransformedPath.getFileExtension() != null && pTransformedPath.getFileExtension().toLowerCase().endsWith("jar")){
        IClasspathAttribute[] attrs = i.getExtraAttributes();
        for(IClasspathAttribute attr: attrs){
          if(attr.getName().equals(CLASSPATH_ATTR_LIBRARY_PATH_ENTRY) && !P5ExportBuilder.nativeLibDirs.contains(P5ExportBuilder.PROJECT_LOCATION + attr.getValue())){
            P5ExportBuilder.nativeLibDirs.add(P5ExportBuilder.PROJECT_LOCATION + attr.getValue());
          }
        }
      }
      
      pTransformedPath = makeAbsolutePath(pTransformedPath);
      sClasspath.append(pTransformedPath.toOSString());
      sClasspath.append(separator);      
    }
    return sClasspath.toString();
  }
  
  /*
   * Returns a file system absolute path given the incoming path
   *  which is either absolute or workspace relative. 
   */
  private IPath makeAbsolutePath(IPath pIncomingPath)
  {
    // Use the Util to find a resource with the given transformed path
    // (either/ workspace relative OR absolute). If the resource found is null, then it's
    // assumed that the path is absolute and do nothing further. If the resource is not null
    // AND the entry type is not a container or variable, then get the raw location.
    
    // If the type of resource obtained is a linked folder, then always set the
    // path in the wrapper to the absolute path.
    IResource rTemp = P5ExportUtils.getInstance().getResource(pIncomingPath);
    if(rTemp != null) {
      // always set the path to absolute:
      return rTemp.getLocation();                
    }
    return pIncomingPath;
  }
  
}
