package dch.eclipse.p5Export;

import java.io.*;
import java.util.*;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import processing.app.*;
import processing.core.PApplet;
import processing.core.PConstants;

public class P5ApplicationExport extends P5ExportType
{  
  private static final String CORE_JAR = "core.jar";
  private static final String JAVAROOT = "$JAVAROOT/";

  public P5ApplicationExport() {
    this.isApplet = false;
  }
  
  public File doExport(P5ExportBuilder builder, ILaunchConfiguration pConfiguration, File workDir, List createdFiles) throws Exception
  {                  
    Preferences.set("build.path", workDir.getAbsolutePath());             
    if (export(builder)) {
      //int[] platformConsts = { 1,3,4 }; // yuck
      String[] platformConsts = { "windows","macosx","linux" }; // yuck
      for (int i = 0; i < platformConsts.length; i++)
      {
        createdFiles.add(new File
          (workDir, "applications."+platformConsts[i]));
      }
      return workDir;
    }
    else  
      throw new P5ExportException("Unable to complete export");
  }
  
  protected boolean export(P5ExportBuilder builder) throws Exception 
  {
    //System.err.println("P5ApplicationExport.export("+sketchFolder+","+javaSrc+","+mainClass+", CP=''"+sketchName+"hasMain= "+hasMain+") : \n\n"+classpath+"\n\n");
    File sketchFolder = builder.workDir;
    String sketchName = builder.sketchName;
    this.mainClass  = builder.mainClass;
    String programText = builder.mainProgram;
    //System.out.println("mainClass: "+mainClass);
    //System.out.println("sketchName: "+sketchName);
    String classpath  = builder.projectClassPath;
    File srcDir = new File(builder.srcPath);
    
    for (int p = 0; p < PLATFORMS.length; p++) 
    {       
      int exportPlatform = PLATFORMS[p];
      String exportPlatformStr = null;
      if (exportPlatform == PConstants.WINDOWS) {
        exportPlatformStr = "windows";
      } else if (exportPlatform == PConstants.MACOSX) {
        exportPlatformStr = "macosx";
      } else if (exportPlatform == PConstants.LINUX) {
        exportPlatformStr = "linux";
      } else {
        throw new RuntimeException("Unexpected OS type!");
      }
  
      String folderName = "application";
      if (exportPlatform != -1) 
        folderName += "."+exportPlatformStr;
      
      // re-create the .application folder
      File appFolder = new File(sketchFolder, folderName);
      P5ExportUtils.deleteDir(appFolder);
      appFolder.mkdirs();
          
      // and the lib folder inside app     
      File jarFolder = new File(appFolder, "lib");
      if (exportPlatform != PConstants.MACOSX && !jarFolder.exists()) 
        jarFolder.mkdir();      
      
      // and the source folder inside app
      File newSource = new File(appFolder, "source");
      if (!newSource.exists()) newSource.mkdir();
      File[] files = srcDir.listFiles();             
      Vector fileVector = new Vector();
      fileVector.addAll(Arrays.asList(files));
      
      // Add all the src files
      while (!fileVector.isEmpty())
      {        
        File file = (File)fileVector.remove(0);    
        if (file.isDirectory()) { 
          File[] tmp = file.listFiles();
          fileVector.addAll(Arrays.asList(tmp));          
        }
        else if  (file.getName().endsWith(".java")) 
        {
          File newFile = new File(newSource, 
            file.getAbsolutePath().replace(srcDir.getAbsolutePath(), ""));
          P5ExportUtils.writeFile(file, newFile);          
        }
      }
      
      String renderer = "";
      String[] params = parseSizeArgs(builder.mainProgram);
      if (params != null && params.length==3)
        renderer = params[2];
      //System.err.println("RENDERER: "+renderer);
      
      File dotAppFolder = null;
      if (exportPlatform == PConstants.MACOSX) {
        dotAppFolder = new File(appFolder, sketchName + ".app");
        String APP_SKELETON = "skeleton.app";
        //File dotAppSkeleton = new File(folder, APP_SKELETON);
        File dotAppSkeleton = new File("lib/export/" + APP_SKELETON);
        Base.copyDir(dotAppSkeleton, dotAppFolder);
        String stubName = "Contents/MacOS/JavaApplicationStub";
        // need to set the stub to executable
        // will work on osx or *nix, but just dies on windows, oh well..
        if (PApplet.platform == PConstants.WINDOWS) {
          File warningFile = new File(appFolder, "readme.txt");
          writeWarningFile(dotAppFolder, stubName, warningFile);  
        } 
        else {
          File stubFile = new File(dotAppFolder, stubName);
          String stubPath = stubFile.getAbsolutePath();
          Runtime.getRuntime().exec(new String[] { "chmod", "+x", stubPath });
        }
  
        // set the jar folder to a different location than windows/linux
        jarFolder = new File(dotAppFolder, "Contents/Resources/Java");
        
        appFolder = dotAppFolder;  // reset the app folder to dotApp (??)
      }
      if (!jarFolder.exists()) jarFolder.mkdirs();
            
      if (exportPlatform == PConstants.WINDOWS) {
        File exe = new File(appFolder, sketchName + ".exe");
        Base.copyFile(new File("lib/export/application.exe"), exe);
        if (!exe.exists())
          P5ExportUtils.errorDialog(builder.shell, "Could not make windows .exe", null);
         //Base.showWarning("Could not make exe", "Could not make exe", null);
      }
        
      if (!builder.hasMain) {
          String message = "  A main() method is required for application  exports: ";
          String breaks = "\n\n";
          String code = "  public static void main(String args[])\n  {\n"
          + "    PApplet.main(new String[] { \"" + mainClass + "\" });\n  }";
          
          //P5ExportUtils.errorDialog(builder.shell, message + breaks + code, null);
          // this next call replaces line above to give selectable code.
          P5ExportUtils.messageWithCode(builder.shell, message, code);
          return false;
      }          
      else // ok, we have a main 
      {
        // check for --present if full-screen was specified
        if (presentMode && programText.indexOf("--present")<0) {
          // ok, we need to add --present   
          System.err.println(getClass().getName()+" handling main(--present)");
          String replaceStr = "new String[] { \"--present\", ";
          if (!addStopButton) // hide stop button
            replaceStr += "\"--hide-stop\", ";            
          System.err.println("PGRM: "+programText);
          String newText = programText.replaceFirst("new *String\\[ *\\] *\\{", replaceStr);
          if (newText.equals(programText))
            throw new P5ExportException("Unable to add'--present' flag: "+programText);
          programText = newText;
          System.err.println("PGRM2: "+programText);
          System.err.println();
          rewriteMainClass = true;          
        }
        
        // double-check
        if (presentMode && programText.indexOf("--present")<0) {
          String msg = "[WARN] Unable to set full-screen mode!\n"+programText+"\n";
          System.err.println(msg);
          //P5ExportUtils.errorDialog(builder.shell, msg, null);
          //return false;
        }
  
          
     /* if (!mainProgram.contains("--present")) 
        System.err.println("[WARN] Unable to set full-screen mode!\n"+mainProgram);
        if (presentMode && mainTag.indexOf("--present")<0) {      
          String msg = "[WARN] No '--present' flag in:\n"+mainTag;
          System.err.println(msg);
          //P5ExportUtils.errorDialog(builder.shell, msg, null);
          //return false;
        }*/

      }
  
      // determine whether to use one jar file or several     
      boolean separateJar = renderer.equals("OPENGL");          
          
      // Write the main zip (copy jars to lib if necessary), & return the classpath 
      Vector classpathList = null;
      try {
        classpathList = createArchive(sketchName, mainClass, classpath, appFolder, 
          jarFolder, separateJar, exportPlatform, separateJar/*true-for-gl-applications-only*/);
      } catch (IOException e) {
        throw new P5ExportException(e);
      }
      
      String jarList[] = new String[classpathList.size()];
      //System.err.println("jarListVector: "+classpathList);
      classpathList.copyInto(jarList);
      StringBuffer exportClassPath = new StringBuffer();
      if (exportPlatform == PConstants.MACOSX) {
        for (int i = 0; i < jarList.length; i++) {
          if (i != 0) exportClassPath.append(":");
          exportClassPath.append(JAVAROOT + jarList[i]);
        }
      } else if (exportPlatform == PConstants.WINDOWS) {
        for (int i = 0; i < jarList.length; i++) {
          if (i != 0) exportClassPath.append(",");
          exportClassPath.append(jarList[i]);
        }
      } else {
        for (int i = 0; i < jarList.length; i++) {
          if (i != 0) exportClassPath.append(":");
          exportClassPath.append("$APPDIR/lib/" + jarList[i]);
        }
      }
      
      String cp = !separateJar ? sketchName+".jar" : exportClassPath.toString();
       
      if (exportPlatform == PConstants.MACOSX) 
      {
      	if (!separateJar) {
      	  cp = JAVAROOT+cp;  // hack
      	  
      	  
      	  // if we are going to do this (Proc does), we need to 
      	  // make sure core.jar is copied into the resources folder
          if (false && cp.indexOf(CORE_JAR)<0) {    	      
      	      cp += ":"+JAVAROOT+CORE_JAR;
      	      System.out.println("[INFO] Added core.jar to classpath: "+cp);
      	  }
      	}
      	// System.err.println("CLASSPATH="+cp);      	
        String PLIST_TEMPLATE = "template.plist";
        File plistTemplate = new File(sketchFolder, PLIST_TEMPLATE);
        if (!plistTemplate.exists()) {
          plistTemplate = new File("lib/export/" + PLIST_TEMPLATE);
        }
        File contentsDir = new File(appFolder, "Contents");
        if (!contentsDir.exists()) contentsDir.mkdirs();
        File plistFile = new File(contentsDir, "Info.plist");
        PrintStream ps = new PrintStream(new FileOutputStream(plistFile));
          
        // FULL_SCREEN SETTINGS             
        Preferences.setBoolean("export.application.fullscreen", presentMode);
        Preferences.setBoolean("export.application.stop", addStopButton);

        // MEMORY SETTINGS (from builder.vmArgs)???
        String vmArgs = " -Xms64m -Xmx384m";
        if (builder.vmArgs.length()>0) {
          if (builder.vmArgs.contains("-Xmx"))
            vmArgs = builder.vmArgs;
          else 
            vmArgs += builder.vmArgs;
        }         
        String[] lines = PApplet.loadStrings(plistTemplate);
        for (int i = 0; i < lines.length; i++) {
          //System.out.println(i+")"+lines[i]);
          if (lines[i].indexOf("@@") != -1) {
            StringBuffer sb = new StringBuffer(lines[i]);
            int index = 0;
            while ((index = sb.indexOf("@@sketchName@@")) != -1) {
              sb.replace(index, index + "@@sketchName@@".length(), sketchName);
            }
            while ((index = sb.indexOf("@@sketch@@")) != -1) {
              sb.replace(index, index + "@@sketch@@".length(), mainClass);
            }
            while ((index = sb.indexOf("@@vmargs@@")) != -1) {
              sb.replace(index, index + "@@vmargs@@".length(), vmArgs);
            }
            while ((index = sb.indexOf("@@vmoptions@@")) != -1) {
              sb.replace(index, index + "@@vmoptions@@".length(), vmArgs);
            }
            while ((index = sb.indexOf("@@classpath@@")) != -1) {
              sb.replace(index, index + "@@classpath@@".length(), cp);
            }
            while ((index = sb.indexOf("@@lsuipresentationmode@@")) != -1) {
              String pmode = Preferences.getBoolean("export.application.fullscreen") ? "4" : "0";
              sb.replace(index, index + "@@lsuipresentationmode@@".length(), pmode);
              //System.out.println("SETTING PMODE="+pmode);
            }
            lines[i] = sb.toString();
          }
          // explicit newlines to avoid Windows CRLF
          ps.print(lines[i] + "\n");
        }
        ps.flush();
        ps.close();  
      } 
      else if (exportPlatform == PConstants.WINDOWS) 
      {
        Sketch s;
        File argsFile = new File(appFolder + "/lib/args.txt");        
        PrintStream ps = new PrintStream(new FileOutputStream(argsFile));       
        if (builder.vmArgs != null && builder.vmArgs.length()>0)
          ps.print(builder.vmArgs);
        ps.println();
        ps.print(mainClass);
  /*      if (progArgs != null && progArgs.length()>0)
          ps.print(progArgs);*/
        ps.println();
        ps.println(cp);
        ps.flush();
        ps.close();
      } 
      else // Linux 
      {
        File shellScript = new File(appFolder, sketchName);
        PrintStream ps = new PrintStream(new FileOutputStream(shellScript));
        ps.print("#!/bin/sh\n\n");
        ps.print("APPDIR=$(dirname \"$0\")\n");  
        if (!separateJar) cp  = "lib/"+cp;
        ps.print("java " + builder.vmArgs +" -Djava.library.path=\"$APPDIR\"" +
          " -cp \"" + cp + "\"" + /*" "+ progArgs +*/" " + mainClass + "\n");  
        ps.flush();
        ps.close();
  
        String shellPath = shellScript.getAbsolutePath();
        // will work on osx or *nix, but dies on windows, oh well..
        if (PApplet.platform != PConstants.WINDOWS) {
          Runtime.getRuntime().exec(new String[] { "chmod", "+x", shellPath });
        }
      }
    }
    return true;
  }

  private void writeWarningFile(File dotAppFolder, String stubName, File warningFile) throws FileNotFoundException
  {
    PrintStream ps = new PrintStream(new FileOutputStream(warningFile));
    ps.println("This application was created on Windows, which doesn't");
    ps.println("properly support setting files as \"executable\",");
    ps.println("a necessity for applications on Mac OS X.");
    ps.println();
    ps.println("To fix this, use the Terminal on Mac OS X, and from this");
    ps.println("directory, type the following:");
    ps.println();
    ps.println("chmod +x " + dotAppFolder.getName() + "/" + stubName);
    ps.flush();
    ps.close();
  }

}// end
