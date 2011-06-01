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

    public static Server findServerByRuntime(AssemblyConfig pConfig, final Runtime pRuntime) {
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

    public static List<Runtime> findRuntimesByServerName(AssemblyConfig pConfig, final String pServerName) {
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

    public static Server findServerByComposite(AssemblyConfig pConfig, Composite pComposite) {
        for (Server server : pConfig.getServers()) {
            for (String name : server.getCompositeNames()) {
                if (pComposite.getName().equals(name)) {
                    return server;
                }
            }
        }

        return null;
    }

    public static Server findServerByCompositeName(AssemblyConfig pConfig, String pCompositeName) {
        for (Server server : pConfig.getServers()) {
            for (String name : server.getCompositeNames()) {
                if (pCompositeName.equals(name)) {
                    return server;
                }
            }
        }

        return null;
    }


    public static Map<RuntimeMode, Integer> findRuntimeModesByServerName(AssemblyConfig pConfig, final String pServerName) {
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

    public static List<Server> findServersByName(AssemblyConfig pConfig, final String pServerName) {
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

    public static Server findServerByName(AssemblyConfig pConfig, final String pServerName) {
        for (Server server : pConfig.getServers()) {
            if (server.getServerName().equals(pServerName)) {
                return server;
            }
        }

        throw new ServerNotFoundException("Server ''{0}'' not found.", pServerName);
    }

    public static Runtime findRuntimeByName(AssemblyConfig pConfig, final String pRuntimeName) {
        for (Runtime runtime : pConfig.getRuntimes()) {
            if (pRuntimeName.equals(runtime.getRuntimeName())) {
                return runtime;
            }
        }

        throw new RuntimeNotFoundException(pRuntimeName);
    }

    public static List<Runtime> findRuntimesByName(AssemblyConfig pConfig, final String pRuntimeName) {
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
            Server server = findServerByRuntime(pConfig, pRuntime);
            if (null != server.getUpdatePolicy()) {
                return server.getUpdatePolicy();
            }
        } catch (ServerNotFoundException e) {
            // no-op
        }

        return pConfig.getUpdatePolicy();
    }

    public static File computeServerPath(AssemblyConfig pConfig, Runtime pRuntime) {
        return findServerByRuntime(pConfig, pRuntime).getServerPath();
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
        List<Runtime> runtimes = findRuntimesByServerName(pConfig, pServer.getServerName());
        for (Runtime runtime : runtimes) {
            if (RuntimeMode.VM == runtime.getRuntimeMode() || RuntimeMode.CONTROLLER == runtime.getRuntimeMode()) {
                return runtime;
            }
        }

        throw new AssemblyException("No CONTROLLER or VM runtime found on server: ''{0}''.", pServer.getServerName());
    }

    public static Server findServerByArchive(AssemblyConfig pConfig, Archive pArchive) {
        String name = pArchive.getName();

        for (Server server : pConfig.getServers()) {
            if (server.getArchiveNames().contains(name)) {
                return server;
            }
        }

        throw new AssemblyException("Composite ''{0}'' not found. Is this a typo?", name);
    }

    public static Archive findArchiveByName(AssemblyConfig pConfig, String pArchiveName) {
        if (StringUtils.isBlank(pArchiveName)) {
            throw new AssemblyException("Archive name is null.");
        }

        Archive result = pConfig.getArchivesMap().get(pArchiveName);
        if (null == result) {
            throw new AssemblyException("Archive ''{0}'' not found. It is added?", pArchiveName);
        }

        return result;
    }

    public static File computeDeployPath(AssemblyConfig pConfig, Archive pArchive) {
        return computeDeployPath(pConfig, findServerByArchive(pConfig, pArchive), pArchive.getName());
    }

    public static File computeDeployPath(AssemblyConfig pConfig, Composite pComposite) {
        if (null != pComposite.getPath()) {
            return computeDeployPath(pConfig, findServerByComposite(pConfig, pComposite), pComposite.getPath().getName());
        }

        throw new AssemblyException("Composite ''{0}'' is not specified via path. Try to use method with File parameter.", pComposite.getName());
    }

    public static File computeDeployPath(AssemblyConfig pConfig, Composite pComposite, File pCompositeFile) {
        return computeDeployPath(pConfig, findServerByComposite(pConfig, pComposite), pCompositeFile.getPath());
    }

    private static File computeDeployPath(AssemblyConfig pConfig, Server pServer, String pFilePath) {
        Runtime runtime = findRuntimeForCompositeDeployOnServer(pConfig, pServer);
        return FileUtils.file(runtime.getDeployFolder(), pFilePath);
    }

}
