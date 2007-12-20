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
package org.fabric3.idea.run;

import com.intellij.execution.ui.ConsoleView;
import static com.intellij.execution.ui.ConsoleViewContentType.NORMAL_OUTPUT;
import org.apache.maven.surefire.report.AbstractReporter;

/**
 * Writes Surefire reports to a ConsoleView.
 *
 * @version $Rev$ $Date$
 */
public class ConsoleReporter extends AbstractReporter {
    ConsoleView view;

    protected ConsoleReporter(ConsoleView view, Boolean trimStackTrace) {
        super(trimStackTrace);
        this.view = view;
    }

    public void writeMessage(String s) {
        view.print(s, NORMAL_OUTPUT);
        view.print("\n", NORMAL_OUTPUT);
    }
}
