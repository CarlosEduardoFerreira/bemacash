package com.kaching123.tcr.commands.payment.blackstone.prepaid;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.SOAPWebCommand;
import com.kaching123.tcr.model.payment.blackstone.prepaid.PaymentRequest;
import com.telly.groundy.TaskResult;

/**
 * Created by pkabakov on 16.04.2014.
 */
public abstract class BasePrepaidPaymentCommand<TypeRequest extends PaymentRequest> extends SOAPWebCommand<TypeRequest> {

    protected final static String ARG_REQUEST = "ARG_REQUEST";

    @Override
    protected TypeRequest getRequest() {
        return (TypeRequest) getArgs().getSerializable(ARG_REQUEST);
    }

    @Override
    protected boolean allowRetries() {
        return false;
    }

    @Override
    protected TaskResult doInBackground() {
        TaskResult result = failed();
        try {
            result = super.doInBackground();
        } catch (Exception e) {
            Logger.e(this.getClass().getSimpleName() + ": doInBackground(): failed", e);
        }

        if (!isFailed(result)) {
            return result;
        }

        try {
            new UpdateBillPaymentFailedStatusCommand().sync(getContext(), getRequest().getOrderId(), getAppCommandContext());
        } catch (Exception e) {
            Logger.e(this.getClass().getSimpleName() + ": update prepaid failed status: failed", e);
        }

        return result;
    }

}
