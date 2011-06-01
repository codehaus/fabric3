package org.fabric3.assembly.assembly;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.utils.ConfigUtils;
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
        Server server = null;
        try {
            server = ConfigUtils.findServerByArchive(mConfig, pArchive);
        } catch (AssemblyException e) {
            // no-op, composite is just not deployed
        }

        if (null == server) {
            LoggerUtils.logWarn("Composite ''{0}'' not bound to any server. Will not be deployed.", pArchive.getName());
            return;
        }

        pArchive.as(ZipExporter.class).exportTo(ConfigUtils.computeDeployPath(mConfig, pArchive), true);
    }

}
