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
package org.fabric3.spi.introspection.xml;

import javax.xml.stream.XMLStreamReader;

/**
 * Indicates the loader encountered an invalid value.
 *
 * @version $Rev$ $Date$
 */
public class InvalidValue extends XmlValidationFailure<String> {
    private Throwable cause;

    public InvalidValue(String message, String value, XMLStreamReader reader) {
        super(message, value, reader);
    }

    public InvalidValue(String message, String value, XMLStreamReader reader, Throwable cause) {
        super(message, value, reader);
        this.cause = cause;
    }

    public Throwable getCause() {
        return cause;
    }

    public String getMessage() {
        if (cause != null) {
            return super.getMessage() + ". The original error was: \n" + cause.toString();
        } else {
            return super.getMessage();
        }
    }
}
