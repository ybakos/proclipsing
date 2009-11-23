package proclipsing.core.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import proclipsing.os.OSHelperManager;

public class ProcessingPathDrawer {

    private static String PROCESSING_APP_PATH_LABEL         = "Processing Path";
    private static String PROCESSING_SKETCH_PATH_LABEL      = "Processing Sketch Path";
    private static String DIR_SEARCH_BUTTON_LABEL           = "Browse...";    
    private static int    PATH_TEXT_WIDTH_HINT              = 350;
    private static int    LABEL_WIDTH_HINT                  = 150;
    
    private Text processing_app_path_text;
    private Text processing_sketch_path_text;
    
    public Composite draw(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));
        drawProcessingAppFinder(composite);
        drawProcessingSketchFinder(composite);
        return composite;
    }
    
    public void drawProcessingAppFinder(final Composite composite) {

        Label processingPathLabel = new Label(composite, SWT.NONE);
        processingPathLabel.setText(PROCESSING_APP_PATH_LABEL);
        GridData gd1 = new GridData();
        gd1.widthHint = LABEL_WIDTH_HINT;
        processingPathLabel.setLayoutData(gd1);
        
        processing_app_path_text = new Text(composite, SWT.NONE | SWT.BORDER );
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = PATH_TEXT_WIDTH_HINT;
        processing_app_path_text.setLayoutData(gd);

        Button button = new Button(composite, SWT.PUSH);
        button.setText(DIR_SEARCH_BUTTON_LABEL);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
            Dialog dialog = OSHelperManager.getHelper().getDialog(composite.getShell());
            
            if(dialog instanceof FileDialog)
                processing_app_path_text.setText(((FileDialog)dialog).open());
            else if(dialog instanceof DirectoryDialog)
                processing_app_path_text.setText(((DirectoryDialog)dialog).open());
                
            }
        });        
    }
    
    public void drawProcessingSketchFinder(final Composite composite) {
        
        Label processingPathLabel = new Label(composite, SWT.NONE);
        processingPathLabel.setText(PROCESSING_SKETCH_PATH_LABEL);
        GridData gd1 = new GridData();
        gd1.widthHint = LABEL_WIDTH_HINT;
        processingPathLabel.setLayoutData(gd1);
        
        processing_sketch_path_text = new Text(composite, SWT.NONE | SWT.BORDER );
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = PATH_TEXT_WIDTH_HINT;
        processing_sketch_path_text.setLayoutData(gd);

        Button button = new Button(composite, SWT.PUSH);
        button.setText(DIR_SEARCH_BUTTON_LABEL);
        button.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                DirectoryDialog dialog = new DirectoryDialog(composite.getShell());
                processing_sketch_path_text.setText(((DirectoryDialog)dialog).open());
            }
        });
    }    
    
    public String getProcessingPath() {
        return processing_app_path_text.getText();
    }
    
    public String getSketchPath() {
        return processing_sketch_path_text.getText();
    }
    
    public Text getProcessingPathTextWidget() {
        return processing_app_path_text;
    }
    
    public Text getSketchPathTextWidget() {
        return processing_sketch_path_text;
    }
    
    
}
