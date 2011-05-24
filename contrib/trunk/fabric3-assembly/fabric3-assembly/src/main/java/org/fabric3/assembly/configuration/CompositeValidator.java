package org.fabric3.assembly.configuration;

import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.utils.StringUtils;

/**
 * @author Michal Capo
 */
public class CompositeValidator {

    public static void validate(Composite pConfiguration) {
        if (null == pConfiguration) {
            throw new AssemblyException("Cannot validate 'null' composite configuration");
        }

        if (StringUtils.isBlank(pConfiguration.getName())) {
            throw new AssemblyException("Name not found for configuration: {0}", pConfiguration);
        }

        if (StringUtils.isBlank(pConfiguration.getRuntimeName())) {
            throw new AssemblyException("Configuration {0} isn't bound to any runtime.", pConfiguration.getName());
        }

        if (null != pConfiguration.getUpdatePolicy()) {
            throw new AssemblyException("Update policy on configuration {0} is null.", pConfiguration.getName());
        }

        if (StringUtils.isBlank(pConfiguration.getDependency()) && (null == pConfiguration.getPath() && !pConfiguration.getPath().exists())) {
            throw new AssemblyException("You need to specify 'path' or 'dependency' for ''{0}'' configuration.", pConfiguration.getName());
        }
    }
}
