package org.fabric3.binding.ws.jaxws.provision;

import java.net.URI;

import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;

/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

public class JaxWsWireSourceDefinition  extends PhysicalWireSourceDefinition {

    private String serviceInterface;
    private URI classloaderURI;
    private String wsdlElement;
    private String wsdlLocation;
    private String serviceName;
    private String portName;
    private String namespaceURI;

    /**
     * @return Service interface for the wire source.
     */
    public String getServiceInterface() {
        return serviceInterface;
    }

    /**
     * @param serviceInterface Service interface for the wire source.
     */
    public void setServiceInterface(String serviceInterface) {
        this.serviceInterface = serviceInterface;
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
   * @return portname for the wsdl
   */
    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }


}
