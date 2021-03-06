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
package org.fabric3.binding.ws.metro.generator.resolver;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.host.contribution.StoreException;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.wsdl.contribution.PortSymbol;
import org.fabric3.wsdl.contribution.WsdlSymbol;
import org.fabric3.wsdl.factory.Wsdl4JFactory;

/**
 * Resolves parsed WSDLs against an external location or those visible to the current contribution installed in the domain.
 *
 * @version $Rev$ $Date$
 */
public class WsdlResolverImpl implements WsdlResolver {
    private MetaDataStore store;
    private Wsdl4JFactory wsdlFactory;

    public WsdlResolverImpl(@Reference MetaDataStore store, @Reference Wsdl4JFactory wsdlFactory) throws WSDLException {
        this.store = store;
        this.wsdlFactory = wsdlFactory;
    }

    public Definition parseWsdl(URL wsdlLocation) throws WsdlResolutionException {
        try {
            WSDLReader reader = wsdlFactory.newReader();
            return reader.readWSDL(wsdlLocation.toURI().toString());
        } catch (WSDLException e) {
            throw new WsdlResolutionException(e);
        } catch (URISyntaxException e) {
            throw new WsdlResolutionException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    public Definition resolveWsdl(URI contributionUri, QName wsdlName) throws WsdlResolutionException {
        WsdlSymbol symbol = new WsdlSymbol(wsdlName);
        try {
            ResourceElement<WsdlSymbol, Definition> element = store.find(contributionUri, Definition.class, symbol);
            if (element == null) {
                throw new WsdlResolutionException("WSDL not found: " + wsdlName);
            }
            return element.getValue();
        } catch (StoreException e) {
            throw new WsdlResolutionException(e);
        }
    }

    public Definition resolveWsdlByPortName(URI contributionUri, QName portName) throws WsdlResolutionException {
        PortSymbol symbol = new PortSymbol(portName);
        ResourceElement<PortSymbol, Port> resourceElement;
        try {
            resourceElement = store.find(contributionUri, Port.class, symbol);
        } catch (StoreException e) {
            throw new WsdlResolutionException("Error resolving port: " + portName, e);
        }
        if (resourceElement == null) {
            throw new WsdlResolutionException("WSDL port not found: " + portName);
        }
        Resource resource = resourceElement.getResource();
        for (ResourceElement<?, ?> element : resource.getResourceElements()) {
            if (element.getSymbol() instanceof WsdlSymbol) {
                return (Definition) element.getValue();
            }
        }
        throw new WsdlResolutionException("WSDL for port not found: " + portName);
    }

}