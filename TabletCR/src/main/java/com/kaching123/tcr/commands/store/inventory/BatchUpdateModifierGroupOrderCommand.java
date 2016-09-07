package com.kaching123.tcr.commands.store.inventory;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.model.converter.StringFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ModifierGroupTable;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by vkompaniets on 30.08.2016.
 */
public class BatchUpdateModifierGroupOrderCommand extends BaseBatchUpdateOrderCommand{

    private UpdateModifierGroupOrderNumCommand cmd;

    @Nullable
    @Override
    protected Map<String, Integer> getCurrentOrder(String guid) {
        Uri uri = ShopProvider.contentUri(ModifierGroupTable.URI_CONTENT);
        String itemId = ProviderAction.query(uri)
                .projection(ModifierGroupTable.ITEM_GUID)
                .where(ModifierGroupTable.GUID + " = ?", guid)
                .perform(getContext())
                .toFluentIterable(new StringFunction())
                .first().orNull();

        if (itemId == null)
            return null;

        Cursor c = ProviderAction.query(uri)
                .projection(ModifierGroupTable.GUID, ModifierGroupTable.ORDER_NUM)
                .where(ModifierGroupTable.ITEM_GUID + " = ?", itemId)
                .perform(getContext());

        HashMap<String, Integer> currentOrder = new HashMap<>(c.getCount());
        while (c.moveToNext()){
            currentOrder.put(c.getString(0), c.getInt(1));
        }
        c.close();

        return currentOrder;
    }

    @Override
    protected SyncResult updateSingleOrderNum(String guid, int orderNum) {
        if (cmd == null)
            cmd = new UpdateModifierGroupOrderNumCommand();

        return cmd.syncDependent(getContext(), guid, orderNum, getAppCommandContext());
    }

    public static void start(Context context,String[] orderedGuids){
        create(BatchUpdateModifierGroupOrderCommand.class).arg(ARG_GUIDS, orderedGuids).queueUsing(context);
    }
}
