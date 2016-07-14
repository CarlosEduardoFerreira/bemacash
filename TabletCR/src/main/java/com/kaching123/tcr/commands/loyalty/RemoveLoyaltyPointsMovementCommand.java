package com.kaching123.tcr.commands.loyalty;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.LoyaltyPointsMovementModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.LoyaltyPointsMovementTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 12.07.2016.
 */
public class RemoveLoyaltyPointsMovementCommand extends AsyncCommand {

    private static final Uri URI_MOVEMENTS = ShopProvider.contentUri(LoyaltyPointsMovementTable.URI_CONTENT);
    private static final String ARG_ORDER_ID = "ARG_ORDER_ID";

    private ArrayList<LoyaltyPointsMovementModel> movements = new ArrayList<>();

    @Override
    protected TaskResult doCommand() {
        String orderId = getStringArg(ARG_ORDER_ID);
        Cursor c = ProviderAction.query(URI_MOVEMENTS)
                .projection(LoyaltyPointsMovementTable.GUID)
                .where(LoyaltyPointsMovementTable.SALE_ORDER_ID + " = ?", orderId)
                .perform(getContext());

        while (c.moveToNext()){
            movements.add(new LoyaltyPointsMovementModel(c.getString(0), null, null, null));
        }
        c.close();

        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        if (movements.isEmpty())
            return null;

        BatchSqlCommand cmd = batchDelete(LoyaltyPointsMovementTable.TABLE_NAME);
        for (LoyaltyPointsMovementModel movement : movements){
            cmd.add(JdbcFactory.delete(movement, getAppCommandContext()));
        }

        return cmd;
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        if (movements.isEmpty())
            return null;

        ArrayList<ContentProviderOperation> ops = new ArrayList<>(movements.size());
        for (LoyaltyPointsMovementModel movement : movements){
            ops.add(ContentProviderOperation.newUpdate(URI_MOVEMENTS)
                    .withValues(ShopStore.DELETE_VALUES)
                    .withSelection(LoyaltyPointsMovementTable.GUID + " = ?", new String[]{movement.guid})
                    .build()
            );
        }
        return ops;
    }

    public static void start(Context context, String orderGuid){
        create(RemoveLoyaltyPointsMovementCommand.class).arg(ARG_ORDER_ID, orderGuid).queueUsing(context);
    }

    public SyncResult sync(Context context, String orderGuid, IAppCommandContext appCommandContext){
        Bundle args = new Bundle(1);
        args.putString(ARG_ORDER_ID, orderGuid);
        return syncDependent(context, args, appCommandContext);
    }
}
