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

import junit.framework.TestCase;
import org.easymock.EasyMock;

import org.fabric3.spi.services.formatter.ExceptionFormatter;
import org.fabric3.fabric.services.formatter.DefaultFormatterRegistry;

/**
 * @version $Rev$ $Date$
 */
public class DefaultFormatterRegistryTestCase extends TestCase {
    private DefaultFormatterRegistry registry;
    private ExceptionFormatter<Exception> formatter;

    public void testDefaultFormatter() {
        assertTrue(registry.getFormatter(AssertionError.class).getType().isAssignableFrom(AssertionError.class));
    }

    public void testSuperclassFormatter() {
        assertEquals(formatter, registry.getFormatter(RuntimeException.class));
    }

    @SuppressWarnings("unchecked")
    protected void setUp() throws Exception {
        super.setUp();
        formatter = EasyMock.createMock(ExceptionFormatter.class);
        registry = new DefaultFormatterRegistry();
        registry.register(Exception.class, formatter);
    }
}
