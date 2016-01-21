package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.function.ItemMatrixWrapFunction;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.CursorUtil;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.List;

public class EditVariantMatrixItemsCommand extends AsyncCommand {

    private static final Uri ITEM_MATRIX_URI = ShopProvider.contentUri(ShopStore.ItemMatrixTable.URI_CONTENT);

    private static final String ARG_ITEM_MATRIX = "arg_item_matrix";

    private ArrayList<ItemMatrixModel> models;
    private ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        models = (ArrayList<ItemMatrixModel>) getArgs().getSerializable(ARG_ITEM_MATRIX);
        JdbcConverter<ItemMatrixModel> jdbc = JdbcFactory.getConverter(ShopStore.ItemMatrixTable.TABLE_NAME);
        operations = new ArrayList<>(models.size());
        sql = batchInsert(ItemMatrixModel.class);
        List<String> childGuidsToNullify = new ArrayList<>();
        for (ItemMatrixModel m : models) {
            operations.add(ContentProviderOperation.newUpdate(ITEM_MATRIX_URI)
                    .withValues(m.toValues())
                    .withSelection(ShopStore.ItemMatrixTable.GUID + "=?",
                            new String[]{m.guid})
                    .build());
            sql.add(jdbc.updateSQL(m, this.getAppCommandContext()));
            if (m.childItemGuid != null) {
                childGuidsToNullify.add(m.childItemGuid);
            }
        }
        if (childGuidsToNullify.size() > 0) {
            List<ItemMatrixModel> modelsWithChildrenToNullify = CursorUtil
                    ._wrap(ProviderAction.query(ITEM_MATRIX_URI)
                            .whereIn(ShopStore.ItemMatrixTable.CHILD_GUID, childGuidsToNullify)
                            .perform(getContext()), new ItemMatrixWrapFunction());
            for (ItemMatrixModel m : modelsWithChildrenToNullify) {
                m.childItemGuid = null;
                operations.add(ContentProviderOperation.newUpdate(ITEM_MATRIX_URI)
                        .withValues(m.toValues()).withSelection(ShopStore.ItemMatrixTable.GUID + "=?", new String[]{m.guid})
                        .build());
                sql.add(jdbc.updateSQL(m, this.getAppCommandContext()));
            }
        }
        return succeeded();
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
        create(EditVariantMatrixItemsCommand.class).arg(ARG_ITEM_MATRIX, models).queueUsing(context);
    }
}
