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
import javax.jws.WebService;
import javax.xml.namespace.QName;

import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;
import org.fabric3.model.type.service.JavaServiceContract;

/**
 * Default EndpointSynthesizer implementation.
 *
 * @version $Rev$ $Date$
 */
public class EndpointSynthesizerImpl implements EndpointSynthesizer {

    public ReferenceEndpointDefinition synthesizeReferenceEndpoint(JavaServiceContract contract, Class<?> serviceClass, URL url) {
        WebService annotation = serviceClass.getAnnotation(WebService.class);
        if (annotation != null) {
            return createDefinition(annotation, serviceClass, url);
        } else {
            return createDefaultDefinition(serviceClass, url);
        }
    }

    public ServiceEndpointDefinition synthesizeServiceEndpoint(JavaServiceContract contract, Class<?> serviceClass, URI uri) {
        WebService annotation = serviceClass.getAnnotation(WebService.class);
        if (annotation != null) {
            return createDefinition(annotation, serviceClass, uri);
        } else {
            return createDefaultDefinition(serviceClass, uri);
        }
    }

    /**
     * Creates a ReferenceEndpointDefinition following JAX-WS rules (section 3.11) for deriving service and port names from a class containing an
     * <code>@WebService</code> annotation.
     *
     * @param annotation   the annotation
     * @param serviceClass the service class
     * @param url          the target endpoint URL
     * @return the ServiceEndpointDefinition
     */
    private ReferenceEndpointDefinition createDefinition(WebService annotation, Class<?> serviceClass, URL url) {
        String namespace = getNamespace(annotation, serviceClass);
        QName serviceQName = getServiceName(annotation, serviceClass, namespace);
        QName portQName = getPortName(annotation, serviceClass, namespace);
        return new ReferenceEndpointDefinition(serviceQName, portQName, url);
    }

    /**
     * Creates a ReferenceEndpointDefinition following JAX-WS rules (section 3.11) for deriving default service and port names from a class.
     *
     * @param serviceClass the service class
     * @param url          the target endpoint URL
     * @return the ServiceEndpointDefinition
     */
    private ReferenceEndpointDefinition createDefaultDefinition(Class<?> serviceClass, URL url) {
        String packageName = serviceClass.getPackage().getName();
        String className = serviceClass.getSimpleName();
        String namespace = deriveNamespace(packageName);
        QName serviceQName = new QName(namespace, className + "Service");
        QName portQName = new QName(namespace, className + "Port");
        return new ReferenceEndpointDefinition(serviceQName, portQName, url);
    }

    /**
     * Creates a ServiceEndpointDefinition following JAX-WS rules (section 3.11) for deriving service and port names from a class containing an
     * <code>@WebService</code> annotation.
     *
     * @param annotation   the annotation
     * @param serviceClass the service class
     * @param uri          the service path
     * @return the ServiceEndpointDefinition
     */
    private ServiceEndpointDefinition createDefinition(WebService annotation, Class<?> serviceClass, URI uri) {
        String namespace = getNamespace(annotation, serviceClass);
        QName serviceQName = getServiceName(annotation, serviceClass, namespace);
        QName portQName = getPortName(annotation, serviceClass, namespace);
        return new ServiceEndpointDefinition(serviceQName, portQName, uri);
    }

    /**
     * Creates a ServiceEndpointDefinition following JAX-WS rules (section 3.11) for deriving default service and port names from a class.
     *
     * @param serviceClass the service class
     * @param uri          the service path
     * @return the ServiceEndpointDefinition
     */
    private ServiceEndpointDefinition createDefaultDefinition(Class<?> serviceClass, URI uri) {
        String packageName = serviceClass.getPackage().getName();
        String className = serviceClass.getSimpleName();
        String namespace = deriveNamespace(packageName);
        QName serviceQName = new QName(namespace, className + "Service");
        QName portQName = new QName(namespace, className + "Port");
        return new ServiceEndpointDefinition(serviceQName, portQName, uri);
    }

    /**
     * Returns the endpoint namespace according to JAX-WS rules.
     *
     * @param annotation   the WebService annotation on the endpoint implementation
     * @param serviceClass the endpoint implementation
     * @return the namsespace
     */
    private String getNamespace(WebService annotation, Class<?> serviceClass) {
        String namespace = annotation.targetNamespace();
        if (namespace.length() < 1) {
            String packageName = serviceClass.getPackage().getName();
            namespace = deriveNamespace(packageName);
        }
        return namespace;
    }

    /**
     * Returns the WSDL service name according to JAX-WS rules.
     *
     * @param annotation   the WebService annotation on the endpoint implementation
     * @param serviceClass the endpoint implementation
     * @param namespace    the namespace
     * @return the service name
     */
    private QName getServiceName(WebService annotation, Class<?> serviceClass, String namespace) {
        String serviceName = annotation.serviceName();
        if (serviceName.length() < 1) {
            serviceName = serviceClass.getSimpleName() + "Service";
        }
        return new QName(namespace, serviceName);
    }

    /**
     * Returns the WSDL port name according to JAX-WS rules.
     *
     * @param annotation   the WebService annotation on the endpoint implementation
     * @param serviceClass the endpoint implementation
     * @param namespace    the namespace
     * @return the service name
     */
    private QName getPortName(WebService annotation, Class<?> serviceClass, String namespace) {
        String portName = annotation.portName();
        if (portName.length() < 1) {
            if (annotation.name().length() < 1) {
                portName = serviceClass.getSimpleName() + "Port";
            } else {
                portName = annotation.name() + "Port";
            }
        }
        return new QName(namespace, portName);
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
