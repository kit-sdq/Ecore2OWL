package edu.kit.ipd.are.ecore2owl.ui.listener.fileopening;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.kit.ipd.are.ecore2owl.ui.listener.FileButtonListener;

public abstract class OpenButtonListener extends FileButtonListener {
    /**
     * Instantiates a new local file system button selection adapter.
     *
     * @param field
     *            the field
     * @param fileExtension
     *            the file extension
     * @param dialogTitle
     *            the dialog title
     * @param shell
     *            the shell
     */
    public OpenButtonListener(Text field, String[] fileExtension, String dialogTitle, Shell shell) {
        super(field, fileExtension, dialogTitle, shell);

    }

    /**
     * Instantiates a new local file system button selection adapter.
     *
     * @param field
     *            the field
     * @param fileExtension
     *            the file extension
     * @param dialogTitle
     *            the dialog title
     * @param shell
     *            the shell
     * @param useFolder
     *            the use folder
     * @param useMultipleSelection
     *            if true, multiple files can be selected.
     */
    public OpenButtonListener(Text field, String[] fileExtension, String dialogTitle, Shell shell,
            boolean useMultipleSelection) {
        super(field, fileExtension, dialogTitle, shell, false, useMultipleSelection);
    }

    public abstract String openFileDialog(Text textField, String[] fileExtension, boolean multipleSelection);

    public abstract String openFileDialog(Text textField, String[] fileExtension);

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e) {
        String selectedFile = null;
        if (useMultipleSelection) {
            selectedFile = openFileDialog(field, extensions, true);
        } else {
            selectedFile = openFileDialog(field, extensions);
        }
        if (selectedFile != null && !selectedFile.isEmpty()) {
            field.setText(selectedFile);
        }
    }
}
