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
package org.fabric3.binding.ws.metro.control;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.fabric3.binding.ws.metro.provision.MetroWireSourceDefinition;
import org.fabric3.binding.ws.metro.provision.MetroWireTargetDefinition;
import org.fabric3.binding.ws.metro.provision.WsdlElement;
import org.fabric3.binding.ws.scdl.WsBindingDefinition;
import org.fabric3.scdl.ReferenceDefinition;
import org.fabric3.scdl.ServiceDefinition;
import org.fabric3.spi.generator.BindingGenerator;
import org.fabric3.spi.generator.GenerationException;
import org.fabric3.spi.model.instance.LogicalBinding;
import org.fabric3.spi.policy.Policy;
import org.fabric3.spi.services.classloading.ClassLoaderRegistry;
import org.osoa.sca.annotations.Reference;

import com.sun.xml.ws.api.model.wsdl.WSDLModel;

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
        URI classLoaderId = binding.getParent().getParent().getClassLoaderId();
        URL wsdlLocation = getWsdlLocation(definition.getWsdlLocation(), classLoaderId);
        WSDLModel wsdlModel = wsdlParser.parse(wsdlLocation);
        
        WsdlElement wsdlElement = wsdlElementParser.parseWsdlElement(definition.getWsdlElement(), wsdlModel, serviceDefinition.getServiceContract());
        URI servicePath = addressResolver.resolveServiceAddress(definition.getTargetUri(), wsdlElement, wsdlModel);
        String interfaze = serviceDefinition.getServiceContract().getQualifiedInterfaceName();
        
        return new MetroWireSourceDefinition(wsdlElement, wsdlLocation, servicePath, interfaze);
        
    }

    /**
     * Creates the wire target definition.
     */
    public MetroWireTargetDefinition generateWireTarget(LogicalBinding<WsBindingDefinition> binding,
                                                        Policy policy,
                                                        ReferenceDefinition referenceDefinition) throws GenerationException {
        
        WsBindingDefinition definition = binding.getDefinition();
        URI classLoaderId = binding.getParent().getParent().getClassLoaderId();
        URL wsdlLocation = getWsdlLocation(definition.getWsdlLocation(), classLoaderId);
        WSDLModel wsdlModel = wsdlParser.parse(wsdlLocation);
        
        WsdlElement wsdlElement = wsdlElementParser.parseWsdlElement(definition.getWsdlElement(), wsdlModel, referenceDefinition.getServiceContract());
        URL[] referenceUrls = addressResolver.resolveReferenceAddress(definition.getTargetUri(), wsdlElement, wsdlModel);
        String interfaze = referenceDefinition.getServiceContract().getQualifiedInterfaceName();
        
        return new MetroWireTargetDefinition(wsdlElement, wsdlLocation, interfaze, referenceUrls);

    }

    /*
     * Gets the WSDL location as a URL.
     */
    private URL getWsdlLocation(String wsdlLocation, URI classloaderId) {
        
        if (wsdlLocation == null) {
            return null;
        }
        
        try {
            return new URL(wsdlLocation);
        } catch (MalformedURLException e) {
            ClassLoader classLoader = classLoaderRegistry.getClassLoader(classloaderId);
            return classLoader.getResource(wsdlLocation);
        }
        
    }

}
