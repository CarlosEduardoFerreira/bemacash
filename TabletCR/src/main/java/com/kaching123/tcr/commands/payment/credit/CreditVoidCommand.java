package com.kaching123.tcr.commands.payment.credit;

import android.content.Context;

import com.kaching123.tcr.commands.payment.AddReturnOrderCommand;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

/**
 * Created by gdubina on 24/02/14.
 */
public class CreditVoidCommand extends CreditRefundCommand {

    public static TaskHandler start(Context context,
                                    CreditVoidCommandBaseCallback callback,
                                    PaymentTransactionModel data,
                                    SaleOrderModel childOrderModel,
                                    boolean needToCancel) {
        return create(CreditVoidCommand.class)
                .arg(AddReturnOrderCommand.ARG_ORDER_MODEL_CHILD, childOrderModel)
                .arg(ARG_DATA, data)
                .arg(ARG_NEED_TO_CANCEL, needToCancel)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class CreditVoidCommandBaseCallback {

        @OnSuccess(CreditVoidCommand.class)
        public final void onSuccess() {
            handleOnSuccess();
        }

        @OnFailure(CreditVoidCommand.class)
        public final void onFailure() {
            handleOnFailure();
        }

        protected abstract void handleOnSuccess();

        protected abstract void handleOnFailure();
    }
}
