package com.kaching123.tcr.model.converter;

import com.telly.groundy.PublicGroundyTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.CustomerOrderModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore.CustomerOrderTable;
import com.kaching123.tcr.util.JdbcJSONObject;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by Rodrigo Busata on 07/22/16.
 */
public class CustomerOrderJdbcConverter extends JdbcConverter<CustomerOrderModel> {

    public static final String TABLE_NAME = "CUSTOMER_ORDER";

    private static final String ID = "ID";
    private static final String CODE = "CODE";
    private static final String ORDER_ID = "ORDER_ID";
    private static final String TABLE_ID = "TABLE_ID";
    private static final String DESCRIPTION = "DESCRIPTION";

    @Override
    public CustomerOrderModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(CustomerOrderTable.GUID);
        if (!rs.has(CODE)) ignoreFields.add(CustomerOrderTable.CODE);
        if (!rs.has(ORDER_ID)) ignoreFields.add(CustomerOrderTable.ORDER_GUID);
        if (!rs.has(TABLE_ID)) ignoreFields.add(CustomerOrderTable.TABLE_GUID);
        if (!rs.has(DESCRIPTION)) ignoreFields.add(CustomerOrderTable.DESCRIPTION);

        return new CustomerOrderModel(
                rs.getString(ID),
                rs.getString(CODE),
                rs.getString(DESCRIPTION),
                rs.getString(ORDER_ID),
                rs.getString(TABLE_ID),
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
        return CustomerOrderTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(CustomerOrderModel model) {
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(CODE, model.code)
                    .put(DESCRIPTION, model.description)
                    .put(ORDER_ID, model.orderGuid)
                    .put(TABLE_ID, model.tableGuid);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(CustomerOrderModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        return _insert(getShopId(), TABLE_NAME)
                .add(ID, model.guid)
                .add(TABLE_ID, model.tableGuid)
                .add(ORDER_ID, model.orderGuid)
                .add(CODE, model.code)
                .add(DESCRIPTION, model.description)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(CustomerOrderModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, null)
                .add(TABLE_ID, model.tableGuid)
                .add(ORDER_ID, model.orderGuid)
                .add(CODE, model.code)
                .add(DESCRIPTION, model.description)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}