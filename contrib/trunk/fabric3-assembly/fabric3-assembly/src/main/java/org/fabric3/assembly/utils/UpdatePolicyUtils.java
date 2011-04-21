package org.fabric3.assembly.utils;

import org.fabric3.assembly.profile.UpdatePolicy;

import java.io.File;

/**
 * @author Michal Capo
 */
public class UpdatePolicyUtils {

    /**
     * Check if given folder should be updated or not.
     *
     * @param folder  to be checked
     * @param pPolicy is currently applied
     * @return <code>true</code> if that folder should be update, otherwise return <code>false</code>
     */
    public static boolean shouldUpdate(final File folder, UpdatePolicy pPolicy) {
        if (!folder.exists()) {
            return true;
        }

        switch (pPolicy) {
            case ALWAYS:
                return true;
            case DAILY:
                if (TimeUtils.day(folder.lastModified()) != TimeUtils.day(System.currentTimeMillis())) {
                    return true;
                }
        }

        return false;
    }

}
