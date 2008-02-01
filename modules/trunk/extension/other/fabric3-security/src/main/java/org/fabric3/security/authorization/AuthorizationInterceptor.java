package org.fabric3.security.authorization;

import org.fabric3.spi.component.WorkContext;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.Message;

/**
 * 
 * @version $Revision$ $Date$
 *
 */
public class AuthorizationInterceptor implements Interceptor {
    
    private Interceptor next;
    private final String[] roles;
    private final AuthorizationService authorizationService;

    public AuthorizationInterceptor(String[] roles, AuthorizationService authorizationService) {
        this.roles = roles;
        this.authorizationService = authorizationService;
    }

    public Interceptor getNext() {
        return next;
    }

    public void setNext(Interceptor next) {
        this.next = next;
    }

    public Message invoke(Message msg) {
        
        WorkContext workContext = msg.getWorkContext();
        
        boolean result = authorizationService.hasRoles(workContext.getSubject(), roles);
        if (result) {
            return next.invoke(msg);
        } else {
            // TODO Pass the fault back
            return null;
        }
        
    }

}
