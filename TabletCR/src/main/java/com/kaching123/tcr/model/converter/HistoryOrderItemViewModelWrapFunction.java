package com.kaching123.tcr.model.converter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.Loader;
import android.text.TextUtils;

import com.getbase.android.db.loaders.CursorLoaderBuilder;
import com.google.common.base.Function;
import com.kaching123.tcr.model.OrderType;
import com.kaching123.tcr.model.SaleOrderItemAddonModel;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel.AddonInfo;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.model.payment.HistoryDetailedOrderItemModel;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.BillPaymentDescriptionTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleAddonTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopStore.ModifierTable;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsView;
import com.kaching123.tcr.util.ContentValuesUtilBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;
import static com.kaching123.tcr.model.ContentValuesUtil._discountType;
import static com.kaching123.tcr.model.ContentValuesUtil._modifierType;
import static com.kaching123.tcr.model.ContentValuesUtil._orderType;
import static com.kaching123.tcr.model.ContentValuesUtil._priceType;

/**
 * Created by gdubina on 03.12.13.
 */
public class HistoryOrderItemViewModelWrapFunction implements Function<Cursor, List<SaleOrderItemViewModel>> {

    private static final Uri URI_ORDER_ITEMS = ShopProvider.getContentUri(SaleOrderItemsView.URI_CONTENT);
    private static final String ORDER_BY = SaleItemTable.SEQUENCE + ", " + SaleAddonTable.TYPE + ", " + SaleOrderItemsView2.ModifierTable.TITLE;

    private static final Uri URI_MODIFIERS_GROUP_BY = ShopProvider.getContentUriGroupBy(ModifierTable.URI_CONTENT, ModifierTable.ITEM_GUID);
    private Context context;

    public HistoryOrderItemViewModelWrapFunction(Context context) {
        this.context = context;
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
                    TaxGroupModel taxModel1 = new TaxGroupModel(c.getString(c.getColumnIndex(SaleOrderItemsView2.TaxGroupTable.GUID)),
                            c.getString(c.getColumnIndex(SaleOrderItemsView2.TaxGroupTable.TITLE)),
                            ContentValuesUtilBase._decimal(c, c.getColumnIndex(SaleOrderItemsView2.TaxGroupTable.TAX)));
                    TaxGroupModel taxModel2 = new TaxGroupModel(c.getString(c.getColumnIndex(SaleOrderItemsView2.TaxGroupTable2.GUID)),
                            c.getString(c.getColumnIndex(SaleOrderItemsView2.TaxGroupTable2.TITLE)),
                            ContentValuesUtilBase._decimal(c, c.getColumnIndex(SaleOrderItemsView2.TaxGroupTable2.TAX)));
                    item = new SaleOrderItemViewModel(
                            itemModel,
                            c.getString(descIndex),
                            null,
                            c.getString(c.getColumnIndex(ItemTable.EAN_CODE)),
                            c.getString(c.getColumnIndex(ItemTable.PRODUCT_CODE)),
                            c.getString(c.getColumnIndex(ItemTable.UNITS_LABEL)),
                            _bool(c, c.getColumnIndex(SaleOrderTable.TAXABLE)),
                            _bool(c, c.getColumnIndex(ItemTable.SERIALIZABLE)),
                            _decimal(c, c.getColumnIndex(SaleOrderTable.DISCOUNT)),
                            _discountType(c, c.getColumnIndex(SaleOrderTable.DISCOUNT_TYPE)),
                            _decimal(c, c.getColumnIndex(SaleOrderTable.TRANSACTION_FEE)),
                            !c.isNull(c.getColumnIndex(ItemTable.PRINTER_ALIAS_GUID)),
                            c.getInt(c.getColumnIndex(SaleItemTable.IS_PREPAID_ITEM)) == 0 ? false : true,
                            taxModel1, taxModel2
                    );
                    item.finalPrice = itemModel.finalGrossPrice.subtract(itemModel.finalDiscount).add(itemModel.finalTax);
                    if (item.isPrepaidItem) {
                        item.description = c.getString(c.getColumnIndex(ShopSchema2.SaleOrderItemsView2.BillPaymentDescriptionTable.DESCRIPTION));
                        item.productCode = c.getString(c.getColumnIndex(SaleOrderItemsView2.BillPaymentDescriptionTable.PREPAID_ORDER_ID));
                    }
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
                AddonInfo modifier = readModifier(c);
                if (modifier == null) {
                    continue;
                }
                /*if (modifier.addon.type == ModifierType.MODIFIER) {
                    item.setModifier(modifier);
                } else if (modifier.addon.type == ModifierType.ADDON || modifier.addon.type == ModifierType.OPTIONAL) {
                    item.addAddon(modifier);
                }*/
            } while (c.moveToNext());
        }

        return items;
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
                _decimal(c, c.getColumnIndex(SaleItemTable.TAX2)),
                c.getLong(c.getColumnIndex(SaleItemTable.SEQUENCE)),
                c.getString(c.getColumnIndex(SaleItemTable.PARENT_GUID)),
                _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_GROSS_PRICE)),
                _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_TAX)),
                _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_DISCOUNT)),
                _decimalQty(c, c.getColumnIndex(SaleItemTable.TMP_REFUND_QUANTITY)),
                c.getString(c.getColumnIndex(SaleItemTable.NOTES)),
                c.getInt(c.getColumnIndex(SaleItemTable.HAS_NOTES)) == 1,
                c.getInt(c.getColumnIndex(SaleItemTable.IS_PREPAID_ITEM)) == 1);
    }

    private AddonInfo readModifier(Cursor c) {
        String addonGuid = c.getString(c.getColumnIndex(SaleAddonTable.ADDON_GUID));
        String groupName = c.getString(c.getColumnIndex(SaleOrderItemsView2.ModifierGroupTable.TITLE));
        if (!TextUtils.isEmpty(addonGuid)) {
            return new AddonInfo(
                    new SaleOrderItemAddonModel(
                            c.getString(c.getColumnIndex(SaleAddonTable.GUID)),
                            addonGuid,
                            c.getString(c.getColumnIndex(SaleAddonTable.ITEM_GUID)),
                            _decimal(c, c.getColumnIndex(SaleAddonTable.EXTRA_COST)),
                            _modifierType(c, c.getColumnIndex(SaleAddonTable.TYPE)),
                            c.getString(c.getColumnIndex(SaleAddonTable.CHILD_ITEM_ID)),
                            ContentValuesUtilBase._decimalQty(c, c.getColumnIndex(SaleAddonTable.CHILD_ITEM_QTY))),

                    c.getString(c.getColumnIndex(SaleOrderItemsView2.ModifierTable.TITLE)),
                    groupName
            );
        }
        return null;
    }

    public static Loader<List<HistoryDetailedOrderItemModel>> createHistoryLoader(Context context, String orderGuid) {
        return CursorLoaderBuilder.forUri(URI_ORDER_ITEMS)
                .where(SaleItemTable.ORDER_GUID + " = ?", orderGuid)
                .orderBy(ORDER_BY)
                .wrap(new HistoryOrderItemViewModelWrapFunction(context))
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
}