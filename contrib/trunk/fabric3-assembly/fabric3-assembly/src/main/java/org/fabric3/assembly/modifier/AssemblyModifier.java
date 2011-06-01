package org.fabric3.assembly.modifier;

import org.fabric3.assembly.configuration.AssemblyConfig;

/**
 * @author Michal Capo
 */
public class AssemblyModifier {

    protected AssemblyConfig mConfig;

    public AssemblyModifier(AssemblyConfig pConfig) {
        mConfig = pConfig;
    }

    public AssemblyModifierArchiveBuilder archive(String pName) {
        return new AssemblyModifierArchiveBuilder(mConfig, pName);
    }

    public AssemblyModifierCompositeBuilder composite(String pName) {
        return new AssemblyModifierCompositeBuilder(mConfig, pName);
    }

}
