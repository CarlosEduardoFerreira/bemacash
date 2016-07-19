package com.kaching123.tcr.processor;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Vladimir Kompaniets on 13.07.2016.
 */
public class LoyaltyIncentiveCache {

    // [saleOrderId -> [incentiveId]]
    private HashMap<String, HashSet<String>> orderIncentives = new HashMap<>();

    public void put(String orderId, String incentiveId){

        if (orderIncentives.containsKey(orderId)){
            orderIncentives.get(orderId).add(incentiveId);
        }else{
            HashSet<String> incentives = new HashSet<>();
            incentives.add(incentiveId);
            orderIncentives.put(orderId, incentives);
        }
    }

    public boolean isIncentiveBannedForOrder(String orderId, String incentiveId){
        if (orderIncentives.containsKey(orderId)){
            return orderIncentives.get(orderId).contains(incentiveId);
        }else{
            return false;
        }
    }

    public static LoyaltyIncentiveCache get(){
        return Holder.incance;
    }

    private LoyaltyIncentiveCache() {}

    private static final class Holder {
        private static final LoyaltyIncentiveCache incance = new LoyaltyIncentiveCache();
    }
}
