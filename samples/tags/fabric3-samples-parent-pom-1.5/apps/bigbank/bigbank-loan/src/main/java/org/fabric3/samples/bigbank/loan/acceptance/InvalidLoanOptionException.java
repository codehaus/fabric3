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
package  org.fabric3.samples.bigbank.loan.acceptance;

import org.fabric3.samples.bigbank.api.loan.LoanException;

/**
 * @version $Revision: 8763 $ $Date: 2010-03-29 11:52:36 +0200 (Mon, 29 Mar 2010) $
 */
public class InvalidLoanOptionException extends LoanException {
    private static final long serialVersionUID = 3716049418234821586L;

    public InvalidLoanOptionException(String message) {
        super(message);
    }
}
