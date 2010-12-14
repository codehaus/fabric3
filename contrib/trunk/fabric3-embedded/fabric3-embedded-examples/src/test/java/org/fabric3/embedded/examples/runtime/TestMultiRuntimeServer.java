package org.fabric3.embedded.examples.runtime;

import org.fabric3.runtime.embedded.api.EmbeddedProfile;
import org.fabric3.runtime.embedded.api.EmbeddedServer;
import org.fabric3.runtime.embedded.factory.EmbeddedServerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * @author Michal Capo
 */
public class TestMultiRuntimeServer {

    public static void main(String[] args) throws IOException {
        EmbeddedServer server = EmbeddedServerFactory.multiRuntime("/tmp/fabric3_embedded_multi", EmbeddedProfile.WEB);
        EmbeddedServerFactory.addControllerRuntime(server, "/runtime/controller.xml");
        EmbeddedServerFactory.addParticipantRuntime(server, "runtime1", "/runtime/runtime1.xml");
        EmbeddedServerFactory.addParticipantRuntime(server, "runtime2", "/runtime/runtime2.xml");

        server.start();

        server.installComposite("embedded.composite:/composite1/testComposite.composite");
        server.installComposite("embedded.war:" + projectPath() + "/src/main/webapp/WEB-INF/web.composite");
    }

    private static String projectPath() throws IOException {
        Properties properties = new Properties();
        properties.load(TestSingleRuntimeServer.class.getResourceAsStream("/paths.properties"));

        return properties.getProperty("projectPath");
    }


}
