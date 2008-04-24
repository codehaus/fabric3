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
package loanapp.appraisal;

/**
 * Interface for receiving appraisal callbacks.
 *
 * @version $Revision$ $Date$
 */
public interface AppraisalCallback {

    /**
     * Notification when an appraisal has been scheduled
     *
     * @param time the scheduled time
     */
    void dateSchedule(long time);

    /**
     * Notification when an appraisal has been completed.
     *
     * @param result the appraisal result
     */
    void appraisalCompleted(AppraisalResult result);

}
