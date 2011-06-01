package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.modifier.AssemblyModifier;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Michal Capo
 */
public class SingleRuntimeCycle {

    public static void main(String[] args) throws IOException, InterruptedException {

        AssemblyModifier modifier = new AssemblyModifier(SingleRuntimeConfiguration.create());

        // start server
        modifier.startServer("server1");
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));

        // deploy composite
        modifier.getArchive("comp").deployToServer("server1");
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));

        // undeploy composite
        modifier.getArchive("comp").undeploy();
        Thread.sleep(TimeUnit.SECONDS.toMillis(5));

        // stop server
        modifier.stopServer("server1");
    }

}
