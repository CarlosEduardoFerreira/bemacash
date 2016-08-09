package com.kaching123.tcr.util;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.AsyncCommand.SyncResult;
import com.kaching123.tcr.commands.wireless.DropUnitsCommand;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.model.ItemExModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.composer.RemoveComposerCommand;
import com.telly.groundy.PublicGroundyTask;

import java.util.ArrayList;

/**
 * Created by irikhmayer on 21.07.2015.
 */
public class InventoryUtils {

    private static final Uri COMPOSER_URI = ShopProvider.contentUri(ShopStore.ComposerTable.URI_CONTENT);
    private static final Uri UNIT_URI = ShopProvider.contentUri(ShopStore.UnitTable.URI_CONTENT);
    private static final Uri ITEM_MODIFIER_URI = ShopProvider.contentUri(ShopStore.ModifierTable.URI_CONTENT);

    public static boolean removeComposers(String itemGuid,
                                                Context context,
                                                PublicGroundyTask.IAppCommandContext commandContext,
                                                ArrayList<ContentProviderOperation> operations,
                                                BatchSqlCommand sql) {

        Cursor c = null;
        try {
            c = ProviderAction.query(COMPOSER_URI).where(ShopStore.ComposerTable.ITEM_CHILD_ID  + " =? OR " + ShopStore.ComposerTable.ITEM_HOST_ID+ " =? ",
                    itemGuid, itemGuid).perform(context);
            if (c.moveToFirst()) {

                do {
                    ComposerModel model = new ComposerModel();
                    model.guid = c.getString(c.getColumnIndex(ShopStore.ComposerTable.ID));
                    AsyncCommand.SyncResult syncResult4 = new RemoveComposerCommand().sync(context, model, commandContext);
                    if (syncResult4 != null) {
                        operations.addAll(syncResult4.getLocalDbOperations());
                        sql.add(syncResult4.getSqlCmd());
                    } else {
                        return false;
                    }
                } while (c.moveToNext());


            }
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return true;
    }

    public static boolean removeUnits(String itemGuid,
                                            Context context,
                                            PublicGroundyTask.IAppCommandContext commandContext,
                                            ArrayList<ContentProviderOperation> operations,
                                            BatchSqlCommand sql){

        ItemExModel model = new ItemExModel();
        model.guid = itemGuid;
        DropUnitsCommand cmd = new DropUnitsCommand();

        SyncResult result = cmd.sync(context, null, model, commandContext);
        if (result == null){
            return false;
        }else {
            operations.addAll(result.getLocalDbOperations());
            sql.add(result.getSqlCmd());
            return true;
        }
    }
}
