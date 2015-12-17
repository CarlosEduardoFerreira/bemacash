package com.kaching123.tcr.model.converter;

import android.content.Context;
import android.support.v4.content.Loader;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleItemTable;

import java.util.List;

/**
 * Created by gdubina on 03.12.13.
 */

public class SaleOrderItemViewModelWrapFunction extends SaleItemWrapFunction {

    public SaleOrderItemViewModelWrapFunction(Context context) {
        super(context);
    }

    @Override
    protected boolean loadSerialItems() {
        return true;
    }

    @Override
    protected boolean recalcSaleItems() {
        return true;
    }

    public static Loader<List<SaleOrderItemViewModel>> createLoader(Context context, String orderGuid) {
        return CursorLoaderBuilder.forUri(URI_ORDER_ITEMS)
                .where(SaleItemTable.ORDER_GUID + " = ? ", orderGuid)
                .orderBy(ORDER_BY)
                .wrap(new SaleOrderItemViewModelWrapFunction(context))
                .build(context);
    }
}

/*
public class SaleOrderItemViewModelWrapFunction implements Function<Cursor, List<SaleOrderItemViewModel>> {

    private static final Uri URI_ORDER_ITEMS = ShopProvider.getContentUri(SaleOrderItemsView.URI_CONTENT);
    private static final String ORDER_BY = SaleItemTable.SEQUENCE + ", " + SaleAddonTable.TYPE + ", " + SaleOrderItemsView2.ModifierTable.TITLE;
    private static final Uri UNIT_URI = ShopProvider.getContentUri(ShopStore.UnitTable.URI_CONTENT);

    private static final Uri URI_MODIFIERS_GROUP_BY = ShopProvider.getContentUriGroupBy(ModifierTable.URI_CONTENT, ModifierTable.ITEM_GUID);
    private Context context;
    private boolean collectCounts;

    public SaleOrderItemViewModelWrapFunction(Context context) {
        this(context, true);
    }

    public SaleOrderItemViewModelWrapFunction(Context context, boolean collectCounts) {
        this.context = context;
        this.collectCounts = collectCounts;
    }

    @Override
    public List<SaleOrderItemViewModel> apply(Cursor c) {
        //Logger.d("Provider: wrap - start");
        //long time = System.currentTimeMillis();
        ArrayList<SaleOrderItemViewModel> items = new ArrayList<SaleOrderItemViewModel>();
        HashSet<String> itemsGuids = new HashSet<String>();
        HashMap<String, List<SaleOrderItemViewModel>> itemsMap = new HashMap<String, List<SaleOrderItemViewModel>>();

        final HashMap<String, SaleOrderItemViewModel> saleItemsMap = new HashMap<String, SaleOrderItemViewModel>();
        if (c.moveToFirst()) {
            do {
                String saleItemGuid = c.getString(c.getColumnIndex(SaleItemTable.SALE_ITEM_GUID));
                SaleOrderItemViewModel item = saleItemsMap.get(saleItemGuid);
                if (item == null) {
                    SaleOrderItemModel itemModel = readSaleItemModel(c);
                    int descIndex = _orderType(c, c.getColumnIndex(SaleOrderTable.ORDER_TYPE)) == OrderType.SALE ? c.getColumnIndex(ItemTable.DESCRIPTION) : c.getColumnIndex(BillPaymentDescriptionTable.DESCRIPTION);
                    item = new SaleOrderItemViewModel(
                            itemModel,
                            c.getString(descIndex),
                            c.getString(c.getColumnIndex(ItemTable.EAN_CODE)),
                            c.getString(c.getColumnIndex(ItemTable.PRODUCT_CODE)),
                            c.getString(c.getColumnIndex(ItemTable.UNITS_LABEL)),
                            null, null,
                            _bool(c, c.getColumnIndex(SaleOrderTable.TAXABLE)),
                            _bool(c, c.getColumnIndex(ItemTable.SERIALIZABLE)),
                            _decimal(c, c.getColumnIndex(SaleOrderTable.DISCOUNT)),
                            _discountType(c, c.getColumnIndex(SaleOrderTable.DISCOUNT_TYPE)),
                            _decimal(c, c.getColumnIndex(SaleOrderTable.TRANSACTION_FEE)),
                            !c.isNull(c.getColumnIndex(ItemTable.PRINTER_ALIAS_GUID))
                    );

                    items.add(item);
                    saleItemsMap.put(saleItemGuid, item);
                    if (itemModel.itemGuid != null) {
                        itemsGuids.add(itemModel.itemGuid);
                        List<SaleOrderItemViewModel> existsItems = itemsMap.get(itemModel.itemGuid);
                        if (existsItems == null) {
                            itemsMap.put(itemModel.itemGuid, existsItems = new ArrayList<SaleOrderItemViewModel>());
                        }
                        existsItems.add(item);
                    }
                }
                if (item.isSerializable) {
                    List<Unit> units = _wrap(unitQuery(item.itemModel.orderGuid, item.itemModel.itemGuid)
                            .perform(context), new UnitWrapFunction());
                    item.tmpUnit.addAll(units);
                }
                AddonInfo modifier = readModifier(c);
                if (modifier == null) {
                    continue;
                }
                if (modifier.addon.type == ModifierType.MODIFIER) {
                    item.setModifier(modifier);
                } else if (modifier.addon.type == ModifierType.ADDON) {
                    item.addAddon(modifier);
                } else if(modifier.addon.type == ModifierType.OPTIONAL){
                    item.addAddon(new AddonInfo(modifier.addon, modifier.addonTitle));
                }
            } while (c.moveToNext());
        }

        //Logger.d("Provider: wrap - before modifiers: " + (System.currentTimeMillis() - time));
        if (collectCounts) {
            collectModifiersCount(itemsGuids, itemsMap);
        }
        //Logger.d("Provider: wrap - end: " + (System.currentTimeMillis() - time));

        OrderTotalPriceCalculator.calculate(items, null, new Handler() {

            @Override
            public void handleItem(String saleItemGuid, String description, BigDecimal qty, BigDecimal itemPriceWithAddons, String unitLabel, PriceType priceType, BigDecimal itemSubTotal, BigDecimal itemTotal, BigDecimal itemFinalPrice, BigDecimal itemFinalDiscount, BigDecimal itemFinalTax) {
                SaleOrderItemViewModel item = saleItemsMap.get(saleItemGuid);
                if (item != null) {
                    item.finalPrice = itemFinalPrice;
                }
            }

            @Override
            public void handleTotal(BigDecimal totalDiscount, BigDecimal subTotalItemTotal, BigDecimal totalTaxVatValue, BigDecimal totalOrderPrice, BigDecimal tipsValue) {

            }

        });
        return items;
    }

    public static Query unitQuery(String orderId, String ItemId) {
        Query query = ProviderAction.query(UNIT_URI);
        if (orderId != null) {
            query = query.where(ShopStore.UnitTable.SALE_ORDER_ID + " = ?", orderId);
        }
        if (ItemId != null) {
            query = query.where(ShopStore.UnitTable.ITEM_ID + " = ?", ItemId);
        }
        query = query.where(ShopStore.UnitTable.IS_DELETED + " = ?", 0);
        return query;
    }

    private SaleOrderItemModel readSaleItemModel(Cursor c) {
        return new SaleOrderItemModel(
                c.getString(c.getColumnIndex(SaleItemTable.SALE_ITEM_GUID)),
                c.getString(c.getColumnIndex(SaleItemTable.ORDER_GUID)),
                c.getString(c.getColumnIndex(SaleItemTable.ITEM_GUID)),
                _decimalQty(c, c.getColumnIndex(SaleItemTable.QUANTITY)),
                _decimalQty(c, c.getColumnIndex(SaleItemTable.KITCHEN_PRINTED_QTY)),
                _priceType(c, c.getColumnIndex(SaleItemTable.PRICE_TYPE)),
                _decimal(c, c.getColumnIndex(SaleItemTable.PRICE)),
                _bool(c, c.getColumnIndex(SaleItemTable.DISCOUNTABLE)),
                _decimal(c, c.getColumnIndex(SaleItemTable.DISCOUNT)),
                _discountType(c, c.getColumnIndex(SaleItemTable.DISCOUNT_TYPE)),
                _bool(c, c.getColumnIndex(SaleItemTable.TAXABLE)),
                _decimal(c, c.getColumnIndex(SaleItemTable.TAX)),
                c.getLong(c.getColumnIndex(SaleItemTable.SEQUENCE)),
                c.getString(c.getColumnIndex(SaleItemTable.PARENT_GUID)),
                _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_GROSS_PRICE)),
                _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_TAX)),
                _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_DISCOUNT)),
                _decimalQty(c, c.getColumnIndex(SaleItemTable.TMP_REFUND_QUANTITY)),
                c.getString(c.getColumnIndex(SaleItemTable.NOTES)),
                c.getInt(c.getColumnIndex(SaleItemTable.HAS_NOTES)) == 1);
    }

    private AddonInfo readModifier(Cursor c) {
        String addonGuid = c.getString(c.getColumnIndex(SaleAddonTable.ADDON_GUID));
        if (!TextUtils.isEmpty(addonGuid)) {
            return new AddonInfo(
                    new SaleOrderItemAddonModel(
                            c.getString(c.getColumnIndex(SaleAddonTable.GUID)),
                            addonGuid,
                            c.getString(c.getColumnIndex(SaleAddonTable.ITEM_GUID)),
                            _decimal(c, c.getColumnIndex(SaleAddonTable.EXTRA_COST)),
                            _modifierType(c, c.getColumnIndex(SaleAddonTable.TYPE))),
                    c.getString(c.getColumnIndex(SaleOrderItemsView2.ModifierTable.TITLE))
            );
        }
        return null;
    }

    private void collectModifiersCount(Collection<String> itemsGuids, HashMap<String, List<SaleOrderItemViewModel>> itemsMap) {
        if (itemsGuids.isEmpty())
            return;

        Cursor cursor = ProviderAction
                .query(URI_MODIFIERS_GROUP_BY)
                .projection(
                        ModifierTable.ITEM_GUID,
                        _caseCount(ModifierTable.TYPE, ModifierType.MODIFIER, "mc"),
                        _caseCount(ModifierTable.TYPE, ModifierType.ADDON, "ac"),
                        _caseCount(ModifierTable.TYPE, ModifierType.OPTIONAL, "oc"))
                .whereIn(ModifierTable.ITEM_GUID, itemsGuids)
                .perform(context);

        while (cursor.moveToNext()) {
            String itemId = cursor.getString(0);
            List<SaleOrderItemViewModel> models = itemsMap.get(itemId);
            if (models == null)
                continue;
            for (SaleOrderItemViewModel model : models) {
                model.modifiersCount = cursor.getInt(1);
                model.addonsCount = cursor.getInt(2);
                model.optionalsCount = cursor.getInt(3);
            }

        }
        cursor.close();
    }

    public static Loader<List<SaleOrderItemViewModel>> createLoader(Context context, String orderGuid) {
        return createLoader(context, orderGuid, true);
    }

    public static Loader<List<SaleOrderItemViewModel>> createLoader(Context context, String orderGuid, boolean loadModifiersCount) {
        return CursorLoaderBuilder.forUri(URI_ORDER_ITEMS)
                .where(SaleItemTable.ORDER_GUID + " = ? ", orderGuid)
                .orderBy(ORDER_BY)
                .wrap(new SaleOrderItemViewModelWrapFunction(context, loadModifiersCount))
                .build(context);
    }

    public static Loader<List<HistoryDetailedOrderItemModel>> createHistoryLoader(Context context, String orderGuid) {
        return CursorLoaderBuilder.forUri(URI_ORDER_ITEMS)
                .where(SaleItemTable.ORDER_GUID + " = ? ", orderGuid)
                .orderBy(ORDER_BY)
                .wrap(new SaleOrderItemViewModelWrapFunction(context, false))
                .wrap(new Function<List<SaleOrderItemViewModel>, List<HistoryDetailedOrderItemModel>>() {
                    @Override
                    public List<HistoryDetailedOrderItemModel> apply(List<SaleOrderItemViewModel> in) {
                        ArrayList<HistoryDetailedOrderItemModel> result = new ArrayList<HistoryDetailedOrderItemModel>(in.size());
                        for (SaleOrderItemViewModel i : in) {
                            result.add(new HistoryDetailedOrderItemModel(i));
                        }
                        return result;
                    }
                })
                .build(context);
    }
}*/