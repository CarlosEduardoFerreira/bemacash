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
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;

/**
 * Created by vkompaniets on 16.01.14.
 */
public class UpdateItemOrderCommand extends AsyncCommand {

    private static final String ARG_GUIDS = "arg_guids";

    private ArrayList<ContentProviderOperation> ops;
    private BatchSqlCommand sqlCommand;

    @Override
    protected TaskResult doCommand() {

        String[] guids = getArgs().getStringArray(ARG_GUIDS);

        ItemsJdbcConverter jdbcConverter = (ItemsJdbcConverter) JdbcFactory.getConverter(ItemTable.TABLE_NAME);
        sqlCommand = batchUpdate(ItemModel.class);
        ops = new ArrayList<ContentProviderOperation>();
        for (int i = 0; i < guids.length; i++){
            String guid = guids[i];
            ops.add(ContentProviderOperation.newUpdate(ShopProvider.getContentUri(ItemTable.URI_CONTENT))
                    .withSelection(ItemTable.GUID + " = ?", new String[]{guid})
                    .withValue(ItemTable.ORDER_NUM, i)
                    .build());
            sqlCommand.add(jdbcConverter.updateOrderSQL(guid, i, this.getAppCommandContext()));
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

    public static void start(Context context, String[] guids, BaseUpdateItemOrderCommandCallback callback){
        create(UpdateItemOrderCommand.class).arg(ARG_GUIDS, guids).callback(callback).queueUsing(context);
    }

    public static abstract class BaseUpdateItemOrderCommandCallback {

        @OnSuccess(UpdateItemOrderCommand.class)
        public void onSuccess(){
            onUpdateSuccess();
        }

        @OnFailure(UpdateItemOrderCommand.class)
        public void onFailure(){
            onUpdateFailure();
        }

        protected abstract void onUpdateSuccess();
        protected abstract void onUpdateFailure();
    }
}
