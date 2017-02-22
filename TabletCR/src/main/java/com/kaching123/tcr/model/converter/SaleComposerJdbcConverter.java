package com.kaching123.tcr.model.converter;

import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.SaleComposerModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore.SaleComposerTable;
import com.kaching123.tcr.util.JdbcJSONObject;

import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by vkompaniets on 08.02.2016.
 */
public class SaleComposerJdbcConverter extends JdbcConverter<SaleComposerModel> {

    public static final String TABLE_NAME = "SALE_COMPOSITION";

    private static final String ID = "ID";
    public static final String SALE_ITEM_ID = "SALE_ITEM_ID";
    private static final String SALE_MODIFIER_ID = "SALE_MODIFIER_ID";
    private static final String HOST_ITEM_ID = "HOST_ITEM_ID";
    private static final String CHILD_ITEM_ID = "CHILD_ITEM_ID";
    private static final String QTY = "QTY";
    private static final String STOCK_TRACKING = "STOCK_TRACKING";
    private static final String RESTRICT_QTY = "RESTRICT_QTY";

    @Override
    public SaleComposerModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(SaleComposerTable.ID);
        if (!rs.has(SALE_ITEM_ID)) ignoreFields.add(SaleComposerTable.SALE_ITEM_ID);
        if (!rs.has(SALE_MODIFIER_ID)) ignoreFields.add(SaleComposerTable.SALE_MODIFIER_ID);
        if (!rs.has(HOST_ITEM_ID)) ignoreFields.add(SaleComposerTable.HOST_ITEM_ID);
        if (!rs.has(CHILD_ITEM_ID)) ignoreFields.add(SaleComposerTable.CHILD_ITEM_ID);
        if (!rs.has(QTY)) ignoreFields.add(SaleComposerTable.QTY);
        if (!rs.has(STOCK_TRACKING)) ignoreFields.add(SaleComposerTable.STOCK_TRACKING_ENABLED);
        if (!rs.has(RESTRICT_QTY)) ignoreFields.add(SaleComposerTable.RESTRICT_QTY);

        return new SaleComposerModel(
                rs.getString(ID),
                rs.getString(SALE_ITEM_ID),
                rs.getString(SALE_MODIFIER_ID),
                rs.getString(HOST_ITEM_ID),
                rs.getString(CHILD_ITEM_ID),
                rs.getBigDecimal(QTY),
                rs.getBoolean(STOCK_TRACKING),
                rs.getBoolean(RESTRICT_QTY),
                ignoreFields);
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
        return SaleComposerTable.ID;
    }

    @Override
    public JSONObject getJSONObject(SaleComposerModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.id)
                    .put(SALE_ITEM_ID, model.saleItemId)
                    .put(SALE_MODIFIER_ID, model.saleModifierId)
                    .put(HOST_ITEM_ID, model.hostItemId)
                    .put(CHILD_ITEM_ID, model.childItemId)
                    .put(QTY, model.qty)
                    .put(STOCK_TRACKING, model.stockTracking)
                    .put(RESTRICT_QTY, model.restrictQty);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(SaleComposerModel model, IAppCommandContext appCommandContext) {
        return JdbcBuilder._insert(TABLE_NAME, appCommandContext)
                .add(ID, model.id)
                .add(SALE_ITEM_ID, model.saleItemId)
                .add(SALE_MODIFIER_ID, model.saleModifierId)
                .add(HOST_ITEM_ID, model.hostItemId)
                .add(CHILD_ITEM_ID, model.childItemId)
                .add(QTY, model.qty)
                .add(STOCK_TRACKING, model.stockTracking)
                .add(RESTRICT_QTY, model.restrictQty)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(SaleComposerModel model, IAppCommandContext appCommandContext) {
        return JdbcBuilder._update(TABLE_NAME, appCommandContext)
                .add(SALE_ITEM_ID, model.saleItemId)
                .add(SALE_MODIFIER_ID, model.saleModifierId)
                .add(HOST_ITEM_ID, model.hostItemId)
                .add(CHILD_ITEM_ID, model.childItemId)
                .add(QTY, model.qty)
                .add(STOCK_TRACKING, model.stockTracking)
                .add(RESTRICT_QTY, model.restrictQty)
                .where(ID, model.id)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand deleteSQL(SaleComposerModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(JdbcBuilder.FIELD_IS_DELETED, 1)
                .where(ID, model.id)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}