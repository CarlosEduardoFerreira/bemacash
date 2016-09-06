package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.converters.IOrderNumUpdater;
import com.kaching123.tcr.service.ISqlCommand;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 30.08.2016.
 */
public abstract class BaseUpdateOrderNumCommand extends AsyncCommand {

    protected final static String ARG_ID = "ARG_ID";
    protected final static String ARG_ORDER_NUM = "ARG_ORDER_NUM";

    private String id;
    private int orderNum;

    @Override
    protected TaskResult doCommand() {
        id = getStringArg(ARG_ID);
        orderNum = getIntArg(ARG_ORDER_NUM);
        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        IOrderNumUpdater converter = createConverter();
        return converter.updateOrderNum(id, orderNum, getAppCommandContext());
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>(1);
        ops.add(ContentProviderOperation.newUpdate(getUri())
                .withValue(getOrderNumColumn(), orderNum)
                .withSelection(getIdColumn() + " = ?", new String[]{id})
                .build()
        );
        return ops;
    }

    protected abstract IOrderNumUpdater createConverter();
    protected abstract Uri getUri();
    protected abstract String getIdColumn();
    protected abstract String getOrderNumColumn();

    public SyncResult syncDependent(Context context, String modifierGuid, int orderNum, IAppCommandContext appCommandContext){
        Bundle args = new Bundle(2);
        args.putString(ARG_ID, modifierGuid);
        args.putInt(ARG_ORDER_NUM, orderNum);
        return syncDependent(context, args, appCommandContext);
    }
}
