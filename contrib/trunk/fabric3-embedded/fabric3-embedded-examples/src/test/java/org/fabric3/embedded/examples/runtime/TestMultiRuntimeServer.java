package org.fabric3.embedded.examples.runtime;

import org.fabric3.runtime.embedded.Profile;
import org.fabric3.runtime.embedded.api.EmbeddedServer;
import org.fabric3.runtime.embedded.factory.EmbeddedServerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Michal Capo
 */
public class TestMultiRuntimeServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        EmbeddedServer server = EmbeddedServerFactory.multiRuntime("/tmp/fabric3_embedded_multi");
        EmbeddedServerFactory.addControllerRuntime(server, "/runtime/controller.xml", Profile.WEB);
        EmbeddedServerFactory.addParticipantRuntime(server, "runtime1", "/runtime/runtime1.xml", Profile.WEB);
        EmbeddedServerFactory.addParticipantRuntime(server, "runtime2", "/runtime/runtime2.xml");

        server.start();

        // composite
        server.installComposite("/composite1/");
        server.installComposite(projectPath() + "/src/main/webapp/");

        // test composite
        server.installComposite("/compositeTest/");

        // start all tests
        server.executeTestsOnRuntime("runtime1");
    }

    private static String projectPath() throws IOException {
        Properties properties = new Properties();
        properties.load(TestSingleRuntimeServer.class.getResourceAsStream("/paths.properties"));

        return properties.getProperty("projectPath");
    }

}
