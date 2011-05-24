package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.profile.fabric.FabricProfiles;
import org.fabric3.assembly.factory.ConfigurationBuilder;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Michal Capo
 */
public class TestSingleRuntimeServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        AssemblyConfig config = ConfigurationBuilder.getBuilder()
                .setVersion("1.8")
                .addServer("/tmp/fabric3_test_single")
                .addRuntime(FabricProfiles.WEB)
                .setUpdatePolicy(UpdatePolicy.ALWAYS)
                .createConfiguration();

        config.process();
    }

    private static String projectPath() throws IOException {
        Properties properties = new Properties();
        properties.load(TestSingleRuntimeServer.class.getResourceAsStream("/paths.properties"));

        return properties.getProperty("projectPath");
    }

}
