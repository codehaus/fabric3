package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.modifier.AssemblyModifier;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Michal Capo
 */
public class MultiRuntimeCycle {

    public static void main(String[] args) throws IOException, InterruptedException {

        AssemblyModifier modifier = new AssemblyModifier(MultiRuntimeConfiguration.create());

        // start server
        modifier.startServer("server1");
        Thread.sleep(TimeUnit.SECONDS.toMillis(30));

        // deploy composite
        modifier.archive("comp").deployToServer("server1");
        Thread.sleep(TimeUnit.SECONDS.toMillis(10));

        // undeploy composite
        modifier.archive("comp").undeploy();
        Thread.sleep(TimeUnit.SECONDS.toMillis(10));

        // stop server
        modifier.stopServer("server1");
    }

}
