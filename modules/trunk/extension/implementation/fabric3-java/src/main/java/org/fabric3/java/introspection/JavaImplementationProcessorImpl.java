/*
* Fabric3
* Copyright (c) 2009 Metaform Systems
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
package org.fabric3.java.introspection;

import org.osoa.sca.annotations.Reference;

import org.fabric3.java.introspection.JavaImplementationProcessor;
import org.fabric3.java.model.JavaImplementation;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.ImplementationNotFoundException;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.annotation.ClassWalker;
import org.fabric3.spi.introspection.java.annotation.HeuristicProcessor;
import org.fabric3.spi.introspection.java.annotation.InvalidImplementation;

/**
 * @version $Rev$ $Date$
 */
public class JavaImplementationProcessorImpl implements JavaImplementationProcessor {
    private final ClassWalker<JavaImplementation> classWalker;
    private final HeuristicProcessor<JavaImplementation> heuristic;
    private final IntrospectionHelper helper;

    public JavaImplementationProcessorImpl(@Reference(name = "classWalker") ClassWalker<JavaImplementation> classWalker,
                                           @Reference(name = "heuristic") HeuristicProcessor<JavaImplementation> heuristic,
                                           @Reference(name = "helper") IntrospectionHelper helper) {
        this.classWalker = classWalker;
        this.heuristic = heuristic;
        this.helper = helper;
    }

    public void introspect(JavaImplementation implementation, IntrospectionContext context) {
        String implClassName = implementation.getImplementationClass();
        InjectingComponentType componentType = new InjectingComponentType(implClassName);
        componentType.setScope("STATELESS");
        implementation.setComponentType(componentType);

        ClassLoader cl = context.getTargetClassLoader();

        Class<?> implClass;
        try {
            implClass = helper.loadClass(implClassName, cl);
        } catch (ImplementationNotFoundException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ClassNotFoundException || cause instanceof NoClassDefFoundError) {
                // CNFE and NCDFE may be thrown as a result of a referenced class not being on the classpath
                // If this is the case, ensure the correct class name is reported, not just the implementation 
                context.addError(new ImplementationArtifactNotFound(implClassName, e.getCause().getMessage()));
            } else {
                context.addError(new ImplementationArtifactNotFound(implClassName));
            }
            return;
        }
        if (implClass.isInterface()) {
            InvalidImplementation failure = new InvalidImplementation("Implementation class is an interface", implClassName);
            context.addError(failure);
            return;
        }
        TypeMapping typeMapping = helper.mapTypeParameters(implClass);

        IntrospectionContext newContext = new DefaultIntrospectionContext(context, typeMapping);
        classWalker.walk(implementation, implClass, newContext);

        heuristic.applyHeuristics(implementation, implClass, newContext);
        if (newContext.hasErrors()) {
            context.addErrors(newContext.getErrors());
        }
        if (newContext.hasWarnings()) {
            context.addWarnings(newContext.getWarnings());
        }

    }

}
