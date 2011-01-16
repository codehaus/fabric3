package org.fabric3.runtime.embedded.service;

import org.fabric3.runtime.embedded.api.EmbeddedProfile;
import org.fabric3.runtime.embedded.api.EmbeddedVersion;
import org.fabric3.runtime.embedded.api.service.EmbeddedDependencyResolver;
import org.fabric3.runtime.embedded.api.service.EmbeddedDependencyUpdatePolicy;
import org.fabric3.runtime.embedded.api.service.EmbeddedLogger;
import org.fabric3.runtime.embedded.api.service.EmbeddedSharedFolders;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3SetupException;
import org.fabric3.runtime.embedded.exception.EmbeddedFabric3StartupException;
import org.fabric3.runtime.embedded.util.DateTime;
import org.fabric3.runtime.embedded.util.FileSystem;
import org.fabric3.runtime.embedded.util.Zip;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;

import static org.fabric3.runtime.embedded.util.FileSystem.folder;
import static org.fabric3.runtime.embedded.util.FileSystem.temporaryFolder;

/**
 * @author Michal Capo
 */
public class EmbeddedSharedFoldersImpl implements EmbeddedSharedFolders {

    private static File mSharedFolder = folder(temporaryFolder() + File.separator + "fabric3_sharedLibs-" + EmbeddedVersion.FABRIC3);

    private static File mBootFolder;
    private static File mExtensionsFolder;
    private static File mHostFolder;
    private static File mLibFolder;

    private EmbeddedDependencyResolver mMavenResolver;
    private EmbeddedDependencyUpdatePolicy mUpdatePolicy;
    private EmbeddedLogger mLogger;

    public EmbeddedSharedFoldersImpl(EmbeddedDependencyResolver dependencyResolver,
                                     EmbeddedDependencyUpdatePolicy updatePolicyService,
                                     EmbeddedLogger loggerService) {

        mMavenResolver = dependencyResolver;
        mUpdatePolicy = updatePolicyService;
        mLogger = loggerService;

        if (recreateFolderIfNeeded(mSharedFolder)) {
            try {
                Zip.unzip(mMavenResolver.findFile("org.codehaus.fabric3:runtime-standalone:" + EmbeddedVersion.FABRIC3 + ":bin@zip"), mSharedFolder);
                FileSystem.delete(mSharedFolder, "bin", "legal", "runtimes");
            } catch (IOException e) {
                throw new EmbeddedFabric3SetupException("Cannot unzip standalone runtime");
            }
        }

        mBootFolder = FileSystem.folder(mSharedFolder, "boot");
        mExtensionsFolder = FileSystem.folder(mSharedFolder, "extensions");
        mHostFolder = FileSystem.folder(mSharedFolder, "host");
        mLibFolder = FileSystem.folder(mSharedFolder, "lib");

        // Check if folders exists. If not delete shared folder and throw an exception
        try {
            FileSystem.checkExistenceAndContent(mBootFolder, mExtensionsFolder, mHostFolder, mLibFolder);
        } catch (EmbeddedFabric3SetupException e) {
            mLogger.log(String.format("Deleting %1$s folder.", mSharedFolder.getAbsolutePath()));
            FileSystem.delete(mSharedFolder);
            throw e;
        }
    }

    private boolean shouldUpdate(final File folder) {
        if (!folder.exists()) {
            return true;
        }

        switch (mUpdatePolicy.getUpdatePolicy()) {
            case ALWAYS:
                return true;
            case DAILY:
                if (DateTime.day(folder.lastModified()) != DateTime.day(System.currentTimeMillis())) {
                    return true;
                }
        }

        return false;
    }

    private boolean recreateFolderIfNeeded(final File folder) {
        if (shouldUpdate(folder)) {
            FileSystem.delete(folder);

            if (!folder.mkdirs()) {
                throw new EmbeddedFabric3SetupException(MessageFormat.format("Cannot create folder: {0}", folder.getAbsolutePath()));
            }

            return true;
        }

        return false;
    }

    public File getBootFolder() {
        return mBootFolder;
    }

    public File getExtensionsFolder() {
        return mExtensionsFolder;
    }

    public File getHostFolder() {
        return mHostFolder;
    }

    public File getLibFolder() {
        return mLibFolder;
    }

    public File getProfileFolder(EmbeddedProfile profile) {
        File profileFolder = FileSystem.folder(mSharedFolder, profile.getFolderName());

        if (recreateFolderIfNeeded(profileFolder)) {
            // for all profiles files
            for (String file : profile.getFiles()) {
                // find it in repository
                File temp = mMavenResolver.findFile(file);

                try {
                    if (temp.getName().endsWith("zip")) {
                        // if it's a zip just unpack it
                        Zip.unzipInSameFolder(mMavenResolver.findFile("org.codehaus.fabric3:profile-web:" + EmbeddedVersion.FABRIC3 + ":bin@zip"), profileFolder);
                        FileSystem.delete(profileFolder, "LICENSE.txt", "NOTICE.txt");
                    } else if (temp.getName().endsWith("jar")) {
                        // if it's a jar copy it
                        FileSystem.copy(mMavenResolver.findFile("org.codehaus.fabric3:fabric3-junit:" + EmbeddedVersion.FABRIC3 + "@jar"), new File(profileFolder, temp.getName()));
                    } else {
                        // no other archives supported
                        throw new EmbeddedFabric3StartupException(MessageFormat.format("Cannot proceed archive ''{0}''. Only JAR and ZIP archives are supported.", temp.getName()));
                    }
                } catch (IOException e) {
                    throw new EmbeddedFabric3StartupException(e);
                }

            }
        }

        return profileFolder;
    }

}
