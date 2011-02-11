package org.fabric3.runtime.embedded.api.service;

import java.io.File;

/**
 * Holds setup of embedded server like servers folder and so on.
 *
 * @author Michal Capo
 */
public interface EmbeddedSetup {

    /**
     * Set folder where server should be unpacked and started.
     *
     * @param serverFolder of embedded server
     */
    void setServerFolder(String serverFolder);

    /**
     * Should the server be deleted on stop?
     *
     * @param deleteAtStop <code>true</code> to delete it, <code>false</code> to not delete it
     */
    void setDeleteAtStop(boolean deleteAtStop);

    /**
     * Get servers folder.
     *
     * @return physical folder
     */
    File getServerFolder();

    /**
     * Should the server be deleted on stop?
     *
     * @return <code>true</code> to delete it, <code>false</code> to not delete it
     */
    boolean shouldDeleteAtStop();

}
