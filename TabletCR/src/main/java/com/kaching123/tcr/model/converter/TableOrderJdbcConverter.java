package com.kaching123.tcr.model.converter;

import com.telly.groundy.PublicGroundyTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.TableOrderModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore.TableOrderTable;
import com.kaching123.tcr.util.JdbcJSONObject;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.util.ContentValuesUtilBase._enum;

/**
 * Created by Rodrigo Busata on 07/22/16.
 */
public class TableOrderJdbcConverter extends JdbcConverter<TableOrderModel> {

    public static final String TABLE_NAME = "TABLE_ORDER";

    private static final String ID = "ID";
    private static final String NAME = "NAME";
    private static final String DESCRIPTION = "DESCRIPTION";
    private static final String QTY_CUSTOMER_ORDER = "QTY_CUSTOMER_ORDER";
    private static final String QTY_CURRENT_CUSTOMER_ORDER = "QTY_CURRENT_CUSTOMER_ORDER";
    private static final String STATUS = "STATUS";
    private static final String STATUS_TIME = "STATUS_TIME";
    private static final String AMOUNT = "AMOUNT";

    @Override
    public TableOrderModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(TableOrderTable.GUID);
        if (!rs.has(NAME)) ignoreFields.add(TableOrderTable.NAME);
        if (!rs.has(DESCRIPTION)) ignoreFields.add(TableOrderTable.DESCRIPTION);
        if (!rs.has(QTY_CUSTOMER_ORDER)) ignoreFields.add(TableOrderTable.QTY_CUSTOMER_ORDER);
        if (!rs.has(STATUS)) ignoreFields.add(TableOrderTable.STATUS);
        if (!rs.has(STATUS_TIME)) ignoreFields.add(TableOrderTable.STATUS_TIME);
        if (!rs.has(QTY_CURRENT_CUSTOMER_ORDER)) ignoreFields.add(TableOrderTable.QTY_CURRENT_CUSTOMER_ORDER);
        if (!rs.has(AMOUNT)) ignoreFields.add(TableOrderTable.AMOUNT);

        return new TableOrderModel(
                rs.getString(ID),
                rs.getString(NAME),
                rs.getString(DESCRIPTION),
                rs.getInt(QTY_CUSTOMER_ORDER),
                rs.getInt(QTY_CURRENT_CUSTOMER_ORDER),
                rs.getDate(STATUS_TIME),
                rs.getBigDecimal(AMOUNT),
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
        return TableOrderTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(TableOrderModel model) {
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(NAME, model.name)
                    .put(DESCRIPTION, model.description)
                    .put(QTY_CUSTOMER_ORDER, model.qtyCustomerOrder)
                    .put(QTY_CURRENT_CUSTOMER_ORDER, model.qtyCurrentCustomerOrder)
                    .put(STATUS_TIME, model.statusTime)
                    .put(AMOUNT, model.amount);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(TableOrderModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        return _insert(getShopId(), TABLE_NAME)
                .add(ID, model.guid)
                .add(NAME, model.name)
                .add(DESCRIPTION, model.description)
                .add(QTY_CUSTOMER_ORDER, model.qtyCustomerOrder)
                .add(QTY_CURRENT_CUSTOMER_ORDER, model.qtyCurrentCustomerOrder)
                .add(STATUS_TIME, model.statusTime)
                .add(AMOUNT, model.amount)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(TableOrderModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, null)
                .add(NAME, model.name)
                .add(DESCRIPTION, model.description)
                .add(QTY_CUSTOMER_ORDER, model.qtyCustomerOrder)
                .add(QTY_CURRENT_CUSTOMER_ORDER, model.qtyCurrentCustomerOrder)
                .add(STATUS_TIME, model.statusTime)
                .add(AMOUNT, model.amount)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}