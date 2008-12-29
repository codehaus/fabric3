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

import java.lang.reflect.Type;
import java.net.URI;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContextType;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.api.jpa.ConversationalDaoImpl;
import org.fabric3.java.control.JavaImplementation;
import org.fabric3.java.control.JavaImplementationProcessor;
import org.fabric3.jpa.scdl.PersistenceContextResource;
import org.fabric3.spi.introspection.DefaultValidationContext;
import org.fabric3.spi.introspection.ValidationContext;
import org.fabric3.model.type.java.FieldInjectionSite;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.spi.Namespaces;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.contract.ContractProcessor;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Implementation loader for JPA component.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class JpaImplementationLoader implements TypeLoader<JavaImplementation> {

    public static final QName IMPLEMENTATION_JPA = new QName(Namespaces.IMPLEMENTATION, "implementation.jpa");

    private final JavaImplementationProcessor implementationProcessor;
    private final ServiceContract<Type> factoryServiceContract;

    public JpaImplementationLoader(@Reference JavaImplementationProcessor implementationProcessor, @Reference ContractProcessor contractProcessor) {
        this.implementationProcessor = implementationProcessor;
        ValidationContext context = new DefaultValidationContext();
        factoryServiceContract = contractProcessor.introspect(new TypeMapping(), EntityManager.class, context);
        assert !context.hasErrors();  // should not happen
    }

    /**
     * Creates the instance of the implementation type.
     *
     * @param reader  Stax XML stream reader used for reading data.
     * @param context Introspection context.
     * @return An instance of the JPA implemenation.
     */
    public JavaImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {
        validateAttributes(reader, context);

        try {
            JavaImplementation implementation = new JavaImplementation();
            String persistenceUnit = reader.getAttributeValue(null, "persistenceUnit");
            if (persistenceUnit == null) {
                MissingAttribute failure = new MissingAttribute("Missing attribute: persistenceUnit", "persistenceUnit", reader);
                context.addError(failure);
                return implementation;
            }

            implementation.setImplementationClass(ConversationalDaoImpl.class.getName());

            URI contributionUri = context.getContributionUri();
            String targetNs = context.getTargetNamespace();
            ClassLoader cl = getClass().getClassLoader();

            IntrospectionContext childContext = new DefaultIntrospectionContext(contributionUri, cl, targetNs);
            implementationProcessor.introspect(implementation, childContext);
            if (childContext.hasErrors()) {
                context.addErrors(childContext.getErrors());
            }
            if (childContext.hasWarnings()) {
                context.addWarnings(childContext.getWarnings());
            }

            PojoComponentType pojoComponentType = implementation.getComponentType();

            PersistenceContextResource resource = new PersistenceContextResource(
                    "unit", persistenceUnit, PersistenceContextType.TRANSACTION, factoryServiceContract, false);
            FieldInjectionSite site = new FieldInjectionSite(ConversationalDaoImpl.class.getDeclaredField("entityManager"));
            pojoComponentType.add(resource, site);
            LoaderUtil.skipToEndElement(reader);

            return implementation;

        } catch (NoSuchFieldException e) {
            // this should not happen
            throw new AssertionError(e);
        }

    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"persistenceUnit".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }


}
