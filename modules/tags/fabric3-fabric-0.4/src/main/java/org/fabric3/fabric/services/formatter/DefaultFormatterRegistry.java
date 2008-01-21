/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.fabric.services.formatter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.fabric3.spi.services.formatter.ExceptionFormatter;
import org.fabric3.spi.services.formatter.FormatterRegistry;

/**
 * @version $Rev$ $Date$
 */
public class DefaultFormatterRegistry implements FormatterRegistry {
    private final Map<Class<?>, ExceptionFormatter<?>> formatters;

    public DefaultFormatterRegistry() {
        this.formatters = new ConcurrentHashMap<Class<?>, ExceptionFormatter<?>>();
        register(Throwable.class, new DefaultExceptionFormatter());
    }

    public <T extends Throwable> void register(Class<T> type, ExceptionFormatter<? super T> formatter) {
        formatters.put(type, formatter);
    }

    public void unregister(Class<?> type) {
        formatters.remove(type);
    }

    public <T extends Throwable> ExceptionFormatter<? super T> getFormatter(Class<? extends T> type) {
        Class<?> clazz = type;
        while (true) {
            @SuppressWarnings("unchecked")
            ExceptionFormatter<? super T> formatter = (ExceptionFormatter<? super T>) formatters.get(clazz);
            if (formatter != null) {
                return formatter;
            }
            clazz = clazz.getSuperclass();
            assert Object.class != clazz;
        }
    }
}
