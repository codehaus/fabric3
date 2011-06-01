package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.examples.Composite1Archive;
import org.fabric3.assembly.examples.Web1Archive;
import org.fabric3.assembly.factory.AssemblyConfigBuilder;
import org.fabric3.assembly.modifier.AssemblyModifier;
import org.fabric3.assembly.runner.AssemblyRunner;

import java.io.IOException;

/**
 * @author Michal Capo
 */
public class TestSingleRuntimeServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        AssemblyConfig config = AssemblyConfigBuilder.getBuilder()
                .setVersion("1.8")
                .setUpdatePolicy(UpdatePolicy.ALWAYS)

                .addServer("server1", "/tmp/fabric3_test_single")
                .addRuntime().withProfiles("web").toServer("server1")

//                .addArchive("composite1", new File("/tmp/composite1.jar")).deployToServer("server1")
//                .addArchive("composite1", "a:a:1.0").deployToServer("server1")
//                .addArchive(Test1Archive.create()).deployToServer("server1")
                .addArchive("comp", Composite1Archive.create())
                .addArchive("web", Web1Archive.create())

                .createConfiguration();

        AssemblyRunner runner = new AssemblyRunner(config);
        runner.startServer("server1");

//        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
//        runner.stopServer("server1");

        AssemblyModifier modifier = new AssemblyModifier(config);
        modifier.archive("comp").deployToServer("server1");
//        modifier.archive("comp").deployToServer("server1").undeploy().redeploy();
//        modifier.archive("web").deployToServer("server1").undeploy().redeploy();

    }

}
