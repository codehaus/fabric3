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

import java.util.Collections;
import java.util.List;

/**
 * Represents the risk associated with a loan calculated by a RiskAssessmentService.
 *
 * @version $Revision$ $Date$
 */
public class RiskAssessment {
    public static final int APPROVE = 1;
    public static final int REJECT = -1;

    private int decision;
    private int factor;
    private List<String> reasons;

    public RiskAssessment(int decision, int factor, List<String> reasons) {
        this.decision = decision;
        this.factor = factor;
        this.reasons = reasons;
    }

    public RiskAssessment() {
    }

    public int getRiskFactor() {
        return factor;
    }

    public void setRiskFactor(int factor) {
        this.factor = factor;
    }

    public List<String> getReasons() {
        return Collections.unmodifiableList(reasons);
    }

    public void setReasons(List<String> reasons) {
        this.reasons = reasons;
    }

    public int getDecision() {
        return decision;
    }

    public void setDecision(int decision) {
        this.decision = decision;
    }

}
