package org.fabric3.security.authorization;

/**
 * Result of performing authorization.
 * 
 * @version $Revision$ $Date$
 *
 */
public interface AuthorizationResult {
    
    /**
     * Checks whether authorization was success.
     * 
     * @return True if authorization was success.
     */
    boolean isSuccess();
    
    /**
     * Returns the fault if authorization was not a success.
     * 
     * @return Fault if authorization was not success.
     */
    Object getFault();

}
