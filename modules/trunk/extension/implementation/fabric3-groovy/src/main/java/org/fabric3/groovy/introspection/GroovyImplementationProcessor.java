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
package org.fabric3.groovy.introspection;

import java.io.IOException;
import java.net.URL;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyCodeSource;
import org.osoa.sca.annotations.Reference;

import org.fabric3.groovy.scdl.GroovyImplementation;
import org.fabric3.model.type.java.InjectingComponentType;
import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.ImplementationNotFoundException;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.annotation.ClassWalker;
import org.fabric3.spi.introspection.java.annotation.HeuristicProcessor;
import org.fabric3.spi.introspection.java.annotation.ImplementationProcessor;
import org.fabric3.spi.introspection.java.annotation.MissingResource;

/**
 * @version $Rev$ $Date$
 */
public class GroovyImplementationProcessor implements ImplementationProcessor<GroovyImplementation> {

    private final ClassWalker<GroovyImplementation> classWalker;
    private final HeuristicProcessor<GroovyImplementation> heuristic;
    private final IntrospectionHelper helper;

    public GroovyImplementationProcessor(@Reference(name = "classWalker") ClassWalker<GroovyImplementation> classWalker,
                                         @Reference(name = "heuristic") HeuristicProcessor<GroovyImplementation> heuristic,
                                         @Reference(name = "helper") IntrospectionHelper helper) {
        this.classWalker = classWalker;
        this.heuristic = heuristic;
        this.helper = helper;
    }

    public void introspect(GroovyImplementation implementation, IntrospectionContext context) {

        Class<?> implClass;
        try {
            implClass = loadImplementation(implementation, context);
        } catch (ClassNotFoundException e) {
            context.addError(new MissingResource("Groovy class not found: ", implementation.getClassName()));
            return;
        } catch (ImplementationNotFoundException e) {
            context.addError(new MissingResource("Groovy script not found: ", implementation.getScriptName()));
            return;
        } catch (IOException e) {
            context.addError(new InvalidGroovySource(implementation.getScriptName(), e));
            return;
        }
        if (implClass == null) {
            return;
        }
        InjectingComponentType componentType = new InjectingComponentType(implClass.getName());
        componentType.setScope("STATELESS");
        implementation.setComponentType(componentType);

        TypeMapping typeMapping = helper.mapTypeParameters(implClass);

        IntrospectionContext childContext = new DefaultIntrospectionContext(context, typeMapping);
        classWalker.walk(implementation, implClass, childContext);

        heuristic.applyHeuristics(implementation, implClass, childContext);
        if (childContext.hasErrors()) {
            context.addErrors(childContext.getErrors());
        }
        if (childContext.hasWarnings()) {
            context.addWarnings(childContext.getWarnings());
        }
    }


    /**
     * Introspects the implementation artiact.
     *
     * @param implementation the artifact to introspect
     * @param context        the context where errors are reported
     * @return the loaded implementation class, or null if there was an error introspecting it. Errors will be reported in the IntrospectionContext
     * @throws ClassNotFoundException if the class was not on the classpath
     * @throws java.io.IOException    if there was an error reading the Groovy script file
     * @throws ImplementationNotFoundException
     *                                if the Groovy script file is not found
     */
    private Class<?> loadImplementation(GroovyImplementation implementation, IntrospectionContext context)
            throws ClassNotFoundException, ImplementationNotFoundException, IOException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            // Set TCCL to the extension classloader as implementations may need access to Groovy classes. Also, Groovy
            // dependencies such as Antlr use the TCCL.
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            GroovyClassLoader gcl = new GroovyClassLoader(context.getTargetClassLoader());

            // if user supplied a class name, use it as the implementation
            String className = implementation.getClassName();
            if (className != null) {
                return gcl.loadClass(className);
            }

            // if user supplied a script name, compile it and use the resulting class as the implementation
            String scriptName = implementation.getScriptName();
            if (scriptName != null) {
                URL scriptURL = gcl.getResource(scriptName);
                if (scriptURL == null) {
                    throw new ImplementationNotFoundException(scriptName);
                }
                GroovyCodeSource codeSource = new GroovyCodeSource(scriptURL);
                return gcl.parseClass(codeSource);
            }
            // we should not have been called without an implementation artifact
            throw new AssertionError();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }
}
