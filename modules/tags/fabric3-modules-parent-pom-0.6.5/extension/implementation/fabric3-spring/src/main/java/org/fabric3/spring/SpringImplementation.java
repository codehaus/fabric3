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
package org.fabric3.spring;

import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import org.osoa.sca.Constants;
import org.springframework.core.io.Resource;

import org.fabric3.scdl.Implementation;

/**
 *
 */
public class SpringImplementation extends Implementation<SpringComponentType> {
    private static final long serialVersionUID = 4308461674679003314L;
    public static final QName IMPLEMENTATION_SPRING = new QName(Constants.SCA_NS, "implementation.spring");

    // The location attribute which points to the Spring application-context XML file
    private String location;
    // The application-context file as a Spring Resource
    private Resource resource;
    private Map<String, String> serviceNameToBeanId;

    public SpringImplementation() {
        serviceNameToBeanId = new HashMap<String, String>();
        refNameToFieldType = new HashMap<String, Class<?>>();
    }

    public QName getType() {
        return IMPLEMENTATION_SPRING;
    }

    public String getLocation() {
      return location;
    }

    public void setLocation(String location) {
    	this.location = location;
    }
    
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    public Resource getResource() {
        return resource;
    }
    
    public void addServiceNameToBeanId(String serviceName, String beanId) {
        serviceNameToBeanId.put(serviceName, beanId);
    }

    public String getBeanId(String serviceName) {
        return serviceNameToBeanId.get(serviceName);
    }

    private Map<String, Class<?>> refNameToFieldType;
    public void addRefNameToFieldType(String refName, Class<?> fieldType) {
        refNameToFieldType.put(refName, fieldType);
    }

    public Class<?> getFieldType(String refName) {
        return refNameToFieldType.get(refName);
    }

}
