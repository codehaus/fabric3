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
package org.fabric3.binding.ws.metro.wsdl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.resources.ClientMessages;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Init;
import org.osoa.sca.annotations.Reference;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import org.fabric3.api.annotation.Monitor;
import org.fabric3.binding.ws.metro.MetroBindingMonitor;
import org.fabric3.host.contribution.InstallException;
import org.fabric3.spi.contribution.Contribution;
import org.fabric3.spi.contribution.ProcessorRegistry;
import org.fabric3.spi.contribution.Resource;
import org.fabric3.spi.contribution.ResourceElement;
import org.fabric3.spi.contribution.ResourceProcessor;
import org.fabric3.spi.contribution.manifest.QNameSymbol;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.xml.XMLFactory;

/**
 * Indexes and processes a WSDL document in a contribution. This implementation uses the Metro WSDL model to represent the document.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class WsdlResourceProcessor implements ResourceProcessor {
    private static final QName DEFINITIONS = new QName("http://schemas.xmlsoap.org/wsdl", "definitions");
    private static final String MIME_TYPE = "text/wsdl+xml";

    private static final EntityResolver RESOLVER = new NullResolver();

    private ProcessorRegistry registry;
    private MetroBindingMonitor monitor;
    private XMLInputFactory xmlFactory;

    public WsdlResourceProcessor(@Reference ProcessorRegistry registry, @Reference XMLFactory xmlFactory, @Monitor MetroBindingMonitor monitor) {
        this.registry = registry;
        this.monitor = monitor;
        this.xmlFactory = xmlFactory.newInputFactoryInstance();
    }

    @Init
    public void init() {
        registry.register(this);
    }

    public String getContentType() {
        return MIME_TYPE;
    }

    public QName getType() {
        return DEFINITIONS;
    }

    public void index(Contribution contribution, URL url, IntrospectionContext context) throws InstallException {
        XMLStreamReader reader = null;
        InputStream stream = null;
        try {
            stream = url.openStream();
            reader = xmlFactory.createXMLStreamReader(stream);
            reader.nextTag();

            String targetNamespace = reader.getAttributeValue(null, "targetNamespace");
            if (targetNamespace == null) {
                context.addError(new MissingAttribute("Target namespace not specified", reader));
                return;
            }

            String name = reader.getAttributeValue(null, "name");
            if (name == null) {
                // synthesize a name
                name = url.toString();
            }

            QNameSymbol symbol = new QNameSymbol(new QName(targetNamespace, name));
            ResourceElement<QNameSymbol, WSDLModel> element = new ResourceElement<QNameSymbol, WSDLModel>(symbol);
            Resource resource = new Resource(url, MIME_TYPE);
            resource.addResourceElement(element);
            contribution.addResource(resource);
        } catch (XMLStreamException e) {
            throw new InstallException(e);
        } catch (IOException e) {
            throw new InstallException(e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }

    }


    @SuppressWarnings({"unchecked"})
    public void process(URI contributionUri, Resource resource, IntrospectionContext context, ClassLoader loader) throws InstallException {
        InputStream stream = null;
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        URL wsdlLocation = resource.getUrl();
        try {
            // The JAX-WS RI dynamically loads classes using TCCL
            Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            stream = resource.getUrl().openStream();
            StreamSource source = new StreamSource(stream);

            // delegate to the Metro WSDL parser
            WSDLModel model = RuntimeWSDLParser.parse(wsdlLocation, source, RESOLVER, false, null);
            ResourceElement<QNameSymbol, WSDLModel> element = (ResourceElement<QNameSymbol, WSDLModel>) resource.getResourceElements().get(0);
            element.setValue(model);
        } catch (WebServiceException e) {
            String message = ClientMessages.WSDL_CONTAINS_NO_SERVICE(wsdlLocation);
            if (message.equals(e.getMessage())) {
                monitor.wsdlSkipped(wsdlLocation);
                return;
            }
            throw e;
        } catch (XMLStreamException e) {
            throw new InstallException(e);
        } catch (IOException e) {
            throw new InstallException(e);
        } catch (SAXException e) {
            throw new InstallException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    private static class NullResolver implements EntityResolver {

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            return null;
        }
    }
}
