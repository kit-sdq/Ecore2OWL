package edu.kit.ipd.are.ecore2owl.ui.listener.filesaver;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.kit.ipd.are.ecore2owl.ui.listener.FilePatternFilter;

/**
 * Copy of de.uka.ipd.sdq.workflow.launchconfig.tabs.LocalFileSystemButtonSelectionAdapter for import reasons
 * 
 * @author Jan Keim
 *
 */
public class SaveWorkspaceButtonListener extends SaveButtonListener {

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
    public SaveWorkspaceButtonListener(Text field, String[] fileExtension, String dialogTitle, Shell shell) {
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
        List<ViewerFilter> filters = new ArrayList<ViewerFilter>();
        if (fileExtension != null) {
            FilePatternFilter filter = new FilePatternFilter();
            filter.setPatterns(fileExtension);
            filters.add(filter);
        }

        IFile file = WorkspaceResourceDialog.openNewFile(getShell(), getDialogTitle(), "Save as ..", null, filters);
        return createPlatformFileString(file);
    }

}
