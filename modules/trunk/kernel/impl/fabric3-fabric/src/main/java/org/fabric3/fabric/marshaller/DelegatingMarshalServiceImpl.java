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
package org.fabric3.fabric.marshaller;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.osoa.sca.annotations.Service;

import org.fabric3.spi.marshaller.DelegatingMarshalService;
import org.fabric3.spi.marshaller.MarshalException;
import org.fabric3.spi.marshaller.MarshalService;

/**
 * Delegates to an underlying marshalling service.
 *
 * @version $Rev$ $Date$
 */
@Service(interfaces = {MarshalService.class, DelegatingMarshalService.class})
public class DelegatingMarshalServiceImpl implements DelegatingMarshalService {
    private MarshalService proxied;

    public void registerMarshalService(MarshalService service) {
        proxied = service;
    }

    public void marshall(Object object, XMLStreamWriter writer) throws MarshalException {
        proxied.marshall(object, writer);
    }

    public <T> T unmarshall(Class<T> type, XMLStreamReader reader) throws MarshalException {
        return proxied.unmarshall(type, reader);
    }
}
