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
package org.fabric3.binding.ws.metro.generator;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;

import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.util.UriHelper;
import org.fabric3.wsdl.contribution.PortSymbol;

/**
 * Default EndpointResolver implementation.
 *
 * @version $Rev$ $Date$
 */
public class EndpointResolverImpl implements EndpointResolver {
    private static final QName SOAP11_ADDRESS = new QName("http://schemas.xmlsoap.org/wsdl/soap/", "address");
    private static final QName SOAP12_ADDRESS = new QName("http://www.w3.org/2003/05/soap/bindings/HTTP/", "address");
    private MetaDataStore store;

    public EndpointResolverImpl(@Reference MetaDataStore store) {
        this.store = store;
    }

    public ServiceEndpointDefinition resolveServiceEndpoint(QName deployable, URI wsdlElement, URL wsdlLocation) throws EndpointResolutionException {
        return resolveServiceEndpoint(deployable, wsdlElement, wsdlLocation, null);
    }

    public ServiceEndpointDefinition resolveServiceEndpoint(QName deployable, URI wsdlElement, URL wsdlLocation, URI uri)
            throws EndpointResolutionException {
        String namespace = UriHelper.getDefragmentedNameAsString(wsdlElement);
        String fragment = wsdlElement.getFragment();

        if (fragment.startsWith("wsdl.port(")) {
            String name = fragment.substring(10, fragment.length() - 1); // wsdl.port(servicename/portname)
            String[] tokens = name.split("/");
            if (tokens.length != 2) {
                throw new EndpointResolutionException("Invalid wsdlElement expression: " + fragment);
            }
            QName serviceName = new QName(namespace, tokens[0]);
            QName portName = new QName(namespace, tokens[1]);
            if (wsdlLocation != null) {
                return resolveServicePort(serviceName, portName, wsdlLocation, uri);
            } else {
                return resolveServicePort(serviceName, portName, deployable, uri);
            }
        } else {
            throw new EndpointResolutionException("Expression not supported: " + fragment);
        }
    }

    public ReferenceEndpointDefinition resolveReferenceEndpoint(QName deployable, URI wsdlElement, URL wsdlLocation)
            throws EndpointResolutionException {
        String namespace = UriHelper.getDefragmentedNameAsString(wsdlElement);
        String fragment = wsdlElement.getFragment();

        if (fragment.startsWith("wsdl.port(")) {
            String name = fragment.substring(10, fragment.length() - 1); // wsdl.port(servicename/portname)
            String[] tokens = name.split("/");
            if (tokens.length != 2) {
                throw new EndpointResolutionException("Invalid wsdlElement expression: " + fragment);
            }
            QName serviceName = new QName(namespace, tokens[0]);
            QName portName = new QName(namespace, tokens[1]);
            if (wsdlLocation != null) {
                return resolveReferencePort(serviceName, portName, wsdlLocation);
            } else {
                return resolveReferencePort(serviceName, portName, deployable);
            }
        } else {
            throw new EndpointResolutionException("Expression not supported: " + fragment);
        }
    }

    private ServiceEndpointDefinition resolveServicePort(QName serviceName, QName portName, URL wsdlLocation, URI uri)
            throws EndpointResolutionException {
        Port port = parseWsdl(serviceName, portName, wsdlLocation);
        URI servicePath;
        if (uri == null) {
            URL url = getAddress(port);
            servicePath = URI.create(url.getPath());
        } else {
            servicePath = uri;
        }
        return new ServiceEndpointDefinition(serviceName, portName, servicePath);
    }

    private ServiceEndpointDefinition resolveServicePort(QName serviceName, QName portName, QName deployable, URI uri)
            throws EndpointResolutionException {

        Contribution contribution = store.resolveContainingContribution(new QNameSymbol(deployable));
        URI contributionUri = contribution.getUri();
        List<Resource> resources = store.resolveResources(contributionUri);

        Port port = resolvePort(portName, resources);
        if (port == null) {
            throw new EndpointResolutionException("WSDL port not found: " + portName);
        }
        URL url = getAddress(port);
        URI servicePath;
        if (uri == null) {
            servicePath = URI.create(url.getPath());
        } else {
            servicePath = uri;
        }
        return new ServiceEndpointDefinition(serviceName, portName, servicePath);
    }

    private ReferenceEndpointDefinition resolveReferencePort(QName serviceName, QName portName, URL wsdlLocation) throws EndpointResolutionException {
        Port port = parseWsdl(serviceName, portName, wsdlLocation);
        URL url = getAddress(port);
        QName portTypeName = port.getBinding().getPortType().getQName();
        return new ReferenceEndpointDefinition(serviceName, false, portName, portTypeName, url);
    }

    private ReferenceEndpointDefinition resolveReferencePort(QName serviceName, QName portName, QName deployable)
            throws EndpointResolutionException {
        Contribution contribution = store.resolveContainingContribution(new QNameSymbol(deployable));
        URI contributionUri = contribution.getUri();
        List<Resource> resources = store.resolveResources(contributionUri);
        Port port = resolvePort(portName, resources);
        QName portTypeName = port.getBinding().getPortType().getQName();
        URL url = getAddress(port);
        return new ReferenceEndpointDefinition(serviceName, false, portName, portTypeName, url);
    }

    private URL getAddress(Port port) throws EndpointResolutionException {
        for (Object o : port.getExtensibilityElements()) {
            ExtensibilityElement element = (ExtensibilityElement) o;
            QName elementType = element.getElementType();
            if (SOAP11_ADDRESS.equals(elementType) || SOAP12_ADDRESS.equals(elementType)) {
                try {
                    return new URL(((SOAPAddress) element).getLocationURI());
                } catch (MalformedURLException e) {
                    throw new EndpointResolutionException("Invalid URL specified for port " + port.getName());
                }
            }
        }
        throw new EndpointResolutionException("SOAP address not found on port " + port.getName());
    }

    private Port parseWsdl(QName serviceName, QName portName, URL wsdlLocation) throws EndpointResolutionException {
        try {
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            reader.setFeature("javax.wsdl.verbose", false);
            // TODO add support for SCA-specific extensions
            reader.setExtensionRegistry(factory.newPopulatedExtensionRegistry());
            Definition definition = reader.readWSDL(wsdlLocation.toURI().toString());
            Service service = definition.getService(serviceName);
            if (service == null) {
                throw new EndpointResolutionException("Service " + serviceName + " not found in WSDL " + wsdlLocation);
            }
            return service.getPort(portName.getLocalPart());
        } catch (WSDLException e) {
            throw new EndpointResolutionException(e);
        } catch (URISyntaxException e) {
            throw new EndpointResolutionException(e);
        }
    }


    @SuppressWarnings({"unchecked"})
    private Port resolvePort(QName portName, List<Resource> resources) throws EndpointResolutionException {
        for (Resource resource : resources) {
            if ("text/wsdl+xml".equals(resource.getContentType())) {
                // resource type is a WSDL
                for (ResourceElement<?, ?> element : resource.getResourceElements()) {
                    if (element.getSymbol() instanceof PortSymbol) {
                        PortSymbol symbol = (PortSymbol) element.getSymbol();
                        if (portName.equals(symbol.getKey())) {
                            return (Port) element.getValue();
                        }
                    }
                }
            }
        }
        throw new EndpointResolutionException("Port not found: " + portName);
    }

}
