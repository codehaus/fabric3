package org.fabric3.assembly.completition;

import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.configuration.Server;
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
public abstract class CompletionHelper {

    public abstract List<Server> getServerConfigurations();

    public abstract List<org.fabric3.assembly.configuration.Runtime> getRuntimeConfigurations();

    public abstract Version getConfigurationVersion();

    public abstract UpdatePolicy getConfigurationUpdatePolicy();

    /*
     *
     *
     * Service methods.
     *
     *
     */

    public Dependency appendVersion(Dependency pDependency, Server pServer) {
        if (pDependency.isVersionLess()) {
            pDependency.setVersion(computeMissingVersion(pServer));
        }

        return pDependency;
    }

    public Server getServerByRuntime(final Runtime pRuntime) {
        String serverLookupName = pRuntime.getServerName();
        if (null == serverLookupName) {
            throw new ServerNotFoundException(MessageFormat.format("Runtime {0} is not assigned to any server. Please check your configuration.", pRuntime.getRuntimeName()));
        }

        for (Server server : getServerConfigurations()) {
            if (serverLookupName.equals(server.getServerName())) {
                return server;
            }
        }

        throw new ServerNotFoundException(MessageFormat.format("Runtime {0} is assigned to {1} server. But no such server found. Is this a typo?", pRuntime.getRuntimeName(), serverLookupName));
    }

    public File findServerPathByRuntime(Runtime pRuntime) {
        return getServerByRuntime(pRuntime).getServerPath();
    }

    public List<Runtime> getRuntimesByServerName(final String pServerName) {
        final List<Runtime> runtimes = new ArrayList<Runtime>();

        ClosureUtils.each(getRuntimeConfigurations(), new Closure<Runtime>() {
            @Override
            public void exec(Runtime pParam) {
                if (pServerName.equals(pParam.getServerName())) {
                    runtimes.add(pParam);
                }
            }
        });

        return runtimes;
    }

    public List<Server> getServersByName(final String pServerName) {
        final List<Server> servers = new ArrayList<Server>();

        ClosureUtils.each(getServerConfigurations(), new Closure<Server>() {
            @Override
            public void exec(Server pParam) {
                if (pServerName.equals(pParam.getServerName())) {
                    servers.add(pParam);
                }
            }
        });

        return servers;
    }

    public Version computeMissingVersion(Runtime pConfiguration) {
        if (null == pConfiguration) {
            return getConfigurationVersion();
        }

        try {
            Server server = getServerByRuntime(pConfiguration);
            if (null != server.getVersion()) {
                return server.getVersion();
            }
        } catch (ServerNotFoundException e) {
            // no-op
        }

        return getConfigurationVersion();
    }

    public Version computeMissingVersion(Server pConfiguration) {
        if (null == pConfiguration) {
            return getConfigurationVersion();
        }

        if (null != pConfiguration.getVersion()) {
            return pConfiguration.getVersion();
        }

        return getConfigurationVersion();
    }

    public UpdatePolicy computeUpdatePolicy(Runtime pConfiguration) {
        if (null == pConfiguration) {
            return getConfigurationUpdatePolicy();
        }

        if (null != pConfiguration.getUpdatePolicy()) {
            return pConfiguration.getUpdatePolicy();
        }

        try {
            Server server = getServerByRuntime(pConfiguration);
            if (null != server.getUpdatePolicy()) {
                return server.getUpdatePolicy();
            }
        } catch (ServerNotFoundException e) {
            // no-op
        }

        return getConfigurationUpdatePolicy();
    }

    public UpdatePolicy computeUpdatePolicy(Server pConfiguration) {
        if (null == pConfiguration) {
            return getConfigurationUpdatePolicy();
        }

        if (null != pConfiguration.getUpdatePolicy()) {
            return pConfiguration.getUpdatePolicy();
        }

        return getConfigurationUpdatePolicy();
    }
}
