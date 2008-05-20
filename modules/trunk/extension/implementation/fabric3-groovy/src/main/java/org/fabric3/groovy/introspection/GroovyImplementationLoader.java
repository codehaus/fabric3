/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.fabric3.groovy.introspection;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.groovy.scdl.GroovyImplementation;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionException;
import org.fabric3.introspection.xml.LoaderException;
import org.fabric3.introspection.xml.LoaderHelper;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingResourceException;
import org.fabric3.introspection.xml.TypeLoader;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class GroovyImplementationLoader implements TypeLoader<GroovyImplementation> {

    private final GroovyImplementationProcessor processor;
    private final LoaderHelper loaderHelper;

    public GroovyImplementationLoader(@Reference(name = "implementationProcessor")GroovyImplementationProcessor processor,
                                      @Reference LoaderHelper loaderHelper) {
        this.processor = processor;
        this.loaderHelper = loaderHelper;
    }

    public GroovyImplementation load(XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException, LoaderException {

        String className = reader.getAttributeValue(null, "class");
        String scriptName = reader.getAttributeValue(null, "script");

        if (className == null && scriptName == null) {
            throw new MissingResourceException("No Groovy script or class name", reader);
        }

/*
        PojoComponentType componentType = new PojoComponentType(implClass.getName());
        introspector.introspect(implClass, componentType, context);
        if (componentType.getScope() == null) {
            componentType.setScope("STATELESS");
        }
*/

        GroovyImplementation impl = new GroovyImplementation(scriptName, className);
        loaderHelper.loadPolicySetsAndIntents(impl, reader);
        try {
            processor.introspect(impl, context);
        } catch (IntrospectionException e) {
            throw new LoaderException(reader, e);
        }

        LoaderUtil.skipToEndElement(reader);
        return impl;
    }
}
