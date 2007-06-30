package org.fabric3.pojo.instancefactory;

import java.lang.annotation.ElementType;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.lang.reflect.Constructor;

/**
 * Represents an injection site on a Java-based component implementation.
 *
 * @version $Revision$ $Date$
 */
public class MemberSite {

    // Element type
    private ElementType elementType;

    // Name of the site
    private String name;

    // Signature of the method or constructor
    private Signature signature;

    public MemberSite() {
    }

    public MemberSite(Member member) {
        setName(member.getName());
        if (member instanceof Method) {
            setElementType(ElementType.METHOD);
            setSignature(new Signature((Method) member));
        } else if (member instanceof Field) {
            setElementType(ElementType.FIELD);
        } else if (member instanceof Constructor) {
            setElementType(ElementType.CONSTRUCTOR);
            setSignature(new Signature((Constructor) member));
        }
    }

    public MemberSite(ElementType elementType, String name) {
        this.elementType = elementType;
        this.name = name;
    }

    /**
     * Gets the name of the site.
     *
     * @return Site name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the site.
     *
     * @param name Name of the site.
     */
    public void setName(String name) {
        this.name = name;
    }

    public Signature getSignature() {
        return signature;
    }

    public void setSignature(Signature signature) {
        this.signature = signature;
    }

    /**
     * Gets the element type.
     *
     * @return Element type.
     */
    public ElementType getElementType() {
        return elementType;
    }

    /**
     * Sets the element type.
     *
     * @param elementType Element type.
     */
    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

}
