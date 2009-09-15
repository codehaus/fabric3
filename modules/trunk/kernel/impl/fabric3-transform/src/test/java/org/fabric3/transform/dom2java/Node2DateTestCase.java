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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.fabric3.spi.transform.TransformationException;

/**
 * Tests String to Date Transform
 */
public class Node2DateTestCase extends BaseTransformTest {

	/**
	 * Test of converting String to Date
	 */
	public void testDateTransform() {
		final String MID_JAN = "20/01/2007";
		final String xml = "<string_to_date>" + MID_JAN + "</string_to_date>";
		try {
			Date date = getStringToDate().transform(getNode(xml), null);
			assertNotNull(date);
            assertEquals(MID_JAN, DateToString(date));
		} catch (TransformationException te) {
			fail("Transform exception should not occur " + te);
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}
	
	/**
	 * Test failure of converting String to Date
	 */
	public void testDateTransformFailure() {
		final String WRONG_DATE = "20/2007/01";
		final String xml = "<string_to_date>" + WRONG_DATE + "</string_to_date>";
		try {
			getStringToDate().transform(getNode(xml), null);
			fail("Should not reach here something wrong in [ String2Date ] code");
		} catch (TransformationException te) {
			assertNotNull(te);
			assertTrue(ParseException.class.isAssignableFrom(te.getCause().getClass()));
		} catch (Exception e) {
			fail("Unexpexcted Exception Should not occur " + e);
		}
	}

	/**
	 * @return
	 */
	private Node2DateTransformer getStringToDate() {
		return new Node2DateTransformer();
	}

	/**
	 * @param date
	 * @return String from Date Object
	 */
	private String DateToString(final Date date) {
		final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		final String convertedDate = sdf.format(date);
		return convertedDate;
	}
}
