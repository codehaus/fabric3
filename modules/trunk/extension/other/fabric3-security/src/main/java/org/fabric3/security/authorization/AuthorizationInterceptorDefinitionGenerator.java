package org.fabric3.security.authorization;

import javax.xml.namespace.QName;

import org.fabric3.spi.Constants;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * Policy definition format: <f3:authorization roles="ADMIN,USER"/>
 * 
 * 
 * @version $Revision$ $Date$
 *
 */
public class AuthorizationInterceptorDefinitionGenerator implements InterceptorDefinitionGenerator {
    
    private static final QName EXTENSION_NAME = new QName(Constants.FABRIC3_NS, "authorization");
    private GeneratorRegistry generatorRegistry;

    @Init
    public void start() {
        generatorRegistry.register(EXTENSION_NAME, this);
    }

    public AuthorizationInterceptorDefinitionGenerator(@Reference GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    public AuthorizationInterceptorDefinition generate(Element policyDefinition, GeneratorContext generatorContext) {
        
        String rolesAttribute = policyDefinition.getAttribute("roles");
        String[] roles = rolesAttribute.split(",");
        
        return new AuthorizationInterceptorDefinition(roles);

    }

}
