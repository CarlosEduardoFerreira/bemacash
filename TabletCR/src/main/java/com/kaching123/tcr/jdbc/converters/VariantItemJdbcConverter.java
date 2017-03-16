package com.kaching123.tcr.jdbc.converters;

import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.VariantItemModel;
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

/**
 * Created by aakimov on 13/05/15.
 */
public class VariantItemJdbcConverter extends JdbcConverter<VariantItemModel> {

    public static final String TABLE_NAME = "VARIANT";

    private static final String ID = "ID";
    private static final String ITEM_ID = "ITEM_ID";
    private static final String SHOP_ID = "SHOP_ID";
    private static final String TITLE = "TITLE";

    @Override
    public VariantItemModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.VariantItemTable.GUID);
        if (!rs.has(TITLE)) ignoreFields.add(ShopStore.VariantItemTable.NAME);
        if (!rs.has(ITEM_ID)) ignoreFields.add(ShopStore.VariantItemTable.ITEM_GUID);
        if (!rs.has(SHOP_ID)) ignoreFields.add(ShopStore.VariantItemTable.SHOP_ID);

        return new VariantItemModel(
                rs.getString(ID),
                rs.getString(TITLE),
                rs.getString(ITEM_ID),
                rs.getLong(SHOP_ID),
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
        return ShopStore.VariantItemTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(VariantItemModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(TITLE, model.name)
                    .put(ITEM_ID, model.parentGuid)
                    .put(SHOP_ID, model.shopId);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(VariantItemModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(TITLE, model.name)
                .add(ITEM_ID, model.parentGuid)
                .add(SHOP_ID, model.shopId)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(VariantItemModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(TITLE, model.name)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }

}
