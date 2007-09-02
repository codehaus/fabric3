package tests.groovy;

/**
 * @version $Rev$ $Date$
 */
public class EchoJava implements EchoService {
    public String hello(String name) {
        return name;
    }
}
