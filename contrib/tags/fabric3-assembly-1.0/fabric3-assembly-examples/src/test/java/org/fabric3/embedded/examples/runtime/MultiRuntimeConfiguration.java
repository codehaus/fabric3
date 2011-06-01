package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.RuntimeMode;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.examples.Composite1Archive;
import org.fabric3.assembly.factory.AssemblyConfigBuilder;

/**
 * @author Michal Capo
 */
public class MultiRuntimeConfiguration {

    public static AssemblyConfig create() {
        return AssemblyConfigBuilder.getBuilder()
                .setVersion("1.8")
                .setUpdatePolicy(UpdatePolicy.ALWAYS)

                .addServer("server1", "/tmp/fabric3_test_multi")

                .addRuntime("server1", "controller", RuntimeMode.CONTROLLER).withProfiles("web", "web-service")
                .addRuntime("server1", "par1", RuntimeMode.PARTICIPANT).withProfiles("web")
                .addRuntime("server1", "par2", RuntimeMode.PARTICIPANT).withProfiles("web-service")

                .addArchive("comp", Composite1Archive.create())

                .createConfiguration();
    }

}
