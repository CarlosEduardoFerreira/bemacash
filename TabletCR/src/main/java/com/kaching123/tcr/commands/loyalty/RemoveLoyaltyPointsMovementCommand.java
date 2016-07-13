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

    private LoyaltyPointsMovementModel movement;

    @Override
    protected TaskResult doCommand() {
        String orderId = getStringArg(ARG_ORDER_ID);
        Cursor c = ProviderAction.query(URI_MOVEMENTS)
                .projection(LoyaltyPointsMovementTable.GUID)
                .where(LoyaltyPointsMovementTable.SALE_ORDER_ID + " = ?", orderId)
                .perform(getContext());

        try {
            if (c.moveToFirst()){
                movement = new LoyaltyPointsMovementModel(c.getString(0), null, null, null);
            }else{
                return succeeded();
            }
        } finally {
            c.close();
        }

        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        if (movement == null)
            return null;

        return JdbcFactory.delete(movement, getAppCommandContext());
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        if (movement == null)
            return null;

        ArrayList<ContentProviderOperation> ops = new ArrayList<>(1);
        ops.add(ContentProviderOperation.newUpdate(URI_MOVEMENTS)
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(LoyaltyPointsMovementTable.GUID + " = ?", new String[]{movement.guid})
                .build()
        );
        return ops;
    }

    public static void start(Context context, String orderGuid){
        create(RemoveLoyaltyPointsMovementCommand.class).arg(ARG_ORDER_ID, orderGuid).queueUsing(context);
    }

    public SyncResult syncNow(Context context, String orderGuid, IAppCommandContext appCommandContext){
        Bundle args = new Bundle(1);
        args.putString(ARG_ORDER_ID, orderGuid);
        return syncDependent(context, args, appCommandContext);
    }
}
