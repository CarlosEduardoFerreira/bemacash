package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.SaleModifierModel;
import com.kaching123.tcr.model.SaleOrderItemAddonModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

public class SaleOrderItemAddonJdbcConverter  extends JdbcConverter<SaleModifierModel>{

    public static final String TABLE_NAME = "SALE_ORDER_ITEM_ADDON";

    private static final String ID = "ID";
    private static final String ADDON_ID = "ADDON_ID";
    private static final String SALE_ITEM_ID = "SALE_ITEM_ID";
    private static final String EXTRA_COST = "EXTRA_COST";
    private static final String ADDON_TYPE = "ADDON_TYPE";
    private static final String SALE_CHILD_ITEM_ID = "SALE_CHILD_ITEM_ID";
    private static final String SALE_CHILD_ITEM_QTY = "SALE_CHILD_ITEM_QTY";

    @Override
    public SaleModifierModel toValues(JdbcJSONObject rs) throws JSONException {

        List<String> ignoreFields = new ArrayList<>();

        if (!rs.has(ID)) ignoreFields.add(ShopStore.SaleAddonTable.GUID);
        if (!rs.has(ADDON_ID)) ignoreFields.add(ShopStore.SaleAddonTable.ADDON_GUID);
        if (!rs.has(SALE_ITEM_ID)) ignoreFields.add(ShopStore.SaleAddonTable.ITEM_GUID);
        if (!rs.has(EXTRA_COST)) ignoreFields.add(ShopStore.SaleAddonTable.EXTRA_COST);
        if (!rs.has(ADDON_TYPE)) ignoreFields.add(ShopStore.SaleAddonTable.TYPE);
        if (!rs.has(SALE_CHILD_ITEM_ID)) ignoreFields.add(ShopStore.SaleAddonTable.CHILD_ITEM_ID);
        if (!rs.has(SALE_CHILD_ITEM_QTY)) ignoreFields.add(ShopStore.SaleAddonTable.CHILD_ITEM_QTY);

        return new SaleModifierModel(
                rs.getString(ID),
                rs.getString(ADDON_ID),
                rs.getString(SALE_ITEM_ID),
                rs.getBigDecimal(EXTRA_COST),
                _enum(ModifierType.class, rs.getString(ADDON_TYPE), ModifierType.ADDON),
                rs.getString(SALE_CHILD_ITEM_ID),
                rs.getBigDecimal(SALE_CHILD_ITEM_QTY),
                ignoreFields
        );
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return ID;
    }

    @Override
    public String getLocalGuidColumn() {
        return ShopStore.SaleAddonTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(SaleModifierModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(ADDON_ID, model.addonGuid)
                    .put(SALE_ITEM_ID, model.saleItemGuid)
                    .put(EXTRA_COST, model.extraCost)
                    .put(ADDON_TYPE, model.type)
                    .put(SALE_CHILD_ITEM_ID, model.childItemGuid)
                    .put(SALE_CHILD_ITEM_QTY, model.childItemQty);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(SaleModifierModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(ADDON_ID, model.addonGuid)
                .add(SALE_ITEM_ID, model.saleItemGuid)
                .add(EXTRA_COST, model.extraCost)
                .add(ADDON_TYPE, model.type)
                .add(SALE_CHILD_ITEM_ID, model.childItemGuid)
                .add(SALE_CHILD_ITEM_QTY, model.childItemQty)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(SaleModifierModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand deleteSQL(SaleModifierModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(JdbcBuilder.FIELD_IS_DELETED, 1)
                .where(ADDON_ID, model.addonGuid)
                .where(SALE_ITEM_ID, model.saleItemGuid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand deleteItemAddonsSQL(String addonGuid, String saleItemGuid, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(JdbcBuilder.FIELD_IS_DELETED, 1)
                .where(ADDON_ID, addonGuid)
                .where(SALE_ITEM_ID, saleItemGuid)
                .build(JdbcFactory.getApiMethod(SaleModifierModel.class));
    }

    public SingleSqlCommand deleteSaleItemAddons(String saleItemGuid, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(JdbcBuilder.FIELD_IS_DELETED, 1)
                .where(SALE_ITEM_ID, saleItemGuid)
                .build(JdbcFactory.getApiMethod(SaleModifierModel.class));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
