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
package org.fabric3.binding.ws.metro.control;

import javax.xml.namespace.QName;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.model.wsdl.WSDLService;

import org.fabric3.binding.ws.provision.WsdlElement;
import org.fabric3.model.type.service.JavaServiceContract;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.generator.GenerationException;

/**
 * Default implementation of the WSDL element parser.
 * <p/>
 * TODO Currently support only WSDL 1.1 and also one WSDL 1.1 port
 */
public class DefaultWsdlElementParser implements WsdlElementParser {

    /**
     * Parses the WSDL element.
     *
     * @param wsdlElement     String representation of the WSDL element.
     * @param wsdlModel       Model object representing the WSDL information.
     * @param serviceContract Service contract for the WSDL.
     * @return Parsed WSDL element.
     * @throws GenerationException If unable to parse the WSDL element.
     */
    public WsdlElement parseWsdlElement(String wsdlElement, WSDLModel wsdlModel, ServiceContract<?> serviceContract) throws GenerationException {

        // No wsdl element, just location, parse the WSDL location
        if (wsdlElement == null && wsdlModel != null) {
            return parseWsdl(wsdlModel);
        }

        // Wsdl element present, so parse it
        if (wsdlElement != null) {
            return parseWsdlElement(wsdlElement, wsdlModel);
        }

        // No wsdl element or location, synthesize the names
        return synthesizeFromContract(serviceContract);

    }

    /*
     * Parses the service name and port name from the WSDL.
     */
    private WsdlElement parseWsdl(WSDLModel wsdlModel) throws GenerationException {

        WSDLService wsdlService = wsdlModel.getServices().values().iterator().next();
        if (wsdlService == null) {
            throw new GenerationException("WSDL doesn't contain any service");
        }
        WSDLPort wsdlPort = wsdlService.getFirstPort();
        if (wsdlPort == null) {
            throw new GenerationException("WSDL doesn't contain any port");
        }

        return new WsdlElement(wsdlService.getName(), wsdlPort.getName());

    }

    /*
     * Parses the service name and port name from the WSDL element.
     */
    private WsdlElement parseWsdlElement(String wsdlElement, WSDLModel wsdlModel) throws GenerationException {

        String[] token = wsdlElement.split("#");
        String namespaceUri = token[0];

        if (!token[1].startsWith("wsdl.port")) {
            throw new GenerationException("Only WSDL 1.1 ports are currently supported");
        }
        token = token[1].substring(token[1].indexOf('(') + 1, token[1].indexOf(')')).split("/");

        QName serviceName = new QName(namespaceUri, token[0]);
        QName portName = new QName(namespaceUri, token[1]);

        if (wsdlModel != null) {
            WSDLService wsdlService = wsdlModel.getService(serviceName);
            if (wsdlService == null) {
                throw new GenerationException("Service " + serviceName + " not found in WSDL");
            }
            if (wsdlService.get(portName) == null) {
                throw new GenerationException("Port " + portName + " not found in WSDL");
            }
        }

        return new WsdlElement(serviceName, portName);

    }

    /*
     * Synthesizes the service name and port name from the service contract.
     */
    private WsdlElement synthesizeFromContract(ServiceContract<?> serviceContract) throws GenerationException {

        if (serviceContract instanceof JavaServiceContract) {

            JavaServiceContract javaServiceContract = (JavaServiceContract) serviceContract;
            String qualifedName = javaServiceContract.getInterfaceClass();
            String packageName = qualifedName.substring(0, qualifedName.lastIndexOf('.'));
            String unqualifiedName = qualifedName.substring(qualifedName.lastIndexOf('.') + 1);

            return new WsdlElement(new QName(packageName, unqualifiedName + "Service"), new QName(packageName, unqualifiedName + "Port"));

        } else {
            // TODO Support interface.wsdl
            throw new GenerationException("Service contract not supported : " + serviceContract.getClass());
        }

    }

}
