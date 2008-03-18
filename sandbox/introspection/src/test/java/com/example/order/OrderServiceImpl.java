/*
 * Copyright (c) 2008 Jeremy Boynes, all rights reserved.
 *
 */
package com.example.order;

import com.example.inventory.InventoryDAO;
import com.example.pricing.PricingService;
import org.osoa.sca.annotations.Reference;

/**
 * @version $Rev$ $Date$
 */
public class OrderServiceImpl implements OrderService {
    private InventoryDAO dao;
    private PricingService pricing;

    public int order(String productName) {
        return 0;
    }

    public InventoryDAO getDao() {
        return dao;
    }

    @Reference
    public void setDao(InventoryDAO dao) {
        this.dao = dao;
    }

    public PricingService getPricing() {
        return pricing;
    }

    @Reference
    public void setPricing(PricingService pricing) {
        this.pricing = pricing;
    }
}
