package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CategoryModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

public class CategoryJdbcConverter extends JdbcConverter<CategoryModel> {

    private static final String CATEGORY_TABLE_NAME = "CATEGORY";

    private static final String ID = "ID";
    private static final String TITLE = "TITLE";
    private static final String IMAGE = "IMAGE";
    private static final String ORDER_NUM = "ORDER_NUM";
    private static final String DEPARTMENT_ID = "DEPARTMENT_ID";
    private static final String ELIGIBLE_FOR_COMMISSION = "ELIGIBLE_FOR_COMMISSION";
    private static final String COMMISSION = "COMMISSION";

    @Override
    public CategoryModel toValues(JdbcJSONObject rs) throws JSONException {
        return new CategoryModel(
                rs.getString(ID),
                rs.getString(DEPARTMENT_ID),
                rs.getString(TITLE),
                rs.getString(IMAGE),
                rs.getInt(ORDER_NUM),
                rs.getBoolean(ELIGIBLE_FOR_COMMISSION),
                rs.getBigDecimal(COMMISSION)
        );
    }

    @Override
    public String getTableName() {
        return CATEGORY_TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return ID;
    }

    @Override
    public SingleSqlCommand insertSQL(CategoryModel model, IAppCommandContext appCommandContext) {
        return _insert(CATEGORY_TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(DEPARTMENT_ID, model.departmentGuid)
                .add(TITLE, model.title)
                .add(IMAGE, model.image)
                .add(ORDER_NUM, model.orderNum)
                .add(ELIGIBLE_FOR_COMMISSION, model.commissionEligible)
                .add(COMMISSION, model.commission)
                .build(JdbcFactory.getApiMethod(CategoryModel.class));
    }

    @Override
    public SingleSqlCommand updateSQL(CategoryModel model, IAppCommandContext appCommandContext) {
        return _update(CATEGORY_TABLE_NAME, appCommandContext)
                .add(TITLE, model.title)
                .add(IMAGE, model.image)
                .add(ORDER_NUM, model.orderNum)
                .add(DEPARTMENT_ID, model.departmentGuid)
                .add(ELIGIBLE_FOR_COMMISSION, model.commissionEligible)
                .add(COMMISSION, model.commission)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(CategoryModel.class));
    }

    public SingleSqlCommand updateOrderSQL(CategoryModel model, IAppCommandContext appCommandContext) {
        return _update(CATEGORY_TABLE_NAME, appCommandContext)
                .add(ORDER_NUM, model.orderNum)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(CategoryModel.class));
    }

    public SingleSqlCommand deleteByDepartment(String departmentGuid, IAppCommandContext appCommandContext) {
        return _update(CATEGORY_TABLE_NAME, appCommandContext)
                .add(JdbcBuilder.FIELD_IS_DELETED, 1)
                .where(DEPARTMENT_ID, departmentGuid)
                .build(JdbcFactory.getApiMethod(CategoryModel.class));
    }
}
