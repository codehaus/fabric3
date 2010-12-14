package org.fabric3.runtime.embedded.api;

import org.fabric3.runtime.embedded.api.service.*;

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

    void installComposite(String path);

    void installComposite(EmbeddedComposite composite);

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
