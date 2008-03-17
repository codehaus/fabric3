/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.fabric3.binding.ejb.scdl;

import java.net.URI;
import javax.xml.namespace.QName;

import org.fabric3.binding.ejb.introspection.EjbBindingLoader;
import org.fabric3.scdl.BindingDefinition;

/**
 * @version $Revision: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 */
public class EjbBindingDefinition extends BindingDefinition {
    private String homeInterface;
    private String ejbLink;
    private boolean isStateless = true;
    private boolean isEjb3;
    private String name;

    public EjbBindingDefinition(URI targetUri) {
        super(targetUri, EjbBindingLoader.BINDING_QNAME);
    }

    //TODO PolicySets & Requires

    public String getHomeInterface() {
        return homeInterface;
    }

    public void setHomeInterface(String homeInterface) {
        this.homeInterface = homeInterface;
    }

    public String getEjbLink() {
        return ejbLink;
    }

    public void setEjbLink(String ejbLink) {
        this.ejbLink = ejbLink;
    }

    public boolean isStateless() {
        return isStateless;
    }

    public void setStateless(boolean stateless) {
        isStateless = stateless;
    }

    public boolean isEjb3() {
        return isEjb3;
    }

    public void setEjb3(boolean Ejb3) {
        isEjb3 = Ejb3;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
