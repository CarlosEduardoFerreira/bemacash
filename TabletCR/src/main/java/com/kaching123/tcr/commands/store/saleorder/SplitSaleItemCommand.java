package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemJdbcConverter;
import com.kaching123.tcr.model.SaleOrderItemAddonModel;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.converter.SaleOrderItemFunction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.SaleAddonTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.telly.groundy.TaskResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._modifierType;
import static com.kaching123.tcr.util.CursorUtil._wrapOrNull;

public class SplitSaleItemCommand extends AsyncCommand {

    private static final Uri URI_ITEM = ShopProvider.getContentUri(SaleItemTable.URI_CONTENT);
    private static final Uri URI_ITEM_ADDONS = ShopProvider.getContentUri(SaleAddonTable.URI_CONTENT);

    private String saleItemGuid;

    private BigDecimal oldQty;
    private BigDecimal splitQty;
    private SaleOrderItemModel newModel;

    private FluentIterable<SaleOrderItemAddonModel> addonModels;

    @Override
    protected TaskResult doCommand() {
        if (!copyItem()) {
            return failed();
        }

        copyAddons();

        return succeeded();
    }

    private boolean copyItem() {
        SaleOrderItemModel oldModel = _wrapOrNull(
                ProviderAction.query(URI_ITEM)
                        .where(SaleItemTable.SALE_ITEM_GUID + " = ?", saleItemGuid)
                        .perform(getContext()),
                new SaleOrderItemFunction());
        if (oldModel == null) {
            return false;
        }

        oldQty = oldModel.qty.subtract(splitQty);

        newModel = oldModel;
        oldModel.saleItemGuid = UUID.randomUUID().toString();
        oldModel.qty = splitQty;

        return true;
    }

    private void copyAddons() {
        addonModels = ProviderAction
                .query(URI_ITEM_ADDONS)
                .projection(
                        SaleAddonTable.ADDON_GUID,
                        SaleAddonTable.EXTRA_COST,
                        SaleAddonTable.TYPE,
                        SaleAddonTable.CHILD_ITEM_ID,
                        SaleAddonTable.CHILD_ITEM_QTY
                ).where(SaleAddonTable.ITEM_GUID + " = ?", saleItemGuid)
                .perform(getContext())
                .toFluentIterable(new Function<Cursor, SaleOrderItemAddonModel>() {
                    @Override
                    public SaleOrderItemAddonModel apply(Cursor c) {
                        //create a copy
                        return new SaleOrderItemAddonModel(
                                UUID.randomUUID().toString(),
                                c.getString(0),
                                newModel.saleItemGuid,
                                _decimal(c.getString(1), BigDecimal.ZERO),
                                _modifierType(c, 2),
                                c.getString(3),
                                _decimal(c.getString(4), BigDecimal.ZERO)
                        );
                    }
                });
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newUpdate(URI_ITEM)
                .withValue(SaleItemTable.QUANTITY, _decimalQty(oldQty))
                .withSelection(SaleItemTable.SALE_ITEM_GUID + " = ?", new String[]{saleItemGuid})
                .build());

        operations.add(ContentProviderOperation.newInsert(URI_ITEM)
                .withValues(newModel.toValues())
                .build());

        for (SaleOrderItemAddonModel addon : addonModels) {
            operations.add(ContentProviderOperation.newInsert(URI_ITEM_ADDONS)
                    .withValues(addon.toValues())
                    .build());
        }

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchUpdate(SaleOrderItemModel.class);

        SaleOrderItemJdbcConverter saleItemConverter = (SaleOrderItemJdbcConverter) JdbcFactory.getConverter(SaleItemTable.TABLE_NAME);
        batch.add(saleItemConverter.updateQty(saleItemGuid, oldQty, getAppCommandContext()));

        batch.add(saleItemConverter.insertSQL(newModel, getAppCommandContext()));

        JdbcConverter saleAddonConverter = null;
        for (SaleOrderItemAddonModel addon : addonModels) {
            if (saleAddonConverter == null)
                saleAddonConverter = JdbcFactory.getConverter(addon);
            batch.add(saleAddonConverter.insertSQL(addon, getAppCommandContext()));
        }

        return batch;
    }

    protected static class SplitSaleItemResult extends SyncResult {

        private String newSaleItemGuid;

        public SplitSaleItemResult(SyncResult result) {
            super(result.getSqlCmd(), result.getLocalDbOperations());
        }

        public String getNewSaleItemGuid() {
            return newSaleItemGuid;
        }

    }

    public SplitSaleItemResult sync(Context context, String saleItemGuid, BigDecimal splitQty, IAppCommandContext appCommandContext) {
        this.saleItemGuid = saleItemGuid;
        this.splitQty = splitQty;
        SyncResult result = super.syncDependent(context, appCommandContext);
        if (result == null)
            return null;
        SplitSaleItemResult splitSaleItemResult = new SplitSaleItemResult(result);
        splitSaleItemResult.newSaleItemGuid = newModel.saleItemGuid;
        return splitSaleItemResult;
    }

}
