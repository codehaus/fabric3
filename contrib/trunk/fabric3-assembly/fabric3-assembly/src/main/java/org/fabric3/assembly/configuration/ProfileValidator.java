package org.fabric3.assembly.configuration;

import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.utils.StringUtils;

/**
 * @author Michal Capo
 */
public class ProfileValidator {

    public static void validate(Profile pConfiguration) {
        if (null == pConfiguration) {
            throw new AssemblyException("Profile configuration is 'null' and cannot perform validation on.");
        }

        String configurationName = pConfiguration.getName();

        if (StringUtils.isBlank(configurationName)) {
            throw new AssemblyException("Name of profile configuration cannot be null: {0}", pConfiguration);
        }

        if (null == pConfiguration.getUpdatePolicy()) {
            throw new AssemblyException("Update policy not defined for {0}", configurationName);
        }

        if (null == pConfiguration.getVersion()) {
            throw new AssemblyException("Version not defined for {0}", configurationName);
        }

        if (pConfiguration.getDependencies().isEmpty() && pConfiguration.getFiles().isEmpty()) {
            throw new AssemblyException("You need to define at lest one 'file' or 'path' for ''{0}'' profile.", configurationName);
        }
    }

}
