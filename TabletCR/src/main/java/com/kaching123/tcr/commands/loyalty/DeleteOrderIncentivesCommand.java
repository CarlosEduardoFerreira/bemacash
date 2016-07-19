package com.kaching123.tcr.commands.loyalty;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.SaleIncentiveModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.SaleIncentiveTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by Vladimir on 19.07.2016.
 */
public class DeleteOrderIncentivesCommand extends AsyncCommand {

    private static final Uri URI_SALE_INCENTIVE = ShopProvider.contentUri(SaleIncentiveTable.URI_CONTENT);
    private static final String ARG_ORDER_ID = "ARG_ORDER_ID";

    private String orderId;
    private ArrayList<SaleIncentiveModel> models;

    @Override
    protected TaskResult doCommand() {
        orderId = getStringArg(ARG_ORDER_ID);

        Cursor c = ProviderAction.query(URI_SALE_INCENTIVE)
                .projection(SaleIncentiveTable.GUID)
                .where(SaleIncentiveTable.ORDER_ID + " = ?", orderId)
                .perform(getContext());

        models = new ArrayList<>(c.getCount());
        while (c.moveToNext()){
            models.add(new SaleIncentiveModel(c.getString(0)));
        }
        c.close();

        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchDelete(SaleIncentiveModel.class);
        for (SaleIncentiveModel model : models){
            batch.add(JdbcFactory.delete(model, getAppCommandContext()));
        }
        return batch;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>(1);
        ops.add(ContentProviderOperation.newUpdate(URI_SALE_INCENTIVE)
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(SaleIncentiveTable.ORDER_ID + " = ?", new String[]{orderId})
                .build()
        );
        return ops;
    }

    public SyncResult sync(Context context, String orderId, IAppCommandContext appCommandContext){
        Bundle args = new Bundle(1);
        args.putString(ARG_ORDER_ID, orderId);
        return syncDependent(context, args, appCommandContext);
    }
}
