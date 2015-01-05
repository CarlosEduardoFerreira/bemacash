package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gdubina on 19/12/13.
 */
public class AddItemsMovementCommand extends AsyncCommand {

    private static final Uri MOVEMENT_URI = ShopProvider.getContentUri(ItemMovementTable.URI_CONTENT);

    private List<ItemMovementModel> models;

    @Override
    protected TaskResult doCommand() {
        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        final ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        for (ItemMovementModel model: models) {
            operations.add(
                    ContentProviderOperation.newInsert(MOVEMENT_URI)
                            .withValues(model.toValues())
                            .build());
        }
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchInsert(ItemMovementModel.class);
        JdbcConverter converter = null;
        for (ItemMovementModel model: models) {
            if (converter == null)
                converter = JdbcFactory.getConverter(model);
            batch.add(converter.insertSQL(model, getAppCommandContext()));
        }
        return batch;
    }

    public SyncResult syncNow(Context context, List<ItemMovementModel> models, IAppCommandContext appCommandContext){
        this.models = models;
        return syncDependent(context, appCommandContext);
    }
}
