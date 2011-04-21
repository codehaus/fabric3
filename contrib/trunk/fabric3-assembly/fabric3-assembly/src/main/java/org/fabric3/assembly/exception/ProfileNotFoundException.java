package org.fabric3.assembly.exception;

/**
 * When cannot find suitable profile by his name.
 *
 * @author Michal Capo
 */
public class ProfileNotFoundException extends Exception {

    public ProfileNotFoundException(String pProfileName) {
        super(pProfileName);
    }

}
