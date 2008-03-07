package com.example.pricing;

import org.osoa.sca.annotations.Remotable;

@Remotable
public interface PricingService {
    public float getPrice(String productName);
}
