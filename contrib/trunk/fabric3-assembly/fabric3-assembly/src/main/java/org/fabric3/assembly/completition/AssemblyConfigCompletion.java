package org.fabric3.assembly.completition;

import org.fabric3.assembly.IAssemblyStep;
import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Composite;
import org.fabric3.assembly.configuration.Profile;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.validation.AssemblyConfigValidator;

/**
 * @author Michal Capo
 */
public class AssemblyConfigCompletion implements IAssemblyStep {

    private AssemblyConfig mConfig;

    private CompletionHelper mHelper;

    public AssemblyConfigCompletion(AssemblyConfig pConfig) {
        mConfig = pConfig;
        mHelper = new CompletionHelper(mConfig);
    }

    @Override
    public void process() {

        //
        // composites - set missing update policy
        //
        for (Composite composite : mConfig.getCompositeConfig().getComposites()) {
            if (null == composite.getUpdatePolicy()) {
                composite.setUpdatePolicy(mHelper.computeMissingUpdatePolicy(composite));
            }
        }

        //
        // profiles - set missing version and update policy
        //
        for (Profile profile : mConfig.getProfileConfig().getProfiles()) {
            if (null == profile.getVersion()) {
                profile.setVersion(mHelper.computeMissingVersion(profile));
            }

            if (null == profile.getUpdatePolicy()) {
                profile.setUpdatePolicy(mHelper.computeMissingUpdatePolicy(profile));
            }
        }

        //
        // servers - set missing update policy and version
        //
        for (Server server : mConfig.getServers()) {
            if (null == server.getUpdatePolicy()) {
                server.setUpdatePolicy(mHelper.computeMissingUpdatePolicy(server));
            }

            if (null == server.getVersion()) {
                server.setVersion(mHelper.computeMissingVersion(server));
            }

            for (String s : server.getProfileNames()) {
                server.addProfile(mHelper.findProfileByName(s));
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
                runtime.setUpdatePolicy(mHelper.computeMissingUpdatePolicy(runtime));
            }
            if (null == runtime.getServerPath()) {
                runtime.setServerPath(mHelper.computeServerPath(runtime));
            }

            for (String s : runtime.getProfileNames()) {
                runtime.addProfile(mHelper.findProfileByName(s));
            }

            Server server = mHelper.getServerByRuntime(runtime);
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
