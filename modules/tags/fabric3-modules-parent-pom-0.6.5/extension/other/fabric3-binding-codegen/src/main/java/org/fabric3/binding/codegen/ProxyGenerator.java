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
package org.fabric3.binding.codegen;

import java.lang.reflect.InvocationTargetException;

public interface ProxyGenerator {

    /**
     *
     * @param clazz interface to be converted to a remote interface
     * @param delegate pojo class implementing clazz interface
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */

    Object getWrapper(Class clazz, Object delegate) throws
            ClassNotFoundException, IllegalAccessException,
            InvocationTargetException,
            InstantiationException;

    /**
     *
     * @param clazz interface to be converted to a remote interface
     * @param delegate pojo class implementing clazz interface
     * @param targetNamespace JAX-WS target namespace
     * @param wsdlLocation JAX-WS wsdl location
     * @param serviceName JAX-WS service name of the web service
     * @param portName JAX-WS port name of the web service
     * @return
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     */

    Object getWrapper(Class clazz, Object delegate,
                           String targetNamespace,
                           String wsdlLocation,
                               String serviceName,
                               String portName) throws
            ClassNotFoundException, IllegalAccessException,
            InvocationTargetException,
            InstantiationException;


    Class getWrapperClass(Class clazz,
                           String targetNamespace,
                           String wsdlLocation,
                               String serviceName,
                               String portName) throws
            ClassNotFoundException;

    /**
     * Generate a remote interface with jax-ws annotation
     * @param clazz
     * @param targetNamespace
     * @param wsdlLocation
     * @param serviceName
     * @param portName
     * @return
     * @throws ClassNotFoundException
     */

    Class getWrapperInterface(Class clazz,
                              String targetNamespace,
                              String wsdlLocation,
                              String serviceName,
                              String portName)
            throws ClassNotFoundException;
}
