package org.fabric3.jpa.model;

import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * @version $Revision$ $Date$
 */
@Entity
public class ExEmployee {

    @Id
    private Long id;

    private String name;

    public ExEmployee() {
    }

    public ExEmployee(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}