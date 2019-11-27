package edu.kit.ipd.are.ecore2owl.ui.listener.filesaver;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.kit.ipd.are.ecore2owl.ui.listener.FileButtonListener;

public abstract class SaveButtonListener extends FileButtonListener {

    public SaveButtonListener(Text field, String[] fileExtensions, String dialogTitle, Shell shell) {
        super(field, fileExtensions, dialogTitle, shell);
    }

    public abstract String saveFileDialog(Text textField, String[] fileExtension);

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e) {
        String selectedFile = saveFileDialog(getField(), getExtensions());
        if (selectedFile != null) {
            field.setText(selectedFile);
        }
    }

    protected Text getField() {
        return this.field;
    }

    protected String[] getExtensions() {
        return this.extensions;
    }

}
