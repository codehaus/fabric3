package org.fabric3.security.authorization;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.builder.interceptor.InterceptorBuilderRegistry;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

/**
 * 
 * @version $Revision$ $Date$
 *
 */
public class AuthorizationInterceptorBuilder implements InterceptorBuilder<AuthorizationInterceptorDefinition, AuthorizationInterceptor> {
    
    private AuthorizationService authorizationService;
    private InterceptorBuilderRegistry registry;

    public AuthorizationInterceptorBuilder(@Reference InterceptorBuilderRegistry registry,
                                @Reference AuthorizationService authorizationService) {
        this.registry = registry;
        this.authorizationService = authorizationService;
    }

    @Init
    public void init() {
        registry.register(AuthorizationInterceptorDefinition.class, this);
    }

    public AuthorizationInterceptor build(AuthorizationInterceptorDefinition definition) throws BuilderException {
        return new AuthorizationInterceptor(definition.getRoles(), authorizationService);
    }

    @Reference
    public void setAuthorizationService(AuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

}
