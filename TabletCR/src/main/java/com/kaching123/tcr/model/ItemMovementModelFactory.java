package com.kaching123.tcr.model;

import com.kaching123.tcr.TcrApplication;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

/**
 * Created by azablotskiy on 03-Jun-15.
 */
public class ItemMovementModelFactory {

    public static final ItemMovementModel getNewModel(String guid,
                                                      String itemGuid,
                                                      String orderGuid,
                                                      String itemUpdateFlag,
                                                      BigDecimal qty,
                                                      boolean manual,
                                                      String operatorGuid,
                                                      Date createTime) {

        return new ItemMovementModel(
                guid,
                itemGuid,
                orderGuid,
                itemUpdateFlag,
                qty,
                manual,
                operatorGuid,
                createTime,
                null);
    }

    public static final ItemMovementModel getNewModel(String guid,
                                                      String itemGuid,
                                                      String orderGuid,
                                                      String itemUpdateFlag,
                                                      BigDecimal qty,
                                                      boolean manual,
                                                      Date createTime) {

        return new ItemMovementModel(
                guid,
                itemGuid,
                orderGuid,
                itemUpdateFlag,
                qty,
                manual,
                TcrApplication.get().getOperatorGuid(),
                createTime,
                null);

    }

    public static final ItemMovementModel getNewModel(String itemGuid,
                                                      String itemUpdateFlag,
                                                      BigDecimal qty,
                                                      boolean manual,
                                                      Date createTime) {

        return new ItemMovementModel(
                UUID.randomUUID().toString(),
                itemGuid,
                null,
                itemUpdateFlag,
                qty,
                manual,
                TcrApplication.get().getOperatorGuid(),
                createTime,
                null);
    }

    public static final ItemMovementModel getNewModel(String itemGuid,
                                                      String orderGuid,
                                                      String itemUpdateFlag,
                                                      BigDecimal qty,
                                                      boolean manual,
                                                      Date createTime) {

        return new ItemMovementModel(
                UUID.randomUUID().toString(),
                itemGuid,
                orderGuid,
                itemUpdateFlag,
                qty,
                manual,
                TcrApplication.get().getOperatorGuid(),
                createTime,
                null);
    }

}
