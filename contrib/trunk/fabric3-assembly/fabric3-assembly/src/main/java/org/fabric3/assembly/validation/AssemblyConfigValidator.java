package org.fabric3.assembly.validation;

import org.fabric3.assembly.IAssemblyStep;
import org.fabric3.assembly.assembly.Assembly;
import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.configuration.Composite;
import org.fabric3.assembly.configuration.Profile;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.configuration.Server;
import org.fabric3.assembly.exception.ValidationException;

/**
 * @author Michal Capo
 */
public class AssemblyConfigValidator implements IAssemblyStep {

    private ServerValidator mServerValidator = new ServerValidator();

    private RuntimeValidator mRuntimeValidator = new RuntimeValidator();

    private ProfileValidator mProfileValidator = new ProfileValidator();

    private CompositeValidator mCompositeValidator = new CompositeValidator();

    private AssemblyConfig mConfig;

    public AssemblyConfigValidator(AssemblyConfig pConfig) {
        mConfig = pConfig;
    }

    public void process() {
        if (null == mConfig.getVersion()) {
            throw new ValidationException("Configuration version is not defined. You need to specify it.");
        }

        for (Server server : mConfig.getServers()) {
            mServerValidator.validate(server);
        }

        for (Runtime runtime : mConfig.getRuntimes()) {
            mRuntimeValidator.validate(runtime);
        }

        for (Composite composite : mConfig.getComposites()) {
            mCompositeValidator.validate(composite);
        }

        for (Profile profile : mConfig.getProfiles()) {
            mProfileValidator.validate(profile);
        }

        // do assembly
        new Assembly(mConfig).process();
    }

}
