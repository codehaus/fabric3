package org.fabric3.assembly.completition;

import org.fabric3.assembly.IAssemblyStep;
import org.fabric3.assembly.configuration.AssemblyConfig;

/**
 * @author Michal Capo
 */
public class AssemblyCompletion implements IAssemblyStep {

    private AssemblyConfig mConfig;

    public AssemblyCompletion(AssemblyConfig pConfig) {
        mConfig = pConfig;
    }

    @Override
    public void process() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
