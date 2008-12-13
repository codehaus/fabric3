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
package org.fabric3.xquery.provision;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.w3c.dom.Document;

import org.fabric3.spi.model.physical.PhysicalComponentDefinition;

/**
 * @version $Rev$ $Date$
 */
public class XQueryComponentDefinition extends PhysicalComponentDefinition {
    private static final long serialVersionUID = -2176668190738465467L;

    private String location;
    private String context;
    private Map<String, Document> propertyValues = new HashMap<String, Document>();
    private Map<String, List<QName>> serviceFunctions;
    private Map<String, List<QName>> referenceFunctions;
    private Map<String, List<QName>> callbackFunctions;
    //private Map<String, List<QName>> serviceCallbackFunctions;
    //private Map<String, List<QName>> referenceCallbackFunctions;
    private Map<String, QName> properties;

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Map<String, Document> getPropertyValues() {
        return propertyValues;
    }

    public void setPropertyValue(String name, Document value) {
        propertyValues.put(name, value);
    }

    public Map<String, List<QName>> getServiceFunctions() {
        return serviceFunctions;
    }

    public Map<String, List<QName>> getReferenceFunctions() {
        return referenceFunctions;
    }


    public Map<String, QName> getProperties() {
        return properties;
    }

    public void setServiceFunctions(Map<String, List<QName>> serviceFunctions) {
        this.serviceFunctions = serviceFunctions;
    }

    public void setReferenceFunctions(Map<String, List<QName>> referenceFunctions) {
        this.referenceFunctions = referenceFunctions;
    }


    public void setCallbackFunctions(Map<String, List<QName>> callbackFunctions) {
        this.callbackFunctions = callbackFunctions;
    }

    public Map<String, List<QName>> getCallbackFunctions() {
        return callbackFunctions;
    }

    /*
     public void setReferenceCallbackFunctions(Map<String, List<QName>> callbackFunctions) {
        this.referenceCallbackFunctions = callbackFunctions;
    }

    public Map<String, List<QName>> getReferenceCallbackFunctions() {
        return referenceCallbackFunctions;
    }
    */
    public void setProperties(Map<String, QName> properties) {
        this.properties = properties;
    }
}
