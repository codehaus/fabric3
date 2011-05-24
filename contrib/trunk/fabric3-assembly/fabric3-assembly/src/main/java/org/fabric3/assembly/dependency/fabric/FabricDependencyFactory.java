package org.fabric3.assembly.dependency.fabric;

import org.fabric3.assembly.configuration.Profile;
import org.fabric3.assembly.dependency.Dependency;

/**
 * @author Michal Capo
 */
public class FabricDependencyFactory {

    public static Profile createProfile(final String pJarName, final String... pAlternativeNames) {
        return new Profile(pJarName, null, null, pAlternativeNames) {
            {
                addDependency(zip(pJarName));
            }
        };
    }

    public static Dependency jar(String pJarName) {
        return new Dependency("org.codehaus.fabric3", pJarName);
    }

    public static Dependency zip(String pZipName) {
        return new Dependency("org.codehaus.fabric3", pZipName, "bin", "zip");
    }

}
