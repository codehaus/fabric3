/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.tests.function.conversation;

import junit.framework.TestCase;
import org.oasisopen.sca.annotation.Reference;

/**
 * @version $Rev$ $Date$
 */
public class ConversationTest extends TestCase {
    @Reference
    public ConversationalService service;

    @Reference
    public ConversationalService subService;

    @Reference
    public ConversationalService compositeService;

//    @Reference
//    public ConversationalService maxAgeService;

    @Reference
    public ConversationalService compositeSubService;

    public void testConversationIdIsInjected() {
        assertNotNull(service.getConversationId());
    }

    public void testStateIsRetained() {
        service.setValue("testStateIsRetained");
        assertEquals("testStateIsRetained", service.getValue());
        assertEquals("testStateIsRetained", service.getValue());
    }

    public void testEndConversation() {
        service.setValue("testEndConversation");
        assertEquals("testEndConversation", service.end());
        assertNull(service.getValue());
    }

    public void testConversationalityIsInherited() {
        subService.setValue("testConversationalityIsInherited");
        assertEquals("testConversationalityIsInherited", subService.getValue());
        assertEquals("testConversationalityIsInherited", subService.getValue());
    }

    public void testCompositeStateIsRetained() {
        compositeService.setValue("testCompositeStateIsRetained");
        assertEquals("testCompositeStateIsRetained", compositeService.getValue());
        assertEquals("testCompositeStateIsRetained", compositeService.getValue());
    }

    public void testCompositeEndConversation() {
        compositeService.setValue("testCompositeEndConversation");
        assertEquals("testCompositeEndConversation", compositeService.end());
        assertNull(compositeService.getValue());
    }

    public void testCompositeConversationalityIsInherited() {
        compositeSubService.setValue("testCompositeConversationalityIsInherited");
        assertEquals("testCompositeConversationalityIsInherited", compositeSubService.getValue());
        assertEquals("testCompositeConversationalityIsInherited", compositeSubService.getValue());
    }

//    public void testExpiration() throws Exception{
//        maxAgeService.setValue("Hello");
//        Thread.sleep(4000);
//        maxAgeService.setValue("Hello");
//    }

}
