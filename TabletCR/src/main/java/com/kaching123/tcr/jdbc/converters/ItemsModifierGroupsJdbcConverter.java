package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.payment.ModifierGroupCondition;
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

/**
 * Created by gdubina on 06/11/13.
 */
public class ItemsModifierGroupsJdbcConverter extends JdbcConverter<ModifierGroupModel> implements IOrderNumUpdater{

    public static final String TABLE_NAME = "ITEM_MODIFIER_GROUP";

    private static final String GUID = "GUID";
    private static final String ITEM_GUID = "ITEM_GUID";
    private static final String GROUP_NAME = "GROUP_NAME";
    private static final String ORDER_NUM = "ORDER_NUM";
    private static final String CONDITION = "PARAMETER_AMOUNT_SELECTED";
    private static final String CONDITION_VALUE = "AMOUNT_SELECTED";

    @Override
    public ModifierGroupModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(GUID)) ignoreFields.add(ShopStore.ModifierGroupTable.GUID);
        if (!rs.has(ITEM_GUID)) ignoreFields.add(ShopStore.ModifierGroupTable.ITEM_GUID);
        if (!rs.has(GROUP_NAME)) ignoreFields.add(ShopStore.ModifierGroupTable.TITLE);
        if (!rs.has(ORDER_NUM)) ignoreFields.add(ShopStore.ModifierGroupTable.ORDER_NUM);
        if (!rs.has(CONDITION)) ignoreFields.add(ShopStore.ModifierGroupTable.CONDITION);
        if (!rs.has(CONDITION_VALUE)) ignoreFields.add(ShopStore.ModifierGroupTable.CONDITION_VALUE);

        return new ModifierGroupModel(
                rs.getString(GUID),
                rs.getString(ITEM_GUID),
                rs.getString(GROUP_NAME),
                rs.getInt(ORDER_NUM),
                ModifierGroupCondition.valueOf(rs.getInt(CONDITION)),
                rs.getInt(CONDITION_VALUE),
                ignoreFields);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return GUID;
    }

    @Override
    public String getLocalGuidColumn() {
        return ShopStore.ModifierGroupTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(ModifierGroupModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(GUID, model.guid)
                    .put(ITEM_GUID, model.itemGuid)
                    .put(GROUP_NAME, model.title)
                    .put(ORDER_NUM, model.orderNum)
                    .put(CONDITION, model.condition)
                    .put(CONDITION_VALUE, model.conditionValue);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(ModifierGroupModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(GUID, model.guid)
                .add(ITEM_GUID, model.itemGuid)
                .add(GROUP_NAME, model.title)
                .add(ORDER_NUM, model.orderNum)
                .add(CONDITION, model.condition.ordinal())
                .add(CONDITION_VALUE, model.conditionValue)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(ModifierGroupModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(ITEM_GUID, model.itemGuid)
                .add(GROUP_NAME, model.title)
                .add(ORDER_NUM, model.orderNum)
                .add(CONDITION, model.condition.ordinal())
                .add(CONDITION_VALUE, model.conditionValue)
                .where(GUID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateOrderNum(String id, int orderNum, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(ORDER_NUM, orderNum)
                .where(GUID, id)
                .build(JdbcFactory.getApiMethod(ModifierGroupModel.class));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}