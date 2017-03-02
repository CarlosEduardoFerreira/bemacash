package com.kaching123.tcr.jdbc.converters;

import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

public class SaleOrderItemJdbcConverter extends JdbcConverter<SaleOrderItemModel> {

    public static final String SALE_ORDER_ITEMS_TABLE_NAME = "SALE_ORDER_ITEM";

    private static final String SALE_ITEM_ID = "SALE_ITEM_ID";
    private static final String ORDER_ID = "ORDER_ID";
    private static final String ITEM_ID = "ITEM_ID";
    private static final String QUANTITY = "QUANTITY";
    private static final String KITCHEN_PRINTED_QUANTITY = "KITCHEN_PRINTED_QUANTITY";
    private static final String PRICE = "PRICE";
    private static final String PRICE_TYPE = "PRICE_TYPE";
    private static final String DISCOUNTABLE = "DISCOUNTABLE";
    private static final String DISCOUNT = "DISCOUNT";
    private static final String DISCOUNT_TYPE = "DISCOUNT_TYPE";
    private static final String TAXABLE = "TAXABLE";
    private static final String SEQUENCE = "SEQUENCE";
    private static final String TAX = "TAX";
    private static final String TAX2 = "TAX2";
    private static final String PARENT_ID = "PARENT_ID";

    private static final String FINAL_GROSS_PRICE = "FINAL_GROSS_PRICE";
    private static final String FINAL_TAX = "FINAL_TAX";
    private static final String FINAL_DISCOUNT = "FINAL_DISCOUNT";
    private static final String NOTES = "NOTES";
    private static final String HAS_NOTES = "HAS_NOTES";
    private static final String IS_PREPAID_ITEM = "IS_PREPAID_ITEM";
    private static final String IS_GIFT_CARD = "IS_GIFT_CARD";
    private static final String LOYALTY_POINTS = "LOYALTY_POINTS";
    private static final String POINTS_FOR_DOLLAR_AMOUNT = "POINTS_FOR_DOLLAR_AMOUNT";
    private static final String DISCOUNT_BUNDLE_ID = "MULTIPLE_DISCOUNT_ID";
    private static final String EBT_ELIGIBLE = "EBT_ELIGIBLE";

    @Override
    public SaleOrderItemModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();

        if (!rs.has(SALE_ITEM_ID)) ignoreFields.add(ShopStore.SaleItemTable.SALE_ITEM_GUID);
        if (!rs.has(ORDER_ID)) ignoreFields.add(ShopStore.SaleItemTable.ORDER_GUID);
        if (!rs.has(ITEM_ID)) ignoreFields.add(ShopStore.SaleItemTable.ITEM_GUID);
        if (!rs.has(QUANTITY)) ignoreFields.add(ShopStore.SaleItemTable.QUANTITY);
        if (!rs.has(KITCHEN_PRINTED_QUANTITY)) ignoreFields.add(ShopStore.SaleItemTable.KITCHEN_PRINTED_QTY);
        if (!rs.has(PRICE_TYPE)) ignoreFields.add(ShopStore.SaleItemTable.PRICE_TYPE);
        if (!rs.has(PRICE)) ignoreFields.add(ShopStore.SaleItemTable.PRICE);
        if (!rs.has(DISCOUNTABLE)) ignoreFields.add(ShopStore.SaleItemTable.DISCOUNTABLE);
        if (!rs.has(DISCOUNT)) ignoreFields.add(ShopStore.SaleItemTable.DISCOUNT);
        if (!rs.has(DISCOUNT_TYPE)) ignoreFields.add(ShopStore.SaleItemTable.DISCOUNT_TYPE);
        if (!rs.has(TAXABLE)) ignoreFields.add(ShopStore.SaleItemTable.TAXABLE);
        if (!rs.has(TAX)) ignoreFields.add(ShopStore.SaleItemTable.TAX);
        if (!rs.has(TAX2)) ignoreFields.add(ShopStore.SaleItemTable.TAX2);
        if (!rs.has(SEQUENCE)) ignoreFields.add(ShopStore.SaleItemTable.SEQUENCE);
        if (!rs.has(PARENT_ID)) ignoreFields.add(ShopStore.SaleItemTable.PARENT_GUID);
        if (!rs.has(FINAL_GROSS_PRICE)) ignoreFields.add(ShopStore.SaleItemTable.FINAL_GROSS_PRICE);
        if (!rs.has(FINAL_TAX)) ignoreFields.add(ShopStore.SaleItemTable.FINAL_TAX);
        if (!rs.has(FINAL_DISCOUNT)) ignoreFields.add(ShopStore.SaleItemTable.FINAL_DISCOUNT);
        if (!rs.has(NOTES)) ignoreFields.add(ShopStore.SaleItemTable.NOTES);
        if (!rs.has(HAS_NOTES)) ignoreFields.add(ShopStore.SaleItemTable.HAS_NOTES);
        if (!rs.has(IS_PREPAID_ITEM)) ignoreFields.add(ShopStore.SaleItemTable.IS_PREPAID_ITEM);
        if (!rs.has(IS_GIFT_CARD)) ignoreFields.add(ShopStore.SaleItemTable.IS_GIFT_CARD);
        if (!rs.has(LOYALTY_POINTS)) ignoreFields.add(ShopStore.SaleItemTable.LOYALTY_POINTS);
        if (!rs.has(POINTS_FOR_DOLLAR_AMOUNT)) ignoreFields.add(ShopStore.SaleItemTable.POINTS_FOR_DOLLAR_AMOUNT);
        if (!rs.has(DISCOUNT_BUNDLE_ID)) ignoreFields.add(ShopStore.SaleItemTable.DISCOUNT_BUNDLE_ID);
        if (!rs.has(EBT_ELIGIBLE)) ignoreFields.add(ShopStore.SaleItemTable.EBT_ELIGIBLE);
        return new SaleOrderItemModel(
                rs.getString(SALE_ITEM_ID),
                rs.getString(ORDER_ID),
                rs.getString(ITEM_ID),
                rs.getBigDecimal(QUANTITY, ContentValuesUtil.QUANTITY_SCALE),
                rs.getBigDecimal(KITCHEN_PRINTED_QUANTITY, ContentValuesUtil.QUANTITY_SCALE),
                _enum(PriceType.class, rs.getString(PRICE_TYPE), PriceType.FIXED),
                rs.getBigDecimal(PRICE),
                rs.getBoolean(DISCOUNTABLE),
                rs.getBigDecimal(DISCOUNT),
                _enum(DiscountType.class, rs.getString(DISCOUNT_TYPE), DiscountType.PERCENT),
                rs.getBoolean(TAXABLE),
                rs.getBigDecimal(TAX),
                rs.getBigDecimal(TAX2),
                rs.isNull(SEQUENCE) || rs.get(SEQUENCE).equals("null") ? 0L : rs.getLong(SEQUENCE),
                rs.getString(PARENT_ID),
                rs.getBigDecimal(FINAL_GROSS_PRICE),
                rs.getBigDecimal(FINAL_TAX),
                rs.getBigDecimal(FINAL_DISCOUNT),
                null,
                rs.getString(NOTES),
                rs.getBoolean(HAS_NOTES),
                rs.getBoolean(IS_PREPAID_ITEM),
                rs.getBoolean(IS_GIFT_CARD),
                rs.getBigDecimal(LOYALTY_POINTS),
                rs.getBoolean(POINTS_FOR_DOLLAR_AMOUNT),
                rs.getString(DISCOUNT_BUNDLE_ID),
                rs.getBoolean(EBT_ELIGIBLE),
                null, ignoreFields);
    }

    @Override
    public String getTableName() {
        return SALE_ORDER_ITEMS_TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return SALE_ITEM_ID;
    }

    @Override
    public String getLocalGuidColumn() {
        return ShopStore.SaleItemTable.SALE_ITEM_GUID;
    }

    @Override
    public String getParentGuidColumn() {
        return PARENT_ID;
    }

    @Override
    public JSONObject getJSONObject(SaleOrderItemModel model){
        JSONObject json = null;
        try {
            json = new JSONObject()
                    .put(SALE_ITEM_ID, model.saleItemGuid)
                    .put(ORDER_ID, model.orderGuid)
                    .put(ITEM_ID, model.itemGuid)
                    .put(QUANTITY, model.qty)
                    .put(KITCHEN_PRINTED_QUANTITY, model.kitchenPrintedQty)
                    .put(PRICE, model.price)
                    .put(PRICE_TYPE, model.priceType)
                    .put(DISCOUNTABLE, model.discountable)
                    .put(DISCOUNT, model.discount)
                    .put(DISCOUNT_TYPE, model.discountType)
                    .put(TAXABLE, model.isTaxable)
                    .put(TAX, model.tax)
                    .put(TAX2, model.tax2)
                    .put(SEQUENCE, model.sequence)
                    .put(PARENT_ID, model.parentGuid)
                    .put(FINAL_GROSS_PRICE, model.finalGrossPrice)
                    .put(FINAL_TAX, model.finalTax)
                    .put(FINAL_DISCOUNT, model.finalDiscount)
                    .put(NOTES, model.notes)
                    .put(HAS_NOTES, model.hasNotes)
                    .put(IS_PREPAID_ITEM, model.isPrepaidItem)
                    .put(IS_GIFT_CARD, model.isGiftCard)
                    .put(LOYALTY_POINTS, model.loyaltyPoints)
                    .put(POINTS_FOR_DOLLAR_AMOUNT, model.pointsForDollarAmount)
                    .put(DISCOUNT_BUNDLE_ID, model.discountBundleId)
                    .put(EBT_ELIGIBLE, model.isEbtEligible);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(SaleOrderItemModel model, IAppCommandContext appCommandContext) {
        return _insert(SALE_ORDER_ITEMS_TABLE_NAME, appCommandContext)
                .add(SALE_ITEM_ID, model.saleItemGuid)
                .add(ORDER_ID, model.orderGuid)
                .add(ITEM_ID, model.itemGuid)
                .add(QUANTITY, model.qty, ContentValuesUtil.QUANTITY_SCALE)
                .add(KITCHEN_PRINTED_QUANTITY, model.kitchenPrintedQty == null ? BigDecimal.ZERO : model.kitchenPrintedQty, ContentValuesUtil.QUANTITY_SCALE)
                .add(PRICE, model.price)
                .add(PRICE_TYPE, model.priceType)
                .add(DISCOUNTABLE, model.discountable)
                .add(DISCOUNT, model.discount)
                .add(DISCOUNT_TYPE, model.discountType)
                .add(TAXABLE, model.isTaxable)
                .add(TAX, model.tax)
                .add(TAX2, model.tax2)
                .add(SEQUENCE, model.sequence)
                .add(PARENT_ID, model.parentGuid)
                .add(FINAL_GROSS_PRICE, model.finalGrossPrice)
                .add(FINAL_TAX, model.finalTax)
                .add(FINAL_DISCOUNT, model.finalDiscount)
                .add(NOTES, model.notes)
                .add(HAS_NOTES, model.hasNotes)
                .add(IS_PREPAID_ITEM, model.isPrepaidItem)
                .add(IS_GIFT_CARD, model.isGiftCard)
                .add(LOYALTY_POINTS, model.loyaltyPoints)
                .add(POINTS_FOR_DOLLAR_AMOUNT, model.pointsForDollarAmount)
                .add(DISCOUNT_BUNDLE_ID, model.discountBundleId)
                .add(EBT_ELIGIBLE, model.isEbtEligible)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(SaleOrderItemModel model, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_ITEMS_TABLE_NAME)
                .add(DISCOUNT, model.discount)
                .add(DISCOUNT_TYPE, model.discountType)
                .add(PRICE, model.price)
                .add(NOTES, model.notes)
                .add(KITCHEN_PRINTED_QUANTITY, model.kitchenPrintedQty)
                .add(QUANTITY, model.qty, ContentValuesUtil.QUANTITY_SCALE)
                .add(SEQUENCE, model.sequence)
                .add(FINAL_GROSS_PRICE, model.finalGrossPrice)
                .add(FINAL_TAX, model.finalTax)
                .add(FINAL_DISCOUNT, model.finalDiscount)
                .where(SALE_ITEM_ID, model.saleItemGuid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand updateQty(String saleItemGuid, BigDecimal qty) {
        SaleOrderItemModel model = SaleOrderItemModel.getByGuid(TcrApplication.get(), saleItemGuid);
        if (model != null) {
            model.qty = qty;
            return updateSQL(model, null);
        }
        return _update(SALE_ORDER_ITEMS_TABLE_NAME)
                .add(QUANTITY, qty, ContentValuesUtil.QUANTITY_SCALE)
                .where(SALE_ITEM_ID, saleItemGuid)
                .build(JdbcFactory.getApiMethod(SaleOrderItemModel.class));

    }

    public SingleSqlCommand updatePrice(String guid, BigDecimal price) {
        SaleOrderItemModel model = SaleOrderItemModel.getByGuid(TcrApplication.get(), guid);
        if (model != null) {
            model.price = price;
            return updateSQL(model, null);
        }
        return _update(SALE_ORDER_ITEMS_TABLE_NAME)
                .add(PRICE, price)
                .where(SALE_ITEM_ID, guid)
                .build(JdbcFactory.getApiMethod(SaleOrderItemModel.class));
    }

    public SingleSqlCommand updateDiscount(String guid, BigDecimal discount, DiscountType discountType) {
        SaleOrderItemModel model = SaleOrderItemModel.getByGuid(TcrApplication.get(), guid);
        if (model != null) {
            model.discount = discount;
            model.discountType = discountType;
            return updateSQL(model, null);
        }
        return _update(SALE_ORDER_ITEMS_TABLE_NAME)
                .add(DISCOUNT, discount)
                .add(DISCOUNT_TYPE, discountType)
                .where(SALE_ITEM_ID, guid)
                .build(JdbcFactory.getApiMethod(SaleOrderItemModel.class));
    }

    public SingleSqlCommand updateFinalPrices(SaleOrderItemModel model) {
        SaleOrderItemModel saleOrderItemModel = SaleOrderItemModel.getByGuid(TcrApplication.get(), model.saleItemGuid);
        if (saleOrderItemModel != null) {
            saleOrderItemModel.finalGrossPrice = model.finalGrossPrice;
            saleOrderItemModel.finalTax = model.finalTax;
            saleOrderItemModel.finalDiscount = model.finalDiscount;

            return updateSQL(saleOrderItemModel, null);
        }

        return _update(SALE_ORDER_ITEMS_TABLE_NAME)
                .add(FINAL_GROSS_PRICE, model.finalGrossPrice)
                .add(FINAL_DISCOUNT, model.finalDiscount)
                .add(FINAL_TAX, model.finalTax)
                .where(SALE_ITEM_ID, model.getGuid())
                .build(JdbcFactory.getApiMethod(SaleOrderItemModel.class));
    }

    public SingleSqlCommand updateNotes(String guid, String notes) {
        SaleOrderItemModel model = SaleOrderItemModel.getByGuid(TcrApplication.get(), guid);
        if (model != null) {
            model.notes = notes;
            return updateSQL(model, null);
        }
        return _update(SALE_ORDER_ITEMS_TABLE_NAME)
                .add(NOTES, notes)
                .where(SALE_ITEM_ID, guid)
                .build(JdbcFactory.getApiMethod(SaleOrderItemModel.class));
    }

    public SingleSqlCommand updateKitchenPrintedQty(String guid, BigDecimal qty) {
        SaleOrderItemModel model = SaleOrderItemModel.getByGuid(TcrApplication.get(), guid);
        if (model != null) {
            model.kitchenPrintedQty = qty;
            return updateSQL(model, null);
        }
        return _update(SALE_ORDER_ITEMS_TABLE_NAME)
                .add(KITCHEN_PRINTED_QUANTITY, qty)
                .where(SALE_ITEM_ID, guid)
                .build(JdbcFactory.getApiMethod(SaleOrderItemModel.class));

    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }

}
