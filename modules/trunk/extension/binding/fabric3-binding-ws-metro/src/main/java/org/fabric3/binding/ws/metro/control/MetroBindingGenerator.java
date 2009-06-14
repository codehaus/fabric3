  /*
   * Fabric3
   * Copyright (C) 2009 Metaform Systems
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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import javax.xml.namespace.QName;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;
import org.osoa.sca.annotations.Reference;

import org.fabric3.binding.ws.metro.provision.MetroWireSourceDefinition;
import org.fabric3.binding.ws.metro.provision.MetroWireTargetDefinition;
import org.fabric3.binding.ws.provision.WsdlElement;
import org.fabric3.binding.ws.scdl.WsBindingDefinition;
import org.fabric3.host.Names;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.classloader.ClassLoaderRegistry;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.model.instance.LogicalOperation;
import org.fabric3.spi.policy.Policy;

/**
 * @version $Revision$ $Date$
 *          <p/>
 *          TODO Add support for WSDL Contract
 */
public class MetroBindingGenerator implements BindingGenerator<WsBindingDefinition> {

    @Reference
    protected ClassLoaderRegistry classLoaderRegistry;
    @Reference
    protected WsdlElementParser wsdlElementParser;
    @Reference
    protected AddressResolver addressResolver;
    @Reference
    protected WsdlParser wsdlParser;

    /**
     * Creates the wire source definition.
     */
    public MetroWireSourceDefinition generateWireSource(LogicalBinding<WsBindingDefinition> binding,
                                                        ServiceContract<?> contract,
                                                        List<LogicalOperation> operations,
                                                        Policy policy) throws GenerationException {

        WsBindingDefinition definition = binding.getDefinition();
        URL wsdlLocation = getWsdlLocation(definition.getWsdlLocation());
        WSDLModel wsdlModel = wsdlParser.parse(wsdlLocation);

        WsdlElement wsdlElement = wsdlElementParser.parseWsdlElement(definition.getWsdlElement(), wsdlModel, contract);
        URI servicePath = addressResolver.resolveServiceAddress(definition.getTargetUri(), wsdlElement, wsdlModel);
        String interfaze = contract.getQualifiedInterfaceName();

        List<QName> requestedIntents = policy.getProvidedIntents();

        return new MetroWireSourceDefinition(wsdlElement, wsdlLocation, servicePath, interfaze, requestedIntents);

    }

    /**
     * Creates the wire target definition.
     */
    public MetroWireTargetDefinition generateWireTarget(LogicalBinding<WsBindingDefinition> binding,
                                                        ServiceContract<?> contract,
                                                        List<LogicalOperation> operations,
                                                        Policy policy) throws GenerationException {

        WsBindingDefinition definition = binding.getDefinition();
        URL wsdlLocation = getWsdlLocation(definition.getWsdlLocation());
        WSDLModel wsdlModel = wsdlParser.parse(wsdlLocation);
        WsdlElement wsdlElement = wsdlElementParser.parseWsdlElement(definition.getWsdlElement(), wsdlModel, contract);
        URL[] referenceUrls = addressResolver.resolveReferenceAddress(definition.getTargetUri(), wsdlElement, wsdlModel);
        String interfaze = contract.getQualifiedInterfaceName();

        List<QName> requestedIntents = policy.getProvidedIntents();

        return new MetroWireTargetDefinition(wsdlElement, wsdlLocation, interfaze, requestedIntents, referenceUrls);

    }

    /*
     * Gets the WSDL location as a URL.
     */
    private URL getWsdlLocation(String wsdlLocation) {

        if (wsdlLocation == null) {
            return null;
        }

        try {
            return new URL(wsdlLocation);
        } catch (MalformedURLException e) {
            ClassLoader classLoader = classLoaderRegistry.getClassLoader(Names.HOST_CONTRIBUTION);
            return classLoader.getResource(wsdlLocation);
        }

    }

}
