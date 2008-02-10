package org.fabric3.scdl;

import java.lang.annotation.ElementType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

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
    
    // Type if the member is a field
    private String type;

    public MemberSite() {
    }

    public MemberSite(Field field) {
        name = field.getName();
        elementType = ElementType.FIELD;
        type = field.getType().getName();
    }

    public MemberSite(Method method) {
        name = method.getName();
        elementType = ElementType.METHOD;
        signature = new Signature(method);
    }

    public MemberSite(Constructor<?> constructor) {
        name = constructor.getName();
        elementType = ElementType.CONSTRUCTOR;
        signature = new Signature(constructor);
    }

    public MemberSite(Member member) {
        name = member.getName();
        if (member instanceof Method) {
            elementType = ElementType.METHOD;
            signature = new Signature((Method) member);
        } else if (member instanceof Field) {
            elementType = ElementType.FIELD;
            type = ((Field) member).getType().getName();
        } else if (member instanceof Constructor) {
            elementType = ElementType.CONSTRUCTOR;
            signature = new Signature((Constructor<?>) member);
        }
    }

    public MemberSite(ElementType elementType, String name) {
        this.elementType = elementType;
        this.name = name;
    }
    
    /**
     * Returns the type of the field.
     * 
     * @return Type of the field.
     */
    public String getType() {
        return type;
    }

    /**
     * Gets the name of the site.
     *
     * @return Site name.
     */
    public String getName() {
        return name;
    }

    public Signature getSignature() {
        return signature;
    }

    /**
     * Gets the element type.
     *
     * @return Element type.
     */
    public ElementType getElementType() {
        return elementType;
    }

}
