package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ComposerModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

public class ComposerJdbcConverter extends JdbcConverter<ComposerModel> {

    public static final String TABLE_NAME = "COMPOSER";

    private static final String ID = "ID";
    private static final String ITEM_HOST_ID = "ITEM_HOST_ID";
    private static final String ITEM_CHILD_ID = "ITEM_CHILD_ID";
    private static final String QUANTITY = "QUANTITY";
    private static final String STORE_TRACKING_ENABLED = "STORE_TRACKING_ENABLED";
    private static final String FREE_OF_CHARGE_COMPOSER = "FREE_OF_CHARGE_COMPOSER";

    @Override
    public ComposerModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID))  ignoreFields.add(ShopStore.ComposerTable.ID);
        if (!rs.has(ITEM_HOST_ID))  ignoreFields.add(ShopStore.ComposerTable.ITEM_HOST_ID);
        if (!rs.has(ITEM_CHILD_ID))  ignoreFields.add(ShopStore.ComposerTable.ITEM_CHILD_ID);
        if (!rs.has(QUANTITY))  ignoreFields.add(ShopStore.ComposerTable.QUANTITY);
        if (!rs.has(STORE_TRACKING_ENABLED))  ignoreFields.add(ShopStore.ComposerTable.STORE_TRACKING_ENABLED);
        if (!rs.has(FREE_OF_CHARGE_COMPOSER))  ignoreFields.add(ShopStore.ComposerTable.FREE_OF_CHARGE_COMPOSER);

        return new ComposerModel(
                rs.getString(ID),
                rs.getString(ITEM_HOST_ID),
                rs.getString(ITEM_CHILD_ID),
                rs.getBigDecimal(QUANTITY),
                rs.getBoolean(STORE_TRACKING_ENABLED),
                rs.getBoolean(FREE_OF_CHARGE_COMPOSER),
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
        return ShopStore.ComposerTable.ID;
    }

    @Override
    public JSONObject getJSONObject(ComposerModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(ITEM_HOST_ID, model.itemHostId)
                    .put(ITEM_CHILD_ID, model.itemChildId)
                    .put(QUANTITY, model.qty)
                    .put(STORE_TRACKING_ENABLED, model.tracked)
                    .put(FREE_OF_CHARGE_COMPOSER, model.restricted);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(ComposerModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(ITEM_HOST_ID, model.itemHostId)
                .add(ITEM_CHILD_ID, model.itemChildId)
                .add(QUANTITY, model.qty)
                .add(STORE_TRACKING_ENABLED, model.tracked)
                .add(FREE_OF_CHARGE_COMPOSER, model.restricted)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand deleteSQL(ComposerModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(JdbcBuilder.FIELD_IS_DELETED, 1)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(ComposerModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(ITEM_HOST_ID, model.itemHostId)
                .add(ITEM_CHILD_ID, model.itemChildId)
                .add(QUANTITY, model.qty)
                .add(STORE_TRACKING_ENABLED, model.tracked)
                .add(FREE_OF_CHARGE_COMPOSER, model.restricted)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }

}