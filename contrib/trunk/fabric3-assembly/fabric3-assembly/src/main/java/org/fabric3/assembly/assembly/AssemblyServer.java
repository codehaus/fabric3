package org.fabric3.assembly.assembly;

import org.fabric3.assembly.configuration.ConfigurationHelper;
import org.fabric3.assembly.configuration.ServerConfiguration;
import org.fabric3.assembly.dependency.UpdatePolicy;
import org.fabric3.assembly.dependency.profile.fabric.FabricDependencyFactory;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.exception.ValidationException;
import org.fabric3.assembly.maven.DependencyResolver;
import org.fabric3.assembly.utils.FileUtils;
import org.fabric3.assembly.utils.FileUtils2;
import org.fabric3.assembly.utils.LoggerUtils;
import org.fabric3.assembly.utils.StringUtils;
import org.fabric3.assembly.utils.ZipUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Michal Capo
 */
public class AssemblyServer extends AssemblyProfiles {

    private DependencyResolver dependencyResolver = new DependencyResolver();

    public void doAssembly(ServerConfiguration pServerConfiguration, UpdatePolicy pPolicy, ConfigurationHelper pConfigurationHelper) {
        validate(pServerConfiguration);

        File serverPath = pServerConfiguration.getServerPath();
        LoggerUtils.log("server in folder - " + serverPath);

        if (FileUtils2.recreateFolderIfNeeded(serverPath, pPolicy)) {
            try {
                ZipUtils.unzip(dependencyResolver.findFile(pConfigurationHelper.appendVersion(FabricDependencyFactory.zip("runtime-standalone"))), serverPath);
                FileUtils.delete(serverPath, "runtimes");

                FileUtils.checkExistenceAndContent(FileUtils.folders(serverPath, "boot", "extensions", "host", "lib"));

                // create runtimes folder
                FileUtils.createFolder(FileUtils.folder(serverPath, "runtimes"));

                processProfiles(pServerConfiguration.getProfiles(), FileUtils.folder(serverPath, "extensions"), pConfigurationHelper.computeMissingVersion(pServerConfiguration));
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

    private void validate(ServerConfiguration serverConfiguration) {
        if (StringUtils.isBlank(serverConfiguration.getServerName())) {
            throw new ValidationException("Server's name cannot be null.");
        }

        if (null == serverConfiguration.getServerPath()) {
            throw new ValidationException("Server's build path cannot be null.");
        }
    }

}
