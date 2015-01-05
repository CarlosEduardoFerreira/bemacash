package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CommissionsModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcUtil._jdbcDate;

/**
 * Created by pkabakov on 09.07.2014.
 */
public class CommissionsJdbcConverter extends JdbcConverter<CommissionsModel> {

    private static final String TABLE_NAME = "COMMISSION";

    private static final String ID = "ID";
    private static final String EMPLOYEE_ID = "EMPLOYEE_ID";
    private static final String SHIFT_ID = "SHIFT_ID";
    private static final String ORDER_ID = "ORDER_ID";
    private static final String CREATE_TIME = "CREATE_TIME";
    private static final String AMOUNT = "AMOUNT";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new CommissionsModel(
                rs.getString(ID),
                rs.getString(EMPLOYEE_ID),
                rs.getString(SHIFT_ID),
                rs.getString(ORDER_ID),
                _jdbcDate(rs.getTimestamp(CREATE_TIME)),
                rs.getBigDecimal(AMOUNT)
        ).toValues();
    }

    @Override
    public CommissionsModel toValues(JdbcJSONObject rs) throws JSONException {
        return new CommissionsModel(
                rs.getString(ID),
                rs.getString(EMPLOYEE_ID),
                rs.getString(SHIFT_ID),
                rs.getString(ORDER_ID),
                rs.getDate(CREATE_TIME),
                rs.getBigDecimal(AMOUNT)
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
}
