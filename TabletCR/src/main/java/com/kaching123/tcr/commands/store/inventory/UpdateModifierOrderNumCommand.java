package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.os.Bundle;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsModifiersJdbcConverter;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 27.08.2016.
 */
public class UpdateModifierOrderNumCommand extends AsyncCommand {

    private static final String ARG_MODIFIER_ID = "ARG_MODIFIER_ID";
    private static final String ARG_ORDRER_NUM = "ARG_ORDRER_NUM";

    private String modifierId;
    private int orderNum;

    @Override
    protected TaskResult doCommand() {
        modifierId = getStringArg(ARG_MODIFIER_ID);
        orderNum = getIntArg(ARG_ORDRER_NUM);
        return succeeded();
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        ItemsModifiersJdbcConverter converter = (ItemsModifiersJdbcConverter) JdbcFactory.getConverter(ModifierTable.TABLE_NAME);
        return converter.updateOrderNum(modifierId, orderNum, getAppCommandContext());
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> ops = new ArrayList<>(1);
        ops.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(ModifierTable.URI_CONTENT))
                .withValue(ModifierTable.ORDER_NUM, orderNum)
                .withSelection(ModifierTable.MODIFIER_GUID + " = ?", new String[]{modifierId})
                .build()
        );
        return ops;
    }

    public SyncResult syncDependent(Context context, String modifierGuid, int orderNum, IAppCommandContext appCommandContext){
        Bundle args = new Bundle(2);
        args.putString(ARG_MODIFIER_ID, modifierGuid);
        args.putInt(ARG_ORDRER_NUM, orderNum);
        return syncDependent(context, args, appCommandContext);
    }
}
