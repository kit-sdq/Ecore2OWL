package edu.kit.ipd.are.ecore2owl.ui;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

public class Ecore2OwlLaunchConfigurationTabGroup extends AbstractLaunchConfigurationTabGroup {

    /**
     * Standard constructor
     */
    public Ecore2OwlLaunchConfigurationTabGroup() {
        super();
    }

    @Override
    public void createTabs(ILaunchConfigurationDialog arg0, String arg1) {
        setTabs(new ILaunchConfigurationTab[] { new Ecore2OwlLaunchConfigurationTab() });
    }

}
