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
package org.fabric3.tests.function.lifecycle;

import junit.framework.TestCase;
import org.osoa.sca.ComponentContext;
import org.osoa.sca.RequestContext;
import org.osoa.sca.annotations.Context;
import org.osoa.sca.annotations.Property;

/**
 * @version $Rev$ $Date$
 */
public class ContextTest extends TestCase {

    @Context
    protected RequestContext requestContext;

    @Context
    protected ComponentContext componentContext;

    @Property
    protected String uri;

    public void testRequestContext() {
        assertNotNull(requestContext);
    }

    public void testComponentContext() {
        assertNotNull(componentContext);
        assertEquals(uri, componentContext.getURI());
    }
}
