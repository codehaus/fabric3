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
package org.fabric3.fabric.services.contribution.manifest;

/**
 * Parses OSGi headers, which are of the form:
 * <pre>
 *    header ::= clause ( ’,’ clause )
 *    clause ::= path ( ’;’ path ) *
 *                 ( ’;’ parameter ) *
 * </pre>
 *
 * @version $Revision$ $Date$
 */
public class OSGiManifestEntryParser {
    public enum EventType {
        /**
         * The parser enters the BEGIN state when its cursor is positioned at the first header character
         */
        BEGIN,

        /**
         * The parser enters the PATH state when an OSGi header path is read
         */
        PATH,

        /**
         * The parser enters the PARAMETER state when an OSGi header parameter is read
         */
        PARAMETER,

        /**
         * The parser enters the END state when cursor is positioned past the last header character
         */
        END
    }

    private static char PARAMETER_SEPARATOR = ';';
    private static char SEPARATOR = ',';

    // the OSGi header text
    private String header;

    // the current parser state
    private EventType state;

    // the internal character buffer
    private StringBuilder text;

    // the current position the parser is at reading the header
    private int pos = 0;

    // true if the parser is currently evaluating a version range, of the form '[' or '('
    private boolean inRange;

    /**
     * Constructor.
     *
     * @param header the OSGi header to parse
     */
    public OSGiManifestEntryParser(String header) {
        assert header != null;
        this.header = header;
        state = EventType.BEGIN;
    }

    /**
     * Advances the cursor to the next event.
     *
     * @return the event type.
     */
    public EventType next() {
        if (EventType.PATH == state || EventType.PARAMETER == state) {
            text = null;
        }
        if (pos <= header.length() - 1) {
            while (pos <= header.length() - 1) {
                char c = header.charAt(pos);
                ++pos;
                if (PARAMETER_SEPARATOR == c) {
                    inRange = false;
                    if (EventType.PATH == state || EventType.BEGIN == state) {
                        state = EventType.PARAMETER;
                        return EventType.PATH;
                    }
                    return state;
                } else if (SEPARATOR == c) {
                    if (inRange) {
                        appendNoWhiteSpace(c);

                    } else {
                        EventType current = state;
                        state = EventType.PATH;
                        return current;
                    }

                } else {
                    if (c == '[' || c == '(') {
                        inRange = true;
                    } else if (c == ']' || c == ')') {
                        inRange = false;
                    }
                    appendNoWhiteSpace(c);
                }
            }
            return state;
        } else {
            state = EventType.END;
            return state;
        }
    }

    /**
     * Returns the text value for the current cursor position for EventType.PATH and EventType.PARAMETER events.
     *
     * @return the text value for the current cursor position
     * @throws IllegalStateException if the parser is not in the EventType.PATH or EventType.PARAMETER state.
     */
    public String getText() {
        if (state != EventType.PATH && state != EventType.PARAMETER) {
            throw new IllegalStateException("Invalid state:" + state);
        }
        return text.toString();
    }

    /**
     * Strips whitespace and newline characters.
     *
     * @param c the character to append to the current text buffer.
     */
    private void appendNoWhiteSpace(char c) {
        if (text == null) {
            text = new StringBuilder();
        }
        if (Character.isWhitespace(c) || c == '\n') {
            return;
        }
        text.append(c);
    }

}
