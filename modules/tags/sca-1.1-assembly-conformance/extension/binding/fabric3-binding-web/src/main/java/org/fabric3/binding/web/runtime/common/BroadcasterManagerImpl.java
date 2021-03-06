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
package org.fabric3.binding.web.runtime.common;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.atmosphere.cpr.Broadcaster;
import org.oasisopen.sca.ServiceRuntimeException;
import org.oasisopen.sca.annotation.EagerInit;
import org.oasisopen.sca.annotation.Reference;

import org.fabric3.spi.model.type.java.JavaClass;
import org.fabric3.spi.model.type.json.JsonType;
import org.fabric3.spi.transform.TransformationException;
import org.fabric3.spi.transform.Transformer;
import org.fabric3.spi.transform.TransformerRegistry;

/**
 * Default implementation of the BroadcasterManager.
 *
 * @version $Rev$ $Date$
 */
@EagerInit
public class BroadcasterManagerImpl implements BroadcasterManager {
    private static final JsonType<Object> JSON_TYPE = new JsonType<Object>(String.class, Object.class);

    private TransformerRegistry registry;
    private Transformer<Object, String> jsonTransformer;
    private Map<String, Broadcaster> broadcasters = new ConcurrentHashMap<String, Broadcaster>();

    public BroadcasterManagerImpl(@Reference TransformerRegistry registry) {
        this.registry = registry;
    }

    public Broadcaster getChannelBroadcaster(String path) {
        Broadcaster broadcaster = broadcasters.get(path);
        if (broadcaster == null) {
            initializeTransformer();
            broadcaster = new ChannelBroadcaster(path, jsonTransformer);
            broadcasters.put(path, broadcaster);
        }
        return broadcaster;
    }

    public Broadcaster getServiceBroadcaster(String path) {
        Broadcaster broadcaster = broadcasters.get(path);
        if (broadcaster == null) {
            initializeTransformer();
            broadcaster = new ServiceBroadcaster(path);
            broadcasters.put(path, broadcaster);
        }
        return broadcaster;
    }

    public void remove(String path) {
        broadcasters.remove(path);
    }

    @SuppressWarnings({"unchecked"})
    public void initializeTransformer() {
        if (jsonTransformer != null) {
            return;
        }
        try {
            JavaClass<Object> javaType = new JavaClass<Object>(Object.class);
            List<Class<?>> list = Collections.emptyList();
            jsonTransformer = (Transformer<Object, String>) registry.getTransformer(javaType, JSON_TYPE, list, list);
            if (jsonTransformer == null) {
                throw new ServiceRuntimeException("JSON transformer not found. Ensure that the JSON databinding extension is installed");
            }
        } catch (TransformationException e) {
            throw new ServiceRuntimeException(e);
        }
    }

}
