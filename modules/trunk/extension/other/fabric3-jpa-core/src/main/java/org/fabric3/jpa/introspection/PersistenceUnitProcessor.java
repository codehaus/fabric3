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
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.jpa.scdl.PersistenceUnitResource;
import org.fabric3.model.type.component.Implementation;
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
 * @version $Rev$ $Date$
 */
@EagerInit
public class PersistenceUnitProcessor<I extends Implementation<? extends InjectingComponentType>> extends AbstractAnnotationProcessor<PersistenceUnit, I> {

    private final ServiceContract<Type> factoryServiceContract;

    public PersistenceUnitProcessor(@Reference ContractProcessor contractProcessor) {
        super(PersistenceUnit.class);
        IntrospectionContext context = new DefaultIntrospectionContext();
        factoryServiceContract = contractProcessor.introspect(new TypeMapping(), EntityManagerFactory.class, context);
        assert !context.hasErrors(); // should not happen
    }

    public void visitField(PersistenceUnit annotation, Field field, I implementation, IntrospectionContext context) {
        FieldInjectionSite site = new FieldInjectionSite(field);
        PersistenceUnitResource definition = createDefinition(annotation);
        InjectingComponentType componentType = implementation.getComponentType();
        componentType.add(definition, site);
        // record that the implementation requires JPA
        componentType.addRequiredCapability("jpa");
    }

    public void visitMethod(PersistenceUnit annotation, Method method, I implementation, IntrospectionContext context) {
        MethodInjectionSite site = new MethodInjectionSite(method, 0);
        PersistenceUnitResource definition = createDefinition(annotation);
        InjectingComponentType componentType = implementation.getComponentType();
        componentType.add(definition, site);
        // record that the implementation requires JPA
        componentType.addRequiredCapability("jpa");
    }

    PersistenceUnitResource createDefinition(PersistenceUnit annotation) {
        String name = annotation.name();
        String unitName = annotation.unitName();
        return new PersistenceUnitResource(name, unitName, factoryServiceContract);
    }
}
