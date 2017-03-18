package com.kaching123.tcr.commands.store.inventory;

import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.kaching123.tcr.commands.AtomicUpload;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.ItemsJdbcConverter;
import com.kaching123.tcr.jdbc.converters.TaxGroupJdbcConverter;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.service.OfflineCommandsService;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;

import java.util.ArrayList;

import static com.kaching123.tcr.model.SqlCommandHelper.getContentValues;

/**
 * Created by pkabakov on 25.12.13.
 */
public class DeleteTaxGroupCommand extends AsyncCommand {

    private static final Uri TAX_GROUP_URI = ShopProvider.getContentUri(ShopStore.TaxGroupTable.URI_CONTENT);
    private static final Uri ITEMS_URI = ShopProvider.getContentUri(ShopStore.ItemTable.URI_CONTENT);

    private static final String ARG_MODEL = "tax_groups";

    protected TaxGroupModel model;

    @Override
    protected TaskResult doCommand() {
        model = (TaxGroupModel) getArgs().getSerializable(ARG_MODEL);

        return succeeded();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newUpdate(ITEMS_URI)
                .withValue(ShopStore.ItemTable.TAX_GROUP_GUID, null)
                .withSelection(ShopStore.ItemTable.TAX_GROUP_GUID + " = ?", new String[]{model.guid})
                .build());

        operations.add(ContentProviderOperation.newUpdate(TAX_GROUP_URI)
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(ShopStore.TaxGroupTable.GUID + " = ?", new String[]{model.guid})
                .build());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {

        BatchSqlCommand sqlTaxGroup = batchUpdate(model);
        sqlTaxGroup.add(JdbcFactory.getConverter(model).deleteSQL(model, getAppCommandContext()));
        new AtomicUpload().upload(sqlTaxGroup, AtomicUpload.UploadType.WEB);

        ItemsJdbcConverter itemsJdbcConverter = (ItemsJdbcConverter)JdbcFactory.getConverter(ShopStore.ItemTable.TABLE_NAME);
        BatchSqlCommand sqlItem = batchUpdate(ItemModel.class);
        sqlItem.add(itemsJdbcConverter.removeTaxGroup(model.guid, getAppCommandContext()));
        new AtomicUpload().upload(sqlItem, AtomicUpload.UploadType.WEB);

        return sqlTaxGroup;
    }

    public static void start(Context context, TaxGroupModel model, BaseDeleteTaxGroupCallback callback) {
        create(DeleteTaxGroupCommand.class).arg(ARG_MODEL, model).callback(callback).queueUsing(context);
    }

    public static abstract class BaseDeleteTaxGroupCallback{

        @OnSuccess(DeleteTaxGroupCommand.class)
        public void onSuccess() {
            onTaxGroupDeleted();
        }

        @OnFailure(DeleteTaxGroupCommand.class)
        public void onFailure() {
            onTaxGroupDeleteError();
        }

        protected abstract void onTaxGroupDeleted();

        protected abstract void onTaxGroupDeleteError();

    }
}
