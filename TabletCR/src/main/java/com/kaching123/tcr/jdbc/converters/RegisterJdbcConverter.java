package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.RegisterModel;
import com.kaching123.tcr.model.RegisterModel.RegisterStatus;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by pkabakov on 21.01.14.
 */
public class RegisterJdbcConverter extends JdbcConverter<RegisterModel> {

    public static final String TABLE_NAME = "REGISTER";

    private static final String ID = "ID";
    public static final String REGISTER_SERIAL = "REGISTER_SERIAL";
    private static final String TITLE = "TITLE";
    public static final String STATUS = "STATUS";
    public static final String PREPAID_TID = "PREPAID_TID";
    public static final String BLACKSTONE_PAYMENT_CID = "BLACKSTONE_PAYMENT_CID";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new RegisterModel(
                rs.getLong(ID),
                rs.getString(REGISTER_SERIAL),
                rs.getString(TITLE),
                _enum(RegisterStatus.class, rs.getString(STATUS), RegisterStatus.ACTIVE),
                rs.getInt(PREPAID_TID),
                rs.getInt(BLACKSTONE_PAYMENT_CID)
        ).toValues();
    }

    @Override
    public RegisterModel toValues(JdbcJSONObject rs) throws JSONException {
        return new RegisterModel(
                rs.getLong(ID),
                rs.getString(REGISTER_SERIAL),
                rs.getString(TITLE),
                _enum(RegisterStatus.class, rs.getString(STATUS), RegisterStatus.ACTIVE),
                rs.optInt(PREPAID_TID),
                rs.optInt(BLACKSTONE_PAYMENT_CID)
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
    public SingleSqlCommand insertSQL(RegisterModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(RegisterModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(TITLE, model.title)
                .add(STATUS, model.status)
                .where(ID, model.id)
                .build(JdbcFactory.getApiMethod(model));
    }

    public static String getGetShopIdQuery(){
        return String.format(Locale.US,
                "select %s from %s where %s = ? and %s <> ?",
                JdbcBuilder.FIELD_SHOP_ID,
                TABLE_NAME,
                REGISTER_SERIAL,
                STATUS
                );
    }
}
