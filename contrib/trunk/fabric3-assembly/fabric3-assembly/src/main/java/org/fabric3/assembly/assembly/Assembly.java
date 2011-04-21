package org.fabric3.assembly.assembly;

import org.fabric3.assembly.configuration.AssemblyConfiguration;
import org.fabric3.assembly.configuration.RuntimeConfiguration;
import org.fabric3.assembly.configuration.ServerConfiguration;

/**
 * @author Michal Capo
 */
public class Assembly {

    private AssemblyServer serverAssembly = new AssemblyServer();

    private AssemblyRuntime runtimeAssembly = new AssemblyRuntime();

    public void doAssembly(AssemblyConfiguration pConfiguration) {
        for (ServerConfiguration server : pConfiguration.getServers()) {
            serverAssembly.doAssembly(server, pConfiguration.getUpdatePolicy());
        }

        for (RuntimeConfiguration runtime : pConfiguration.getRuntimes()) {
            runtimeAssembly.doAssembly(runtime, pConfiguration.getUpdatePolicy(), pConfiguration.getServerLookupPath());
        }
    }

}
