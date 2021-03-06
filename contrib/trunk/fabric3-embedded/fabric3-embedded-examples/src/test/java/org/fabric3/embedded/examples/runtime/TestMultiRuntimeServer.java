package org.fabric3.embedded.examples.runtime;

import org.fabric3.runtime.embedded.Profile;
import org.fabric3.runtime.embedded.api.EmbeddedServer;
import org.fabric3.runtime.embedded.api.EmbeddedUpdatePolicy;
import org.fabric3.runtime.embedded.factory.EmbeddedServerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Michal Capo
 */
public class TestMultiRuntimeServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        EmbeddedServer server = EmbeddedServerFactory.multiRuntime("/tmp/fabric3_embedded_multi", EmbeddedUpdatePolicy.ALWAYS);
        EmbeddedServerFactory.addControllerRuntime(server, "/runtime/controller.xml", Profile.WEB);
        EmbeddedServerFactory.addParticipantRuntime(server, "runtime1", "/runtime/runtime1.xml", Profile.WEB);
        EmbeddedServerFactory.addParticipantRuntime(server, "runtime2", "/runtime/runtime2.xml");

        server.start();

        // composite
        server.deployComposite("/composite1/");
        server.deployComposite(projectPath() + "/src/main/webapp/");

        // test composite
        server.deployComposite("/compositeTest/");

        // include in domain/activate all deployed composites
        server.activeAllDeployedComposites();

        // start all tests
        server.executeTestsOnRuntime("runtime1");
    }

    private static String projectPath() throws IOException {
        Properties properties = new Properties();
        properties.load(TestSingleRuntimeServer.class.getResourceAsStream("/paths.properties"));

        return properties.getProperty("projectPath");
    }

}
