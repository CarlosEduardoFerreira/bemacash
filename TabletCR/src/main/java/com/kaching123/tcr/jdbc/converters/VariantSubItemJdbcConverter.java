package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.VariantSubItemModel;
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
public class VariantSubItemJdbcConverter extends JdbcConverter<VariantSubItemModel> {

    public static final String TABLE_NAME = "SUB_VARIANT";

    private static final String ID = "ID";
    private static final String VARIANT_ID = "VARIANT_ID";
    private static final String VALUE = "VALUE";
    private static final String ITEM_ID = "ITEM_ID";

    @Override
    public VariantSubItemModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.VariantSubItemTable.GUID);
        if (!rs.has(VALUE)) ignoreFields.add(ShopStore.VariantSubItemTable.NAME);
        if (!rs.has(VARIANT_ID)) ignoreFields.add(ShopStore.VariantSubItemTable.VARIANT_ITEM_GUID);
        if (!rs.has(ITEM_ID)) ignoreFields.add(ShopStore.VariantSubItemTable.ITEM_GUID);

        return new VariantSubItemModel(
                rs.getString(ID),
                rs.getString(VALUE),
                rs.getString(VARIANT_ID),
                rs.getString(ITEM_ID),
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
        return ShopStore.VariantSubItemTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(VariantSubItemModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(VALUE, model.name)
                    .put(VARIANT_ID, model.parentVariantItemGuid)
                    .put(ITEM_ID, model.itemGuid);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(VariantSubItemModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(VALUE, model.name)
                .add(VARIANT_ID, model.parentVariantItemGuid)
                .add(ITEM_ID, model.itemGuid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(VariantSubItemModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(VALUE, model.name)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
