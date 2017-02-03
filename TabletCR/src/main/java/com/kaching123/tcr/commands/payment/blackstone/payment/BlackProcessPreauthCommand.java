package com.kaching123.tcr.commands.payment.blackstone.payment;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.RESTWebCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.request.ProcessPreauthRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.response.PreauthResponse;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.PreauthResult;
import com.kaching123.tcr.websvc.service.BlackStoneWebService;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by pkabakov on 17.05.2014.
 */
public class BlackProcessPreauthCommand extends RESTWebCommand<PreauthResponse, PreauthResult> {

    private static final Uri URI_PAYMENT = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);

    public static final String ARG_TRANSACTION = "ARG_TRANSACTION";

    private Transaction transaction;
    private PaymentTransactionModel transactionModel;

    @Override
    protected PreauthResult getEmptyResult() {
        return new PreauthResult();
    }

    @Override
    protected final TaskResult doInBackground() {
        return super.doInBackground().add(ARG_TRANSACTION, transaction);
    }

    @Override
    protected boolean doCommand(PreauthResult result) throws IOException {
        ProcessPreauthRequest data = getArgs().getParcelable(ARG_DATA);
        BlackStoneWebService.processPreauth(data, result);

        //mockSuccessResponse(result);

        boolean success = isResultSuccessful();
        if (!success) {
            Logger.e("BlackProcessPreauthCommand.doCommand(): error result: " + (result.getData() == null ? null : result.getData().toDebugString()));
        }

        transaction = data.getTransaction().updateFromProcessPreauth(result.getData());

        transactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), transaction);
        Logger.d("BlackProcessPreauthCommand.doCommand 66" + transactionModel.toDebugString());

        return result.isValid();
    }

    private void mockSuccessResponse(PreauthResult result) {
        result.setResultCode(200);
        PreauthResponse response = new PreauthResponse();
        response.setResponseCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);
        response.setServiceReferenceNumber("TEST");
        response.setAuthorizationNumber("TEST");
        response.setCardType("TEST");
        result.setData(response);
    }

    @Override
	protected boolean validateAppCommandContext() {
        return true;
    }

    @Override    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newInsert(URI_PAYMENT)
                .withValues(transactionModel.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(transactionModel).insertSQL(transactionModel, getAppCommandContext());
    }

    /*package*/  final static TaskHandler start(Context context, BaseProcessPreauthCallback callback, ProcessPreauthRequest data) {
        return create(BlackProcessPreauthCommand.class).arg(ARG_DATA, data).callback(callback).queueUsing(context);
    }

    public static abstract class BaseProcessPreauthCallback {

        @OnSuccess(BlackProcessPreauthCommand.class)
        public void onSuccess(@Param(RESTWebCommand.RESULT_DATA) PreauthResponse response,
                              @Param(BlackProcessPreauthCommand.ARG_TRANSACTION) Transaction transaction) {
            handleSuccess(response, transaction);
        }

        @OnFailure(BlackProcessPreauthCommand.class)
        public void onFailure(@Param(RESTWebCommand.RESULT_DATA) PreauthResponse response,
                              @Param(RESTWebCommand.RESULT_REASON) ErrorReason errorReason,
                              @Param(BlackProcessPreauthCommand.ARG_TRANSACTION) Transaction transaction) {
            handleFailure(response, errorReason, transaction);
        }

        protected abstract void handleSuccess(PreauthResponse response, Transaction transaction);

        protected abstract void handleFailure(PreauthResponse response, ErrorReason errorReason, Transaction transaction);

    }
}
