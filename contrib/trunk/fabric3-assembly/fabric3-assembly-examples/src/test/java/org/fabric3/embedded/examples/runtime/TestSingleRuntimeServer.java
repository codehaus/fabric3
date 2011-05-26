package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.factory.ConfigurationBuilder;
import org.fabric3.assembly.runner.AssemblyRunner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Michal Capo
 */
public class TestSingleRuntimeServer {

    public static void main(String[] args) throws IOException, InterruptedException {

        AssemblyConfig config = ConfigurationBuilder.getBuilder()
                .setVersion("1.8")
                .setUpdatePolicy(UpdatePolicy.ALWAYS)

                .addProfile("test2").dependency("org.codehaus.fabric3:fabric3-junit:1.8").dependency("org.codehaus.fabric3:fabric3-jaxb:1.8")

                .addServer("server1", "/tmp/fabric3_test_single")
                .addRuntime().withProfiles("web").toServer("server1")

                .createConfiguration();
        // config.process();

        AssemblyRunner runner = new AssemblyRunner(config);
        runner.startServer("server1");

        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        runner.stopServer("server1");
    }

}
