package loanapp.appraisal;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Date;

/**
 * @version $Revision$ $Date$
 */
@XmlRootElement
public class AppraisalSchedule {
    private long id;
    private Date date;

    public AppraisalSchedule() {
    }

    public AppraisalSchedule(long id, Date date) {
        this.id = id;
        this.date = date;
    }

    public long getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }
}
