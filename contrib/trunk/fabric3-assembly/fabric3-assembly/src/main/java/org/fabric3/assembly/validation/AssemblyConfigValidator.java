package org.fabric3.assembly.validation;

import org.fabric3.assembly.IAssemblyStep;
import org.fabric3.assembly.assembly.Assembly;
import org.fabric3.assembly.configuration.*;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.dependency.fabric.FabricProfiles;
import org.fabric3.assembly.exception.ServerAlreadyExistsException;
import org.fabric3.assembly.exception.ValidationException;
import org.fabric3.assembly.utils.ConfigUtils;
import org.fabric3.assembly.utils.LoggerUtils;

import java.text.MessageFormat;
import java.util.Map;

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
        LoggerUtils.log("validating configuration");

        if (null == mConfig.getVersion()) {
            throw new ValidationException("Configuration version is not defined. You need to specify it.");
        }

        //
        // validate composites
        //
        for (Composite composite : mConfig.getComposites()) {
            mCompositeValidator.validate(composite);
        }

        //
        // validate profiles
        //
        for (Profile profile : mConfig.getProfiles()) {
            mProfileValidator.validate(profile);
        }

        //
        // validate servers
        //
        for (Server server : mConfig.getServers()) {
            mServerValidator.validate(server, mProfileValidator);
        }
        for (Server server : mConfig.getServers()) {
            // check for same server name
            if (1 < ConfigUtils.getServersByName(mConfig, server.getServerName()).size()) {
                throw new ServerAlreadyExistsException(MessageFormat.format("You defined two servers with the same name: ''{0}''.", server.getServerName()));
            }

            ValidationHelper.validateProfileExistence(server, mConfig.getProfiles(), FabricProfiles.all());
        }

        //
        // validate runtimes
        //
        for (Runtime runtime : mConfig.getRuntimes()) {
            mRuntimeValidator.validate(runtime, mProfileValidator, mCompositeValidator);
        }
        for (Runtime runtime : mConfig.getRuntimes()) {
            String serverName = runtime.getServerName();

            Map<RuntimeMode, Integer> result = ConfigUtils.getRuntimeModesByServerName(mConfig, serverName);
            Integer countVM = null == result.get(RuntimeMode.VM) ? 0 : result.get(RuntimeMode.VM);
            Integer countController = null == result.get(RuntimeMode.CONTROLLER) ? 0 : result.get(RuntimeMode.CONTROLLER);
            Integer countParticipant = null == result.get(RuntimeMode.PARTICIPANT) ? 0 : result.get(RuntimeMode.PARTICIPANT);

            if (countVM > 1) {
                throw new ValidationException("Only one VM runtime is allowed per server. Your servers is: {0}", serverName);
            }
            if (countController > 1) {
                throw new ValidationException("Only one CONTROLLER runtime is allowed per server. Your servers is: {0}", serverName);
            }

            if (countVM != 0 && (countController != 0 || countParticipant != 0)) {
                throw new ValidationException("You are trying to add VM runtime to ''{0}'' server which already has some other runtimes. This won''t work.", serverName);
            }

            if (countVM == 0 && countController == 0 && countParticipant == 0) {
                throw new ValidationException("Your server ''{0}'' doesn't contain any runtime.", serverName);
            }

            ValidationHelper.validateSameRuntimeName(serverName, ConfigUtils.getRuntimesByServerName(mConfig, serverName));

            // check if specified composites are available/exists
            ValidationHelper.validateCompositeExistence(runtime, mConfig.getComposites());
            // check if specified profiles are available/exists
            ValidationHelper.validateProfileExistence(runtime, mConfig.getProfiles(), FabricProfiles.all());
        }

        //
        // do assembly
        //
        new Assembly(mConfig).process();
    }

}
