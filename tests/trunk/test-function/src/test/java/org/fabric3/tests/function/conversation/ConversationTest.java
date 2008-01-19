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
package org.fabric3.tests.function.conversation;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
public class ConversationTest extends TestCase {
    @Reference
    public ConversationalService service;

    @Reference
    public ConversationalSubservice subService;

    public void testStateIsRetained() {
        assertNull(service.getValue());
        service.setValue("Hello");
        assertEquals("Hello", service.getValue());
        assertEquals("Hello", service.getValue());
    }

    public void testEndConversation() {
        service.setValue("Hello");
        assertEquals("Hello", service.end());
        assertNull(service.getValue());
    }

    public void testConversationalityIsInherited() {
        assertNull(subService.getValue());
        subService.setValue("Hello");
        assertEquals("Hello", subService.getValue());
        assertEquals("Hello", subService.getValue());
    }
}
