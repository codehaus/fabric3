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
package loanapp.risk;

import java.util.List;
import java.util.Collections;

/**
 * @version $Revision$ $Date$
 */
public class RiskAssessmentResult {
    private final int riskFactor;
    private final List<String> reasons;

    public RiskAssessmentResult(int riskFactor, List<String> reasons) {
        this.riskFactor = riskFactor;
        this.reasons = reasons;
    }

    public int getRiskFactor() {
        return riskFactor;
    }

    public List<String> getReasons() {
        return Collections.unmodifiableList(reasons);
    }
}
