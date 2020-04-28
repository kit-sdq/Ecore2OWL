package edu.kit.ipd.are.ecore2owl.ui.listener;

import org.eclipse.core.resources.IFile;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public abstract class FileButtonListener extends SelectionAdapter {

    protected Text field;
    protected String[] extensions;
    protected Shell shell;
    protected String dialogTitle;
    protected boolean useFolder;
    protected boolean useMultipleSelection;

    public FileButtonListener(Text field, String[] fileExtensions, String dialogTitle, Shell shell) {
        this(field, fileExtensions, dialogTitle, shell, false, false);
    }

    public FileButtonListener(Text field, String[] fileExtensions, String dialogTitle, Shell shell, boolean useFolder) {
        this(field, fileExtensions, dialogTitle, shell, useFolder, false);
    }

    public FileButtonListener(Text field, String[] fileExtensions, String dialogTitle, Shell shell, boolean useFolder,
            boolean useMultipleSelection) {
        super();
        this.field = field;
        extensions = fileExtensions;
        this.dialogTitle = dialogTitle;
        this.shell = shell;
        this.useMultipleSelection = useMultipleSelection;
        this.useFolder = useFolder;
    }

    /**
     * @return the shell
     */
    public Shell getShell() {
        return shell;
    }

    /**
     * @return the dialogTitle
     */
    public String getDialogTitle() {
        return dialogTitle;
    }

    @Override
    public abstract void widgetSelected(SelectionEvent e);

    protected String createPlatformFileString(IFile file) {
        if (file == null) {
            return "";
        }
        String portableString = file.getFullPath()
                                    .toPortableString();
        return "platform:/resource" + portableString;
    }

}
