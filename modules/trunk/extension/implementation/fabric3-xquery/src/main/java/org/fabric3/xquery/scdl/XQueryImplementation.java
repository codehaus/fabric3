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
package org.fabric3.xquery.scdl;

import javax.xml.namespace.QName;

import org.fabric3.model.type.component.Implementation;
import org.fabric3.spi.Namespaces;

/**
 * @version $Rev$ $Date$
 */
public class XQueryImplementation extends Implementation<XQueryComponentType> {

    public static final QName QNAME = new QName(Namespaces.IMPLEMENTATION, "implementation.xquery");
    private String location;
    private String context;
    private boolean isModule;
    private QName moduleNameSpace;

    public QName getType() {
        return QNAME;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean isIsModule() {
        return isModule;
    }

    public void setIsModule(boolean isModule) {
        this.isModule = isModule;
    }

    public QName getModuleNameSpace() {
        return moduleNameSpace;
    }

    public void setModuleNameSpace(QName moduleNameSpace) {
        this.moduleNameSpace = moduleNameSpace;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
    
   
}
