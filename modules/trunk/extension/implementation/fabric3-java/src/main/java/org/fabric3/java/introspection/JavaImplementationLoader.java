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
package org.fabric3.java.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.Reference;

import org.fabric3.java.control.JavaImplementation;
import org.fabric3.java.control.JavaImplementationProcessor;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;
import org.fabric3.spi.introspection.xml.UnrecognizedAttribute;

/**
 * Loads <implementation.java> in a composite.
 */
public class JavaImplementationLoader implements TypeLoader<JavaImplementation> {

    private final JavaImplementationProcessor implementationProcessor;
    private final LoaderHelper loaderHelper;


    public JavaImplementationLoader(@Reference JavaImplementationProcessor implementationProcessor, @Reference LoaderHelper loaderHelper) {
        this.implementationProcessor = implementationProcessor;
        this.loaderHelper = loaderHelper;
    }


    public JavaImplementation load(XMLStreamReader reader, IntrospectionContext introspectionContext) throws XMLStreamException {

        assert JavaImplementation.IMPLEMENTATION_JAVA.equals(reader.getName());

        validateAttributes(reader, introspectionContext);
        JavaImplementation implementation = new JavaImplementation();
        String implClass = reader.getAttributeValue(null, "class");
        if (implClass == null) {
            MissingAttribute failure = new MissingAttribute("The class attribute was not specified", "class", reader);
            introspectionContext.addError(failure);
            LoaderUtil.skipToEndElement(reader);
            return implementation;
        }
        loaderHelper.loadPolicySetsAndIntents(implementation, reader, introspectionContext);

        LoaderUtil.skipToEndElement(reader);

        implementation.setImplementationClass(implClass);
        implementationProcessor.introspect(implementation, introspectionContext);
        return implementation;
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
