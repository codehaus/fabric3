package org.fabric3.assembly.assembly;

import org.fabric3.assembly.IAssemblyStep;
import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.configuration.Server;

/**
 * @author Michal Capo
 */
public class Assembly implements IAssemblyStep {

    private AssemblyServer mServerAssembly = new AssemblyServer();

    private AssemblyRuntime mRuntimeAssembly = new AssemblyRuntime();

    private AssemblyConfig mConfig;

    public Assembly(AssemblyConfig pConfig) {
        mConfig = pConfig;
    }

    public void process() {
        for (Server server : mConfig.getServers()) {
            mServerAssembly.doAssembly(server);
        }

        for (Runtime runtime : mConfig.getRuntimes()) {
            mRuntimeAssembly.doAssembly(runtime);
        }


    }

}
