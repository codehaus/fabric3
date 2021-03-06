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
package org.fabric3.binding.ws.metro.runtime.wire;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.ws.WebServiceFeature;

import com.sun.xml.ws.api.BindingID;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.metro.provision.MetroWsdlSourceDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.binding.ws.metro.runtime.core.DocumentInvoker;
import org.fabric3.binding.ws.metro.runtime.core.EndpointConfiguration;
import org.fabric3.binding.ws.metro.runtime.core.EndpointException;
import org.fabric3.binding.ws.metro.runtime.core.EndpointService;
import org.fabric3.binding.ws.metro.runtime.core.F3Provider;
import org.fabric3.binding.ws.metro.runtime.policy.FeatureResolver;
import org.fabric3.binding.ws.metro.util.BindingIdResolver;
import org.fabric3.spi.artifact.ArtifactCache;
import org.fabric3.spi.artifact.CacheException;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.model.physical.PhysicalTargetDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Source wire attacher that provisions WSDL-based web service endpoints.
 *
 * @version $Rev$ $Date$
 */
public class MetroWsdlSourceWireAttacher extends AbstractMetroSourceWireAttacher<MetroWsdlSourceDefinition> {
    private FeatureResolver featureResolver;
    private BindingIdResolver bindingIdResolver;
    private ArtifactCache cache;

    public MetroWsdlSourceWireAttacher(@Reference FeatureResolver featureResolver,
                                       @Reference BindingIdResolver bindingIdResolver,
                                       @Reference EndpointService endpointService,
                                       @Reference ArtifactCache cache) {
        super(endpointService);
        this.featureResolver = featureResolver;
        this.bindingIdResolver = bindingIdResolver;
        this.cache = cache;
    }

    public void attach(MetroWsdlSourceDefinition source, PhysicalTargetDefinition target, Wire wire) throws WiringException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            ServiceEndpointDefinition endpointDefinition = source.getEndpointDefinition();
            QName serviceName = endpointDefinition.getServiceName();
            QName portName = endpointDefinition.getPortName();
            URI servicePath = endpointDefinition.getServicePath();
            List<InvocationChain> invocationChains = wire.getInvocationChains();
            List<QName> requestedIntents = source.getIntents();

            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());

            BindingID bindingId = bindingIdResolver.resolveBindingId(requestedIntents);
            WebServiceFeature[] features = featureResolver.getFeatures(requestedIntents);

            String path = servicePath.toString();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            String wsdl = source.getWsdl();
            URL wsdlLocation = cache.cache(servicePath, new ByteArrayInputStream(wsdl.getBytes()));
            List<URL> generatedSchemas = null;

            DocumentInvoker invoker = new DocumentInvoker(invocationChains);
            EndpointConfiguration configuration = new EndpointConfiguration(F3Provider.class,
                                                                            serviceName,
                                                                            portName,
                                                                            path,
                                                                            wsdlLocation,
                                                                            invoker,
                                                                            features,
                                                                            bindingId,
                                                                            null,
                                                                            generatedSchemas);
            endpointService.registerService(configuration);
        } catch (CacheException e) {
            throw new WiringException(e);
        } catch (EndpointException e) {
            throw new WiringException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    @Override
    public void detach(MetroWsdlSourceDefinition source, PhysicalTargetDefinition target) throws WiringException {
        try {
            URI servicePath = source.getEndpointDefinition().getServicePath();
            cache.remove(servicePath);
        } catch (CacheException e) {
            throw new WiringException(e);
        }
    }

}