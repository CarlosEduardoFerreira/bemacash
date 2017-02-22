package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.UnitLabelModel;
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
 * Created by alboyko on 26.11.2015.
 */

public class UnitLabelJdbcConverter extends JdbcConverter<UnitLabelModel> {

    public static final String TABLE_NAME = "UNIT_LABEL";

    private static final String ID = "ID";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String SHORTCUT = "SHORTCUT";

    @Override
    public UnitLabelModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.UnitLabelTable.GUID);
        if (!rs.has(DESCRIPTION)) ignoreFields.add(ShopStore.UnitLabelTable.DESCRIPTION);
        if (!rs.has(SHORTCUT)) ignoreFields.add(ShopStore.UnitLabelTable.SHORTCUT);

        return new UnitLabelModel(
                rs.getString(ID),
                rs.getString(DESCRIPTION),
                rs.getString(SHORTCUT),
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
        return ShopStore.UnitLabelTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(UnitLabelModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(DESCRIPTION, model.description)
                    .put(SHORTCUT, model.shortcut);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(UnitLabelModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(DESCRIPTION, model.description)
                .add(SHORTCUT, model.shortcut)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(UnitLabelModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(DESCRIPTION, model.description)
                .add(SHORTCUT, model.shortcut)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
