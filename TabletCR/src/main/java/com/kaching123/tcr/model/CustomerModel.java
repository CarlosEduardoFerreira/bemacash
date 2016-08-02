package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopSchema2.SaleOrderView2;
import com.kaching123.tcr.store.ShopStore.CustomerTable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._nullableDate;

/**
 * Created by pkabakov on 10.02.14.
 */
public class CustomerModel implements IValueModel, Serializable {

    public String guid;
    public String firstName;
    public String lastName;
    public String street;
    public String complementary;
    public String city;
    public String state;
    public String country;
    public String zip;
    public String email;
    public String phone;
    public boolean sex;
    public Date birthday;
    public Date birthdayRewardApplyDate;
    public Date createTime;
    public boolean consentPromotions;
    public String notes;
    public String customerIdentification;
    public String loyaltyPlanId;
    public BigDecimal loyaltyPoints;
    public String loyaltyBarcode;

    public CustomerModel(String guid, Date createTime) {
        this.guid = guid;
        this.createTime = createTime;
    }

    public static CustomerModel fromOrderView(Cursor cursor){
        return new CustomerModel(
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.GUID)),
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.FISRT_NAME)),
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.LAST_NAME)),
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.STREET)),
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.COMPLEMENTARY)),
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.CITY)),
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.STATE)),
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.COUNTRY)),
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.ZIP)),
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.EMAIL)),
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.PHONE)),
                _bool(cursor, cursor.getColumnIndex(SaleOrderView2.CustomerTable.SEX)),
                _nullableDate(cursor, cursor.getColumnIndex(SaleOrderView2.CustomerTable.BIRTHDAY)),
                _nullableDate(cursor, cursor.getColumnIndex(SaleOrderView2.CustomerTable.BIRTHDAY_REWARD_APPLY_DATE)),
                _nullableDate(cursor, cursor.getColumnIndex(SaleOrderView2.CustomerTable.CREATE_TIME)),
                _bool(cursor, cursor.getColumnIndex(SaleOrderView2.CustomerTable.CONSENT_PROMOTIONS)),
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.NOTES)),
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.CUSTOMER_IDENTIFICATION)),
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.LOYALTY_PLAN_ID)),
                _decimal(cursor, cursor.getColumnIndex(SaleOrderView2.CustomerTable.TMP_LOYALTY_POINTS)),
                cursor.getString(cursor.getColumnIndex(SaleOrderView2.CustomerTable.LOYALTY_BARCODE)));
    }

    public CustomerModel(Cursor cursor) {
        this(cursor.getString(cursor.getColumnIndex(CustomerTable.GUID)),
                cursor.getString(cursor.getColumnIndex(CustomerTable.FISRT_NAME)),
                cursor.getString(cursor.getColumnIndex(CustomerTable.LAST_NAME)),
                cursor.getString(cursor.getColumnIndex(CustomerTable.STREET)),
                cursor.getString(cursor.getColumnIndex(CustomerTable.COMPLEMENTARY)),
                cursor.getString(cursor.getColumnIndex(CustomerTable.CITY)),
                cursor.getString(cursor.getColumnIndex(CustomerTable.STATE)),
                cursor.getString(cursor.getColumnIndex(CustomerTable.COUNTRY)),
                cursor.getString(cursor.getColumnIndex(CustomerTable.ZIP)),
                cursor.getString(cursor.getColumnIndex(CustomerTable.EMAIL)),
                cursor.getString(cursor.getColumnIndex(CustomerTable.PHONE)),
                _bool(cursor, cursor.getColumnIndex(CustomerTable.SEX)),
                _nullableDate(cursor, cursor.getColumnIndex(CustomerTable.BIRTHDAY)),
                _nullableDate(cursor, cursor.getColumnIndex(CustomerTable.BIRTHDAY_REWARD_APPLY_DATE)),
                _nullableDate(cursor, cursor.getColumnIndex(CustomerTable.CREATE_TIME)),
                _bool(cursor, cursor.getColumnIndex(CustomerTable.CONSENT_PROMOTIONS)),
                cursor.getString(cursor.getColumnIndex(CustomerTable.NOTES)),
                cursor.getString(cursor.getColumnIndex(CustomerTable.CUSTOMER_IDENTIFICATION)),
                cursor.getString(cursor.getColumnIndex(CustomerTable.LOYALTY_PLAN_ID)),
                _decimal(cursor, cursor.getColumnIndex(CustomerTable.TMP_LOYALTY_POINTS)), cursor.getString(cursor.getColumnIndex(CustomerTable.LOYALTY_BARCODE)));
    }

    public CustomerModel(String guid, String firstName, String lastName, String street,
                         String complementary, String city, String state, String country,
                         String zip, String email, String phone, boolean sex, Date birthday,
                         Date birthdayRewardApplyDate, Date createTime,
                         boolean consentPromotions, String notes, String customerIdentification,
                         String loyaltyPlanId, BigDecimal loyaltyPoints, String loyaltyBarcode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.guid = guid;
        this.street = street;
        this.complementary = complementary;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zip = zip;
        this.email = email;
        this.phone = phone;
        this.sex = sex;
        this.birthday = birthday;
        this.birthdayRewardApplyDate = birthdayRewardApplyDate;
        this.createTime = createTime;
        this.consentPromotions = consentPromotions;
        this.notes = notes;
        this.customerIdentification = customerIdentification;
        this.loyaltyPlanId = loyaltyPlanId;
        this.loyaltyPoints = loyaltyPoints;
        this.loyaltyBarcode = loyaltyBarcode;
    }

    public String getFullName() {
        return UiHelper.concatFullname(firstName, lastName);
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(CustomerTable.GUID, guid);
        v.put(CustomerTable.FISRT_NAME, firstName);
        v.put(CustomerTable.LAST_NAME, lastName);
        v.put(CustomerTable.STREET, street);
        v.put(CustomerTable.COMPLEMENTARY, complementary);
        v.put(CustomerTable.CITY, city);
        v.put(CustomerTable.STATE, state);
        v.put(CustomerTable.COUNTRY, country);
        v.put(CustomerTable.ZIP, zip);
        v.put(CustomerTable.EMAIL, email);
        v.put(CustomerTable.PHONE, phone);
        v.put(CustomerTable.SEX, sex);
        _nullableDate(v, CustomerTable.BIRTHDAY, birthday);
        _nullableDate(v, CustomerTable.CREATE_TIME, createTime);
        v.put(CustomerTable.CONSENT_PROMOTIONS, consentPromotions);
        v.put(CustomerTable.NOTES, notes);
        v.put(CustomerTable.CUSTOMER_IDENTIFICATION, customerIdentification);
        v.put(CustomerTable.LOYALTY_PLAN_ID, loyaltyPlanId);
        v.put(CustomerTable.LOYALTY_BARCODE, loyaltyBarcode);
        return v;
    }

    public ContentValues toUpdateValues() {
        ContentValues v = new ContentValues();
        v.put(CustomerTable.FISRT_NAME, firstName);
        v.put(CustomerTable.LAST_NAME, lastName);
        v.put(CustomerTable.STREET, street);
        v.put(CustomerTable.COMPLEMENTARY, complementary);
        v.put(CustomerTable.CITY, city);
        v.put(CustomerTable.STATE, state);
        v.put(CustomerTable.COUNTRY, country);
        v.put(CustomerTable.ZIP, zip);
        v.put(CustomerTable.EMAIL, email);
        v.put(CustomerTable.PHONE, phone);
        v.put(CustomerTable.SEX, sex);
        _nullableDate(v, CustomerTable.BIRTHDAY, birthday);
        v.put(CustomerTable.CONSENT_PROMOTIONS, consentPromotions);
        v.put(CustomerTable.NOTES, notes);
        v.put(CustomerTable.CUSTOMER_IDENTIFICATION, customerIdentification);
        v.put(CustomerTable.LOYALTY_PLAN_ID, loyaltyPlanId);
        v.put(CustomerTable.LOYALTY_BARCODE, loyaltyBarcode);
        return v;
    }

    public static CustomerModel loadSync(Context context, String guid){
        Cursor c = ProviderAction.query(ShopProvider.contentUri(CustomerTable.URI_CONTENT))
                .where(CustomerTable.GUID + " = ?", guid)
                .perform(context);
        CustomerModel result = null;
        if (c.moveToFirst()){
            result = new CustomerModel(c);
        }
        c.close();

        return result;
    }

}
