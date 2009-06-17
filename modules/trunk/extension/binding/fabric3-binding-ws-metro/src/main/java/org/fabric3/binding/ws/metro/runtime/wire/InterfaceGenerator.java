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
package org.fabric3.binding.ws.metro.runtime.wire;

/**
 * Generates an interface with JAX-WS annotations from another interface. This allows classes with non-annotated interfaces to be used with Metro,
 * which requires interfaces that defined service endpoints to be annotated. Specifically, adds @WebService to the generated interface, @WebMethod to
 * all methods, and @Oneway to methods marked with the SCA @OneWay annotation.
 *
 * @version $Rev$ $Date$
 */
public interface InterfaceGenerator {

    /**
     * Generates the annotated interface from another interface
     *
     * @param interfaze       the source interface
     * @param targetNamespace the target namespace to use with the @WebService annotation or null.
     * @param wsdlLocation    the WSDL location to use with the @WebService annotation or null.
     * @param serviceName     the service name to use with the @WebService annotation or null.
     * @param portName        the port name to use with the @WebService annotation or null.
     * @return the generated interface
     * @throws InterfaceGenerationException if an error generating the exception occurs
     */
    Class<?> generateAnnotatedInterface(Class interfaze, String targetNamespace, String wsdlLocation, String serviceName, String portName)
            throws InterfaceGenerationException;
}
