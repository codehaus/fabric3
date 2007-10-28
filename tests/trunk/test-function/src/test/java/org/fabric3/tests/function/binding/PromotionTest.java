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
package org.fabric3.tests.function.binding;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

import org.fabric3.tests.function.common.HelloService;

/**
 * Verifies service and reference promotion.
 *
 * @version $Rev$ $Date$
 */
public class PromotionTest extends TestCase {
    private HelloService helloService;

    @Reference
    public void setHelloService(HelloService helloService) {
        this.helloService = helloService;
    }

    public void testReferenceIsBound() {
        assertEquals("hello", helloService.send("hello"));
    }
}
