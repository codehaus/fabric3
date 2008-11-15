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
package org.fabric3.spi.introspection.validation;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.fabric3.scdl.ValidationFailure;

/**
 * @version $Revision$ $Date$
 */
public class ValidationUtilsTestCase extends TestCase {
    private List<ValidationFailure> failures;

    public void testWriteErrors() throws Exception {
        String output = ValidationUtils.outputErrors(failures);
        assertTrue(output.indexOf("this is a test") > 0);
    }

    public void testWriteWarnings() throws Exception {
        String output = ValidationUtils.outputWarnings(failures);
        assertTrue(output.indexOf("this is a test") > 0);
    }

    protected void setUp() throws Exception {
        super.setUp();
        failures = new ArrayList<ValidationFailure>();
        failures.add(new Failure());
    }

    private static class Failure extends ValidationFailure<Object> {

        public Failure() {
            super(null);
        }

        public String getMessage() {
            return "this is a test";
        }
    }
}
