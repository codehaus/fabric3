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
package org.oasisopen.sca;

/**
 * A ServiceReference represents a client's perspective of a reference to another service.
 *
 * @version $Rev: 1 $ $Date: 2007-05-14 10:40:37 -0700 (Mon, 14 May 2007) $
 * @param <B> the Java interface associated with this reference
 */
public interface ServiceReference<B> extends java.io.Serializable {
    /**
     * Returns a typed reference to the target of this reference.
     *
     * @return a typed reference to the target of this reference.
     */
    B getService();

    /**
     * Returns the interface type associated with this reference.
     *
     * @return the the interface type associated with this reference
     */
    Class<B> getBusinessInterface();
}