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
public class LoanApplication implements Serializable {
    private static final long serialVersionUID = 8045590944866727036L;
    private int status;

    private LoanOption[] options;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public LoanOption[] getOptions() {
        return options;
    }

    public void setOptions(LoanOption[] options) {
        this.options = options;
    }

}
