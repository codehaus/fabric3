package org.fabric3.runtime.embedded;

import org.fabric3.host.Names;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.contribution.ContributionSource;
import org.fabric3.host.contribution.FileContributionSource;
import org.fabric3.host.contribution.SyntheticContributionSource;
import org.fabric3.host.monitor.MonitorEventDispatcherFactory;
import org.fabric3.host.monitor.MonitorProxyService;
import org.fabric3.host.runtime.*;
import org.fabric3.host.util.FileHelper;
import org.fabric3.jmx.management.AbstractMBean;
import org.fabric3.runtime.embedded.api.EmbeddedProfile;
import org.fabric3.runtime.embedded.api.EmbeddedRuntime;
import org.fabric3.runtime.embedded.api.EmbeddedServer;
import org.fabric3.runtime.embedded.api.service.EmbeddedLogger;
import org.fabric3.runtime.embedded.api.service.EmbeddedSetup;
import org.fabric3.runtime.embedded.api.service.EmbeddedSharedFolders;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3SetupException;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3StartupException;
import org.fabric3.runtime.embedded.factory.EmbeddedMonitorEventDispatcherFactory;
import org.fabric3.runtime.embedded.service.EmbeddedMonitorEventDispatcher;
import org.fabric3.runtime.embedded.util.EmbeddedBootstrapHelper;
import org.fabric3.runtime.embedded.util.FileSystem;
import org.fabric3.runtime.standalone.server.Fabric3Server;
import org.w3c.dom.Document;

import javax.management.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.*;

import static org.fabric3.host.Names.MONITOR_FACTORY_URI;
import static org.fabric3.host.Names.RUNTIME_MONITOR_CHANNEL_URI;

/**
 * @author Michal Capo
 */
public class EmbeddedRuntimeImpl extends AbstractMBean implements EmbeddedRuntime {

    private static final String DOMAIN = "fabric3";
    private static final String RUNTIME_MBEAN = "fabric3:SubDomain=runtime, type=component, name=RuntimeMBean";

    /**
     * Runtime name.
     */
    private String mName;

    /**
     * Runtime mode.
     */
    private RuntimeMode mRuntimeMode;

    /**
     * Runtimes config.
     */
    private URL mSystemConfig;

    /**
     * Runtime folder.
     */
    private File mRuntimeFolder;

    /**
     * Configuration folder.
     */
    private File mConfigFolder;

    /**
     * Runtime profiles.
     */
    private Map<String, EmbeddedProfile> mProfiles = new HashMap<String, EmbeddedProfile>();

    /**
     * Fabric3 runtime.
     */
    private Fabric3Runtime mRuntime;

    /**
     * Coordinator.
     */
    private RuntimeCoordinator mCoordinator;

    /**
     * Servers setup.
     */
    private EmbeddedSetup mSetup;

    /**
     * Logger.
     */
    private EmbeddedLogger mLogger;

    /**
     * Shared folders manager.
     */
    private EmbeddedSharedFolders mSharedFolders;

    /**
     * Server reference.
     */
    private EmbeddedServer mServer;

    /**
     * A monitor for this server.
     */
    private Fabric3Server.ServerMonitor mMonitor;

    /**
     * Bean server.
     */
    private static MBeanServer mBeanServer;

    /**
     * Runtimes mode.
     */
    private RuntimeMode mMode;

    public EmbeddedRuntimeImpl(
            final String name,
            final String systemConfigPath,
            final RuntimeMode runtimeMode,
            final EmbeddedServer server,
            final EmbeddedSetup setup,
            final EmbeddedLogger logger,
            final EmbeddedSharedFolders sharedFolders,
            final EmbeddedProfile... profiles
    ) {
        super(new MBeanInfo(EmbeddedRuntimeImpl.class.toString(), null, null, null, null, null));

        mName = name;
        mRuntimeMode = runtimeMode;

        mSetup = setup;
        mLogger = logger;
        mSharedFolders = sharedFolders;
        mServer = server;

        // add all profiles from server
        for (EmbeddedProfile serverProfile : mServer.getProfiles()) {
            mProfiles.put(serverProfile.getName(), serverProfile);
        }
        // add runtime specific profiles
        for (EmbeddedProfile profile : profiles) {
            mProfiles.put(profile.getName(), profile);
        }

        try {
            initialize(systemConfigPath);
            createFolders();
            createRuntimeCoordinator();
        } catch (ParseException e) {
            throw new EmbeddedFabric3StartupException("Cannot start embedded runtime", e);
        } catch (IOException e) {
            throw new EmbeddedFabric3StartupException("Cannot start embedded runtime", e);
        } catch (URISyntaxException e) {
            throw new EmbeddedFabric3StartupException("Cannot start embedded runtime", e);
        } catch (ScanException e) {
            throw new EmbeddedFabric3StartupException("Cannot start embedded runtime", e);
        } catch (MalformedObjectNameException e) {
            throw new EmbeddedFabric3StartupException("Cannot start embedded runtime", e);
        } catch (InstanceAlreadyExistsException e) {
            throw new EmbeddedFabric3StartupException("Cannot start embedded runtime", e);
        } catch (NotCompliantMBeanException e) {
            throw new EmbeddedFabric3StartupException("Cannot start embedded runtime", e);
        } catch (MBeanRegistrationException e) {
            throw new EmbeddedFabric3StartupException("Cannot start embedded runtime", e);
        }
    }

    /**
     * Initialize name, runtime mode and runtime config to default values if not defined
     *
     * @param systemConfigPath string representation of runtime config
     */
    private void initialize(final String systemConfigPath) {
        String configPath = systemConfigPath;

        // setup name
        if (null == mName) {
            mName = "vm";
        }

        // setup runtime type
        if (null == mRuntimeMode) {
            mRuntimeMode = RuntimeMode.VM;
        }

        // setup runtime folder
        mRuntimeFolder = FileSystem.folder(mSetup.getServerFolder(), "runtimes/" + mName);
        if (!mRuntimeFolder.exists()) {
            if (!mRuntimeFolder.mkdirs()) {
                throw new EmbeddedFabric3SetupException("Cannot create folder: " + mRuntimeFolder.getAbsolutePath());
            }
        }

        // setup config path
        if (null == configPath) {
            switch (mRuntimeMode) {
                case VM:
                    configPath = "/config/systemConfigVm.xml";
                    break;
                case CONTROLLER:
                    configPath = "/config/systemConfigController.xml";
                    break;
                case PARTICIPANT:
                    configPath = "/config/systemConfigParticipant.xml";
                    break;
                default:
                    throw new EmbeddedFabric3SetupException("Unknown runtime type: " + mRuntimeMode);
            }
        } else {
            if (null == configPath || 0 == configPath.trim().length()) {
                throw new EmbeddedFabric3SetupException("System config path cannot be null or empty");
            }

            if (!FileSystem.isAbsolute(configPath)) {
                throw new EmbeddedFabric3SetupException("Path is not absolute: " + configPath);
            }
        }
        try {
            mSystemConfig = FileSystem.fileAtClassPath(configPath);
        } catch (URISyntaxException e) {
            throw new EmbeddedFabric3SetupException(MessageFormat.format("Cannot find runtime configuration file: {0}", configPath));
        } catch (MalformedURLException e) {
            throw new EmbeddedFabric3SetupException(MessageFormat.format("Cannot find runtime configuration file: {0}", configPath));
        }
    }

    /**
     * Create runtime folders (repository, deploy, tmp, data) and copy configuration file, if they doesn't exists.
     */
    private void createFolders() {
        FileSystem.createFolders(FileSystem.folders(mRuntimeFolder, "repository/user", "repository/runtime", "deploy", "tmp", "data"));
        mConfigFolder = FileSystem.createFolder(FileSystem.folder(mRuntimeFolder, "config"));

        // copy system config file
        try {
            FileSystem.copy(mSystemConfig, FileSystem.file(mConfigFolder, "systemConfig.xml"));
        } catch (IOException e) {
            throw new EmbeddedFabric3SetupException(MessageFormat.format("Cannot find {0} file", mSystemConfig.toExternalForm()));
        }
    }

    private void createRuntimeCoordinator() throws ParseException, IOException, URISyntaxException, ScanException, MalformedObjectNameException, MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException {
        // create the classloaders for booting the runtime
        ClassLoader systemClassLoader = new MaskingClassLoader(ClassLoader.getSystemClassLoader(), "org.slf4j", "ch.qos.logback");
        ClassLoader libClassLoader = EmbeddedBootstrapHelper.createClassLoader(systemClassLoader, mSharedFolders.getLibFolder());

        // mask hidden JDK and system classpath packages
        ClassLoader maskingClassLoader = new MaskingClassLoader(libClassLoader, HiddenPackages.getPackages());
        ClassLoader hostLoader = BootstrapHelper.createClassLoader(maskingClassLoader, mSharedFolders.getHostFolder());
        ClassLoader bootLoader = EmbeddedBootstrapHelper.createClassLoader(hostLoader, mSharedFolders.getBootFolder(), mSharedFolders.getLibFolder());

        BootstrapService bootstrapService = BootstrapFactory.getService(bootLoader);

        // load the system configuration
        Document systemConfig = bootstrapService.loadSystemConfig(mConfigFolder);

        List<File> deployDirs = bootstrapService.parseDeployDirectories(systemConfig);

        URI domainName = bootstrapService.parseDomainName(systemConfig);
        mMode = bootstrapService.parseRuntimeMode(systemConfig);
        String zoneName = bootstrapService.parseZoneName(systemConfig);
        String runtimeName = bootstrapService.getRuntimeName(domainName, zoneName, mName, mRuntimeMode);

        // create the HostInfo and runtime
        HostInfo hostInfo = BootstrapHelper.createHostInfo(runtimeName, mRuntimeMode, domainName, mRuntimeFolder, mConfigFolder, mSharedFolders.getExtensionsFolder(), deployDirs);
        // clear out the tmp directory
        FileHelper.cleanDirectory(hostInfo.getTempDir());

        mBeanServer = MBeanServerFactory.createMBeanServer(DOMAIN);

        EmbeddedMonitorEventDispatcher runtimeDispatcher = new EmbeddedMonitorEventDispatcher();
        EmbeddedMonitorEventDispatcher appDispatcher = new EmbeddedMonitorEventDispatcher();
        RuntimeConfiguration runtimeConfig = new RuntimeConfiguration(hostInfo, mBeanServer, runtimeDispatcher, appDispatcher);

        mRuntime = bootstrapService.createDefaultRuntime(runtimeConfig);

        Map<String, String> exportedPackages = new HashMap<String, String>();
        exportedPackages.put("org.fabric3.runtime.ant.api", Names.VERSION);

        URL systemComposite = getClass().getResource("/boot/system.composite").toURI().toURL();

        ScanResult result = bootstrapService.scanRepository(hostInfo);

        // add runtime profiles
        for (EmbeddedProfile runtimeProfile : getProfiles()) {
            appendProfile(result, runtimeProfile);
        }

        BootConfiguration configuration = new BootConfiguration();

        List<ComponentRegistration> registrations = new ArrayList<ComponentRegistration>();
        EmbeddedMonitorEventDispatcherFactory factory = new EmbeddedMonitorEventDispatcherFactory();
        ComponentRegistration registration = new ComponentRegistration("MonitorEventDispatcherFactory",
                MonitorEventDispatcherFactory.class,
                factory, true);
        registrations.add(registration);
        configuration.addRegistrations(registrations);

        configuration.setRuntime(mRuntime);
        configuration.setHostClassLoader(hostLoader);
        configuration.setBootClassLoader(bootLoader);
        configuration.setSystemCompositeUrl(systemComposite);
        configuration.setSystemConfig(systemConfig);
        configuration.setExtensionContributions(result.getExtensionContributions());
        configuration.setUserContributions(result.getUserContributions());
        configuration.setExportedPackages(exportedPackages);

        mCoordinator = bootstrapService.createCoordinator(configuration);

        // register the runtime with the MBean server
        ObjectName objectName = new ObjectName(RUNTIME_MBEAN);
        mBeanServer.registerMBean(this, objectName);
    }

    private void appendProfile(ScanResult result, EmbeddedProfile profile) throws ScanException {
        Map<URI, ContributionSource> sources = new HashMap<URI, ContributionSource>();
        Collection<File> files = FileSystem.filesIn(mSharedFolders.getProfileFolder(profile));

        for (File file : files) {
            try {
                URL location = file.toURI().toURL();
                ContributionSource source;
                if (file.isDirectory()) {
                    // create synthetic contributions from directories contained in the repository
                    URI uri = URI.create("f3-" + file.getName());
                    source = new SyntheticContributionSource(uri, location, true);

                } else {
                    URI uri = URI.create(file.getName());
                    source = new FileContributionSource(uri, location, -1, true);
                }
                sources.put(source.getUri(), source);
            } catch (MalformedURLException e) {
                throw new ScanException("Error loading contribution:" + file.getName(), e);
            }
        }

        // add extensions to scan result
        result.getExtensionContributions().addAll(sources.values());
    }

    public void startRuntime() throws InitializationException {
        try {
            mCoordinator.start();

            MonitorProxyService monitorService = mRuntime.getComponent(MonitorProxyService.class, MONITOR_FACTORY_URI);
            mMonitor = monitorService.createMonitor(Fabric3Server.ServerMonitor.class, RUNTIME_MONITOR_CHANNEL_URI);
            mMonitor.started(mMode.toString());
        } catch (Exception e) {
            shutdown();
            mLogger.log(String.format("Cannot start runtime: %s", mRuntime.getName()), e);
        }

    }

    private void shutdown() {
        try {
            if (mCoordinator != null) {
                mCoordinator.shutdown();
            }
        } catch (ShutdownException ex) {
            mMonitor.shutdownError(ex);
        }
    }

    public void shutdownRuntime() {
        if (null != mCoordinator) {
            shutdown();
        } else {
            mLogger.log(String.format("Runtime %1$s is not running, so it cannot be stopped.", mName));
        }
    }

    public Collection<EmbeddedProfile> getProfiles() {
        return mProfiles.values();
    }

    public <T> T getComponent(Class<T> pClass, URI pURI) {
        return mRuntime.getComponent(pClass, pURI);
    }

    public String getName() {
        return mName;
    }

    public URL getSystemConfig() {
        return mSystemConfig;
    }

    public RuntimeMode getRuntimeMode() {
        return mRuntimeMode;
    }

    public File getRuntimeFolder() {
        return mRuntimeFolder;
    }

    public Object getAttribute(String attribute) throws AttributeNotFoundException, MBeanException, ReflectionException {
        return null;
    }

    public void setAttribute(Attribute attribute) throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException {
    }

    public Object invoke(String actionName, Object[] params, String[] signature) throws MBeanException, ReflectionException {
        return null;
    }
}
