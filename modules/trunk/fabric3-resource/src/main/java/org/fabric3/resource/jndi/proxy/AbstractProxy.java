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
package org.fabric3.resource.jndi.proxy;

import org.osoa.sca.annotations.EagerInit;

/**
 *
 * @version $Revision$ $Date$
 */
@EagerInit
public class AbstractProxy<D> {
    
    private D delegate;
    private String jndiName;
    private String providerUrl;
    private String initialContextFactory;
    private boolean env;
    
    protected D getDelegate() {
        return delegate;
    }
    
    /**
     * 
     */
    public void init() {
        
    }

    /**
     * @param jndiName the jndiName to set
     */
    public void setJndiName(String jndiName) {
        this.jndiName = jndiName;
    }

    /**
     * @param providerUrl the providerUrl to set
     */
    public void setProviderUrl(String providerUrl) {
        this.providerUrl = providerUrl;
    }

    /**
     * @param initialContextFactory the initialContextFactory to set
     */
    public void setInitialContextFactory(String initialContextFactory) {
        this.initialContextFactory = initialContextFactory;
    }

    /**
     * @param env the env to set
     */
    public void setEnv(boolean env) {
        this.env = env;
    }
    

}
