/*
 * See the NOTICE file distributed with this work for information
 * regarding copyright ownership.  This file is licensed
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */package com.example.order;

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
