package com.kaching123.tcr.commands.payment.blackstone.payment;

import android.content.Context;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.RESTWebCommand;
import com.kaching123.tcr.model.payment.blackstone.payment.request.AutomaticBatchCloseRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.response.AutomaticBatchCloseResponse;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.AutomaticBatchCloseResult;
import com.kaching123.tcr.websvc.service.BlackStoneWebService;
import com.telly.groundy.TaskResult;

import java.io.IOException;

/**
 * Created by pkabakov on 11.06.2014.
 */
public class BlackSetAutomaticBatchCloseCommand extends RESTWebCommand<AutomaticBatchCloseResponse, AutomaticBatchCloseResult> {

    private AutomaticBatchCloseRequest request;

    @Override
    protected AutomaticBatchCloseResult getEmptyResult() {
        return new AutomaticBatchCloseResult();
    }

    @Override
    protected boolean doCommand(AutomaticBatchCloseResult result) throws IOException {

        BlackStoneWebService.setAutomaticBatchClose(request, result);

        boolean success = isResultSuccessful();
        if (!success) {
            Logger.e("BlackSetAutomaticBatchCloseCommand.doCommand(): error result: " + (result.getData() == null ? null : result.getData().toDebugString()));
        }

        return result.isValid();
    }

    @Override
    protected boolean validateAppCommandContext() {
        return false;
    }

    public boolean sync(Context context, AutomaticBatchCloseRequest request) {
        this.request = request;
        //no need in commands cache (creds on start)
        TaskResult result = syncStandalone(context, null, null); //TODO: check params
        return !isFailed(result);
    }

}
