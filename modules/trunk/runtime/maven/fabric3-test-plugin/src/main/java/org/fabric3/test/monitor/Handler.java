/*
 * Fabric3
 * Copyright (c) 2009 Metaform Systems
 *
 * Fabric3 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version, with the
 * following exception:
 *
 * Linking this software statically or dynamically with other
 * modules is making a combined work based on this software.
 * Thus, the terms and conditions of the GNU General Public
 * License cover the whole combination.
 *
 * As a special exception, the copyright holders of this software
 * give you permission to link this software with independent
 * modules to produce an executable, regardless of the license
 * terms of these independent modules, and to copy and distribute
 * the resulting executable under terms of your choice, provided
 * that you also meet, for each linked independent module, the
 * terms and conditions of the license of that module. An
 * independent module is a module which is not derived from or
 * based on this software. If you modify this software, you may
 * extend this exception to your version of the software, but
 * you are not obligated to do so. If you do not wish to do so,
 * delete this exception statement from your version.
 *
 * Fabric3 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the
 * GNU General Public License along with Fabric3.
 * If not, see <http://www.gnu.org/licenses/>.
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
                } else if (log.isDebugEnabled()) {
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
                } else if (log.isDebugEnabled()) {
                    log.debug(formattedMessage, throwable);
                }
            }

        }

    }

}
