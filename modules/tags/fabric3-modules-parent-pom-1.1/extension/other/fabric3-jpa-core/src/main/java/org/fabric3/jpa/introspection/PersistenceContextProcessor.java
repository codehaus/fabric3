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
package org.fabric3.jpa.introspection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.jpa.scdl.PersistenceContextResource;
import org.fabric3.model.type.component.Implementation;
import org.fabric3.model.type.component.Scope;
import org.fabric3.model.type.java.FieldInjectionSite;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.model.type.java.MethodInjectionSite;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.java.AbstractAnnotationProcessor;

/**
 * Processes @PersistenceContext annotations.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class PersistenceContextProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<PersistenceContext, I> {
    private final ServiceContract<Type> factoryServiceContract;

    public PersistenceContextProcessor(@Reference ContractProcessor contractProcessor) {
        super(PersistenceContext.class);
        IntrospectionContext context = new DefaultIntrospectionContext();
        factoryServiceContract = contractProcessor.introspect(new TypeMapping(), EntityManager.class, context);
        assert !context.hasErrors(); // should not happen
    }

    public void visitField(PersistenceContext annotation, Field field, I implementation, IntrospectionContext context) {
        FieldInjectionSite site = new FieldInjectionSite(field);
        InjectingComponentType componentType = implementation.getComponentType();
        PersistenceContextResource definition = createDefinition(annotation, componentType);
        componentType.add(definition, site);
        // record that the implementation requires JPA
        componentType.addRequiredCapability("jpa");
    }

    public void visitMethod(PersistenceContext annotation, Method method, I implementation, IntrospectionContext context) {
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        InjectingComponentType componentType = implementation.getComponentType();
        PersistenceContextResource definition = createDefinition(annotation, componentType);
        componentType.add(definition, site);
        // record that the implementation requires JPA
        componentType.addRequiredCapability("jpa");
    }

    private PersistenceContextResource createDefinition(PersistenceContext annotation, InjectingComponentType componentType) {
        String name = annotation.name();
        String unitName = annotation.unitName();
        PersistenceContextType type = annotation.type();
        boolean multiThreaded = Scope.COMPOSITE.getScope().equals(componentType.getScope());
        return new PersistenceContextResource(name, unitName, type, factoryServiceContract, multiThreaded);
    }
}