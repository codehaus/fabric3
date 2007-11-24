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
package org.fabric3.host.monitor;

/**
 * A registry for exception formatters
 *
 * @version $Rev$ $Date$
 */
public interface FormatterRegistry {
    /**
     * Registers a formatter for a type of exception.
     *
     * @param type the type of exception the formatter can handle
     * @param formatter the formatter to register
     */
    <T extends Throwable> void register(Class<T> type, ExceptionFormatter<? super T> formatter);

    /**
     * Unregisters the given formatter
     *
     * @param type the type of formatter to unregister
     */
    void unregister(Class<?> type);

    /**
     * Return the formatter for a type of exception.
     *
     * @param type the type of exception
     * @return a formatter that can handle that type
     */
    <T extends Throwable> ExceptionFormatter<? super T> getFormatter(Class<? extends T> type);
}
