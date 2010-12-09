package org.fabric3.runtime.embedded;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.runtime.embedded.api.EmbeddedProfile;
import org.fabric3.runtime.embedded.api.EmbeddedServer;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3StartupException;
import org.fabric3.runtime.embedded.factory.EmbeddedServerFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * @author Michal Capo
 */
public class EmbeddedServerTest {

    public static void main(String[] args) throws InterruptedException, MalformedURLException, EmbeddedFabric3StartupException, ContributionException, DeploymentException, URISyntaxException {
        //TODO hide all exceptions
        EmbeddedServer server = EmbeddedServerFactory.singleRuntime("/tmp/fabric3_embedded", "/system.xml", EmbeddedProfile.WEB);
        server.start();

        server.installComposite("embedded.classpath:/composite/testComposite.composite");
        server.installComposite("embedded.file:/home/michal/research/fabric3/tries/embedded-server/embedded/fabric3-embedded/src/test/webapp/WEB-INF/web.composite");
    }

}
