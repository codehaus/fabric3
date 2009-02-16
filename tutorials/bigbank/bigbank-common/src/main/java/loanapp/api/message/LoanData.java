package loanapp.api.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @version $Rev: 6163 $ $Date: 2008-12-03 19:55:22 -0800 (Wed, 03 Dec 2008) $
 */
@XmlRootElement
public class LoanData implements Serializable {
    private static final long serialVersionUID = 8045590944866727036L;
    private List<LoanOption> options = new ArrayList<LoanOption>();

    public List<LoanOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public void setOptions(List<LoanOption> options) {
        this.options = options;
    }

    public void addOption(LoanOption option) {
        options.add(option);
    }
}
