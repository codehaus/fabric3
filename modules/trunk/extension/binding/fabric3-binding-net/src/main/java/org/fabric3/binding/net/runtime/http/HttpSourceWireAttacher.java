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
package org.fabric3.binding.net.runtime.http;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.net.provision.HttpWireSourceDefinition;
import org.fabric3.binding.net.runtime.TransportService;
import org.fabric3.binding.net.runtime.WireHolder;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.binding.format.ParameterEncoder;
import org.fabric3.spi.binding.format.ParameterEncoderFactory;
import org.fabric3.spi.binding.format.EncoderException;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.SourceWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * Attaches services to an HTTP channel.
 *
 * @version $Revision$ $Date$
 */
public class HttpSourceWireAttacher implements SourceWireAttacher<HttpWireSourceDefinition> {
    private TransportService service;
    private ClassLoaderRegistry classLoaderRegistry;
    private Map<String, ParameterEncoderFactory> formatterFactories = new HashMap<String, ParameterEncoderFactory>();

    public HttpSourceWireAttacher(@Reference TransportService service, @Reference ClassLoaderRegistry classLoaderRegistry) {
        this.service = service;
        this.classLoaderRegistry = classLoaderRegistry;
    }

    @Reference
    public void setFormatterFactories(Map<String, ParameterEncoderFactory> formatterFactories) {
        this.formatterFactories = formatterFactories;
    }

    public void attachToSource(HttpWireSourceDefinition source, PhysicalWireTargetDefinition target, Wire wire) throws WiringException {
        URI uri = source.getUri();
        if (uri.getScheme() != null) {
            throw new WiringException("Absolute URIs not supported: " + uri);
        }
        String sourceUri = uri.toString();
        String callbackUri = null;
        if (target.getCallbackUri() != null) {
            callbackUri = target.getCallbackUri().toString();
        }
        String wireFormat = source.getConfig().getWireFormat();
        if (wireFormat == null) {
            wireFormat = "jaxb";
        }
        ParameterEncoderFactory formatterFactory = formatterFactories.get(wireFormat);
        if (formatterFactory == null) {
            throw new WiringException("WireFormatterFactory not found for: " + wireFormat);
        }
        URI id = source.getClassLoaderId();
        ClassLoader loader = classLoaderRegistry.getClassLoader(id);
        WireHolder wireHolder = createWireHolder(wire, callbackUri, formatterFactory, loader);
        service.registerHttp(sourceUri, wireHolder);
    }

    public void detachFromSource(HttpWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        service.unregisterHttp(source.getUri().toString());
    }

    public void attachObjectFactory(HttpWireSourceDefinition source, ObjectFactory<?> objectFactory, PhysicalWireTargetDefinition target)
            throws WiringException {
        throw new UnsupportedOperationException();
    }

    public void detachObjectFactory(HttpWireSourceDefinition source, PhysicalWireTargetDefinition target) throws WiringException {
        throw new UnsupportedOperationException();
    }

    private WireHolder createWireHolder(Wire wire, String callbackUri, ParameterEncoderFactory formatterFactory, ClassLoader loader)
            throws WiringException {
        try {
            List<InvocationChain> chains = wire.getInvocationChains();
            ParameterEncoder formatter = formatterFactory.getInstance(wire, loader);
            return new WireHolder(chains, formatter, callbackUri);
        } catch (EncoderException e) {
            throw new WiringException(e);
        }
    }

}
