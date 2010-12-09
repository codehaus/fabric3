package org.fabric3.embedded.examples.runtime;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.runtime.embedded.api.EmbeddedProfile;
import org.fabric3.runtime.embedded.api.EmbeddedServer;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3StartupException;
import org.fabric3.runtime.embedded.factory.EmbeddedServerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * @author Michal Capo
 */
public class TestSingleRuntimeServer {

    public static void main(String[] args) throws InterruptedException, IOException, EmbeddedFabric3StartupException, ContributionException, DeploymentException, URISyntaxException {
        //TODO hide all exceptions
        EmbeddedServer server = EmbeddedServerFactory.singleRuntime("/tmp/fabric3_embedded", "/runtime/system.xml", EmbeddedProfile.WEB);
        server.start();

        server.installComposite("embedded.classpath:/composite1/testComposite.composite");
        server.installComposite("embedded.file:" + projectPath() + "/src/main/webapp/WEB-INF/web.composite");
    }

    private static String projectPath() throws IOException {
        Properties properties = new Properties();
        properties.load(TestSingleRuntimeServer.class.getResourceAsStream("/paths.properties"));

        return properties.getProperty("projectPath");
    }

}
