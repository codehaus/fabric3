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
package org.fabric3.hessian.runtime;

import java.net.URI;

import com.caucho.hessian.io.SerializerFactory;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.hessian.provision.HessianReferenceInterceptorDefinition;
import org.fabric3.spi.builder.BuilderException;
import org.fabric3.spi.builder.interceptor.InterceptorBuilder;
import org.fabric3.spi.classloader.ClassLoaderRegistry;

/**
 * Creates HessianReferenceInterceptor instances.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class HessianReferenceInterceptorBuilder implements InterceptorBuilder<HessianReferenceInterceptorDefinition, HessianReferenceInterceptor> {
    private ClassLoaderRegistry registry;
    private SerializerFactory serializerFactory;

    public HessianReferenceInterceptorBuilder(@Reference ClassLoaderRegistry registry) {
        this.registry = registry;
        this.serializerFactory = new SerializerFactory();
    }

    public HessianReferenceInterceptor build(HessianReferenceInterceptorDefinition definition) throws BuilderException {
        URI classloaderId = definition.getWireClassLoaderId();
        ClassLoader loader = registry.getClassLoader(classloaderId);
        return new HessianReferenceInterceptor(serializerFactory, loader);
    }
}