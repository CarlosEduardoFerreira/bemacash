package com.kaching123.tcr.model.converter;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.getbase.android.db.provider.ProviderAction;
import com.getbase.android.db.provider.Query;
import com.google.common.base.Function;
import com.kaching123.tcr.function.OrderTotalPriceCalculator;
import com.kaching123.tcr.function.OrderTotalPriceCalculator.Handler;
import com.kaching123.tcr.function.UnitWrapFunction;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.SaleOrderItemAddonModel;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel;
import com.kaching123.tcr.model.SaleOrderItemViewModel.AddonInfo;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.ItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.ModifierGroupTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleAddonSubItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleAddonTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleItemTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.SaleOrderTable;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderItemsView2.UnitLabelTable;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.SaleOrderItemsView;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal2;
import static com.kaching123.tcr.model.ContentValuesUtil._modifierType;
import static com.kaching123.tcr.model.ContentValuesUtil._priceType;
import static com.kaching123.tcr.util.CalculationUtil.getSubTotal;
import static com.kaching123.tcr.util.ContentValuesUtilBase._bool;
import static com.kaching123.tcr.util.ContentValuesUtilBase._decimal;
import static com.kaching123.tcr.util.ContentValuesUtilBase._decimalQty;
import static com.kaching123.tcr.util.ContentValuesUtilBase._discountType;
import static com.kaching123.tcr.util.CursorUtil._wrap;

/**
 * Created by vkompaniets on 7/14/2015.
 */
public abstract class SaleItemWrapFunction implements Function<Cursor, List<SaleOrderItemViewModel>> {

    protected static final Uri URI_ORDER_ITEMS = ShopProvider.contentUri(SaleOrderItemsView.URI_CONTENT);
    protected static final Uri UNIT_URI = ShopProvider.contentUri(ShopStore.UnitTable.URI_CONTENT);
    protected static final String ORDER_BY = SaleItemTable.SEQUENCE + ", " + SaleAddonTable.TYPE + ", " + SaleOrderItemsView2.ModifierTable.TITLE;

    private Context context;

    public SaleItemWrapFunction(Context context) {
        this.context = context;
    }

    protected abstract boolean loadSerialItems();

    protected abstract boolean recalcSaleItems();

    @Override
    public List<SaleOrderItemViewModel> apply(Cursor c) {
        ArrayList<SaleOrderItemViewModel> items = new ArrayList<>();
        HashSet<String> itemsGuids = new HashSet<>();
        HashMap<String, List<SaleOrderItemViewModel>> itemsMap = new HashMap<>();

        final HashMap<String, SaleOrderItemViewModel> saleItemsMap = new HashMap<>();
        if (c.moveToFirst()) {
            do {
                String saleItemGuid = c.getString(c.getColumnIndex(SaleItemTable.SALE_ITEM_GUID));
                SaleOrderItemViewModel item = saleItemsMap.get(saleItemGuid);
                if (item == null) {
                    SaleOrderItemModel itemModel = readSaleItemModel(c);
                    int descIndex = c.getColumnIndex(ItemTable.DESCRIPTION);

                    TaxGroupModel taxModel1 = new TaxGroupModel(c.getString(c.getColumnIndex(SaleOrderItemsView2.TaxGroupTable.GUID)),
                            c.getString(c.getColumnIndex(SaleOrderItemsView2.TaxGroupTable.TITLE)),
                            _decimal(c, c.getColumnIndex(SaleOrderItemsView2.TaxGroupTable.TAX)));
                    TaxGroupModel taxModel2 = new TaxGroupModel(c.getString(c.getColumnIndex(SaleOrderItemsView2.TaxGroupTable2.GUID)),
                            c.getString(c.getColumnIndex(SaleOrderItemsView2.TaxGroupTable2.TITLE)),
                            _decimal(c, c.getColumnIndex(SaleOrderItemsView2.TaxGroupTable2.TAX)));
                    item = new SaleOrderItemViewModel(
                            itemModel,
                            c.getString(descIndex),
                            null,
                            c.getString(c.getColumnIndex(ItemTable.EAN_CODE)),
                            c.getString(c.getColumnIndex(ItemTable.PRODUCT_CODE)),
                            c.getString(c.getColumnIndex(UnitLabelTable.SHORTCUT)),
                            _bool(c, c.getColumnIndex(SaleOrderTable.TAXABLE)),
                            _bool(c, c.getColumnIndex(ItemTable.SERIALIZABLE)),
                            _decimal(c, c.getColumnIndex(SaleOrderTable.DISCOUNT)),
                            _discountType(c, c.getColumnIndex(SaleOrderTable.DISCOUNT_TYPE)),
                            _decimal(c, c.getColumnIndex(SaleOrderTable.TRANSACTION_FEE)),
                            !c.isNull(c.getColumnIndex(ItemTable.PRINTER_ALIAS_GUID)),
                            c.getInt(c.getColumnIndex(SaleItemTable.IS_PREPAID_ITEM)) == 1,
                            c.getInt(c.getColumnIndex(SaleItemTable.IS_GIFT_CARD)) == 1,
                            taxModel1, taxModel2);

                    item.finalPrice = itemModel.finalGrossPrice.subtract(itemModel.finalDiscount);
                    if (item.isPrepaidItem || item.isGiftCard) {
                        item.description = c.getString(c.getColumnIndex(ShopSchema2.SaleOrderItemsView2.BillPaymentDescriptionTable.DESCRIPTION));
                        item.productCode = c.getString(c.getColumnIndex(SaleOrderItemsView2.BillPaymentDescriptionTable.PREPAID_ORDER_ID));
                    }
                    items.add(item);
                    saleItemsMap.put(saleItemGuid, item);
                    if (itemModel.itemGuid != null) {
                        itemsGuids.add(itemModel.itemGuid);
                        List<SaleOrderItemViewModel> existsItems = itemsMap.get(itemModel.itemGuid);
                        if (existsItems == null) {
                            itemsMap.put(itemModel.itemGuid, existsItems = new ArrayList<>());
                        }
                        existsItems.add(item);
                    }
                }

                AddonInfo modifier = readModifier(c);
                if (modifier != null) {

                    if (item.modifiers == null)
                        item.modifiers = new ArrayList<>();

                    item.modifiers.add(modifier);
                }

                if (item.isSerializable && loadSerialItems()) {
                    List<Unit> units = _wrap(unitQuery(item.itemModel.orderGuid, item.itemModel.itemGuid, item.getSaleItemGuid(), item.hasModifiers())
                            .perform(context), new UnitWrapFunction());
                    item.tmpUnit.addAll(units);
                }


            } while (c.moveToNext());
        }


        if (recalcSaleItems()) {
            OrderTotalPriceCalculator.calculate(items, null, new Handler() {
                @Override
                public void handleItem(String saleItemGuid,
                                       String description,
                                       BigDecimal qty,
                                       BigDecimal itemPriceWithAddons,
                                       BigDecimal itemSubTotal,
                                       BigDecimal itemTotal,
                                       BigDecimal itemEbtTotal,
                                       BigDecimal itemFinalPrice,
                                       BigDecimal itemFinalDiscount,
                                       BigDecimal itemFinalTax) {
                    SaleOrderItemViewModel item = saleItemsMap.get(saleItemGuid);
                    if (item != null) {
                        item.finalPrice = itemFinalPrice;
                    }
                }

                @Override
                public void handleTotal(BigDecimal totalDiscount,
                                        BigDecimal subTotalItemTotal,
                                        BigDecimal totalTaxVatValue,
                                        BigDecimal totalOrderPrice,
                                        BigDecimal tipsValue) {

                }

            });
        }
        return items;
    }

    private static Query unitQuery(String orderId, String itemId, String saleItemGuid, boolean hasModifiers) {
        Query query = ProviderAction.query(UNIT_URI);
        if (orderId != null) {
            query = query.where(ShopStore.UnitTable.SALE_ORDER_ID + " = ?", orderId);
        }
        if (itemId != null) {
            query = query.where(ShopStore.UnitTable.ITEM_ID + " = ?", itemId);
        }
        if (saleItemGuid != null && hasModifiers) {
            query = query.where(ShopStore.UnitTable.SALE_ITEM_ID + " = ?", saleItemGuid);
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
                _decimal(c, c.getColumnIndex(SaleItemTable.TAX2)),
                c.getLong(c.getColumnIndex(SaleItemTable.SEQUENCE)),
                c.getString(c.getColumnIndex(SaleItemTable.PARENT_GUID)),
                _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_GROSS_PRICE)),
                _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_TAX)),
                _decimal(c, c.getColumnIndex(SaleItemTable.FINAL_DISCOUNT)),
                _decimalQty(c, c.getColumnIndex(SaleItemTable.TMP_REFUND_QUANTITY)),
                c.getString(c.getColumnIndex(SaleItemTable.NOTES)),
                c.getInt(c.getColumnIndex(SaleItemTable.HAS_NOTES)) == 1,
                c.getInt(c.getColumnIndex(SaleItemTable.IS_PREPAID_ITEM)) == 1,
                c.getInt(c.getColumnIndex(SaleItemTable.IS_GIFT_CARD)) == 1,
                _decimal(c, c.getColumnIndex(SaleItemTable.LOYALTY_POINTS)),
                _bool(c, c.getColumnIndex(SaleItemTable.POINTS_FOR_DOLLAR_AMOUNT)),
                c.getString(c.getColumnIndex(SaleItemTable.DISCOUNT_BUNDLE_ID)),
                _bool(c, c.getColumnIndex(SaleItemTable.EBT_ELIGIBLE)),
                _decimal2(c, c.getColumnIndex(SaleItemTable.TMP_EBT_PAYED), 6, BigDecimal.ZERO));
    }

    private AddonInfo readModifier(Cursor c) {
        if (c.getString(c.getColumnIndex(SaleAddonTable.GUID)) != null) {
            String childItemId = c.getString(c.getColumnIndex(SaleAddonTable.CHILD_ITEM_ID));
            String groupName = c.getString(c.getColumnIndex(SaleOrderItemsView2.ModifierGroupTable.TITLE));

            /*** SaleModifierModel ***/
            BigDecimal cost;
            if (_modifierType(c, c.getColumnIndex(SaleAddonTable.TYPE)) == ModifierType.OPTIONAL) {
                cost = BigDecimal.ZERO;
            } else if (childItemId != null) {
                cost = getSubTotal(_decimalQty(c, c.getColumnIndex(SaleAddonTable.CHILD_ITEM_QTY)), _decimal(c, c.getColumnIndex(SaleAddonSubItemTable.SALE_PRICE)));
            } else {
                cost = _decimal(c, c.getColumnIndex(SaleAddonTable.EXTRA_COST));
            }

            SaleOrderItemAddonModel saleModifierModel = new SaleOrderItemAddonModel(
                    c.getString(c.getColumnIndex(SaleAddonTable.GUID)),
                    c.getString(c.getColumnIndex(SaleAddonTable.ADDON_GUID)),
                    c.getString(c.getColumnIndex(SaleAddonTable.ITEM_GUID)),
                    cost,
                    _modifierType(c, c.getColumnIndex(SaleAddonTable.TYPE)),
                    childItemId,
                    _decimal(c, c.getColumnIndex(SaleAddonTable.CHILD_ITEM_QTY))
            );

            /*** title ***/
            String title;
            if (childItemId != null) {
                String modifierTitle = c.getString(c.getColumnIndex(SaleOrderItemsView2.ModifierTable.TITLE));
                String itemTitle = c.getString(c.getColumnIndex(SaleAddonSubItemTable.DESCRIPTION));
                title = TextUtils.isEmpty(modifierTitle) ? String.format("[%s]", itemTitle) : modifierTitle;
            } else {
                title = c.getString(c.getColumnIndex(SaleOrderItemsView2.ModifierTable.TITLE));
            }

            int groupOrderNum = c.getInt(c.getColumnIndex(ModifierGroupTable.ORDER_NUM));
            int orderNum = c.getInt(c.getColumnIndex(SaleOrderItemsView2.ModifierTable.ORDER_NUM));

            /*** result ***/
            return new AddonInfo(
                    saleModifierModel,
                    title,
                    groupName,
                    groupOrderNum,
                    orderNum);
        }
        return null;
    }
}
