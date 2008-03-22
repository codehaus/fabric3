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
package org.fabric3.spi.component;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class GroupInitializationExceptionTestCase extends TestCase {
    private PrintWriter writer;
    private PrintStream printer;
    private Exception cause1;
    private Exception cause2;
    private List<Exception> causes;
    private GroupInitializationException e;

    public void testCauses() {
        assertTrue(e.getCauses().contains(cause1));
        assertTrue(e.getCauses().contains(cause2));
    }
    
    // commented out to prevent confusing stack traces in the build log - uncomment to verify output
/*
    public void testPrintStackTraceToWriter() {
        e.printStackTrace(writer);
    }

    public void testPrintStackTraceToStream() {
        e.printStackTrace(printer);
    }

    public void testPrintStackTrace() {
        e.printStackTrace();
    }
*/

    protected void setUp() throws Exception {
        super.setUp();
        cause1 = new Exception("An Exception", new Exception("Nested Cause"));
        cause2 = new RuntimeException("A RuntimeException");
        causes = new ArrayList<Exception>();
        causes.add(cause1);
        causes.add(cause2);
        writer = new PrintWriter(System.err);
        printer = new PrintStream(System.err);
        e = new GroupInitializationException("test", causes);
    }
}
