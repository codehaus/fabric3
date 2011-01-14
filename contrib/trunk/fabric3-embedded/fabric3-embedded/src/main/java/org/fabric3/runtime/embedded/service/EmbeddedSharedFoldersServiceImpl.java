package org.fabric3.runtime.embedded.service;

import org.fabric3.host.runtime.ScanException;
import org.fabric3.runtime.embedded.api.EmbeddedUpdatePolicy;
import org.fabric3.runtime.embedded.api.EmbeddedVersion;
import org.fabric3.runtime.embedded.api.service.EmbeddedLoggerService;
import org.fabric3.runtime.embedded.api.service.EmbeddedSharedFoldersService;
import org.fabric3.runtime.embedded.api.service.EmbeddedUpdatePolicyService;
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
public class EmbeddedSharedFoldersServiceImpl implements EmbeddedSharedFoldersService {

    private static File mSharedFolder = folder(temporaryFolder() + File.separator + "fabric3_sharedLibs-" + EmbeddedVersion.FABRIC3);

    private static File mBootFolder;
    private static File mExtensionsFolder;
    private static File mHostFolder;
    private static File mLibFolder;
    private static File mJmsFolder;
    private static File mJpaFolder;
    private static File mNetFolder;
    private static File mTimerFolder;
    private static File mWebFolder;
    private static File mJUnitFolder;

    private MavenDependencyResolver mMavenResolver;
    private EmbeddedUpdatePolicyService mUpdatePolicyService;
    private EmbeddedLoggerService mLoggerService;

    public EmbeddedSharedFoldersServiceImpl(MavenDependencyResolver dependencyResolver, EmbeddedUpdatePolicyService updatePolicyService, EmbeddedLoggerService loggerService) throws ScanException, IOException {
        mMavenResolver = dependencyResolver;
        mUpdatePolicyService = updatePolicyService;
        mLoggerService = loggerService;

        if (null == mMavenResolver) {
            throw new EmbeddedFabric3SetupException("Dependency resolver cannot be null.");
        }
        if (null == mUpdatePolicyService) {
            throw new EmbeddedFabric3SetupException("Update policy service cannot be null.");
        }
        if (null == mLoggerService) {
            throw new EmbeddedFabric3SetupException("Logger service cannot be null.");
        }
    }

    public void initialize() throws IOException, EmbeddedFabric3StartupException {
        mBootFolder = FileSystem.folder(mSharedFolder, "boot");
        mExtensionsFolder = FileSystem.folder(mSharedFolder, "extensions");
        mHostFolder = FileSystem.folder(mSharedFolder, "host");
        mLibFolder = FileSystem.folder(mSharedFolder, "lib");
        mJmsFolder = FileSystem.folder(mSharedFolder, "profile-jms");
        mJpaFolder = FileSystem.folder(mSharedFolder, "profile-jpa");
        mNetFolder = FileSystem.folder(mSharedFolder, "profile-net");
        mTimerFolder = FileSystem.folder(mSharedFolder, "profile-timer");
        mWebFolder = FileSystem.folder(mSharedFolder, "profile-web");
        mJUnitFolder = FileSystem.folder(mSharedFolder, "profile-junit");

        boolean update = false;

        if (!mSharedFolder.exists() || !mBootFolder.exists() || !mExtensionsFolder.exists() || !mHostFolder.exists() || !mLibFolder.exists() ||
                EmbeddedUpdatePolicy.ALWAYS == mUpdatePolicyService.getUpdatePolicy() ||
                (EmbeddedUpdatePolicy.DAILY == mUpdatePolicyService.getUpdatePolicy() && DateTime.day(mSharedFolder.lastModified()) != DateTime.day(System.currentTimeMillis()))
                ) {
            update = true;
        }

        if (update) {
            FileSystem.delete(mSharedFolder);

            if (!mSharedFolder.mkdirs()) {
                throw new EmbeddedFabric3SetupException(MessageFormat.format("Cannot create shared folder: {0}", mSharedFolder.getAbsolutePath()));
            }

            FileSystem.createFolders(mBootFolder, mExtensionsFolder, mHostFolder, mLibFolder, mJmsFolder, mJpaFolder, mNetFolder, mTimerFolder, mWebFolder, mJUnitFolder);

            Zip.unzip(mMavenResolver.findFile("org.codehaus.fabric3:runtime-standalone:" + EmbeddedVersion.FABRIC3 + ":bin@zip"), mSharedFolder);
            FileSystem.delete(mSharedFolder, "bin", "legal", "runtimes");

            Zip.unzipInSameFolder(mMavenResolver.findFile("org.codehaus.fabric3:profile-jms:" + EmbeddedVersion.FABRIC3 + ":bin@zip"), mJmsFolder);
            FileSystem.delete(mJmsFolder, "LICENSE.txt", "NOTICE.txt");

            Zip.unzipInSameFolder(mMavenResolver.findFile("org.codehaus.fabric3:profile-jpa:" + EmbeddedVersion.FABRIC3 + ":bin@zip"), mJpaFolder);
            FileSystem.delete(mJpaFolder, "LICENSE.txt", "NOTICE.txt");

            Zip.unzipInSameFolder(mMavenResolver.findFile("org.codehaus.fabric3:profile-net:" + EmbeddedVersion.FABRIC3 + ":bin@zip"), mNetFolder);
            FileSystem.delete(mNetFolder, "LICENSE.txt", "NOTICE.txt");

            Zip.unzipInSameFolder(mMavenResolver.findFile("org.codehaus.fabric3:profile-timer:" + EmbeddedVersion.FABRIC3 + ":bin@zip"), mTimerFolder);
            FileSystem.delete(mTimerFolder, "LICENSE.txt", "NOTICE.txt");

            Zip.unzipInSameFolder(mMavenResolver.findFile("org.codehaus.fabric3:profile-web:" + EmbeddedVersion.FABRIC3 + ":bin@zip"), mWebFolder);
            FileSystem.delete(mWebFolder, "LICENSE.txt", "NOTICE.txt");

            FileSystem.copy(mMavenResolver.findFile("org.codehaus.fabric3:fabric3-junit:" + EmbeddedVersion.FABRIC3 + "@jar"), new File(mJUnitFolder, "fabric3-junit.jar"));
            FileSystem.copy(mMavenResolver.findFile("org.codehaus.fabric3:fabric3-test-spi:" + EmbeddedVersion.FABRIC3 + "@jar"), new File(mJUnitFolder, "fabric3-test-spi.jar"));
            FileSystem.copy(mMavenResolver.findFile("org.codehaus.fabric3:fabric3-ant-api:" + EmbeddedVersion.FABRIC3 + "@jar"), new File(mJUnitFolder, "fabric3-ant-api.jar"));
            FileSystem.copy(mMavenResolver.findFile("org.codehaus.fabric3:fabric3-ant-extension:" + EmbeddedVersion.FABRIC3 + "@jar"), new File(mJUnitFolder, "fabric3-ant-extension.jar"));
        }

        // check if needed folders exists
        try {
            FileSystem.checkExistenceAndContent(mBootFolder, mExtensionsFolder, mHostFolder, mLibFolder, mJmsFolder, mJpaFolder, mNetFolder, mTimerFolder, mWebFolder);
        } catch (EmbeddedFabric3SetupException e) {
            mLoggerService.log(String.format("Deleting %1$s folder.", mSharedFolder.getAbsolutePath()));
            FileSystem.delete(mSharedFolder);
            throw e;
        }
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

    public File getProfileJmsFolder() {
        return mJmsFolder;
    }

    public File getProfileJpaFolder() {
        return mJpaFolder;
    }

    public File getProfileNetFolder() {
        return mNetFolder;
    }

    public File getProfileTimerFolder() {
        return mTimerFolder;
    }

    public File getProfileWebFolder() {
        return mWebFolder;
    }

    public File getJUnitFolder() {
        return mJUnitFolder;
    }
}
