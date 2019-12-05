package edu.kit.ipd.are.ecore2owl.ui.listener.filesaver;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Text;

/**
 * Copy of de.uka.ipd.sdq.workflow.launchconfig.tabs.LocalFileSystemButtonSelectionAdapter for import reasons
 *
 * @author Jan Keim
 *
 */
public class SaveLocalFileSystemButtonListener extends SaveButtonListener {

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
    public SaveLocalFileSystemButtonListener(Text field, String[] fileExtension, String dialogTitle,
            org.eclipse.swt.widgets.Shell shell) {
        super(field, fileExtension, dialogTitle, shell);

    }

    /*
     * (non-Javadoc)
     *
     * @see edu.kit.ipd.are.emf2owl.ui.listener.FileButtonListener#openFileDialog(org.eclipse.swt.widgets.Text,
     * java.lang.String[])
     */
    @Override
    public String saveFileDialog(Text textField, String[] fileExtension) {
        FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
        dialog.setFilterExtensions(fileExtension);
        dialog.setText(getDialogTitle());
        dialog.setFileName(textField.getText());

        String filename = null;
        if (dialog.open() != null) {
            String root = dialog.getFilterPath() + File.separatorChar;
            filename = root + dialog.getFileName();
        }
        return filename;
    }

}
