package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.examples.Composite1Archive;
import org.fabric3.assembly.examples.Web1Archive;
import org.fabric3.assembly.factory.AssemblyConfigBuilder;

/**
 * @author Michal Capo
 */
public class SingleRuntimeConfiguration {

    public static AssemblyConfig create() {
        return AssemblyConfigBuilder.getBuilder()
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
    }

}
