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
package org.fabric3.rs.introspection;

import javax.ws.rs.Path;
import javax.ws.rs.ext.Provider;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.IntrospectionHelper;
import org.fabric3.introspection.java.ImplementationNotFoundException;
import org.fabric3.rs.scdl.RsBindingDefinition;
import org.osoa.sca.annotations.Reference;

/**
 * This would better have been implemented as a custom ImplementationProcessor/Hueristic
 * but then it would have limited reuse of the Java Implementation extension
 * without adding much new functionality
 * @version $Rev$ $Date$
 */
public class RsHeuristicImpl implements RsHeuristic {

    private final IntrospectionHelper helper;

    public RsHeuristicImpl(@Reference IntrospectionHelper helper) {
        this.helper = helper;
    }

    public void applyHeuristics(RsBindingDefinition definition, String implClassName, IntrospectionContext context) {

        ClassLoader cl = context.getTargetClassLoader();

        Class<?> implClass;
        try {
            implClass = helper.loadClass(implClassName, cl);
        } catch (ImplementationNotFoundException e) {
            //This should have already been recorded
            return;
        }
        Path path = implClass.getAnnotation(Path.class);
        if (path != null) {
            definition.setIsResource(true);
        }

        Provider provider = implClass.getAnnotation(Provider.class);
        if (provider != null) {
            definition.setIsProvider(true);
        }

        if (!definition.isResource() && !definition.isProvider()) {
            context.addError(new InvalidRsClass(implClass));
        }
    }
}
