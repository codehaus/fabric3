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

import org.fabric3.transform.TransformationException;

/**
 * Tests String to Byte Transform
 */
public class String2ByteTestCase extends BaseTransformTest {

	/**
	 * Test of converting String to Byte success
	 */
	public void testByteTransform() {
		final String BYTE = "125";
		final String xml = "<string_to_byte>" + BYTE + "</string_to_byte>";
		try {
			final byte convertedByte = getStringToByte().transform(getNode(xml), null);
			assertNotNull(convertedByte);
            assertEquals(Byte.valueOf(BYTE).byteValue(), convertedByte);
		} catch (TransformationException te) {
			fail("Transform exception should not occur " + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}

	/**
	 * Test failure of converting String to Byte
	 */
	public void testDateTransformFailure() {
		final String OUT_OF_RANGE_BYTE = "129";
		final String xml = "<string_to_byte>" + OUT_OF_RANGE_BYTE + "</string_to_byte>";
		try {
			getStringToByte().transform(getNode(xml), null);
			fail("Should not reach here something wrong in [ String2Byte ] code");
		} catch (TransformationException te) {
			assertNotNull(te);
			assertTrue(NumberFormatException.class.isAssignableFrom(te.getCause().getClass()));
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}

	/**
	 * @return StringToBoolean
	 */
	private String2Byte getStringToByte() {
		return new String2Byte();
	}
}
