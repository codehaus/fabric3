package org.fabric3.runtime.embedded.api.service;

import org.fabric3.runtime.embedded.api.EmbeddedProfile;

import java.io.File;

/**
 * Shared folders used by every instance of embedded server.
 *
 * @author Michal Capo
 */
public interface EmbeddedSharedFolders {

    /**
     * Get boot folder of embedded server.
     *
     * @return physical folder
     */
    File getBootFolder();

    /**
     * Get extensions folder of embedded server.
     *
     * @return physical folder
     */
    File getExtensionsFolder();

    /**
     * Get host folder of embedded server.
     *
     * @return physical folder
     */
    File getHostFolder();

    /**
     * Get lib folder of embedded server.
     *
     * @return physical folder
     */
    File getLibFolder();

    /**
     * Get folder for given profile. If given profile is not unpacked this will do it.
     *
     * @param profile you want to get folder for
     * @return physical folder related to this profile
     */
    File getProfileFolder(EmbeddedProfile profile);

}
