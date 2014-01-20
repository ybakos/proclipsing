package dch.eclipse.p5Export;

import java.io.*;
import java.util.*;

import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Shell;

import processing.app.Base;
import processing.app.Preferences;
import processing.core.PApplet;

public class P5ExportBuilder
{
  public static final boolean DBUG = false;

  private static final String P5_PREFS_FILE = "p5eprefs.txt";
  private static final String P5_EXPORTS_JAR = "p5Exports.jar";
  static final String P5_EXPORTS_GL_JAR = "p5ExportsGL.jar";

  private static final String PREFS_FILE = "preferences.txt";

  private static String SLASH, MAIN_PATT = "(?:.*)static[^v]+void\\s+main\\s*\\(\\s*String[^\\[]*\\[\\][^\\]]*[^\\)]*\\).*";

  protected Hashtable prefsHash = new Hashtable();

  Shell shell;
  File workDir;
  P5ExportType exportType;

  public static String PROJECT_LOCATION;
  String projectLocation;
  String projectClassPath;
  String mainClass;
  String vmArgs = "";
  String progArgs = "";
  String srcPath;
  String relPathToJava;
  String fullPathToJava;
  String sketchName;
  String mainProgram;
  public static List<String> nativeLibDirs;

  boolean hasMain;

  public P5ExportBuilder(Shell shell, IJavaProject pProject, P5ExportType type) throws Exception
  {
    if (DBUG)
      System.out.println("P5ExportBuilder.P5ExportBuilder()");

    this.shell = shell;
    this.exportType = type;
    SLASH = File.separator;
    this.projectLocation = pProject.getProject().getLocation().toOSString();
    PROJECT_LOCATION = projectLocation.substring(0, projectLocation.lastIndexOf('/') + 1);
    
    P5ClasspathBuilder cpb = new P5ClasspathBuilder(File.pathSeparator, pProject);
    this.projectClassPath = cpb.buildClasspath(null, null);
    this.workDir = new File(projectLocation);
    

    if (DBUG){
      System.out.println("Collected Native Lib Dirs:");
      for (String s : nativeLibDirs)
      {
        System.out.println(s);
        
      }
    }

    if (DBUG)
      System.out.println("Checking lib dir ----------------------------------------");

    File libDir = new File(System.getProperty("user.dir"), "lib");
    if (libDir.exists())
    {

      if (DBUG)
        System.out.println("Found lib dir... checking prefs file");

      // check for our prefs file
      File verFile = new File(libDir, P5_PREFS_FILE);
      // if it's not there, recreate the directory & add it
//      if (!verFile.exists())
//      {

        if (DBUG)
          System.out.println("No prefs file... lets delete and recreate");
        P5ExportUtils.deleteDir(libDir);
        if (DBUG)
          System.out.println("deleted lib dir...");
        libDir.mkdir();
        if (DBUG)
          System.out.println("created lib dir...");
        P5ExportUtils.writeFile(verFile, "p5e.version=" + P5ExportPlugin.VERSION);
        if (DBUG)
          System.out.println("writing file..." + verFile);
//      }
    }

    // write export resources to "user.dir" as needed
    P5ExportUtils.writeJarResourcesAsFiles(getClass(), P5_EXPORTS_JAR, libDir);

    this.initPreferences(workDir, getClass());
  }

  public File doExport(final ILaunchConfiguration pConfiguration, List lFiles) throws Exception
  {
    mainClass = pConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "");

    srcPath = pConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_SOURCE_PATH, "");

    srcPath = pConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_SOURCE_PATH, srcPath);

    if (srcPath.length() == 0)
      srcPath = projectLocation + SLASH + "src" + SLASH;

    String vmArgsAttr = pConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, "");
    if (vmArgsAttr != null)
      vmArgs = vmArgsAttr;

    String progArgsAttr = pConfiguration.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");
    if (progArgsAttr != null)
      progArgs = progArgsAttr;

    sketchName = mainClass.substring(mainClass.lastIndexOf(".") + 1);
    relPathToJava = mainClass.replaceAll("\\.", "\\" + SLASH) + ".java";
    fullPathToJava = srcPath + relPathToJava;

    // Get the program contents as a String
    mainProgram = P5ExportUtils.loadFile(new FileInputStream(fullPathToJava));
    hasMain = Regex.getInstance().test(MAIN_PATT, mainProgram);

    // dumpVars();

    return exportType.doExport(this, pConfiguration, workDir, lFiles);
  }

  private void dumpVars()
  {
    System.err.println("SRC=" + srcPath);
    System.err.println("MAIN=" + mainClass);
    System.err.println("SKETCH=" + sketchName);
    System.err.println("VM_ARGS=" + vmArgs);
    System.err.println("PROG_ARGS=" + progArgs);
    System.err.println("HAS_MAIN=" + hasMain);
    System.err.println("CLASSPATH=" + projectClassPath);
  }

  private void initPreferences(File workDir, Class resourceDir) throws Exception
  {
    // System.out.println("P5ExportBuilder.initPreferences("+workDir+", "+resourceDir+")");
    File prefs = new File(workDir, PREFS_FILE);
    if (!prefs.exists())
    { // try the jar
      File jarPrefs = new File("lib", PREFS_FILE);
      Base.copyFile(jarPrefs, prefs);
      if (!prefs.exists())
        throw new RuntimeException("Unable to locate preference file");

      // TODO: figure why this writes to the user-dir in eclipse
    }
    loadPrefsData(new FileInputStream(prefs));

    // check for platform-specific properties in the defaults
    String platformExt = "." + P5ExportUtils.platforms[PApplet.platform];
    int platformExtLength = platformExt.length();
    Enumeration e = prefsHash.keys();
    while (e.hasMoreElements())
    {
      String key = (String) e.nextElement();
      if (key.endsWith(platformExt))
      {
        // this is a key specific to a particular platform
        key = key.substring(0, key.length() - platformExtLength);
      }
      String value = (String) prefsHash.get(key);
      if (key != null && value != null) // fix to bug from p5 list (?)
        prefsHash.put(key, value);
      // System.err.println("PREF: "+key+"="+value);
    }
    for (Iterator iter = prefsHash.keySet().iterator(); iter.hasNext();)
    {
      String key = (String) iter.next();
      Preferences.set(key, (String) prefsHash.get(key));
    }

    // custom prefs
    // Preferences.set("preproc.jdk_version", "1.1");
    // Preferences.set("run.options.memory.maximum", "384");
    // Preferences.set("export.applet.separate_jar_files","true");
  }

  private void loadPrefsData(InputStream input) throws IOException
  {
    BufferedReader reader = new BufferedReader(new InputStreamReader(input, "UTF-8"));

    // table = new Hashtable();
    String line = null;
    while ((line = reader.readLine()) != null)
    {
      if ((line.length() == 0) || (line.charAt(0) == '#'))
        continue;

      // this won't properly handle = signs being in the text
      int equals = line.indexOf('=');
      if (equals != -1)
      {
        String key = line.substring(0, equals).trim();
        String value = line.substring(equals + 1).trim();
        // System.err.println(key + "=" + value);
        prefsHash.put(key, value);
      }
    }
    reader.close();
  }

  public static void main(String[] args) throws Exception
  {
    String s = " \n size( 200, 300, OPENGL); \n\n static public void main(String args[]) \n\n{     \nPApplet.main(new String[] { \"acd123.test.Test\" }); }\"; }";
    Regex re = Regex.getInstance();
  }
}
