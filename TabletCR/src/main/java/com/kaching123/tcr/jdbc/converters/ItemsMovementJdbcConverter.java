package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcUtil._jdbcDate;

public class ItemsMovementJdbcConverter extends JdbcConverter<ItemMovementModel> {

    private static final String TABLE_NAME = "ITEM_MOVEMENT";

    private static final String MOVEMENT_ID = "MOVEMENT_ID";
    private static final String ITEM_ID = "ITEM_ID";
    private static final String QTY = "QTY";
    private static final String ITEM_UPDATE_QTY_FLAG = "ITEM_UPDATE_QTY_FLAG";
    private static final String MANUAL = "MANUAL";
    private static final String CREATE_TIME = "CREATE_TIME";


    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        ItemMovementModel model = new ItemMovementModel(
                rs.getString(MOVEMENT_ID),
                rs.getString(ITEM_ID),
                rs.getString(ITEM_UPDATE_QTY_FLAG),
                rs.getBigDecimal(QTY),
                rs.getBoolean(MANUAL),
                _jdbcDate(rs.getTimestamp(CREATE_TIME))
        );
        return model.toValues();
    }

    @Override
    public ItemMovementModel toValues(JdbcJSONObject rs) throws JSONException {
        return new ItemMovementModel(
                rs.getString(MOVEMENT_ID),
                rs.getString(ITEM_ID),
                rs.getString(ITEM_UPDATE_QTY_FLAG),
                rs.getBigDecimal(QTY, ContentValuesUtil.QUANTITY_SCALE),
                rs.getBoolean(MANUAL),
                rs.getDate(CREATE_TIME)
        );
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return MOVEMENT_ID;
    }

    @Override
    public SingleSqlCommand insertSQL(ItemMovementModel item, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(MOVEMENT_ID, item.guid)
                .add(ITEM_ID, item.itemGuid)
                .add(ITEM_UPDATE_QTY_FLAG, item.itemUpdateFlag)
                .add(QTY, item.qty, ContentValuesUtil.QUANTITY_SCALE)
                .add(MANUAL, item.manual)
                .add(CREATE_TIME, item.createTime)
                .build(JdbcFactory.getApiMethod(item));
    }

    @Override
    public SingleSqlCommand updateSQL(ItemMovementModel item, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

}
