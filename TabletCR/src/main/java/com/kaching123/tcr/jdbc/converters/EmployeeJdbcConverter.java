package com.kaching123.tcr.jdbc.converters;

import android.text.TextUtils;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.EmployeeModel;
import com.kaching123.tcr.model.EmployeeStatus;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by gdubina on 08/11/13.
 */
public class EmployeeJdbcConverter extends JdbcConverter<EmployeeModel> {

    public static final String TABLE_NAME = "EMPLOYEE";
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

    @Override
    public EmployeeModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(GUID)) ignoreFields.add(ShopStore.EmployeeTable.GUID);
        if (!rs.has(FIRST_NAME)) ignoreFields.add(ShopStore.EmployeeTable.FIRST_NAME);
        if (!rs.has(LAST_NAME)) ignoreFields.add(ShopStore.EmployeeTable.LAST_NAME);
        if (!rs.has(LOGIN)) ignoreFields.add(ShopStore.EmployeeTable.LOGIN);
        if (!rs.has(PASSWORD)) ignoreFields.add(ShopStore.EmployeeTable.PASSWORD);
        if (!rs.has(STREET)) ignoreFields.add(ShopStore.EmployeeTable.STREET);
        if (!rs.has(COMPLEMENTARY)) ignoreFields.add(ShopStore.EmployeeTable.COMPLEMENTARY);
        if (!rs.has(CITY)) ignoreFields.add(ShopStore.EmployeeTable.CITY);
        if (!rs.has(STATE)) ignoreFields.add(ShopStore.EmployeeTable.STATE);
        if (!rs.has(COUNTRY)) ignoreFields.add(ShopStore.EmployeeTable.COUNTRY);
        if (!rs.has(ZIP)) ignoreFields.add(ShopStore.EmployeeTable.ZIP);
        if (!rs.has(PHONE)) ignoreFields.add(ShopStore.EmployeeTable.PHONE);
        if (!rs.has(EMAIL)) ignoreFields.add(ShopStore.EmployeeTable.EMAIL);
        if (!rs.has(SEX)) ignoreFields.add(ShopStore.EmployeeTable.SEX);
        if (!rs.has(HIRE_DATE)) ignoreFields.add(ShopStore.EmployeeTable.HIRE_DATE);
        if (!rs.has(FIRE_DATE)) ignoreFields.add(ShopStore.EmployeeTable.FIRE_DATE);
        if (!rs.has(STATUS)) ignoreFields.add(ShopStore.EmployeeTable.STATUS);
        if (!rs.has(SHOP_ID)) ignoreFields.add(ShopStore.EmployeeTable.SHOP_ID);
        if (!rs.has(HOURLY_RATE)) ignoreFields.add(ShopStore.EmployeeTable.HOURLY_RATE);
        if (!rs.has(TIPS_ELIGIBLE)) ignoreFields.add(ShopStore.EmployeeTable.TIPS_ELIGIBLE);
        if (!rs.has(ELIGIBLE_FOR_COMMISSION)) ignoreFields.add(ShopStore.EmployeeTable.ELIGIBLE_FOR_COMMISSION);
        if (!rs.has(COMMISSION)) ignoreFields.add(ShopStore.EmployeeTable.COMMISSION);
        if (!rs.has(RESELLER_ID)) ignoreFields.add(ShopStore.EmployeeTable.IS_MERCHANT);
        if (!rs.has(IS_SYNC)) ignoreFields.add(ShopStore.EmployeeTable.IS_SYNC);

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
                rs.getBoolean(IS_SYNC),
                ignoreFields
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
    public String getLocalGuidColumn() {
        return ShopStore.EmployeeTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(EmployeeModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(GUID, model.guid)
                    .put(FIRST_NAME, model.firstName)
                    .put(LAST_NAME, model.lastName)
                    .put(FULLNAME, model.fullName())
                    .put(LOGIN, model.login)
                    .put(PASSWORD, model.password)
                    .put(STREET, model.street)
                    .put(COMPLEMENTARY, model.complementary)
                    .put(CITY, model.city)
                    .put(STATE, model.state)
                    .put(COUNTRY, model.country)
                    .put(ZIP, model.zip)
                    .put(PHONE, model.phone)
                    .put(EMAIL, model.email)
                    .put(SEX, model.sexMale)
                    .put(HIRE_DATE, model.hireDate)
                    .put(FIRE_DATE, model.fireDate)
                    .put(STATUS, model.status)
                    .put(SHOP_ID, model.shopId)
                    .put(HOURLY_RATE, model.hRate)
                    .put(TIPS_ELIGIBLE, model.tipsEligible)
                    .put(ELIGIBLE_FOR_COMMISSION, model.commissionEligible)
                    .put(COMMISSION, model.commission)
                    .put(RESELLER_ID, model.isMerchant)
                    .put(IS_SYNC, model.isSynced);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
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

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
