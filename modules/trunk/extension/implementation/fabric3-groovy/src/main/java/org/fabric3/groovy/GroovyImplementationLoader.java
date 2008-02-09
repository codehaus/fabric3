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
package org.fabric3.groovy;

import java.io.IOException;
import java.net.URL;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.osoa.sca.annotations.Destroy;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;

import org.fabric3.pojo.processor.Introspector;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.Scope;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.spi.loader.LoaderException;
import org.fabric3.spi.loader.LoaderRegistry;
import org.fabric3.spi.loader.LoaderUtil;
import org.fabric3.spi.loader.MissingResourceException;
import org.fabric3.spi.loader.PolicyHelper;
import org.fabric3.spi.loader.StAXElementLoader;

/**
 * @version $Rev$ $Date$
 */
@EagerInit
public class GroovyImplementationLoader implements StAXElementLoader<GroovyImplementation> {

    private final LoaderRegistry registry;
    private final Introspector introspector;
    private final PolicyHelper policyHelper;

    public GroovyImplementationLoader(@Reference LoaderRegistry registry,
                                      @Reference Introspector introspector,
                                      @Reference PolicyHelper policyHelper) {
        this.registry = registry;
        this.introspector = introspector;
        this.policyHelper = policyHelper;
    }

    @Init
    public void init() {
        registry.registerLoader(GroovyImplementation.IMPLEMENTATION_GROOVY, this);
    }

    @Destroy
    public void destroy() {
        registry.unregisterLoader(GroovyImplementation.IMPLEMENTATION_GROOVY);
    }

    public GroovyImplementation load(XMLStreamReader reader, IntrospectionContext context)
            throws XMLStreamException, LoaderException {

        String className = reader.getAttributeValue(null, "class");
        String scriptName = reader.getAttributeValue(null, "script");

        Class<?> implClass;
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // Set TCCL to the extension classloader as implementations may need access to Groovy classes. Also, Groovy
            // dependencies such as Antlr use the TCCL.
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            GroovyClassLoader gcl = new GroovyClassLoader(context.getTargetClassLoader());
            if (className != null) {
                try {
                    implClass = gcl.loadClass(className);
                } catch (ClassNotFoundException e) {
                    throw new MissingResourceException(className, e);
                }
            } else if (scriptName != null) {
                try {
                    URL scriptURL = gcl.getResource(scriptName);
                    if (scriptURL == null) {
                        throw new MissingResourceException(scriptName);
                    }
                    GroovyCodeSource codeSource = new GroovyCodeSource(scriptURL);
                    implClass = gcl.parseClass(codeSource);
                } catch (IOException e) {
                    throw new MissingResourceException(scriptName, e);
                }
            } else {
                throw new MissingResourceException("No Groovy script or class name");
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
        PojoComponentType componentType = new PojoComponentType(implClass.getName());
        introspector.introspect(implClass, componentType, context);
        if (componentType.getImplementationScope() == null) {
            componentType.setImplementationScope(Scope.STATELESS);
        }
        GroovyImplementation impl = new GroovyImplementation(scriptName, className, componentType);

        policyHelper.loadPolicySetsAndIntents(impl, reader);

        LoaderUtil.skipToEndElement(reader);

        return impl;
    }
}
