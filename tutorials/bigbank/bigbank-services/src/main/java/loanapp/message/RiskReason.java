package loanapp.message;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @version $Revision$ $Date$
 */
public class RiskReason implements Serializable {
    private static final long serialVersionUID = -1781028701570454727L;
    private String description;

    public RiskReason() {
    }

    public RiskReason(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
