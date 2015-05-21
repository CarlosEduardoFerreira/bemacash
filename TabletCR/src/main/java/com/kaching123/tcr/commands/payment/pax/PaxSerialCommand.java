package com.kaching123.tcr.commands.payment.pax;

import android.content.Context;
import android.os.Bundle;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.SerialRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.SerialResponse;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import retrofit.RetrofitError;

public class PaxSerialCommand extends PaxBaseCommand {

    public static final String RESULT_DETAILS = "RESULT_DETAILS";
    public static final String RESULT_CODE = "RESULT_CODE";

    public static void start(Context context, PaxModel paxTerminal, PaxSerialCommandBaseCallback callback) {
        create(PaxSerialCommand.class).arg(ARG_DATA_PAX, paxTerminal).allowNonUiCallbacks().callback(callback).queueUsing(context);
    }

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }

    public TaskResult sync(Context context, PaxModel model) {
        Bundle b = new Bundle();
        b.putParcelable(ARG_DATA_PAX, model);
        //no need in commands cache (creds on start)
        return super.sync(context, b, null);
    }

    @Override
    protected TaskResult doCommand(PaxWebApi api) {
        String errorMsg = null;
        int errorCode = 0;
        try {
            SerialResponse response = api.serial(new SerialRequest());
            errorCode = response.getResponse();
            if (errorCode == 200) {
                getApp().getShopPref().paxSerial().put(response.getDetails());
                Logger.d("response was ok");
                return succeeded().add(RESULT_DETAILS, response.getDetails()).add(RESULT_CODE, errorCode);
            }
            errorMsg = response.getDetails();
        } catch (Pax404Exception e) {
            Logger.e("Pax 404", e);
            errorMsg = e.getMessage();
        } catch (RetrofitError e) {
            Logger.e("PaxError", e);
            errorMsg = e.getMessage();
        } catch (Exception e) {
            Logger.e("Pax timeout Error", e);
            errorMsg = e.getMessage();
        }
        return failed().add(RESULT_DETAILS, errorMsg).add(RESULT_CODE, errorCode);
    }

    public static abstract class PaxSerialCommandBaseCallback {

        @OnSuccess(PaxSerialCommand.class)
        public final void onSuccess(@Param(RESULT_DETAILS) String details) {
            handleSuccess(details);
        }

        protected abstract void handleSuccess(String details);

        @OnFailure(PaxSerialCommand.class)
        public final void onFailure(@Param(RESULT_DETAILS) String error) {
            handleError(error);
        }

        protected abstract void handleError(String error);
    }
}
