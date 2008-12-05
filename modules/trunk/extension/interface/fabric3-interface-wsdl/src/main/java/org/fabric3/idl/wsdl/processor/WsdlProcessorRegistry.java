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
package org.fabric3.idl.wsdl.processor;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchemaType;
import org.fabric3.idl.wsdl.version.WsdlVersionChecker;
import org.fabric3.idl.wsdl.version.WsdlVersionChecker.WsdlVersion;
import org.fabric3.model.type.service.Operation;
import org.osoa.sca.annotations.Reference;
import org.osoa.sca.annotations.Service;

/**
 * Default WSDL processor implementation.
 *
 * @version $Revsion$ $Date$
 */
@Service(interfaces={WsdlProcessorRegistry.class,WsdlProcessor.class})
public class WsdlProcessorRegistry implements WsdlProcessor {

    /**
     * WSDL processors.
     */
    private Map<WsdlVersion, WsdlProcessor> wsdlProcessors = new HashMap<WsdlVersion, WsdlProcessor>();
    
    /**
     * WSDL version checker.
     */
    private WsdlVersionChecker versionChecker;

    /**
     * @param versionChecker Injected WSDL version checker.
     */
    public WsdlProcessorRegistry(@Reference(name="versionChecker")WsdlVersionChecker versionChecker) {
        this.versionChecker = versionChecker;
    }

    public List<Operation<XmlSchemaType>> getOperations(QName portTypeOrInterfaceName, URL wsdlUrl) {

        WsdlVersion wsdlVersion = versionChecker.getVersion(wsdlUrl);
        if(!wsdlProcessors.containsKey(wsdlVersion)) {
            throw new WsdlProcessorException("No processor registered for version " + wsdlVersion);
        }
        return wsdlProcessors.get(wsdlVersion).getOperations(portTypeOrInterfaceName, wsdlUrl);

    }

    /**
     * Registers a processor.
     *
     * @param wsdlVersion WSDL version.
     * @param processor WSDL processor.
     */
    public void registerProcessor(WsdlVersion wsdlVersion, WsdlProcessor processor) {
        wsdlProcessors.put(wsdlVersion, processor);
    }

}
