package loanapp.appraisal;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @version $Revision$ $Date$
 */
@XmlRootElement
public class AppraisalResult {
    public static int APPROVED = 1;
    public static int DECLINED = -1;
    private long id;
    private int result;
    private String[] comments;

    public AppraisalResult() {
    }

    public AppraisalResult(long id, int result, String[] comments) {
        this.id = id;
        this.result = result;
        this.comments = comments;
    }

    public long getId() {
        return id;
    }
    public int getResult() {
        return result;
    }

    public String[] getComments() {
        return comments;
    }

}
