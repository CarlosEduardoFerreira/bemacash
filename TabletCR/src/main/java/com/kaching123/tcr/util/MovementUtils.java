package com.kaching123.tcr.util;

import android.content.Context;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.store.saleorder.OrderTreeManagementCommand;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.model.ItemMovementModelFactory;
import com.telly.groundy.PublicGroundyTask;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Created by Hans on 5/28/2015.
 */
public class MovementUtils {

    public static void processAllRefund(Context context,
                                  PublicGroundyTask.IAppCommandContext appcontext,
                                  String orderGuid,
                                  List<ItemMovementModel> itemMovements,
                                  String movementJustification) {
        processAll(context, appcontext, orderGuid, itemMovements, movementJustification);
        for (ItemMovementModel item : itemMovements) {
            item.qty = restrictABS(CalculationUtil.negativeQty(item.qty), false);
        }
    }

    public static void processAll(Context context,
                                   PublicGroundyTask.IAppCommandContext appcontext,
                                   String orderGuid,
                                   List<ItemMovementModel> itemMovements,
                                   String movementJustification) {

        List<OrderTreeManagementCommand.MovementMetadata> result = new OrderTreeManagementCommand().sync(context, orderGuid, appcontext);

        if (result == null || result.isEmpty()) {
        } else {
            for (OrderTreeManagementCommand.MovementMetadata item : result) {
                itemMovements.add(ItemMovementModelFactory.getNewModel(
                                item.getGuid(),
                                item.getFlag(),
                                movementJustification,
                                restrictABS(item.getMovement(), true),
                                false,
                                new Date())
                                );
            }
        }
    }
    // sales -2 -> -2
    // sales 2 -> 0
    private static BigDecimal restrictABS(BigDecimal value, boolean sales) {
        return sales ? value.min(BigDecimal.ZERO) : value.max(BigDecimal.ZERO);
    }

    public static String getJustification(ItemMovementModel.JustificationType type) {
        int resId = ItemMovementModel.JustificationType.toValue(type);
        return TcrApplication.get().getApplicationContext().getString(resId);
    }
}
