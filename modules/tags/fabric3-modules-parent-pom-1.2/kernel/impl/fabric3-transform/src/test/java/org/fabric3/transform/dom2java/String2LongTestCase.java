  /*
   * Fabric3
   * Copyright (c) 2009 Metaform Systems
   *
   * Fabric3 is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as
   * published by the Free Software Foundation, either version 3 of
   * the License, or (at your option) any later version, with the
   * following exception:
   *
   * Linking this software statically or dynamically with other
   * modules is making a combined work based on this software.
   * Thus, the terms and conditions of the GNU General Public
   * License cover the whole combination.
   *
   * As a special exception, the copyright holders of this software
   * give you permission to link this software with independent
   * modules to produce an executable, regardless of the license
   * terms of these independent modules, and to copy and distribute
   * the resulting executable under terms of your choice, provided
   * that you also meet, for each linked independent module, the
   * terms and conditions of the license of that module. An
   * independent module is a module which is not derived from or
   * based on this software. If you modify this software, you may
   * extend this exception to your version of the software, but
   * you are not obligated to do so. If you do not wish to do so,
   * delete this exception statement from your version.
   *
   * Fabric3 is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty
   * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
   * See the GNU General Public License for more details.
   *
   * You should have received a copy of the
   * GNU General Public License along with Fabric3.
   * If not, see <http://www.gnu.org/licenses/>.
   */
package org.fabric3.transform.dom2java;

import org.fabric3.spi.transform.TransformationException;

/**
 * Tests String to Integer Transform
 */
public class  String2LongTestCase extends BaseTransformTest {

	/**
	 * Test of converting String to Long
	 */
	public void testLongTransform() {
		final String ANY_LONG = "9965732839230";
		final String xml = "<string_to_long>" + ANY_LONG + "</string_to_long>";
		try {
			final long convertedLong = getStringToLong().transform(getNode(xml), null);
			assertNotNull(convertedLong);
            assertEquals(9965732839230l, convertedLong);
		} catch (TransformationException te) {
			fail("Transform exception should not occur " + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * Test failure of converting String to Integer
	 */
	public void testLongTransformFailure() {
	    final String NON_INTEGER = "11l";
		final String xml = "<string_to_long>" + NON_INTEGER + "</string_to_long>";
		try {
			getStringToLong().transform(getNode(xml), null);
			fail("Should not reach here something wrong in [ String2Long ] code");
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
	private String2Long getStringToLong() {
		return new String2Long();
	}
}
