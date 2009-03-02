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
 * Tests String to Integer Transform
 */
public class  String2ShortTestCase extends BaseTransformTest {

	/**
	 * Test of converting String to Short
	 */
	public void testShortTransform() {
		final String ANY_SHORT = "153";
		final String xml = "<string_to_short>" + ANY_SHORT + "</string_to_short>";
		try {
			final short convertedShort = getStringToShort().transform(getNode(xml), null);
			assertNotNull(convertedShort);
            assertEquals(153, convertedShort);
		} catch (TransformationException te) {
			fail("Transform exception should not occur " + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * Test failure of converting String to Short
	 */
	public void testShortTransformFailure() {
		final String INVALID_SHORT = "153908765";
		final String xml = "<string_to_short>" + INVALID_SHORT + "</string_to_short>";
		try {
			getStringToShort().transform(getNode(xml), null);
			fail("Should not reach here something wrong in [ String2Short ] code");
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
	private String2Short getStringToShort() {
		return new String2Short();
	}
}
