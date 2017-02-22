package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.TipsModel;
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
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by pkabakov on 16.05.2014.
 */
public class TipsJdbcConverter extends JdbcConverter<TipsModel> {

    public static final String TABLE_NAME = "RECEIVED_TIPS";

    private static final String ID = "ID";
    private static final String PARENT_ID = "PARENT_ID";
    private static final String EMPLOYEE_ID = "EMPLOYEE_ID";
    private static final String SHIFT_ID = "SHIFT_ID";
    private static final String ORDER_ID = "ORDER_ID";
    private static final String PAYMENT_TRANSACTION_ID = "PAYMENT_TRANSACTION_ID";
    private static final String CREATE_TIME = "CREATE_TIME";
    private static final String AMOUNT = "AMOUNT";
    private static final String COMMENT = "COMMENT";
    private static final String PAYMENT_TYPE = "PAYMENT_TYPE";

    @Override
    public TipsModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.EmployeeTipsTable.GUID);
        if (!rs.has(PARENT_ID)) ignoreFields.add(ShopStore.EmployeeTipsTable.PARENT_GUID);
        if (!rs.has(EMPLOYEE_ID)) ignoreFields.add(ShopStore.EmployeeTipsTable.EMPLOYEE_ID);
        if (!rs.has(SHIFT_ID)) ignoreFields.add(ShopStore.EmployeeTipsTable.SHIFT_ID);
        if (!rs.has(ORDER_ID)) ignoreFields.add(ShopStore.EmployeeTipsTable.ORDER_ID);
        if (!rs.has(PAYMENT_TRANSACTION_ID)) ignoreFields.add(ShopStore.EmployeeTipsTable.PAYMENT_TRANSACTION_ID);
        if (!rs.has(CREATE_TIME)) ignoreFields.add(ShopStore.EmployeeTipsTable.CREATE_TIME);
        if (!rs.has(AMOUNT)) ignoreFields.add(ShopStore.EmployeeTipsTable.AMOUNT);
        if (!rs.has(COMMENT)) ignoreFields.add(ShopStore.EmployeeTipsTable.COMMENT);
        if (!rs.has(PAYMENT_TYPE)) ignoreFields.add(ShopStore.EmployeeTipsTable.PAYMENT_TYPE);

        return new TipsModel(
                rs.getString(ID),
                rs.getString(PARENT_ID),
                rs.getString(EMPLOYEE_ID),
                rs.getString(SHIFT_ID),
                rs.getString(ORDER_ID),
                rs.getString(PAYMENT_TRANSACTION_ID),
                rs.getDate(CREATE_TIME),
                rs.getBigDecimal(AMOUNT),
                rs.getString(COMMENT),
                _enum(TipsModel.PaymentType.class, rs.getString(PAYMENT_TYPE), TipsModel.PaymentType.CASH),
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
        return ShopStore.EmployeeTipsTable.GUID;
    }

    @Override
    public String getParentGuidColumn() {
        return PARENT_ID;
    }

    @Override
    public JSONObject getJSONObject(TipsModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.id)
                    .put(PARENT_ID, model.parentId)
                    .put(EMPLOYEE_ID, model.employeeId)
                    .put(SHIFT_ID, model.shiftId)
                    .put(ORDER_ID, model.orderId)
                    .put(PAYMENT_TRANSACTION_ID, model.paymentTransactionId)
                    .put(CREATE_TIME, model.createTime)
                    .put(AMOUNT, model.amount)
                    .put(COMMENT, model.comment)
                    .put(PAYMENT_TYPE, model.paymentType);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(TipsModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.id)
                .add(PARENT_ID, model.parentId)
                .add(EMPLOYEE_ID, model.employeeId)
                .add(SHIFT_ID, model.shiftId)
                .add(ORDER_ID, model.orderId)
                .add(PAYMENT_TRANSACTION_ID, model.paymentTransactionId)
                .add(CREATE_TIME, model.createTime)
                .add(AMOUNT, model.amount)
                .add(COMMENT, model.comment)
                .add(PAYMENT_TYPE, model.paymentType)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(TipsModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}
