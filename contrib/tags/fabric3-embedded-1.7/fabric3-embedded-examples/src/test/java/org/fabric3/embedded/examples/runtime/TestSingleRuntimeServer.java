package org.fabric3.embedded.examples.runtime;

import org.fabric3.runtime.embedded.Profile;
import org.fabric3.runtime.embedded.api.EmbeddedServer;
import org.fabric3.runtime.embedded.factory.EmbeddedServerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Michal Capo
 */
public class TestSingleRuntimeServer {

    public static void main(String[] args) throws IOException, InterruptedException {
        EmbeddedServer server = EmbeddedServerFactory.singleRuntime("/tmp/fabric3_embedded", "/runtime/system.xml", Profile.WEB);
        server.start();

        // composite
        server.deployComposite("/composite1/");
        server.deployComposite(projectPath() + "/src/main/webapp/");

        // test composite
        server.deployComposite("/compositeTest/");

        // start all tests
        server.executeTests();
    }

    private static String projectPath() throws IOException {
        Properties properties = new Properties();
        properties.load(TestSingleRuntimeServer.class.getResourceAsStream("/paths.properties"));

        return properties.getProperty("projectPath");
    }

}
