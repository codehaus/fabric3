package org.fabric3.assembly.assembly;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Composite;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.maven.DependencyResolver;
import org.fabric3.assembly.utils.ConfigUtils;
import org.fabric3.assembly.utils.DependencyUtils;
import org.fabric3.assembly.utils.FileUtils;
import org.fabric3.assembly.utils.LoggerUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Michal Capo
 */
public class AssemblyComposite {

    private AssemblyConfig mConfig;

    protected DependencyResolver mDependencyResolver = new DependencyResolver();

    public AssemblyComposite(AssemblyConfig pConfig) {
        mConfig = pConfig;
    }

    public void doAssembly(Composite pComposite) {
        Server server = null;
        try {
            server = ConfigUtils.findServerByComposite(mConfig, pComposite);
        } catch (AssemblyException e) {
            // no-op, composite is just not deployed
        }

        if (null == server) {
            LoggerUtils.logWarn("Composite ''{0}'' not bound to any server. Will not be deployed.", pComposite.getName());
            return;
        }

        if (null != pComposite.getPath()) {
            try {
                FileUtils.copy(pComposite.getPath(), ConfigUtils.computeDeployPath(mConfig, pComposite));
            } catch (IOException e) {
                LoggerUtils.log(e, "Cannot deploy ''{0}'' composite.", pComposite.getName());
                throw new AssemblyException("Cannot deploy composite.", e);
            }
        }

        if (null != pComposite.getDependency()) {
            try {
                File file = mDependencyResolver.findFile(DependencyUtils.parseDependency(pComposite.getDependency()));
                FileUtils.copy(file, ConfigUtils.computeDeployPath(mConfig, pComposite, file));
            } catch (IOException e) {
                LoggerUtils.log(e, "Cannot deploy ''{0}'' composite.", pComposite.getDependency());
                throw new AssemblyException("Cannot deploy composite.", e);
            }
        }
    }

}
