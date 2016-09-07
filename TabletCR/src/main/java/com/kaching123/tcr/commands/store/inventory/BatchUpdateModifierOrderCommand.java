package com.kaching123.tcr.commands.store.inventory;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.converter.ModifierFunction;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ModifierTable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Vladimir on 29.08.2016.
 */
public class BatchUpdateModifierOrderCommand extends BaseBatchUpdateOrderCommand {

    private UpdateModifierOrderNumCommand cmd;

    @Override
    protected Map<String, Integer> getCurrentOrder(String guid) {
        Uri uri = ShopProvider.contentUri(ModifierTable.URI_CONTENT);
        ModifierModel modifier = ProviderAction.query(uri)
                .where(ModifierTable.MODIFIER_GUID + " = ?", guid)
                .perform(getContext())
                .toFluentIterable(new ModifierFunction())
                .first().orNull();
        if (modifier == null)
            return null;

        Query query = ProviderAction.query(uri)
                .projection(ModifierTable.MODIFIER_GUID, ModifierTable.ORDER_NUM)
                .where(ModifierTable.ITEM_GUID + " = ?", modifier.itemGuid)
                .where(ModifierTable.TYPE + " = ?", modifier.type.ordinal());

        if (modifier.modifierGroupGuid != null)
            query.where(ModifierTable.ITEM_GROUP_GUID + " = ?", modifier.modifierGroupGuid);

        Cursor c = query.perform(getContext());

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
            cmd = new UpdateModifierOrderNumCommand();

        return cmd.syncDependent(getContext(), guid, orderNum, getAppCommandContext());
    }

    public static void start(Context context, String[] orderedGuids){
        create(BatchUpdateModifierOrderCommand.class).arg(ARG_GUIDS, orderedGuids).queueUsing(context);
    }
}
