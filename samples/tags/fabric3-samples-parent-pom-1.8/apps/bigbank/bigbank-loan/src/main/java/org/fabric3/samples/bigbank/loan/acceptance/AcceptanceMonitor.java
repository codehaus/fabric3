/*
 * Copyright (c) 2010 Metaform Systems
 *
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
package org.fabric3.samples.bigbank.loan.acceptance;

import org.fabric3.api.annotation.monitor.Info;
import org.fabric3.api.annotation.monitor.Severe;

/**
 * @version $Revision$ $Date$
 */
public interface AcceptanceMonitor {

    @Severe("The following error occurred")
    void onError(Throwable e);

    @Info("Loan application accepted for {0}")
    void accepted(long id);

    @Info("Loan application declined for {0}")
    void declined(long id);

    @Info("Appraisal scheduled for {0}")
    void appraisalScheduled(long id);

    @Info("Appraisal completed for {0}")
    void appraisalCompleted(long id);

    @Info("Loan declined based on appraisal results for {0}")
    void appraisalDeclined(long id);
}
