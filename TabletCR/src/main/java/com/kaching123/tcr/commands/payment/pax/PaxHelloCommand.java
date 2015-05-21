package com.kaching123.tcr.commands.payment.pax;

import android.content.Context;
import android.os.Bundle;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.model.PaxModel;
import com.kaching123.tcr.processor.PaxPokeProcessor;
import com.kaching123.tcr.websvc.api.pax.model.payment.request.HelloRequest;
import com.kaching123.tcr.websvc.api.pax.model.payment.result.response.HelloResponse;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import retrofit.RetrofitError;

public class PaxHelloCommand extends PaxBaseCommand {

    public static final String RESULT_DETAILS = "RESULT_DETAILS";
    public static final String RESULT_CODE = "RESULT_CODE";

    public static void start(Context context, PaxModel paxTerminal, PaxHelloCommandBaseCallback callback) {
        create(PaxHelloCommand.class).arg(ARG_DATA_PAX, paxTerminal).allowNonUiCallbacks().callback(callback).queueUsing(context);
    }

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }

    public TaskResult sync(Context context, PaxModel model) {
        Bundle b = new Bundle();
        b.putParcelable(ARG_DATA_PAX, model);
        b.putBoolean(ARG_INIT_CONNECT, true);
        //no need in commands cache (creds on start)
        return super.sync(context, b, null);
    }

    @Override
    protected TaskResult doCommand(PaxWebApi api) {
        String errorMsg = null;
        int errorCode = 0;
        try {
            String top = getApp().getShopInfo().displayWelcomeMsg;
            String bottom = getApp().getShopInfo().displayWelcomeMsgBottom;
            HelloResponse response = api.hello(new HelloRequest().setDisplay(String.format("%s^%s", top != null ? top : "", bottom != null ? bottom : "")));
            errorCode = response.getResponse();
            if (errorCode == 200) {
                new PaxMIDownloadCommand().sync(getContext(), paxTerminal);
                PaxPokeProcessor.get().start(getContext());
                getApp().getShopPref().paxUrl().put(paxTerminal.ip);
                getApp().getShopPref().paxPort().put(paxTerminal.port);
                Logger.d("response was ok");

                new PaxSerialCommand().sync(getContext(), paxTerminal);
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

    public static abstract class PaxHelloCommandBaseCallback {

        @OnSuccess(PaxHelloCommand.class)
        public final void onSuccess(@Param(RESULT_DETAILS) String details) {
            handleSuccess(details);
        }

        protected abstract void handleSuccess(String details);

        @OnFailure(PaxHelloCommand.class)
        public final void onFailure(@Param(RESULT_DETAILS) String error) {
            handleError(error);
        }

        protected abstract void handleError(String error);
    }
}
