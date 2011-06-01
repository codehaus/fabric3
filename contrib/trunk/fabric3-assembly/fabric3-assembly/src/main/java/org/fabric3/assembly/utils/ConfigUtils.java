package org.fabric3.assembly.utils;

import org.fabric3.assembly.configuration.*;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.dependency.fabric.FabricProfiles;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.exception.RuntimeNotFoundException;
import org.fabric3.assembly.exception.ServerNotFoundException;
import org.fabric3.assembly.exception.ValidationException;
import org.jboss.shrinkwrap.api.Archive;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michal Capo
 */
public class ConfigUtils {

    public static Server getServerByRuntime(AssemblyConfig pConfig, final Runtime pRuntime) {
        String serverLookupName = pRuntime.getServerName();
        if (null == serverLookupName) {
            throw new ServerNotFoundException(MessageFormat.format("Runtime {0} is not assigned to any server. Please check your configuration.", pRuntime.getRuntimeName()));
        }

        for (Server server : pConfig.getServers()) {
            if (serverLookupName.equals(server.getServerName())) {
                return server;
            }
        }

        throw new ServerNotFoundException(MessageFormat.format("Runtime {0} is assigned to {1} server. But no such server found. Is this a typo?", pRuntime.getRuntimeName(), serverLookupName));
    }

    public static List<Runtime> getRuntimesByServerName(AssemblyConfig pConfig, final String pServerName) {
        final List<Runtime> runtimes = new ArrayList<Runtime>();

        ClosureUtils.each(pConfig.getRuntimes(), new Closure<Runtime>() {
            @Override
            public void exec(Runtime pParam) {
                if (pServerName.equals(pParam.getServerName())) {
                    runtimes.add(pParam);
                }
            }
        });

        return runtimes;
    }

    public static Server getServerByComposite(AssemblyConfig pConfig, Composite pComposite) {
        for (Server server : pConfig.getServers()) {
            for (String name : server.getCompositeNames()) {
                if (pComposite.getName().equals(name)) {
                    return server;
                }
            }
        }

        return null;
    }

    public static Server getServerByComposite(AssemblyConfig pConfig, String pCompositeName) {
        for (Server server : pConfig.getServers()) {
            for (String name : server.getCompositeNames()) {
                if (pCompositeName.equals(name)) {
                    return server;
                }
            }
        }

        return null;
    }


    public static Map<RuntimeMode, Integer> getRuntimeModesByServerName(AssemblyConfig pConfig, final String pServerName) {
        final Map<RuntimeMode, Integer> runtimes = new HashMap<RuntimeMode, Integer>();

        ClosureUtils.each(pConfig.getRuntimes(), new Closure<Runtime>() {
            @Override
            public void exec(Runtime pParam) {
                if (pServerName.equals(pParam.getServerName())) {
                    Integer count = runtimes.get(pParam.getRuntimeMode());
                    if (null == count) {
                        count = 0;
                    }
                    count++;
                    runtimes.put(pParam.getRuntimeMode(), count);
                }
            }
        });

        return runtimes;
    }

    public static List<Server> getServersByName(AssemblyConfig pConfig, final String pServerName) {
        final List<Server> servers = new ArrayList<Server>();

        ClosureUtils.each(pConfig.getServers(), new Closure<Server>() {
            @Override
            public void exec(Server pParam) {
                if (pServerName.equals(pParam.getServerName())) {
                    servers.add(pParam);
                }
            }
        });

        return servers;
    }

    public static Server getServerByName(AssemblyConfig pConfig, final String pServerName) {
        for (Server server : pConfig.getServers()) {
            if (server.getServerName().equals(pServerName)) {
                return server;
            }
        }

        throw new ServerNotFoundException("Server ''{0}'' not found.", pServerName);
    }

    public static Runtime getRuntimeByName(AssemblyConfig pConfig, final String pRuntimeName) {
        for (Runtime runtime : pConfig.getRuntimes()) {
            if (pRuntimeName.equals(runtime.getRuntimeName())) {
                return runtime;
            }
        }

        throw new RuntimeNotFoundException(pRuntimeName);
    }

    public static List<Runtime> getRuntimesByName(AssemblyConfig pConfig, final String pRuntimeName) {
        final List<Runtime> runtimes = new ArrayList<Runtime>();

        ClosureUtils.each(pConfig.getRuntimes(), new Closure<Runtime>() {
            @Override
            public void exec(Runtime pParam) {
                if (pRuntimeName.equals(pParam.getServerName())) {
                    runtimes.add(pParam);
                }
            }
        });

        return runtimes;
    }

    public static Version computeMissingVersion(AssemblyConfig pConfig, Profile pProfile) {
        if (null != pProfile.getVersion()) {
            throw new ValidationException("Profile ''{0}'' already has a version.", pProfile.getName());
        }

        if (null != pConfig.getProfileConfig().getVersion()) {
            return pConfig.getProfileConfig().getVersion();
        }

        return pConfig.getVersion();
    }

    public static Version computeMissingVersion(AssemblyConfig pConfig, Server pServer) {
        if (null != pServer.getVersion()) {
            throw new ValidationException("Server ''{0}'' already has a version.", pServer.getServerName());
        }

        return pConfig.getVersion();
    }

    public static UpdatePolicy computeMissingUpdatePolicy(AssemblyConfig pConfig, Profile pProfile) {
        if (null != pProfile.getUpdatePolicy()) {
            throw new ValidationException("Profile ''{0}'' already has a update policy.", pProfile.getName());
        }

        if (null != pConfig.getProfileConfig().getUpdatePolicy()) {
            return pConfig.getProfileConfig().getUpdatePolicy();
        }

        return pConfig.getUpdatePolicy();
    }

    public static UpdatePolicy computeMissingUpdatePolicy(AssemblyConfig pConfig, Server pServer) {
        if (null != pServer.getUpdatePolicy()) {
            throw new ValidationException("Server ''{0}'' already has a update policy.", pServer.getServerName());
        }

        return pConfig.getUpdatePolicy();
    }

    public static UpdatePolicy computeMissingUpdatePolicy(AssemblyConfig pConfig, Runtime pRuntime) {
        if (null != pRuntime.getUpdatePolicy()) {
            throw new ValidationException("Runtime ''{0}'' already has a update policy.", pRuntime.getRuntimeName());
        }

        try {
            Server server = getServerByRuntime(pConfig, pRuntime);
            if (null != server.getUpdatePolicy()) {
                return server.getUpdatePolicy();
            }
        } catch (ServerNotFoundException e) {
            // no-op
        }

        return pConfig.getUpdatePolicy();
    }

    public static File computeServerPath(AssemblyConfig pConfig, Runtime pRuntime) {
        return getServerByRuntime(pConfig, pRuntime).getServerPath();
    }


    public static Profile findProfileByName(AssemblyConfig pConfig, String pProfileName) {
        for (Profile profile : pConfig.getProfiles()) {
            if (profile.getAllNames().contains(pProfileName)) {
                return profile;
            }
        }

        for (Profile profile : FabricProfiles.all()) {
            if (profile.getAllNames().contains(pProfileName)) {
                return profile;
            }
        }

        throw new AssemblyException("Profile ''{0}'' not found. Is this a typo?", pProfileName);
    }

    public static Composite findCompositeByName(AssemblyConfig pConfig, String pCompositeName) {
        for (Composite composite : pConfig.getComposites()) {
            if (composite.getName().equals(pCompositeName)) {
                return composite;
            }
        }

        throw new AssemblyException("Composite ''{0}'' not found. Is this a typo?", pCompositeName);
    }

    public static Runtime findRuntimeForCompositeDeployOnServer(AssemblyConfig pConfig, Server pServer) {
        List<Runtime> runtimes = getRuntimesByServerName(pConfig, pServer.getServerName());
        for (Runtime runtime : runtimes) {
            if (RuntimeMode.VM == runtime.getRuntimeMode() || RuntimeMode.CONTROLLER == runtime.getRuntimeMode()) {
                return runtime;
            }
        }

        throw new AssemblyException("No CONTROLLER or VM runtime found on server: ''{0}''.", pServer.getServerName());
    }

    public static Server getServerByArchive(AssemblyConfig pConfig, Archive pArchive) {
        String name = pArchive.getName();

        for (Server server : pConfig.getServers()) {
            if (server.getArchiveNames().contains(name)) {
                return server;
            }
        }

        throw new AssemblyException("Composite ''{0}'' not found. Is this a typo?", name);
    }
}
