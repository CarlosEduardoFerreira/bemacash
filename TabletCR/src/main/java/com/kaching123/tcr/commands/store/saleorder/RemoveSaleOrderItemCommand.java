package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemAddonJdbcConverter;
import com.kaching123.tcr.jdbc.converters.UnitsJdbcConverter;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.SaleAddonTable;
import com.kaching123.tcr.store.ShopStore.SaleItemTable;
import com.kaching123.tcr.store.ShopStore.UnitTable;
import com.telly.groundy.TaskResult;

import java.util.ArrayList;
import java.util.List;

public class RemoveSaleOrderItemCommand extends AsyncCommand {

    private static final Uri URI_ITEMS = ShopProvider.getContentUri(SaleItemTable.URI_CONTENT);
    private static final Uri URI_SALE_ADDONS = ShopProvider.getNoNotifyContentUri(SaleAddonTable.URI_CONTENT);
    private static final Uri URI_UNIT = ShopProvider.getContentUri(ShopStore.UnitTable.URI_CONTENT);

    private static final String ARG_SALE_ITEM_GUID = "arg_sale_item_guid";

    private String saleItemId;
    private boolean skipOrderUpdate;

    private String orderGuid;
    private String itemGuid;
    private String discountBundleId;

    private SyncResult updateOrderResult;
    private List<SyncResult> removeDiscountResults;

    @Override
    protected TaskResult doCommand() {
        if (saleItemId == null)
            saleItemId = getStringArg(ARG_SALE_ITEM_GUID);

        if (!loadData())
            return failed();

        if (skipOrderUpdate)
            return succeeded();

        updateOrderResult = new UpdateSaleOrderKitchenPrintStatusCommand().sync(getContext(), orderGuid, KitchenPrintStatus.UPDATED, getAppCommandContext());
        if (updateOrderResult == null)
            return failed();

        if (discountBundleId != null){
            List<String> bundleItems = loadDiscountBundleItems(getContext(), discountBundleId);
            if (bundleItems.size() > 1){
                DiscountSaleOrderItemCommand cmd = new DiscountSaleOrderItemCommand();
                removeDiscountResults = new ArrayList<>(bundleItems.size());
                for (String id : bundleItems){
                    SyncResult result = cmd.syncDependent(getContext(), id, null, null, null, getAppCommandContext());
                    if (result == null)
                        return failed();
                    removeDiscountResults.add(result);
                }
            }

        }

        return succeeded();
    }

    private boolean loadData() {
        Cursor c = ProviderAction.query(URI_ITEMS)
                .projection(SaleItemTable.ORDER_GUID, SaleItemTable.ITEM_GUID, SaleItemTable.DISCOUNT_BUNDLE_ID)
                .where(SaleItemTable.SALE_ITEM_GUID + " = ?", saleItemId)
                .perform(getContext());

        if (!c.moveToFirst()) {
            c.close();
            return false;
        }

        orderGuid = c.getString(0);
        itemGuid = c.getString(1);
        discountBundleId = c.getString(2);

        c.close();
        return true;
    }

    private static List<String> loadDiscountBundleItems(Context context, String discountBundleId) {
        return ProviderAction.query(URI_ITEMS)
                .projection(SaleItemTable.SALE_ITEM_GUID)
                .where(SaleItemTable.DISCOUNT_BUNDLE_ID + " = ?", discountBundleId)
                .perform(context)
                .toFluentIterable(new Function<Cursor, String>() {
                    @Override
                    public String apply(Cursor input) {
                        return input.getString(0);
                    }
                }).toImmutableList();
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        operations.add(ContentProviderOperation.newUpdate(URI_SALE_ADDONS)
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(SaleAddonTable.ITEM_GUID + " = ?", new String[]{saleItemId})
                .build());

        operations.add(ContentProviderOperation.newUpdate(URI_UNIT)
                .withValue(UnitTable.SALE_ORDER_ID, null)
                .withValue(UnitTable.SALE_ITEM_ID, null)
                .withSelection(UnitTable.SALE_ORDER_ID + " = ? AND "
                                + UnitTable.ITEM_ID + " = ? AND "
                                + UnitTable.SALE_ITEM_ID + " = ?",
                        new String[]{orderGuid, itemGuid, saleItemId})
                .build());

        operations.add(ContentProviderOperation.newUpdate(URI_ITEMS)
                .withValues(ShopStore.DELETE_VALUES)
                .withSelection(SaleItemTable.SALE_ITEM_GUID + " = ?", new String[]{saleItemId})
                .build());

        if (updateOrderResult != null && updateOrderResult.getLocalDbOperations() != null)
            operations.addAll(updateOrderResult.getLocalDbOperations());

        if (removeDiscountResults != null){
            for (SyncResult result : removeDiscountResults){
                operations.addAll(result.getLocalDbOperations());
            }
        }

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        SaleOrderItemAddonJdbcConverter converter = (SaleOrderItemAddonJdbcConverter) JdbcFactory.getConverter(SaleAddonTable.TABLE_NAME);
        BatchSqlCommand batch = batchDelete(SaleOrderItemModel.class);

        batch.add(converter.deleteSaleItemAddons(saleItemId, getAppCommandContext()));

        UnitsJdbcConverter unitConverter = (UnitsJdbcConverter) JdbcFactory.getConverter(UnitTable.TABLE_NAME);
        batch.add(unitConverter.removeItemFromOrder(orderGuid, itemGuid, saleItemId, getAppCommandContext()));

        SaleOrderItemModel model = new SaleOrderItemModel(saleItemId);
        batch.add(JdbcFactory.getConverter(model).deleteSQL(model, getAppCommandContext()));

        if (updateOrderResult != null)
            batch.add(updateOrderResult.getSqlCmd());

        if (removeDiscountResults != null){
            for (SyncResult result : removeDiscountResults){
                batch.add(result.getSqlCmd());
            }
        }

        return batch;
    }

    public static void start(Context context, String saleItemGuid, Object callback) {
        create(RemoveSaleOrderItemCommand.class)
                .arg(ARG_SALE_ITEM_GUID, saleItemGuid)
                .callback(callback)
                .queueUsing(context);
    }

    public SyncResult sync(Context context, String saleItemGuid, IAppCommandContext appCommandContext) {
        this.saleItemId = saleItemGuid;
        skipOrderUpdate = true;
        return syncDependent(context, appCommandContext);
    }
}
