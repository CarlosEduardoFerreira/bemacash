package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;

import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 16.01.14.
 */
public class UpdateItemOrderCommand extends AsyncCommand {

    private static final String ARG_GUIDS = "arg_guids";
    private static final String ARG_OFFSET = "arg_offset";

    private ArrayList<ContentProviderOperation> ops;
    private BatchSqlCommand sqlCommand;

    @Override
    protected TaskResult doCommand() {

        String[] guids = getArgs().getStringArray(ARG_GUIDS);
        int offset = getIntArg(ARG_OFFSET);

        ItemsJdbcConverter jdbcConverter = (ItemsJdbcConverter) JdbcFactory.getConverter(ItemTable.TABLE_NAME);
        sqlCommand = batchUpdate(ItemModel.class);
        ops = new ArrayList<>();
        for (int i = 0; i < guids.length; i++){
            String guid = guids[i];
            int orderNum = i + offset + 1;
            ops.add(ContentProviderOperation.newUpdate(ShopProvider.contentUri(ItemTable.URI_CONTENT))
                    .withSelection(ItemTable.GUID + " = ?", new String[]{guid})
                    .withValue(ItemTable.ORDER_NUM, orderNum)
                    .build());
            sqlCommand.add(jdbcConverter.updateOrderSQL(guid, orderNum, this.getAppCommandContext()));
        }

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return ops;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sqlCommand;
    }

    public static void start(Context context, String[] guids, int offset){
        create(UpdateItemOrderCommand.class).arg(ARG_GUIDS, guids).arg(ARG_OFFSET, offset).queueUsing(context);
    }
}
