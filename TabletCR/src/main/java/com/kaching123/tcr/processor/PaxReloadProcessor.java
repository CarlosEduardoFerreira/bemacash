package com.kaching123.tcr.processor;

import android.content.Context;

import com.kaching123.tcr.R;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.payment.WebCommand.ErrorReason;
import com.kaching123.tcr.commands.payment.pax.PaxGateway;
import com.kaching123.tcr.commands.payment.pax.blackstone.PaxBlackstoneLastTransactionCommand;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;

import java.math.BigDecimal;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to
 */
public class PaxReloadProcessor {

    private PaxReloadProcessor() {

    }
    //TODO: RELOAD IN PAX PROCESSOR
    public static PaxReloadProcessor get() {
        return new PaxReloadProcessor();
    }

    public void reload(final Context context, final Transaction transaction, final BigDecimal amount, final IPAXReloadCallback callback) {
        assert callback != null;
        callback.onStart();
        if (TcrApplication.get().isBlackstonePax()) {

        }
        else{
            PaxBlackstoneLastTransactionCommand.startSale(context, PaxModel.get(), transaction, amount, new PaxBlackstoneLastTransactionCommand.PaxLastTransactionCommandBaseCallback() {

                @Override
                protected void handleSuccess(SaleActionResponse response) {
                    assert callback != null;
                    callback.onComplete(response);
                }

                @Override
                protected void handleError(PaxGateway.Error error) {
                    assert callback != null;
                    boolean allowFurther = PaxGateway.Error.SERVICE != error;
                    callback.onError(getErrorMessage(context, error), allowFurther);
                }
            });
        }

    }

    private String getErrorMessage(Context context, PaxGateway.Error error) {
        final String message;
        final int messageClarification;

        if (error != null) {
            switch (error) {
                case CONNECTIVITY:
                    message = context.getString(R.string.pax_timeout);
                    break;
                case PAX:
                case PAX404:
                    message = context.getString(R.string.blackstone_pax_failure_reason_pax);
                    break;
                case SERVICE:
                    message = context.getString(R.string.blackstone_pax_reload_failure_reason_service);
                    break;
                default:
                    message = ErrorReason.UNKNOWN.getDescription();
                    break;
            }
            messageClarification = R.string.blackstone_pay_failure_body_3nd;
        } else {
            message = ErrorReason.UNKNOWN.getDescription();
            messageClarification = R.string.blackstone_pay_failure_body_3nd;
        }
        String mainString = context.getResources().getString(R.string.blackstone_pay_failure_body_1st);
        return context.getString(R.string.blackstone_pay_failure_body_constructor, mainString, context.getString(messageClarification), message);
    }

    public interface IPAXReloadCallback {
        void onStart();
        void onComplete(SaleActionResponse reloadResponse);
        void onError(String errorMessage, boolean allowFurtherReload);
    }
}
