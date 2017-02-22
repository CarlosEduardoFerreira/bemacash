package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ShiftModel;
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
import static com.kaching123.tcr.jdbc.JdbcUtil._jdbcDate;

/**
 * Created by gdubina on 03/12/13.
 */
public class ShiftJdbcConverter extends JdbcConverter<ShiftModel> {

    public static final String TABLE_NAME = "SHIFT";

    private static final String ID = "ID";
    private static final String START_TIME = "START_TIME";
    private static final String END_TIME = "END_TIME";

    private static final String OPEN_MANAGER_ID = "OPEN_MANAGER_ID";
    private static final String CLOSE_MANAGER_ID = "CLOSE_MANAGER_ID";

    private static final String REGISTER_ID = "REGISTER_ID";
    private static final String OPEN_AMOUNT = "OPEN_AMOUNT";
    private static final String CLOSE_AMOUNT = "CLOSE_AMOUNT";

    @Override
    public ShiftModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.ShiftTable.GUID);
        if (!rs.has(START_TIME)) ignoreFields.add(ShopStore.ShiftTable.START_TIME);
        if (!rs.has(END_TIME)) ignoreFields.add(ShopStore.ShiftTable.END_TIME);
        if (!rs.has(OPEN_MANAGER_ID)) ignoreFields.add(ShopStore.ShiftTable.OPEN_MANAGER_ID);
        if (!rs.has(CLOSE_MANAGER_ID)) ignoreFields.add(ShopStore.ShiftTable.CLOSE_MANAGER_ID);
        if (!rs.has(REGISTER_ID)) ignoreFields.add(ShopStore.ShiftTable.REGISTER_ID);
        if (!rs.has(OPEN_AMOUNT)) ignoreFields.add(ShopStore.ShiftTable.OPEN_AMOUNT);
        if (!rs.has(CLOSE_AMOUNT)) ignoreFields.add(ShopStore.ShiftTable.CLOSE_AMOUNT);

        return new ShiftModel(
                rs.getString(ID),
                rs.getDate(START_TIME),
                rs.getDate(END_TIME),
                rs.getString(OPEN_MANAGER_ID),
                rs.getString(CLOSE_MANAGER_ID),
                rs.getLong(REGISTER_ID),
                rs.getBigDecimal(OPEN_AMOUNT),
                rs.getBigDecimal(CLOSE_AMOUNT),
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
        return ShopStore.ShiftTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(ShiftModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(START_TIME, model.startTime)
                    .put(END_TIME, model.endTime)
                    .put(OPEN_MANAGER_ID, model.openManagerId)
                    .put(CLOSE_MANAGER_ID, model.closeManagerId)
                    .put(REGISTER_ID, model.registerId)
                    .put(OPEN_AMOUNT, model.openAmount)
                    .put(CLOSE_AMOUNT, model.closeAmount);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(ShiftModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(START_TIME, model.startTime)
                .add(END_TIME, model.endTime)
                .add(OPEN_MANAGER_ID, model.openManagerId)
                .add(CLOSE_MANAGER_ID, model.closeManagerId)
                .add(REGISTER_ID, model.registerId)
                .add(OPEN_AMOUNT, model.openAmount)
                .add(CLOSE_AMOUNT, model.closeAmount)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(ShiftModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(END_TIME, model.endTime)
                .add(CLOSE_MANAGER_ID, model.closeManagerId)
                .add(CLOSE_AMOUNT, model.closeAmount)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }

}
