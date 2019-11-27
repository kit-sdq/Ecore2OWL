package edu.kit.ipd.are.ecore2owl.ui.listener;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.views.navigator.ResourcePatternFilter;

/**
 * copied from de.uka.ipd.sdq.workflow.launchconfig.tabs.FilePatternFilter for import reasons
 * 
 * @author Jan Keim
 *
 */
@SuppressWarnings("deprecation")
public class FilePatternFilter extends ResourcePatternFilter {

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.views.navigator.ResourcePatternFilter#select(org.eclipse .jface.viewers.Viewer,
     * java.lang.Object, java.lang.Object)
     */
    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (element instanceof IResource) {
            IResource resource = (IResource) element;
            if (resource.getType() == IResource.ROOT || resource.getType() == IResource.PROJECT
                    || resource.getType() == IResource.FOLDER) {
                return true;
            } else {
                return !super.select(viewer, parentElement, element);
            }
        } else {
            return true;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.views.navigator.ResourcePatternFilter#setPatterns(java.lang.String[])
     */
    @Override
    public void setPatterns(String[] newPatterns) {
        super.setPatterns(newPatterns);
    }
}
