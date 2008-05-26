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
package org.fabric3.introspection.validation;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;

import org.fabric3.host.contribution.ValidationFailure;
import org.fabric3.scdl.ArtifactValidationFailure;

/**
 * @version $Revision$ $Date$
 */
public final class ValidationUtils {
    private static ValidationExceptionComparator COMPARATOR = new ValidationExceptionComparator();

    private static enum TYPE {
        WARNING,
        ERROR
    }

    private ValidationUtils() {
    }

    /**
     * Sorts and writes the list of error messages to a string.
     *
     * @param failures the collection of failures to write
     * @return the string containing the validation messages
     */
    public static String outputErrors(List<ValidationFailure> failures) {
        return output(failures, TYPE.ERROR);
    }

    /**
     * Sorts and writes the list of warning messages to a string.
     *
     * @param failures the collection of failures to write
     * @return the string containing the validation messages
     */
    public static String outputWarnings(List<ValidationFailure> failures) {
        return output(failures, TYPE.WARNING);
    }

    /**
     * Sorts and writes the list of errors to the given writer.
     *
     * @param writer   the writer
     * @param failures the collection of failures to write
     */
    public static void writeErrors(PrintWriter writer, List<ValidationFailure> failures) {
        write(writer, failures, TYPE.ERROR);
    }

    /**
     * Sorts and writes the list of warnings to the given writer.
     *
     * @param writer   the writer
     * @param failures the collection of failures to write
     */
    public static void writeWarnings(PrintWriter writer, List<ValidationFailure> failures) {
        write(writer, failures, TYPE.WARNING);
    }

    private static String output(List<ValidationFailure> failures, TYPE type) {
        ByteArrayOutputStream bas = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(bas);
        write(writer, failures, type);
        return bas.toString();
    }

    private static void write(PrintWriter writer, List<ValidationFailure> failures, TYPE type) {
        int count = 0;
        List<ValidationFailure> sorted = new ArrayList<ValidationFailure>(failures);
        // sort the errors so that ArtifactValidationFailures are evaluated last. This is done so that nested failures are printed after all
        // failures in the parent artifact.
        Collections.sort(sorted, COMPARATOR);
        for (ValidationFailure failure : sorted) {
            count = writerError(failure, writer, count, type);
        }
        if (count == 1) {
            if (type == TYPE.ERROR) {
                writer.write("1 error was found \n\n");
            } else {
                writer.write("1 warning was found \n\n");
            }
        } else {
            writer.write(count + " errors were found \n\n");
        }
        writer.flush();
    }

    private static int writerError(ValidationFailure failure, PrintWriter writer, int count, TYPE type) {
        if (failure instanceof ArtifactValidationFailure) {
            ArtifactValidationFailure artifactFailure = (ArtifactValidationFailure) failure;
            if (!errorsOnlyInContainedArtifacts(artifactFailure)) {
                if (type == TYPE.ERROR) {
                    writer.write("Errors in " + artifactFailure.getArtifactName() + "\n\n");
                } else {
                    writer.write("Warnings in " + artifactFailure.getArtifactName() + "\n\n");
                }
            }
            for (ValidationFailure childFailure : artifactFailure.getFailures()) {
                count = writerError(childFailure, writer, count, type);
            }
        } else {
            if (type == TYPE.ERROR) {
                writer.write("  ERROR: " + failure.getMessage() + "\n\n");
            } else {
                writer.write("  WARNING: " + failure.getMessage() + "\n\n");
            }
            ++count;
        }
        return count;
    }

    private static boolean errorsOnlyInContainedArtifacts(ArtifactValidationFailure artifactFailure) {
        for (ValidationFailure failure : artifactFailure.getFailures()) {
            if (!(failure instanceof ArtifactValidationFailure)) {
                return false;
            }
        }
        return true;
    }

}
