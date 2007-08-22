package org.fabric3.test.wiring;

/**
 * @version $Rev$ $Date$
 */
public interface TestService {

    Target getTarget();

    Target getConstructorTarget();

    Target getPromotedReference();

    Target getNonConfiguredPromotedReference();

    Target getOptionalNonSetReference();

}
