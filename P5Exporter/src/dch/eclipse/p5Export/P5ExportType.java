package dch.eclipse.p5Export;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.eclipse.debug.core.ILaunchConfiguration;

import processing.app.Base;
import processing.app.Sketch;
import processing.core.PApplet;
import processing.core.PConstants;

public abstract class P5ExportType
{
  private static final String JAVA_RES_DIR_OSX = "Contents/Resources/Java";
  
  protected static final String GLGRAPHICS = "codeanticode.glgraphics.GLGraphics";
  protected static final String OPENGL = "OPENGL";
  
  protected static final int[] PLATFORMS = {  PConstants.LINUX, PConstants.WINDOWS, PConstants.MACOSX, };
  
  protected static final String[][] GL_NATIVE_LIBS = { 
    {"jogl.dll","jogl_awt.dll","jogl_cg.dll", "gluegen-rt.dll"},       // WINDOWS
    {"libgluegen-rt.jnilib","libjogl.jnilib","libjogl_awt.jnilib","libjogl_cg.jnilib"},  // OSX
    {"libgluegen-rt.so","libjogl.so","libjogl_awt.so","libjogl_cg.so"}, // UNIX
  };
  
  protected static final String[] GL_NATIVE_JARS = {
    "gluegen-rt-natives-linux-amd64.jar", "gluegen-rt-natives-linux-i586.jar",
    "gluegen-rt-natives-macosx-ppc.jar", "gluegen-rt-natives-macosx-universal.jar",
    "gluegen-rt-natives-windows-amd64.jar", "gluegen-rt-natives-windows-i586.jar", 
    "jogl-natives-linux-amd64.jar", "jogl-natives-linux-i586.jar", 
    "jogl-natives-macosx-ppc.jar", "jogl-natives-macosx-universal.jar", 
    "jogl-natives-windows-amd64.jar", "jogl-natives-windows-i586.jar",
  };
  
  protected boolean isApplet, forceMultipleJars, presentMode, addStopButton;
  protected Vector jarListVector = new Vector();  
  protected File libGl, libDir;
  protected String mainClass;
    
  protected void writeGlExportsToLibGL() throws P5ExportException {
    
    // make sure we have libe/opengl dir
    if (libDir == null)
      libDir = new File(System.getProperty("user.dir"), "lib");
    if (!libDir.exists()) 
      throw new P5ExportException("No lib dir found at: "+libDir.getPath());
    if (libGl == null)
      libGl = new File(libDir, "opengl");   
    if (!libGl.exists()) libGl.mkdirs();
    
    // Write the GL native jars if we need them...
    try {
      P5ExportUtils.writeJarResourcesAsFiles(getClass(), P5ExportBuilder.P5_EXPORTS_GL_JAR, libGl);
    } catch (Exception e) {
      throw new P5ExportException(e);
    }
  }
    
  public abstract File doExport
    (P5ExportBuilder builder, ILaunchConfiguration configuration, File workDir, List createdFiles) 
  throws Exception;
  
  protected Vector createArchive(String sketchName, String mainClass, String classpath, 
      File appFolder, File jarFolder, boolean separateJarsForGL, int exportPlatform, boolean isApplication) 
  throws IOException, P5ExportException
  {        
    //System.err.println("P5ExportType.createArchive("+sketchName+", "+ mainClass+", "+ classpath+", "+ 
      //appFolder+", "+ jarFolder+", "+ separateJar+", "+ exportPlatform+")");
    
    Vector classpathList = new Vector();
    classpathList.add(sketchName + ".jar");
    
    // Create zip & add the manifest file
    File mainJarFile = new File(jarFolder, sketchName + ".jar");
    FileOutputStream zipOutputFile = new FileOutputStream(mainJarFile);
    
    ZipOutputStream zos = new ZipOutputStream(zipOutputFile);
    addManifest(zos, mainClass);

    if (separateJarsForGL) // its GL
    {
      String cp = "";
      String[] codeList = classpath.split(File.pathSeparator);
      
//System.out.println("GL_CODE_LIST: "+Arrays.asList(codeList));

      for (int i = 0; i < codeList.length; i++)
      {
        if (codeList[i].toLowerCase().endsWith(".jar")
            || codeList[i].toLowerCase().endsWith(".zip"))
        {
          File exportFile = new File(codeList[i]);
          String exportFilename = exportFile.getName();
          File toLib = new File(jarFolder, exportFilename);
          copyFile(exportFile, toLib);
          addToClasspath(classpathList, codeList[i]);
        } 
        else
        {
          cp += codeList[i] + File.separatorChar;
//          packClassPathIntoZipFile(cp, zos, classpathList, exportPlatform, appFolder);

          addExternalProjectsToZipFile(classpath, zos, classpathList, exportPlatform, appFolder);
        }
      }
      
      for(String nativeLibPath: P5ExportBuilder.nativeLibDirs){
        //        File toLib = new File(jarFolder, exportFilename);

        File nativeDir = new File(nativeLibPath);
        if(nativeDir.exists() && nativeDir.isDirectory()){
          for(File f: nativeDir.listFiles()){
            if(f.isDirectory()){

              String exportDir = f.getName();
              File toDir = new File(jarFolder, exportDir);
              if(!toDir.exists()){
                toDir.mkdir();
              }
              
              for(File f1: f.listFiles()){
                String exportFilename = f.getName() + "/" + f1.getName();
                File toLib = new File(jarFolder, exportFilename);
                copyFile(f1, toLib);
              }
            } else if(!f.getName().toLowerCase().endsWith(".jar")){
              String exportFilename = f.getName();
              File toLib = new File(jarFolder, exportFilename);
              copyFile(f, toLib);
            }
          }
        }
      }
      
      if (isApplication) {
        //MATT: REMOVED WHEN UPDATED PROCESSING 2
        //addGLNatives(appFolder, zos, exportPlatform);   
        
      }
    } 
    else  // not GL
    {
      packClassPathIntoZipFile(classpath, zos, classpathList, exportPlatform, appFolder);
    }

    // add the project's .class files to the jar
    // just grabs everything from the build directory
    // since there may be some inner classes
    // (add any .class files from the applet dir, then delete them)
    String classfiles[] = appFolder.list();
    for (int i = 0; i < classfiles.length; i++) { // never happens?
      if (classfiles[i].endsWith(".class")) {
        //System.err.println("ADDED CLASS FILE: " + classfiles[i]);
        addZipEntry(appFolder, classfiles[i], zos);
      }
    }

    // remove the .class files from the applet folder. if they're not
    // removed, the msjvm will complain about an illegal access error,
    // since the classes are outside the jar file.
    for (int i = 0; i < classfiles.length; i++)
    {
      if (classfiles[i].endsWith(".class"))
      {
        //System.err.println("REMOVED OLD .CLASS: " + classfiles[i]);
        File deadguy = new File(appFolder, classfiles[i]);
        if (!deadguy.delete())
          Base.showWarning("Could not delete", classfiles[i] + " could not \n"
            + "be deleted from the applet folder.  \n"
            + "You'll need to remove it by hand.", null);
      }
    }

    // close up the jar file
    zos.flush();
    zos.close();
    
    return classpathList;
  }

  private void addGLNatives(File appFolder, ZipOutputStream zos, int platform) throws P5ExportException
  {
    switch (platform){
      case PConstants.WINDOWS:          
        addGLNatives(appFolder, zos, GL_NATIVE_LIBS[0]);
        break;
      case PConstants.MACOSX:        
        File res = new File(appFolder, JAVA_RES_DIR_OSX);
        if (!res.exists()) {
          res.mkdirs();        
          System.err.println("NO OSX RESOURCE DIR: '"+res+"'!");
        }
        addGLNatives(res, zos, GL_NATIVE_LIBS[1]);
        break;
      default: // PConstants.LINUX
        addGLNatives(appFolder, zos, GL_NATIVE_LIBS[2]);
    }
  }

  // TODO: add from exportsGL.jar if not found?
  private void addGLNatives(File dir, ZipOutputStream zos, String[] natives) throws P5ExportException
  {
    writeGlExportsToLibGL();
    for (int i = 0; i < natives.length; i++) {
       File nfile = new File(dir, natives[i]);
       if (!nfile.exists()) {
         nfile = new File(libGl, natives[i]);
         if (!nfile.exists())
            throw new P5ExportException("Unable to locate native library: "+
              natives[i]+", in "+libGl.getPath());
         copyFile(nfile, new File(dir, natives[i]));
       }
       //System.err.println("ADDING NATIVE: "+natives[i]);
       try {
        addZipEntry(dir, natives[i], zos);
      } catch (IOException e) {
        throw new P5ExportException(e);
      }             
    }
  }

  private void addZipEntry(File dir, String file, ZipOutputStream zos)
    throws IOException
  {
    ZipEntry entry = new ZipEntry(file);
    zos.putNextEntry(entry);
    zos.write(Base.loadBytesRaw(new File(dir,file)));
    //zos.write(Base.grabFile(new File(dir,file)));
    zos.closeEntry();
  }
  
  private static String sizeRegex ="(?:^|\\s|;)size\\s*\\(\\s*(\\S+)\\s*,\\s*(\\d+),?\\s*([^\\)]*)\\s*\\)";
  protected static /* TMP */ String[] parseSizeArgs(String program) throws P5ExportException
  {      
    String scrubbed = Sketch.scrubComments(program);
    String[] matches = PApplet.match(scrubbed, sizeRegex);
    
    String wide="200", high="200", renderer=""; //defaults
    if (matches != null) {
      try {
        wide = ""+Integer.parseInt(matches[1]);
        high = ""+Integer.parseInt(matches[2]);
        if (matches.length == 4) 
          renderer = matches[3].trim();
      } 
      catch (NumberFormatException e) {
        // found a reference to size, but it didn't seem to contain numbers       
        throw new P5ExportException(SIZE_PARSING_MSG, e);
      }
    }  // else no size() command found
    else
      throw new P5ExportException(SIZE_PARSING_MSG);
      
    return new String[] { wide, high, renderer };
  }
  
  private static final String SIZE_PARSING_MSG =
    "The size of this applet could not automatically be " +
    "determined from your code. Use only numeric values " +
    "(not variables) for the size() command. See the size() "+
    "reference for an explanation.";
  
  protected void writeMain(File javaFile, String className)
  {
    String contents = null;
    PrintStream out = null;
    try
    {
      contents = P5ExportUtils.loadFile(new FileInputStream(javaFile));
      out = new PrintStream(new FileOutputStream(javaFile));
    } 
    catch (Exception e) {
      e.printStackTrace();
    }
    int lastBraceIdx = contents.lastIndexOf("}");
    contents = contents.substring(0,lastBraceIdx);
    out.print(contents);
    out.println();
    out.print("  static public void main(String args[]) { ");
    out.println();
    out.print("    PApplet.main(new String[] { \"" + className + "\" });");
    out.println();
    out.print("  }");
    out.println();
    out.print("}");
    out.println();
  }

  /**
   * Slurps up .class files from a colon (or semicolon on windows)
   * separated list of paths and adds them to a ZipOutputStream.
   * @param exportPlatform 
   * @param appFolder 
   */
  public void packClassPathIntoZipFile(String classpath, ZipOutputStream zos, 
    Vector classpathList, int exportPlatform, File appFolder) throws P5ExportException 
  {
//System.out.println("P5ExportType.packClassPathIntoZipFile("+classpath+","+classpathList+")");    
    Set zipFileContents = new HashSet();
    
    String pieces[] = PApplet.split(classpath, File.pathSeparatorChar);

    for (int i = 0; i < pieces.length; i++)
    {
//System.out.println(i+") "+pieces[i]);
      
      if (pieces[i] == null || pieces[i].length() == 0)  
        continue;
      
      if (pieces[i].startsWith("org.eclipse"))
        continue;

      // not sure about all this ===================================
      if (pieces[i].startsWith("/System/Library/Frameworks/JavaVM"))
        continue;
      
      if (pieces[i].startsWith("/System/Library/Java/"))
        continue;
      
      if (pieces[i].contains("/Library/Java/Extensions/"))
        continue;
      
//System.out.println("  Using "+pieces[i]);
      // ===========================================================

      // is it a jar file or directory?
      if (pieces[i].toLowerCase().endsWith(".jar") || pieces[i].toLowerCase().endsWith(".zip")) 
      {     
//        addToClasspath(classpathList, pieces[i]);
//        
//        try {
//          ZipFile file = new ZipFile(pieces[i]);
//          Enumeration entries = file.entries();
//          while (entries.hasMoreElements()) {
//            ZipEntry entry = (ZipEntry) entries.nextElement();
//            if (entry.isDirectory()) {             
//              //actually 'continue's for all dir entries
//            } 
//            else {
//              addToZip(zos, zipFileContents, entry.getName(), getBytes(file, entry));
//            }
//          }
//        } 
//        catch (IOException e) {
//          System.err.println("Error in file " + pieces[i]);
//          e.printStackTrace();
//        }
      } 
      else 
      { 
        // not a .jar or .zip, prob. a directory
        File dir = new File(pieces[i]);
        if (dir.exists()) 
          packClassPathIntoZipFileRecursive(dir, null, zos, exportPlatform, appFolder, zipFileContents);
      }
    }
  }

  public void addExternalProjectsToZipFile(String classpath, ZipOutputStream zos, 
      Vector classpathList, int exportPlatform, File appFolder) throws P5ExportException 
    {  
      Set zipFileContents = new HashSet();
      
      String pieces[] = PApplet.split(classpath, File.pathSeparatorChar);

      for (int i = 0; i < pieces.length; i++)
      {
        
        if (pieces[i] == null || pieces[i].length() == 0)  
          continue;
        
        if (pieces[i].startsWith("org.eclipse"))
          continue;

        // not sure about all this ===================================
        if (pieces[i].startsWith("/System/Library/Frameworks/JavaVM"))
          continue;
        
        if (pieces[i].startsWith("/System/Library/Java/"))
          continue;
        
        if (pieces[i].contains("/Library/Java/Extensions/"))
          continue;
        
  //System.out.println("  Using "+pieces[i]);
        // ===========================================================

        // is it a jar file or directory?
        if (pieces[i].toLowerCase().endsWith(".jar") || pieces[i].toLowerCase().endsWith(".zip")) 
        {     
        } 
        else 
        { 
          try{
          // not a .jar or .zip, prob. a directory
          File dir = new File(pieces[i]);
          if (dir.exists()) 
            packClassPathIntoZipFileRecursive(dir, null, zos, exportPlatform, appFolder, zipFileContents);
          } catch (Exception e) {
            // TODO: handle exception
          }
        }
      }
    }
  
  private boolean addToZip
    (ZipOutputStream zos, Set zipFileContents, String entryName, byte[] bytes) 
  throws IOException
  {
    if (entryName.indexOf("META-INF") == 0) 
      return false;
    
    // don't allow duplicate entries
    if (zipFileContents.contains(entryName)) 
      return false;
    
    zipFileContents.add(entryName);   
    ZipEntry entree = new ZipEntry(entryName);
    
    /*if (entryName.equals(this.mainClass));
      System.err.println(mainClass+" Adding mainClass to zipfile: "+entryName);
    */
    zos.putNextEntry(entree);
    
    zos.write(bytes);
    zos.flush();
    zos.closeEntry();
    
    return true;
  }


  private byte[] getBytes(ZipFile file, ZipEntry entry) throws IOException {
    byte buffer[] = new byte[(int) entry.getSize()]; 
    return getBytes(file.getInputStream(entry), buffer);
  }

  protected byte[] getBytes(File file) throws IOException { 
    byte[] buffer = new byte[(int) file.length()];
    return getBytes(new FileInputStream(file), buffer);
  }
  
 protected byte[] getBytes(InputStream input, byte[] buffer) throws IOException {
    int offset = 0;    
    int bytesRead;
    int bufSz = buffer.length;
    while ((bytesRead = input.read(buffer, offset, bufSz-offset)) != -1) {
      offset += bytesRead;
      if (bytesRead == 0) break;
    }
    input.close();
    input = null;
    return buffer;
  }
  
  
  protected void packClassPathIntoZipFileRecursive(File dir, String sofar,
    ZipOutputStream zos, int exportPlatform, File appFolder, Set contents)  throws P5ExportException
  {
    String files[] = dir.list();
   // boolean isData = dir.toString().endsWith("data");
    for (int i = 0; i < files.length; i++)
    {
      // ignore . .. and .DS_Store
      if (files[i].charAt(0) == '.') continue;
//      if (files[i].equals("data")) continue; 

      File sub = new File(dir, files[i]);
      String nowfar = (sofar == null) ? files[i] : (sofar + "/" + files[i]);

      if (sub.isDirectory())
      {
        //if (!isData)
        packClassPathIntoZipFileRecursive(sub, nowfar, zos, exportPlatform, appFolder, contents);
      } 
      else if (!isApplet && sub.getName().toLowerCase().endsWith(".jnilib"))        
      {
        if ((exportPlatform == PConstants.MACOSX)) {
          // jnilib files can be placed in Contents/Resources/Java
          File jniDir = new File(appFolder,JAVA_RES_DIR_OSX);
          if (!jniDir.exists()) jniDir.mkdirs();
          
          File nativeLib = new File(jniDir, files[i]);
          //System.err.println("WRITING.OSX: "+nativeLib);
          copyFile(sub, nativeLib);   
        }
      }
      else if (!isApplet && sub.getName().toLowerCase().endsWith(".dll"))
      {
        if (exportPlatform == PConstants.WINDOWS) {
          File nativeLib = new File(appFolder, files[i]);
          //System.err.println("WRITING.WIN: "+nativeLib);
          copyFile(sub, nativeLib);
        }
      }
      else if (!isApplet && sub.getName().toLowerCase().endsWith(".so"))
      {      
          if (exportPlatform == PConstants.LINUX) {
            File nativeLib = new File(appFolder, files[i]);
            //System.err.println("WRITING.LIN: "+nativeLib);
            copyFile(sub, nativeLib);
          }
      }
      else if (!files[i].toLowerCase().endsWith(".jar")
          && !files[i].toLowerCase().endsWith(".zip")
          && files[i].charAt(0) != '.')
      {
        try {
          addToZip(zos, contents, nowfar, getBytes(sub));
        } catch (IOException e) {
          throw new P5ExportException(e);
        } 
      }
    }
  }
  
  private void copyFile(File sourceFile, File targetFile) throws P5ExportException {
    try {
      Base.copyFile(sourceFile, targetFile);
    } catch (IOException e) {
      throw new P5ExportException(e);
    }
  }

  protected void addToClasspath(Vector classpathList, String entry)
  {
    int idx = entry.lastIndexOf(File.separator);
    if (idx > -1) entry = entry.substring(idx+1);
    if (!classpathList.contains(entry)) {
      //System.err.println("Adding to classpath: "+entry);
      classpathList.add(entry);
    }
  }
  
  public void addManifest(ZipOutputStream zos, String name) throws IOException {
    ZipEntry entry = new ZipEntry("META-INF/MANIFEST.MF");
    zos.putNextEntry(entry);
    String contents =
      "Manifest-Version: 1.0\n" +
      "Created-By: Processing+EclipseP5Exporter\n"+// + Base.VERSION_NAME + \n" +
      "Main-Class: " + name + "\n";  // TODO not package friendly
    zos.write(contents.getBytes());
    zos.closeEntry();
  }  
  
  // misc. options
  public void setForceMultipleJars(boolean forceMultipleJars) {
    this.forceMultipleJars = forceMultipleJars;
  }
    
  public void setUsePresentMode(boolean usePresentMode) {
    this.presentMode = usePresentMode;
  }
  
  public void setAddStopButton(boolean useStopButton) {
    this.addStopButton = useStopButton;
  }
  
  public static void main(String[] args) throws P5ExportException
  {
    String program = "\n  size(w,h, OPENGL );\n  println(\"hello\";\n";
    String[] parms = parseSizeArgs(program);
    for (int i = 0; i < parms.length; i++)
    {
      System.out.println(i+") '"+parms[i]+"'");
    }    
  }


  
}// end
