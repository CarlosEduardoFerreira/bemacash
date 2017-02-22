package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.DepartmentModel;
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

public class DepartmentJdbcConverter extends JdbcConverter<DepartmentModel> {

    public static final String DEPARTMENT_TABLE_NAME = "DEPARTMENT";

    private static final String ID = "ID";
    private static final String TITLE = "TITLE";

    @Override
    public DepartmentModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.DepartmentTable.GUID);
        if (!rs.has(TITLE)) ignoreFields.add(ShopStore.DepartmentTable.TITLE);

        return new DepartmentModel(
                rs.getString(ID),
                rs.getString(TITLE),
                ignoreFields);
    }

    @Override
    public String getTableName() {
        return DEPARTMENT_TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return ID;
    }

    @Override
    public String getLocalGuidColumn() {
        return ShopStore.DepartmentTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(DepartmentModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(TITLE, model.title);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(DepartmentModel model, IAppCommandContext appCommandContext) {
        return _insert(DEPARTMENT_TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(TITLE, model.title)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(DepartmentModel model, IAppCommandContext appCommandContext) {
        return _update(DEPARTMENT_TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(TITLE, model.title)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
