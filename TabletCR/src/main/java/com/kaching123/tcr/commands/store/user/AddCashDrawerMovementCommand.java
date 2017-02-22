package com.kaching123.tcr.commands.store.user;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CashDrawerMovementModel;
import com.kaching123.tcr.model.payment.MovementType;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

/**
 * Created by pkabakov on 17.12.13.
 */
public class AddCashDrawerMovementCommand extends AsyncCommand {

    private static final Uri MOVEMENTS_URI = ShopProvider.getContentUri(ShopStore.CashDrawerMovementTable.URI_CONTENT);

    private static final String ARG_GUID = "ARG_GUID";
    private static final String ARG_TYPE = "ARG_TYPE";
    private static final String ARG_AMOUNT = "ARG_AMOUNT";
    private static final String ARG_COMMENT = "ARG_COMMENT";

    private CashDrawerMovementModel model;

    @Override
    protected TaskResult doCommand() {
        String guid = getStringArg(ARG_GUID);
        MovementType type = (MovementType) getArgs().getSerializable(ARG_TYPE);
        BigDecimal amount = (BigDecimal) getArgs().getSerializable(ARG_AMOUNT);
        String comment = getStringArg(ARG_COMMENT);

        model = new CashDrawerMovementModel(
                UUID.randomUUID().toString(),
                guid,
                getAppCommandContext().getEmployeeGuid(),
                type,
                amount,
                new Date(),
                comment,
                null);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>(1);
        operations.add(ContentProviderOperation.newInsert(MOVEMENTS_URI).withValues(model.toValues()).build());
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return JdbcFactory.getConverter(model).insertSQL(model, getAppCommandContext());
    }

    public static void start(Context context, Object callback, String guid, MovementType type, BigDecimal amount, String comment) {
        create(AddCashDrawerMovementCommand.class).arg(ARG_GUID, guid).arg(ARG_TYPE, type).arg(ARG_AMOUNT, amount).arg(ARG_COMMENT, comment).callback(callback).queueUsing(context);
    }
}
