package org.fabric3.assembly.profile;

import org.fabric3.assembly.configuration.Version;

import java.text.MessageFormat;

/**
 * @author Michal Capo
 */
public class ProfileFactory {

    public static Profile createProfile(final String name) {
        return new Profile(name) {{
            files.add(zip(name));
        }};
    }

    public static Profile createProfile(final String name, final String... alternativeNames) {
        return new Profile(name, alternativeNames) {{
            files.add(zip(name));
        }};
    }

    public static String jar(String jarName) {
        return MessageFormat.format("org.codehaus.fabric3:{0}:{1}", jarName, Version.FABRIC3);
    }

    public static String zip(String zipName) {
        return MessageFormat.format("org.codehaus.fabric3:{0}:zip:bin:{1}", zipName, Version.FABRIC3);
    }

}
