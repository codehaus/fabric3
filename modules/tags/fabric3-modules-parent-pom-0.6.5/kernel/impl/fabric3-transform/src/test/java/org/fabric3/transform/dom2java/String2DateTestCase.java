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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.fabric3.transform.TransformationException;

/**
 * Tests String to Date Transform
 */
public class String2DateTestCase extends BaseTransformTest {

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
	private String2Date getStringToDate() {
		return new String2Date();
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
