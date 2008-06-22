/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
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
package org.fabric3.maven.contribution;

import org.fabric3.host.contribution.ValidationFailure;

import java.io.File;

/**
 * Validation warning indicating that the possible contribution file with the given File could not be loaded.
 *
 * @version $Rev$ $Date$
 */
public class ContributionIndexingFailure extends ValidationFailure<File> {
    private Exception ex;

    public ContributionIndexingFailure(File file, Exception ex) {
        super(file);
        this.ex = ex;
    }

    /**
     * Retrieves the message for the failure that includes both the standard ValidationFailure message along with
     * details of the exception.
     * @return the mesasge.
     */
    public String getMessage() {
        return super.getMessage() + " " + ex;
    }
}
