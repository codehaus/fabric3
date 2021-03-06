/*
* Fabric3
* Copyright (c) 2009-2012 Metaform Systems
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
package org.fabric3.implementation.java.introspection;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.model.type.component.Scope;
import org.fabric3.spi.introspection.ImplementationNotFoundException;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.IntrospectionHelper;
import org.fabric3.spi.introspection.java.InvalidImplementation;
import org.fabric3.spi.introspection.java.annotation.ClassVisitor;
import org.fabric3.spi.model.type.java.InjectingComponentType;

/**
 * @version $Rev$ $Date$
 */
public class JavaImplementationProcessorImpl implements JavaImplementationProcessor {
    private final ClassVisitor classVisitor;
    private final HeuristicProcessor heuristic;
    private final IntrospectionHelper helper;

    public JavaImplementationProcessorImpl(@Reference(name = "classVisitor") ClassVisitor classVisitor,
                                           @Reference(name = "heuristic") HeuristicProcessor heuristic,
                                           @Reference(name = "helper") IntrospectionHelper helper) {
        this.classVisitor = classVisitor;
        this.heuristic = heuristic;
        this.helper = helper;
    }

    public InjectingComponentType introspect(String className, IntrospectionContext context) {
        InjectingComponentType componentType = new InjectingComponentType(className);
        componentType.setScope("STATELESS");

        ClassLoader cl = context.getClassLoader();

        Class<?> implClass;
        try {
            implClass = helper.loadClass(className, cl);
        } catch (ImplementationNotFoundException e) {
            Throwable cause = e.getCause();
            if (cause instanceof ClassNotFoundException || cause instanceof NoClassDefFoundError) {
                // CNFE and NCDFE may be thrown as a result of a referenced class not being on the classpath
                // If this is the case, ensure the correct class name is reported, not just the implementation 
                context.addError(new ImplementationArtifactNotFound(className, e.getCause().getMessage()));
            } else {
                context.addError(new ImplementationArtifactNotFound(className));
            }
            return componentType;
        }
        if (implClass.isInterface()) {
            InvalidImplementation failure = new InvalidImplementation("Implementation class is an interface", className);
            context.addError(failure);
            return componentType;
        }

        TypeMapping mapping = context.getTypeMapping(implClass);
        if (mapping == null) {
            mapping = new TypeMapping();
            context.addTypeMapping(implClass, mapping);
            helper.resolveTypeParameters(implClass, mapping);
        }

        try {
            classVisitor.visit(componentType, implClass, context);
            heuristic.applyHeuristics(componentType, implClass, context);
        } catch (NoClassDefFoundError e) {
            // May be thrown as a result of a referenced class not being on the classpath
            context.addError(new ImplementationArtifactNotFound(className, e.getMessage()));
        }
        validateScope(componentType, implClass, context);
        return componentType;

    }

    private void validateScope(InjectingComponentType componentType, Class<?> implClass, IntrospectionContext context) {
        String scope = componentType.getScope();
        if (componentType.isEagerInit() && !Scope.COMPOSITE.getScope().equals(scope) && !Scope.DOMAIN.getScope().equals(scope)) {
            EagerInitNotSupported warning = new EagerInitNotSupported(implClass);
            context.addWarning(warning);
        }
    }

}
