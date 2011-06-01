package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.factory.AssemblyConfigBuilder;
import org.fabric3.assembly.runner.AssemblyRunner;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author Michal Capo
 */
public class TestSingleRuntimeServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        AssemblyConfig config = AssemblyConfigBuilder.getBuilder()
                .setVersion("1.8")
                .setUpdatePolicy(UpdatePolicy.ALWAYS)

                .addProfile("test2").dependency("org.codehaus.fabric3:fabric3-junit:1.8").dependency("org.codehaus.fabric3:fabric3-jaxb:1.8")

                .addServer("server1", "/tmp/fabric3_test_single")
                .addRuntime().withProfiles("web").toServer("server1")

//                .addComposite("composite1", new File("/tmp/composite1.jar")).deployToServer("server1")
                .addComposite("composite1", "a:a:1.0").deployToServer("server1")
//                .addComposite(Composite1Archive.create()).deployToServer("server1")

                .createConfiguration();
        // config.process();

        AssemblyRunner runner = new AssemblyRunner(config);
        runner.startServer("server1");

        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        runner.stopServer("server1");
    }

}
