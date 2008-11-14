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
package org.fabric3.transform.dom2java;

import org.fabric3.spi.transform.TransformationException;

/**
 * Tests String to Double Transform
 */
public class  String2DoubleTestCase extends BaseTransformTest {

	/**
	 * Test of converting String to Double
	 */
	public void testDoubleTransform() {
		final String ANY_DOUBLE_NUMBER = "99919329323.00102345";
		final String xml = "<string_to_double>" + ANY_DOUBLE_NUMBER + "</string_to_double>";
		try {
			double convertedDouble = getStringToDouble().transform(getNode(xml), null);
			assertNotNull(convertedDouble);
            assertEquals(99919329323.00102345, convertedDouble);
		} catch (TransformationException te) {
			fail("Transform exception should not occur " + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * Test failure of converting String to Double
	 */
	public void testDoubleTransformFailure() {
	    final String NON_DOUBLE = "NOT DOUBLE";
		final String xml = "<string_to_double>" + NON_DOUBLE + "</string_to_double>";
		try {
			getStringToDouble().transform(getNode(xml), null);
			fail("Should not reach here something wrong in [ String2Double ] code");
		} catch (TransformationException te) {
			assertNotNull(te);
			assertTrue(NumberFormatException.class.isAssignableFrom(te.getCause().getClass()));
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}

	/**
	 * @return
	 */
	private String2Double getStringToDouble() {
		return new String2Double();
	}
}
