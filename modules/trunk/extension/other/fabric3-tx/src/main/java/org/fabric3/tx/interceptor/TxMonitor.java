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
package org.fabric3.tx.interceptor;

import org.fabric3.api.annotation.LogLevel;

/**
 *
 * @version $Revision$ $Date$
 */
public interface TxMonitor {
    
    @LogLevel("INFO")
    void started(int hashCode);
    
    @LogLevel("INFO")
    void committed(int hashCode);
    
    @LogLevel("INFO")
    void suspended(int hashCode);
    
    @LogLevel("INFO")
    void rolledback(int hashCode);
    
    @LogLevel("INFO")
    void resumed(int hashCode);
    
    @LogLevel("INFO")
    void markedForRollback(int hashCode);
    
    @LogLevel("INFO")
    void interceptorInitialized(TxAction txAction);
    
    @LogLevel("INFO")
    void joined(int hashCode);

}
