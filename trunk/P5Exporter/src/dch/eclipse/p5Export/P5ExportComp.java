package dch.eclipse.p5Export;

import java.util.*;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.*;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * The SWT popup interface for the plugin
 */
public class P5ExportComp extends Composite implements SelectionListener
{
  //private static final boolean showApplicationOptions = false;  
    
  // GUI CONTROLS
  Button radioApplet, radioApplication, bCurrentlySelected, multipleJarsButton;
  Label lblSelProjects, lblSelTarget, dummyLabel;
  Label label6, label5, label4;// label3, label2, label1;
  //Button presentModeButton, showStopButton; // presentation-modes
  Group exportTypeGroup, optionsGroup;
  CheckboxTableViewer cViewer;  
    
  // MEMBER VARS
  boolean forceMultipleJars, usePresentMode, addStopButton;
  IJavaProject jSelectedProject;
  ILaunchConfiguration selectedConfig;
  List projectList;  
  WizardPage wPage;  
  P5ExportUtils sUtil;
  Table tblTargets;
  Map builderMap;
  
  public P5ExportComp(WizardPage pPage, Composite parent, int style)
  {
    super(parent, style);
    sUtil = P5ExportUtils.getInstance();
    wPage = pPage;
    initialize();

    // Map the buttons to the builder
    builderMap = new HashMap();
    builderMap.put(radioApplet, new P5AppletExport());
    builderMap.put(radioApplication, new P5ApplicationExport());    
    bCurrentlySelected = radioApplet;
    radioApplet.setSelection(true);
  }

  private void initialize()
  {
    GridData gridData3 = new GridData();
    gridData3.grabExcessHorizontalSpace = true;
    gridData3.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
    gridData3.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
    gridData3.grabExcessVerticalSpace = true;
    GridData gridData1 = new GridData();
    gridData1.grabExcessHorizontalSpace = true;
    gridData1.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
    GridData gridData = new GridData();
    gridData.grabExcessHorizontalSpace = true;
    gridData.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
    lblSelProjects = new Label(this, SWT.CENTER);
    lblSelProjects.setText("Select Project");
    lblSelProjects.setLayoutData(gridData1);
    lblSelTarget = new Label(this, SWT.CENTER);
    lblSelTarget.setText("Select Run-Config");
    lblSelTarget.setLayoutData(gridData);
    dummyLabel = new Label(this, SWT.NONE);
    projectList = new List(this, SWT.BORDER | SWT.V_SCROLL);
    projectList.setLayoutData(gridData3);    
    
    ListViewer lViewer = new ListViewer(projectList);
    lViewer.setContentProvider(new BaseWorkbenchContentProvider() {
      public Object[] getElements(Object element){
        if (element instanceof IJavaProject[]) 
          return (IJavaProject[])element;
        return null;
    }});
    lViewer.setLabelProvider(new WorkbenchLabelProvider());
    lViewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
      public void selectionChanged(SelectionChangedEvent event)
      {
        StructuredSelection sSelection = (StructuredSelection)event.getSelection();
        jSelectedProject = (IJavaProject)sSelection.getFirstElement();
        if (jSelectedProject != null)
        try {
          cViewer.setInput(sUtil.getLaunchConfigurations(jSelectedProject));
        }
        catch (CoreException e) {
          throw new RuntimeException(e);
        }
        selectedConfig = null;
        wPage.setPageComplete(selectedConfig != null);
      }});
    lViewer.setInput(sUtil.getProjects());           
    GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;
    setLayout(gridLayout);
    createTblTargets();
    setSize(new Point(412, 270));
    createOutputOptions();
    createOtherOptions();
    
  }

  private void createTblTargets()
  {
    GridData gridData2 = new GridData();
    gridData2.grabExcessHorizontalSpace = true;
    gridData2.grabExcessVerticalSpace = true;
    gridData2.verticalAlignment = org.eclipse.swt.layout.GridData.FILL;
    gridData2.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
    tblTargets = new Table(this, SWT.RADIO | SWT.BORDER  | SWT.V_SCROLL);
    tblTargets.setHeaderVisible(false);
    tblTargets.setLayoutData(gridData2);
    tblTargets.setLinesVisible(false);
    cViewer = new CheckboxTableViewer(tblTargets);
    
    //cViewer.setChecked(util.getLastRunConfig(), true);  // would be nice...
    
    cViewer.addPostSelectionChangedListener(new ISelectionChangedListener() { 
      public void selectionChanged(SelectionChangedEvent event)
      {
        StructuredSelection sSelection = (StructuredSelection)event.getSelection();
        selectedConfig = (ILaunchConfiguration)sSelection.getFirstElement();
        wPage.setPageComplete(selectedConfig != null && jSelectedProject != null);
      }}
    );
    cViewer.setContentProvider(new BaseWorkbenchContentProvider()
    {
      public Object[] getElements(Object element) {
        if(element instanceof ArrayList) {
          ArrayList a = (ArrayList)element;
          return a.toArray();
        }
        return null;
      }
    });
    cViewer.setLabelProvider(new LabelProvider()
    {
      public String getText(Object element) {
        ILaunchConfiguration i = (ILaunchConfiguration)element;
        return i.getName();
      }
    });    
  }

  private void createOutputOptions()
  {
    label6 = new Label(this, SWT.NONE);// hack for space
    label5 = new Label(this, SWT.NONE);
    label4 = new Label(this, SWT.NONE);
    
    GridData gridData5 = new GridData();
    gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
    gridData5.grabExcessHorizontalSpace = true;
    
    GridLayout gridLayout1 = new GridLayout();
    gridLayout1.numColumns = 2;
    
    GridData gridData4 = new GridData();
    gridData4.grabExcessHorizontalSpace = true;
    gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
    gridData4.verticalSpan = 2;
    gridData4.horizontalSpan = 2;
    
    exportTypeGroup = new Group(this, SWT.NONE);
    exportTypeGroup.setText("Output");
    exportTypeGroup.setLayout(gridLayout1);
    exportTypeGroup.setLayoutData(gridData4);
    
    radioApplet = new Button(exportTypeGroup, SWT.RADIO);
    radioApplet.setText("Applet");
    radioApplet.addSelectionListener(this);
        
    radioApplication = new Button(exportTypeGroup, SWT.RADIO);
    radioApplication.setText("Application");
    radioApplication.setLayoutData(gridData5);
    radioApplication.addSelectionListener(this);
  }
  
  private void createOtherOptions()
  {    
    GridData gridData5 = new GridData();
    gridData5.horizontalAlignment = org.eclipse.swt.layout.GridData.CENTER;
    gridData5.grabExcessHorizontalSpace = true;
    
    GridLayout gridLayout1 = new GridLayout();
    gridLayout1.numColumns = 2;
    
    GridData gridData4 = new GridData();
    gridData4.grabExcessHorizontalSpace = true;  
    gridData4.horizontalAlignment = org.eclipse.swt.layout.GridData.FILL;
    //gridData4.verticalAlignment = org.eclipse.swt.layout.GridData.CENTER;
    gridData4.verticalSpan = 2;
    gridData4.horizontalSpan = 2;
    
    optionsGroup = new Group(this, SWT.NONE);
    optionsGroup.setText("Options");
    optionsGroup.setLayout(gridLayout1);
    optionsGroup.setLayoutData(gridData4);   

    // Create present option buttons
/*    presentModeButton = new Button(optionsGroup, SWT.CHECK);
    presentModeButton.setText("Full-Screen (Present mode)");
    presentModeButton.addSelectionListener(this);
    presentModeButton.setEnabled(false);
    
    showStopButton = new Button(optionsGroup, SWT.CHECK);
    showStopButton.setText("Show a Stop Button");    
    showStopButton.addSelectionListener(this);
    showStopButton.setEnabled(false);
    showStopButton.setSelection(true);*/
        
    // Create jar option button
    multipleJarsButton = new Button(optionsGroup, SWT.CHECK);
    multipleJarsButton.setText("Force 'Multiple-Jars' (for custom OpenGL renderers)");
    multipleJarsButton.addSelectionListener(this);
  }

  public void widgetDefaultSelected(SelectionEvent e) { }

  public void widgetSelected(SelectionEvent e)
  {
    //System.out.println("P5ExportComp.widgetSelected("+e+")");
    
    // handle the export type 
    Button b = (Button)e.getSource();        
    if (b==radioApplet || b==radioApplication) 
      bCurrentlySelected = b;
    
    // get jar option state
    forceMultipleJars = multipleJarsButton.getSelection();
    multipleJarsButton.setEnabled(bCurrentlySelected==radioApplet);
    
    // check for present and hide options
    /* usePresentMode = presentModeButton.getSelection();    
    addStopButton = (usePresentMode && showStopButton.getSelection());      
    presentModeButton.setEnabled(bCurrentlySelected==radioApplication); 
    showStopButton.setEnabled(bCurrentlySelected==radioApplication && usePresentMode);*/
  }
  
  // Getters for info about the panel ========================
  
  public P5ExportType getSelectedExportType()
  {      
    return (P5ExportType)builderMap.get(bCurrentlySelected); 
  }
  
  public IJavaProject getSelectedJavaProject()
  {
    return jSelectedProject;
  }
  
  public boolean getForceMultipleJars()
  {
    return forceMultipleJars;
  }
  
  public ILaunchConfiguration getConfiguration()
  {
    return selectedConfig;
  }

  public boolean getUsePresentMode() {
    return usePresentMode;
  }
  
  
  public boolean getAddStopButton() {
    return addStopButton;
  }
 
}// end



