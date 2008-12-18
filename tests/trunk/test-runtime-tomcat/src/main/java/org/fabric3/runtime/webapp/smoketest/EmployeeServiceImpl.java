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
package org.fabric3.runtime.webapp.smoketest;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import org.fabric3.runtime.webapp.smoketest.model.Employee;

/**
 * @version $Revision$ $Date$
 */
public class EmployeeServiceImpl implements EmployeeService {
    private EntityManager em;

    @PersistenceContext(name = "employeeEmf", unitName = "employee")
    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    public Employee createEmployee(Long id, String name) {
        Employee employee = new Employee(id, name);
        em.persist(employee);
        return employee;
    }

    public Employee findEmployee(Long id) {
        Employee e = em.find(Employee.class, id);
        return e;
    }

    public void removeEmployee(Long id) {
        Employee employee = em.find(Employee.class, id);
        em.remove(employee);
    }

    public List<Employee> searchWithCriteria(String name) {
        Session session = (Session) em.getDelegate();
        Criteria criteria = session.createCriteria(Employee.class);

        criteria.add(Restrictions.eq("name", name));

        return (List<Employee>) criteria.list();
    }

}