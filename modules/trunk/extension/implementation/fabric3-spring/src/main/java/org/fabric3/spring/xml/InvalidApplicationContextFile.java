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
package org.fabric3.spring.xml;

import javax.xml.stream.XMLStreamReader;

import org.fabric3.introspection.xml.XmlValidationFailure;

/**
 * @version $Revision$ $Date$
 */
public class InvalidApplicationContextFile extends XmlValidationFailure<String> {
    private Throwable cause;

    protected InvalidApplicationContextFile(String message, String modelObject, Throwable cause, XMLStreamReader reader) {
        super(message, modelObject, reader);
        this.cause = cause;
    }

    public InvalidApplicationContextFile(String message, String modelObject, XMLStreamReader reader) {
        super(message, modelObject, reader);
    }

    public Throwable getCause() {
        return cause;
    }

    public String getMessage() {
        if (cause != null) {
            return super.getMessage() + ". Original cause was: \n" + cause;
        } else {
            return super.getMessage();
        }
    }
}
