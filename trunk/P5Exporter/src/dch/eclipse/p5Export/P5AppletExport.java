package dch.eclipse.p5Export;

import java.io.*;
import java.util.*;

import org.eclipse.debug.core.ILaunchConfiguration;

import processing.app.Base;
import processing.app.Preferences;
import processing.core.PApplet;

public class P5AppletExport extends P5ExportType
{  
  public P5AppletExport() 
  { 
    this.isApplet = true; 
  }  

  public File doExport(P5ExportBuilder builder, ILaunchConfiguration pConfiguration, File workDir, List lFiles) throws Exception
  {
    boolean success = false;
    File appletFolder = new File(workDir, "applet"); 
    Preferences.set("build.path", appletFolder.toString());
    success = export(builder);
    if (success) 
    {       
      lFiles.add(appletFolder);
      return appletFolder;
    }
    else  
      throw new P5ExportException("Unable to complete applet export");
  } 
    
  protected boolean export(P5ExportBuilder builder) throws Exception 
  {
//System.err.println("P5AppletExport.export("+builder+")");

    String sketchName = builder.sketchName;
    this.mainClass  = builder.mainClass;
    String classpath = builder.projectClassPath;
    File javaSrc = new File(builder.fullPathToJava);       
    
    File appletFolder = new File(builder.workDir,"applet");
    if (!appletFolder.exists()) appletFolder.mkdir();
               
    File tmpFolder = new File(appletFolder.getParentFile(), "tmp"); 
    if (!tmpFolder.exists()) tmpFolder.mkdir();

    writePdeFile(builder.mainProgram, sketchName, tmpFolder);
        
    FilenameFilter javaFilter = new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".java");
    }};
    
    // Write all the java files to the appletDir
    File srcFolder = javaSrc.getParentFile();
    File[] files = srcFolder.listFiles(javaFilter);
    //System.err.println("FILES="+Arrays.asList(files));
    for (int i = 0; i < files.length; i++)
    {
      //File javaFile = ;
      File newFile = new File(appletFolder, files[i].getName());
      P5ExportUtils.writeFile(files[i], newFile);
    }

    // parse the size call arguments
    String renderer = "";
    int wide = PApplet.DEFAULT_WIDTH;
    int high = PApplet.DEFAULT_HEIGHT;    
    String[] params = parseSizeArgs(builder.mainProgram);
    if (params != null && params.length==3) {
      wide = Integer.parseInt(params[0]);
      high = Integer.parseInt(params[1]);
      renderer = params[2];
    }

    
    // determine whether to use one jar file or several  (GL or user-specified)
    boolean separateJarsForGL = renderer.equals(OPENGL) || renderer.equals(GLGRAPHICS) || forceMultipleJars; 
    
    //System.err.println("SEPARATE-JAR="+separateJarsForGL+" forced="+forceMultipleJars+" RENDERER: "+renderer);      
    
    StringBuffer dbuffer = new StringBuffer();
    String lines[] = PApplet.split(builder.mainProgram, '\n');
    for (int i = 0; i < lines.length; i++) {
      if (lines[i].trim().startsWith("/**")) {  // our comment     
        for (int j = i+1; j < lines.length; j++) {
          if (lines[j].trim().endsWith("*/")) {
            break;
          }
          int offset = 0;
          while ((offset < lines[j].length()) &&
                 ((lines[j].charAt(offset) == '*') ||
                  (lines[j].charAt(offset) == ' '))) {
            offset++;
          }
          // insert the return into the html to help w/ line breaks
          dbuffer.append(lines[j].substring(offset) + "\n");
        }
      }
    }
    String description = dbuffer.toString();
    StringBuffer sources = new StringBuffer(); 
    
    // no pde for now // sources.append("<a href=\"" + pdeName + "\">" + pdeName + "</a> ");
    
    // ADD ALL OTHER JAVA FILES IN THE DIRECTORY    
    for (int i = 0; i < files.length; i++) {
      if (files[i].equals(sketchName+".java")) continue; // skip the pde ??
      sources.append("<a href=\"" + files[i].getName()
        + "\">" + files[i].getName()+ "</a> ");
    }

    // copy the loading gif to the applet dir
    String LOADING_IMAGE = "loading.gif";
    File loadingImage = new File(appletFolder, LOADING_IMAGE);
    if (!loadingImage.exists()) { 
      loadingImage = new File("lib/export", LOADING_IMAGE);     
      if (!loadingImage.exists())
        throw new P5ExportException("Unable to create source file for copy: "+loadingImage+" cwd: "+System.getProperty("user.dir"));
      File tmp = new File(appletFolder, LOADING_IMAGE);
      Base.copyFile(loadingImage, tmp);
    }
    
    // if openGl, copy the natives to the applet dir     (from where?!)
    if (separateJarsForGL) {
      writeGlExportsToLibGL();      
      for (int i = 0; i < GL_NATIVE_JARS.length; i++) {
        File nativeLib = new File(appletFolder, GL_NATIVE_JARS[i]);
        if (!nativeLib.exists()) {
          nativeLib = new File(libGl, GL_NATIVE_JARS[i]);
          if (!nativeLib.exists()) 
            throw new P5ExportException("Unable to locate native library: "+
              GL_NATIVE_JARS[i]+", in "+libGl.getPath());
          Base.copyFile(nativeLib, new File(appletFolder, GL_NATIVE_JARS[i]));
        }
      }
    }
    
    // Write the main zip (copy jars to lib if necessary), & return the classpath 
    Vector classpathList = null;
    try {
      classpathList = createArchive(sketchName, mainClass, classpath, 
        appletFolder, appletFolder, separateJarsForGL, PApplet.platform, false);
    } catch (IOException e) {
      throw new P5ExportException(e);
    }
    
    // Determine the archive path for the html page
    String jarList[] = new String[classpathList.size()];        
    classpathList.copyInto(jarList);
    StringBuffer exportClassPath = new StringBuffer();
    
    //System.err.println("RENDERER: "+renderer+" separateJar="+separateJarsForGL+"\n  JAR_LIST: "+classpathList);
    
    for (int i = 0; i < jarList.length; i++) {
      if (separateJarsForGL && jarList[i].indexOf("natives") != -1) 
        continue;         
      exportClassPath.append(jarList[i]);
      exportClassPath.append(","); 
    }
    String multiCp = exportClassPath.toString();
    if (multiCp.endsWith(","))
       multiCp = multiCp.substring(0,multiCp.length()-1);
    String cp = !separateJarsForGL ? sketchName+".jar" : multiCp;
    
    // convert the applet template
    // @@sketch@@, @@width@@, @@height@@, @@archive@@, @@source@@
    // and now @@description@@

    File htmlOutputFile = new File(appletFolder, "index.html");
    FileOutputStream fos = new FileOutputStream(htmlOutputFile);
    // UTF-8 fixes http://dev.processing.org/bugs/show_bug.cgi?id=474
    PrintStream ps = new PrintStream(fos, false, "UTF-8");

    InputStream is = null;
    // if there is an applet.html file in the sketch folder, use that
    File customHtml = new File(appletFolder.getParentFile(), "applet.html");
    if (customHtml.exists()) {
      is = new FileInputStream(customHtml);
    }
    if (is == null) {
      if (renderer.equals("OPENGL") || forceMultipleJars) {
        is = Base.getLibStream("export/applet-opengl.html");
      } else {
        is = Base.getLibStream("export/applet.html");
      }
    }    
    
    if (is == null) 
      throw new RuntimeException("Unable to find 'export' dir...");
      
    String line = null;
    InputStreamReader isr = new InputStreamReader(is, "UTF-8");
    BufferedReader reader = new BufferedReader(isr);
    while ((line = reader.readLine()) != null) {
      if (line.indexOf("@@") != -1) {
        StringBuffer sb = new StringBuffer(line);
        int index = 0;
        while ((index = sb.indexOf("@@sketchName@@")) != -1) {
          sb.replace(index, index + "@@sketchName@@".length(), sketchName);
        }
        while ((index = sb.indexOf("@@sketch@@")) != -1) {
          sb.replace(index, index + "@@sketch@@".length(), mainClass);
        }
        while ((index = sb.indexOf("@@source@@")) != -1) {
          sb.replace(index, index + "@@source@@".length(),  sources.toString());
        }
         
        while ((index = sb.indexOf("@@archive@@")) != -1) {
          sb.replace(index, index + "@@archive@@".length(), cp);
        }
        while ((index = sb.indexOf("@@width@@")) != -1) {
          sb.replace(index, index + "@@width@@".length(), String.valueOf(wide));
        }
        while ((index = sb.indexOf("@@height@@")) != -1) {
          sb.replace(index, index + "@@height@@".length(), String.valueOf(high));
        }
        while ((index = sb.indexOf("@@description@@")) != -1) {
          sb.replace(index, index + "@@description@@".length(), description);
        }
        line = sb.toString();
      }
      ps.println(line);
    }

    reader.close();
    ps.flush();
    ps.close();
    
    P5ExportUtils.deleteDir(tmpFolder);

    return true;
  }
  
  protected File writePdeFile(String contents, String javaShortClass, File workDir) throws Exception, FileNotFoundException
  {
    File pdeFile = new File(workDir, javaShortClass+".pde");
    if (pdeFile.exists() && !pdeFile.delete())
        throw new RuntimeException("Unable to delete .pde file: "+pdeFile);
    String pdeCode = unProcess(javaShortClass, contents);                          
    P5ExportUtils.writeFile(pdeFile, pdeCode);
    return pdeFile;
  }

  private String unProcess(String className, String contents)
  {        
    //System.err.println("P5ExportBuilder.processForP5("+className+",\n\n"+contents);
    Regex re = Regex.getInstance();
    
    String regex = "(.*?)package([^;]+?);";
    if (re.test(regex, contents)) 
      contents = re.replace(regex, contents, "");
    
    regex = "^(.*?)import\\s+processing\\.core\\.(.*?)$";
    if (!re.test(regex, contents)) {
      System.err.println("NO MATCH1.5!");
    }
    contents = re.replace(regex, contents, "");
    
    regex = "(.*?)public\\s+class\\s+"+className+"[^\\{]*?\\{";
    if (!re.test(regex, contents)) {
      System.err.println("NO MATCH2!");
    }
    contents = re.replace(regex, contents, "");
    
    contents = re.replace("public ", contents, "");
    int idx = contents.lastIndexOf("}");
    if (idx>-1) 
      contents = contents.substring(0,idx)+"\n";
    
    String header = "// Generated by EclipseP5Exporter: "+new Date().toString(); 
    
    return header+"\n"+contents;
  }
  
}