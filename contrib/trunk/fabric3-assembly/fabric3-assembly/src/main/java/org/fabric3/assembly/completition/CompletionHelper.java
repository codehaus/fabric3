package org.fabric3.assembly.completition;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.configuration.RuntimeMode;
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

        for (Server server : mConfig.getServers()) {
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

    public Version computeMissingVersion(Runtime pConfiguration) {
        if (null == pConfiguration) {
            return mConfig.getVersion();
        }

        try {
            Server server = getServerByRuntime(pConfiguration);
            if (null != server.getVersion()) {
                return server.getVersion();
            }
        } catch (ServerNotFoundException e) {
            // no-op
        }

        return mConfig.getVersion();
    }

    public Version computeMissingVersion(Server pConfiguration) {
        if (null == pConfiguration) {
            return mConfig.getVersion();
        }

        if (null != pConfiguration.getVersion()) {
            return pConfiguration.getVersion();
        }

        return mConfig.getVersion();
    }

    public UpdatePolicy computeUpdatePolicy(Runtime pConfiguration) {
        if (null == pConfiguration) {
            return mConfig.getUpdatePolicy();
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

        return mConfig.getUpdatePolicy();
    }

    public UpdatePolicy computeUpdatePolicy(Server pConfiguration) {
        if (null == pConfiguration) {
            return mConfig.getUpdatePolicy();
        }

        if (null != pConfiguration.getUpdatePolicy()) {
            return pConfiguration.getUpdatePolicy();
        }

        return mConfig.getUpdatePolicy();
    }
}
