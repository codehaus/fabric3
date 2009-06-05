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
package org.fabric3.resource.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.annotation.Resource;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.AbstractAnnotationProcessor;
import org.fabric3.resource.model.SystemSourcedResource;
import org.fabric3.model.type.java.FieldInjectionSite;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.model.type.java.MethodInjectionSite;
import org.fabric3.model.type.component.ResourceDefinition;
import org.fabric3.model.type.service.ServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class JSR250ResourceProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<Resource, I> {
    private final IntrospectionHelper helper;
    private final ContractProcessor contractProcessor;

    public JSR250ResourceProcessor(@Reference IntrospectionHelper helper, @Reference ContractProcessor contractProcessor) {
        super(Resource.class);
        this.helper = helper;
        this.contractProcessor = contractProcessor;
    }

    public void visitField(Resource annotation, Field field, I implementation, IntrospectionContext context) {
        String name = helper.getSiteName(field, annotation.name());
        Type type = field.getGenericType();
        FieldInjectionSite site = new FieldInjectionSite(field);
        ResourceDefinition definition = createResource(name, type, false, annotation.mappedName(), context.getTypeMapping(), context);
        implementation.getComponentType().add(definition, site);
    }

    public void visitMethod(Resource annotation, Method method, I implementation, IntrospectionContext context) {
        String name = helper.getSiteName(method, annotation.name());
        Type type = helper.getGenericType(method);
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        ResourceDefinition definition = createResource(name, type, false, annotation.mappedName(), context.getTypeMapping(), context);
        implementation.getComponentType().add(definition, site);
    }

    SystemSourcedResource createResource(String name,
                                         Type type,
                                         boolean optional,
                                         String mappedName,
                                         TypeMapping typeMapping,
                                         IntrospectionContext context) {
        ServiceContract<Type> serviceContract = contractProcessor.introspect(typeMapping, type, context);
        return new SystemSourcedResource(name, optional, mappedName, serviceContract);
    }
}
