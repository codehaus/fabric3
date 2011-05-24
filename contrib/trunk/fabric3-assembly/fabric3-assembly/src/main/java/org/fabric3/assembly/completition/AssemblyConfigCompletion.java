package org.fabric3.assembly.completition;

import org.fabric3.assembly.IAssemblyStep;
import org.fabric3.assembly.configuration.AssemblyConfig;
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
        new AssemblyConfigValidator(mConfig).process();
    }
}
