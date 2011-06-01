package org.fabric3.assembly.modifier;

import org.fabric3.assembly.configuration.AssemblyConfig;
import org.fabric3.assembly.factory.AssemblyConfigBuilder;

/**
 * @author Michal Capo
 */
public class AssemblyModifier {

    private AssemblyConfig mConfig;

    public AssemblyModifier(AssemblyConfig pConfig) {
        mConfig = pConfig;
    }

    public AssemblyConfigBuilder.ShrinkWrapBuilder archive(String pName) {
        return new AssemblyConfigBuilder.ShrinkWrapBuilder(mConfig, pName);
    }

    public AssemblyConfigBuilder.CompositeBuilder composite(String pName) {
        return new AssemblyConfigBuilder.CompositeBuilder(mConfig, pName);
    }

}
