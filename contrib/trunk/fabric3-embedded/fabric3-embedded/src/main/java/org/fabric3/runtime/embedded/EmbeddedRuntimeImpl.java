package org.fabric3.runtime.embedded;

import org.fabric3.host.Names;
import org.fabric3.host.RuntimeMode;
import org.fabric3.host.contribution.*;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.domain.Domain;
import org.fabric3.host.monitor.MonitorEventDispatcherFactory;
import org.fabric3.host.runtime.*;
import org.fabric3.host.util.FileHelper;
import org.fabric3.runtime.embedded.api.EmbeddedComposite;
import org.fabric3.runtime.embedded.api.EmbeddedRuntime;
import org.fabric3.runtime.embedded.api.service.EmbeddedLoggerService;
import org.fabric3.runtime.embedded.api.service.EmbeddedProfileService;
import org.fabric3.runtime.embedded.api.service.EmbeddedSetupService;
import org.fabric3.runtime.embedded.api.service.EmbeddedSharedFoldersService;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3SetupException;
import org.fabric3.runtime.embedded.factory.EmbeddedMonitorEventDispatcherFactory;
import org.fabric3.runtime.embedded.service.EmbeddedMonitorEventDispatcher;
import org.fabric3.runtime.embedded.util.EmbeddedBootstrapHelper;
import org.fabric3.runtime.embedded.util.FileSystem;
import org.w3c.dom.Document;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;

/**
 * @author Michal Capo
 */
public class EmbeddedRuntimeImpl implements EmbeddedRuntime {

    private String mName;
    private URL mSystemConfig;
    private RuntimeMode mRuntimeType;

    private File mRuntimeFolder;
    private File mConfigFolder;

    private Fabric3Runtime mRuntime;
    private RuntimeCoordinator mCoordinator;

    private EmbeddedLoggerService mLogService;

    public EmbeddedRuntimeImpl(final EmbeddedSetupService setupService,
                               final EmbeddedLoggerService logService,
                               final EmbeddedProfileService profileService,
                               final EmbeddedSharedFoldersService sharedFoldersService,
                               final String name,
                               final String systemConfigPath,
                               final RuntimeMode runtimeMode) throws IOException, URISyntaxException, ScanException, ParseException {

        initializeRuntime(setupService, logService, name, systemConfigPath, runtimeMode);
        createRuntimeFolders();
        createRuntimeCoordinator(sharedFoldersService, profileService, runtimeMode);
    }

    private void initializeRuntime(EmbeddedSetupService setupService, EmbeddedLoggerService logService, String name, String systemConfigPath, RuntimeMode runtimeType) throws URISyntaxException, MalformedURLException {
        mLogService = logService;
        String configPath = systemConfigPath;

        mName = name;
        mRuntimeType = runtimeType;

        // setup name
        if (null == mName) {
            mName = "vm";
        }

        // setup runtime type
        if (null == mRuntimeType) {
            mRuntimeType = RuntimeMode.VM;
        }

        // setup runtime folder
        mRuntimeFolder = FileSystem.folder(setupService.getServerFolder(), "runtimes/" + mName);
        if (!mRuntimeFolder.exists()) {
            if (!mRuntimeFolder.mkdirs()) {
                throw new EmbeddedFabric3SetupException("Cannot create folder: " + mRuntimeFolder.getAbsolutePath());
            }
        }

        // setup config path
        if (null == configPath) {
            switch (runtimeType) {
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
                    throw new EmbeddedFabric3SetupException("Unknown runtime type: " + runtimeType);
            }
        } else {
            if (null == configPath || 0 == configPath.trim().length()) {
                throw new EmbeddedFabric3SetupException("System config path cannot be null or empty");
            }

            if (!FileSystem.isAbsolute(configPath)) {
                throw new EmbeddedFabric3SetupException("Path is not absolute: " + configPath);
            }
        }
        mSystemConfig = FileSystem.fileAtClassPath(configPath);
    }

    private void createRuntimeFolders() throws IOException {
        FileSystem.createFolders(FileSystem.folders(mRuntimeFolder, "repository/user", "repository/runtime", "deploy", "tmp", "data"));
        mConfigFolder = FileSystem.createFolder(FileSystem.folder(mRuntimeFolder, "config"));

        // copy system config file
        FileSystem.copy(mSystemConfig, FileSystem.file(mConfigFolder, "systemConfig.xml"));
    }

    private void createRuntimeCoordinator(final EmbeddedSharedFoldersService sharedFoldersService, final EmbeddedProfileService profileService, final RuntimeMode runtimeMode) throws ParseException, IOException, URISyntaxException, ScanException {
        // create the classloaders for booting the runtime
        ClassLoader systemClassLoader = new MaskingClassLoader(ClassLoader.getSystemClassLoader(), "org.slf4j", "ch.qos.logback");
        ClassLoader libClassLoader = EmbeddedBootstrapHelper.createClassLoader(systemClassLoader, sharedFoldersService.getLibFolder());

        // mask hidden JDK and system classpath packages
        ClassLoader maskingClassLoader = new MaskingClassLoader(libClassLoader, HiddenPackages.getPackages());
        ClassLoader hostLoader = BootstrapHelper.createClassLoader(maskingClassLoader, sharedFoldersService.getHostFolder());
        ClassLoader bootLoader = EmbeddedBootstrapHelper.createClassLoader(hostLoader, sharedFoldersService.getBootFolder(), sharedFoldersService.getLibFolder());

        BootstrapService bootstrapService = BootstrapFactory.getService(bootLoader);

        // load the system configuration
        Document systemConfig = bootstrapService.loadSystemConfig(mConfigFolder);

        List<File> deployDirs = bootstrapService.parseDeployDirectories(systemConfig);

        URI domainName = bootstrapService.parseDomainName(systemConfig);
        String zoneName = bootstrapService.parseZoneName(systemConfig);
        String runtimeName = bootstrapService.getRuntimeName(domainName, zoneName, mName, runtimeMode);

        // create the HostInfo and runtime
        HostInfo hostInfo = BootstrapHelper.createHostInfo(runtimeName, runtimeMode, domainName, mRuntimeFolder, mConfigFolder, sharedFoldersService.getExtensionsFolder(), deployDirs);
        // clear out the tmp directory
        FileHelper.cleanDirectory(hostInfo.getTempDir());

        MBeanServer mBeanServer = MBeanServerFactory.createMBeanServer("fabric3");

        EmbeddedMonitorEventDispatcher runtimeDispatcher = new EmbeddedMonitorEventDispatcher();
        EmbeddedMonitorEventDispatcher appDispatcher = new EmbeddedMonitorEventDispatcher();
        RuntimeConfiguration runtimeConfig = new RuntimeConfiguration(hostInfo, mBeanServer, runtimeDispatcher, appDispatcher);

        mRuntime = bootstrapService.createDefaultRuntime(runtimeConfig);

        Map<String, String> exportedPackages = new HashMap<String, String>();
        exportedPackages.put("org.fabric3.runtime.ant.api", Names.VERSION);

        URL systemComposite = getClass().getResource("/boot/system.composite").toURI().toURL();

        ScanResult result = bootstrapService.scanRepository(hostInfo);
        appendExtensions(profileService, result);
        appendJunitAsExtension(sharedFoldersService, result);

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
    }

    private void appendExtensions(EmbeddedProfileService profileService, ScanResult result) throws ScanException {
        Map<URI, ContributionSource> sources = new HashMap<URI, ContributionSource>();
        for (File file : profileService.getProfilesFiles()) {
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

    private void appendJunitAsExtension(EmbeddedSharedFoldersService pSharedFoldersService, ScanResult pResult) throws ScanException {
        Map<URI, ContributionSource> sources = new HashMap<URI, ContributionSource>();

        for (File f : FileSystem.filesIn(pSharedFoldersService.getJUnitFolder())) {
            try {
                URI uri = URI.create(f.getName());
                ContributionSource source = new FileContributionSource(uri, f.toURI().toURL(), -1, true);
                sources.put(source.getUri(), source);
            } catch (MalformedURLException e) {
                throw new ScanException("Error loading junit as extension:" + f.getName(), e);
            }
        }

        pResult.getExtensionContributions().addAll(sources.values());
    }

    public void installComposite(final EmbeddedComposite composite) throws ContributionException, DeploymentException {
        // contribute the Maven project to the application domain
        ContributionService contributionService = mRuntime.getComponent(ContributionService.class, Names.CONTRIBUTION_SERVICE_URI);
        Domain domain = mRuntime.getComponent(Domain.class, Names.APPLICATION_DOMAIN_URI);
        URI uri = contributionService.store(composite);
        contributionService.install(uri);
        // activate the deployable composite in the domain
        domain.include(Arrays.asList(uri));
    }

    public void startRuntime() throws IOException, InitializationException {
        mCoordinator.start();
    }

    public void stopRuntime() throws ShutdownException {
        if (null != mCoordinator) {
            mCoordinator.shutdown();
        } else {
            mLogService.log(String.format("Runtime %1$s is not running, so it cannot be stopped.", mName));
        }
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
        return mRuntimeType;
    }

    public File getRuntimeFolder() {
        return mRuntimeFolder;
    }
}
