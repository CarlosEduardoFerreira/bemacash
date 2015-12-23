package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.Unit.CodeType;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

public class ItemsJdbcConverter extends JdbcConverter<ItemModel> {

    private static final String ITEM_TABLE_NAME = "ITEM";

    private static final String ID = "ID";
    private static final String CATEGORY_ID = "CATEGORY_ID";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String CODE = "CODE";
    private static final String EAN_CODE = "EAN_CODE";
    private static final String PRODUCT_CODE = "PRODUCT_CODE";
    private static final String PRICE_TYPE = "PRICE_TYPE";
    private static final String SALE_PRICE = "SALE_PRICE";
    private static final String UNITS_LABEL = "UNITS_LABEL";
    private static final String UNITS_LABEL_ID = "UNIT_LABEL_ID";
    private static final String STOCK_TRACKING = "STOCK_TRACKING";
    private static final String ACTIVE_STATUS = "ACTIVE_STATUS";
    private static final String DISCOUNTABLE = "DISCOUNTABLE";
    private static final String SALABLE = "SALABLE";
    private static final String DISCOUNT = "DISCOUNT";
    private static final String DISCOUNT_TYPE = "DISCOUNT_TYPE";
    private static final String TAXABLE = "TAXABLE";
    private static final String COST = "COST";
    private static final String MINIMUM_QTY = "MINIMUM_QTY";
    private static final String RECOMMENDED_QTY = "RECOMMENDED_QTY";

    private static final String UPDATE_QTY_FLAG = "UPDATE_QTY_FLAG";
    private static final String TAX_GROUP_ID = "TAX_GROUP_ID";
    private static final String CODE_TYPE = "CODE_TYPE";
    private static final String SERIALIZABLE = "SERIALIZABLE";
    private static final String ORDER_NUM = "ORDER_NUM";
    private static final String DEFAULT_MODIFIER_ID = "DEFAULT_MODIFIER_ID";
    private static final String PRINTER_ID = "PRINTER_ID";
    private static final String BUTTON_VIEW = "BUTTON_VIEW";
    private static final String HAS_NOTES = "HAS_NOTES";
    private static final String ELIGIBLE_FOR_COMMISSION = "ELIGIBLE_FOR_COMMISSION";
    private static final String COMMISSION = "COMMISSION";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        ItemModel model = new ItemModel(
                rs.getString(ID),
                rs.getString(CATEGORY_ID),
                rs.getString(DESCRIPTION),
                rs.getString(CODE),
                rs.getString(EAN_CODE),
                rs.getString(PRODUCT_CODE),
                _enum(PriceType.class, rs.getString(PRICE_TYPE), PriceType.OPEN),
                rs.getBigDecimal(SALE_PRICE),
                BigDecimal.ZERO,
                rs.getString(UNITS_LABEL),
                rs.getString(UNITS_LABEL_ID),
                rs.getBoolean(STOCK_TRACKING),
                rs.getBoolean(ACTIVE_STATUS),
                rs.getBoolean(DISCOUNTABLE),
                rs.getBoolean(SALABLE),
                rs.getBigDecimal(DISCOUNT),
                _enum(DiscountType.class, rs.getString(DISCOUNT_TYPE), DiscountType.PERCENT),
                rs.getBoolean(TAXABLE),
                rs.getBigDecimal(COST),
                rs.getBigDecimal(MINIMUM_QTY),
                rs.getBigDecimal(RECOMMENDED_QTY),
                rs.getString(UPDATE_QTY_FLAG),
                rs.getString(TAX_GROUP_ID),
                rs.getString(DEFAULT_MODIFIER_ID),
                rs.getInt(ORDER_NUM),
                rs.getString(PRINTER_ID),
                rs.getInt(BUTTON_VIEW),
                rs.getBoolean(HAS_NOTES),
                rs.getBoolean(SERIALIZABLE),
                _enum(CodeType.class, rs.getString(CODE_TYPE), CodeType.SN),
                rs.getBoolean(ELIGIBLE_FOR_COMMISSION),
                rs.getBigDecimal(COMMISSION)
        );
        return model.toValues();
    }

    @Override
    public ItemModel toValues(JdbcJSONObject rs) throws JSONException {
        return new ItemModel(
                rs.getString(ID),
                rs.getString(CATEGORY_ID),
                rs.getString(DESCRIPTION),
                rs.getString(CODE),
                rs.getString(EAN_CODE),
                rs.getString(PRODUCT_CODE),
                _enum(PriceType.class, rs.getString(PRICE_TYPE), PriceType.OPEN),
                rs.getBigDecimal(SALE_PRICE),
                BigDecimal.ZERO,
                rs.getString(UNITS_LABEL),
                rs.getString(UNITS_LABEL_ID),
                rs.getBoolean(STOCK_TRACKING),
                rs.getBoolean(ACTIVE_STATUS),
                rs.getBoolean(DISCOUNTABLE),
                rs.getBoolean(SALABLE),
                rs.getBigDecimal(DISCOUNT),
                _enum(DiscountType.class, rs.getString(DISCOUNT_TYPE), DiscountType.PERCENT),
                rs.getBoolean(TAXABLE),
                rs.getBigDecimal(COST),
                rs.getBigDecimal(MINIMUM_QTY, ContentValuesUtil.QUANTITY_SCALE),
                rs.getBigDecimal(RECOMMENDED_QTY, ContentValuesUtil.QUANTITY_SCALE),
                rs.getString(UPDATE_QTY_FLAG),
                rs.getString(TAX_GROUP_ID),
                rs.getString(DEFAULT_MODIFIER_ID),
                rs.getInt(ORDER_NUM),
                rs.getString(PRINTER_ID),
                rs.getInt(BUTTON_VIEW),
                rs.getBoolean(HAS_NOTES),
                rs.getBoolean(SERIALIZABLE),
                _enum(CodeType.class, rs.getString(CODE_TYPE), null),
                rs.getBoolean(ELIGIBLE_FOR_COMMISSION),
                rs.getBigDecimal(COMMISSION)
        );
    }

    @Override
    public String getTableName() {
        return ITEM_TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return ID;
    }

    @Override
    public SingleSqlCommand insertSQL(ItemModel item, IAppCommandContext appCommandContext) {
        return _insert(ITEM_TABLE_NAME, appCommandContext)
                .add(ID, item.guid)
                .add(CATEGORY_ID, item.categoryId)
                .add(DESCRIPTION, item.description)
                .add(CODE, item.code)
                .add(EAN_CODE, item.eanCode)
                .add(PRODUCT_CODE, item.productCode)
                .add(PRICE_TYPE, item.priceType)
                .add(SALE_PRICE, item.price)
                .add(UNITS_LABEL, item.unitsLabel)
                .add(UNITS_LABEL_ID, item.unitsLabelId)
                .add(STOCK_TRACKING, item.isStockTracking)
                .add(ACTIVE_STATUS, item.isActiveStatus)
                .add(DISCOUNTABLE, item.isDiscountable)
                .add(SALABLE, item.isSalable)
                .add(DISCOUNT, item.discount)
                .add(DISCOUNT_TYPE, item.discountType)
                .add(TAXABLE, item.isTaxable)
                .add(COST, item.cost)
                .add(MINIMUM_QTY, item.minimumQty, ContentValuesUtil.QUANTITY_SCALE)
                .add(RECOMMENDED_QTY, item.recommendedQty, ContentValuesUtil.QUANTITY_SCALE)
                .add(UPDATE_QTY_FLAG, item.updateQtyFlag)
                .add(TAX_GROUP_ID, item.taxGroupGuid)
                .add(DEFAULT_MODIFIER_ID, item.defaultModifierGuid)
                .add(ORDER_NUM, item.orderNum)
                .add(PRINTER_ID, item.printerAliasGuid)
                .add(HAS_NOTES, item.hasNotes)
                .add(SERIALIZABLE, item.serializable)
                .add(CODE_TYPE, item.codeType)
                .add(ELIGIBLE_FOR_COMMISSION, item.commissionEligible)
                .add(COMMISSION, item.commission)
                .build(JdbcFactory.getApiMethod(item));
    }

    @Override
    public SingleSqlCommand updateSQL(ItemModel item, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(CATEGORY_ID, item.categoryId)
                .add(DESCRIPTION, item.description)
                .add(CODE, item.code)
                .add(EAN_CODE, item.eanCode)
                .add(PRODUCT_CODE, item.productCode)
                .add(PRICE_TYPE, item.priceType)
                .add(SALE_PRICE, item.price)
                .add(UNITS_LABEL, item.unitsLabel)
                .add(UNITS_LABEL_ID, item.unitsLabelId)
                .add(STOCK_TRACKING, item.isStockTracking)
                .add(ACTIVE_STATUS, item.isActiveStatus)
                .add(DISCOUNTABLE, item.isDiscountable)
                .add(SALABLE, item.isSalable)
                .add(DISCOUNT, item.discount)
                .add(DISCOUNT_TYPE, item.discountType)
                .add(TAXABLE, item.isTaxable)
                .add(COST, item.cost)
                .add(MINIMUM_QTY, item.minimumQty, ContentValuesUtil.QUANTITY_SCALE)
                .add(RECOMMENDED_QTY, item.recommendedQty, ContentValuesUtil.QUANTITY_SCALE)
                .add(UPDATE_QTY_FLAG, item.updateQtyFlag)
                .add(TAX_GROUP_ID, item.taxGroupGuid)
                .add(DEFAULT_MODIFIER_ID, item.defaultModifierGuid)
                .add(ORDER_NUM, item.orderNum)
                .add(PRINTER_ID, item.printerAliasGuid)
                .add(BUTTON_VIEW, item.btnView)
                .add(HAS_NOTES, item.hasNotes)
                .add(SERIALIZABLE, item.serializable)
                .add(CODE_TYPE, item.codeType)
                .add(ELIGIBLE_FOR_COMMISSION, item.commissionEligible)
                .add(COMMISSION, item.commission)
                .where(ID, item.guid)
                .build(JdbcFactory.getApiMethod(item));
    }

    public SingleSqlCommand removeTaxGroup(String taxGroupGuid, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(TAX_GROUP_ID, (String) null)
                .where(TAX_GROUP_ID, taxGroupGuid)
                .build(JdbcFactory.getApiMethod(ItemModel.class));
    }

    public SingleSqlCommand updateDefaultModifierGuid(String itemGuid, String modifierGuid, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(DEFAULT_MODIFIER_ID, modifierGuid)
                .where(ID, itemGuid)
                .build(JdbcFactory.getApiMethod(ItemModel.class));
    }

    public SingleSqlCommand updateOrderSQL(String guid, int orderNum, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(ORDER_NUM, orderNum)
                .where(ID, guid)
                .build(JdbcFactory.getApiMethod(ItemModel.class));
    }

    public SingleSqlCommand updatePriceSQL(String guid, BigDecimal price, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(SALE_PRICE, price)
                .where(ID, guid)
                .build(JdbcFactory.getApiMethod(ItemModel.class));
    }

    public SingleSqlCommand updatePriceSQL(String guid, BigDecimal price, PriceType type, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(SALE_PRICE, price)
                .add(PRICE_TYPE, type)
                .where(ID, guid)
                .build(JdbcFactory.getApiMethod(ItemModel.class));
    }

    public SingleSqlCommand updateQtyFlagSQL(String guid, String updateFlag, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(UPDATE_QTY_FLAG, updateFlag)
                .where(ID, guid)
                .build(JdbcFactory.getApiMethod(ItemModel.class));
    }

    public SingleSqlCommand updateItemInfoSQL(String guid, BigDecimal price, BigDecimal cost, BigDecimal minimumQty, BigDecimal recommendedQty, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(SALE_PRICE, price)
                .add(COST, cost)
                .add(MINIMUM_QTY, minimumQty, ContentValuesUtil.QUANTITY_SCALE)
                .add(RECOMMENDED_QTY, recommendedQty, ContentValuesUtil.QUANTITY_SCALE)
                .where(ID, guid)
                .build(JdbcFactory.getApiMethod(ItemModel.class));
    }

    public SingleSqlCommand updatePrinterAliasSQL(String guid, String aliasGuid, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(PRINTER_ID, aliasGuid)
                .where(ID, guid)
                .build(JdbcFactory.getApiMethod(ItemModel.class));
    }

    public SingleSqlCommand removePrinterAlias(String aliasGuid, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(PRINTER_ID, (String) null)
                .where(PRINTER_ID, aliasGuid)
                .build(JdbcFactory.getApiMethod(ItemModel.class));
    }

    public SingleSqlCommand updateStockTrackingSQL(String guid, boolean isStockTracking, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(STOCK_TRACKING, isStockTracking)
                .where(ID, guid)
                .build(JdbcFactory.getApiMethod(ItemModel.class));
    }
}
