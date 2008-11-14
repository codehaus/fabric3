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
 * Tests String to Float Transform
 */
public class  String2FloatTestCase extends BaseTransformTest {

	/**
	 * Test of converting String to Float
	 */
	public void testFloatTransform() {
		final String ANY_FLOAT_NUMBER = "99.00";
		final String xml = "<string_to_float>" + ANY_FLOAT_NUMBER + "</string_to_float>";
		try {
			final double convertedFloat = getStringToFloat().transform(getNode(xml), null);
			assertNotNull(convertedFloat);
            assertEquals(99.00, convertedFloat);
		} catch (TransformationException te) {
			fail("Transform exception should not occur " + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * Test failure of converting String to Float
	 */
	public void testFloatTransformFailure() {
	    final String NON_FLOAT = "NON FLOAT";
		final String xml = "<string_to_float>" + NON_FLOAT + "</string_to_float>";
		try {
			getStringToFloat().transform(getNode(xml), null);
			fail("Should not reach here something wrong in [ String2Float ] code");
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
	private String2Float getStringToFloat() {
		return new String2Float();
	}
}
