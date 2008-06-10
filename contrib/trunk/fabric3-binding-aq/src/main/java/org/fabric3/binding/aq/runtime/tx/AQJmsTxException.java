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
package org.fabric3.binding.aq.runtime.tx;

import org.fabric3.host.Fabric3RuntimeException;
/**
 * @version $Revision: 2902 $ $Date: 2008-02-26 09:26:36 +0000 (Tue, 26 Feb 2008) $
 */
public class AQJmsTxException extends Fabric3RuntimeException {
   
    private static final long serialVersionUID = 1628828589986749176L;
    
    
    /**
     * Construct a JmsTxException by a given message and the underlying cause
     * @param message
     * @param cause
     */
    public AQJmsTxException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Construct a JmsTxException by a message
     * @param message
     */
    public AQJmsTxException(String message) {
        super(message);
    }

    /**
     * Construct a JmsTxException by the underlying cause
     * @param cause
     */
    public AQJmsTxException(Throwable cause) {
        super(cause);
    }

}
