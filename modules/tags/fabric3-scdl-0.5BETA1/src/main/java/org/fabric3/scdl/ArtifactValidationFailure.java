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
package org.fabric3.scdl;

import java.util.List;
import java.util.ArrayList;

import org.fabric3.host.contribution.ValidationFailure;

/**
 * @version $Revision$ $Date$
 */
public class ArtifactValidationFailure extends ValidationFailure<Void> {
    private List<ValidationFailure> failures;
    private String artifactName;

    public ArtifactValidationFailure(String artifactName) {
        super(null);
        this.artifactName = artifactName;
        this.failures = new ArrayList<ValidationFailure>();
    }

    public String getArtifactName() {
        return artifactName;
    }

    public List<ValidationFailure> getFailures() {
        return failures;
    }

    public void addFailure(ValidationFailure failure) {
        failures.add(failure);
    }

    public void addFailures(List<ValidationFailure> failures) {
        this.failures.addAll(failures);
    }

}
