package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.factory.AssemblyConfigBuilder;
import org.fabric3.assembly.runner.AssemblyRunner;
import org.fabric3.embedded.examples.Test1Archive;

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

                .addServer("server1", "/tmp/fabric3_test_single")

                        //TODO <capo> test if test suite can be running on this server
                .addRuntime().withProfiles("web", "test").toServer("server1")

//                .addComposite("composite1", new File("/tmp/composite1.jar")).deployToServer("server1")
//                .addComposite("composite1", "a:a:1.0").deployToServer("server1")
//                .addComposite(Composite1Archive.create()).deployToServer("server1")
                .addComposite(Test1Archive.create()).deployToServer("server1")

                .createConfiguration();
        // config.process();

        AssemblyRunner runner = new AssemblyRunner(config);
        runner.startServer("server1");

        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
        runner.stopServer("server1");
    }

}
