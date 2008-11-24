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
package org.fabric3.policy.interceptor.simple;

import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.wire.Interceptor;

/**
 * Builder for simple interceptors.
 * <p/>
 * TODO Not sure whether the loader will have the class definition, probably not, the interceptor builder will need the classloader passed in.
 *
 * @version $Revision$ $Date$
 */
public class SimpleInterceptorBuilder implements InterceptorBuilder<SimpleInterceptorDefinition, Interceptor> {

    public SimpleInterceptorBuilder() {
    }

    public Interceptor build(SimpleInterceptorDefinition definition) throws BuilderException {

        String className = definition.getInterceptorClass();

        try {
            @SuppressWarnings("unchecked")
            Class<Interceptor> interceptorClass = (Class<Interceptor>) Class.forName(className);
            return interceptorClass.newInstance();
        } catch (InstantiationException ex) {
            throw new SimpleInterceptorBuilderException("Unable to instantiate", className, ex);
        } catch (IllegalAccessException ex) {
            throw new SimpleInterceptorBuilderException("Cannot access class or constructor", className, ex);
        } catch (ClassNotFoundException ex) {
            throw new SimpleInterceptorBuilderException("Class not found", className, ex);
        }

    }

}
