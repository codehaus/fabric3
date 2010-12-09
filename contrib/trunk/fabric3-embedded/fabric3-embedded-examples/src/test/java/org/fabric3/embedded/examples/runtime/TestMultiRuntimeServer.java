package org.fabric3.embedded.examples.runtime;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.runtime.embedded.api.EmbeddedServer;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3StartupException;
import org.fabric3.runtime.embedded.factory.EmbeddedServerFactory;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * @author Michal Capo
 */
public class TestMultiRuntimeServer {

    public static void main(String[] args) throws InterruptedException, MalformedURLException, EmbeddedFabric3StartupException, ContributionException, DeploymentException, URISyntaxException {
        //TODO make multi runtime working
        EmbeddedServer server = EmbeddedServerFactory.multiRuntime("/tmp/fabric3_embedded_multi");
        EmbeddedServerFactory.addControllerRuntime(server, "controller");
        EmbeddedServerFactory.addParticipantRuntime(server, "runtime1");

        server.start();

        server.installComposite("embedded.composite:/composite1/testComposite.composite");
    }

}
