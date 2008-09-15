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
package org.fabric3.binding.jms.introspection;

import org.fabric3.binding.jms.common.CreateOption;
import org.fabric3.binding.jms.common.JmsBindingMetadata;
import org.fabric3.binding.jms.common.JmsURIMetadata;

import junit.framework.TestCase;

public class JmsLoaderHelperTestCase extends TestCase {

    public void testGetJmsMetadataFromURI() throws Exception {
        JmsURIMetadata meta = JmsURIMetadata
                .parseURI("jms:dest?connectionFactoryName=factory&deliveryMode=PERSISTENT");
        JmsBindingMetadata bindingMetadata = JmsLoaderHelper
                .getJmsMetadataFromURI(meta);
        assertEquals("dest", bindingMetadata.getDestination().getName());
        assertEquals("factory", bindingMetadata.getConnectionFactory()
                .getName());
        assertEquals(CreateOption.never, bindingMetadata.getDestination()
                .getCreate());

        meta = JmsURIMetadata.parseURI("jms:dest?deliveryMode=PERSISTENT");
        bindingMetadata = JmsLoaderHelper.getJmsMetadataFromURI(meta);
        assertEquals("connectionFactory", bindingMetadata
                .getConnectionFactory().getName());
        assertEquals("clientQueue", bindingMetadata
                .getResponseDestination().getName());
    }

}
