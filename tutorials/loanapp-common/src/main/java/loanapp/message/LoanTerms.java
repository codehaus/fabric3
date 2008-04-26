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
public class LoanTerms implements Serializable {
    private static final long serialVersionUID = 8045590944866727036L;
    private List<LoanOption> options = new ArrayList<LoanOption>();
    private String typeSelected;

    public List<LoanOption> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public void setOptions(List<LoanOption> options) {
        this.options = options;
    }

    public void addOption(LoanOption option) {
        options.add(option);
    }

    public void setSelected(String type) {
        typeSelected = type;
    }

    public LoanOption getSelectedOption() {
        if (typeSelected == null) {
            return null;
        }
        for (LoanOption option : options) {
            if (option.getType().equals(typeSelected)) {
                return option;
            }
        }
        return null;
    }

}
