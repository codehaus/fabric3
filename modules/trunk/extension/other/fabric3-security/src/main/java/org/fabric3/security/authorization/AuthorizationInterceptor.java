package org.fabric3.security.authorization;

import org.fabric3.spi.invocation.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;

/**
 * Interceptor for performing role based authorization.
 * 
 * @version $Revision$ $Date$
 *
 */
public class AuthorizationInterceptor implements Interceptor {
    
    private Interceptor next;
    private final String[] roles;
    private final AuthorizationService authorizationService;

    /**
     * Initializes the roles required to pass through this interceptor and the user 
     * provided SPI extension for performing authorization.
     * 
     * @param roles Roles that need to be checked by this instance of the interceptor.
     * @param authorizationService AUthorization service extension to perform authorization.
     */
    public AuthorizationInterceptor(String[] roles, AuthorizationService authorizationService) {
        this.roles = roles;
        this.authorizationService = authorizationService;
    }

    /**
     * Gets the next interceptor in the chain.
     * 
     * @return The next interceptor in the chain.
     */
    public Interceptor getNext() {
        return next;
    }

    /**
     * Sets the next interceptor in the chain.
     * 
     * @param next The next interceptor in the chain.
     */
    public void setNext(Interceptor next) {
        this.next = next;
    }

    /**
     * Performs the authorization check. If succesful the next interceptor in the chain is 
     * invoked. If authorization fails, a fault is set on the message and returned to the 
     * preceding intereceptor in the chain.
     * 
     * @param msg Message passed in by the preceding interceptor.
     */
    public Message invoke(Message msg) {
        
        WorkContext workContext = msg.getWorkContext();
        
        AuthorizationResult result = authorizationService.hasRoles(workContext.getSubject(), roles);
        if (result.isSuccess()) {
            return next.invoke(msg);
        } else {
            msg.setBodyWithFault(result.getFault());
            return msg;
        }
        
    }

}
