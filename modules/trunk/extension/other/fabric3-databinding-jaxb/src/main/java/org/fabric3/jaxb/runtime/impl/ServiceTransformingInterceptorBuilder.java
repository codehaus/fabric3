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
package org.fabric3.jaxb.runtime.impl;

import java.util.Map;
import javax.xml.bind.JAXBContext;

import org.osoa.sca.annotations.Reference;

import org.fabric3.jaxb.provision.ServiceTransformingInterceptorDefinition;
import org.fabric3.jaxb.runtime.spi.DataBindingTransformerFactory;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.transform.PullTransformer;

/**
 * Builds a transforming interceptor for the service side of a wire.
 *
 * @version $Revision$ $Date$
 */
public class ServiceTransformingInterceptorBuilder
        extends AbstractTransformingInterceptorBuilder<ServiceTransformingInterceptorDefinition, TransformingInterceptor<?, ?>> {
    private Map<String, DataBindingTransformerFactory<?>> factories;

    public ServiceTransformingInterceptorBuilder(@Reference ClassLoaderRegistry classLoaderRegistry) {
        super(classLoaderRegistry);
    }

    @Reference
    public void setFactories(Map<String, DataBindingTransformerFactory<?>> factories) {
        this.factories = factories;
    }

    @SuppressWarnings({"unchecked"})
    protected TransformingInterceptor<?, ?> build(String encoding, JAXBContext context, ClassLoader classLoader)
            throws TransformingBuilderException {
        DataBindingTransformerFactory<?> factory = factories.get(encoding);
        if (factory == null) {
            throw new TransformingBuilderException("No DataBindingTransformerFactory found for: " + encoding);
        }
        PullTransformer<?, Object> inTransformer = factory.createToJAXBTransformer(context);
        PullTransformer<Object, ?> outTransformer = factory.createFromJAXBTransformer(context);
        return new TransformingInterceptor(inTransformer, outTransformer, classLoader);
    }

}