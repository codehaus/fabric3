package org.fabric3.assembly.validation;

import org.fabric3.assembly.configuration.Profile;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.exception.ValidationException;
import org.fabric3.assembly.utils.StringUtils;

import java.io.File;

/**
 * @author Michal Capo
 */
public class ProfileValidator {

    public void validate(Profile pConfiguration) {
        if (null == pConfiguration) {
            throw new AssemblyException("Profile configuration is 'null' and cannot perform validation on.");
        }

        String configurationName = pConfiguration.getName();

        if (StringUtils.isBlank(configurationName)) {
            throw new AssemblyException("Name of profile configuration cannot be null: ''{0}''", pConfiguration);
        }

        if (null == pConfiguration.getUpdatePolicy()) {
            throw new AssemblyException("Update policy is not defined for ''{0}''", configurationName);
        }

        if (null == pConfiguration.getVersion()) {
            throw new AssemblyException("Version is not defined for ''{0}''", configurationName);
        }

        if (pConfiguration.getDependencies().isEmpty() && pConfiguration.getFiles().isEmpty()) {
            throw new AssemblyException("You need to define at lest one 'file' or 'path' for ''{0}'' profile.", configurationName);
        }

        for (File file : pConfiguration.getFiles()) {
            if (!file.exists()) {
                throw new ValidationException("File ''{0}'' doesn't exists. Your profile definition ''{1}'' is broken.", file.getAbsoluteFile(), configurationName);
            }
        }
    }

}
