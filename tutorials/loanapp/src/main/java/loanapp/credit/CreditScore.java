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
package loanapp.credit;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * @version $Revision$ $Date$
 */
@XmlRootElement
public class CreditScore implements Serializable {
    private static final long serialVersionUID = -452032042185332788L;
    private int score;
    private int[] reasons;

    public CreditScore(int score, int[] reasons) {
        this.score = score;
        this.reasons = reasons;
    }

    public int getScore() {
        return score;
    }

    public int[] getReasons() {
        return reasons;
    }

}
