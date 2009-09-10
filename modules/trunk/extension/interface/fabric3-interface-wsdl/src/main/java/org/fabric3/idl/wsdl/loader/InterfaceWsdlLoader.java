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
package org.fabric3.idl.wsdl.loader;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.idl.wsdl.processor.WsdlProcessor;
import org.fabric3.idl.wsdl.scdl.WsdlServiceContract;
import org.fabric3.spi.introspection.IntrospectionContext;
import org.fabric3.spi.introspection.xml.LoaderUtil;
import org.fabric3.spi.introspection.xml.MissingAttribute;
import org.fabric3.spi.introspection.xml.TypeLoader;

/**
 * Loader for interface.wsdl.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class InterfaceWsdlLoader implements TypeLoader<WsdlServiceContract> {
    private final WsdlProcessor processor;

    public InterfaceWsdlLoader(@Reference WsdlProcessor processor) {
        this.processor = processor;
    }

    public WsdlServiceContract load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        WsdlServiceContract wsdlContract = new WsdlServiceContract();

        URL wsdlUrl = resolveWsdl(reader, context);
        if (wsdlUrl == null) {
            // there was a problem, return an empty contract
            return wsdlContract;
        }
        processInterface(reader, wsdlContract, wsdlUrl, context);

        processCallbackInterface(reader, wsdlContract);

        LoaderUtil.skipToEndElement(reader);

        return wsdlContract;

    }

    @SuppressWarnings("unchecked")
    private void processCallbackInterface(XMLStreamReader reader, WsdlServiceContract wsdlContract) {

        String callbackInterfaze = reader.getAttributeValue(null, "callbackInterface");
        if (callbackInterfaze != null) {
            QName callbackInterfaceQName = getQName(callbackInterfaze);
            wsdlContract.setCallbackQname(callbackInterfaceQName);
        }

    }

    @SuppressWarnings("unchecked")
    private void processInterface(XMLStreamReader reader, WsdlServiceContract wsdlContract, URL wsdlUrl, IntrospectionContext context) {

        String interfaze = reader.getAttributeValue(null, "interface");
        if (interfaze == null) {
            MissingAttribute failure = new MissingAttribute("Interface attribute is required", reader);
            context.addError(failure);
            return;
        }
        QName interfaceQName = getQName(interfaze);
        wsdlContract.setQname(interfaceQName);
        wsdlContract.setOperations(processor.getOperations(interfaceQName, wsdlUrl));

    }

    private URL resolveWsdl(XMLStreamReader reader, IntrospectionContext context) {

        String wsdlLocation = reader.getAttributeValue(null, "wsdlLocation");
        if (wsdlLocation == null) {
            // We don't support auto dereferecing of namespace URI
            MissingAttribute failure = new MissingAttribute("wsdlLocation Location is required", reader);
            context.addError(failure);
            return null;
        }
        URL wsdlUrl = getWsdlUrl(wsdlLocation);
        if (wsdlUrl == null) {
            InvalidWSDLLocation failure = new InvalidWSDLLocation("Unable to locate WSDL: " + wsdlLocation, reader);
            context.addError(failure);
        }
        return wsdlUrl;

    }

    /*
     * Returns the interface.portType qname.
     */
    private QName getQName(String interfaze) {
        throw new UnsupportedOperationException("Not supported yet");
    }

    /*
    * Gets the WSDL URL.
    */
    private URL getWsdlUrl(String wsdlPath) {

        try {
            return new URL(wsdlPath);
        } catch (MalformedURLException ex) {
            return getClass().getClassLoader().getResource(wsdlPath);
        }
    }

}
