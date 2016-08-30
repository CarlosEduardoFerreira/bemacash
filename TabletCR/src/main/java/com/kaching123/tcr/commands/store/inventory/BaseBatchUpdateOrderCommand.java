package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.converters.IOrderNumUpdater;
import com.kaching123.tcr.model.IOrderedModel;
import com.kaching123.tcr.model.IValueModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.telly.groundy.TaskResult;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Vladimir on 29.08.2016.
 */
public abstract class BaseBatchUpdateOrderCommand<T extends IValueModel & IOrderedModel & Serializable> extends AsyncCommand {

    protected static final String ARG_OLD_ORDER = "ARG_OLD_ORDER";
    protected static final String ARG_NEW_ORDER = "ARG_NEW_ORDER";

    private ArrayList<ContentProviderOperation> ops;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        Object[] oldOrder = (Object[]) getArgs().get(ARG_OLD_ORDER);
        Object[] newOrder = (Object[]) getArgs().get(ARG_NEW_ORDER);
        if (oldOrder == null || newOrder == null || oldOrder.length != newOrder.length)
            return failed();

        int n = oldOrder.length;
        ops = new ArrayList<>(n);
        sql = createBatch();
        IOrderNumUpdater converter = createConverter();

        for (int i = 0; i < n; i++) {
            String guid = ((T) newOrder[i]).getGuid();
            int orderNum = ((T) oldOrder[i]).getOrderNum();
            ops.add(ContentProviderOperation.newUpdate(getUri())
                    .withSelection(getIdColumn() + " = ?", new String[]{guid})
                    .withValue(getOrderNumColumn(), orderNum)
                    .build());
            sql.add(converter.updateOrderNum(guid, orderNum, this.getAppCommandContext()));
        }

        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sql;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return ops;
    }

    protected abstract Uri getUri();
    protected abstract String getIdColumn();
    protected abstract String getOrderNumColumn();
    protected abstract IOrderNumUpdater createConverter();
    protected abstract BatchSqlCommand createBatch();
}
