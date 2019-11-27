package edu.kit.ipd.are.ecore2owl.ui.listener.fileopening;

import java.io.File;
import java.util.Arrays;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Copy of de.uka.ipd.sdq.workflow.launchconfig.tabs.LocalFileSystemButtonSelectionAdapter for import reasons
 * 
 * @author Jan Keim
 *
 */
public class OpenLocalFileSystemButtonListener extends OpenButtonListener {

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
    public OpenLocalFileSystemButtonListener(Text field, String[] fileExtension, String dialogTitle, Shell shell) {
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
    public OpenLocalFileSystemButtonListener(Text field, String[] fileExtension, String dialogTitle, Shell shell,
            boolean useMultipleSelection) {
        super(field, fileExtension, dialogTitle, shell, useMultipleSelection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see edu.kit.ipd.are.emf2owl.ui.listener.FileButtonListener#openFileDialog(org.eclipse.swt.widgets.Text,
     * java.lang.String[])
     */
    @Override
    public String openFileDialog(Text textField, String[] fileExtension) {
        FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
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

    /*
     * (non-Javadoc)
     * 
     * @see edu.kit.ipd.are.emf2owl.ui.listener.FileButtonListener#openFileDialog(org.eclipse.swt.widgets.Text,
     * java.lang.String[], boolean)
     */
    @Override
    public String openFileDialog(Text textField, String[] fileExtension, boolean multipleSelection) {
        FileDialog dialog = new FileDialog(getShell(), SWT.MULTI);
        dialog.setFilterExtensions(fileExtension);
        dialog.setText(getDialogTitle());
        String filename = "";
        StringTokenizer tokenizer = new StringTokenizer(textField.getText(), ";");
        if (tokenizer.countTokens() > 0) {
            dialog.setFileName(tokenizer.nextToken());
        } else {
            dialog.setFileName(textField.getText());
        }

        if (dialog.open() != null) {
            String root = dialog.getFilterPath() + File.separatorChar;
            if (multipleSelection) {
                filename = Arrays.stream(dialog.getFileNames())
                                 .map(f -> root + f)
                                 .collect(Collectors.joining(";"));
            } else {
                filename = root + dialog.getFileName();
            }
        }

        return filename;
    }
}
