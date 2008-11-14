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
package org.fabric3.junit.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.TypeLoader;
import org.fabric3.introspection.xml.UnrecognizedAttribute;
import org.fabric3.junit.scdl.JUnitImplementation;
import org.fabric3.junit.scdl.JUnitBindingDefinition;
import org.fabric3.scdl.ServiceDefinition;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class JUnitImplementationLoader implements TypeLoader<JUnitImplementation> {

    private final JUnitImplementationProcessor implementationProcessor;

    public JUnitImplementationLoader(@Reference JUnitImplementationProcessor implementationProcessor) {
        this.implementationProcessor = implementationProcessor;
    }

    public JUnitImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {
        validateAttributes(reader, introspectionContext);
        String className = reader.getAttributeValue(null, "class");
        LoaderUtil.skipToEndElement(reader);

        JUnitImplementation impl = new JUnitImplementation(className);
        implementationProcessor.introspect(impl, introspectionContext);
        // Add bindings so wires are generated to the test operations. These wires will be used by the testing runtime to dispatch to the
        // JUnit components
        for (ServiceDefinition definition : impl.getComponentType().getServices().values()) {
            definition.addBinding(new JUnitBindingDefinition());
        }
        return impl;
    }

    private void validateAttributes(XMLStreamReader reader, IntrospectionContext context) {
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            String name = reader.getAttributeLocalName(i);
            if (!"class".equals(name) && !"requires".equals(name) && !"policySets".equals(name)) {
                context.addError(new UnrecognizedAttribute(name, reader));
            }
        }
    }


}
