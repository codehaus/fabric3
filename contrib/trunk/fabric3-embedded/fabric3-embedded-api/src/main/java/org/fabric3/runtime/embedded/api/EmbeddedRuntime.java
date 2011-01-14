package org.fabric3.runtime.embedded.api;

import org.fabric3.host.RuntimeMode;
import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.host.runtime.InitializationException;
import org.fabric3.host.runtime.ShutdownException;
import org.fabric3.runtime.embedded.util.IncreasableCountDownLatch;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

/**
 * @author Michal Capo
 */
public interface EmbeddedRuntime {

    String getName();

    URL getSystemConfig();

    RuntimeMode getRuntimeMode();

    File getRuntimeFolder();

    void startRuntime() throws IOException, InitializationException;

    void stopRuntime() throws ShutdownException;

    void installComposite(IncreasableCountDownLatch latch, EmbeddedComposite composite) throws ContributionException, DeploymentException;

    <T> T getComponent(Class<T> pClass, URI pURI);
}
