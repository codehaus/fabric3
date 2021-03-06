/*
 * Fabric3
 * Copyright (c) 2009-2011 Metaform Systems
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
*/
package org.fabric3.fabric.channel;


import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.oasisopen.sca.ServiceRuntimeException;

import org.fabric3.model.type.contract.DataType;
import org.fabric3.spi.channel.EventStreamHandler;
import org.fabric3.spi.channel.EventWrapper;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 * An {@link EventStreamHandler} that transforms wrapped events to a type expected by the consumer.
 * <p/>
 * If the event is not wrapped, no transformation is done. This implementation also lazily creates transformers and caches them for reuse as binding
 * contexts such as JAXB can be expensive to create.
 *
 * @version $Rev$ $Date$
 */
public class TransformerHandler implements EventStreamHandler {
    private EventStreamHandler next;
    private DataType<Object> targetType;
    private TransformerRegistry registry;
    private List<Class<?>> typeList;

    private Map<DataType<?>, Transformer<Object, Object>> cache;

    public TransformerHandler(DataType<Object> targetType, TransformerRegistry registry) {
        this.targetType = targetType;
        this.registry = registry;
        Class<?> clazz = targetType.getPhysical();
        typeList = cast(Collections.singletonList(clazz));
    }

    @SuppressWarnings({"unchecked"})
    public void handle(Object event) {
        if (event instanceof EventWrapper) {
            if (cache == null) {
                cache = new ConcurrentHashMap<DataType<?>, Transformer<Object, Object>>();
            }
            EventWrapper wrapper = (EventWrapper) event;

            // check to see if the transformed event is cached
            Object content = ((EventWrapper) event).getEvent(targetType);
            if (content == null) {
                try {
                    DataType<?> type = wrapper.getType();
                    ClassLoader loader = targetType.getClass().getClassLoader();
                    Transformer<Object, Object> transformer = cache.get(type);
                    if (transformer == null) {
                        transformer = (Transformer<Object, Object>) registry.getTransformer(type, targetType, typeList, typeList);
                        cache.put(type, transformer);
                    }
                    content = transformer.transform(wrapper.getEvent(), loader);
                    wrapper.cache(targetType, content);
                } catch (TransformationException e) {
                    throw new ServiceRuntimeException(e);
                }
            }
            // TODO optimize
            event = new Object[]{content};
        }
        next.handle(event);
    }

    public void setNext(EventStreamHandler next) {
        this.next = next;
    }

    public EventStreamHandler getNext() {
        return next;
    }

    @SuppressWarnings({"unchecked"})
    private <T> T cast(Object o) {
        return (T) o;
    }
}
