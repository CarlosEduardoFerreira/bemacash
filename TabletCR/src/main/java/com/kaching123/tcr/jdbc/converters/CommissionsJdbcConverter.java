package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CommissionsModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;

/**
 * Created by pkabakov on 09.07.2014.
 */
public class CommissionsJdbcConverter extends JdbcConverter<CommissionsModel> {

    public static final String TABLE_NAME = "COMMISSION";

    private static final String ID = "ID";
    private static final String EMPLOYEE_ID = "EMPLOYEE_ID";
    private static final String SHIFT_ID = "SHIFT_ID";
    private static final String ORDER_ID = "ORDER_ID";
    private static final String CREATE_TIME = "CREATE_TIME";
    private static final String AMOUNT = "AMOUNT";

    @Override
    public CommissionsModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.EmployeeCommissionsTable.GUID);
        if (!rs.has(EMPLOYEE_ID)) ignoreFields.add(ShopStore.EmployeeCommissionsTable.EMPLOYEE_ID);
        if (!rs.has(SHIFT_ID)) ignoreFields.add(ShopStore.EmployeeCommissionsTable.SHIFT_ID);
        if (!rs.has(ORDER_ID)) ignoreFields.add(ShopStore.EmployeeCommissionsTable.ORDER_ID);
        if (!rs.has(CREATE_TIME)) ignoreFields.add(ShopStore.EmployeeCommissionsTable.CREATE_TIME);
        if (!rs.has(AMOUNT)) ignoreFields.add(ShopStore.EmployeeCommissionsTable.AMOUNT);

        return new CommissionsModel(
                rs.getString(ID),
                rs.getString(EMPLOYEE_ID),
                rs.getString(SHIFT_ID),
                rs.getString(ORDER_ID),
                rs.getDate(CREATE_TIME),
                rs.getBigDecimal(AMOUNT),
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
        return ShopStore.EmployeeCommissionsTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(CommissionsModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.id)
                    .put(EMPLOYEE_ID, model.employeeId)
                    .put(SHIFT_ID, model.shiftId)
                    .put(ORDER_ID, model.orderId)
                    .put(CREATE_TIME, model.createTime)
                    .put(AMOUNT, model);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(CommissionsModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.id)
                .add(EMPLOYEE_ID, model.employeeId)
                .add(SHIFT_ID, model.shiftId)
                .add(ORDER_ID, model.orderId)
                .add(CREATE_TIME, model.createTime)
                .add(AMOUNT, model.amount)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(CommissionsModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
