package org.fabric3.assembly.assembly;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.utils.ConfigUtils;
import org.fabric3.assembly.utils.FileUtils;
import org.fabric3.assembly.utils.LoggerUtils;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;

/**
 * @author Michal Capo
 */
public class AssemblyArchive {

    private AssemblyConfig mConfig;

    public AssemblyArchive(AssemblyConfig pConfig) {
        mConfig = pConfig;
    }

    public void doAssembly(Archive pArchive) {
        Server server = ConfigUtils.getServerByArchive(mConfig, pArchive);

        if (null == server) {
            LoggerUtils.logWarn("Composite ''{0}'' not bound to any server. Will not be deployed.", pArchive.getName());
            return;
        }

        org.fabric3.assembly.configuration.Runtime runtime = ConfigUtils.findRuntimeForCompositeDeployOnServer(mConfig, server);
        pArchive.as(ZipExporter.class).exportTo(FileUtils.file(runtime.getDeployFolder(), pArchive.getName()), true);
    }

}
