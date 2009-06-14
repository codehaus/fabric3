  /*
   * Fabric3
   * Copyright (C) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
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
