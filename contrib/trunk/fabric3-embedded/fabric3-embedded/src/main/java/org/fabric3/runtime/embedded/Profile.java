package org.fabric3.runtime.embedded;

import org.fabric3.runtime.embedded.api.EmbeddedProfile;
import org.fabric3.runtime.embedded.api.EmbeddedVersion;

import java.text.MessageFormat;

/**
 * Contain existing fabric3 profiles.
 *
 * @author Michal Capo
 */
public class Profile {

    /**
     * JMS profile.
     */
    public static final EmbeddedProfile JMS = createProfile("profile-jms");

    /**
     * JPA profile.
     */
    public static final EmbeddedProfile JPA = createProfile("profile-jpa");

    /**
     * Rest profile.
     */
    public static final EmbeddedProfile REST = createProfile("profile-rs");

    /**
     * Spring profile.
     */
    public static final EmbeddedProfile SPRING = createProfile("profile-spring");

    /**
     * Web service profile.
     */
    public static final EmbeddedProfile WEB_SERVICE = createProfile("profile-ws");

    /**
     * Web profile.
     */
    public static final EmbeddedProfile WEB = createProfile("profile-web");

    /**
     * Timer profile.
     */
    public static final EmbeddedProfile TIMER = createProfile("profile-timer");

    /**
     * Net profile.
     */
    public static final EmbeddedProfile NET = createProfile("profile-net");

    /**
     * FTP profile.
     */
    public static final EmbeddedProfile FTP = createProfile("profile-ftp");

    /**
     * Test profile. List of filed needed for running tests on embedded fabric.
     */
    public static final EmbeddedProfile TEST = new EmbeddedProfileImpl("profile-test") {{
        files.add(jar("fabric3-junit"));
        files.add(jar("fabric3-test-spi"));
        files.add(jar("fabric3-ant-api"));
        files.add(jar("fabric3-ant-extension"));
    }};

    private static EmbeddedProfile createProfile(final String name) {
        return new EmbeddedProfileImpl(name) {{
            files.add(zip(name));
        }};
    }

    private static String jar(String jarName) {
        return MessageFormat.format("org.codehaus.fabric3:{0}:{1}@jar", jarName, EmbeddedVersion.FABRIC3);
    }

    private static String zip(String zipName) {
        return MessageFormat.format("org.codehaus.fabric3:{0}:{1}:bin@zip", zipName, EmbeddedVersion.FABRIC3);
    }

}
