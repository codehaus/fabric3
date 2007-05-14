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
package org.fabric3.scdl4j;

import java.io.IOException;
import java.net.URL;
import javax.xml.namespace.QName;

import org.scdl4j.Composite;
import org.scdl4j.SCDL4J;

import org.fabric3.scdl4j.spi.ClassIntrospector;
import org.fabric3.scdl4j.spi.IntrospectionException;
import org.fabric3.scdl4j.spi.ResourceIntrospector;

/**
 * @version $Rev$ $Date$
 */
public class SCDL4JImpl implements SCDL4J {
    private final ClassIntrospector classIntrospector;
    private final ResourceIntrospector resourceIntrospector;

    public SCDL4JImpl(ClassIntrospector classIntrospector, ResourceIntrospector resourceIntrospector) {
        this.classIntrospector = classIntrospector;
        this.resourceIntrospector = resourceIntrospector;
    }

    public Composite createComposite(QName name) {
        return new CompositeImpl(this, name);
    }

    public Composite createComposite(QName name, boolean autowire) {
        CompositeImpl composite = new CompositeImpl(this, name);
        composite.setAutowire(autowire);
        return composite;
    }

    public ImplementationImpl introspect(Class<?> implClass) throws IntrospectionException {
        return classIntrospector.introspect(implClass);
    }

    public ImplementationImpl introspect(URL resource) throws IOException, IntrospectionException {
        return resourceIntrospector.introspect(resource);
    }
}
