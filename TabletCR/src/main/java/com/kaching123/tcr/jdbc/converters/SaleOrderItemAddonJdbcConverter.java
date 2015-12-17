package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ModifierType;
import com.kaching123.tcr.model.SaleOrderItemAddonModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

public class SaleOrderItemAddonJdbcConverter extends JdbcConverter<SaleOrderItemAddonModel> {

    private static final String TABLE_NAME = "SALE_ORDER_ITEM_ADDON";

    private static final String ID = "ID";
    private static final String ADDON_ID = "ADDON_ID";
    private static final String SALE_ITEM_ID = "SALE_ITEM_ID";
    private static final String EXTRA_COST = "EXTRA_COST";
    private static final String ADDON_TYPE = "ADDON_TYPE";
    private static final String SALE_CHILD_ITEM_ID = "SALE_CHILD_ITEM_ID";
    private static final String SALE_CHILD_ITEM_QTY = "SALE_CHILD_ITEM_QTY";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new SaleOrderItemAddonModel(
                rs.getString(ID),
                rs.getString(ADDON_ID),
                rs.getString(SALE_ITEM_ID),
                rs.getBigDecimal(EXTRA_COST),
                _enum(ModifierType.class, rs.getString(ADDON_TYPE), ModifierType.ADDON),
                rs.getString(SALE_CHILD_ITEM_ID),
                rs.getBigDecimal(SALE_CHILD_ITEM_QTY)
        ).toValues();
    }

    @Override
    public SaleOrderItemAddonModel toValues(JdbcJSONObject rs) throws JSONException {
        return new SaleOrderItemAddonModel(
                rs.getString(ID),
                rs.getString(ADDON_ID),
                rs.getString(SALE_ITEM_ID),
                rs.getBigDecimal(EXTRA_COST),
                _enum(ModifierType.class, rs.getString(ADDON_TYPE), ModifierType.ADDON),
                rs.getString(SALE_CHILD_ITEM_ID),
                rs.getBigDecimal(SALE_CHILD_ITEM_QTY)
        );
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        //throw new UnsupportedOperationException("don't support standard delete method");
        return ID;
    }

    @Override
    public SingleSqlCommand insertSQL(SaleOrderItemAddonModel model, IAppCommandContext appCommandContext) {
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
    public SingleSqlCommand updateSQL(SaleOrderItemAddonModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand deleteSQL(SaleOrderItemAddonModel model, IAppCommandContext appCommandContext) {
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
                .build(JdbcFactory.getApiMethod(SaleOrderItemAddonModel.class));
    }

    public SingleSqlCommand deleteItemAddonsSQL(String guid, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(JdbcBuilder.FIELD_IS_DELETED, 1)
                .where(ID, guid)
                .build(JdbcFactory.getApiMethod(SaleOrderItemAddonModel.class));
    }

    public SingleSqlCommand deleteSaleItemAddons(String saleItemGuid, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(JdbcBuilder.FIELD_IS_DELETED, 1)
                .where(SALE_ITEM_ID, saleItemGuid)
                .build(JdbcFactory.getApiMethod(SaleOrderItemAddonModel.class));
    }
}
