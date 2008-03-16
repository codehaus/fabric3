package org.fabric3.security.authorization;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;

/**
 * Builder for the authorization interceptor. The builde is injected with the authorization 
 * service extension.
 * 
 * TODO We may need to support multiple authorization services based on the context in which 
 * the authorization service is used. The reference to authorization services here can be a 
 * keyed map. The key can be specified in the policy definition, which would map to a specific 
 * instance of the interceptor.
 * 
 * @version $Revision$ $Date$
 *
 */
public class AuthorizationInterceptorBuilder implements InterceptorBuilder<AuthorizationInterceptorDefinition, AuthorizationInterceptor> {
    
    private AuthorizationService authorizationService;

    /**
     * Injects the required references.
     * 
     * @param authorizationService Authorization service extension provided by the user.
     */
    public AuthorizationInterceptorBuilder(@Reference AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    /**
     * Builds the interceptor.
     * 
     * @param definition Authorization interceptor definition.
     * @return An instance of the authorization interceptor.
     */
    public AuthorizationInterceptor build(AuthorizationInterceptorDefinition definition) throws BuilderException {
        return new AuthorizationInterceptor(definition.getRoles(), authorizationService);
    }

}
