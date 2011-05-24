package org.fabric3.assembly.assembly;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.configuration.Server;

/**
 * @author Michal Capo
 */
public class Assembly {

    private AssemblyServer serverAssembly = new AssemblyServer();

    private AssemblyRuntime runtimeAssembly = new AssemblyRuntime();

    public void doAssembly(AssemblyConfig pConfig) {
        for (Server server : pConfig.getServers()) {
            serverAssembly.doAssembly(server, pConfig.getConfigurationHelper());
        }

        for (Runtime runtime : pConfig.getRuntimes()) {
            runtimeAssembly.doAssembly(runtime, pConfig.getConfigurationHelper());
        }
    }

}
