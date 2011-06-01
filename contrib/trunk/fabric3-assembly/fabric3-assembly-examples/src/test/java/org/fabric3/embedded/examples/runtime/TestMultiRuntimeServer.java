package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.RuntimeMode;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.examples.Composite1Archive;
import org.fabric3.assembly.factory.AssemblyConfigBuilder;
import org.fabric3.assembly.runner.AssemblyRunner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Michal Capo
 */
public class TestMultiRuntimeServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        AssemblyConfig config = AssemblyConfigBuilder.getBuilder()
                .setVersion("1.8")
                .setUpdatePolicy(UpdatePolicy.ALWAYS)

                .addServer("server1", "/tmp/fabric3_test_multi")
                .addRuntime("server1", "controller", RuntimeMode.CONTROLLER).withProfiles("web", "web-service")
                .addRuntime("server1", "par1", RuntimeMode.PARTICIPANT).withProfiles("web")
                .addRuntime("server1", "par2", RuntimeMode.PARTICIPANT).withProfiles("web-service")

//                .addArchive("composite1", new File("/tmp/composite1.jar")).addToServer("server1")
//                .addArchive("composite1", "a:a:1.0").addToServer("server1")

                .addArchive(Composite1Archive.create()).addToServer("server1")

                .createConfiguration();
        // config.process();

        AssemblyRunner runner = new AssemblyRunner(config);
        runner.startServer("server1");

        Thread.sleep(TimeUnit.SECONDS.toMillis(30));
        runner.stopServer("server1");
    }

}
