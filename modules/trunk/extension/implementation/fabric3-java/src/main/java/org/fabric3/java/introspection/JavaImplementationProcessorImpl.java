/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.java.introspection;

import java.net.URL;

import org.osoa.sca.annotations.Reference;

import org.fabric3.spi.introspection.DefaultIntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.IntrospectionHelper;
import org.fabric3.spi.introspection.TypeMapping;
import org.fabric3.spi.introspection.java.ClassWalker;
import org.fabric3.spi.introspection.java.HeuristicProcessor;
import org.fabric3.spi.introspection.java.ImplementationNotFoundException;
import org.fabric3.spi.introspection.xml.LoaderException;
import org.fabric3.java.scdl.JavaImplementation;
import org.fabric3.pojo.scdl.PojoComponentType;
import org.fabric3.scdl.validation.InvalidImplementation;

/**
 * @version $Rev$ $Date$
 */
public class JavaImplementationProcessorImpl implements JavaImplementationProcessor {
    private final ClassWalker<JavaImplementation> classWalker;
    private final HeuristicProcessor<JavaImplementation> heuristic;
    private final IntrospectionHelper helper;

    public JavaImplementationProcessorImpl(@Reference(name = "classWalker")ClassWalker<JavaImplementation> classWalker,
                                           @Reference(name = "heuristic")HeuristicProcessor<JavaImplementation> heuristic,
                                           @Reference(name = "helper") IntrospectionHelper helper) {
        this.classWalker = classWalker;
        this.heuristic = heuristic;
        this.helper = helper;
    }

    public void introspect(JavaImplementation implementation, IntrospectionContext context) {
        String implClassName = implementation.getImplementationClass();
        PojoComponentType componentType = new PojoComponentType(implClassName);
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

    PojoComponentType loadFromSidefile(URL url, IntrospectionContext introspectionContext) throws LoaderException {
        // FIXME we need to merge the loaded componentType information with the introspection result
        throw new UnsupportedOperationException();
/*
        PojoComponentType componentType = new PojoComponentType();
        return loader.load(url, PojoComponentType.class, introspectionContext);
*/
    }
}
