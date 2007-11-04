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
package org.fabric3.tests.function.composite;

import junit.framework.TestCase;
import org.osoa.sca.annotations.Reference;

import org.fabric3.tests.function.common.IdentityService;
import org.fabric3.tests.function.common.Source;

/**
 * @version $Rev$ $Date$
 */
public class InnerCompositeTest extends TestCase {
    @Reference
    protected IdentityService inner;

    @Reference
    protected IdentityService innerSpecified;

    @Reference
    protected Source boundSource;

    public void testInnerComposite() {
        assertEquals("innerOne", inner.getIdentity());
        assertEquals("two", innerSpecified.getIdentity());
        assertEquals("test", boundSource.invoke("test"));
    }
}
