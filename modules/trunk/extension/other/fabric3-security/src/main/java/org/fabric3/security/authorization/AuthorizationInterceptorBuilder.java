/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.security.authorization;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.wire.Interceptor;

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
public class AuthorizationInterceptorBuilder implements InterceptorBuilder<AuthorizationInterceptorDefinition> {
    
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
    public Interceptor build(AuthorizationInterceptorDefinition definition) throws BuilderException {
        return new AuthorizationInterceptor(definition.getRoles(), authorizationService);
    }

}
