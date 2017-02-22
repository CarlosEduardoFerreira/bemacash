package com.kaching123.tcr.model.converter;

import com.telly.groundy.PublicGroundyTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.TableHistoryModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.TableHistoryTable;
import com.kaching123.tcr.util.JdbcJSONObject;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.util.ContentValuesUtilBase._enum;

/**
 * Created by Rodrigo Busata on 07/22/16.
 */
public class TableHistoryJdbcConverter extends JdbcConverter<TableHistoryModel> {

    public static final String TABLE_NAME = "TABLE_HISTORY";

    private static final String ID = "ID";
    private static final String TABLE_ID = "TABLE_ID";
    private static final String STATUS = "STATUS";
    private static final String STATUS_START_TIME = "STATUS_TIME";
    private static final String STATUS_END_STATUS_TIME = "STATUS_END_STATUS_TIME";

    @Override
    public TableHistoryModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.TableHistoryTable.GUID);
        if (!rs.has(TABLE_ID)) ignoreFields.add(TableHistoryTable.TABLE_GUID);
        if (!rs.has(STATUS)) ignoreFields.add(TableHistoryTable.STATUS);
        if (!rs.has(STATUS_START_TIME)) ignoreFields.add(ShopStore.TableHistoryTable.STATUS_START_TIME);
        if (!rs.has(STATUS_END_STATUS_TIME)) ignoreFields.add(TableHistoryTable.STATUS_END_TIME);

        return new TableHistoryModel(
                rs.getString(ID),
                rs.getString(TABLE_ID),
                rs.getDate(STATUS_START_TIME),
                rs.getDate(STATUS_END_STATUS_TIME),
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
        return TableHistoryTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(TableHistoryModel model) {
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(TABLE_ID, model.tableGuid)
                    .put(STATUS_START_TIME, model.startTime)
                    .put(STATUS_END_STATUS_TIME, model.endTime);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(TableHistoryModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        return _insert(getShopId(), TABLE_NAME)
                .add(ID, model.guid)
                .add(TABLE_ID, model.tableGuid)
                .add(STATUS_START_TIME, model.startTime)
                .add(STATUS_END_STATUS_TIME, model.endTime)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(TableHistoryModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}