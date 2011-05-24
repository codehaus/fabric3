package org.fabric3.assembly.configuration;

import org.fabric3.assembly.dependency.Dependency;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.exception.ServerNotFoundException;
import org.fabric3.assembly.utils.Closure;
import org.fabric3.assembly.utils.ClosureUtils;

import java.io.File;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public abstract class ConfigurationHelper {

    public abstract List<ServerConfiguration> getServerConfigurations();

    public abstract List<RuntimeConfiguration> getRuntimeConfigurations();

    public abstract Version getConfigurationVersion();

    public abstract UpdatePolicy getConfigurationUpdatePolicy();

    /*
     *
     *
     * Service methods.
     *
     *
     */

    public Dependency appendVersion(Dependency pDependency, ServerConfiguration pServerConfiguration) {
        if (pDependency.isVersionLess()) {
            pDependency.setVersion(computeMissingVersion(pServerConfiguration));
        }

        return pDependency;
    }

    public ServerConfiguration getServerByRuntime(final RuntimeConfiguration pRuntime) {
        String serverLookupName = pRuntime.getServerName();
        if (null == serverLookupName) {
            throw new ServerNotFoundException(MessageFormat.format("Runtime {0} is not assigned to any server. Please check your configuration.", pRuntime.getRuntimeName()));
        }

        for (ServerConfiguration server : getServerConfigurations()) {
            if (serverLookupName.equals(server.getServerName())) {
                return server;
            }
        }

        throw new ServerNotFoundException(MessageFormat.format("Runtime {0} is assigned to {1} server. But no such server found. Is this a typo?", pRuntime.getRuntimeName(), serverLookupName));
    }

    public File findServerPathByRuntime(RuntimeConfiguration pRuntime) {
        return getServerByRuntime(pRuntime).getServerPath();
    }

    public List<RuntimeConfiguration> getRuntimesByServerName(final String pServerName) {
        final List<RuntimeConfiguration> runtimes = new ArrayList<RuntimeConfiguration>();

        ClosureUtils.each(getRuntimeConfigurations(), new Closure<RuntimeConfiguration>() {
            @Override
            public void exec(RuntimeConfiguration pParam) {
                if (pServerName.equals(pParam.getServerName())) {
                    runtimes.add(pParam);
                }
            }
        });

        return runtimes;
    }

    public List<ServerConfiguration> getServersByName(final String pServerName) {
        final List<ServerConfiguration> servers = new ArrayList<ServerConfiguration>();

        ClosureUtils.each(getServerConfigurations(), new Closure<ServerConfiguration>() {
            @Override
            public void exec(ServerConfiguration pParam) {
                if (pServerName.equals(pParam.getServerName())) {
                    servers.add(pParam);
                }
            }
        });

        return servers;
    }

    public Version computeMissingVersion(RuntimeConfiguration pConfiguration) {
        if (null == pConfiguration) {
            return getConfigurationVersion();
        }

        if (null != pConfiguration.getVersion()) {
            return pConfiguration.getVersion();
        }

        try {
            ServerConfiguration serverConfiguration = getServerByRuntime(pConfiguration);
            if (null != serverConfiguration.getVersion()) {
                return serverConfiguration.getVersion();
            }
        } catch (ServerNotFoundException e) {
            // no-op
        }

        return getConfigurationVersion();
    }

    public Version computeMissingVersion(ServerConfiguration pConfiguration) {
        if (null == pConfiguration) {
            return getConfigurationVersion();
        }

        if (null != pConfiguration.getVersion()) {
            return pConfiguration.getVersion();
        }

        return getConfigurationVersion();
    }

    public UpdatePolicy computeUpdatePolicy(RuntimeConfiguration pConfiguration) {
        if (null == pConfiguration) {
            return getConfigurationUpdatePolicy();
        }

        if (null != pConfiguration.getUpdatePolicy()) {
            return pConfiguration.getUpdatePolicy();
        }

        try {
            ServerConfiguration serverConfiguration = getServerByRuntime(pConfiguration);
            if (null != serverConfiguration.getUpdatePolicy()) {
                return serverConfiguration.getUpdatePolicy();
            }
        } catch (ServerNotFoundException e) {
            // no-op
        }

        return getConfigurationUpdatePolicy();
    }

    public UpdatePolicy computeUpdatePolicy(ServerConfiguration pConfiguration) {
        if (null == pConfiguration) {
            return getConfigurationUpdatePolicy();
        }

        if (null != pConfiguration.getUpdatePolicy()) {
            return pConfiguration.getUpdatePolicy();
        }

        return getConfigurationUpdatePolicy();
    }
}
