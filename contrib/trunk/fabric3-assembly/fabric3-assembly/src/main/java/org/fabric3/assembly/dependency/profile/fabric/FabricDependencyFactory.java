package org.fabric3.assembly.dependency.profile.fabric;

import org.fabric3.assembly.configuration.Profile;
import org.fabric3.assembly.dependency.Dependency;

/**
 * @author Michal Capo
 */
public class FabricDependencyFactory {

    public static Profile createProfile(final String jarName, final String... alternativeNames) {
        return new Profile(jarName, null, null, alternativeNames) {{
            addDependency(zip(jarName));
        }};
    }

    public static Dependency jar(String jarName) {
        return new Dependency("org.codehaus.fabric3", jarName);
    }

    public static Dependency zip(String zipName) {
        return new Dependency("org.codehaus.fabric3", zipName, "bin", "zip");
    }

}
