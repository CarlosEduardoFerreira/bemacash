package com.kaching123.tcr.commands.payment.credit;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CreditReceiptModel;
import com.kaching123.tcr.model.PaymentTransactionModel;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentStatus;
import com.kaching123.tcr.model.PaymentTransactionModel.PaymentType;
import com.kaching123.tcr.model.payment.credit.CreditReceiptData;
import com.kaching123.tcr.model.payment.general.transaction.Transaction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.CreditReceiptView2.CreditReceiptTable;
import com.kaching123.tcr.store.ShopSchema2.CreditReceiptView2.RegisterTable;
import com.kaching123.tcr.store.ShopSchema2.PaymentTransactionView2.PaymentTransactionTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CreditReceiptView;
import com.kaching123.tcr.store.ShopStore.PaymentTransactionView;
import com.telly.groundy.TaskHandler;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.util.DateUtils.cutTime;

/**
 * @author gdubina
 */
public class CreditSaleCommand extends AsyncCommand {

    private static final Uri URI_CREDIT_RECEIPT_VIEW = ShopProvider.getContentUri(CreditReceiptView.URI_CONTENT);
    private static final Uri URI_PAYMENT_VIEW = ShopProvider.getContentUri(PaymentTransactionView.URI_CONTENT);
    private static final Uri URI_PAYMENT = ShopProvider.getContentUri(ShopStore.PaymentTransactionTable.URI_CONTENT);
    private static final Uri URI_CREDIT_RECEIPT = ShopProvider.getContentUri(ShopStore.CreditReceiptTable.URI_CONTENT);

    private static final String ARG_DATA = "ARG_DATA";
    private static final String ARG_CREDIT_INFO = "ARG_CREDIT_INFO";
    public static final String EXTRA_CHANGE = "EXTRA_CHANGE";

    private PaymentTransactionModel transactionModel;
    private CreditReceiptModel changeCreditReceipt;

    @Override
    protected TaskResult doCommand() {
        Transaction data = getArgs().getParcelable(ARG_DATA);
        CreditReceiptData creditReceiptData = getArgs().getParcelable(ARG_CREDIT_INFO);
        assert creditReceiptData != null;

        transactionModel = new PaymentTransactionModel(getAppCommandContext().getShiftGuid(), data);
        CreditRegisterInfo info = getCreditReceiptAmount(creditReceiptData);
        if (info == null)
            return failed();
        if (info.availableAmount.compareTo(transactionModel.amount) == -1) {
            return failed();
        }
        transactionModel.paymentId = info.guid;
        Logger.d(transactionModel.toDebugString());
        BigDecimal change = info.availableAmount.subtract(transactionModel.amount);

        transactionModel.changeAmount = change;

        if (change.compareTo(BigDecimal.ZERO) == 1) {
            changeCreditReceipt = CreateCreditReceiptCommand.createCreditReceiptModel(getContext(), change, getNewExpireTime(info.createDate, info.expireTime),
                        getAppCommandContext().getEmployeeGuid(),
                        getAppCommandContext().getRegisterId(),
                        getAppCommandContext().getShiftGuid());
        }

        return succeeded().add(EXTRA_CHANGE, changeCreditReceipt);
    }

    private int getNewExpireTime(Date createDate, int expireTime){
        Calendar c = Calendar.getInstance();
        cutTime(c);
        long now = c.getTimeInMillis();

        c.setTime(createDate);
        cutTime(c);
        c.add(Calendar.DATE, expireTime);
        long expireDate = c.getTimeInMillis();
        long diff = expireDate - now;

        int days = (int)TimeUnit.MILLISECONDS.toDays(diff);
        return days + 1;
    }

    private CreditRegisterInfo getCreditReceiptAmount(CreditReceiptData data) {
        Cursor c = ProviderAction
                .query(URI_CREDIT_RECEIPT_VIEW)
                .projection(CreditReceiptTable.GUID,
                        CreditReceiptTable.AMOUNT,
                        CreditReceiptTable.CREATE_TIME,
                        CreditReceiptTable.EXPIRE_TIME)
                .where(RegisterTable.TITLE + " = ?", data.register)
                .where(CreditReceiptTable.PRINT_NUMBER + " = ?", data.receiptNum)
                .perform(getContext());

        boolean exists = c.getCount() != 0;
        BigDecimal availableAmount = BigDecimal.ZERO;
        Date createTime = null;
        int expireTime = 0;
        String guid = null;
        if (c.moveToFirst()) {
            guid = c.getString(0);
            availableAmount = _decimal(c, 1, BigDecimal.ZERO);
            createTime = new Date(c.getLong(2));
            expireTime = c.getInt(3);
        }
        c.close();

        if (!exists) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();
        cutTime(calendar);
        long now = calendar.getTimeInMillis();

        calendar.setTime(createTime);
        cutTime(calendar);

        calendar.add(Calendar.DATE, expireTime);
        long expiredDate = calendar.getTimeInMillis();

        if(now > expiredDate){
            return null;
        }

        c = ProviderAction
                .query(URI_PAYMENT_VIEW)
                .projection(PaymentTransactionTable.AMOUNT)
                .where(PaymentTransactionTable.GATEWAY_PAYMENT_ID + " = ?", guid)
                .where(PaymentTransactionTable.STATUS + " = ?", PaymentStatus.SUCCESS.ordinal())
                .where(PaymentTransactionTable.TYPE + " = ?", PaymentType.SALE.ordinal())
                .perform(getContext());

        boolean isUsed = c.getCount() != 0;

        c.close();

        return new CreditRegisterInfo(guid, isUsed ? BigDecimal.ZERO : availableAmount, createTime, expireTime);
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newInsert(URI_PAYMENT)
                .withValues(transactionModel.toValues())
                .build());

        if (changeCreditReceipt != null)
            operations.add(ContentProviderOperation.newInsert(URI_CREDIT_RECEIPT)
                    .withValues(changeCreditReceipt.toValues())
                    .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand command = batchInsert(PaymentTransactionModel.class);

        command.add(JdbcFactory.getConverter(transactionModel).insertSQL(transactionModel, getAppCommandContext()));

        if (changeCreditReceipt != null) {
            command.add(JdbcFactory.getConverter(changeCreditReceipt).insertSQL(changeCreditReceipt, getAppCommandContext()));
        }

        return command;
    }

    public static TaskHandler start(Context context, CreditSaleCommandBaseCallback callback, Transaction data, CreditReceiptData creditReceiptData) {
        return create(CreditSaleCommand.class).arg(ARG_DATA, data).arg(ARG_CREDIT_INFO, creditReceiptData).callback(callback).queueUsing(context);
    }

    private static class CreditRegisterInfo {
        String guid;
        BigDecimal availableAmount;
        Date createDate;
        int expireTime;

        private CreditRegisterInfo(String guid, BigDecimal availableAmount, Date createDate, int expireTime) {
            this.guid = guid;
            this.availableAmount = availableAmount;
            this.createDate = createDate;
            this.expireTime = expireTime;
        }
    }

    public static abstract class CreditSaleCommandBaseCallback {

        @OnSuccess(CreditSaleCommand.class)
        public final void onSuccess(@Param(EXTRA_CHANGE) CreditReceiptModel changeReceipt) {
            handleOnSuccess(changeReceipt);
        }

        @OnFailure(CreditSaleCommand.class)
        public final void onFailure() {
            handleOnFailure();
        }

        protected abstract void handleOnSuccess(CreditReceiptModel changeReceipt);

        protected abstract void handleOnFailure();
    }
}
