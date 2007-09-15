package loanapp.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.Serializable;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @version $Rev$ $Date$
 */
@XmlRootElement
public class LoanResult implements Serializable {
    private static final long serialVersionUID = 8045590944866727036L;
    public static final int Approved = 1;
    public static final int DECLINED = -1;
    private int result;
    private List<LoanOption> options = new ArrayList<LoanOption>();

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    List<LoanOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public void addOption(LoanOption option) {
        options.add(option);
    }
}
