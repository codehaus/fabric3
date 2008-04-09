/*
 * Copyright (c) 2008 Jeremy Boynes, all rights reserved.
 *
 */
package com.example.autowire;

import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
public class SourceImpl {

    @Reference
    public Target target;

    public void foo() {
    }
}
