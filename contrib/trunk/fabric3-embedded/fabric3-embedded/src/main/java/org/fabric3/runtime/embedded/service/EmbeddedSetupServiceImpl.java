package org.fabric3.runtime.embedded.service;

import org.fabric3.runtime.embedded.api.service.EmbeddedLoggerService;
import org.fabric3.runtime.embedded.api.service.EmbeddedSetupService;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3SetupException;
import org.fabric3.runtime.embedded.util.FileSystem;

import java.io.File;

/**
 * @author Michal Capo
 */
public class EmbeddedSetupServiceImpl implements EmbeddedSetupService {

    private String mServerFolder;

    /**
     * Default flag 'delete on stop' is false.
     */
    private boolean mDeleteOnStop = false;

    private EmbeddedLoggerService mLoggerService;

    public EmbeddedSetupServiceImpl(EmbeddedLoggerService loggerService) {
        mLoggerService = loggerService;

        if (null == mLoggerService) {
            throw new EmbeddedFabric3SetupException("Logger service cannot be null.");
        }
    }

    public void initialize() {
        mLoggerService.log("starting in folder - " + getServerFolder());

        if (getServerFolder().exists()) {
            // cleanup existing directory structure
            mLoggerService.log("cleaning up - " + getServerFolder().getAbsolutePath());

            for (File file : FileSystem.filesIn(FileSystem.folder(getServerFolder(), "runtimes"))) {
                FileSystem.delete(FileSystem.filesIn(FileSystem.folder(file, "tmp")));
                FileSystem.delete(FileSystem.filesIn(FileSystem.folder(file, "data")));
                FileSystem.delete(FileSystem.filesIn(FileSystem.folder(file, "deploy")));
                FileSystem.delete(FileSystem.filesIn(FileSystem.folder(file, "repository/user")));
            }
        } else {
            // create runtimes folder
            FileSystem.createFolder(FileSystem.folder(getServerFolder(), "runtimes"));
        }
    }

    public void setServerFolder(String serverFolder) {
        mServerFolder = serverFolder;
    }

    public void setDeleteAtStop(boolean deleteAtStop) {
        mDeleteOnStop = deleteAtStop;
    }

    public File getServerFolder() {
        String temp;

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
