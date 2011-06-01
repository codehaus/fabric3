package org.fabric3.assembly.assembly;

import org.fabric3.assembly.IAssemblyStep;
import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Composite;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.utils.LoggerUtils;
import org.jboss.shrinkwrap.api.Archive;

/**
 * @author Michal Capo
 */
public class Assembly implements IAssemblyStep {

    private AssemblyServer mServerAssembly = new AssemblyServer();

    private AssemblyRuntime mRuntimeAssembly = new AssemblyRuntime();

    private AssemblyComposite mCompositeAssembly;

    private AssemblyArchive mArchiveAssembly;

    private AssemblyConfig mConfig;

    public Assembly(AssemblyConfig pConfig) {
        mConfig = pConfig;
        mCompositeAssembly = new AssemblyComposite(pConfig);
        mArchiveAssembly = new AssemblyArchive(pConfig);
    }

    public void process() {
        for (Server server : mConfig.getServers()) {
            mServerAssembly.doAssembly(server);
        }

        for (Runtime runtime : mConfig.getRuntimes()) {
            mRuntimeAssembly.doAssembly(runtime);
        }

        for (Composite composite : mConfig.getComposites()) {
            mCompositeAssembly.doAssembly(composite);
        }

        for (Archive archive : mConfig.getArchives()) {
            mArchiveAssembly.doAssembly(archive);
        }

        LoggerUtils.log("Assembling done at ''{0,time}''", System.currentTimeMillis());
    }

}
