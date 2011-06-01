package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.examples.Composite1Archive;
import org.fabric3.assembly.examples.Web1Archive;
import org.fabric3.assembly.factory.AssemblyConfigBuilder;
import org.fabric3.assembly.modifier.AssemblyModifier;
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

                .addServer("server1", "/tmp/fabric3_test_single")
                .addRuntime().withProfiles("web").toServer("server1")

//                .addArchive("composite1", new File("/tmp/composite1.jar")).addToServer("server1")
//                .addArchive("composite1", "a:a:1.0").addToServer("server1")
//                .addArchive(Test1Archive.create()).addToServer("server1")
                .addArchive("comp", Composite1Archive.create())
                .addArchive("web", Web1Archive.create())

                .createConfiguration();


        AssemblyRunner runner = new AssemblyRunner(config);
        AssemblyModifier modifier = new AssemblyModifier(config);

        runner.startServer("server1");

        modifier.archive("comp").deployToServer("server1");

        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        modifier.archive("comp").redeploy();

        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        modifier.archive("comp").undeploy();

        Thread.sleep(TimeUnit.SECONDS.toMillis(5));
        runner.stopServer("server1");


    }

}
