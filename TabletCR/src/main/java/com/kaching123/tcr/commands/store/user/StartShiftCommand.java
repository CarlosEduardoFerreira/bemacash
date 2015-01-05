package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ShiftModel;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ShiftTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by gdubina on 03/12/13.
 */
public class StartShiftCommand extends AsyncCommand {

    private static final Uri SHIFT_URI = ShopProvider.getContentUri(ShiftTable.URI_CONTENT);

    private static final String ARG_OPEN_AMOUNT = "ARG_OPEN_AMOUNT";

    private ShiftModel model;

    @Override
    protected TaskResult doCommand() {
        BigDecimal openAmaunt = (BigDecimal) getArgs().getSerializable(ARG_OPEN_AMOUNT);
        model = new ShiftModel(
                UUID.randomUUID().toString(),
                new Date(),
                null,
                getAppCommandContext().getEmployeeGuid(),
                null,
                getAppCommandContext().getRegisterId(),
                openAmaunt,
                null
        );

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newInsert(SHIFT_URI)
                .withValues(model.toValues())
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(model).insertSQL(model, getAppCommandContext());
    }

    public static void start(Context context, BaseStartShiftCallback callback, BigDecimal openAmount) {
        create(StartShiftCommand.class).arg(ARG_OPEN_AMOUNT, openAmount).callback(callback).queueUsing(context);
    }

    public static abstract class BaseStartShiftCallback {

        @OnSuccess(StartShiftCommand.class)
        public void onSuccess() {
            onShiftOpened();
        }

        @OnFailure(StartShiftCommand.class)
        public void onFailure() {
            onShiftOpenedError();
        }

        protected abstract void onShiftOpened();

        protected abstract void onShiftOpenedError();

    }
}
