package org.fabric3.assembly.configuration;

import org.fabric3.assembly.exception.ValidationException;
import org.fabric3.assembly.utils.StringUtils;

import java.text.MessageFormat;

/**
 * @author Michal Capo
 */
public class RuntimeValidator {

    public static void validate(Runtime pRuntime) {
        String runtimeName = pRuntime.getRuntimeName();

        if (StringUtils.isBlank(runtimeName)) {
            throw new ValidationException("Runtime name cannot be null.");
        }

        if (StringUtils.isBlank(pRuntime.getServerName())) {
            throw new ValidationException(MessageFormat.format("Server associated with runtime {0} is null.", runtimeName));
        }

        if (null == pRuntime.getRuntimeMode()) {
            throw new ValidationException("Runtime mode of " + runtimeName + " is null.");
        }
    }

}
