package org.fabric3.assembly.completition;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Composite;
import org.fabric3.assembly.configuration.Profile;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.configuration.RuntimeMode;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.dependency.fabric.FabricProfiles;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.exception.ServerNotFoundException;
import org.fabric3.assembly.exception.ValidationException;
import org.fabric3.assembly.utils.Closure;
import org.fabric3.assembly.utils.ClosureUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michal Capo
 */
public class CompletionHelper {

    private AssemblyConfig mConfig;

    public CompletionHelper(AssemblyConfig pConfig) {
        mConfig = pConfig;
    }

    /*
     *
     *
     * Service methods.
     *
     *
     */

    public Server getServerByRuntime(final Runtime pRuntime) {
        String serverLookupName = pRuntime.getServerName();
        if (null == serverLookupName) {
            throw new ServerNotFoundException(MessageFormat.format("Runtime {0} is not assigned to any server. Please check your configuration.", pRuntime.getRuntimeName()));
        }

        for (Server server : mConfig.getServers()) {
            if (serverLookupName.equals(server.getServerName())) {
                return server;
            }
        }

        throw new ServerNotFoundException(MessageFormat.format("Runtime {0} is assigned to {1} server. But no such server found. Is this a typo?", pRuntime.getRuntimeName(), serverLookupName));
    }

    public List<Runtime> getRuntimesByServerName(final String pServerName) {
        final List<Runtime> runtimes = new ArrayList<Runtime>();

        ClosureUtils.each(mConfig.getRuntimes(), new Closure<Runtime>() {
            @Override
            public void exec(Runtime pParam) {
                if (pServerName.equals(pParam.getServerName())) {
                    runtimes.add(pParam);
                }
            }
        });

        return runtimes;
    }

    public Map<RuntimeMode, Integer> getRuntimeModesByServerName(final String pServerName) {
        final Map<RuntimeMode, Integer> runtimes = new HashMap<RuntimeMode, Integer>();

        ClosureUtils.each(mConfig.getRuntimes(), new Closure<Runtime>() {
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

    public List<Server> getServersByName(final String pServerName) {
        final List<Server> servers = new ArrayList<Server>();

        ClosureUtils.each(mConfig.getServers(), new Closure<Server>() {
            @Override
            public void exec(Server pParam) {
                if (pServerName.equals(pParam.getServerName())) {
                    servers.add(pParam);
                }
            }
        });

        return servers;
    }

    public List<Runtime> getRuntimesByName(final String pRuntimeName) {
        final List<Runtime> runtimes = new ArrayList<Runtime>();

        ClosureUtils.each(mConfig.getRuntimes(), new Closure<Runtime>() {
            @Override
            public void exec(Runtime pParam) {
                if (pRuntimeName.equals(pParam.getServerName())) {
                    runtimes.add(pParam);
                }
            }
        });

        return runtimes;
    }

    public Version computeMissingVersion(Profile pProfile) {
        if (null != pProfile.getVersion()) {
            throw new ValidationException("Profile ''{0}'' already has a version.", pProfile.getName());
        }

        if (null != mConfig.getProfileConfig().getVersion()) {
            return mConfig.getProfileConfig().getVersion();
        }

        return mConfig.getVersion();
    }

    public Version computeMissingVersion(Server pServer) {
        if (null != pServer.getVersion()) {
            throw new ValidationException("Server ''{0}'' already has a version.", pServer.getServerName());
        }

        return mConfig.getVersion();
    }

    public UpdatePolicy computeMissingUpdatePolicy(Composite pComposite) {
        if (null != pComposite.getUpdatePolicy()) {
            throw new ValidationException("Composite ''{0}'' already has a update policy.", pComposite.getName());
        }

        if (null != mConfig.getCompositeConfig().getUpdatePolicy()) {
            return mConfig.getCompositeConfig().getUpdatePolicy();
        }

        return mConfig.getUpdatePolicy();
    }

    public UpdatePolicy computeMissingUpdatePolicy(Profile pProfile) {
        if (null != pProfile.getUpdatePolicy()) {
            throw new ValidationException("Profile ''{0}'' already has a update policy.", pProfile.getName());
        }

        if (null != mConfig.getProfileConfig().getUpdatePolicy()) {
            return mConfig.getProfileConfig().getUpdatePolicy();
        }

        return mConfig.getUpdatePolicy();
    }

    public UpdatePolicy computeMissingUpdatePolicy(Server pServer) {
        if (null != pServer.getUpdatePolicy()) {
            throw new ValidationException("Server ''{0}'' already has a update policy.", pServer.getServerName());
        }

        return mConfig.getUpdatePolicy();
    }

    public UpdatePolicy computeMissingUpdatePolicy(Runtime pRuntime) {
        if (null != pRuntime.getUpdatePolicy()) {
            throw new ValidationException("Runtime ''{0}'' already has a update policy.", pRuntime.getRuntimeName());
        }

        try {
            Server server = getServerByRuntime(pRuntime);
            if (null != server.getUpdatePolicy()) {
                return server.getUpdatePolicy();
            }
        } catch (ServerNotFoundException e) {
            // no-op
        }

        return mConfig.getUpdatePolicy();
    }

    public File computeServerPath(Runtime pRuntime) {
        return getServerByRuntime(pRuntime).getServerPath();
    }


    public Profile findProfileByName(String pProfileName) {
        for (Profile profile : mConfig.getProfiles()) {
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

    public Composite findCompositeByName(String pCompositeName) {
        for (Composite composite : mConfig.getComposites()) {
            if (composite.getName().equals(pCompositeName)) {
                return composite;
            }
        }

        throw new AssemblyException("Composite ''{0}'' not found. Is this a typo?", pCompositeName);
    }
}
