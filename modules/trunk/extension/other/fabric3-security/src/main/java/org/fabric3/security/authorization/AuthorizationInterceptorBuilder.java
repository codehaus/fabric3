package org.fabric3.security.authorization;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.builder.interceptor.InterceptorBuilderRegistry;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

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
@EagerInit
public class AuthorizationInterceptorBuilder implements InterceptorBuilder<AuthorizationInterceptorDefinition, AuthorizationInterceptor> {
    
    private AuthorizationService authorizationService;
    private InterceptorBuilderRegistry registry;

    /**
     * Injects the required references.
     * 
     * @param registry Interceptor builder registry.
     * @param authorizationService Authorization service extension provided by the user.
     */
    public AuthorizationInterceptorBuilder(@Reference InterceptorBuilderRegistry registry, @Reference AuthorizationService authorizationService) {
        this.registry = registry;
        this.authorizationService = authorizationService;
    }

    /**
     * Registers with the interceptor builder registry.
     */
    @Init
    public void init() {
        registry.register(AuthorizationInterceptorDefinition.class, this);
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
