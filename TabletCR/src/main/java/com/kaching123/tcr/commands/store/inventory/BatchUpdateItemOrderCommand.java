package com.kaching123.tcr.commands.store.inventory;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.model.converter.StringFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemTable;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * Created by vkompaniets on 31.08.2016.
 */
public class BatchUpdateItemOrderCommand extends BaseBatchUpdateOrderCommand {

    private UpdateItemOrderNumCommand cmd;

    @Nullable
    @Override
    protected Map<String, Integer> getCurrentOrder(String guid) {
        Uri uri = ShopProvider.contentUri(ItemTable.URI_CONTENT);
        String categoryId = ProviderAction.query(uri)
                .projection(ItemTable.CATEGORY_ID)
                .where(ItemTable.GUID + " = ?", guid)
                .perform(getContext())
                .toFluentIterable(new StringFunction())
                .first().orNull();
        if (categoryId == null)
            return null;

        Cursor c = ProviderAction.query(uri)
                .projection(ItemTable.GUID, ItemTable.ORDER_NUM)
                .where(ItemTable.CATEGORY_ID + " = ?", categoryId)
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
            cmd = new UpdateItemOrderNumCommand();

        return cmd.sync(getContext(), guid, orderNum, getAppCommandContext());
    }

    public static void start(Context context, String[] orderedGuids){
        create(BatchUpdateItemOrderCommand.class).arg(ARG_GUIDS, orderedGuids).queueUsing(context);
    }
}
