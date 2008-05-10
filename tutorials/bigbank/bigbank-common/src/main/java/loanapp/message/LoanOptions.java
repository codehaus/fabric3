package loanapp.message;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @version $Rev$ $Date$
 */
@XmlRootElement
public class LoanOptions implements Serializable {
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
