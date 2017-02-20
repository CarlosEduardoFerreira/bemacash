package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.SaleOrderItemAddonJdbcConverter;
import com.kaching123.tcr.jdbc.converters.UnitsJdbcConverter;
import com.kaching123.tcr.model.OrderStatus;
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

import static com.kaching123.tcr.model.ContentValuesUtil._orderStatus;

public class RemoveSaleOrderItemCommand extends AsyncCommand {

    private static final Uri URI_ORDER = ShopProvider.getContentUri(ShopStore.SaleOrderTable.URI_CONTENT);
    private static final Uri URI_DEFINED_ON_HOLD = ShopProvider.getContentUri(ShopStore.DefinedOnHoldTable.URI_CONTENT);
    private static final Uri URI_ITEMS = ShopProvider.getContentUri(SaleItemTable.URI_CONTENT);
    private static final Uri URI_SALE_ADDONS = ShopProvider.getNoNotifyContentUri(SaleAddonTable.URI_CONTENT);
    private static final Uri URI_UNIT = ShopProvider.getContentUri(ShopStore.UnitTable.URI_CONTENT);

    private static final String ARG_SALE_ITEM_GUID = "arg_sale_item_guid";
    private static final String ARG_ACTION_TYPE = "arg_action_type";

    private String saleItemId;
    private boolean skipOrderUpdate;

    private String orderGuid;
    private String itemGuid;
    private String discountBundleId;

    private OrderStatus orderStatus;
    private ActionType actionType;
    private String orderHoldName;

    private SyncResult updateOrderResult;
    private List<SyncResult> removeDiscountResults;

    @Override
    protected TaskResult doCommand() {
        if (saleItemId == null)
            saleItemId = getStringArg(ARG_SALE_ITEM_GUID);
        if (actionType == null)
            actionType = (ActionType)getArgs().getSerializable(ARG_ACTION_TYPE);

        if (!loadData())
            return failed();

        if(orderStatus == OrderStatus.HOLDON && actionType != null) {
            switch (actionType) {
                case REMOVE:
                    new PrintItemsForKitchenCommand().sync(getContext(), true, false, orderGuid, null, true, true, orderHoldName, true, saleItemId, getAppCommandContext());
                    break;
            }
        }

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
        Cursor itemCursor = ProviderAction.query(URI_ITEMS)
                .projection(SaleItemTable.ORDER_GUID, SaleItemTable.ITEM_GUID, SaleItemTable.DISCOUNT_BUNDLE_ID)
                .where(SaleItemTable.SALE_ITEM_GUID + " = ?", saleItemId)
                .perform(getContext());

        if (!itemCursor.moveToFirst()) {
            itemCursor.close();
            return false;
        }

        orderGuid = itemCursor.getString(0);
        itemGuid = itemCursor.getString(1);
        discountBundleId = itemCursor.getString(2);

        itemCursor.close();

        Cursor orderCursor = ProviderAction.query(URI_ORDER)
                .projection(ShopStore.SaleOrderTable.STATUS, ShopStore.SaleOrderTable.HOLD_NAME, ShopStore.SaleOrderTable.DEFINED_ON_HOLD_ID)
                .where(ShopStore.SaleOrderTable.GUID + " = ?", orderGuid)
                .perform(getContext());

        if (!orderCursor.moveToFirst()) {
            orderCursor.close();
            return false;
        }

        orderStatus =  _orderStatus(orderCursor, 0);
        orderHoldName =  orderCursor.getString(1);
        String definedTableId =  orderCursor.getString(2);
        orderCursor.close();

        if (!TextUtils.isEmpty(definedTableId)) {
            Cursor definedOnHoldCursor = ProviderAction.query(URI_DEFINED_ON_HOLD)
                    .projection(ShopStore.DefinedOnHoldTable.NAME)
                    .where(ShopStore.DefinedOnHoldTable.ID + " = ?", definedTableId)
                    .perform(getContext());
            try {
                if (definedOnHoldCursor.moveToFirst()) {
                    orderHoldName = definedOnHoldCursor.getString(0);
                }
            } finally {
                definedOnHoldCursor.close();
            }
        }

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
                }).toList();
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

    public static void start(Context context, String saleItemGuid, ActionType actionType, Object callback) {
        create(RemoveSaleOrderItemCommand.class)
                .arg(ARG_SALE_ITEM_GUID, saleItemGuid)
                .arg(ARG_ACTION_TYPE, actionType)
                .callback(callback)
                .queueUsing(context);
    }

    public SyncResult sync(Context context, String saleItemGuid, ActionType actionType, IAppCommandContext appCommandContext) {
        this.saleItemId = saleItemGuid;
        this.actionType = actionType;
        skipOrderUpdate = true;
        return syncDependent(context, appCommandContext);
    }

    public enum ActionType {
        REMOVE,
        VOID
    }
}
