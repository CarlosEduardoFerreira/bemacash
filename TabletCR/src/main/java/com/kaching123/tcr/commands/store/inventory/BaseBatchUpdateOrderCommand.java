package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.converters.OrderNumJdbcConverter;
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
        T[] oldOrder = (T[]) getArgs().getSerializable(ARG_OLD_ORDER);
        T[] newOrder = (T[]) getArgs().getSerializable(ARG_NEW_ORDER);

        int n = oldOrder.length;
        ops = new ArrayList<>(n);
        sql = createBatch();
        OrderNumJdbcConverter converter = createConverter();

        for (int i = 0; i < n; i++) {
            String guid = newOrder[i].getGuid();
            int orderNum = oldOrder[i].getOrderNum();
            ops.add(ContentProviderOperation.newUpdate(getUri())
                    .withSelection(getIdColumn() + " = ?", new String[]{guid})
                    .withValue(getOrderNumColumn(), orderNum)
                    .build());
            sql.add(converter.updateOrderNumSql(guid, orderNum, this.getAppCommandContext()));
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
    protected abstract OrderNumJdbcConverter createConverter();
    protected abstract BatchSqlCommand createBatch();
}
