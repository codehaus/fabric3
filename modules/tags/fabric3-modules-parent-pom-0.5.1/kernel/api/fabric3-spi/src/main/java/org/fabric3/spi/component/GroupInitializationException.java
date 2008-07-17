/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */
package org.fabric3.spi.component;

import java.util.List;
import java.io.PrintWriter;
import java.io.PrintStream;

/**
 * @version $Rev$ $Date$
 */
public class GroupInitializationException extends ComponentException {
    private static final long serialVersionUID = 2049226987838195489L;
    private final List<Exception> causes;

    /**
     * Exception indicating a problem initializing a group of components.
     *
     * @param causes    the individual exceptions that occurred
     */
    public GroupInitializationException(List<Exception> causes) {
        super("Error initializing components");
        this.causes = causes;
    }

    /**
     * Return the exceptions that occurred as the group was initialized.
     *
     * @return a list of exceptions that occurred
     */
    public List<Exception> getCauses() {
        return causes;
    }

    /**
     * Override stacktrace output to include all causes.
     *
     * @param printStream the stream to write to
     */
    @Override
    public void printStackTrace(PrintStream printStream) {
        PrintWriter writer = new PrintWriter(printStream);
        printStackTrace(writer);
    }

    /**
     * Override stacktrace output to include all causes.
     *
     * @param writer the writer to use
     */
    @Override
    public void printStackTrace(PrintWriter writer) {
        writer.println(toString());
        printStackTraceElements(writer);
        writer.println("-------------------------------------------------------------------------------");
        for (Exception cause : causes) {
            writer.print("Caused by: ");
            cause.printStackTrace(writer);
            writer.println("-------------------------------------------------------------------------------");
        }
        writer.flush();
    }
}
