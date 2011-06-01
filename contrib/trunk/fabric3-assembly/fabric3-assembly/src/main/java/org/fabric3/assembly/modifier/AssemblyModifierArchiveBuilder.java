package org.fabric3.assembly.modifier;

import org.fabric3.assembly.assembly.AssemblyArchive;
import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.utils.ConfigUtils;
import org.jboss.shrinkwrap.api.Archive;

import java.io.File;

/**
 * @author Michal Capo
 */
public class AssemblyModifierArchiveBuilder extends AssemblyModifier {

    private Archive mArchive;

    private AssemblyArchive mAssemblyArchive;

    public AssemblyModifierArchiveBuilder(AssemblyConfig pConfig, String pName) {
        super(pConfig);
        mArchive = ConfigUtils.findArchiveByName(mConfig, pName);
        mAssemblyArchive = new AssemblyArchive(mConfig);
    }

    public AssemblyModifierArchiveBuilder deployToServer(String pServerName) {
        Server server = ConfigUtils.findServerByName(mConfig, pServerName);
        if (!server.getArchiveNames().contains(mArchive.getName())) {
            server.addArchive(mArchive.getName());
        }

        mAssemblyArchive.doAssembly(mArchive);

        return this;
    }

    public AssemblyModifierArchiveBuilder undeploy() {
        File fileLocation = ConfigUtils.computeDeployPath(mConfig, mArchive);
        if (fileLocation.exists()) {
            if (!fileLocation.delete()) {
                throw new AssemblyException("Cannot removeFromServer archive: {0}", fileLocation.getAbsolutePath());
            }
        }

        return this;
    }

    public AssemblyModifierArchiveBuilder redeploy() {
        try {
            undeploy();
            deployToServer(ConfigUtils.findServerByArchive(mConfig, mArchive).getServerName());
        } catch (AssemblyException e) {
            throw new AssemblyException("Archive ''{0}'' is not deployed. You cannot redeploy it.", mArchive.getName());
        }

        return this;
    }

}
