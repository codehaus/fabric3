package org.fabric3.runtime.embedded.api;

import org.fabric3.host.contribution.ContributionException;
import org.fabric3.host.domain.DeploymentException;
import org.fabric3.runtime.embedded.api.service.EmbeddedLoggerService;
import org.fabric3.runtime.embedded.api.service.EmbeddedProfileService;
import org.fabric3.runtime.embedded.api.service.EmbeddedRuntimeService;
import org.fabric3.runtime.embedded.api.service.EmbeddedSetupService;
import org.fabric3.runtime.embedded.api.service.EmbeddedSharedFoldersService;
import org.fabric3.runtime.embedded.api.service.EmbeddedUpdatePolicyService;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

/**
 * @author Michal Capo
 */
public interface EmbeddedServer {

    EmbeddedLoggerService getLoggerService();

    EmbeddedSharedFoldersService getSharedFoldersService();

    EmbeddedProfileService getProfileService();

    EmbeddedSetupService getSetupService();

    EmbeddedUpdatePolicyService getUpdatePolicyService();

    EmbeddedRuntimeService getRuntimeService();

    void initialize();

    void start();

    void stop();

    void installComposite(String path) throws ContributionException, DeploymentException, MalformedURLException, URISyntaxException;

    void installComposite(EmbeddedComposite composite) throws ContributionException, DeploymentException;

/*
    void installComposites(List<EmbeddedComposite> composites);

    List<EmbeddedComposite> getInstalledComposites();

    void uninstallComposite(EmbeddedComposite composite);

    void uninstallComposites(List<EmbeddedComposite> composites);

    void uninstallAll();

    void redeployComposite(EmbeddedComposite composite);

    void redeployComposites(List<EmbeddedComposite> composites);

    void redeployAll();
*/

}
