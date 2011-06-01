package org.fabric3.assembly.completition;

import org.fabric3.assembly.IAssemblyStep;
import org.fabric3.assembly.configuration.*;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.utils.ConfigUtils;
import org.fabric3.assembly.utils.LoggerUtils;
import org.fabric3.assembly.validation.AssemblyConfigValidator;

/**
 * @author Michal Capo
 */
public class AssemblyConfigCompletion implements IAssemblyStep {

    private AssemblyConfig mConfig;

    public AssemblyConfigCompletion(AssemblyConfig pConfig) {
        mConfig = pConfig;
    }

    @Override
    public void process() {
        LoggerUtils.log("computing missing configuration data");

        //
        // composites - set missing update policy
        //
        for (Composite composite : mConfig.getCompositeConfig().getComposites()) {
            if (null == composite.getUpdatePolicy()) {
                composite.setUpdatePolicy(ConfigUtils.computeMissingUpdatePolicy(mConfig, composite));
            }
        }

        //
        // profiles - set missing version and update policy
        //
        for (Profile profile : mConfig.getProfileConfig().getProfiles()) {
            if (null == profile.getVersion()) {
                profile.setVersion(ConfigUtils.computeMissingVersion(mConfig, profile));
            }

            if (null == profile.getUpdatePolicy()) {
                profile.setUpdatePolicy(ConfigUtils.computeMissingUpdatePolicy(mConfig, profile));
            }
        }

        //
        // servers - set missing update policy and version
        //
        for (Server server : mConfig.getServers()) {
            if (null == server.getUpdatePolicy()) {
                server.setUpdatePolicy(ConfigUtils.computeMissingUpdatePolicy(mConfig, server));
            }

            if (null == server.getVersion()) {
                server.setVersion(ConfigUtils.computeMissingVersion(mConfig, server));
            }

            // find existing profile by his name and add them into server
            for (String s : server.getProfileNames()) {
                server.addProfile(ConfigUtils.findProfileByName(mConfig, s));
            }

            // find existing composite by name and add them into runtime
            for (String s : server.getCompositeNames()) {
                server.addComposite(ConfigUtils.findCompositeByName(mConfig, s));
            }

            for (Profile profile : server.getProfiles()) {
                if (null == profile.getVersion()) {
                    profile.setVersion(server.getVersion());
                }
                if (null == profile.getUpdatePolicy()) {
                    profile.setUpdatePolicy(server.getUpdatePolicy());
                }
            }
        }

        //
        // runtimes - set missing update policy and server path
        //
        for (Runtime runtime : mConfig.getRuntimes()) {
            if (null == runtime.getUpdatePolicy()) {
                runtime.setUpdatePolicy(ConfigUtils.computeMissingUpdatePolicy(mConfig, runtime));
            }
            if (null == runtime.getServerPath()) {
                runtime.setServerPath(ConfigUtils.computeServerPath(mConfig, runtime));
            }

            // find existing profile by his name and add them into runtime
            for (String s : runtime.getProfileNames()) {
                runtime.addProfile(ConfigUtils.findProfileByName(mConfig, s));
            }

            Server server = ConfigUtils.getServerByRuntime(mConfig, runtime);
            for (Profile profile : runtime.getProfiles()) {
                if (null == profile.getVersion()) {
                    profile.setVersion(server.getVersion());
                }
                if (null == profile.getUpdatePolicy()) {
                    profile.setUpdatePolicy(runtime.getUpdatePolicy());
                }
            }
        }

        //
        // validate configuration
        //
        new AssemblyConfigValidator(mConfig).process();
    }

}
