package org.fabric3.assembly.assembly;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Composite;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.utils.ConfigUtils;
import org.fabric3.assembly.utils.FileUtils;
import org.fabric3.assembly.utils.LoggerUtils;

import java.io.IOException;

/**
 * @author Michal Capo
 */
public class AssemblyComposite {

    private AssemblyConfig mConfig;

    public AssemblyComposite(AssemblyConfig pConfig) {
        mConfig = pConfig;
    }

    public void doAssembly(Composite pComposite) {
        Server server = ConfigUtils.getServerByComposite(mConfig, pComposite);

        if (null == server) {
            LoggerUtils.logWarn("Composite ''{0}'' not bound to any server. Will not be deployed.", pComposite.getName());
            return;
        }

        if (null != pComposite.getPath()) {
            try {
                org.fabric3.assembly.configuration.Runtime runtime = ConfigUtils.findRuntimeForCompositeDeployOnServer(mConfig, server);
                FileUtils.copy(pComposite.getPath(), FileUtils.file(runtime.getDeployFolder(), pComposite.getPath().getName()));
            } catch (IOException e) {
                LoggerUtils.log(e, "Cannot deploy ''{0}'' composite.", pComposite.getName());
                throw new AssemblyException("Cannot deploy composite.", e);
            }
        }

        //TODO <capo> add assembling composite with dependency
    }

}
