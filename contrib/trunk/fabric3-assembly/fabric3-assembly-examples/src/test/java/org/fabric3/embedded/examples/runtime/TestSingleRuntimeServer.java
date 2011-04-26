package org.fabric3.embedded.examples.runtime;

import org.fabric3.assembly.configuration.AssemblyConfiguration;
import org.fabric3.assembly.factory.ConfigurationBuilder;
import org.fabric3.assembly.profile.Profiles;
import org.fabric3.assembly.profile.UpdatePolicy;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Michal Capo
 */
public class TestSingleRuntimeServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        AssemblyConfiguration configuration = ConfigurationBuilder.getBuilder()
                .addServer("/tmp/fabric3_test_single")
                .addRuntime(Profiles.WEB)
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
