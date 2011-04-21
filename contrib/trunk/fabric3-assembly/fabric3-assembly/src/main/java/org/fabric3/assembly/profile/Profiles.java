package org.fabric3.assembly.profile;

import org.fabric3.assembly.exception.ProfileNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * Contain existing fabric3 profiles.
 *
 * @author Michal Capo
 */
public class Profiles {

    /**
     * JMS profile.
     */
    public static final Profile JMS = ProfileFactory.createProfile("profile-jms", "jms");

    /**
     * JPA profile.
     */
    public static final Profile JPA = ProfileFactory.createProfile("profile-jpa", "jpa");

    /**
     * Rest profile.
     */
    public static final Profile REST = ProfileFactory.createProfile("profile-rs", "rs", "rest");

    /**
     * Spring profile.
     */
    public static final Profile SPRING = ProfileFactory.createProfile("profile-spring", "spring");

    /**
     * Web service profile.
     */
    public static final Profile WEB_SERVICE = ProfileFactory.createProfile("profile-ws", "ws", "webservice", "web-service", "web_service");

    /**
     * Web profile.
     */
    public static final Profile WEB = ProfileFactory.createProfile("profile-web", "web");

    /**
     * Timer profile.
     */
    public static final Profile TIMER = ProfileFactory.createProfile("profile-timer", "timer");

    /**
     * Infinispan profile
     */
    public static final Profile INFINISPAN = ProfileFactory.createProfile("profile-infinispan", "infinispan");

    /**
     * FTP profile.
     */
    public static final Profile FTP = ProfileFactory.createProfile("profile-ftp", "ftp");

    /**
     * Test profile. List of files needed for running tests on fabric server.
     */
    public static final Profile TEST = new Profile("profile-test", "test") {{
        files.add(ProfileFactory.jar("fabric3-junit"));
        files.add(ProfileFactory.jar("fabric3-test-spi"));
        files.add(ProfileFactory.jar("fabric3-ant-api"));
        files.add(ProfileFactory.jar("fabric3-ant-extension"));
    }};

    public static Profile getProfileByName(String pName) throws ProfileNotFoundException {
        List<Profile> knownProfiles = new ArrayList<Profile>();
        knownProfiles.add(JMS);
        knownProfiles.add(JPA);
        knownProfiles.add(REST);
        knownProfiles.add(SPRING);
        knownProfiles.add(WEB_SERVICE);
        knownProfiles.add(WEB);
        knownProfiles.add(TIMER);
        knownProfiles.add(INFINISPAN);
        knownProfiles.add(TEST);

        for (Profile profile : knownProfiles) {
            if (pName.equals(profile.getName()) || profile.getAlternativeNames().contains(pName)) {
                return profile;
            }
        }

        throw new ProfileNotFoundException(pName);
    }

}
