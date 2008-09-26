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
package org.fabric3.binding.ws.jaxws.provision;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalWireTargetDefinition;

public class JaxWsWireTargetDefinition extends PhysicalWireTargetDefinition {

  
    private String wsdlElement;
    private String referenceInterface;
    private URI classloaderURI;
    private String wsdlLocation;
    private String serviceName;
    private String portName;
    private String namespaceURI;


    /**
     * @return Reference interface for the wire target.
     */
    public String getReferenceInterface() {
        return referenceInterface;
    }

    /**
     * @param referenceInterface Reference interface for the wire target.
     */
    public void setReferenceInterface(String referenceInterface) {
        this.referenceInterface = referenceInterface;
    }

    /**
     * @return Classloader URI.
     */
    public URI getClassloaderURI() {
        return classloaderURI;
    }

    /**
     * @param classloaderURI Classloader URI.
     */
    public void setClassloaderURI(URI classloaderURI) {
        this.classloaderURI = classloaderURI;
    }


    /**
     *
     * @return WSDL Element
     */
    public String getWsdlElement() {
        return wsdlElement;
    }

    /**
     * @param wsdlElement WSDL Element
     */
    public void setWsdlElement(String wsdlElement) {
        this.wsdlElement = wsdlElement;
        String[] parsed = ProvisionHelper.parseWSDLElement(wsdlElement);
        setNamespaceURI(parsed[0]);
        setServiceName(parsed[1]);
        setPortName(parsed[2]);
        if (wsdlLocation == null) {
            setWsdlLocation(namespaceURI  + serviceName + "/" + portName + "?wsdl");
        }
    }

    /**
     * @return WSDL Location
     */
    public String getWsdlLocation() {
        return wsdlLocation;
    }

    /**
     * @param wsdlLocation WSDL Location
     */
    public void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }


    public void setNamespaceURI(String uri) {
        this.namespaceURI = uri;
    }

    /**
     * Get WSDL namespace uri
     * @return wsdl namespace uri
     */
    public String getNamespaceURI() {
        return namespaceURI;
    }

    /**
     * Get WSDL Service Name
    *
    * @return service name
    */
    public String getServiceName() {
       return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
      * Get WSDL port name
      *
      * @return portname for the wsdl
    */
    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

}
