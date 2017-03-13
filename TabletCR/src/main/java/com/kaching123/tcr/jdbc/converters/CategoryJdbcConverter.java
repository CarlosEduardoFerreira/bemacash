package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CategoryModel;
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

public class CategoryJdbcConverter extends JdbcConverter<CategoryModel> {

    public static final String CATEGORY_TABLE_NAME = "CATEGORY";

    private static final String ID = "ID";
    private static final String TITLE = "TITLE";
    private static final String IMAGE = "IMAGE";
    private static final String ORDER_NUM = "ORDER_NUM";
    private static final String DEPARTMENT_ID = "DEPARTMENT_ID";
    private static final String ELIGIBLE_FOR_COMMISSION = "ELIGIBLE_FOR_COMMISSION";
    private static final String COMMISSION = "COMMISSION";

    @Override
    public CategoryModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.CategoryTable.GUID);
        if (!rs.has(DEPARTMENT_ID)) ignoreFields.add(ShopStore.CategoryTable.DEPARTMENT_GUID);
        if (!rs.has(TITLE)) ignoreFields.add(ShopStore.CategoryTable.TITLE);
        if (!rs.has(IMAGE)) ignoreFields.add(ShopStore.CategoryTable.IMAGE);
        if (!rs.has(ORDER_NUM)) ignoreFields.add(ShopStore.CategoryTable.ORDER_NUM);
        if (!rs.has(ELIGIBLE_FOR_COMMISSION)) ignoreFields.add(ShopStore.CategoryTable.ELIGIBLE_FOR_COMMISSION);
        if (!rs.has(COMMISSION)) ignoreFields.add(ShopStore.CategoryTable.COMMISSION);

        return new CategoryModel(
                rs.getString(ID),
                rs.getString(DEPARTMENT_ID),
                rs.getString(TITLE),
                rs.getString(IMAGE),
                rs.getInt(ORDER_NUM),
                rs.getBoolean(ELIGIBLE_FOR_COMMISSION),
                rs.getBigDecimal(COMMISSION),
                ignoreFields
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
    public String getLocalGuidColumn() {
        return ShopStore.CategoryTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(CategoryModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(DEPARTMENT_ID, model.departmentGuid)
                    .put(TITLE, model.title)
                    .put(IMAGE, model.image)
                    .put(ORDER_NUM, model.orderNum)
                    .put(ELIGIBLE_FOR_COMMISSION, model.commissionEligible)
                    .put(COMMISSION, model.commission);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
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
                .add(DEPARTMENT_ID, model.departmentGuid)
                .add(TITLE, model.title)
                .add(IMAGE, model.image)
                .add(ORDER_NUM, model.orderNum)
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

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
