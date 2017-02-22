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

/**
 * Created by pkabakov on 04/12/13.
 */
public class StopShiftCommand extends AsyncCommand {

    private static final Uri SHIFT_URI = ShopProvider.getContentUri(ShiftTable.URI_CONTENT);

    private static final String ARG_GUID = "ARG_GUID";
    private static final String ARG_CLOSE_AMOUNT = "ARG_CLOSE_AMOUNT";

    private ShiftModel model;

    @Override
    protected TaskResult doCommand() {
        String guid = getStringArg(ARG_GUID);
        BigDecimal closeAmount = (BigDecimal) getArgs().getSerializable(ARG_CLOSE_AMOUNT);
        model = new ShiftModel(
                guid,
                null,
                new Date(),
                null,
                getAppCommandContext().getEmployeeGuid(),
                getAppCommandContext().getRegisterId(),
                null,
                closeAmount,
                null
        );

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newUpdate(SHIFT_URI)
                .withValues(model.toUpdateValues())
                .withSelection(ShiftTable.GUID + " = ?", new String[]{model.guid})
                .build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(model).updateSQL(model, getAppCommandContext());
    }

    public static void start(Context context, BaseStopShiftCallback callback, String guid, BigDecimal closeAmount) {
        create(StopShiftCommand.class).arg(ARG_GUID, guid).arg(ARG_CLOSE_AMOUNT, closeAmount).callback(callback).queueUsing(context);
    }

    public static abstract class BaseStopShiftCallback {

        @OnSuccess(StopShiftCommand.class)
        public void onSuccess() {
            onShiftClosed();
        }

        @OnFailure(StopShiftCommand.class)
        public void onFailure() {
            onShiftClosedError();
        }

        protected abstract void onShiftClosed();

        protected abstract void onShiftClosedError();

    }
}
