package com.kaching123.tcr.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import com.kaching123.tcr.TcrApplication;

/**
 * Created by azablotskiy on 03-Jun-15.
 */
public class ItemMovementModelFactory {

    public static final ItemMovementModel getNewModel(String guid,
                                                      String itemGuid,
                                                      String itemUpdateFlag,
                                                      String justification,
                                                      BigDecimal qty,
                                                      boolean manual,
                                                      String operatorGuid,
                                                      Date createTime) {

        return new ItemMovementModel(
                guid,
                itemGuid,
                itemUpdateFlag,
                justification,
                qty,
                manual,
                operatorGuid,
                createTime);
    }

    public static final ItemMovementModel getNewModel(String guid,
                                                      String itemGuid,
                                                      String itemUpdateFlag,
                                                      String justification,
                                                      BigDecimal qty,
                                                      boolean manual,
                                                      Date createTime) {

        return new ItemMovementModel(
                guid,
                itemGuid,
                itemUpdateFlag,
                justification,
                qty,
                manual,
                TcrApplication.get().getOperatorGuid(),
                createTime);

    }

    public static final ItemMovementModel getNewModel(String itemGuid,
                                                      String itemUpdateFlag,
                                                      String justification,
                                                      BigDecimal qty,
                                                      boolean manual,
                                                      Date createTime) {

        return new ItemMovementModel(
                UUID.randomUUID().toString(),
                itemGuid,
                itemUpdateFlag,
                justification,
                qty,
                manual,
                TcrApplication.get().getOperatorGuid(),
                createTime);
    }

}
