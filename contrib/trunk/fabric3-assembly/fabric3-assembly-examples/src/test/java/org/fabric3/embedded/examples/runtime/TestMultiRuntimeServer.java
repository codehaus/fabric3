package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfiguration;
import org.fabric3.assembly.configuration.RuntimeMode;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.Version;
import org.fabric3.assembly.dependency.profile.fabric.FabricProfiles;
import org.fabric3.assembly.factory.ConfigurationBuilder;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Michal Capo
 */
public class TestMultiRuntimeServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        AssemblyConfiguration configuration = ConfigurationBuilder.getBuilder()
                .addServer("server1", "/tmp/fabric3_test_multi", new Version("1.8"), FabricProfiles.WEB)
                .addRuntime("server1", "controller", RuntimeMode.CONTROLLER, FabricProfiles.WEB, FabricProfiles.WEB_SERVICE)
                .addRuntime("server1", "par1", RuntimeMode.PARTICIPANT, FabricProfiles.WEB)
                .addRuntime("server1", "par2", RuntimeMode.PARTICIPANT, FabricProfiles.WEB_SERVICE)
                .setUpdatePolicy(UpdatePolicy.ALWAYS)
                .createConfiguration();

        configuration.doAssembly();
    }

    private static String projectPath() throws IOException {
        Properties properties = new Properties();
        properties.load(TestSingleRuntimeServer.class.getResourceAsStream("/paths.properties"));

        return properties.getProperty("projectPath");
    }

}
