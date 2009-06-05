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
package org.fabric3.admin.interpreter.parser;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.fabric3.admin.interpreter.Command;
import org.fabric3.admin.interpreter.ParseException;

public class ParserHelper {

    private ParserHelper() {
    }

    /**
     * Parses authorization parameters.
     *
     * @param command the command being parsed.
     * @param tokens  the input parameters in token form
     * @param index   the starting paramter index to parse
     * @throws ParseException if there is an error parsing the parameters
     */
    public static void parseAuthorization(Command command, String[] tokens, int index) throws ParseException {
        if ("-u".equals(tokens[index])) {
            command.setUsername(tokens[index + 1]);
        } else if ("-p".equals(tokens[index])) {
            command.setPassword(tokens[index + 1]);
        } else {
            throw new ParseException("Unrecognized parameter: " + tokens[index]);
        }
        if ("-u".equals(tokens[index + 2])) {
            command.setUsername(tokens[index + 3]);
        } else if ("-p".equals(tokens[index + 2])) {
            command.setPassword(tokens[index + 3]);
        } else {
            throw new ParseException("Unrecognized parameter: " + tokens[index + 2]);
        }

    }

    /**
     * Parses a URL input parameter.
     *
     * @param value the value to parse.
     * @return the URL
     * @throws MalformedURLException if the value is an invalid URL
     */
    public static URL parseUrl(String value) throws MalformedURLException {
        if (!value.contains(":/")) {
            // assume it is a file
            return new File(value).toURI().toURL();
        } else {
            return new URL(value);
        }
    }
}
