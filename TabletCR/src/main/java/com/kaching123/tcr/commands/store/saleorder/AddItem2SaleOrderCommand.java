package com.kaching123.tcr.commands.store.saleorder;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.google.common.base.Function;
import com.kaching123.tcr.commands.store.AsyncCommand;
import com.kaching123.tcr.commands.store.saleorder.PrintItemsForKitchenCommand.KitchenPrintStatus;
import com.kaching123.tcr.commands.wireless.EditUnitCommand;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.OrderStatus;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.SaleOrderModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.converter.SaleOrderItemViewModelWrapFunction;
import com.kaching123.tcr.service.BatchSqlCommand;
import com.kaching123.tcr.service.ISqlCommand;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsView;
import com.kaching123.tcr.store.ShopStore.SaleOrderTable;
import com.telly.groundy.TaskResult;
import com.telly.groundy.annotations.OnCallback;
import com.telly.groundy.annotations.OnFailure;
import com.telly.groundy.annotations.OnSuccess;
import com.telly.groundy.annotations.Param;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.kaching123.tcr.util.CursorUtil._wrap;

public class AddItem2SaleOrderCommand extends AsyncCommand {

    private static final Uri URI_ORDER = ShopProvider.getContentUri(SaleOrderTable.URI_CONTENT);

    private static final Uri URI_ITEM = ShopProvider.getContentUri(ShopStore.SaleItemTable.URI_CONTENT);
    private static final Uri URI_ITEM_NO_NOTIFY = ShopProvider.getNoNotifyContentUri(ShopStore.SaleItemTable.URI_CONTENT);

    private static final Uri URI_ITEM_VIEW = ShopProvider.getContentUri(SaleOrderItemsView.URI_CONTENT);

    private static final String CALLBACK_ADD_ORDER = "CALLBACK_ADD_ORDER";
    private static final String EXTRA_CALLBACK_ORDER = "EXTRA_CALLBACK_ORDER";
    private static final String EXTRA_ITEM = "EXTRA_ITEM";

    private static final String ARG_ITEM = "arg_item";

    private static final String ARG_MODIFIER_GUID = "ARG_MODIFIER_GUID";
    private static final String ARG_ADDONS_GUIDS = "ARG_ADDONS_GUIDS";
    private static final String ARG_OPTIONALS_GUIDS = "ARG_OPTIONALS_GUIDS";

    private static final String ARG_UNIT = "arg_unit";

    private SaleOrderItemModel item;
    private SaleOrderItemViewModel existsItem;
    private Unit unit;

    private String modifierGiud;
    private ArrayList<String> addonGuids;
    private ArrayList<String> optionalGuids;

    private SyncResult updateSaleItemAddonsResult;
    private SyncResult updateSaleOrderKitchenPrintStatusResult;
    private SyncResult editUnitResult;

    @Override
    protected TaskResult doCommand() {
        Bundle args = getArgs();
        item = (SaleOrderItemModel) args.getSerializable(ARG_ITEM);
        modifierGiud = args.getString(ARG_MODIFIER_GUID);
        addonGuids = args.getStringArrayList(ARG_ADDONS_GUIDS);
        optionalGuids = args.getStringArrayList(ARG_OPTIONALS_GUIDS);
        unit = (Unit) args.getSerializable(ARG_UNIT);

        if (TextUtils.isEmpty(item.orderGuid)) {
            SaleOrderModel order = createSaleOrder();
            if (!new AddSaleOrderCommand().sync(getContext(), order, true, getAppCommandContext()))
                return failed();
            item.orderGuid = order.guid;
            if (unit != null)
                unit.orderId = order.guid;

            fireAddOrderEvent(item.orderGuid);
        }

        if (item.priceType != PriceType.UNIT_PRICE && TextUtils.isEmpty(modifierGiud) && (addonGuids == null || addonGuids.isEmpty()) && (optionalGuids == null || optionalGuids.isEmpty())) {
            tryCombineWithExistingItem();
        }

        item.sequence = System.currentTimeMillis();

        if (hasModifiers()) {
            updateSaleItemAddonsResult = new UpdateSaleItemAddonsCommand().sync(getContext(), item.saleItemGuid,
                    item.itemGuid,
                    modifierGiud,
                    addonGuids,
                    optionalGuids,
					false,
					getAppCommandContext());
            if (updateSaleItemAddonsResult == null)
                return failed();
        }

        if (unit != null) {
            editUnitResult = new EditUnitCommand().sync(getContext(), unit, getAppCommandContext());
            if (editUnitResult == null)
                return failed();
        }

        updateSaleOrderKitchenPrintStatusResult = new UpdateSaleOrderKitchenPrintStatusCommand().sync(getContext(), item.orderGuid, KitchenPrintStatus.UPDATED, getAppCommandContext());
        if (updateSaleOrderKitchenPrintStatusResult == null)
            return failed();

        return succeeded().add(EXTRA_ITEM, item);
    }

    private void tryCombineWithExistingItem() {
        List<SaleOrderItemViewModel> items = _wrap(ProviderAction.query(URI_ITEM_VIEW)
                .where(SaleItemTable.ITEM_GUID + " = ?", item.itemGuid)
                .where(SaleItemTable.ORDER_GUID + " = ?", item.orderGuid)
                .orderBy(SaleItemTable.SEQUENCE)
                .perform(getContext()),
                new SaleOrderItemViewModelWrapFunction(getContext(), false));

        for (SaleOrderItemViewModel i : items) {
            if (i != null
                    && item.tmpUnit.size() == 0
                    && comparePrice(i, item)
                    && compareDiscount(i, item) && i.getModifier() == null
                    && (i.getAddons() == null || i.getAddons().isEmpty())) {
                i.itemModel.qty = i.itemModel.qty.add(item.qty);
                item = i.itemModel;
                this.existsItem = i;
                break;
            }
        }
    }

    private void fireAddOrderEvent(String orderGuid) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CALLBACK_ORDER, orderGuid);
        callback(CALLBACK_ADD_ORDER, bundle);
    }

    private static boolean comparePrice(SaleOrderItemViewModel existsItem, SaleOrderItemModel item) {
        if (existsItem.itemModel.price == null || item.price == null)
            return false;
        return existsItem.itemModel.price.compareTo(item.price) == 0;
    }

    private static boolean compareDiscount(SaleOrderItemViewModel existsItem, SaleOrderItemModel item) {
        if ((!existsItem.itemModel.discountable && !item.discountable) || (existsItem.itemModel.discount == null && item.discount == null))
            return true;
        if (existsItem.itemModel.discount == null || item.discount == null || existsItem.itemModel.discountType != item.discountType)
            return false;
        return existsItem.itemModel.discount.compareTo(item.discount) == 0;
    }

    private SaleOrderModel createSaleOrder() {
        return createSaleOrder(getContext(), getAppCommandContext().getRegisterId(), getAppCommandContext().getEmployeeGuid(), getAppCommandContext().getShiftGuid(), OrderType.SALE, BigDecimal.ZERO);
    }

    public static SaleOrderModel createSaleOrder(Context context, long registerId, String operatorGuid, String shiftGuid, OrderType type, BigDecimal transactionFee) {
        Integer seq = _wrap(ProviderAction
                        .query(URI_ORDER)
                        .projection("max(" + SaleOrderTable.PRINT_SEQ_NUM + ")")
                        .where(SaleOrderTable.REGISTER_ID + " = ?", registerId)
                        .perform(context),
                new Function<Cursor, Integer>() {
                    @Override
                    public Integer apply(Cursor cursor) {
                        if (cursor.moveToFirst()) {
                            return cursor.getInt(0) + 1;
                        }
                        return 1;
                    }
                });
        return new SaleOrderModel(UUID.randomUUID().toString(),
                new Date(),
                operatorGuid,
                shiftGuid,
                null,
                BigDecimal.ZERO,
                DiscountType.VALUE,
                OrderStatus.ACTIVE,
                null,
                true,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                seq,
                registerId,
                null,
                type,
                KitchenPrintStatus.PRINT,
                transactionFee
        );
    }

    private boolean hasModifiers() {
        return !TextUtils.isEmpty(modifierGiud) || (addonGuids != null && !addonGuids.isEmpty()) || (optionalGuids != null && !optionalGuids.isEmpty());
    }

    @Override
    protected ArrayList<ContentProviderOperation> createDbOperations() {
        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();

        if (updateSaleItemAddonsResult != null && updateSaleItemAddonsResult.getLocalDbOperations() != null) {
            operations.add(ContentProviderOperation.newInsert(URI_ITEM_NO_NOTIFY)
                    .withValues(item.toValues())
                    .build());
            operations.addAll(updateSaleItemAddonsResult.getLocalDbOperations());
        } else {
            operations.add(ContentProviderOperation.newInsert(URI_ITEM)
                    .withValues(item.toValues())
                    .build());
        }

        if (editUnitResult != null && editUnitResult.getLocalDbOperations() != null)
            operations.addAll(editUnitResult.getLocalDbOperations());

        if (updateSaleOrderKitchenPrintStatusResult.getLocalDbOperations() != null)
            operations.addAll(updateSaleOrderKitchenPrintStatusResult.getLocalDbOperations());

        return operations;
    }

    @Override
    protected ISqlCommand createSqlCommand() {
        BatchSqlCommand batch = batchInsert(item);
        if (existsItem != null) {
            batch.add(JdbcFactory.getConverter(item).updateSQL(item, getAppCommandContext()));
        } else {
            batch.add(JdbcFactory.getConverter(item).insertSQL(item, getAppCommandContext()));
        }
        if (updateSaleItemAddonsResult != null) {
            batch.add(updateSaleItemAddonsResult.getSqlCmd());
        }
        if (editUnitResult != null)
            batch.add(editUnitResult.getSqlCmd());
        batch.add(updateSaleOrderKitchenPrintStatusResult.getSqlCmd());
        return batch;
    }

    public static void start(Context context, BaseAddItem2SaleOrderCallback callback, SaleOrderItemModel model, String modifierGiud, ArrayList<String> addonsGuids, ArrayList<String> optionalGuids, Unit unit) {
        create(AddItem2SaleOrderCommand.class)
                .arg(ARG_ITEM, model)
                .arg(ARG_MODIFIER_GUID, modifierGiud)
                .arg(ARG_ADDONS_GUIDS, addonsGuids)
                .arg(ARG_OPTIONALS_GUIDS, optionalGuids)
                .arg(ARG_UNIT, unit)
                .callback(callback)
                .queueUsing(context);
    }

    public static abstract class BaseAddItem2SaleOrderCallback {

        @OnSuccess(AddItem2SaleOrderCommand.class)
        public void handleSuccess(@Param(AddItem2SaleOrderCommand.EXTRA_ITEM) SaleOrderItemModel item) {
            onItemAdded(item);
        }

        @OnFailure(AddItem2SaleOrderCommand.class)
        public void handleFailure() {
            onItemAddError();
        }

        @OnCallback(value = AddItem2SaleOrderCommand.class, name = CALLBACK_ADD_ORDER)
        public void handleOrderAdded(@Param(EXTRA_CALLBACK_ORDER) String orderGuid) {
            onOrderAdded(orderGuid);
        }

        protected abstract void onItemAdded(SaleOrderItemModel item);

        protected abstract void onItemAddError();

        protected abstract void onOrderAdded(String orderGuid);
    }
}
