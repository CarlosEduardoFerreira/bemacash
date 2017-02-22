package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.Unit;
import com.kaching123.tcr.model.Unit.CodeType;
import com.kaching123.tcr.model.Unit.Status;
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
 * Created by mayer
 */
public class UnitsJdbcConverter extends JdbcConverter<Unit> {

    public static final String TABLE_NAME = "UNIT";

    private static final String ID = "ID";
    private static final String ITEM_ID = "ITEM_ID";
    private static final String SALE_ITEM_ID = "SALE_ITEM_ID";
    private static final String SERIAL_CODE = "SERIAL_CODE";
    private static final String CODE_TYPE = "CODE_TYPE";
    private static final String STATUS = "STATUS";
    private static final String WARRANTY_PERIOD = "WARRANTY_PERIOD";
    private static final String SALE_ORDER_ID = "SALE_ORDER_ITEM_ID";
    private static final String CHILD_ORDER_ID = "CHILD_ORDER_ITEM_ID";

    @Override
    public Unit toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.UnitTable.ID);
        if (!rs.has(ITEM_ID)) ignoreFields.add(ShopStore.UnitTable.ITEM_ID);
        if (!rs.has(SALE_ITEM_ID)) ignoreFields.add(ShopStore.UnitTable.SALE_ITEM_ID);
        if (!rs.has(SERIAL_CODE)) ignoreFields.add(ShopStore.UnitTable.SERIAL_CODE);
        if (!rs.has(CODE_TYPE)) ignoreFields.add(ShopStore.UnitTable.CODE_TYPE);
        if (!rs.has(STATUS)) ignoreFields.add(ShopStore.UnitTable.STATUS);
        if (!rs.has(WARRANTY_PERIOD)) ignoreFields.add(ShopStore.UnitTable.WARRANTY_PERIOD);
        if (!rs.has(SALE_ORDER_ID)) ignoreFields.add(ShopStore.UnitTable.SALE_ORDER_ID);
        if (!rs.has(CHILD_ORDER_ID)) ignoreFields.add(ShopStore.UnitTable.CHILD_ORDER_ID);

        return new Unit(
                rs.getString(ID),
                rs.getString(ITEM_ID),
                rs.getString(SALE_ITEM_ID),
                rs.getString(SERIAL_CODE),
                _enum(CodeType.class, rs.getString(CODE_TYPE), CodeType.SN),
                _enum(Status.class, rs.getString(STATUS), Status.NEW),
                rs.getInt(WARRANTY_PERIOD),
                rs.getString(SALE_ORDER_ID),
                rs.getString(CHILD_ORDER_ID),
                null
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
        return ShopStore.UnitTable.ID;
    }

    @Override
    public JSONObject getJSONObject(Unit model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(SALE_ITEM_ID, model.saleItemId)
                    .put(SERIAL_CODE, model.serialCode)
                    .put(CODE_TYPE, model.codeType)
                    .put(STATUS, model.status)
                    .put(ITEM_ID, model.itemId)
                    .put(WARRANTY_PERIOD, model.warrantyPeriod)
                    .put(SALE_ORDER_ID, model.orderId)
                    .put(CHILD_ORDER_ID, model.childOrderId);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(Unit model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(SERIAL_CODE, model.serialCode)
                .add(CODE_TYPE, model.codeType)
                .add(STATUS, model.status)
                .add(ITEM_ID, model.itemId)
                .add(SALE_ITEM_ID, model.saleItemId)
                .add(WARRANTY_PERIOD, model.warrantyPeriod)
                .add(SALE_ORDER_ID, model.orderId)
                .add(CHILD_ORDER_ID, model.childOrderId)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand deleteSQL(Unit model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(JdbcBuilder.FIELD_IS_DELETED, 1)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(Unit model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(STATUS, model.status)
                .add(SERIAL_CODE, model.serialCode)
                .add(SALE_ORDER_ID, model.orderId)
                .add(SALE_ITEM_ID, model.saleItemId)
                .add(WARRANTY_PERIOD, model.warrantyPeriod)
                .add(CHILD_ORDER_ID, model.childOrderId)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public SingleSqlCommand removeFromOrder(String orderId, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(SALE_ORDER_ID, (String) null)
                .add(SALE_ITEM_ID, (String) null)
                .where(SALE_ORDER_ID, orderId)
                .build(JdbcFactory.getApiMethod(Unit.class));
    }

    public SingleSqlCommand removeItemFromOrder(String orderId, String itemId, String saleItemId, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(SALE_ORDER_ID, (String) null)
                .add(SALE_ITEM_ID, (String) null)
                .where(SALE_ORDER_ID, orderId)
                .where(ITEM_ID, itemId)
                .where(SALE_ITEM_ID, saleItemId)
                .build(JdbcFactory.getApiMethod(Unit.class));
    }

    public SingleSqlCommand setSold(String orderId, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(STATUS, Status.SOLD)
                .add(CHILD_ORDER_ID, (String) null)
                .where(SALE_ORDER_ID, orderId)
                .build(JdbcFactory.getApiMethod(Unit.class));
    }

    public SingleSqlCommand updateWarranty(String itemId, int warranty, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(WARRANTY_PERIOD, warranty)
                .where(ITEM_ID, itemId)
                .where(STATUS, Status.NEW.name())
                .build(JdbcFactory.getApiMethod(Unit.class));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }

}
