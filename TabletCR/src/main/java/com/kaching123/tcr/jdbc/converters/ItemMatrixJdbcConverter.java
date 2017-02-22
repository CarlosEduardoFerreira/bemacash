package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ItemMatrixModel;
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
public class ItemMatrixJdbcConverter extends JdbcConverter<ItemMatrixModel> {

    public static final String TABLE_NAME = "ITEM_MATRIX";

    private static final String ID = "ID";
    private static final String PARENT_ITEM_ID = "PARENT_ITEM_ID";
    private static final String CHILD_ITEM_ID = "CHILD_ITEM_ID";
    private static final String TITLE = "TITLE";

    @Override
    public ItemMatrixModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.ItemMatrixTable.ID);
        if (!rs.has(TITLE)) ignoreFields.add(ShopStore.ItemMatrixTable.NAME);
        if (!rs.has(PARENT_ITEM_ID)) ignoreFields.add(ShopStore.ItemMatrixTable.PARENT_GUID);
        if (!rs.has(CHILD_ITEM_ID)) ignoreFields.add(ShopStore.ItemMatrixTable.CHILD_GUID);

        return new ItemMatrixModel(
                rs.getString(ID),
                rs.getString(TITLE),
                rs.getString(PARENT_ITEM_ID),
                rs.getString(CHILD_ITEM_ID),
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
        return ShopStore.ItemMatrixTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(ItemMatrixModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(TITLE, model.name)
                    .put(PARENT_ITEM_ID, model.parentItemGuid)
                    .put(CHILD_ITEM_ID, model.childItemGuid);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(ItemMatrixModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(TITLE, model.name)
                .add(PARENT_ITEM_ID, model.parentItemGuid)
                .add(CHILD_ITEM_ID, model.childItemGuid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(ItemMatrixModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(TITLE, model.name)
                .add(CHILD_ITEM_ID, model.childItemGuid)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
