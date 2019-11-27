package edu.kit.ipd.are.ecore2owl.ui.listener.fileopening;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import edu.kit.ipd.are.ecore2owl.ui.listener.FilePatternFilter;

/**
 * Copy of de.uka.ipd.sdq.workflow.launchconfig.tabs.WorkspaceButtonSelectionListener for import reasons
 * 
 * @author Jan Keim
 *
 */
public class OpenWorkspaceButtonListener extends OpenButtonListener {
    /**
     * Instantiates a new workspace button selection listener.
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
    public OpenWorkspaceButtonListener(Text field, String[] fileExtension, String dialogTitle, Shell shell) {
        super(field, fileExtension, dialogTitle, shell, false);
    }

    /**
     * Instantiates a new workspace button selection listener.
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
    public OpenWorkspaceButtonListener(Text field, String[] fileExtension, String dialogTitle, Shell shell,
            boolean useMultipleSelection) {
        super(field, fileExtension, dialogTitle, shell, useMultipleSelection);
    }

    @Override
    public String openFileDialog(Text textField, String[] fileExtension) {
        List<ViewerFilter> filters = getFiltersFromExtensions(fileExtension);

        IFile[] files = WorkspaceResourceDialog.openFileSelection(this.getShell(), null, this.getDialogTitle(), false,
                null, filters);

        if (files.length != 0 && files[0] != null) {
            return createPlatformFileString(files[0]);
        } else {
            return null;
        }
    }

    private List<ViewerFilter> getFiltersFromExtensions(String[] fileExtension) {
        List<ViewerFilter> filters = new ArrayList<ViewerFilter>();
        if (fileExtension != null) {
            FilePatternFilter filter = new FilePatternFilter();
            filter.setPatterns(fileExtension);
            filters.add(filter);
        }
        return filters;
    }

    @Override
    public String openFileDialog(Text textField, String[] fileExtension, boolean multipleSelection) {
        List<ViewerFilter> filters = getFiltersFromExtensions(fileExtension);

        IFile[] files = WorkspaceResourceDialog.openFileSelection(this.getShell(), null, this.getDialogTitle(),
                multipleSelection, null, filters);

        if (files.length != 0 && files[0] != null) {
            if (multipleSelection) {
                return Arrays.stream(files)
                             .filter(f -> f != null)
                             .map(f -> createPlatformFileString(f))
                             .collect(Collectors.joining(";"));
            } else {
                return createPlatformFileString(files[0]);
            }
        }
        return null;
    }
}
