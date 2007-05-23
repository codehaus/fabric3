package org.fabric3.host;

import java.io.PrintWriter;
import java.util.List;

/**
 * Groups multiple, potentially unrelated, exceptions thrown during an operation
 *
 * @version $Rev$ $Date$
 */
public interface GroupException {
    /**
     * Return the exceptions that occurred as the group was initialized.
     *
     * @return a list of exceptions that occurred
     */
    List<Exception> getCauses();

    /**
     * Appends a message to the writer
     *
     * @param writer the writer
     * @return the writer
     */
    PrintWriter appendBaseMessage(PrintWriter writer);

    void printStackTrace(PrintWriter writer);

}
