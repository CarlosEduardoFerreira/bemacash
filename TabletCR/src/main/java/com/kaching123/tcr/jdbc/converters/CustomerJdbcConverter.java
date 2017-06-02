package com.kaching123.tcr.jdbc.converters;

import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.CustomerModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.jdbc.JdbcBuilder.FIELD_UPDATE_TIME;
import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by pkabakov on 10.02.14.
 */
public class CustomerJdbcConverter extends JdbcConverter<CustomerModel> {

    public static final String TABLE_NAME = "CUSTOMER";

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
    private static final String BIRTHDAY = "BIRTHDAY";
    private static final String BIRTHDAY_REWARD_APPLY_DATE = "BIRTHDAY_REWARD_APPLY_DATE";
    private static final String CREATE_DATE = "CREATE_DATE";
    private static final String CONSENT_EMAIL = "CONSENT_EMAIL";
    private static final String NOTES = "NOTES";
    private static final String CUSTOMER_IDENTIFICATION = "CUSTOMER_IDENTIFICATION";
    private static final String LOYALTY_PLAN_ID = "LOYALTY_PLAN_ID";
    private static final String TMP_LOYALTY_POINTS = "TMP_LOYALTY_POINTS";
    private static final String LOYALTY_BARCODE = "LOYALTY_BARCODE";

    @Override
    public CustomerModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(GUID)) ignoreFields.add(ShopStore.CustomerTable.GUID);
        if (!rs.has(FIRST_NAME)) ignoreFields.add(ShopStore.CustomerTable.FISRT_NAME);
        if (!rs.has(LAST_NAME)) ignoreFields.add(ShopStore.CustomerTable.LAST_NAME);
        if (!rs.has(STREET)) ignoreFields.add(ShopStore.CustomerTable.STREET);
        if (!rs.has(COMPLEMENTARY)) ignoreFields.add(ShopStore.CustomerTable.COMPLEMENTARY);
        if (!rs.has(CITY)) ignoreFields.add(ShopStore.CustomerTable.CITY);
        if (!rs.has(STATE)) ignoreFields.add(ShopStore.CustomerTable.STATE);
        if (!rs.has(COUNTRY)) ignoreFields.add(ShopStore.CustomerTable.COUNTRY);
        if (!rs.has(ZIP)) ignoreFields.add(ShopStore.CustomerTable.ZIP);
        if (!rs.has(EMAIL)) ignoreFields.add(ShopStore.CustomerTable.EMAIL);
        if (!rs.has(PHONE)) ignoreFields.add(ShopStore.CustomerTable.PHONE);
        if (!rs.has(SEX)) ignoreFields.add(ShopStore.CustomerTable.SEX);
        if (!rs.has(BIRTHDAY)) ignoreFields.add(ShopStore.CustomerTable.BIRTHDAY);
        if (!rs.has(BIRTHDAY_REWARD_APPLY_DATE)) ignoreFields.add(ShopStore.CustomerTable.BIRTHDAY_REWARD_APPLY_DATE);
        if (!rs.has(CREATE_DATE)) ignoreFields.add(ShopStore.CustomerTable.CREATE_TIME);
        if (!rs.has(CONSENT_EMAIL)) ignoreFields.add(ShopStore.CustomerTable.CONSENT_PROMOTIONS);
        if (!rs.has(NOTES)) ignoreFields.add(ShopStore.CustomerTable.NOTES);
        if (!rs.has(CUSTOMER_IDENTIFICATION)) ignoreFields.add(ShopStore.CustomerTable.CUSTOMER_IDENTIFICATION);
        if (!rs.has(LOYALTY_PLAN_ID)) ignoreFields.add(ShopStore.CustomerTable.LOYALTY_PLAN_ID);
        if (!rs.has(TMP_LOYALTY_POINTS)) ignoreFields.add(ShopStore.CustomerTable.TMP_LOYALTY_POINTS);
        if (!rs.has(LOYALTY_BARCODE)) ignoreFields.add(ShopStore.CustomerTable.LOYALTY_BARCODE);
        Log.d("BemaCarl23","CustomerJdbcConverter.toValues.rs.getDate(BIRTHDAY_REWARD_APPLY_DATE): " + rs.getDate(BIRTHDAY_REWARD_APPLY_DATE));
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
                rs.getSimpleDate(BIRTHDAY),
                rs.getDate(BIRTHDAY_REWARD_APPLY_DATE),
                rs.getDate(CREATE_DATE),
                rs.getBoolean(CONSENT_EMAIL),
                rs.getString(NOTES),
                rs.getString(CUSTOMER_IDENTIFICATION),
                rs.getString(LOYALTY_PLAN_ID),
                rs.getBigDecimal(TMP_LOYALTY_POINTS),
                rs.getString(LOYALTY_BARCODE),
                ignoreFields);
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
        return ShopStore.CustomerTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(CustomerModel model){
        JSONObject json = null;
        Log.d("BemaCarl23","CustomerJdbcConverter.getJSONObject.model.birthdayRewardApplyDate: " + model.birthdayRewardApplyDate);
        try {
            json = new JSONObject()
                    .put(GUID, model.guid)
                    .put(FIRST_NAME, model.firstName)
                    .put(LAST_NAME, model.lastName)
                    .put(STREET, model.street)
                    .put(COMPLEMENTARY, model.complementary)
                    .put(CITY, model.city)
                    .put(STATE, model.state)
                    .put(COUNTRY, model.country)
                    .put(ZIP, model.zip)
                    .put(EMAIL, model.email)
                    .put(PHONE, model.phone)
                    .put(SEX, model.sex)
                    .put(BIRTHDAY, model.birthday)
                    .put(BIRTHDAY_REWARD_APPLY_DATE, model.birthdayRewardApplyDate)
                    .put(CREATE_DATE, model.createTime)
                    .put(CONSENT_EMAIL, model.consentPromotions)
                    .put(NOTES, model.notes)
                    .put(CUSTOMER_IDENTIFICATION, model.customerIdentification)
                    .put(LOYALTY_PLAN_ID, model.loyaltyPlanId)
                    .put(TMP_LOYALTY_POINTS, model.loyaltyPoints)
                    .put(LOYALTY_BARCODE, model.loyaltyBarcode);
        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
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
                .add(BIRTHDAY, model.birthday)
                .add(BIRTHDAY_REWARD_APPLY_DATE, model.birthdayRewardApplyDate)
                .add(CREATE_DATE, model.createTime)
                .add(CONSENT_EMAIL, model.consentPromotions)
                .add(NOTES, model.notes)
                .add(CUSTOMER_IDENTIFICATION, model.customerIdentification)
                .add(LOYALTY_PLAN_ID, model.loyaltyPlanId)
                .add(TMP_LOYALTY_POINTS, model.loyaltyPoints)
                .add(LOYALTY_BARCODE, model.loyaltyBarcode)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(CustomerModel model, IAppCommandContext appCommandContext) {
        Log.d("BemaCarl23","CustomerJdbcConverter.updateSQL.model.birthdayRewardApplyDate: " + model.birthdayRewardApplyDate);
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
                .add(BIRTHDAY, model.birthday)
                .add(BIRTHDAY_REWARD_APPLY_DATE, model.birthdayRewardApplyDate)
                .add(CONSENT_EMAIL, model.consentPromotions)
                .add(NOTES, model.notes)
                .add(CUSTOMER_IDENTIFICATION, model.customerIdentification)
                .add(LOYALTY_PLAN_ID, model.loyaltyPlanId)
                .add(TMP_LOYALTY_POINTS, model.loyaltyPoints)
                .add(LOYALTY_BARCODE, model.loyaltyBarcode)
                .where(GUID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand deleteSQL(CustomerModel model, IAppCommandContext appCommandContext) {
        return super.deleteSQL(model, appCommandContext);
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }

    public SingleSqlCommand updateBirthdayRewardDate(CustomerModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(BIRTHDAY_REWARD_APPLY_DATE, model.birthdayRewardApplyDate)
                .where(GUID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

}
