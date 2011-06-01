package org.fabric3.assembly.factory;

import org.fabric3.assembly.configuration.AssemblyConfig;

/**
 * @author Michal Capo
 */
public class AssemblyConfigHolder {

    private static AssemblyConfig mConfig;

    public synchronized static AssemblyConfig getConfig() {
        if (null == mConfig) {
            mConfig = new AssemblyConfig();
        }

        return mConfig;
    }

}
