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

import groovy.lang.GroovyClassLoader;

import org.fabric3.fabric.component.ReflectiveInstanceFactoryProvider;
import org.fabric3.fabric.component.instancefactory.IFProviderBuilderException;
import org.fabric3.fabric.component.instancefactory.impl.ReflectiveIFProviderBuilder;

/**
 * @version $Rev$ $Date$
 */
public class GroovyInstanceFactoryProviderBuilder extends ReflectiveIFProviderBuilder<GroovyInstanceFactoryDefinition> {

    protected Class<?> getIfpdClass() {
        return GroovyInstanceFactoryDefinition.class;
    }

    public ReflectiveInstanceFactoryProvider<GroovyInstanceFactoryDefinition> build(GroovyInstanceFactoryDefinition ifpd, ClassLoader cl)
            throws IFProviderBuilderException {
        cl = new GroovyClassLoader(cl);
        return super.build(ifpd, cl);
    }
}
