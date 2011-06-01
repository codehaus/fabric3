package org.fabric3.assembly.dependency.fabric;

import org.fabric3.assembly.configuration.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Contain existing fabric3 profiles.
 *
 * @author Michal Capo
 */
public class FabricProfiles {

    /**
     * JMS profile.
     */
    public static final Profile JMS = FabricDependencyFactory.createProfile("profile-jms", "jms");

    /**
     * JPA profile.
     */
    public static final Profile JPA = FabricDependencyFactory.createProfile("profile-jpa", "jpa");

    /**
     * Rest profile.
     */
    public static final Profile REST = FabricDependencyFactory.createProfile("profile-rs", "rs", "rest");

    /**
     * Spring profile.
     */
    public static final Profile SPRING = FabricDependencyFactory.createProfile("profile-spring", "spring");

    /**
     * Web service profile.
     */
    public static final Profile WEB_SERVICE = FabricDependencyFactory.createProfile("profile-ws", "ws", "webservice", "web-service", "web_service");

    /**
     * Web profile.
     */
    public static final Profile WEB = FabricDependencyFactory.createProfile("profile-web", "web");

    /**
     * Timer profile.
     */
    public static final Profile TIMER = FabricDependencyFactory.createProfile("profile-timer", "timer");

    /**
     * Infinispan profile.
     */
    public static final Profile INFINISPAN = FabricDependencyFactory.createProfile("profile-infinispan", "infinispan");

    /**
     * FTP profile.
     */
    public static final Profile FTP = FabricDependencyFactory.createProfile("profile-ftp", "ftp");

    /**
     * Test profile. List of files needed for running tests on fabric server.
     */
    public static final Profile TEST = new Profile("profile-test", null, null, "test") {
        {
            addDependency(FabricDependencyFactory.jar("fabric3-junit"));
            addDependency(FabricDependencyFactory.jar("fabric3-test-spi"));
            addDependency(FabricDependencyFactory.jar("fabric3-ant-extension"));
            addDependency(FabricDependencyFactory.jar("fabric3-ant-api"));
        }
    };

    public static List<Profile> all() {
        List<Profile> result = new ArrayList<Profile>();

        result.add(JMS);
        result.add(JPA);
        result.add(REST);
        result.add(SPRING);
        result.add(WEB_SERVICE);
        result.add(WEB);
        result.add(TIMER);
        result.add(INFINISPAN);
        result.add(FTP);
        result.add(TEST);

        return result;
    }

}
