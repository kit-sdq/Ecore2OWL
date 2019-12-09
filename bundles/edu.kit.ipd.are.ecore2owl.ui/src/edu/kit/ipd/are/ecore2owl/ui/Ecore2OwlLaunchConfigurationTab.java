package edu.kit.ipd.are.ecore2owl.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import edu.kit.ipd.are.ecore2owl.ui.listener.fileopening.OpenEMFPackageButtonListener;
import edu.kit.ipd.are.ecore2owl.ui.listener.fileopening.OpenLocalFileSystemButtonListener;
import edu.kit.ipd.are.ecore2owl.ui.listener.fileopening.OpenWorkspaceButtonListener;
import edu.kit.ipd.are.ecore2owl.ui.listener.filesaver.SaveLocalFileSystemButtonListener;
import edu.kit.ipd.are.ecore2owl.ui.listener.filesaver.SaveWorkspaceButtonListener;

public class Ecore2OwlLaunchConfigurationTab extends AbstractLaunchConfigurationTab {
    private static final String TEXT_LOAD_ECORE_FILE = "Load Ecore File";
    private static final String TEXT_LOAD_MODEL_FILE = "Load Model File";
    private static Logger logger = Logger.getLogger("ADLLaunchConfigurationTab");
    private Text textEcoreIn;
    private Text textOntology;
    private Text textModelIn;

    private Button autoLoadMetaModelButton;
    private boolean autoLoadMetaModel = true;
    private List<Control> autoLoadMetaModelWidgets = new ArrayList<>();

    private Button loadFromModelButton;

    private List<Control> loadFromModelWidgets = new ArrayList<>();

    private static final String[] ecoreFileExtensions = new String[] { "*.ecore" };
    private static final String[] modelFileExtensions = new String[] { "*" };
    private static final String[] owlFileExtensions = new String[] { "*.owl" };

    @Override
    public void createControl(Composite parent) {
        final Composite container = new Composite(parent, SWT.NONE);
        setControl(container);

        GridLayoutFactory.swtDefaults()
                         .applyTo(container);

        final ModifyListener modifyListener = (ModifyEvent e) -> {
            setDirty(true);
            updateLaunchConfigurationDialog();
        };

        org.eclipse.swt.widgets.Shell shell = getShell();

        textEcoreIn = new Text(container, SWT.SINGLE | SWT.BORDER);
        textEcoreIn.setToolTipText("Input meta-model file(s) [*.ecore]");
        autoLoadMetaModelWidgets.add(textEcoreIn);
        createMetaModelInputSection(container, modifyListener, "Meta-Model input (Ecore)", ecoreFileExtensions,
                textEcoreIn, shell);

        textModelIn = new Text(container, SWT.SINGLE | SWT.BORDER);
        textModelIn.setToolTipText("Input model file(s)");
        loadFromModelWidgets.add(textModelIn);
        createModelInputSection(container, modifyListener, "Model input", modelFileExtensions, textModelIn, shell);

        textOntology = new Text(container, SWT.SINGLE | SWT.BORDER);
        textOntology.setToolTipText("Output ontology file (owl)");
        createOutputSection(container, modifyListener, "Ontology file", owlFileExtensions, textOntology,
                "Set Ontology file", shell);
    }

    private void createTextInFileGroup(final Group fileGroup, final ModifyListener modifyListener,
            final String groupLabel, Text text) {
        fileGroup.setText(groupLabel);
        fileGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        int hintWidth = 200;
        text.setParent(fileGroup);
        GridDataFactory.swtDefaults()
                       .align(SWT.FILL, SWT.CENTER)
                       .grab(true, false)
                       .hint(hintWidth, SWT.DEFAULT)
                       .applyTo(text);
        text.addModifyListener(modifyListener);
    }

    private List<Button> createButtonsInFileGroup(Group fileGroup, SelectionListener workspaceListener,
            SelectionListener localFileSystemListener) {
        final Button workspaceButton = new Button(fileGroup, SWT.NONE);
        workspaceButton.setText("Workspace...");
        workspaceButton.addSelectionListener(workspaceListener);

        final Button localFileSystemButton = new Button(fileGroup, SWT.NONE);
        localFileSystemButton.setText("File System...");
        localFileSystemButton.addSelectionListener(localFileSystemListener);

        return List.of(workspaceButton, localFileSystemButton);
    }

    private void createMetaModelInputSection(final Composite parentContainer, final ModifyListener modifyListener,
            final String groupLabel, final String[] fileExtensions, Text textFileNameToLoad,
            org.eclipse.swt.widgets.Shell dialogShell) {
        final Group inputGroup = new Group(parentContainer, SWT.NONE);
        int numColumns = 4;
        GridLayoutFactory.swtDefaults()
                         .numColumns(numColumns)
                         .applyTo(inputGroup);
        createTextInFileGroup(inputGroup, modifyListener, groupLabel, textFileNameToLoad);

        SelectionListener workspaceListener = new OpenWorkspaceButtonListener(textFileNameToLoad, fileExtensions,
                TEXT_LOAD_ECORE_FILE, dialogShell, true);
        SelectionListener localFileSystemListener = new OpenLocalFileSystemButtonListener(textFileNameToLoad,
                fileExtensions, TEXT_LOAD_ECORE_FILE, dialogShell, true);

        autoLoadMetaModelWidgets.addAll(
                createButtonsInFileGroup(inputGroup, workspaceListener, localFileSystemListener));

        SelectionListener pluginsListener = new OpenEMFPackageButtonListener(textFileNameToLoad, fileExtensions,
                TEXT_LOAD_ECORE_FILE, dialogShell);
        final Button pluginsButton = new Button(inputGroup, SWT.NONE);
        pluginsButton.setText("Plugin...");
        pluginsButton.addSelectionListener(pluginsListener);

        autoLoadMetaModelWidgets.add(pluginsButton);

        autoLoadMetaModelButton = new Button(inputGroup, SWT.CHECK);
        autoLoadMetaModelButton.setSelection(autoLoadMetaModel);
        autoLoadMetaModelButton.setText("Automatically load meta-model (ecore)");
        autoLoadMetaModelButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent event) {
                autoLoadMetaModel = !autoLoadMetaModel;
                updateAutoLoad();
                setDirty(true);
                updateLaunchConfigurationDialog();
            }
        });
        loadFromModelWidgets.add(autoLoadMetaModelButton);
    }

    private void updateAutoLoad() {
        autoLoadMetaModelButton.setSelection(autoLoadMetaModel);
        for (Control widget : autoLoadMetaModelWidgets) {
            widget.setEnabled(!autoLoadMetaModel);
        }
    }

    private void createModelInputSection(final Composite parentContainer, final ModifyListener modifyListener,
            final String groupLabel, final String[] fileExtensions, Text textFileNameToLoad,
            org.eclipse.swt.widgets.Shell dialogShell) {
        final Group inputGroup = new Group(parentContainer, SWT.NONE);
        int numColumns = 3;
        GridLayoutFactory.swtDefaults()
                         .numColumns(numColumns)
                         .applyTo(inputGroup);

        createTextInFileGroup(inputGroup, modifyListener, groupLabel, textFileNameToLoad);

        SelectionListener workspaceListener = new OpenWorkspaceButtonListener(textFileNameToLoad, fileExtensions,
                TEXT_LOAD_MODEL_FILE, dialogShell, true);
        SelectionListener localFileSystemListener = new OpenLocalFileSystemButtonListener(textFileNameToLoad,
                fileExtensions, TEXT_LOAD_MODEL_FILE, dialogShell, true);

        List<Button> buttonsInFileGroup = createButtonsInFileGroup(inputGroup, workspaceListener,
                localFileSystemListener);
        loadFromModelWidgets.addAll(buttonsInFileGroup);
    }

    private void createOutputSection(Composite parentContainer, ModifyListener modifyListener, String groupLabel,
            String[] owlFileExtensions, Text textSaveFile, String dialogTitle,
            org.eclipse.swt.widgets.Shell dialogShell) {
        final Group ouputGroup = new Group(parentContainer, SWT.NONE);
        int numColumns = 3;
        GridLayoutFactory.swtDefaults()
                         .numColumns(numColumns)
                         .applyTo(ouputGroup);
        createTextInFileGroup(ouputGroup, modifyListener, groupLabel, textSaveFile);

        SelectionListener workspaceListener = new SaveWorkspaceButtonListener(textOntology, owlFileExtensions,
                dialogTitle, dialogShell);
        SelectionListener localFileSystemListener = new SaveLocalFileSystemButtonListener(textOntology,
                owlFileExtensions, dialogTitle, dialogShell);

        createButtonsInFileGroup(ouputGroup, workspaceListener, localFileSystemListener);
    }

    @Override
    public String getName() {
        return "Ecore2Owl Launch Tab";
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            String ecoreInText = configuration.getAttribute(Ecore2OwlConfigurationAttributes.ECORE_IN, "");
            textEcoreIn.setText(ecoreInText);
            String modelInText = configuration.getAttribute(Ecore2OwlConfigurationAttributes.MODEL_IN, "");
            textModelIn.setText(modelInText);
            String owlOutText = configuration.getAttribute(Ecore2OwlConfigurationAttributes.OWL_OUT, "");
            textOntology.setText(owlOutText);
            autoLoadMetaModel = configuration.getAttribute(Ecore2OwlConfigurationAttributes.AUTOLOAD, true);
            updateAutoLoad();
        } catch (CoreException e) {
            logger.warning(e.getMessage());
        }
    }

    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        String ecoreIn = textEcoreIn.getText();
        configuration.setAttribute(Ecore2OwlConfigurationAttributes.ECORE_IN, ecoreIn);

        String modelIn = textModelIn.getText();
        configuration.setAttribute(Ecore2OwlConfigurationAttributes.MODEL_IN, modelIn);

        String owlOut = textOntology.getText();
        configuration.setAttribute(Ecore2OwlConfigurationAttributes.OWL_OUT, owlOut);
        configuration.setAttribute(Ecore2OwlConfigurationAttributes.AUTOLOAD, autoLoadMetaModel);
    }

    @Override
    public boolean isValid(ILaunchConfiguration launchConfig) {
        String ecoreInput = "";
        String modelInput = "";
        String owlOutput = "";
        try {
            ecoreInput = launchConfig.getAttribute(Ecore2OwlConfigurationAttributes.ECORE_IN, "");
            modelInput = launchConfig.getAttribute(Ecore2OwlConfigurationAttributes.MODEL_IN, "");
            owlOutput = launchConfig.getAttribute(Ecore2OwlConfigurationAttributes.OWL_OUT, "");
        } catch (CoreException e) {
            logger.warning(e.getMessage());
            return false;
        }

        if ((ecoreInput.isEmpty() && !autoLoadMetaModel) || owlOutput.isEmpty() || modelInput.isEmpty()) {
            return false;
        }

        if (!autoLoadMetaModel && !validateEcoreInputs(ecoreInput)) {
            return false;
        }

        if (!validateModelInputs(modelInput)) {
            return false;
        }

        return validateFilenameExtensions(owlOutput, owlFileExtensions);
    }

    private boolean validateModelInputs(String modelInput) {
        for (String modelInputFile : modelInput.split(";")) {
            if (modelInputFile.trim()
                              .endsWith("_diagram")) {
                return false;
            }
        }
        return true;
    }

    private boolean validateEcoreInputs(String ecoreInput) {
        for (String ecoreInputFile : ecoreInput.split(";")) {
            if (!validateFilenameExtensions(ecoreInputFile, ecoreFileExtensions)) {
                return false;
            }
        }
        return true;
    }

    private static boolean validateFilenameExtensions(String filePath, String[] extensions) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        return Arrays.stream(extensions)
                     .map(extension -> extension.replace("*", ""))
                     .allMatch(filePath::endsWith);
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(Ecore2OwlConfigurationAttributes.ECORE_IN, "");
        configuration.setAttribute(Ecore2OwlConfigurationAttributes.MODEL_IN, "");
        configuration.setAttribute(Ecore2OwlConfigurationAttributes.OWL_OUT, "");
        configuration.setAttribute(Ecore2OwlConfigurationAttributes.AUTOLOAD, true);
    }
}
