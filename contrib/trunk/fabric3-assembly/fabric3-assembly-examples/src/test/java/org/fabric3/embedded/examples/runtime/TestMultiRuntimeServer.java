package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.RuntimeMode;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.fabric.FabricProfiles;
import org.fabric3.assembly.factory.ConfigurationBuilder;

import java.io.IOException;

/**
 * @author Michal Capo
 */
public class TestMultiRuntimeServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        AssemblyConfig config = ConfigurationBuilder.getBuilder()
                .setVersion("1.8")
                .addServer("server1", "/tmp/fabric3_test_multi", FabricProfiles.WEB)
                .addRuntime("server1", "controller", RuntimeMode.CONTROLLER, FabricProfiles.WEB, FabricProfiles.WEB_SERVICE)
                .addRuntime("server1", "par1", RuntimeMode.PARTICIPANT, FabricProfiles.WEB)
                .addRuntime("server1", "par2", RuntimeMode.PARTICIPANT, FabricProfiles.WEB_SERVICE)
                .setUpdatePolicy(UpdatePolicy.ALWAYS)
                .createConfiguration();

        config.process();
    }

}
