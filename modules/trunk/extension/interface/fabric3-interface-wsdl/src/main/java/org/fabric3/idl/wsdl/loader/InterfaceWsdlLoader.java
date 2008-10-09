/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.idl.wsdl.loader;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.osoa.sca.annotations.EagerInit;

import org.fabric3.idl.wsdl.scdl.WsdlContract;
import org.fabric3.idl.wsdl.processor.WsdlProcessor;
import org.fabric3.introspection.IntrospectionContext;
import org.fabric3.introspection.xml.LoaderUtil;
import org.fabric3.introspection.xml.MissingAttribute;
import org.fabric3.introspection.xml.TypeLoader;
import org.osoa.sca.annotations.Reference;

/**
 * Loader for interface.wsdl.
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class InterfaceWsdlLoader implements TypeLoader<WsdlContract> {

    /**
     * WSDL processor.
     */
    private final WsdlProcessor processor;

    /**
     * @param loaderRegistry Loader registry.
     * @param processor      WSDL processor.
     */
    public InterfaceWsdlLoader(@Reference(name="processor")WsdlProcessor processor) {
        this.processor = processor;
    }

    public WsdlContract load(XMLStreamReader reader, IntrospectionContext context) throws XMLStreamException {

        WsdlContract wsdlContract = new WsdlContract();

        URL wsdlUrl = resolveWsdl(reader, context);
        if (wsdlUrl == null) {
            // there was a problem, return an empty contract
            return wsdlContract;
        }
        processInterface(reader, wsdlContract, wsdlUrl, context);

        processCallbackInterface(reader, wsdlContract, wsdlUrl);
        
        LoaderUtil.skipToEndElement(reader);
        
        return wsdlContract;

    }

    /*
     * Processes the callback interface.
     */
    @SuppressWarnings("unchecked")
    private void processCallbackInterface(XMLStreamReader reader, WsdlContract wsdlContract, URL wsdlUrl) {

        String callbackInterfaze = reader.getAttributeValue(null, "callbackInterface");
        if (callbackInterfaze != null) {
            QName callbackInterfaceQName = getQName(callbackInterfaze);
            wsdlContract.setCallbackQname(callbackInterfaceQName);
        }

    }

    /*
     * Processes the interface.
     */
    @SuppressWarnings("unchecked")
    private void processInterface(XMLStreamReader reader, WsdlContract wsdlContract, URL wsdlUrl, IntrospectionContext context) {

        String interfaze = reader.getAttributeValue(null, "interface");
        if (interfaze == null) {
            MissingAttribute failure = new MissingAttribute("Interface attribute is required", "interface", reader);
            context.addError(failure);
            return;
        }
        QName interfaceQName = getQName(interfaze);
        wsdlContract.setQname(interfaceQName);
        wsdlContract.setOperations(processor.getOperations(interfaceQName, wsdlUrl));

    }

    /*
     * Resolves the WSDL.
     */
    private URL resolveWsdl(XMLStreamReader reader, IntrospectionContext context) {

        String wsdlLocation = reader.getAttributeValue(null, "wsdlLocation");
        if (wsdlLocation == null) {
            // We don't support auto dereferecing of namespace URI
            MissingAttribute failure = new MissingAttribute("wsdlLocation Location is required", "wsdlLocation", reader);
            context.addError(failure);
            return null;
        }
        URL wsdlUrl = getWsdlUrl(wsdlLocation);
        if (wsdlUrl == null) {
            InvalidWSDLLocation failure = new InvalidWSDLLocation("Unable to locate WSDL: " + wsdlLocation, wsdlLocation, reader);
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
