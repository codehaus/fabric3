package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.runner.AssemblyRunner;

import java.io.IOException;

/**
 * @author Michal Capo
 */
public class SingleRuntimeStart {

    public static void main(String[] args) throws IOException, InterruptedException {
        AssemblyConfig config = SingleRuntimeConfiguration.create();

        AssemblyRunner runner = new AssemblyRunner(config);
        runner.startServer("server1");
    }

}
