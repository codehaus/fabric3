package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.RuntimeMode;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.factory.ConfigurationBuilder;
import org.fabric3.assembly.runner.AssemblyRunner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Michal Capo
 */
public class TestMultiRuntimeServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        AssemblyConfig config = ConfigurationBuilder.getBuilder()
                .setVersion("1.8")
                .setUpdatePolicy(UpdatePolicy.ALWAYS)

                .addServer("server1", "/tmp/fabric3_test_multi")
                .addRuntime("server1", "controller", RuntimeMode.CONTROLLER).withProfiles("web", "web-service")
                .addRuntime("server1", "par1", RuntimeMode.PARTICIPANT).withProfiles("web")
                .addRuntime("server1", "par2", RuntimeMode.PARTICIPANT).withProfiles("web-service")

                .addServer("server2", "/tmp/fabric3_test_multi2")
                .addRuntime("server2", "par1", RuntimeMode.PARTICIPANT).withProfiles("web")

                .createConfiguration();
        // config.process();

        AssemblyRunner runner = new AssemblyRunner(config);
        runner.startServer("server1");
        runner.startServer("server2");

        Thread.sleep(TimeUnit.MINUTES.toMillis(1));
        runner.stopServer("server1");
        runner.stopServer("server2");

    }

}
