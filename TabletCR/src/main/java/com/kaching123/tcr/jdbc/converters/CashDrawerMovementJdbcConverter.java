package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CashDrawerMovementModel;
import com.kaching123.tcr.model.payment.MovementType;
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
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by gdubina on 03/12/13.
 */
public class CashDrawerMovementJdbcConverter extends JdbcConverter<CashDrawerMovementModel> {

    public static final String TABLE_NAME = "CASH_DRAWER_MOVEMENT";

    private static final String ID = "ID";
    private static final String SHIT_ID = "SHIFT_ID";
    private static final String MANAGER_ID = "MANAGER_ID";
    private static final String TYPE = "TYPE";
    private static final String AMOUNT = "AMOUNT";
    private static final String MOVEMENT_TIME = "MOVEMENT_TIME";
    private static final String COMMENT = "COMMENT";

    @Override
    public CashDrawerMovementModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.CashDrawerMovementTable.GUID);
        if (!rs.has(SHIT_ID)) ignoreFields.add(ShopStore.CashDrawerMovementTable.SHIFT_GUID);
        if (!rs.has(MANAGER_ID)) ignoreFields.add(ShopStore.CashDrawerMovementTable.MANAGER_GUID);
        if (!rs.has(TYPE)) ignoreFields.add(ShopStore.CashDrawerMovementTable.TYPE);
        if (!rs.has(AMOUNT)) ignoreFields.add(ShopStore.CashDrawerMovementTable.AMOUNT);
        if (!rs.has(MOVEMENT_TIME)) ignoreFields.add(ShopStore.CashDrawerMovementTable.MOVEMENT_TIME);
        if (!rs.has(COMMENT)) ignoreFields.add(ShopStore.CashDrawerMovementTable.COMMENT);

        return new CashDrawerMovementModel(
                rs.getString(ID),
                rs.getString(SHIT_ID),
                rs.getString(MANAGER_ID),
                _enum(MovementType.class, rs.getString(TYPE), null),
                rs.getBigDecimal(AMOUNT),
                rs.getDate(MOVEMENT_TIME),
                rs.getString(COMMENT),
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
        return ShopStore.CashDrawerMovementTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(CashDrawerMovementModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(SHIT_ID, model.shiftGuid)
                    .put(MANAGER_ID, model.managerGuid)
                    .put(TYPE, model.type)
                    .put(AMOUNT, model.amount)
                    .put(MOVEMENT_TIME, model.time)
                    .put(COMMENT, model.comment);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(CashDrawerMovementModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(SHIT_ID, model.shiftGuid)
                .add(MANAGER_ID, model.managerGuid)
                .add(TYPE, model.type)
                .add(AMOUNT, model.amount)
                .add(MOVEMENT_TIME, model.time)
                .add(COMMENT, model.comment)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(CashDrawerMovementModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(SHIT_ID, model.shiftGuid)
                .add(MANAGER_ID, model.managerGuid)
                .add(TYPE, model.type)
                .add(AMOUNT, model.amount)
                .add(MOVEMENT_TIME, model.time)
                .add(COMMENT, model.comment)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }

}
