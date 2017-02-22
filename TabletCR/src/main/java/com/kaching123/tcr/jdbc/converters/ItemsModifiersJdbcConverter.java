package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ModifierModel;
import com.kaching123.tcr.model.ModifierType;
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
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by gdubina on 06/11/13.
 */
public class ItemsModifiersJdbcConverter extends JdbcConverter<ModifierModel> implements IOrderNumUpdater {

    public static final String TABLE_NAME = "ITEM_MODIFIER";

    private static final String MODIFIER_GUID = "MODIFIER_GUID";
    private static final String ITEM_GUID = "ITEM_GUID";
    private static final String TYPE = "TYPE";
    private static final String TITLE = "TITLE";
    private static final String EXTRA_COST = "EXTRA_COST";

    private static final String ITEM_SUB_GUID = "ITEM_SUB_GUID";
    private static final String ITEM_SUB_QUANTITY = "ITEM_SUB_QUANTITY";
    private static final String ITEM_GROUP_GUID = "ITEM_GROUP_GUID";
    private static final String AUTO_APPLY = "AUTO_APPLY";
    private static final String ORDER_NUM = "ORDER_NUM";


    @Override
    public ModifierModel toValues(JdbcJSONObject rs) throws JSONException {

        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(MODIFIER_GUID)) ignoreFields.add(ShopStore.ModifierTable.MODIFIER_GUID);
        if (!rs.has(ITEM_GUID)) ignoreFields.add(ShopStore.ModifierTable.ITEM_GUID);
        if (!rs.has(TYPE)) ignoreFields.add(ShopStore.ModifierTable.TYPE);
        if (!rs.has(TITLE)) ignoreFields.add(ShopStore.ModifierTable.TITLE);
        if (!rs.has(EXTRA_COST)) ignoreFields.add(ShopStore.ModifierTable.EXTRA_COST);
        if (!rs.has(ITEM_SUB_GUID)) ignoreFields.add(ShopStore.ModifierTable.ITEM_SUB_GUID);
        if (!rs.has(ITEM_SUB_QUANTITY)) ignoreFields.add(ShopStore.ModifierTable.ITEM_SUB_QTY);
        if (!rs.has(ITEM_GROUP_GUID)) ignoreFields.add(ShopStore.ModifierTable.ITEM_GROUP_GUID);

        return new ModifierModel(
                rs.getString(MODIFIER_GUID),
                rs.getString(ITEM_GUID),
                _enum(ModifierType.class, rs.getString(TYPE), ModifierType.ADDON),
                rs.getString(TITLE),
                rs.getBigDecimal(EXTRA_COST),
                rs.getString(ITEM_SUB_GUID),
                rs.getBigDecimal(ITEM_SUB_QUANTITY),
                rs.getString(ITEM_GROUP_GUID),
                rs.getBoolean(AUTO_APPLY),
                rs.getInt(ORDER_NUM),
                ignoreFields
        );
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return MODIFIER_GUID;
    }

    @Override
    public String getLocalGuidColumn() {
        return ShopStore.ModifierTable.MODIFIER_GUID;
    }

    @Override
    public JSONObject getJSONObject(ModifierModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(MODIFIER_GUID, model.modifierGuid)
                    .put(ITEM_GUID, model.itemGuid)
                    .put(TYPE, model.type)
                    .put(TITLE, model.title)
                    .put(EXTRA_COST, model.cost)
                    .put(ITEM_SUB_GUID, model.childItemGuid)
                    .put(ITEM_SUB_QUANTITY, model.childItemQty)
                    .put(ITEM_GROUP_GUID, model.modifierGroupGuid)
                    .put(AUTO_APPLY, model.autoApply)
                    .put(ORDER_NUM, model.orderNum);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(ModifierModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(MODIFIER_GUID, model.modifierGuid)
                .add(ITEM_GUID, model.itemGuid)
                .add(TYPE, model.type)
                .add(TITLE, model.title)
                .add(EXTRA_COST, model.cost)
                .add(ITEM_SUB_GUID, model.childItemGuid)
                .add(ITEM_SUB_QUANTITY, model.childItemQty)
                .add(ITEM_GROUP_GUID, model.modifierGroupGuid)
                .add(AUTO_APPLY, model.autoApply)
                .add(ORDER_NUM, model.orderNum)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(ModifierModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(TITLE, model.title)
                .add(EXTRA_COST, model.cost)
                .add(ITEM_SUB_GUID, model.childItemGuid)
                .add(ITEM_SUB_QUANTITY, model.childItemQty)
                .add(ITEM_GROUP_GUID, model.modifierGroupGuid)
                .add(AUTO_APPLY, model.autoApply)
                .add(ORDER_NUM, model.orderNum)
                .where(MODIFIER_GUID, model.modifierGuid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand clearGroups(String guid, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(ITEM_GROUP_GUID, (String)null)
                .where(ITEM_GROUP_GUID, guid)
                .build(JdbcFactory.getApiMethod(new ModifierModel()));
    }

    @Override
    public SingleSqlCommand updateOrderNum(String id, int orderNum, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(ORDER_NUM, orderNum)
                .where(MODIFIER_GUID, id)
                .build(JdbcFactory.getApiMethod(new ModifierModel()));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
