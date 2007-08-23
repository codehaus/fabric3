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

import org.scdl4j.Component;

import org.fabric3.scdl4j.spi.IntrospectionException;

/**
 * @version $Rev$ $Date$
 */
public class ComponentImpl implements Component {
    private final SCDL4JImpl scdl4j;
    private final String name;
    private ImplementationImpl implementation;

    public ComponentImpl(SCDL4JImpl scdl4j, String name) {
        this.scdl4j = scdl4j;
        this.name = name;
    }

    public Component implementedBy(Class<?> implementation) {
        try {
            this.implementation = scdl4j.introspect(implementation);
        } catch (IntrospectionException e) {
            throw new SCDL4JException(e);
        }
        return this;
    }

    public Component implementedBy(URL resource) {
        try {
            this.implementation = scdl4j.introspect(resource);
        } catch (IOException e) {
            throw new SCDL4JException(e);
        } catch (IntrospectionException e) {
            throw new SCDL4JException(e);
        }
        return this;
    }

    public Component setProperty(String name, Object value) {
        return this;
    }

    public Component referencing(String name, String... targets) {
        return this;
    }

    public String getName() {
        return name;
    }

    public ImplementationImpl getImplementation() {
        return implementation;
    }
}
