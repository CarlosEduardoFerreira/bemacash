package com.kaching123.tcr.commands.payment.pax.blackstone;

import android.content.Context;
import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.R;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.SaleActionRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SaleActionResponse;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;

import retrofit.RetrofitError;

/**
 * Created by mayer
 */
public class PaxBlackstoneBalanceCommand extends PaxBlackstoneBaseCommand {

    public static final int TRANSACTION_ID_BALANCE = 9;

    private static final String RESULT_AMOUNT = "RES_TRANSACTION";
    private static final String RESULT_LAST4 = "RESULT_LAST4";
    private static final String RESULT_ERROR_REASON = "ERROR";
    private static final String ARG_PURPOSE = "ARG_AMOUNT_1";


    public static final TaskHandler start(Context context,
                                              PaxModel paxTerminal,
                                              PaxBalanceCommandBaseCallback callback) {
        return  create(PaxBlackstoneBalanceCommand.class)
                .arg(ARG_DATA_PAX, paxTerminal)
                .arg(ARG_PURPOSE, TRANSACTION_ID_BALANCE)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected TaskResult doCommand(PaxWebApi api) {

        int transactionId = getIntArg(ARG_PURPOSE);
        BigDecimal amount = null;
        String lastFour = "";
        String errorReason = "";
        try {
            SaleActionResponse response = api.sale(new SaleActionRequest(transactionId, "001", null));
            if (response != null && response.getDetails() != null && 200 == response.getResponse() && response.getDetails().getSale().getBalance() != null)  {
                amount = new BigDecimal(response.getDetails().getSale().getBalance());
                lastFour = response.getDetails().getDigits();
            } else {
                for (String msg : response.getDetails().getSale().getMessage()) {
                    if (errorReason.length() > 0) {
                        errorReason = errorReason.concat(". ");
                    }
                    errorReason = errorReason.concat(msg);
                }
                if (response.getDetails().getVerbiage() != null) {
                    if (!TextUtils.isEmpty(errorReason)) {
                        errorReason = errorReason.concat(": ");
                    }
                    errorReason = errorReason.concat(response.getDetails().getVerbiage());
                }
                if (TextUtils.isEmpty(errorReason)) {
                    errorReason = "The balance check request failed. No particular reason was provided, the reason could be related to software issues.";
                }
            }
        } catch (Pax404Exception e) {
            errorReason = "Request cancelled or connection problem.";
            Logger.e("Pax 404", e);
        } catch (RetrofitError e) {
            errorReason = getContext().getString(R.string.pax_timeout);
            Logger.e("PaxError", e);
        } catch (Exception e) {
            // Though it should not happen, as Gena confirms we only care about local DB and data will sync after,
            // I put this check due to possibilty on DB corruption, DB access failure and many other ugly rare stuff
            Logger.e("Rare SQL exception caught, data was not updated", e);
            errorReason = "Rare SQL exception caught, data was not updated";
        }
        return succeeded().add(RESULT_LAST4, lastFour).add(RESULT_AMOUNT, amount).add(RESULT_ERROR_REASON, errorReason);
    }

    public static abstract class PaxBalanceCommandBaseCallback {

        @OnSuccess(PaxBlackstoneBalanceCommand.class)
        public final void onSuccess(@Param(PaxBlackstoneBalanceCommand.RESULT_AMOUNT) BigDecimal result,
                                    @Param(PaxBlackstoneBalanceCommand.RESULT_LAST4) String last4,
                                    @Param(PaxBlackstoneBalanceCommand.RESULT_ERROR_REASON) String errorReason) {
            handleSuccess(result, last4, errorReason);
        }

        protected abstract void handleSuccess(BigDecimal result, String last4, String errorReason);

        @OnFailure(PaxBlackstoneBalanceCommand.class)
        public final void onFailure() {
            handleError();
        }

        protected abstract void handleError();
    }

}
