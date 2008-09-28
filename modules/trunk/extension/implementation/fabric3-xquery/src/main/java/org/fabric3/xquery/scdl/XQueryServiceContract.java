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
 package org.fabric3.xquery.scdl;

import java.lang.reflect.Type;
import java.util.ArrayList;
import javax.xml.namespace.QName;
import org.fabric3.scdl.Operation;
import org.fabric3.scdl.ServiceContract;

/**
 * @version $Rev$ $Date$
 */
public class XQueryServiceContract extends ServiceContract {

    public XQueryServiceContract(){
        operations=new  ArrayList<Operation<Type>>();
    }

    private QName qname;

    public QName getQname() {
        return qname;
    }

    public void setQname(QName qname) {
        this.qname = qname;
    }

    @Override
    public boolean isAssignableFrom(ServiceContract contract) {
        return true;
    }

    @Override
    public String getQualifiedInterfaceName() {
        return qname.toString();
    }


}
