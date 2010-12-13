package org.fabric3.runtime.embedded.factory;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.runtime.ParseException;
import org.fabric3.host.runtime.ScanException;
import org.fabric3.runtime.embedded.EmbeddedRuntimeImpl;
import org.fabric3.runtime.embedded.EmbeddedServerImpl;
import org.fabric3.runtime.embedded.api.EmbeddedProfile;
import org.fabric3.runtime.embedded.api.EmbeddedServer;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3SetupException;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3StartupException;
import org.fabric3.runtime.embedded.util.FileSystem;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Michal Capo
 */
public final class EmbeddedServerFactory {

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

        public EmbeddedServer get() throws EmbeddedFabric3StartupException {
            EmbeddedServer server = null;
            try {
                server = new EmbeddedServerImpl();
                modifySetup(server);
                server.initialize();
                addBehaviour(server);
            } catch (Exception e) {
                if (null != server && null != server.getSetupService()) {
                    File sFolder = server.getSetupService().getServerFolder();

                    System.out.println(String.format("Deleting server folder: %1$s", sFolder.getAbsolutePath()));
                    FileSystem.delete(sFolder);
                }
                throw new EmbeddedFabric3StartupException("Cannot create embedded server.", e);
            }

            return server;
        }

        public abstract void modifySetup(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException;

        public abstract void addBehaviour(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException;

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
        server.getRuntimeService().addRuntime(new EmbeddedRuntimeImpl(
                server.getSetupService(),
                server.getLoggerService(),
                server.getProfileService(),
                server.getSharedFoldersService(),
                null, null, RuntimeMode.VM)
        );
    }

    private static void addRuntime(final EmbeddedServer server, final String runtimeName, RuntimeMode runtimeType) throws IOException, ScanException, URISyntaxException, ParseException {
        server.getRuntimeService().addRuntime(new EmbeddedRuntimeImpl(
                server.getSetupService(),
                server.getLoggerService(),
                server.getProfileService(),
                server.getSharedFoldersService(),
                runtimeName, null, runtimeType)
        );
    }

    private static void addRuntime(final EmbeddedServer server, final String runtimeName, final String systemConfigPath) throws IOException, ScanException, URISyntaxException, ParseException {
        server.getRuntimeService().addRuntime(new EmbeddedRuntimeImpl(
                server.getSetupService(),
                server.getLoggerService(),
                server.getProfileService(),
                server.getSharedFoldersService(),
                runtimeName, systemConfigPath, RuntimeMode.VM)
        );
    }

    private static void addRuntime(final EmbeddedServer server, final String runtimeName, final String systemConfigPath, RuntimeMode runtimeType) throws IOException, ScanException, URISyntaxException, ParseException {
        server.getRuntimeService().addRuntime(new EmbeddedRuntimeImpl(
                server.getSetupService(),
                server.getLoggerService(),
                server.getProfileService(),
                server.getSharedFoldersService(),
                runtimeName, systemConfigPath, runtimeType)
        );
    }

    private static void addProfiles(final EmbeddedServer server, final EmbeddedProfile... profiles) {
        for (EmbeddedProfile profile : profiles) {
            server.getProfileService().addProfile(profile);
        }
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


    private static void modifyServerPath(final EmbeddedServer server, final String serverPath) {
        validateServerPath(serverPath);

        server.getSetupService().setServerFolder(serverPath);
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

    public static EmbeddedServer singleRuntime() throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
            }

            @Override
            public void addBehaviour(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addRuntime(server);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntime(final String atPath) throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addRuntime(server);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntimeWithConfig(final String configSystemPath) throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
            }

            @Override
            public void addBehaviour(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addRuntime(server, null, configSystemPath);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntime(final EmbeddedProfile... profiles) throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addProfiles(server, profiles);
            }

            @Override
            public void addBehaviour(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addRuntime(server);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntime(final String atPath, final EmbeddedProfile... profiles) throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addProfiles(server, profiles);
                addRuntime(server);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntime(final String atPath, final String systemConfigPath) throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addRuntime(server, null, systemConfigPath);
            }
        }.get();
    }

    public static EmbeddedServer singleRuntime(final String atPath, final String systemConfigPath, final EmbeddedProfile... profiles) throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addProfiles(server, profiles);
                addRuntime(server, null, systemConfigPath);
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
            public void modifySetup(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
            }

            @Override
            public void addBehaviour(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
/*
                addRuntime(server, "controller", RuntimeMode.CONTROLLER);
                addRuntime(server, "runtime1", RuntimeMode.PARTICIPANT);
                addRuntime(server, "runtime2", RuntimeMode.PARTICIPANT);
*/
            }
        }.get();
    }

    public static EmbeddedServer multiRuntime(final String atPath) throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
/*
                addRuntime(server, "controller", RuntimeMode.CONTROLLER);
                addRuntime(server, "runtime1", RuntimeMode.PARTICIPANT);
                addRuntime(server, "runtime2", RuntimeMode.PARTICIPANT);
*/
            }
        }.get();
    }

    public static EmbeddedServer multiRuntime(final String atPath, final EmbeddedProfile... profiles) throws EmbeddedFabric3StartupException {
        return new InlineServer() {
            @Override
            public void modifySetup(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                modifyServerPath(server, atPath);
            }

            @Override
            public void addBehaviour(EmbeddedServer server) throws IOException, ScanException, URISyntaxException, ParseException {
                addProfiles(server, profiles);
/*
                addRuntime(server, "controller", RuntimeMode.CONTROLLER);
                addRuntime(server, "runtime1", RuntimeMode.PARTICIPANT);
                addRuntime(server, "runtime2", RuntimeMode.PARTICIPANT);
*/
            }
        }.get();
    }

    public static void addControllerRuntime(final EmbeddedServer server) throws EmbeddedFabric3StartupException {
        try {
            addRuntime(server, "controller", RuntimeMode.CONTROLLER);
        } catch (Exception e) {
            throw new EmbeddedFabric3StartupException("Cannot add runtime.", e);
        }
    }

    public static void addControllerRuntime(final EmbeddedServer server, final String systemConfigPath) throws EmbeddedFabric3StartupException {
        try {
            addRuntime(server, "controller", systemConfigPath, RuntimeMode.CONTROLLER);
        } catch (Exception e) {
            throw new EmbeddedFabric3StartupException("Cannot add runtime.", e);
        }
    }

    public static void addParticipantRuntime(final EmbeddedServer server, final String runtimeName) throws EmbeddedFabric3StartupException {
        try {
            addRuntime(server, runtimeName, RuntimeMode.PARTICIPANT);
        } catch (Exception e) {
            throw new EmbeddedFabric3StartupException("Cannot add runtime.", e);
        }
    }

    public static void addParticipantRuntime(final EmbeddedServer server, final String runtimeName, final String systemConfigPath) throws EmbeddedFabric3StartupException {
        try {
            addRuntime(server, runtimeName, systemConfigPath, RuntimeMode.PARTICIPANT);
        } catch (Exception e) {
            throw new EmbeddedFabric3StartupException("Cannot add runtime.", e);
        }
    }

}
