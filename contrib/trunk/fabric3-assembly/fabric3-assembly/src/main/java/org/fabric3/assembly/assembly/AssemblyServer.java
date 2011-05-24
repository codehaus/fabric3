package org.fabric3.assembly.assembly;

import org.fabric3.assembly.completition.CompletitionHelper;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.dependency.fabric.FabricDependencyFactory;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.utils.FileUtils;
import org.fabric3.assembly.utils.FileUtils2;
import org.fabric3.assembly.utils.LoggerUtils;
import org.fabric3.assembly.utils.ZipUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Michal Capo
 */
public class AssemblyServer extends AbstractAssemblyProfiles {

    public void doAssembly(Server pConfiguration, CompletitionHelper pCompletitionHelper) {
        File serverPath = pConfiguration.getServerPath();
        LoggerUtils.log("server in folder - " + serverPath);

        if (FileUtils2.recreateFolderIfNeeded(serverPath, pCompletitionHelper.computeUpdatePolicy(pConfiguration))) {
            try {
                ZipUtils.unzip(mDependencyResolver.findFile(pCompletitionHelper.appendVersion(FabricDependencyFactory.zip("runtime-standalone"), pConfiguration)), serverPath);
                FileUtils.delete(serverPath, "runtimes");

                FileUtils.checkExistenceAndContent(FileUtils.folders(serverPath, "boot", "extensions", "host", "lib"));

                // create runtimes folder
                FileUtils.createFolder(FileUtils.folder(serverPath, "runtimes"));

                processProfiles(pConfiguration.getProfiles(), FileUtils.folder(serverPath, "extensions"), pCompletitionHelper.computeMissingVersion(pConfiguration));
            } catch (IOException e) {
                throw new AssemblyException("Cannot assembly standalone runtime.", e);
            }
        } else {
            // cleanup existing directory structure
            LoggerUtils.log("server cleaning up - " + serverPath.getAbsolutePath());

            for (File file : FileUtils.filesIn(FileUtils.folder(serverPath, "runtimes"))) {
                FileUtils.delete(FileUtils.filesIn(FileUtils.folder(file, "tmp")));
                FileUtils.delete(FileUtils.filesIn(FileUtils.folder(file, "data")));
                FileUtils.delete(FileUtils.filesIn(FileUtils.folder(file, "deploy")));
                FileUtils.delete(FileUtils.filesIn(FileUtils.folder(file, "repository/user")));
            }
        }
    }

}
