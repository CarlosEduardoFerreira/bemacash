package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by Vladimir on 29.08.2016.
 */
public abstract class BaseBatchUpdateOrderCommand extends AsyncCommand {

    protected static final String ARG_GUIDS = "ARG_GUIDS";

    private ArrayList<ContentProviderOperation> ops;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        String[] guids = getArgs().getStringArray(ARG_GUIDS);
        if (guids == null || guids.length == 0)
            return failed();

        Map<String, Integer> currentOrder = getCurrentOrder(guids[0]);
        if (currentOrder == null)
            return failed();

        ops = new ArrayList<>();
        sql = batchUpdate(ItemTable.TABLE_NAME);
        for (int i = 0; i < guids.length; i++) {
            String guid = guids[i];
            int orderNum = i + 1;
            if (orderNum != currentOrder.get(guid)){
                SyncResult result = updateSingleOrderNum(guid, orderNum);
                if (result == null)
                    return failed();
                ops.addAll(result.getLocalDbOperations());
                sql.add(result.getSqlCmd());
            }
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

    @Nullable
    protected abstract Map<String, Integer> getCurrentOrder(String guid);
    protected abstract SyncResult updateSingleOrderNum(String guid, int orderNum);
}
