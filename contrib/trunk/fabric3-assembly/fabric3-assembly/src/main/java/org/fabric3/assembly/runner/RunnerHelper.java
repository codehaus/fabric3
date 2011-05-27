package org.fabric3.assembly.runner;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.exception.ServerNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public class RunnerHelper {

    private AssemblyConfig mConfig;

    public RunnerHelper(AssemblyConfig pConfig) {
        mConfig = pConfig;
    }

    public Server getServerByName(final String pServerName) {
        for (Server server : mConfig.getServers()) {
            if (pServerName.equals(server.getServerName())) {
                return server;
            }
        }

        throw new ServerNotFoundException("Server ''{0}'' not found. Is this a typo?", pServerName);
    }

    public List<Runtime> getRuntimesByServerName(String pServerName) {
        List<Runtime> runtimes = new ArrayList<Runtime>();

        for (Runtime runtime : mConfig.getRuntimes()) {
            if (runtime.getServerName().equals(pServerName)) {
                runtimes.add(runtime);
            }
        }

        return runtimes;
    }
}
