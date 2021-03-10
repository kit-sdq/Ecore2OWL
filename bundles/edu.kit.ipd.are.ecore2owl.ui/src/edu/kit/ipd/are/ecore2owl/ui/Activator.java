package edu.kit.ipd.are.ecore2owl.ui;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import edu.kit.ipd.are.ecore2owl.core.Ecore2OWLTransformer;
import edu.kit.ipd.are.ecore2owl.ontology.OntologyAccess;

public class Activator implements BundleActivator {
    protected static final Level LOGGING_LEVEL = Level.DEBUG;
    private static final String PATTERN = "%d{HH:mm:ss} [%-5p | %c]: %m%n";

    private static BundleContext context;

    static BundleContext getContext() {
        return context;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        context = bundleContext;
        configureLogger();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        // nothing
    }

    private void configureLogger() {
        Logger.getRootLogger().getLoggerRepository().resetConfiguration();

        Logger.getLogger(Ecore2OwlLaunchConfigurationDelegate.class).addAppender(getConsoleAppender());
        Logger.getLogger(Ecore2OWLTransformer.class).addAppender(getConsoleAppender());
        Logger.getLogger(OntologyAccess.class).addAppender(getConsoleAppender());
    }

    protected static Appender getConsoleAppenderWithLevel(Level level) {
        ConsoleAppender console = new ConsoleAppender();

        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(level);
        console.activateOptions();
        return console;
    }

    protected static Appender getConsoleAppender() {
        return getConsoleAppenderWithLevel(LOGGING_LEVEL);
    }

}
