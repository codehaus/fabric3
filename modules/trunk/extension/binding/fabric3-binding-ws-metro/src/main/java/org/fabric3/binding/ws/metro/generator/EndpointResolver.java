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
import javax.xml.namespace.QName;

import org.fabric3.binding.ws.metro.provision.ReferenceEndpointDefinition;
import org.fabric3.binding.ws.metro.provision.ServiceEndpointDefinition;

/**
 * Resolves endpoint information contained in a WSDL document. This is done by parsing the WSDL element URI which must be of the form
 * <code><WSDL-namespace-URI>#expression<code>. The WSDL namespace URI is used to determine the set of documents the expression is applied against.
 * The set of documents will be constrained by the import/exports of the contribution containing the deployable composite. In other words, WSDL
 * documents not visible in the deployable composite's contribution space will not be evaluated.
 *
 * @version $Rev$ $Date$
 */
public interface EndpointResolver {

    /**
     * Resolves service-side endpoint information.
     *
     * @param deployable  the deployable composite the endpoint is deployed with
     * @param wsdlElement the WSDL element expression identifying how endpoint information should be resolved, e.g.
     *                    <code><WSDL-namespace-URI>#wsdl.port(servicename/portname)</code>
     * @return the service-side endpoint information
     * @throws EndpointResolutionException if an error performing resolution is encountered
     */
    ServiceEndpointDefinition resolveServiceEndpoint(QName deployable, URI wsdlElement) throws EndpointResolutionException;

    /**
     * Resolves reference-side endpoint information.
     *
     * @param deployable  the deployable composite the reference is deployed with
     * @param wsdlElement the WSDL element expression identifying how endpoint information should be resolved, e.g.
     *                    <code><WSDL-namespace-URI>#wsdl.port(servicename/portname)</code>
     * @return the reference-side endpoint information
     * @throws EndpointResolutionException if an error performing resolution is encountered
     */
    ReferenceEndpointDefinition resolveReferenceEndpoint(QName deployable, URI wsdlElement) throws EndpointResolutionException;

}
