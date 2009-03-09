/*
 * Fabric3
 * Copyright © 2008 Metaform Systems Limited
 *
 * This proprietary software may be used only connection with the Fabric3 license
 * (the “License”), a copy of which is included in the software or may be
 * obtained at: http://www.metaformsystems.com/licenses/license.html.

 * Software distributed under the License is distributed on an “as is” basis,
 * without warranties or conditions of any kind.  See the License for the
 * specific language governing permissions and limitations of use of the software.
 * This software is distributed in conjunction with other software licensed under
 * different terms.  See the separate licenses for those programs included in the
 * distribution for the permitted and restricted uses of such software.
 *
 */
package org.fabric3.jpa.service;

import java.util.List;

import org.oasisopen.sca.annotation.Reference;

import org.fabric3.jpa.ConversationalDao;
import org.fabric3.jpa.model.Employee;

/**
 *
 * @version $Revision$ $Date$
 */
public class EmployeeServiceImpl implements EmployeeService {
    
    @Reference
    protected ConversationalDao<Employee, Long> employeeDao;

    public void createEmployees(List<Employee> employees) {
        
        for (Employee employee : employees) {
            employeeDao.persist(employee);
        }
        employeeDao.close();

    }

    public void remove(Employee employee) {
        employee = employeeDao.merge(employee);
        employeeDao.remove(employee);
    }

    public List<Employee> findAll() {
        return employeeDao.findByNamedQuery("findAll", Employee.class);
    }

}
