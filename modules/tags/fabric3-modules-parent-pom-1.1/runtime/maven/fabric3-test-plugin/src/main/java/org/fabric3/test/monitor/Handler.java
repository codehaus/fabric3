/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the ñLicenseî), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an ñas isî basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 * --- Original Apache License ---
 *
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
package org.fabric3.test.monitor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.maven.plugin.logging.Log;

/**
 *
 */
class Handler implements InvocationHandler {
    
    private final Map<Method, MethodHandler> handlers = new HashMap<Method, MethodHandler>();
    
    void addMethodHandler(Method method, Log log, String message, int value, int throwableIndex) {
        handlers.put(method, new MethodHandler(log, message, value, throwableIndex));
    }

    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
        MethodHandler handler = handlers.get(method);
        if (handler != null) {
            handler.invoke(objects);
        }
        return null;
    }
    
    private class MethodHandler {
        
        private final Log log;
        private final String message;
        private final int value;
        private final int throwableIndex;

        private MethodHandler(Log log, String message, int value, int throwableIndex) {
            this.log = log;
            this.message = message;
            this.value = value;
            this.throwableIndex = throwableIndex;
        }

        private void invoke(Object[] objects) {
            
            String formattedMessage = MessageFormat.format(message, objects);
            if (throwableIndex == -1) {
                if (value >= Level.SEVERE.intValue() && log.isErrorEnabled()) {
                    log.error(formattedMessage);
                } else if (value >= Level.WARNING.intValue() && log.isWarnEnabled()) {
                    log.warn(formattedMessage);
                } else if (value >= Level.INFO.intValue() && log.isInfoEnabled()) {
                    log.info(formattedMessage);
                } else if (log.isDebugEnabled()){
                    log.debug(formattedMessage);
                }
            } else {
                Throwable throwable = (Throwable) objects[throwableIndex];
                if (value >= Level.SEVERE.intValue() && log.isErrorEnabled()) {
                    log.error(formattedMessage, throwable);
                } else if (value >= Level.WARNING.intValue() && log.isWarnEnabled()) {
                    log.warn(formattedMessage, throwable);
                } else if (value >= Level.INFO.intValue() && log.isInfoEnabled()) {
                    log.info(formattedMessage, throwable);
                } else if (log.isDebugEnabled()){
                    log.debug(formattedMessage, throwable);
                }
            }
            
        }

    }
    
}
