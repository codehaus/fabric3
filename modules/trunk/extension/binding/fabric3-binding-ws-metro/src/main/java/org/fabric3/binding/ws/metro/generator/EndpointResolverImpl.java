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

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import org.osoa.sca.annotations.Reference;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.MetaDataStore;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.util.UriHelper;

/**
 * Default EndpointResolver implementation.
 *
 * @version $Rev$ $Date$
 */
public class EndpointResolverImpl implements EndpointResolver {
    private static final EntityResolver RESOLVER = new NullResolver();
    private MetaDataStore store;

    public EndpointResolverImpl(@Reference MetaDataStore store) {
        this.store = store;
    }

    public ServiceEndpointDefinition resolveServiceEndpoint(QName deployable, URI wsdlElement, URL wsdlLocation) throws EndpointResolutionException {
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
                return resolveServicePort(serviceName, portName, wsdlLocation);
            } else {
                return resolveServicePort(namespace, serviceName, portName, deployable);
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
                return resolveReferencePort(namespace, serviceName, portName, deployable);
            }
        } else {
            throw new EndpointResolutionException("Expression not supported: " + fragment);
        }
    }

    private ServiceEndpointDefinition resolveServicePort(QName serviceName, QName portName, URL wsdlLocation)
            throws EndpointResolutionException {
        try {
            WSDLPort port = parseWsdl(serviceName, portName, wsdlLocation);
            URL url = port.getAddress().getURL();
            URI servicePath = URI.create(url.getPath());
            return new ServiceEndpointDefinition(serviceName, portName, servicePath);
        } catch (IOException e) {
            throw new EndpointResolutionException(e);
        } catch (SAXException e) {
            throw new EndpointResolutionException(e);
        } catch (XMLStreamException e) {
            throw new EndpointResolutionException(e);
        }
    }

    private ServiceEndpointDefinition resolveServicePort(String namespace, QName serviceName, QName portName, QName deployable)
            throws EndpointResolutionException {

        Contribution contribution = store.resolveContainingContribution(new QNameSymbol(deployable));
        URI contributionUri = contribution.getUri();
        List<Resource> resources = store.resolveResources(contributionUri);

        WSDLPort port = resolvePort(namespace, serviceName, portName, resources);
        if (port == null) {
            throw new EndpointResolutionException("WSDL port not found: " + portName);
        }
        URL url = port.getAddress().getURL();
        URI servicePath = URI.create(url.getPath());
        return new ServiceEndpointDefinition(serviceName, portName, servicePath);
    }

    private ReferenceEndpointDefinition resolveReferencePort(QName serviceName, QName portName, URL wsdlLocation)
            throws EndpointResolutionException {
        try {
            WSDLPort port = parseWsdl(serviceName, portName, wsdlLocation);
            URL url = port.getAddress().getURL();
            QName portTypeName = port.getBinding().getPortTypeName();
            return new ReferenceEndpointDefinition(serviceName, false, portName, portTypeName, url);
        } catch (IOException e) {
            throw new EndpointResolutionException(e);
        } catch (SAXException e) {
            throw new EndpointResolutionException(e);
        } catch (XMLStreamException e) {
            throw new EndpointResolutionException(e);
        }
    }

    private ReferenceEndpointDefinition resolveReferencePort(String namespace, QName serviceName, QName portName, QName deployable)
            throws EndpointResolutionException {
        Contribution contribution = store.resolveContainingContribution(new QNameSymbol(deployable));
        URI contributionUri = contribution.getUri();
        List<Resource> resources = store.resolveResources(contributionUri);
        WSDLPort port = resolvePort(namespace, serviceName, portName, resources);
        URL url = port.getAddress().getURL();
        QName portTypeName = port.getBinding().getPortTypeName();
        return new ReferenceEndpointDefinition(serviceName, false, portName, portTypeName, url);
    }

    private WSDLPort parseWsdl(QName serviceName, QName portName, URL wsdlLocation)
            throws IOException, XMLStreamException, SAXException, EndpointResolutionException {
        InputStream stream = wsdlLocation.openStream();
        StreamSource source = new StreamSource(stream);
        WSDLModel model = RuntimeWSDLParser.parse(wsdlLocation, source, RESOLVER, false, null);

        return getPort(serviceName, portName, model);
    }

    @SuppressWarnings({"unchecked"})
    private WSDLPort resolvePort(String namespace, QName serviceName, QName portName, List<Resource> resources) throws EndpointResolutionException {
        for (Resource resource : resources) {
            if ("text/wsdl+xml".equals(resource.getContentType())) {
                // resource type is a WSDL
                ResourceElement<QNameSymbol, WSDLModel> element = (ResourceElement<QNameSymbol, WSDLModel>) resource.getResourceElements().get(0);
                if (namespace.equals(element.getSymbol().getKey().getNamespaceURI())) {
                    WSDLModel model = element.getValue();
                    return getPort(serviceName, portName, model);
                }
            }
        }
        return null;
    }

    private WSDLPort getPort(QName serviceName, QName portName, WSDLModel model) throws EndpointResolutionException {
        WSDLService service = model.getService(serviceName);
        if (service == null) {
            throw new EndpointResolutionException("WSDL service not found: " + serviceName);
        }
        for (WSDLPort port : service.getPorts()) {
            if (portName.equals(port.getName())) {
                return port;
            }
        }
        throw new EndpointResolutionException("WSDL port not found: " + portName);
    }

    private static class NullResolver implements EntityResolver {

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return null;
        }
    }

}
