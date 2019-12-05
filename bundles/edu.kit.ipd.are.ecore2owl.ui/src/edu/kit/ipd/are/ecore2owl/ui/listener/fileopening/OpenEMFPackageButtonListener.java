package edu.kit.ipd.are.ecore2owl.ui.listener.fileopening;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.plugin.EcorePlugin;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class OpenEMFPackageButtonListener extends OpenButtonListener {

    public OpenEMFPackageButtonListener(Text field, String[] fileExtension, String dialogTitle,
            org.eclipse.swt.widgets.Shell shell) {
        super(field, fileExtension, dialogTitle, shell);
    }

    @Override
    public String openFileDialog(Text textField, String[] fileExtension, boolean multipleSelection) {
        ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), new LabelProvider() {
            @Override
            public Image getImage(Object element) {
                return PlatformUI.getWorkbench()
                                 .getSharedImages()
                                 .getImage(ISharedImages.IMG_OBJ_FILE);
            }
        });
        dialog.setMultipleSelection(multipleSelection);
        String msg = "Select registered meta-model package(s)";
        dialog.setMessage(msg);
        dialog.setTitle(msg);
        dialog.setElements(getEMFPlugins());

        dialog.open();
        Object[] results = dialog.getResult();
        if (results != null) {
            Map<String, URI> ePackageNsURIToGenModelLocationMap = EcorePlugin.getEPackageNsURIToGenModelLocationMap(
                    false);
            return Arrays.stream(results)
                         .map(result -> {
                             URI resultGenModelURI = ePackageNsURIToGenModelLocationMap.get(result.toString());
                             return resultGenModelURI.toString()
                                                     .replace(".genmodel", ".ecore");
                         })
                         .collect(Collectors.joining(";"));
        } else {
            return "";
        }

    }

    @Override
    public String openFileDialog(Text textField, String[] fileExtension) {
        return openFileDialog(textField, fileExtension, false);
    }

    private Object[] getEMFPlugins() {
        Object[] returnArray = EPackage.Registry.INSTANCE.keySet()
                                                         .toArray(new Object[EPackage.Registry.INSTANCE.size()]);
        return returnArray;
    }

}
