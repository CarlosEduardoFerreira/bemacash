package com.kaching123.tcr.commands.payment.other;

import android.content.Context;

import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.model.payment.other.CheckTransactionFactory;
import com.telly.groundy.TaskHandler;

import java.math.BigDecimal;

/**
 * Created by pkabakov on 23.04.2014.
 */
public class CheckRefundCommand extends OtherRefundCommand {

    @Override
    protected Transaction getChildTransaction(BigDecimal amount, String orderGuid, String parentGuid, String cardname) {
        return CheckTransactionFactory.createChild(getAppCommandContext().getEmployeeGuid(),
                amount,
                orderGuid,
                parentGuid,
                cardname);
    }

    public final static TaskHandler start(Context context,
                                          Object callback,
                                          PaymentTransactionModel data,
                                          BigDecimal refundAmount,
                                          SaleOrderModel childOrderModel,
                                          boolean isManualReturn) {
        return create(CheckRefundCommand.class)
                .arg(AddReturnOrderCommand.ARG_ORDER_MODEL_CHILD, childOrderModel)
                .arg(ARG_DATA, data)
                .arg(ARG_AMOUNT, refundAmount)
                .arg(ARG_IS_MANUAL_RETURN, isManualReturn)
                .callback(callback)
                .queueUsing(context);
    }

}
