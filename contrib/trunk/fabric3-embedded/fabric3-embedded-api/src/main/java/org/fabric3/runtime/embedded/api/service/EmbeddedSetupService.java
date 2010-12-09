package org.fabric3.runtime.embedded.api.service;

import java.io.File;

/**
 * @author Michal Capo
 */
public interface EmbeddedSetupService {

    void initialize();

    void setServerFolder(String serverFolder);

    void setDeleteAtStop(boolean deleteAtStop);

    File getServerFolder();

    boolean shouldDeleteAtStop();

}
