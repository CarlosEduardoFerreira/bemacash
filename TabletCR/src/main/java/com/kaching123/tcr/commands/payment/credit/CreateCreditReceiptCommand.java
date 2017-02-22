package com.kaching123.tcr.commands.payment.credit;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CreditReceiptModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CreditReceiptTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * Created by gdubina on 25/02/14.
 */
public class CreateCreditReceiptCommand extends AsyncCommand {

    private static final Uri URI_CREDIT_RECEIPT = ShopProvider.getContentUri(CreditReceiptTable.URI_CONTENT);
    private static final Uri URI_CREDIT_RECEIPT_GROUP_BY = ShopProvider.getContentUriGroupBy(CreditReceiptTable.URI_CONTENT, CreditReceiptTable.REGISTER_ID);

    private static final String ARG_AMOUNT = "ARG_AMOUNT";

    private static final String EXTRA_RECEIPT = "EXTRA_RECEIPT";

    private CreditReceiptModel model;

    public static CreditReceiptModel createCreditReceiptModel(Context context,
                                                              BigDecimal amount,
                                                              int creditReceiptExpireTime,
                                                              String operatorGuid,
                                                              long registerId,
                                                              String shiftGuid) {
        CreditReceiptModel receiptModel = new CreditReceiptModel(
                UUID.randomUUID().toString(),
                operatorGuid,
                registerId,
                shiftGuid,
                new Date(),
                amount,
                getNextPrintNumber(context, registerId),
                creditReceiptExpireTime,
                null
        );
        return receiptModel;
    }

    public static long getNextPrintNumber(Context context, long registerId) {
        return _wrap(ProviderAction
                .query(URI_CREDIT_RECEIPT_GROUP_BY)
                .projection("max(" + CreditReceiptTable.PRINT_NUMBER + ")")
                .where(CreditReceiptTable.REGISTER_ID + " = ?", registerId)
                .perform(context), new Function<Cursor, Long>() {
            @Override
            public Long apply(Cursor cursor) {
                if (cursor.moveToFirst()) {
                    return cursor.getLong(0) + 1L;
                }
                return 1L;
            }
        });
    }

    public static void start(Context context, BigDecimal amount, CreateCreditReceiptBaseCallback callback) {
        create(CreateCreditReceiptCommand.class)
                .arg(ARG_AMOUNT, amount)
                .callback(callback)
                .queueUsing(context);
    }

    @Override
    protected TaskResult doCommand() {
        model = createCreditReceiptModel((BigDecimal) getArgs().getSerializable(ARG_AMOUNT));
        return succeeded().add(EXTRA_RECEIPT, model);
    }

    private CreditReceiptModel createCreditReceiptModel(BigDecimal amount) {
        return createCreditReceiptModel(getContext(), amount, getApp().getShopInfo().creditReceiptExpireTime,
                getAppCommandContext().getEmployeeGuid(),
                getAppCommandContext().getRegisterId(),
                getAppCommandContext().getShiftGuid());
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newInsert(URI_CREDIT_RECEIPT)
                .withValues(model.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(model).insertSQL(model, getAppCommandContext());
    }


    public static abstract class CreateCreditReceiptBaseCallback {

        @OnSuccess(CreateCreditReceiptCommand.class)
        public final void onSuccess(@Param(EXTRA_RECEIPT) CreditReceiptModel receipt) {
            handleSuccess(receipt);
        }

        protected abstract void handleSuccess(CreditReceiptModel receipt);

        @OnFailure(CreateCreditReceiptCommand.class)
        public final void onFailure() {
            handleOnFailure();
        }

        protected abstract void handleOnFailure();
    }

}
