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
package org.fabric3.binding.aq.wire;

import java.util.List;

import org.fabric3.api.annotation.Management;


/**
 *  MBean for Source Wire Attacher
 * @version $Revision: 3125 $ $Date: 2008-03-16 17:01:06 +0000 (Sun, 16 Mar 2008) $
 */
@Management
public interface AQSourceWireAttacherMBean {
    
    /**
     * Start the wire attacher
     */
    void start(String serviceNamespace);
    
    /**
     * Stop the wire attacher
     */
    void stop();
    
    
    /**
     * Gets the List Service names regsistered
     * @return
     */
    List<String> getServiceNames();
    
}
