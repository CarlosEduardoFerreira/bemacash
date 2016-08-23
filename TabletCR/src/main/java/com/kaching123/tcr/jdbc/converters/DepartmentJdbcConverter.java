package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.DepartmentModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

public class DepartmentJdbcConverter extends JdbcConverter<DepartmentModel> {

    private static final String DEPARTMENT_TABLE_NAME = "DEPARTMENT";

    private static final String ID = "ID";
    private static final String TITLE = "TITLE";

    @Override
    public DepartmentModel toValues(JdbcJSONObject rs) throws JSONException {
        return new DepartmentModel(rs.getString(ID), rs.getString(TITLE));
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
    public SingleSqlCommand insertSQL(DepartmentModel model, IAppCommandContext appCommandContext) {
//        return String.format(Locale.US, "insert into DEPARTMENT(ID, SHOP_ID, TITLE)" +
//                " values('%s', %d, '%s')", model.guid, TcrApplication.get().getShopId(), model.title, model.hidden);
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
}
