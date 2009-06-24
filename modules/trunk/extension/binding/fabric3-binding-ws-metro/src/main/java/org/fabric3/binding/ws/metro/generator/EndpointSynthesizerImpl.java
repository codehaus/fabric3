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

import java.net.URI;
import java.net.URL;
import javax.xml.namespace.QName;

import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.model.type.service.JavaServiceContract;
import org.fabric3.model.type.service.ServiceContract;

/**
 * Default EndpointSynthesizer implementation.
 *
 * @version $Rev$ $Date$
 */
public class EndpointSynthesizerImpl implements EndpointSynthesizer {

    public ReferenceEndpointDefinition synthesizeReferenceEndpoint(ServiceContract<?> contract, URL url) throws UnsupportedContractException {
        if (!(contract instanceof JavaServiceContract)) {
            throw new UnsupportedContractException("Service contract type not supported: " + contract.getClass());
        }
        JavaServiceContract javaContract = (JavaServiceContract) contract;
        String qualifedName = javaContract.getInterfaceClass();
        String packageName = qualifedName.substring(0, qualifedName.lastIndexOf('.'));
        String namespace = deriveNamespace(packageName);
        String unqualifiedName = qualifedName.substring(qualifedName.lastIndexOf('.') + 1);
        QName serviceName = new QName(namespace, unqualifiedName + "Service");
        QName portName = new QName(namespace, unqualifiedName + "Port");
        return new ReferenceEndpointDefinition(serviceName, portName, url);
    }

    public ServiceEndpointDefinition synthesizeServiceEndpoint(ServiceContract<?> contract, URI uri) throws UnsupportedContractException {
        if (!(contract instanceof JavaServiceContract)) {
            throw new UnsupportedContractException("Service contract type not supported: " + contract.getClass());
        }
        JavaServiceContract javaContract = (JavaServiceContract) contract;
        String qualifedName = javaContract.getInterfaceClass();
        String packageName = qualifedName.substring(0, qualifedName.lastIndexOf('.'));
        String unqualifiedName = qualifedName.substring(qualifedName.lastIndexOf('.') + 1);
        String namespace = deriveNamespace(packageName);
        QName serviceName = new QName(namespace, unqualifiedName + "Service");
        QName portName = new QName(namespace, unqualifiedName + "Port");
        return new ServiceEndpointDefinition(serviceName, portName, uri);
    }

    /**
     * Derives an XML namespace from a Java package according to JAXB rules. For example, org.foo is rendered as http://foo.org/.
     *
     * @param pkg the Java package
     * @return the XML namespace
     */
    String deriveNamespace(String pkg) {
        String[] tokens = pkg.split("\\.");
        StringBuilder builder = new StringBuilder("http://");
        for (int i = tokens.length - 1; i >= 0; i--) {
            String token = tokens[i];
            builder.append(token);
            if (i != 0) {
                builder.append(".");
            } else {
                builder.append("/");
            }
        }
        return builder.toString();
    }

}
