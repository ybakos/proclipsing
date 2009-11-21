package dch.eclipse.p5Export;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import processing.app.Base;
import processing.app.Preferences;

public class P5ExportUtils
{
  public static final String platforms[] = {
    "other","windows","macos9","macosx","linux"
  };  // dont change order (PConstants silliness)  

  private IJavaModel javaModel;
  private static P5ExportUtils instance;
  private IWorkspaceRoot workspace;

  public static P5ExportUtils getInstance()
  {
    if (instance==null)
      instance = new P5ExportUtils();
    return instance;
  }
  
  private P5ExportUtils()
  {
    workspace = ResourcesPlugin.getWorkspace().getRoot();
    javaModel = JavaCore.create(workspace);
  }
  
  /**
   * Returns the JavaModel associated with the workspace.
   * @return
   */
  public IJavaProject getJavaProject(String pProjectName)
  {
    return javaModel.getJavaProject(pProjectName);
  }
  
  /**
   * Returns a list of launch configurations for a given java project. 
   * @param pProject The desired java project to query the launch configurations.
   */
  public List getLaunchConfigurations(IJavaProject pProject) throws CoreException
  {
    List aList = new ArrayList();
    ILaunchConfiguration[] launches = DebugPlugin.getDefault()
        .getLaunchManager().getLaunchConfigurations();

    for (ILaunchConfiguration i : launches)
    {
      String sProjectName = i.getAttribute(
          IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
      //Null category seems to mean only those that are runtime configurations
      //and not those like ant build.xml targets.
      if (sProjectName.equalsIgnoreCase(pProject.getElementName()) && i.getCategory() == null) {
        aList.add(i);
      }
      //System.out.println(i.getName());
      //System.out.println(i.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME,""));
    }
    return aList;
  }

  /**
   * Returns a list of the java projects associated with the current workspace.
   * @return
   */
  public IJavaProject[] getProjects()
  {
    IJavaProject[] javaProjects;
    try
    {
      javaProjects = javaModel.getJavaProjects();     
    }
    catch(JavaModelException ee)
    {
      javaProjects = new IJavaProject[0];
    }
    return javaProjects;
  }
/*  
  public IJavaProject getLastModifiedProject() {
    int newestIdx = -1;
    long newestTs = Long.MIN_VALUE;
    IJavaProject[] javaProjects = getProjects();
    for (int i = 0; i < javaProjects.length; i++)
    {
      long ts = javaProjects[i].getProject().getLocalTimeStamp();
      System.err.println("  "+javaProjects[i].getProject().getName()+" : "+ts);
      if (ts > newestTs) {
        newestTs = ts; 
        newestIdx = i;
      }
    }
    return javaProjects[newestIdx];
  }*/
  
  public IResource getResource(IPath pPath)
  {
    return workspace.findMember(pPath);
  }  
  
  // -------------------------- statics ---------------------------------
  
  public static boolean deleteDir(File path) {
    if( path.exists() ) {
      File[] files = path.listFiles();
      for(int i=0; i<files.length; i++) {
         if(files[i].isDirectory()) {
           deleteDir(files[i]);
         }
         else {
           files[i].delete();
         }
      }
    }
    boolean ok = path.delete();
    if (!ok) System.err.println("[WARN] Unable to delete: "+path);
    return ok;
  }
  
  public static void errorDialog(final Shell shell, final String message, final Throwable e)
  {
    shell.getDisplay().syncExec(new Runnable() {
      public void run() 
      {
        String msg = message;
        if (e != null)
          msg += "\nException: "+e.getMessage()+" :: "+e; 
        
        ErrorDialog.openError(shell, P5ExportPlugin.NAME+" Error", "Unable to complete export", 
          new Status(IStatus.ERROR, P5ExportPlugin.ID, IStatus.OK, message, null));
      
        P5ExportPlugin.toConsole(msg, e);
      }
    });
  }
  
  /**
   * Displays  a message window w/ an area for code that can be selected & copied.
   * 
   * @param shell
   * @param heading
   * @param code
   */
  public static void messageWithCode(final Shell shell, final String heading, final String code)
  {
      shell.getDisplay().asyncExec(new Runnable() {
          public void run() {
              new MessageDialog(shell, P5ExportPlugin.NAME+" Error", 
                      null, "", MessageDialog.NONE, new String[]{"OK"}, 0) {
                  
                  @Override
                  protected Control createMessageArea(Composite composite) {
                      Label label = new Label(composite, SWT.NONE);              
                      label.setText(heading);
                      GridDataFactory
                          .fillDefaults()
                          .minSize(600, 600) 
                          .align(SWT.FILL, SWT.BEGINNING)
                          .grab(true, false).span(2, 1)
                          .applyTo(label);
                      Text multiText = new Text(composite, SWT.MULTI | SWT.READ_ONLY);
                      multiText.setBackground(label.getBackground());
                      multiText.setText(code);
                      GridDataFactory
                          .fillDefaults()
                          .minSize(600, 600)
                          .align(SWT.FILL, SWT.BEGINNING)
                          .grab(true, false)
                          .applyTo(multiText);
                      return composite;
                  }
              }.open();
              P5ExportPlugin.toConsole(heading+code, null);
          }
          
      });
  }
  
  /**
   * Returns a String representation of Exception + stacktrace
   */  
  public static String exceptionToString(Throwable e) {
    return exceptionToString(e, false);
  }
  
  /**
   * Returns a String rep. of Exception type + stacktrace
   * @param throwable
   * @param miniStack - if true, returns only stacktrace elements w line numbers
   */
  public static String exceptionToString(Throwable throwable, boolean miniStack) {
    if (throwable == null) return "null";
    StringBuffer s = new StringBuffer(throwable+"\n");
    StackTraceElement[] stes = throwable.getStackTrace();
    for (int i = 0; i < stes.length; i++)
    {
      String ste = stes[i].toString();
      if (!miniStack || ste.matches(".*[0-9]+\\)"))
        s.append("    " +ste+ "\n" );
    }
    return s.toString();
  }
  
  public static void writeJarResourcesAsFiles(Class resourceDir, String resourceName, File writeDir)  throws Exception
  {
if(P5ExportBuilder.DBUG)System.err.println("P5ExportUtils.writeJarResourcesAsFiles("+resourceDir+","+resourceName+","+writeDir+")");
 
    // make the directory if needed...
    if (!writeDir.exists()) writeDir.mkdirs();

    int bufferSize = 2048;
    InputStream is = openStream(resourceDir, resourceName);
    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is));
    
    ZipEntry entry; // loop thorugh the zip
    while ((entry = zis.getNextEntry()) != null)
    {
      int count;
      byte data[] = new byte[bufferSize];
      
      File f = new File(writeDir, entry.getName());
      
      // create the dir if it doesnt exist
      if (entry.isDirectory()) 
      {        
        if (!f.exists()) {
if(P5ExportBuilder.DBUG)System.out.println("Creating dir: "+f);
          if (!f.mkdir()) 
            System.err.println("ERROR: Unable to make directory: "+f);            
        }
        continue;
      }
if(P5ExportBuilder.DBUG)System.out.println("Checking for file: "+f);

      if (f.exists()) continue;

if(P5ExportBuilder.DBUG)System.out.println("Creating file: "+f);

      // write the files to the disk
      FileOutputStream fos = new FileOutputStream(f);        
      BufferedOutputStream dest = new BufferedOutputStream(fos, bufferSize);
      while ((count = zis.read(data, 0, bufferSize)) != -1)
      {
        dest.write(data, 0, count);
      }
      dest.flush();
      dest.close();
    }
    zis.close();
  }

  public static void writeFile(File src, File dst) throws IOException
  {   
    File parent = dst.getParentFile();
    if (!parent.exists()) parent.mkdirs();
    byte[] bytes = loadBytes(new FileInputStream(src));              
    writeBytes(dst, bytes);
    if (!dst.exists())
      throw new RuntimeException("UNABLE TO WRITE FILE: "+dst.getAbsolutePath());
  } 
  
  public static void writeBytes(File file, byte[] bytes) throws IOException {
    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
    bos.write(bytes);
    bos.flush();
    bos.close();
  }
  
  public static String loadFile(InputStream is) throws Exception
  {
    StringBuffer sb = new StringBuffer();
    BufferedInputStream bis = new BufferedInputStream(is);
    int c;
    while ((c = bis.read()) != -1)
      sb.append((char)c);
    bis.close();
    return sb.toString();
  }
  
  public static byte[] loadBytes(InputStream input) throws IOException {
    BufferedInputStream bis = new BufferedInputStream(input);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int c = bis.read();
    while (c != -1) {
      out.write(c);
      c = bis.read();
    }
    return out.toByteArray();
  }
  
  public static void writeFile(File file, String contents) throws IOException
  {
    BufferedWriter writer = new BufferedWriter(new FileWriter(file));
    writer.write(contents);
    writer.flush();
    writer.close();
    writer = null;
  }
  
  public static URL getResourceURL(Class loc, String file) throws P5ExportException 
  {
    try {     
      return loc.getResource(file);      
    }
    catch (Exception e) {     
      throw new P5ExportException("in getResourceURL()", e);
    }
  }
  
  public static InputStream openStream(Class loc, String file) throws P5ExportException 
  {
    try {
      URL url = getResourceURL(loc, file);
      if (url != null) 
        return url.openStream();
      else 
        throw new P5ExportException("Unable to load file: "+file);
    }
    catch (Exception e) {     
      throw new P5ExportException("in openStream("+loc+","+file+")", e);
    }
  }
  
  public static void openFolderOld(File file) {
    try {
      String folder = file.getAbsolutePath();
      if (Base.isWindows()) {
        Runtime.getRuntime().exec("explorer \"" + folder + "\"");
      } 
      else if (Base.isMacOS()) {
        String launcher = Preferences.get("launcher.macosx");
        if (launcher != null) {
          Runtime.getRuntime().exec(new String[] { launcher, folder });
        }
      } 
      else if (Base.isLinux()) {
        String launcher = Preferences.get("launcher.linux");
        if (launcher != null) {
          Runtime.getRuntime().exec(new String[] { launcher, folder });
        }
      }
    } 
    catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public static void openMacURL(String url) {
    if (!url.startsWith("http://")) {
      url = "file://" + url;

      // replace spaces with %20 for the url
      // can't just use URLEncoder, since that 
      // makes slashes into %2F characters, which is no good. 
      if (url.indexOf(' ') != -1) {
        StringBuffer sb = new StringBuffer();
        char c[] = url.toCharArray();
        for (int i = 0; i < c.length; i++) {
          if (c[i] == ' ') {
            sb.append("%20");
          } else {
            sb.append(c[i]);
          }
        }
        url = sb.toString();
      }
    }
    
    Class fMan = null;
    
    try
    {
      fMan = Class.forName("com.apple.eio.FileManager");
    }
    catch (Exception e)
    {
      System.err.println("[WARN] unable to load class: com.apple.eio.FileManager");
      return;
    }
    
    try
    {
      Method m = fMan.getMethod( "openURL", new Class[] {String.class});
      m.invoke(null, new Object[]{url});
    }
    catch (Exception e)
    {
      System.err.println("[WARN] unable to invoke method: com.apple.eio.FileManager.openURL()");
    }
  }


  public static void openFolder(File file) throws P5ExportException {
    boolean error = false;
    String launcher = Preferences.get("launcher");
    String folder = file.getAbsolutePath();
    if (launcher != null) {
      try {
        Runtime.getRuntime().exec(new String[] { launcher, folder });       
      }
      catch (IOException e) {
        System.err.println(e.getMessage());
        error = true;      
      }
    }
    else if (Base.isMacOS()) {
      openMacURL(folder);
    }
    else if (Base.isWindows()) { // launcher == null
      try {
        Runtime.getRuntime().exec("explorer \"" + folder + "\"");       
      }
      catch (IOException e) {
        System.err.println(e.getMessage());
        error = true;      
      }
    }
    else {
      error = true;
    }
    if (error) {
      throw new P5ExportException( 
          "Unspecified platform, no launcher available.\n" + 
          "To enable opening URLs or folders, add a \n" +
          "\"launcher=/path/to/app\" line to preferences.txt");
    }
  }
  
  public static void copy(File source, File dest) throws IOException {
    FileChannel in = null, out = null;
    try {          
         in = new FileInputStream(source).getChannel();
         out = new FileOutputStream(dest).getChannel();

         long size = in.size();
         MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
         out.write(buf);
    } finally {
         if (in != null) in.close();
         if (out != null) out.close();
    }
  }
  
}// end
