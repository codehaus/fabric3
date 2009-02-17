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

import loanapp.message.RiskResponse;
import org.oasisopen.sca.annotation.OneWay;

/**
 * Interface to receive risk assessment callbacks.
 *
 * @version $Revision$ $Date$
 */
public interface RiskAssessmentCallback {

    /**
     * Notification when a risk assessment result has been received.
     *
     * @param result the assessment result
     */
    @OneWay
    void onAssessment(RiskResponse result);

    /**
     * Notificaiton when a error was encountered during risk assessment.
     *
     * @param exception the error
     */
    @OneWay
    void riskAssessmentError(Exception exception);

}
