package org.fabric3.assembly.modifier;

import org.fabric3.assembly.configuration.AssemblyConfig;

/**
 * @author Michal Capo
 */
public class AssemblyModifier {

    protected AssemblyConfig mConfig;

    public AssemblyModifier(AssemblyConfig pConfig) {
        mConfig = pConfig;

        mConfig.setIsModifyProcess(true);

        // run assembly if not processed
        if (!pConfig.isAlreadyProcessed()) {
            pConfig.process();
        }
    }

    public AssemblyModifierArchiveBuilder archive(String pName) {
        return new AssemblyModifierArchiveBuilder(mConfig, pName);
    }

    public AssemblyModifierCompositeBuilder composite(String pName) {
        return new AssemblyModifierCompositeBuilder(mConfig, pName);
    }

}
