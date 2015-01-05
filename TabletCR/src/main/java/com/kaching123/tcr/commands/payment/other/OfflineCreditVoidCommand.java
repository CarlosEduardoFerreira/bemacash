package com.kaching123.tcr.commands.payment.other;

import android.content.Context;

import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.model.payment.other.OfflineCreditTransactionFactory;
import com.telly.groundy.TaskHandler;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 23.04.2014.
 */
public class OfflineCreditVoidCommand extends OtherVoidCommand {
    @Override
    protected Transaction getChildTransaction(BigDecimal amount, String orderGuid, String parentGuid, String cardname) {
        return OfflineCreditTransactionFactory.createChild(getAppCommandContext().getEmployeeGuid(),
                amount,
                orderGuid,
                parentGuid,
                cardname);
    }

    public static TaskHandler start(Context context,
                                    Object callback,
                                    PaymentTransactionModel data,
                                    SaleOrderModel childOrderModel,
                                    boolean needToCancel) {
        return create(OfflineCreditVoidCommand.class)
                .arg(AddReturnOrderCommand.ARG_ORDER_MODEL_CHILD, childOrderModel)
                .arg(ARG_DATA, data)
                .arg(ARG_NEED_TO_CANCEL, needToCancel)
                .callback(callback)
                .queueUsing(context);
    }
}
