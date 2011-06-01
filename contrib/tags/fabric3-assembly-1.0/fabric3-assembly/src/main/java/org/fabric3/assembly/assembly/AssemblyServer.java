package org.fabric3.assembly.assembly;

import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.dependency.Dependency;
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

    public void doAssembly(Server pConfiguration) {
        File serverPath = pConfiguration.getServerPath();
        LoggerUtils.log("Assembling server ''{0}'' in folder ''{1}'' ", pConfiguration.getServerName(), serverPath);

        if (FileUtils2.recreateFolderIfNeeded(serverPath, pConfiguration.getUpdatePolicy())) {
            try {
                Dependency zip = FabricDependencyFactory.zip("runtime-standalone");
                zip.setVersion(pConfiguration.getVersion());

                ZipUtils.unzip(mDependencyResolver.findFile(zip), serverPath);
                FileUtils.delete(serverPath, "runtimes");

                FileUtils.checkExistenceAndContent(FileUtils.folders(serverPath, "boot", "extensions", "host", "lib"));

                // create runtimes folder
                FileUtils.createFolder(FileUtils.folder(serverPath, "runtimes"));

                processProfiles(pConfiguration.getProfiles(), FileUtils.folder(serverPath, "extensions"));
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
