package org.fabric3.runtime.embedded.factory;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.ParseException;
import org.fabric3.host.runtime.ScanException;
import org.fabric3.runtime.embedded.EmbeddedRuntimeImpl;
import org.fabric3.runtime.embedded.EmbeddedServerImpl;
import org.fabric3.runtime.embedded.api.EmbeddedProfile;
import org.fabric3.runtime.embedded.api.EmbeddedServer;
import org.fabric3.runtime.embedded.api.EmbeddedUpdatePolicy;
import org.fabric3.runtime.embedded.api.service.EmbeddedDependencyResolver;
import org.fabric3.runtime.embedded.api.service.EmbeddedDependencyUpdatePolicy;
import org.fabric3.runtime.embedded.api.service.EmbeddedLogger;
import org.fabric3.runtime.embedded.api.service.EmbeddedRuntimeManager;
import org.fabric3.runtime.embedded.api.service.EmbeddedSetup;
import org.fabric3.runtime.embedded.api.service.EmbeddedSharedFolders;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3SetupException;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3StartupException;
import org.fabric3.runtime.embedded.service.EmbeddedDependencyUpdatePolicyImpl;
import org.fabric3.runtime.embedded.service.EmbeddedLoggerImpl;
import org.fabric3.runtime.embedded.service.EmbeddedRuntimeManagerImpl;
import org.fabric3.runtime.embedded.service.EmbeddedSetupImpl;
import org.fabric3.runtime.embedded.service.EmbeddedSharedFoldersImpl;
import org.fabric3.runtime.embedded.service.MavenDependencyResolver;
import org.fabric3.runtime.embedded.util.FileSystem;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Michal Capo
 */
public final class EmbeddedServerFactory {

    private static final Map<EmbeddedServer, InlineServer> currentServers = new ConcurrentHashMap<EmbeddedServer, InlineServer>();

    /*
     *
     *
     *
     * Inner server
     *
     *
     *
     */

    abstract static class InlineServer {

        protected EmbeddedLogger logger = new EmbeddedLoggerImpl();

        protected EmbeddedSetup setup = new EmbeddedSetupImpl();

        protected EmbeddedRuntimeManager runtimeManager = new EmbeddedRuntimeManagerImpl(logger, setup);

        protected EmbeddedDependencyResolver dependencyResolver = new MavenDependencyResolver();

        protected EmbeddedDependencyUpdatePolicy dependencyUpdatePolicy = new EmbeddedDependencyUpdatePolicyImpl();

        protected EmbeddedSharedFolders sharedFolder = new EmbeddedSharedFoldersImpl(dependencyResolver, dependencyUpdatePolicy, logger);

        protected EmbeddedServer server = new EmbeddedServerImpl(runtimeManager, logger, setup);

        protected InlineServer() {
            currentServers.put(server, this);
        }

        public EmbeddedServer get() throws EmbeddedFabric3StartupException {
            try {
                modifySetup(this);
                addBehaviour(this);
            } catch (Exception e) {
                if (null != server && null != setup) {
                    File sFolder = setup.getServerFolder();

                    System.out.println(String.format("Deleting server folder: %1$s", sFolder.getAbsolutePath()));
                    FileSystem.delete(sFolder);
                }
                throw new EmbeddedFabric3StartupException("Cannot create embedded server.", e);
            }

            return server;
        }

        public abstract void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException;

        public abstract void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException;

    }

    /*
     *
     *
     *
     * Adding functionality to server
     *
     *
     *
     * 
     */

    private static void addRuntime(final EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
        InlineServer inlineServer = currentServers.get(server);

        inlineServer.runtimeManager.addRuntime(new EmbeddedRuntimeImpl(
                null,
                null,
                RuntimeMode.VM,
                inlineServer.server,
                inlineServer.setup,
                inlineServer.logger,
                inlineServer.sharedFolder
        ));
    }

    private static void addRuntime(final EmbeddedServer server, final String runtimeName, RuntimeMode runtimeType, EmbeddedProfile... profiles) throws IOException, ScanException, URISyntaxException, ParseException {
        InlineServer inlineServer = currentServers.get(server);

        EmbeddedRuntimeImpl runtime = new EmbeddedRuntimeImpl(
                runtimeName,
                null,
                runtimeType,
                inlineServer.server,
                inlineServer.setup,
                inlineServer.logger,
                inlineServer.sharedFolder,
                profiles
        );
        inlineServer.runtimeManager.addRuntime(runtime);
    }

    private static void addRuntime(final EmbeddedServer server, final String runtimeName, final String systemConfigPath) throws IOException, ScanException, URISyntaxException, ParseException {
        InlineServer inlineServer = currentServers.get(server);

        inlineServer.runtimeManager.addRuntime(new EmbeddedRuntimeImpl(
                runtimeName,
                systemConfigPath,
                RuntimeMode.VM,
                inlineServer.server,
                inlineServer.setup,
                inlineServer.logger,
                inlineServer.sharedFolder
        ));
    }

    private static void addRuntime(final EmbeddedServer server, final String runtimeName, final String systemConfigPath, RuntimeMode runtimeType, EmbeddedProfile... profiles) throws IOException, ScanException, URISyntaxException, ParseException {
        InlineServer inlineServer = currentServers.get(server);

        EmbeddedRuntimeImpl runtime = new EmbeddedRuntimeImpl(
                runtimeName,
                systemConfigPath,
                runtimeType,
                inlineServer.server,
                inlineServer.setup,
                inlineServer.logger,
                inlineServer.sharedFolder,
                profiles
        );
        inlineServer.runtimeManager.addRuntime(runtime);
    }

    private static void validateServerPath(String atPath) {
        if (null == atPath || 0 == atPath.trim().length()) {
            throw new EmbeddedFabric3SetupException("Path cannot be null or empty");
        }

        File file = new File(atPath);

        if (!file.isAbsolute()) {
            throw new EmbeddedFabric3SetupException("Path is not absolute: " + atPath);
        }
    }


    private static void modifyUpdatePolicy(final InlineServer server, final EmbeddedUpdatePolicy policy) {
        server.dependencyUpdatePolicy.setUpdatePolicy(policy);
    }

    private static void modifyServerPath(final InlineServer server, final String serverPath) {
        validateServerPath(serverPath);
        server.setup.setServerFolder(serverPath);
    }


    private static void addProfiles(InlineServer server, EmbeddedProfile[] profiles) {
        for (EmbeddedProfile profile : profiles) {
            server.server.addProfile(profile);
        }
    }

    /*
     *
     *
     *
     * Single runtime
     *
     *
     *
     */

    public static EmbeddedServer singleRuntime() {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addRuntime(server.server);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntime(final String atPath) {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addRuntime(server.server);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntimeWithConfig(final String configSystemPath) {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addRuntime(server.server, null, configSystemPath);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntime(final EmbeddedProfile... profiles) {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addProfiles(server, profiles);
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addRuntime(server.server);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntime(final EmbeddedUpdatePolicy updatePolicy, final EmbeddedProfile... profiles) {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyUpdatePolicy(server, updatePolicy);
                addProfiles(server, profiles);
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addRuntime(server.server);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntime(final String atPath, final EmbeddedProfile... profiles) {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addProfiles(server, profiles);
                addRuntime(server.server);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntime(final String atPath, final EmbeddedUpdatePolicy updatePolicy, final EmbeddedProfile... profiles) {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyUpdatePolicy(server, updatePolicy);
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addProfiles(server, profiles);
                addRuntime(server.server);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntime(final String atPath, final String systemConfigPath) {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addRuntime(server.server, null, systemConfigPath);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntime(final String atPath, final String systemConfigPath, final EmbeddedProfile... profiles) {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addProfiles(server, profiles);
                addRuntime(server.server, null, systemConfigPath);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntime(final String atPath, final String systemConfigPath, final EmbeddedUpdatePolicy updatePolicy, final EmbeddedProfile... profiles) {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyUpdatePolicy(server, updatePolicy);
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addProfiles(server, profiles);
                addRuntime(server.server, null, systemConfigPath);
            }
        }.get();
    }

    /*
     *
     *
     *
     * Multi runtime
     *
     *
     *
     */

    public static EmbeddedServer multiRuntime() throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
            }
        }.get();
    }

    public static EmbeddedServer multiRuntime(final EmbeddedUpdatePolicy updatePolicy) throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyUpdatePolicy(server, updatePolicy);
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
            }
        }.get();
    }

    public static EmbeddedServer multiRuntime(final String atPath) throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
            }
        }.get();
    }

    public static EmbeddedServer multiRuntime(final String atPath, final EmbeddedUpdatePolicy updatePolicy) throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyUpdatePolicy(server, updatePolicy);
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
            }
        }.get();
    }

    public static EmbeddedServer multiRuntime(final String atPath, final EmbeddedProfile... profiles) throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addProfiles(server, profiles);
            }
        }.get();
    }

    public static EmbeddedServer multiRuntime(final String atPath, final EmbeddedUpdatePolicy updatePolicy, final EmbeddedProfile... profiles) throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyUpdatePolicy(server, updatePolicy);
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(InlineServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addProfiles(server, profiles);
            }
        }.get();
    }

    public static void addControllerRuntime(final EmbeddedServer server, final EmbeddedProfile... profiles) throws EmbeddedFabric3StartupException {
        try {
            addRuntime(server, "controller", RuntimeMode.CONTROLLER, profiles);
        } catch (Exception e) {
            throw new EmbeddedFabric3StartupException("Cannot add runtime.", e);
        }
    }

    public static void addControllerRuntime(final EmbeddedServer server, final String systemConfigPath, final EmbeddedProfile... profiles) {
        try {
            addRuntime(server, "controller", systemConfigPath, RuntimeMode.CONTROLLER, profiles);
        } catch (Exception e) {
            throw new EmbeddedFabric3StartupException("Cannot add runtime.", e);
        }
    }

    public static void addParticipantRuntime(final EmbeddedServer server, final String runtimeName, final EmbeddedProfile... profiles) {
        try {
            addRuntime(server, runtimeName, RuntimeMode.PARTICIPANT, profiles);
        } catch (Exception e) {
            throw new EmbeddedFabric3StartupException("Cannot add runtime.", e);
        }
    }

    public static void addParticipantRuntime(final EmbeddedServer server, final String runtimeName, final String systemConfigPath, final EmbeddedProfile... profiles) {
        try {
            addRuntime(server, runtimeName, systemConfigPath, RuntimeMode.PARTICIPANT, profiles);
        } catch (Exception e) {
            throw new EmbeddedFabric3StartupException("Cannot add runtime.", e);
        }
    }

}
