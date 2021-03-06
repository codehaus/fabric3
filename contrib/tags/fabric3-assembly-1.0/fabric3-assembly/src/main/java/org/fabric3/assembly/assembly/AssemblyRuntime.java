package org.fabric3.assembly.assembly;

import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.utils.FileUtils;
import org.fabric3.assembly.utils.UpdatePolicyUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.text.MessageFormat;

/**
 * @author Michal Capo
 */
public class AssemblyRuntime extends AbstractAssemblyProfiles {

    public void doAssembly(Runtime pConfiguration) {
        // calculate runtime folder
        File runtimeFolder = FileUtils.folder(pConfiguration.getServerPath(), "runtimes/" + pConfiguration.getRuntimeName());

        // setup config path
        String configPath;
        if (null == pConfiguration.getSystemConfig()) {
            switch (pConfiguration.getRuntimeMode()) {
                case VM:
                    configPath = "/config/defaultSystemConfigVm.xml";
                    break;
                case CONTROLLER:
                    configPath = "/config/defaultSystemConfigController.xml";
                    break;
                case PARTICIPANT:
                    configPath = "/config/defaultSystemConfigParticipant.xml";
                    break;
                default:
                    throw new AssemblyException("Unknown runtime type: " + pConfiguration.getRuntimeMode());
            }

            try {
                pConfiguration.setSystemConfig(new File(FileUtils.fileAtClassPath(configPath).toURI()));
            } catch (URISyntaxException e) {
                throw new AssemblyException(MessageFormat.format("Cannot find runtime configuration file: {0}", configPath));
            } catch (MalformedURLException e) {
                throw new AssemblyException(MessageFormat.format("Cannot find runtime configuration file: {0}", configPath));
            }
        } else {
            if (!pConfiguration.getSystemConfig().exists()) {
                throw new AssemblyException(MessageFormat.format("Given ''{0}'' config path doesn''t exists.", pConfiguration.getSystemConfig().getAbsolutePath()));
            }
        }

        if (UpdatePolicyUtils.shouldUpdate(runtimeFolder, pConfiguration.getUpdatePolicy())) {
            runtimeFolder.delete();
            runtimeFolder.mkdirs();

            // Create runtime folders (repository, deploy, tmp, data) and copy configuration file, if they doesn't exists.
            FileUtils.createFolders(FileUtils.folders(runtimeFolder, "repository/user", "repository/runtime", "deploy", "tmp", "data", "config"));
            File extensionFolder = FileUtils.folder(runtimeFolder, "repository/runtime");

            // copy system config file
            try {
                FileUtils.copy(pConfiguration.getSystemConfig(), FileUtils.file(FileUtils.folder(runtimeFolder, "config"), "systemConfig.xml"));
            } catch (IOException e) {
                throw new AssemblyException(MessageFormat.format("Cannot find {0} file", pConfiguration.getSystemConfig()));
            }

            // process profile files
            processProfiles(pConfiguration.getProfiles(), extensionFolder);
        }
    }


}
