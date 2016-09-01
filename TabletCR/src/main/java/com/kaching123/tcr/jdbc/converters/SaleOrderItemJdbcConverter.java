package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.SaleOrderItemModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.math.BigDecimal;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

public class SaleOrderItemJdbcConverter extends JdbcConverter<SaleOrderItemModel> {

    private static final String SALE_ORDER_ITEMS_TABLE_NAME = "SALE_ORDER_ITEM";

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
    private static final String EBT_ELIGIBLE = "EBT_ELIGIBLE";

  /*  @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new SaleOrderItemModel(
                rs.getString(SALE_ITEM_ID),
                rs.getString(ORDER_ID),
                rs.getString(ITEM_ID),
                rs.getBigDecimal(QUANTITY),
                rs.getBigDecimal(KITCHEN_PRINTED_QUANTITY),
                _enum(PriceType.class, rs.getString(PRICE_TYPE), PriceType.FIXED),
                rs.getBigDecimal(PRICE),
                rs.getBoolean(DISCOUNTABLE),
                rs.getBigDecimal(DISCOUNT),
                _enum(DiscountType.class, rs.getString(DISCOUNT_TYPE), DiscountType.PERCENT),
                rs.getBoolean(TAXABLE),
                rs.getBigDecimal(TAX),
                rs.getBigDecimal(TAX2),
                rs.getLong(SEQUENCE),
                rs.getString(PARENT_ID),
                rs.getBigDecimal(FINAL_GROSS_PRICE),
                rs.getBigDecimal(FINAL_TAX),
                rs.getBigDecimal(FINAL_DISCOUNT),
                null,
                rs.getString(NOTES),
                rs.getBoolean(HAS_NOTES),
                rs.getBoolean(IS_PREPAID_ITEM),
                null,
                false,
                rs.getBoolean(EBT_ELIGIBLE)).toValues()
        );
    }*/

    @Override
    public SaleOrderItemModel toValues(JdbcJSONObject rs) throws JSONException {
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
                rs.getLong(SEQUENCE),
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
                rs.getBoolean(EBT_ELIGIBLE));
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
    public String getParentGuidColumn() {
        return PARENT_ID;
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
                .add(EBT_ELIGIBLE, model.isEbtEligible)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(SaleOrderItemModel model, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_ITEMS_TABLE_NAME, appCommandContext)
                .add(QUANTITY, model.qty, ContentValuesUtil.QUANTITY_SCALE)
                .add(SEQUENCE, model.sequence)

                .add(FINAL_GROSS_PRICE, model.finalGrossPrice)
                .add(FINAL_TAX, model.finalTax)
                .add(FINAL_DISCOUNT, model.finalDiscount)

                .where(SALE_ITEM_ID, model.saleItemGuid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand updateQty(SaleOrderItemModel model, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_ITEMS_TABLE_NAME, appCommandContext)
                .add(QUANTITY, model.qty, ContentValuesUtil.QUANTITY_SCALE)

                .add(FINAL_GROSS_PRICE, model.finalGrossPrice)
                .add(FINAL_TAX, model.finalTax)
                .add(FINAL_DISCOUNT, model.finalDiscount)

                .where(SALE_ITEM_ID, model.saleItemGuid)
                .build(JdbcFactory.getApiMethod(model));

    }

    public SingleSqlCommand updateQty(String saleItemGuid, BigDecimal qty, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_ITEMS_TABLE_NAME, appCommandContext)
                .add(QUANTITY, qty, ContentValuesUtil.QUANTITY_SCALE)
                .where(SALE_ITEM_ID, saleItemGuid)
                .build(JdbcFactory.getApiMethod(SaleOrderItemModel.class));

    }

    public SingleSqlCommand updatePrice(SaleOrderItemModel model, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_ITEMS_TABLE_NAME, appCommandContext)
                .add(PRICE, model.price)

                .add(FINAL_GROSS_PRICE, model.finalGrossPrice)
                .add(FINAL_TAX, model.finalTax)
                .add(FINAL_DISCOUNT, model.finalDiscount)

                .where(SALE_ITEM_ID, model.saleItemGuid)
                .build(JdbcFactory.getApiMethod(model));

    }

    public SingleSqlCommand updateDiscount(SaleOrderItemModel model, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_ITEMS_TABLE_NAME, appCommandContext)
                .add(DISCOUNT, model.discount)
                .add(DISCOUNT_TYPE, model.discountType)

                .add(FINAL_GROSS_PRICE, model.finalGrossPrice)
                .add(FINAL_TAX, model.finalTax)
                .add(FINAL_DISCOUNT, model.finalDiscount)

                .where(SALE_ITEM_ID, model.saleItemGuid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand updateFinalPrices(SaleOrderItemModel model, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_ITEMS_TABLE_NAME, appCommandContext)

                .add(FINAL_GROSS_PRICE, model.finalGrossPrice)
                .add(FINAL_TAX, model.finalTax)
                .add(FINAL_DISCOUNT, model.finalDiscount)

                .where(SALE_ITEM_ID, model.saleItemGuid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand updateNotes(SaleOrderItemModel model, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_ITEMS_TABLE_NAME, appCommandContext)
                .add(NOTES, model.notes)
                .where(SALE_ITEM_ID, model.saleItemGuid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand updateKitchenPrintedQty(SaleOrderItemModel model, IAppCommandContext appCommandContext) {
        return _update(SALE_ORDER_ITEMS_TABLE_NAME, appCommandContext)
                .add(KITCHEN_PRINTED_QUANTITY, model.kitchenPrintedQty, ContentValuesUtil.QUANTITY_SCALE)
                .where(SALE_ITEM_ID, model.saleItemGuid)
                .build(JdbcFactory.getApiMethod(model));
    }

}
