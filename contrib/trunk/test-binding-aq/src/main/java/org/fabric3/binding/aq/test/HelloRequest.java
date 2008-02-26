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
package org.fabric3.binding.aq.test;

import java.io.Serializable;
import java.util.Date;

/**
 * @version $Revision$ $Date$
 */
public class HelloRequest implements Serializable {
    
    /** SERIAL ID*/
    private static final long serialVersionUID = -3896071380449163733L;
        
    /** Message for Request*/
    private final String message;
    
    /** Date for the Message set*/
    private final Date date;
        
    public HelloRequest(final String message, final Date date){
        this.message = message;
        this.date = date;
    }
    
    /**     
     * @return Date
     */
    public Date getDate() {
        return date;
    }        
    
    /**
     * @return Returns the message.
     */
    protected String getReq() {
        return message;
    }

}
