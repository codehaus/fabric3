package org.fabric3.assembly.validation;

import org.fabric3.assembly.configuration.Profile;
import org.fabric3.assembly.exception.ValidationException;
import org.fabric3.assembly.utils.StringUtils;

import java.text.MessageFormat;

/**
 * @author Michal Capo
 */
public class RuntimeValidator {

    public void validate(org.fabric3.assembly.configuration.Runtime pRuntime, ProfileValidator pProfileValidator) {
        String runtimeName = pRuntime.getRuntimeName();

        if (StringUtils.isBlank(runtimeName)) {
            throw new ValidationException("Runtime name cannot be null.");
        }

        if (StringUtils.isBlank(pRuntime.getServerName())) {
            throw new ValidationException(MessageFormat.format("Server associated with runtime ''{0}'' is null.", runtimeName));
        }

        if (null == pRuntime.getServerPath()) {
            throw new ValidationException("Servers path is null or doesn''t exists, but your runtime ''{0}'' is asociated with ''{1}'' server.", pRuntime.getRuntimeName(), pRuntime.getServerName());
        }

        if (null == pRuntime.getRuntimeMode()) {
            throw new ValidationException("Runtime mode of ''{0}'' runtime is null.", runtimeName);
        }

        if (null == pRuntime.getUpdatePolicy()) {
            throw new ValidationException("Update policy of ''{0}'' runtime is null.", runtimeName);
        }

        for (Profile profile : pRuntime.getProfiles()) {
            pProfileValidator.validate(profile);
        }
        ValidationHelper.validateSameProfileName(runtimeName, pRuntime.getProfiles());


    }

}
