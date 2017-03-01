package com.kaching123.tcr.jdbc.converters;

import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.DiscountType;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.ItemRefType;
import com.kaching123.tcr.model.PriceType;
import com.kaching123.tcr.model.Unit.CodeType;
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
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;
import static com.kaching123.tcr.store.ShopStore.ItemTable.GUID;
import static com.kaching123.tcr.store.ShopStore.ItemTable.UNIT_LABEL_ID;

public class ItemsJdbcConverter extends JdbcConverter<ItemModel> implements IOrderNumUpdater{

    public static final String ITEM_TABLE_NAME = "ITEM";

    private static final String ID = "ID";
    private static final String CATEGORY_ID = "CATEGORY_ID";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String CODE = "CODE";
    private static final String EAN_CODE = "EAN_CODE";
    private static final String PRODUCT_CODE = "PRODUCT_CODE";
    private static final String PRICE_TYPE = "PRICE_TYPE";
    private static final String SALE_PRICE = "SALE_PRICE";
    private static final String PRICE_1 = "PRICE_LEVEL_1";
    private static final String PRICE_2 = "PRICE_LEVEL_2";
    private static final String PRICE_3 = "PRICE_LEVEL_3";
    private static final String PRICE_4 = "PRICE_LEVEL_4";
    private static final String PRICE_5 = "PRICE_LEVEL_5";
    private static final String UNITS_LABEL_ID = "UNIT_LABEL_ID";
    private static final String STOCK_TRACKING = "STOCK_TRACKING";
    private static final String LIMIT_QTY = "LIMIT_QTY";
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
    private static final String TAX_GROUP_ID2 = "TAX_GROUP_ID2";
    private static final String CODE_TYPE = "CODE_TYPE";
    private static final String SERIALIZABLE = "SERIALIZABLE";
    private static final String ORDER_NUM = "ORDER_NUM";
    private static final String PRINTER_ID = "PRINTER_ID";
    private static final String BUTTON_VIEW = "BUTTON_VIEW";
    private static final String HAS_NOTES = "HAS_NOTES";
    private static final String ELIGIBLE_FOR_COMMISSION = "ELIGIBLE_FOR_COMMISSION";
    private static final String COMMISSION = "COMMISSION";
    private static final String IS_DELETED = "IS_DELETED";
    private static final String ITEM_REF_TYPE = "ITEM_REF_TYPE";
    private static final String REFERENCE_ITEM_ID = "REFERENCE_ITEM_ID";
    private static final String LOYALTY_POINTS = "LOYALTY_POINTS";
    private static final String EXCLUDE_FROM_LOYALTY_PLAN = "EXCLUDE_FROM_LOYALTY_PLAN";
    private static final String EBT_ELIGIBLE = "EBT_ELIGIBLE";

    @Override
    public ItemModel toValues(JdbcJSONObject rs) throws JSONException {
        Log.d("BemaCarl","ItemsJdbcConverter.toValues.rs: " + rs);

        List<String> ignoreFields = new ArrayList<>();

        if (!rs.has(ID)) ignoreFields.add(ID);
        if (!rs.has(CATEGORY_ID)) ignoreFields.add(ShopStore.ItemTable.CATEGORY_ID);
        if (!rs.has(DESCRIPTION)) ignoreFields.add(ShopStore.ItemTable.DESCRIPTION);
        if (!rs.has(CODE)) ignoreFields.add(ShopStore.ItemTable.CODE);
        if (!rs.has(EAN_CODE)) ignoreFields.add(ShopStore.ItemTable.EAN_CODE);
        if (!rs.has(PRODUCT_CODE)) ignoreFields.add(ShopStore.ItemTable.PRODUCT_CODE);
        if (!rs.has(PRICE_TYPE)) ignoreFields.add(ShopStore.ItemTable.PRICE_TYPE);
        if (!rs.has(SALE_PRICE)) ignoreFields.add(ShopStore.ItemTable.SALE_PRICE);
        if (!rs.has(PRICE_1)) ignoreFields.add(ShopStore.ItemTable.PRICE_1);
        if (!rs.has(PRICE_2)) ignoreFields.add(ShopStore.ItemTable.PRICE_2);
        if (!rs.has(PRICE_3)) ignoreFields.add(ShopStore.ItemTable.PRICE_3);
        if (!rs.has(PRICE_4)) ignoreFields.add(ShopStore.ItemTable.PRICE_4);
        if (!rs.has(PRICE_5)) ignoreFields.add(ShopStore.ItemTable.PRICE_5);
        if (!rs.has(UNIT_LABEL_ID)) ignoreFields.add(UNIT_LABEL_ID);
        if (!rs.has(STOCK_TRACKING)) ignoreFields.add(ShopStore.ItemTable.STOCK_TRACKING);
        if (!rs.has(LIMIT_QTY)) ignoreFields.add(ShopStore.ItemTable.LIMIT_QTY);
        if (!rs.has(ACTIVE_STATUS)) ignoreFields.add(ShopStore.ItemTable.ACTIVE_STATUS);
        if (!rs.has(DISCOUNTABLE)) ignoreFields.add(ShopStore.ItemTable.DISCOUNTABLE);
        if (!rs.has(SALABLE)) ignoreFields.add(ShopStore.ItemTable.SALABLE);
        if (!rs.has(DISCOUNT)) ignoreFields.add(ShopStore.ItemTable.DISCOUNT);
        if (!rs.has(DISCOUNT_TYPE)) ignoreFields.add(ShopStore.ItemTable.DISCOUNT_TYPE);
        if (!rs.has(TAXABLE)) ignoreFields.add(ShopStore.ItemTable.TAXABLE);
        if (!rs.has(COST)) ignoreFields.add(ShopStore.ItemTable.COST);
        if (!rs.has(MINIMUM_QTY)) ignoreFields.add(ShopStore.ItemTable.MINIMUM_QTY);
        if (!rs.has(RECOMMENDED_QTY)) ignoreFields.add(ShopStore.ItemTable.RECOMMENDED_QTY);
        if (!rs.has(UPDATE_QTY_FLAG)) ignoreFields.add(ShopStore.ItemTable.UPDATE_QTY_FLAG);
        if (!rs.has(TAX_GROUP_ID)) ignoreFields.add(ShopStore.ItemTable.TAX_GROUP_GUID);
        if (!rs.has(TAX_GROUP_ID2)) ignoreFields.add(ShopStore.ItemTable.TAX_GROUP_GUID2);
        if (!rs.has(ORDER_NUM)) ignoreFields.add(ShopStore.ItemTable.ORDER_NUM);
        if (!rs.has(PRINTER_ID)) ignoreFields.add(ShopStore.ItemTable.PRINTER_ALIAS_GUID);
        if (!rs.has(BUTTON_VIEW)) ignoreFields.add(ShopStore.ItemTable.BUTTON_VIEW);
        if (!rs.has(HAS_NOTES)) ignoreFields.add(ShopStore.ItemTable.HAS_NOTES);
        if (!rs.has(SERIALIZABLE)) ignoreFields.add(ShopStore.ItemTable.SERIALIZABLE);
        if (!rs.has(CODE_TYPE)) ignoreFields.add(ShopStore.ItemTable.CODE_TYPE);
        if (!rs.has(ELIGIBLE_FOR_COMMISSION)) ignoreFields.add(ShopStore.ItemTable.ELIGIBLE_FOR_COMMISSION);
        if (!rs.has(COMMISSION)) ignoreFields.add(ShopStore.ItemTable.COMMISSION);
        if (!rs.has(REFERENCE_ITEM_ID)) ignoreFields.add(ShopStore.ItemTable.REFERENCE_ITEM_ID);
        if (!rs.has(ITEM_REF_TYPE)) ignoreFields.add(ShopStore.ItemTable.ITEM_REF_TYPE);
        if (!rs.has(LOYALTY_POINTS)) ignoreFields.add(ShopStore.ItemTable.LOYALTY_POINTS);
        if (!rs.has(EXCLUDE_FROM_LOYALTY_PLAN)) ignoreFields.add(ShopStore.ItemTable.EXCLUDE_FROM_LOYALTY_PLAN);
        if (!rs.has(EBT_ELIGIBLE)) ignoreFields.add(ShopStore.ItemTable.EBT_ELIGIBLE);

        Log.d("BemaCarl","ItemsJdbcConverter.toValues.rs.getString(DISCOUNT_TYPE): " + rs.getString(DISCOUNT_TYPE));
        Log.d("BemaCarl","ItemsJdbcConverter.toValues._enum(DiscountType: " + _enum(DiscountType.class, rs.getString(DISCOUNT_TYPE), DiscountType.PERCENT));

        return new ItemModel(
                rs.getString(ID),
                rs.getString(CATEGORY_ID),
                rs.getString(DESCRIPTION),
                rs.getString(CODE),
                rs.getString(EAN_CODE),
                rs.getString(PRODUCT_CODE),
                _enum(PriceType.class, rs.getString(PRICE_TYPE), PriceType.OPEN),
                rs.getBigDecimal(SALE_PRICE),
                rs.getBigDecimal(PRICE_1),
                rs.getBigDecimal(PRICE_2),
                rs.getBigDecimal(PRICE_3),
                rs.getBigDecimal(PRICE_4),
                rs.getBigDecimal(PRICE_5),
                BigDecimal.ZERO,
                rs.getString(UNITS_LABEL_ID),
                rs.getBoolean(STOCK_TRACKING),
                rs.getBoolean(LIMIT_QTY),
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
                rs.getString(TAX_GROUP_ID2),
                rs.getInt(ORDER_NUM),
                rs.getString(PRINTER_ID),
                rs.getInt(BUTTON_VIEW),
                rs.getBoolean(HAS_NOTES),
                rs.getBoolean(SERIALIZABLE),
                _enum(CodeType.class, rs.getString(CODE_TYPE), null),
                rs.getBoolean(ELIGIBLE_FOR_COMMISSION),
                rs.getBigDecimal(COMMISSION),
                rs.getString(REFERENCE_ITEM_ID),
                ItemRefType.valueOf(rs.getInt(ITEM_REF_TYPE)),
                rs.getBigDecimal(LOYALTY_POINTS),
                rs.getBoolean(EXCLUDE_FROM_LOYALTY_PLAN),
                rs.getBoolean(EBT_ELIGIBLE),
                ignoreFields
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
    public String getLocalGuidColumn() {
        return ShopStore.ItemTable.GUID;
    }


    @Override
    public JSONObject getJSONObject(ItemModel item){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, item.guid)
                    .put(CATEGORY_ID, item.categoryId)
                    .put(DESCRIPTION, item.description)
                    .put(CODE, item.code)
                    .put(EAN_CODE, item.eanCode)
                    .put(PRODUCT_CODE, item.productCode)
                    .put(PRICE_TYPE, item.priceType)
                    .put(SALE_PRICE, item.price)
                    .put(PRICE_1, item.price1)
                    .put(PRICE_2, item.price2)
                    .put(PRICE_3, item.price3)
                    .put(PRICE_4, item.price4)
                    .put(PRICE_5, item.price5)
                    .put(UNITS_LABEL_ID, item.unitsLabelId)
                    .put(STOCK_TRACKING, item.isStockTracking)
                    .put(LIMIT_QTY, item.limitQty)
                    .put(ACTIVE_STATUS, item.isActiveStatus)
                    .put(DISCOUNTABLE, item.isDiscountable)
                    .put(SALABLE, item.isSalable)
                    .put(DISCOUNT, item.discount)
                    .put(DISCOUNT_TYPE, item.discountType)
                    .put(TAXABLE, item.isTaxable)
                    .put(COST, item.cost)
                    .put(MINIMUM_QTY, item.minimumQty)
                    .put(RECOMMENDED_QTY, item.recommendedQty)
                    .put(UPDATE_QTY_FLAG, item.updateQtyFlag)
                    .put(TAX_GROUP_ID, item.taxGroupGuid)
                    .put(TAX_GROUP_ID2, item.taxGroupGuid2)
                    .put(ORDER_NUM, item.orderNum)
                    .put(PRINTER_ID, item.printerAliasGuid)
                    .put(HAS_NOTES, item.hasNotes)
                    .put(SERIALIZABLE, item.serializable)
                    .put(CODE_TYPE, item.codeType)
                    .put(ELIGIBLE_FOR_COMMISSION, item.commissionEligible)
                    .put(COMMISSION, item.commission)
                    .put(REFERENCE_ITEM_ID, item.referenceItemGuid)
                    .put(ITEM_REF_TYPE, item.refType.ordinal())
                    .put(LOYALTY_POINTS, item.loyaltyPoints)
                    .put(EXCLUDE_FROM_LOYALTY_PLAN, item.excludeFromLoyaltyPlan)
                    .put(EBT_ELIGIBLE, item.isEbtEligible);

            Log.d("BemaCarl","ItemsJdbcConverter.getJSONObject.json: " + json);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
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
                .add(PRICE_1, item.price1)
                .add(PRICE_2, item.price2)
                .add(PRICE_3, item.price3)
                .add(PRICE_4, item.price4)
                .add(PRICE_5, item.price5)
                .add(UNITS_LABEL_ID, item.unitsLabelId)
                .add(STOCK_TRACKING, item.isStockTracking)
                .add(LIMIT_QTY, item.limitQty)
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
                .add(TAX_GROUP_ID2, item.taxGroupGuid2)
                .add(ORDER_NUM, item.orderNum)
                .add(PRINTER_ID, item.printerAliasGuid)
                .add(HAS_NOTES, item.hasNotes)
                .add(SERIALIZABLE, item.serializable)
                .add(CODE_TYPE, item.codeType)
                .add(ELIGIBLE_FOR_COMMISSION, item.commissionEligible)
                .add(COMMISSION, item.commission)
                .add(ITEM_REF_TYPE, item.refType.ordinal())
                .add(REFERENCE_ITEM_ID, item.referenceItemGuid)
                .add(LOYALTY_POINTS, _decimal(item.loyaltyPoints))
                .add(EXCLUDE_FROM_LOYALTY_PLAN, item.excludeFromLoyaltyPlan)
                .add(EBT_ELIGIBLE, item.isEbtEligible)
                .build(JdbcFactory.getApiMethod(item));
    }

    @Override
    public SingleSqlCommand updateSQL(ItemModel item, IAppCommandContext appCommandContext) {
        Log.d("BemaCarl","ItemsJdbcConverter.updateSQL.item.discountType: " + item.discountType);
        Log.d("BemaCarl","ItemsJdbcConverter.updateSQL.item.discountType.ordinal(): " + item.discountType.ordinal());
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(CATEGORY_ID, item.categoryId)
                .add(DESCRIPTION, item.description)
                .add(CODE, item.code)
                .add(EAN_CODE, item.eanCode)
                .add(PRODUCT_CODE, item.productCode)
                .add(PRICE_TYPE, item.priceType)
                .add(SALE_PRICE, item.price)
                .add(PRICE_1, item.price1)
                .add(PRICE_2, item.price2)
                .add(PRICE_3, item.price3)
                .add(PRICE_4, item.price4)
                .add(PRICE_5, item.price5)
                .add(UNITS_LABEL_ID, item.unitsLabelId)
                .add(STOCK_TRACKING, item.isStockTracking)
                .add(LIMIT_QTY, item.limitQty)
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
                .add(TAX_GROUP_ID2, item.taxGroupGuid2)
                .add(ORDER_NUM, item.orderNum)
                .add(PRINTER_ID, item.printerAliasGuid)
                .add(BUTTON_VIEW, item.btnView)
                .add(HAS_NOTES, item.hasNotes)
                .add(SERIALIZABLE, item.serializable)
                .add(CODE_TYPE, item.codeType)
                .add(ELIGIBLE_FOR_COMMISSION, item.commissionEligible)
                .add(COMMISSION, item.commission)
                .add(ITEM_REF_TYPE, item.refType.ordinal())
                .add(REFERENCE_ITEM_ID, item.referenceItemGuid)
                .add(LOYALTY_POINTS, _decimal(item.loyaltyPoints))
                .add(EXCLUDE_FROM_LOYALTY_PLAN, item.excludeFromLoyaltyPlan)
                .add(EBT_ELIGIBLE, item.isEbtEligible)
                .where(ID, item.guid)
                .build(JdbcFactory.getApiMethod(item));
    }

    public SingleSqlCommand removeTaxGroup(String taxGroupGuid, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(TAX_GROUP_ID, (String) null)
                .where(TAX_GROUP_ID, taxGroupGuid)
                .build(JdbcFactory.getApiMethod(ItemModel.class));
    }

    public SingleSqlCommand updateOrderSQL(String guid, int orderNum, IAppCommandContext appCommandContext) {
        ItemModel model = ItemModel.getById(TcrApplication.get(), guid, true);
        model.orderNum = orderNum;
        return updateSQL(model, appCommandContext);
    }

    public SingleSqlCommand updatePriceSQL(String guid, BigDecimal price, IAppCommandContext appCommandContext) {
        ItemModel model = ItemModel.getById(TcrApplication.get(), guid, true);
        model.price = price;
        return updateSQL(model, appCommandContext);
    }

    public SingleSqlCommand updatePriceSQL(String guid, BigDecimal price, PriceType type, IAppCommandContext appCommandContext) {
        ItemModel model = ItemModel.getById(TcrApplication.get(), guid, true);
        model.price = price;
        model.priceType = type;
        return updateSQL(model, appCommandContext);
    }

    public SingleSqlCommand updateQtyFlagSQL(String guid, String updateFlag, IAppCommandContext appCommandContext) {
        ItemModel model = ItemModel.getById(TcrApplication.get(), guid, true);
        model.updateQtyFlag = updateFlag;
        return updateSQL(model, appCommandContext);
    }

    public SingleSqlCommand updateIsDeletedSQL(String guid, boolean isDeleted, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(IS_DELETED, isDeleted)
                .where(ID, guid)
                .build(JdbcFactory.getApiMethod(ItemModel.class));
    }

    public SingleSqlCommand removePrinterAlias(String aliasGuid, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(PRINTER_ID, (String) null)
                .where(PRINTER_ID, aliasGuid)
                .build(JdbcFactory.getApiMethod(ItemModel.class));
    }

    @Override
    public SingleSqlCommand updateOrderNum(String id, int orderNum, IAppCommandContext appCommandContext) {
        return _update(ITEM_TABLE_NAME, appCommandContext)
                .add(ORDER_NUM, orderNum)
                .where(ID, id)
                .build(JdbcFactory.getApiMethod(ItemModel.class));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
