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
package org.fabric3.jpa.service;

import java.util.List;

import org.fabric3.jpa.model.Employee;
import org.fabric3.jpa.model.ExEmployee;

/**
 * @version $Revision$ $Date$
 */
public interface EmployeeService {

    Employee createEmployee(Long id, String name);

    Employee findEmployee(Long id);

    void removeEmployee(Long id);

    List<Employee> searchWithCriteria(String name);

    void fire(Long id);

    ExEmployee findExEmployee(Long id);

    void removeExEmployee(Long id);

}
