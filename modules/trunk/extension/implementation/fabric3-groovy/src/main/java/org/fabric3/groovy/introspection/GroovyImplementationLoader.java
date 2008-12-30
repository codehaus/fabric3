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
package org.fabric3.groovy.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.groovy.scdl.GroovyImplementation;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.java.ImplementationProcessor;
import org.fabric3.spi.introspection.xml.LoaderHelper;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class GroovyImplementationLoader implements TypeLoader<GroovyImplementation> {

    private final ImplementationProcessor<GroovyImplementation> processor;
    private final LoaderHelper loaderHelper;

    public GroovyImplementationLoader(@Reference(name = "implementationProcessor")ImplementationProcessor<GroovyImplementation> processor,
                                      @Reference LoaderHelper loaderHelper) {
        this.processor = processor;
        this.loaderHelper = loaderHelper;
    }

    public GroovyImplementation load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        String className = reader.getAttributeValue(null, "class");
        String scriptName = reader.getAttributeValue(null, "script");

        if (className == null && scriptName == null) {
            MissingAttribute failure = new MissingAttribute("No Groovy script or class name specified", reader);
            context.addError(failure);
            return null;
        }

/*
        PojoComponentType componentType = new PojoComponentType(implClass.getName());
        introspector.introspect(implClass, componentType, context);
        if (componentType.getScope() == null) {
            componentType.setScope("STATELESS");
        }
*/

        GroovyImplementation impl = new GroovyImplementation(scriptName, className);
        loaderHelper.loadPolicySetsAndIntents(impl, reader, context);
        processor.introspect(impl, context);
        LoaderUtil.skipToEndElement(reader);
        return impl;
    }
}
