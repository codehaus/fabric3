package org.fabric3.runtime.embedded.service;

import org.fabric3.runtime.embedded.api.service.EmbeddedSetup;
import org.fabric3.runtime.embedded.util.FileSystem;

import java.io.File;

/**
 * @author Michal Capo
 */
public class EmbeddedSetupImpl implements EmbeddedSetup {

    private String mServerFolder;

    /**
     * Default flag 'delete on stop' is false.
     */
    private boolean mDeleteOnStop = false;

    public void setServerFolder(String serverFolder) {
        mServerFolder = serverFolder;
    }

    public void setDeleteAtStop(boolean deleteAtStop) {
        mDeleteOnStop = deleteAtStop;
    }

    public File getServerFolder() {
        // if server folder doesn't exists generate one
        if (null == mServerFolder) {
            mServerFolder = FileSystem.temporaryFolder() + File.separator + FileSystem.generateFolderName();
        }

        return new File(mServerFolder);
    }

    public boolean shouldDeleteAtStop() {
        return mDeleteOnStop;
    }

}
