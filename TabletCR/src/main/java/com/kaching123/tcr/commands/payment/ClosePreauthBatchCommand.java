package com.kaching123.tcr.commands.payment;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.R;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.user.AddTipsCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.PaymentTransactionJdbcConverter;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.TipsModel;
import com.kaching123.tcr.model.TipsModel.PaymentType;
import com.kaching123.tcr.model.payment.blackstone.payment.TransactionStatusCode;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionTable;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by pkabakov on 23.06.2014.
 */
public class ClosePreauthBatchCommand extends AsyncCommand {

    private static final Uri URI_PAYMENT = ShopProvider.getContentUri(PaymentTransactionTable.URI_CONTENT);

    private ArrayList<SyncResult> addTipsResult;
    private ArrayList<PaymentTransactionModel> list;

    @Override
    protected TaskResult doCommand() {

        list = (ArrayList<PaymentTransactionModel>) getArgs().getSerializable(ARG_PAYMENT_TRANSACTION);
        addTipsResult = new ArrayList<SyncResult>();
        BigDecimal tipsAmount = BigDecimal.ZERO;
        String tipsComments = getContext().getString(R.string.batch_close_tips_comments);
        String tippedEmployeeId = getStringArg(ARG_TIPPED_EMPLOYEE);


        addTips(tipsAmount, tipsComments, tippedEmployeeId, list);
        closePreauthTransaction(list);


        return succeeded();
    }

    private void closePreauthTransaction(ArrayList<PaymentTransactionModel> list) {
        for (PaymentTransactionModel transactionModel : list)
            transactionModel.status = PaymentStatus.SUCCESS;
    }

    private boolean addTips(BigDecimal tipsAmount, String tipsComments, String tippedEmployeeId, ArrayList<PaymentTransactionModel> list) {
        //TODO: add tips amount field to transaction?

        if (list != null) {
            for (PaymentTransactionModel transactionModel : list) {
                addTipsResult.add(new AddTipsCommand().sync(getContext(), new TipsModel(
                        UUID.randomUUID().toString(),
                        null,
                        tippedEmployeeId,
                        getAppCommandContext().getShiftGuid(),
                        transactionModel.orderGuid,
                        transactionModel.guid,
                        new Date(),
                        tipsAmount,
                        tipsComments,
                        PaymentType.CREDIT, null
                ), getAppCommandContext()));
            }
        }
        return addTipsResult.size() != 0;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        for (int i = 0; i < list.size(); i++) {
            operations.add(ContentProviderOperation.newUpdate(URI_PAYMENT)
                    .withValues(list.get(i).toUpdateValues())
                    .withSelection(PaymentTransactionTable.GUID + " = ?", new String[]{list.get(i).guid})
                    .build());

            if (addTipsResult != null && addTipsResult.size() != 0 && addTipsResult.get(i).getLocalDbOperations() != null)
                operations.addAll(addTipsResult.get(i).getLocalDbOperations());
        }

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand sqlCommand = batchUpdate(PaymentTransactionTable.TABLE_NAME);

        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                sqlCommand.add(JdbcFactory.getConverter(list.get(i)).updateSQL(list.get(i), getAppCommandContext()));
                if (addTipsResult != null && addTipsResult.size() != 0)
                    sqlCommand.add(addTipsResult.get(i).getSqlCmd());
            }
        }


        return sqlCommand;
    }

    protected static final String ARG_DATA_PAX = "ARG_DATA_PAX";

    protected static final String RESULT_DATA = "RESULT_DATA";
    protected static final String RESULT_ERROR = "RESULT_ERROR";
    protected static final String RESULT_ERROR_CODE = "RESULT_ERROR_CODE";
    public static final String ARG_PAYMENT_TRANSACTION = "ARG_PAYMENT_TRANSACTION";
    public static final String ARG_TIPPED_EMPLOYEE = "ARG_TIPPED_EMPLOYEE";

    public static final TaskHandler start(Context context,
                                          ArrayList<PaymentTransactionModel> list,
                                          String tippedEmployeeId,
                                          ClosePreauthCommandCallback callback) {
        return create(ClosePreauthBatchCommand.class)
                .arg(ARG_PAYMENT_TRANSACTION, list)
                .arg(ARG_TIPPED_EMPLOYEE, tippedEmployeeId)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class ClosePreauthCommandCallback {

        @OnSuccess(ClosePreauthBatchCommand.class)
        public void handleSuccess() {
            onCloseSuccess();
        }

        @OnFailure(ClosePreauthBatchCommand.class)
        public void handleFailure() {
            onCloseFailure();
        }

        public abstract void onCloseSuccess();

        public abstract void onCloseFailure();
    }

}
