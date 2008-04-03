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
package org.fabric3.sandbox.introspection;

import org.fabric3.introspection.java.ImplementationProcessor;
import org.fabric3.introspection.xml.Loader;
import org.fabric3.scdl.Implementation;
import org.fabric3.scdl.Composite;
import org.fabric3.spi.assembly.ActivateException;

/**
 * @version $Rev$ $Date$
 */
public interface IntrospectionFactory {

    /**
     * Returns a Loader that can be used to introspect XML definitions such as composite files.
     *
     * @return a Loader that can be used to introspect XML definitions
     */
    Loader getLoader();

    /**
     * Returns an ImplementationProcessor that can used used to introspect an implementation.
     *
     * @param implementationType the type of implementation that will be introspected
     * @param <I>                the model object for the implementation
     * @param <IP>               a specific implementation processor type
     * @return an implementation processor for that type of implementation
     */
    <I extends Implementation<?>, IP extends ImplementationProcessor<I>> IP getImplementationProcessor(Class<I> implementationType);

    /**
     * Initialize the domain context against which validation should be performed.
     *
     * @param context the model of the domain
     * @throws ActivateException if there was a problem activating the model
     */
    void initializeContext(Composite context) throws ActivateException;

    /**
     * Validate the attempt to include a composite into a domain.
     *
     * The supplied composite definition is validated against the domain context. The domain context is not modified.
     *
     * @param include a composite to validate for inclusion in the domain context
     * @throws ActivateException if there was a problem including the composite
     */
    void validate(Composite include) throws ActivateException;
}
