package org.fabric3.assembly.validation;

import org.fabric3.assembly.configuration.Composite;
import org.fabric3.assembly.exception.AssemblyException;
import org.fabric3.assembly.utils.StringUtils;

/**
 * @author Michal Capo
 */
public class CompositeValidator {

    public void validate(Composite pConfiguration) {
        if (null == pConfiguration) {
            throw new AssemblyException("Cannot validate 'null' composite configuration");
        }

        if (StringUtils.isBlank(pConfiguration.getName())) {
            throw new AssemblyException("Name not found for configuration: {0}", pConfiguration);
        }

        if (null != pConfiguration.getUpdatePolicy()) {
            throw new AssemblyException("Update policy on configuration {0} is null.", pConfiguration.getName());
        }

        if (StringUtils.isBlank(pConfiguration.getDependency()) && (null == pConfiguration.getPath() && !pConfiguration.getPath().exists())) {
            throw new AssemblyException("You need to specify 'path' or 'dependency' for ''{0}'' configuration.", pConfiguration.getName());
        }
    }
}
