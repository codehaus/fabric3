package org.fabric3.embedded.examples.runtime;

import org.fabric3.runtime.embedded.api.EmbeddedProfile;
import org.fabric3.runtime.embedded.api.EmbeddedServer;
import org.fabric3.runtime.embedded.factory.EmbeddedServerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Michal Capo
 */
public class TestSingleRuntimeServer {

    public static void main(String[] args) throws IOException {
        EmbeddedServer server = EmbeddedServerFactory.singleRuntime("/tmp/fabric3_embedded", "/runtime/system.xml", EmbeddedProfile.WEB);
        server.start();

        server.installComposite("/composite1/");
        server.installComposite(projectPath() + "/src/main/webapp/");
    }

    private static String projectPath() throws IOException {
        Properties properties = new Properties();
        properties.load(TestSingleRuntimeServer.class.getResourceAsStream("/paths.properties"));

        return properties.getProperty("projectPath");
    }

}
