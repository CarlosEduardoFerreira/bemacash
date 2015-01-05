package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CreditReceiptModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcUtil._jdbcDate;

/**
 * Created by gdubina on 24/02/14.
 */
public class CreditReceiptJdbcConverter extends JdbcConverter<CreditReceiptModel> {

    public static final String TABLE_NAME = "CREDIT_RECEIPT";

    private static final String ID = "ID";
    private static final String CASHIER_ID = "CASHIER_ID";
    private static final String REGISTER_ID = "REGISTER_ID";
    private static final String SHIFT_ID = "SHIFT_ID";
    private static final String CREATE_TIME = "CREATE_TIME";
    private static final String AMOUNT = "AMOUNT";
    private static final String PRINT_NUMBER = "PRINT_NUMBER";
    private static final String EXPIRE_TIME = "EXPIRE_TIME";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new CreditReceiptModel(
                rs.getString(ID),
                rs.getString(CASHIER_ID),
                rs.getLong(REGISTER_ID),
                rs.getString(SHIFT_ID),
                _jdbcDate(rs.getTimestamp(CREATE_TIME)),
                rs.getBigDecimal(AMOUNT),
                rs.getLong(PRINT_NUMBER),
                rs.getInt(EXPIRE_TIME)
        ).toValues();
    }

    @Override
    public CreditReceiptModel toValues(JdbcJSONObject rs) throws JSONException {
        return new CreditReceiptModel(
                rs.getString(ID),
                rs.getString(CASHIER_ID),
                rs.getLong(REGISTER_ID),
                rs.getString(SHIFT_ID),
                rs.getDate(CREATE_TIME),
                rs.getBigDecimal(AMOUNT),
                rs.getLong(PRINT_NUMBER),
                rs.getInt(EXPIRE_TIME)
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
    public SingleSqlCommand insertSQL(CreditReceiptModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(CASHIER_ID, model.cashierGuid)
                .add(REGISTER_ID, model.registerId)
                .add(SHIFT_ID, model.shiftId)
                .add(CREATE_TIME, model.createTime)
                .add(AMOUNT, model.amount)
                .add(PRINT_NUMBER, model.printNumber)
                .add(EXPIRE_TIME, model.expireTime)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(CreditReceiptModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand deleteSQL(CreditReceiptModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}
