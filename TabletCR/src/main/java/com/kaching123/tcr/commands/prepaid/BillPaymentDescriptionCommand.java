package com.kaching123.tcr.commands.prepaid;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.BillPaymentDescriptionModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;

/**
 * Created by yinteli on 16/3/7.
 */
public class BillPaymentDescriptionCommand extends AsyncCommand {
    public static final String ARG_BILLPAYMENT_DESCRIPTION_MODEL = "ARG_BILLPAYMENT_DESCRIPTION_MODEL";
    private static final Uri BILLPAYMENT_DESCRIPTION_URI = ShopProvider.getContentUri(ShopStore.BillPaymentDescriptionTable.URI_CONTENT);
    private static final String ARG_IS_ORDER_GUID = "ARG_IS_ORDER_GUID";
    private static final String EXTRA_IS_ORDER_GUID = "EXTRA_IS_ORDER_GUID";

    protected BillPaymentDescriptionModel model;
    @Override
    protected TaskResult doCommand() {
        model = (BillPaymentDescriptionModel)getArgs().getSerializable(ARG_BILLPAYMENT_DESCRIPTION_MODEL);
        boolean isOrderGuid = getBooleanArg(ARG_IS_ORDER_GUID);
        return succeeded().add(EXTRA_IS_ORDER_GUID, isOrderGuid);
    }

    @Override
    protected ISqlCommand createSqlCommand() {

        Log.d("BemaCarl6","BillPaymentDescriptionCommand.createSqlCommand.order." + model.isVoided);
        BatchSqlCommand batch = batchInsert(model);
        batch.add(JdbcFactory.insert(model, getAppCommandContext()));

        new AtomicUpload().upload(batch, AtomicUpload.UploadType.WEB);

        return batch;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newInsert(BILLPAYMENT_DESCRIPTION_URI)
                .withValues(model.toValues())
                .build());
        return operations;
    }

    public static void start(Context context, BillPaymentDescriptionModel model,boolean isOrderGuid, BillPaymentDescriptionCallback callback) {
        create(BillPaymentDescriptionCommand.class).arg(ARG_BILLPAYMENT_DESCRIPTION_MODEL, model).arg(ARG_IS_ORDER_GUID, isOrderGuid).callback(callback).queueUsing(context);
    }

    public static abstract class BillPaymentDescriptionCallback {

        @OnSuccess(BillPaymentDescriptionCommand.class)
        public void handleSuccess(@Param(EXTRA_IS_ORDER_GUID) boolean isOrderGuid) {
            onSuccess(isOrderGuid);
        }

        @OnFailure(BillPaymentDescriptionCommand.class)
        public void handleFailure() {
            onFailure();
        }

        protected abstract void onSuccess(boolean isOrderGuid);

        protected abstract void onFailure();
    }
}
