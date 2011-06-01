package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.modifier.AssemblyModifier;
import org.fabric3.assembly.runner.AssemblyRunner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Michal Capo
 */
public class MultiRuntimeCycle {

    public static void main(String[] args) throws IOException, InterruptedException {
        AssemblyConfig config = MultiRuntimeConfiguration.create();

        AssemblyRunner runner = new AssemblyRunner(config);
        AssemblyModifier modifier = new AssemblyModifier(config);

        // start server
        runner.startServer("server1");
        Thread.sleep(TimeUnit.SECONDS.toMillis(30));

        // deploy composite
        modifier.archive("comp").deployToServer("server1");
        Thread.sleep(TimeUnit.SECONDS.toMillis(10));

        // undeploy composite
        modifier.archive("comp").undeploy();
        Thread.sleep(TimeUnit.SECONDS.toMillis(10));

        // stop server
        runner.stopServer("server1");
    }

}
