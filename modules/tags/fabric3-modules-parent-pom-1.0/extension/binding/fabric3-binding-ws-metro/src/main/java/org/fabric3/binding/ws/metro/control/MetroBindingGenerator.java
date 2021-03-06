/*
 * Fabric3
 * Copyright � 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the �License�), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an �as is� basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
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
import org.fabric3.model.type.component.ServiceDefinition;
import org.fabric3.model.type.service.ServiceContract;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;
import org.fabric3.spi.classloader.ClassLoaderRegistry;

/**
 * @version $Revision$ $Date$
 * 
 * TODO Add support for WSDL Contract
 */
public class MetroBindingGenerator  implements BindingGenerator<MetroWireSourceDefinition, MetroWireTargetDefinition, WsBindingDefinition> {
    
    @Reference protected ClassLoaderRegistry classLoaderRegistry;
    @Reference protected WsdlElementParser wsdlElementParser;
    @Reference protected AddressResolver addressResolver;
    @Reference protected WsdlParser wsdlParser;

    /**
     * Creates the wire source definition.
     */
    public MetroWireSourceDefinition generateWireSource(LogicalBinding<WsBindingDefinition> binding,
                                                        Policy policy,  
                                                        ServiceDefinition serviceDefinition) throws GenerationException {
        
        WsBindingDefinition definition = binding.getDefinition();
        URL wsdlLocation = getWsdlLocation(definition.getWsdlLocation());
        WSDLModel wsdlModel = wsdlParser.parse(wsdlLocation);
        
        WsdlElement wsdlElement = wsdlElementParser.parseWsdlElement(definition.getWsdlElement(), wsdlModel, serviceDefinition.getServiceContract());
        URI servicePath = addressResolver.resolveServiceAddress(definition.getTargetUri(), wsdlElement, wsdlModel);
        String interfaze = serviceDefinition.getServiceContract().getQualifiedInterfaceName();
        
        List<QName> requestedIntents = policy.getProvidedIntents();
        
        return new MetroWireSourceDefinition(wsdlElement, wsdlLocation, servicePath, interfaze, requestedIntents);
        
    }

    /**
     * Creates the wire target definition.
     */
    public MetroWireTargetDefinition generateWireTarget(LogicalBinding<WsBindingDefinition> binding,
                                                        Policy policy,
                                                        ServiceContract<?> contract) throws GenerationException {
        
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
