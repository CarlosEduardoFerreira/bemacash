package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.function.ItemMatrixWrapFunction;
import com.kaching123.tcr.function.VariantItemWrapFunction;
import com.kaching123.tcr.function.VariantSubItemWrapFunction;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemRefType;
import com.kaching123.tcr.model.VariantItemModel;
import com.kaching123.tcr.model.VariantSubItemModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.ItemMatrixTable;
import com.kaching123.tcr.store.ShopStore.ItemTable;
import com.kaching123.tcr.store.ShopStore.VariantItemTable;
import com.kaching123.tcr.store.ShopStore.VariantSubItemTable;
import com.kaching123.tcr.util.CursorUtil;
import com.kaching123.tcr.util.InventoryUtils;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.store.ShopStore.DELETE_VALUES;


/**
 * Created by gdubina on 04.12.13.
 */
public class DeleteItemCommand extends AsyncCommand {

    private static final Uri ITEM_URI = ShopProvider.contentUri(ItemTable.URI_CONTENT);
    private static final Uri ITEM_MATRIX_URI = ShopProvider.contentUri(ItemMatrixTable.URI_CONTENT);
    private static final Uri VARIANT_ITEM_URI = ShopProvider.contentUri(VariantItemTable.URI_CONTENT);
    private static final Uri VARIANT_SUB_ITEM_URI = ShopProvider.contentUri(VariantSubItemTable.URI_CONTENT);

    private static final String ARG_ITEM_GUID = "ARG_ITEM_GUID";

    private String itemGuid;
    ArrayList<ContentProviderOperation> operations;
    private BatchSqlCommand sql;

    @Override
    protected TaskResult doCommand() {
        Logger.d("DeleteItemCommand doCommand");
        itemGuid = getStringArg(ARG_ITEM_GUID);

        operations = new ArrayList<>(1);
        operations.add(ContentProviderOperation.newUpdate(ITEM_URI)
                .withValues(DELETE_VALUES)
                .withSelection(ItemTable.GUID + " = ?", new String[]{itemGuid}).build());
        JdbcConverter<ItemModel> jdbc = JdbcFactory.getConverter(ShopStore.ItemTable.TABLE_NAME);
        ItemModel m = new ItemModel(itemGuid);
        sql = batchDelete(m);
        sql.add(jdbc.deleteSQL(m, this.getAppCommandContext()));

        Cursor c = null;
        try {
            c = ProviderAction.query(ITEM_URI).where(ItemTable.GUID + "=?", itemGuid).perform(getContext());
            if (c.moveToFirst()) {
                // Item exists
                ItemRefType itemRefType = ItemRefType.valueOf(c.getInt(c.getColumnIndex(ItemTable.ITEM_REF_TYPE)));
                if (itemRefType == ItemRefType.Reference) {
                    List<ItemMatrixModel> itemMatrixModels = CursorUtil._wrap(ProviderAction.query(ITEM_MATRIX_URI).where(ItemMatrixTable.PARENT_GUID + "=?", itemGuid).perform(getContext()), new ItemMatrixWrapFunction());
                    SyncResult syncResult1 = new DeleteVariantMatrixItemsCommand().sync(getContext(), itemMatrixModels, getAppCommandContext());
                    if (syncResult1 != null) {
                        operations.addAll(syncResult1.getLocalDbOperations());
                        sql.add(syncResult1.getSqlCmd());
                    } else {
                        return failed();
                    }
                    List<VariantSubItemModel> variantSubItemModels = CursorUtil._wrap(ProviderAction.query(VARIANT_SUB_ITEM_URI).where(VariantSubItemTable.ITEM_GUID + "=?", itemGuid).perform(getContext()), new VariantSubItemWrapFunction());
                    SyncResult syncResult2 = new DeleteVariantSubItemCommand().sync(getContext(), variantSubItemModels, getAppCommandContext());
                    if (syncResult2 != null) {
                        operations.addAll(syncResult2.getLocalDbOperations());
                        sql.add(syncResult2.getSqlCmd());
                    } else {
                        return failed();
                    }
                    List<VariantItemModel> variantItemModels = CursorUtil._wrap(ProviderAction.query(VARIANT_ITEM_URI).where(VariantItemTable.ITEM_GUID + "=?", itemGuid).perform(getContext()), new VariantItemWrapFunction());
                    SyncResult syncResult3 = new DeleteVariantItemsCommand().sync(getContext(), variantItemModels, getAppCommandContext());
                    if (syncResult3 != null) {
                        operations.addAll(syncResult3.getLocalDbOperations());
                        sql.add(syncResult3.getSqlCmd());
                    } else {
                        return failed();
                    }
                }
            }
        } finally {
            if (c != null) {
                c.close();
            }
        }

        if (!InventoryUtils.removeComposers(itemGuid, getContext(), getAppCommandContext(), operations, sql)) {
            return failed();
        } else {
            return succeeded();
        }
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        return sql;
    }

    public static void start(Context context, String itemGuid) {
        create(DeleteItemCommand.class).arg(ARG_ITEM_GUID, itemGuid).queueUsing(context);
    }
}
