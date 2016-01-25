package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;
import android.text.TextUtils;

import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeeStatus;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by gdubina on 08/11/13.
 */
public class EmployeeJdbcConverter extends JdbcConverter<EmployeeModel> {

    private static final String TABLE_NAME = "EMPLOYEE";
    private static final String FULLNAME = "FULLNAME"; //TODO delete when web team makes changes

    private static final String GUID = "ID";
    private static final String FIRST_NAME = "FIRST_NAME";
    private static final String LAST_NAME = "LAST_NAME";
    private static final String LOGIN = "LOGIN";
    private static final String PASSWORD = "PASSWORD";
    private static final String STREET = "STREET";
    private static final String COMPLEMENTARY = "COMPLEMENTARY";
    private static final String CITY = "CITY";
    private static final String STATE = "STATE";
    private static final String COUNTRY = "COUNTRY";
    private static final String ZIP = "ZIP";
    private static final String PHONE = "PHONE";
    private static final String EMAIL = "EMAIL";
    private static final String SEX = "SEX";
    private static final String HIRE_DATE = "HIRE_DATE";
    private static final String FIRE_DATE = "FIRE_DATE";
    private static final String STATUS = "STATUS";
    private static final String SHOP_ID = "SHOP_ID";
    private static final String HOURLY_RATE = "HOURLY_RATE";
    private static final String TIPS_ELIGIBLE = "TIPS_ELIGIBLE";
    private static final String ELIGIBLE_FOR_COMMISSION = "ELIGIBLE_FOR_COMMISSION";
    private static final String COMMISSION = "COMMISSION";
    private static final String RESELLER_ID = "RESELLER_ID";
    private static final String IS_SYNC = "IS_SYNC";
    private static final String ITEMS_COUNT = "ITEMS_COUNT";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return toModel(rs).toValues();
    }

    @Override
    public EmployeeModel toValues(JdbcJSONObject rs) throws JSONException {
        return new EmployeeModel(
                rs.getString(GUID),
                rs.getString(FIRST_NAME),
                rs.getString(LAST_NAME),
                rs.getString(LOGIN),
                rs.getString(PASSWORD),
                rs.getString(STREET),
                rs.getString(COMPLEMENTARY),
                rs.getString(CITY),
                rs.getString(STATE),
                rs.getString(COUNTRY),
                rs.getString(ZIP),
                rs.getString(PHONE),
                rs.getString(EMAIL),
                rs.getBoolean(SEX),
                rs.getDate(HIRE_DATE),
                rs.getDate(FIRE_DATE),
                _enum(EmployeeStatus.class, rs.getString(STATUS), EmployeeStatus.BLOCKED),
                rs.getLong(SHOP_ID),
                rs.getBigDecimal(HOURLY_RATE),
                rs.getBoolean(TIPS_ELIGIBLE),
                rs.getBoolean(ELIGIBLE_FOR_COMMISSION),
                rs.getBigDecimal(COMMISSION),
                !rs.isNull(RESELLER_ID),
                rs.getBoolean(IS_SYNC)
        );
    }

    public EmployeeModel toModel(ResultSet rs) throws SQLException {
        return new EmployeeModel(
                rs.getString(GUID),
                rs.getString(FIRST_NAME),
                rs.getString(LAST_NAME),
                rs.getString(LOGIN),
                rs.getString(PASSWORD),
                rs.getString(STREET),
                rs.getString(COMPLEMENTARY),
                rs.getString(CITY),
                rs.getString(STATE),
                rs.getString(COUNTRY),
                rs.getString(ZIP),
                rs.getString(PHONE),
                rs.getString(EMAIL),
                rs.getBoolean(SEX),
                rs.getDate(HIRE_DATE),
                rs.getDate(FIRE_DATE),
                _enum(EmployeeStatus.class, rs.getString(STATUS), EmployeeStatus.BLOCKED),
                rs.getLong(SHOP_ID),
                rs.getBigDecimal(HOURLY_RATE),
                rs.getBoolean(TIPS_ELIGIBLE),
                rs.getBoolean(ELIGIBLE_FOR_COMMISSION),
                rs.getBigDecimal(COMMISSION),
                !TextUtils.isEmpty(rs.getString(RESELLER_ID)),
                rs.getBoolean(IS_SYNC)
        );
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
    public SingleSqlCommand insertSQL(EmployeeModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(GUID, model.guid)
                .add(FIRST_NAME, model.firstName)
                .add(LAST_NAME, model.lastName)
                .add(FULLNAME, model.fullName()) //TODO delete when web team makes changes
                .add(LOGIN, model.login)
                .add(PASSWORD, model.password)

                .add(STREET, model.street)
                .add(COMPLEMENTARY, model.complementary)
                .add(CITY, model.city)
                .add(STATE, model.state)
                .add(COUNTRY, model.country)
                .add(ZIP, model.zip)
                .add(PHONE, model.phone)
                .add(EMAIL, model.email)

                .add(SEX, model.sexMale)

                .add(HIRE_DATE, model.hireDate)
                .add(FIRE_DATE, model.fireDate)
                .add(HOURLY_RATE, model.hRate)
                .add(STATUS, model.status)
                .add(TIPS_ELIGIBLE, model.tipsEligible)
                .add(ELIGIBLE_FOR_COMMISSION, model.commissionEligible)
                .add(COMMISSION, model.commission)
                .add(IS_SYNC, model.isSynced)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(EmployeeModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(FIRST_NAME, model.firstName)
                .add(LAST_NAME, model.lastName)
                .add(FULLNAME, model.fullName()) //TODO delete when web team makes changes
                .add(LOGIN, model.login)
                .add(PASSWORD, model.password)

                .add(STREET, model.street)
                .add(COMPLEMENTARY, model.complementary)
                .add(CITY, model.city)
                .add(STATE, model.state)
                .add(COUNTRY, model.country)
                .add(ZIP, model.zip)
                .add(PHONE, model.phone)
                .add(EMAIL, model.email)

                .add(SEX, model.sexMale)

                .add(HIRE_DATE, model.hireDate)
                .add(FIRE_DATE, model.fireDate)
                .add(STATUS, model.status)
                .add(HOURLY_RATE, model.hRate)
                .add(TIPS_ELIGIBLE, model.tipsEligible)
                .add(ELIGIBLE_FOR_COMMISSION, model.commissionEligible)
                .add(COMMISSION, model.commission)
                .add(IS_SYNC, model.isSynced)
                .where(GUID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public static SingleSqlCommand updateEmployeeNoCreSQL(EmployeeModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(FIRST_NAME, model.firstName)
                .add(LAST_NAME, model.lastName)
                .add(FULLNAME, model.fullName()) //TODO delete when web team makes changes

                .add(STREET, model.street)
                .add(COMPLEMENTARY, model.complementary)
                .add(CITY, model.city)
                .add(STATE, model.state)
                .add(COUNTRY, model.country)
                .add(ZIP, model.zip)
                .add(PHONE, model.phone)
                .add(EMAIL, model.email)

                .add(SEX, model.sexMale)

                .add(HIRE_DATE, model.hireDate)
                .add(FIRE_DATE, model.fireDate)
                .add(STATUS, model.status)
                .add(HOURLY_RATE, model.hRate)
                .add(TIPS_ELIGIBLE, model.tipsEligible)
                .add(ELIGIBLE_FOR_COMMISSION, model.commissionEligible)
                .add(COMMISSION, model.commission)
                .add(IS_SYNC, model.isSynced)
                .where(GUID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    public static String getLoginQuery() {
        return String.format(Locale.US,
                "select * from %s where %s = ? and %s = ? and %s = ?",
                TABLE_NAME, JdbcBuilder.FIELD_SHOP_ID, LOGIN, PASSWORD
        );
    }

    public static String getLoginQuery2() {
        return String.format(Locale.US,
                "select * from %s as e INNER JOIN %s as r ON r.%s = e.%s where e.%s = ? and e.%s = ? and r.%s = ? and r.%s <> ?",
                TABLE_NAME,
                RegisterJdbcConverter.TABLE_NAME,
                JdbcBuilder.FIELD_SHOP_ID,
                JdbcBuilder.FIELD_SHOP_ID,
                LOGIN, PASSWORD,
                RegisterJdbcConverter.REGISTER_SERIAL, RegisterJdbcConverter.STATUS
        );
    }
}
