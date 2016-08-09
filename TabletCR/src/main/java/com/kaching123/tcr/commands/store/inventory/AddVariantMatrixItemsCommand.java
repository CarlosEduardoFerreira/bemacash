package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.converter.ItemMatrixFunction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemMatrixTable;
import com.kaching123.tcr.util.CursorUtil;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;

public class AddVariantMatrixItemsCommand extends AsyncCommand {

    private static final Uri ITEM_MATRIX_URI = ShopProvider.contentUri(ShopStore.ItemMatrixTable.URI_CONTENT);
    private static final String ARG_VARIANT_MATRIX = "arg_variant_matrix";

    private ArrayList<ItemMatrixModel> models;
    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        models = (ArrayList<ItemMatrixModel>) getArgs().getSerializable(ARG_VARIANT_MATRIX);
        JdbcConverter<ItemMatrixModel> jdbc = JdbcFactory.getConverter(ItemMatrixTable.TABLE_NAME);
        operations = new ArrayList<>();
        sql = batchInsert(ItemMatrixModel.class);
        for (ItemMatrixModel m : models) {
            operations.add(ContentProviderOperation.newInsert(ShopProvider.contentUri(ItemMatrixTable.URI_CONTENT))
                    .withValues(m.toValues())
                    .build());
            sql.add(jdbc.insertSQL(m, this.getAppCommandContext()));

            if(m.childItemGuid != null)
                clearDuplicates(m);
        }
        return succeeded();
    }

    private void clearDuplicates(ItemMatrixModel model) {
        Cursor c = ProviderAction.query(ITEM_MATRIX_URI).where(ShopStore.ItemMatrixTable.CHILD_GUID + "=?", model.childItemGuid).perform(getContext());
        if (c.moveToFirst()) {
            ItemMatrixModel itemMatrixModel = CursorUtil._wrap(c, new ItemMatrixFunction());
            itemMatrixModel.childItemGuid = null;
            operations.add(ContentProviderOperation.newUpdate(ITEM_MATRIX_URI)
                    .withSelection(ShopStore.ItemMatrixTable.GUID + "=?", new String[]{itemMatrixModel.guid})
                    .withValues(itemMatrixModel.toValues())
                    .build());
            sql.add(JdbcFactory.update(itemMatrixModel, getAppCommandContext()));
        }
        c.close();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sql;
    }

    public static void start(Context context, ArrayList<ItemMatrixModel> models) {
        create(AddVariantMatrixItemsCommand.class).arg(ARG_VARIANT_MATRIX, models).queueUsing(context);
    }

}
