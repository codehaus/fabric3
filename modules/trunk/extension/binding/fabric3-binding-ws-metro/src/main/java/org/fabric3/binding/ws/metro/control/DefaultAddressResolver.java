/*
 * Fabric3
 * Copyright (C) 2009 Metaform Systems
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
package org.fabric3.binding.ws.metro.control;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;

import org.fabric3.binding.ws.provision.WsdlElement;
import org.fabric3.spi.generator.GenerationException;

/**
 * Default implementation of the address resolvers.
 *
 */
public class DefaultAddressResolver implements AddressResolver {
    
    /**
     * Resolves the address on which the service is provisioned.
     * 
     * @param targetUri Target URI specified on the service binding.
     * @param wsdlElement WSDL element containing the service and port name.
     * @param wsdlModel Model object containing the WSDL information.
     * @return URI on which the service is provisioned.
     * 
     * @throws GenerationException If unable to resolve the address.
     */
    public URI resolveServiceAddress(URI targetUri, WsdlElement wsdlElement, WSDLModel wsdlModel) throws GenerationException {
        
        URI uri = getUri(targetUri, wsdlElement, wsdlModel);
        
        if (!uri.toASCIIString().startsWith("/")) {
            throw new GenerationException("Service URIs should be relative");
        }
        return uri;
        
    }
    
    /**
     * Resolves the address on which the service is provisioned.
     * 
     * @param targetUri Target URI specified on the reference binding.
     * @param wsdlElement WSDL element containing the service and port name.
     * @param wsdlModel Model object containing the WSDL information.
     * @return List of URLs on which the service can be invoked..
     * 
     * @throws GenerationException If unable to resolve the address.
     */
    public URL[] resolveReferenceAddress(URI targetUri, WsdlElement wsdlElement, WSDLModel wsdlModel) throws GenerationException {
        
        URI uri = getUri(targetUri, wsdlElement, wsdlModel);
        
        URL[] referenceAddresses = null;
        
        StringTokenizer stringTokenizer = new StringTokenizer(uri.toASCIIString(), ",");
        referenceAddresses = new URL[stringTokenizer.countTokens()];
        for (int i = 0; i < referenceAddresses.length;i++) {
            try {
                referenceAddresses[i] = new URL(stringTokenizer.nextToken());
            } catch (MalformedURLException e) {
                throw new GenerationException(e);
            }
        }
        
        return referenceAddresses;
        
    }

    /*
     * Gets the URI.
     */
    @SuppressWarnings("deprecation")
    private URI getUri(URI targetUri, WsdlElement wsdlElement, WSDLModel wsdlModel) throws GenerationException {
        
        if (targetUri != null) {
            return URI.create(URLDecoder.decode(targetUri.toASCIIString()));
        } else if (wsdlModel!= null) {
            WSDLService wsdlService = wsdlModel.getService(wsdlElement.getServiceName());
            WSDLPort wsdlPort = wsdlService.get(wsdlElement.getPortName());
            return wsdlPort.getAddress().getURI();
        } else {
            throw new GenerationException("Either target URI or wsdlLocation should be specified");
        }
        
    }

}
