package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.jdbc.JdbcUtil._jdbcDate;

/**
 * Created by pkabakov on 10.02.14.
 */
public class CustomerJdbcConverter extends JdbcConverter<CustomerModel> {

    private static final String TABLE_NAME = "CUSTOMER";

    private static final String GUID = "ID";
    private static final String FIRST_NAME = "FIRST_NAME";
    private static final String LAST_NAME = "LAST_NAME";
    private static final String STREET = "STREET";
    private static final String COMPLEMENTARY = "COMPLEMENTARY";
    private static final String CITY = "CITY";
    private static final String STATE = "STATE";
    private static final String COUNTRY = "COUNTRY";
    private static final String ZIP = "ZIP";
    private static final String EMAIL = "EMAIL";
    private static final String PHONE = "PHONE";
    private static final String SEX = "SEX";
    private static final String CREATE_DATE = "CREATE_DATE";
    private static final String CONSENT_EMAIL = "CONSENT_EMAIL";
    private static final String NOTES = "NOTES";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new CustomerModel(
                rs.getString(GUID),
                rs.getString(FIRST_NAME),
                rs.getString(LAST_NAME),
                rs.getString(STREET),
                rs.getString(COMPLEMENTARY),
                rs.getString(CITY),
                rs.getString(STATE),
                rs.getString(COUNTRY),
                rs.getString(ZIP),
                rs.getString(EMAIL),
                rs.getString(PHONE),
                rs.getBoolean(SEX),
                _jdbcDate(rs.getTimestamp(CREATE_DATE)),
                rs.getBoolean(CONSENT_EMAIL),
                rs.getString(NOTES))
                .toValues();
    }

    @Override
    public CustomerModel toValues(JdbcJSONObject rs) throws JSONException {
        return new CustomerModel(
                rs.getString(GUID),
                rs.getString(FIRST_NAME),
                rs.getString(LAST_NAME),
                rs.getString(STREET),
                rs.getString(COMPLEMENTARY),
                rs.getString(CITY),
                rs.getString(STATE),
                rs.getString(COUNTRY),
                rs.getString(ZIP),
                rs.getString(EMAIL),
                rs.getString(PHONE),
                rs.getBoolean(SEX),
                rs.getDate(CREATE_DATE),
                rs.getBoolean(CONSENT_EMAIL),
                rs.getString(NOTES));
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
    public SingleSqlCommand insertSQL(CustomerModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(GUID, model.guid)
                .add(FIRST_NAME, model.firstName)
                .add(LAST_NAME, model.lastName)
                .add(STREET, model.street)
                .add(COMPLEMENTARY, model.complementary)
                .add(CITY, model.city)
                .add(STATE, model.state)
                .add(COUNTRY, model.country)
                .add(ZIP, model.zip)
                .add(EMAIL, model.email)
                .add(PHONE, model.phone)
                .add(SEX, model.sex)
                .add(CREATE_DATE, model.createTime)
                .add(CONSENT_EMAIL, model.consentPromotions)
                .add(NOTES, model.notes)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(CustomerModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(FIRST_NAME, model.firstName)
                .add(LAST_NAME, model.lastName)
                .add(STREET, model.street)
                .add(COMPLEMENTARY, model.complementary)
                .add(CITY, model.city)
                .add(STATE, model.state)
                .add(COUNTRY, model.country)
                .add(ZIP, model.zip)
                .add(EMAIL, model.email)
                .add(PHONE, model.phone)
                .add(SEX, model.sex)
                .add(CONSENT_EMAIL, model.consentPromotions)
                .add(NOTES, model.notes)
                .where(GUID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

}
