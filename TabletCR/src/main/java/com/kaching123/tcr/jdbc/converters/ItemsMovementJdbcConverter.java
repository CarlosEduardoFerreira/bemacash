package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ContentValuesUtil;
import com.kaching123.tcr.model.ItemMovementModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcUtil._jdbcDate;

public class ItemsMovementJdbcConverter extends JdbcConverter<ItemMovementModel> {

    public static final String TABLE_NAME = "ITEM_MOVEMENT";

    private static final String MOVEMENT_ID = "MOVEMENT_ID";
    private static final String ITEM_ID = "ITEM_ID";
    private static final String QTY = "QTY";
    private static final String ITEM_UPDATE_QTY_FLAG = "ITEM_UPDATE_QTY_FLAG";
    private static final String OPERATOR_GUID = "OPERATOR_GUID";
    private static final String MANUAL = "MANUAL";
    private static final String CREATE_TIME = "CREATE_TIME";


    @Override
    public ItemMovementModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(MOVEMENT_ID)) ignoreFields.add(ShopStore.ItemMovementTable.GUID);
        if (!rs.has(ITEM_ID)) ignoreFields.add(ShopStore.ItemMovementTable.ITEM_GUID);
        if (!rs.has(ITEM_UPDATE_QTY_FLAG)) ignoreFields.add(ShopStore.ItemMovementTable.ITEM_UPDATE_QTY_FLAG);
        if (!rs.has(QTY)) ignoreFields.add(ShopStore.ItemMovementTable.QTY);
        if (!rs.has(MANUAL)) ignoreFields.add(ShopStore.ItemMovementTable.MANUAL);
        if (!rs.has(OPERATOR_GUID)) ignoreFields.add(ShopStore.ItemMovementTable.OPERATOR_GUID);
        if (!rs.has(CREATE_TIME)) ignoreFields.add(ShopStore.ItemMovementTable.CREATE_TIME);

        return new ItemMovementModel(
                rs.getString(MOVEMENT_ID),
                rs.getString(ITEM_ID),
                rs.getString(ITEM_UPDATE_QTY_FLAG),
                rs.getBigDecimal(QTY, ContentValuesUtil.QUANTITY_SCALE),
                rs.getBoolean(MANUAL),
                rs.getString(OPERATOR_GUID),
                rs.getDate(CREATE_TIME),
                ignoreFields
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
    public String getLocalGuidColumn() {
        return ShopStore.ItemMovementTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(ItemMovementModel item){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(MOVEMENT_ID, item.guid)
                    .put(ITEM_ID, item.itemGuid)
                    .put(ITEM_UPDATE_QTY_FLAG, item.itemUpdateFlag)
                    .put(QTY, item.qty)
                    .put(MANUAL, item.manual)
                    .put(OPERATOR_GUID, item.operatorGuid)
                    .put(CREATE_TIME, item.createTime);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(ItemMovementModel item, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(MOVEMENT_ID, item.guid)
                .add(ITEM_ID, item.itemGuid)
                .add(ITEM_UPDATE_QTY_FLAG, item.itemUpdateFlag)
                .add(QTY, item.qty, ContentValuesUtil.QUANTITY_SCALE)
                .add(MANUAL, item.manual)
                .add(OPERATOR_GUID, item.operatorGuid)
                .add(CREATE_TIME, item.createTime)
                .build(JdbcFactory.getApiMethod(item));
    }

    @Override
    public SingleSqlCommand updateSQL(ItemMovementModel item, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

}
