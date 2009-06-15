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
package org.fabric3.binding.ws.axis2.runtime;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.description.AxisService;
import org.osoa.sca.annotations.EagerInit;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.axis2.provision.Axis2WireTargetDefinition;
import org.fabric3.binding.ws.axis2.provision.AxisPolicy;
import org.fabric3.binding.ws.axis2.runtime.config.F3Configurator;
import org.fabric3.binding.ws.axis2.runtime.policy.PolicyApplier;
import org.fabric3.spi.ObjectFactory;
import org.fabric3.spi.builder.WiringException;
import org.fabric3.spi.builder.component.TargetWireAttacher;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.model.physical.PhysicalWireSourceDefinition;
import org.fabric3.spi.services.expression.ExpressionExpander;
import org.fabric3.spi.services.expression.ExpressionExpansionException;
import org.fabric3.spi.wire.Interceptor;
import org.fabric3.spi.wire.InvocationChain;
import org.fabric3.spi.wire.Wire;

/**
 * @version $Rev$ $Date$
 *          <p/>
 *          TODO Add support for WSDL contract
 */
@EagerInit
public class Axis2TargetWireAttacher implements TargetWireAttacher<Axis2WireTargetDefinition> {

    @Reference
    protected PolicyApplier policyApplier;
    @Reference
    protected F3Configurator f3Configurator;
    @Reference
    protected ExpressionExpander expander;
    @Reference
    protected ClassLoaderRegistry classLoaderRegistry;

    public void attachToTarget(PhysicalWireSourceDefinition source, Axis2WireTargetDefinition target, Wire wire) throws WiringException {

        ClassLoader classLoader = classLoaderRegistry.getClassLoader(source.getClassLoaderId());

        List<String> endpointUris = new LinkedList<String>();
        String endpointUri = expandUri(target.getUri());
        StringTokenizer tok = new StringTokenizer(endpointUri);
        while (tok.hasMoreElements()) {
            endpointUris.add(tok.nextToken().trim());
        }
        AxisService axisService = createAxisClientService(target, classLoader);

        for (InvocationChain chain : wire.getInvocationChains()) {

            String operation = chain.getPhysicalOperation().getName();

            Set<AxisPolicy> policies = target.getPolicies(operation);
            Map<String, String> opInfo = target.getOperationInfo() != null ? target.getOperationInfo().get(operation) : null;

            Interceptor interceptor = new Axis2TargetInterceptor(endpointUris,
                                                                 operation,
                                                                 policies,
                                                                 opInfo,
                                                                 target.getConfig(),
                                                                 f3Configurator,
                                                                 policyApplier,
                                                                 axisService,
                                                                 classLoader);
            chain.addInterceptor(interceptor);
        }

    }

    public void detachFromTarget(PhysicalWireSourceDefinition source, Axis2WireTargetDefinition target) throws WiringException {
        // no-op
    }

    public ObjectFactory<?> createObjectFactory(Axis2WireTargetDefinition target) throws WiringException {
        throw new AssertionError();
    }

    /**
     * Expands the target URI if it contains an expression of the form ${..}.
     *
     * @param uri the target uri to expand
     * @return the expanded URI with sourced values for any expressions
     * @throws WiringException if there is an error expanding an expression
     */
    private String expandUri(URI uri) throws WiringException {
        try {
            String decoded = URLDecoder.decode(uri.toASCIIString(), "UTF-8");
            // classloaders not needed since the type is String
            return expander.expand(decoded);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        } catch (ExpressionExpansionException e) {
            throw new WiringException(e);
        }
    }

    private URL getWsdlURL(String wsdlLocation, ClassLoader classLoader) {
        if (wsdlLocation == null) {
            return null;
        }
        try {
            return new URL(wsdlLocation);
        } catch (MalformedURLException e) {
            return classLoader.getResource(wsdlLocation);
        }
    }

    /*
    * Create instance of client side Axis2 service to get info about the Webservice
    */
    private AxisService createAxisClientService(Axis2WireTargetDefinition target, ClassLoader classLoader) throws WiringException {

        URL wsdlURL = getWsdlURL(target.getWsdlLocation(), classLoader);
        if (wsdlURL != null) {
            try {
                return AxisService.createClientSideAxisService(wsdlURL,
                                                               target.getWsdlElement().getServiceName(),
                                                               target.getWsdlElement().getPortName().getLocalPart(),
                                                               new Options());
            } catch (AxisFault e) {
                throw new WiringException(e);
            }
        } else {
            return null;
        }
    }
}