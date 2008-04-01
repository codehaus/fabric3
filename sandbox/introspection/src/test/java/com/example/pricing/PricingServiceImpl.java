package com.example.pricing;

import org.osoa.sca.annotations.Property;


public class PricingServiceImpl implements PricingService {

    @Property
    public void setValue(int value) {
    }

    @Property
    public void setName(String name) {
    }

    public float getPrice(String productName) {
        return 0;
    }

}
