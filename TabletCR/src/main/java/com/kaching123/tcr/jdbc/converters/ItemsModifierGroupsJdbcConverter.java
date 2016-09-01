package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ModifierGroupModel;
import com.kaching123.tcr.model.payment.ModifierGroupCondition;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by gdubina on 06/11/13.
 */
public class ItemsModifierGroupsJdbcConverter extends JdbcConverter<ModifierGroupModel> implements IOrderNumUpdater{

    private static final String TABLE_NAME = "ITEM_MODIFIER_GROUP";

    private static final String GUID = "GUID";
    private static final String ITEM_GUID = "ITEM_GUID";
    private static final String GROUP_NAME = "GROUP_NAME";
    private static final String ORDER_NUM = "ORDER_NUM";
    private static final String CONDITION = "PARAMETER_AMOUNT_SELECTED";
    private static final String CONDITION_VALUE = "AMOUNT_SELECTED";

    @Override
    public ModifierGroupModel toValues(JdbcJSONObject rs) throws JSONException {
        return new ModifierGroupModel(
                rs.getString(GUID),
                rs.getString(ITEM_GUID),
                rs.getString(GROUP_NAME),
                rs.getInt(ORDER_NUM),
                ModifierGroupCondition.valueOf(rs.getInt(CONDITION)),
                rs.getInt(CONDITION_VALUE));
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
}