package org.fabric3.security.authorization;

import javax.xml.namespace.QName;

import org.fabric3.scdl.Operation;
import org.fabric3.spi.Constants;
import org.fabric3.spi.generator.GeneratorContext;
import org.fabric3.spi.generator.GeneratorRegistry;
import org.fabric3.spi.generator.InterceptorDefinitionGenerator;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.w3c.dom.Element;

/**
 * Generates the physical interceptor definition from the underlying policy infoset. An example 
 * for the policy definition is shown below,
 * 
 * <f3:authorization roles="ADMIN,USER"/>
 * 
 * where the namespace prefix f3 maps to the url http://fabric3.org/xmlns/sca/2.0-alpha.
 * 
 * @version $Revision$ $Date$
 *
 */
@EagerInit
public class AuthorizationInterceptorDefinitionGenerator implements InterceptorDefinitionGenerator {
    
    private static final QName EXTENSION_NAME = new QName(Constants.FABRIC3_NS, "authorization");
    private GeneratorRegistry generatorRegistry;

    /**
     * Initializes the interceptor definition generator registry.
     * 
     * @param generatorRegistry Interceptor definition generator registry.
     */
    public AuthorizationInterceptorDefinitionGenerator(@Reference GeneratorRegistry generatorRegistry) {
        this.generatorRegistry = generatorRegistry;
    }

    /**
     * Registers with the intereceptor generator registry.
     */
    @Init
    public void init() {
        generatorRegistry.register(EXTENSION_NAME, this);
    }

    /**
     * Generates the interceptor definition from the underlying policy infoset.
     * 
     * @param policyDefinition Policy set definition.
     * @param generatorContext Generator context.
     * @param operation Operation against which the interceptor is generated.
     * @param logicalBinding Logical binding on the service or reference.
     * @return Physical interceptor definition.
     */
    public AuthorizationInterceptorDefinition generate(Element policyDefinition, 
                                                       GeneratorContext generatorContext,
                                                       Operation<?> operation,
                                                       LogicalBinding<?> logicalBinding) {
        
        String rolesAttribute = policyDefinition.getAttribute("roles");
        if (rolesAttribute == null) {
            throw new AssertionError("No roles are defined in the authorization policy");
        }
        String[] roles = rolesAttribute.split(",");
        
        return new AuthorizationInterceptorDefinition(roles);

    }

}
