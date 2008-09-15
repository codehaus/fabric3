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
package org.fabric3.jaxb.runtime.impl;

import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.jaxb.provision.ServiceTransformingInterceptorDefinition;
import org.fabric3.jaxb.runtime.spi.DataBindingTransformerFactory;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.fabric3.transform.PullTransformer;

/**
 * Builds a transforming interceptor for the service side of a wire.
 *
 * @version $Revision$ $Date$
 */
public class ServiceTransformingInterceptorBuilder
        extends AbstractTransformingInterceptorBuilder<ServiceTransformingInterceptorDefinition, TransformingInterceptor<?, ?>> {
    private Map<QName, DataBindingTransformerFactory<?>> factories;

    public ServiceTransformingInterceptorBuilder(@Reference ClassLoaderRegistry classLoaderRegistry) {
        super(classLoaderRegistry);
    }

    @Reference
    public void setFactories(Map<QName, DataBindingTransformerFactory<?>> factories) {
        this.factories = factories;
    }

    @SuppressWarnings({"unchecked"})
    protected TransformingInterceptor<?, ?> build(QName dataType, JAXBContext context, ClassLoader classLoader)
            throws TransformingBuilderException {
        DataBindingTransformerFactory<?> factory = factories.get(dataType);
        if (factory == null) {
            throw new TransformingBuilderException("No DataBindingTransformerFactory found for: " + dataType);
        }
        PullTransformer<?, Object> inTransformer = factory.createToJAXBTransformer(context);
        PullTransformer<Object, ?> outTransformer = factory.createFromJAXBTransformer(context);
        return new TransformingInterceptor(inTransformer, outTransformer, classLoader);
    }

}