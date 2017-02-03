package com.kaching123.tcr.commands.payment.blackstone.payment;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.payment.RESTWebCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.model.payment.blackstone.payment.request.SaleRequest;
import com.kaching123.tcr.model.payment.blackstone.payment.response.SaleResponse;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.kaching123.tcr.websvc.result.blackstone.pinserve.SaleResult;
import com.kaching123.tcr.websvc.service.BlackStoneWebService;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author Ivan v. Rikhmayer
 *         This class is intended to call blackstone sale
 *         Process a credit card sale transaction where sales do not include tips. Refer to Preauthorization Request when sales include tips.
 */
public class BlackSaleCommand extends RESTWebCommand<SaleResponse, SaleResult> {

    private static final Uri URI_PAYMENT = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);

    public static final String ARG_TRANSACTION = "ARG_TRANSACTION";

    private Transaction transaction;
    private PaymentTransactionModel transactionModel;

    @Override
    protected SaleResult getEmptyResult() {
        return new SaleResult();
    }

    @Override
    protected final TaskResult doInBackground() {
        return super.doInBackground().add(ARG_TRANSACTION, transaction);
    }

    @Override
    protected boolean validateAppCommandContext() {
        return true;
    }

    @Override
    protected boolean doCommand(SaleResult result) throws IOException {
        SaleRequest data = getArgs().getParcelable(ARG_DATA);
        BlackStoneWebService.sale(data, result);

        boolean success = isResultSuccessful();
        if (!success) {
            Logger.e("BlackSaleCommand.doCommand(): error result: " + (result.getData() == null ? null : result.getData().toDebugString()));
        }

        //mockSuccessResponse(result);

        transactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), transaction = data.getTransaction().updateFromSale(result.getData()));
        Logger.d("BlackSaleCommand.doCommand " + transactionModel.toDebugString());

        return result.isValid();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
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

    private void mockSuccessResponse(SaleResult result) {
        result.getData().setResponseCode(TransactionStatusCode.OPERATION_COMPLETED_SUCCESSFULLY);
        result.getData().setServiceReferenceNumber("TEST");
        result.getData().setAuthorizationNumber("TEST");
        result.getData().setCardType("TEST");
        result.getData().setLastFour("TEST");
    }

    /*package*/  final static TaskHandler start(Context context, Object callback, SaleRequest data) {
        return create(BlackSaleCommand.class).arg(ARG_DATA, data).callback(callback).queueUsing(context);
    }
}