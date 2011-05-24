package org.fabric3.assembly.validation;

import org.fabric3.assembly.configuration.Composite;
import org.fabric3.assembly.configuration.Profile;
import org.fabric3.assembly.configuration.Runtime;
import org.fabric3.assembly.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Michal Capo
 */
public class ValidationHelper {

    public static void validateSameProfileName(String pContainerName, List<Profile> pProfiles) {
        List<String> names = new ArrayList<String>();
        for (Profile profile : pProfiles) {
            boolean contains = false;
            if (names.contains(profile.getName())) {
                contains = true;
            }
            for (String s : profile.getAlternativeNames()) {
                if (names.contains(s)) {
                    contains = true;
                }
            }

            if (contains) {
                throw new ValidationException("You specified ''{0}'' profile twice in the: {1}", profile.getName(), pContainerName);
            }

            names.add(profile.getName());
            names.addAll(profile.getAlternativeNames());
        }
    }

    public static void validateSameCompositeName(String pContainerName, List<Composite> pComposites) {
        List<String> names = new ArrayList<String>();
        for (Composite profile : pComposites) {
            if (names.contains(profile.getName())) {
                throw new ValidationException("You specified ''{0}'' composite twice in the: {1}", profile.getName(), pContainerName);
            }

            names.add(profile.getName());
        }
    }

    public static void validateSameRuntimeName(String pContainerName, List<Runtime> pRuntimes) {
        List<String> names = new ArrayList<String>();
        for (org.fabric3.assembly.configuration.Runtime r : pRuntimes) {
            if (names.contains(r.getRuntimeName())) {
                throw new ValidationException("You specified ''{0}'' runtime twice in the: {1}", r.getRuntimeName(), pContainerName);
            }

            names.add(r.getRuntimeName());
        }

    }

}
