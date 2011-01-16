package org.fabric3.runtime.embedded;

import org.fabric3.runtime.embedded.api.EmbeddedProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Profile implementation. Name will be used also for folder name. Profiles folder needs to be set to some physical
 * folder in initialisation phase of embedded server. Otherwise getProfileFolder will throw an exception.
 *
 * @author Michal Capo
 */
public class EmbeddedProfileImpl implements EmbeddedProfile {

    /**
     * Profile name.
     */
    private String name;

    /**
     * Profile folder.
     */
//    private File profileFolder;

    /**
     * File specified for this profile.
     */
    protected List<String> files = new ArrayList<String>();

    /**
     * Create profile with given name.
     *
     * @param pName of profile
     */
    public EmbeddedProfileImpl(String pName) {
        name = pName;
    }

    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    public String getFolderName() {
        return name;
    }

    public List<String> getFiles() {
        return files;
    }

/*
    public File getProfileFolder() {
        if (!isAttached()) {
            throw new EmbeddedFabric3SetupException(String.format("Profile '%s' is not attached to existing folder. This may be caused by an error or no initialization had been made.", getName()));
        }

        return profileFolder;
    }

    public void setProfileFolder(File pProfileFolder) {
        profileFolder = pProfileFolder;
    }

    public boolean isAttached() {
        return null != getProfileFolder() && getProfileFolder().exists();
    }
*/
}
