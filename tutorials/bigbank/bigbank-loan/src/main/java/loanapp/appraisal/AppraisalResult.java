package loanapp.appraisal;

/**
 * @version $Revision$ $Date$
 */
public class AppraisalResult {
    public static int APPROVED = 1;
    public static int DECLINED = -1;

    private int result;
    private String[] comments;

    public AppraisalResult(int result, String[] comments) {
        this.result = result;
        this.comments = comments;
    }

    public int getResult() {
        return result;
    }

    public String[] getComments() {
        return comments;
    }
}
