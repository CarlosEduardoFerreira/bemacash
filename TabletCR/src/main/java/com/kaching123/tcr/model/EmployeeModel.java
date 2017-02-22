package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.fragment.UiHelper;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.EmployeeTable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

import static com.kaching123.tcr.model.ContentValuesUtil._bool;
import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._employeeStatus;
import static com.kaching123.tcr.model.ContentValuesUtil._nullableDate;
import static com.kaching123.tcr.model.ContentValuesUtil._putDate;

/**
 * Created by gdubina on 08/11/13.
 */
public class EmployeeModel implements IValueModel, Serializable{

    public final String guid;
    public String firstName;
    public String lastName;
    public String login;
    public String password;
    public String street;
    public String complementary;
    public String city;
    public String state;
    public String country;
    public String zip;
    public String phone;
    public String email;
    public boolean sexMale;

    public Date hireDate;
    public Date fireDate;

    public EmployeeStatus status;
    public long shopId;
    public BigDecimal hRate;

    public boolean tipsEligible;
    public boolean commissionEligible;
    public BigDecimal commission;

    public boolean isMerchant;

    public boolean isSynced;

    public BigDecimal inventoryItemCount;

    private List<String> mIgnoreFields;

    public EmployeeModel(String guid, String firstName, String lastName, String login){
        this.guid = guid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
    }

    public EmployeeModel(String guid, String firstName, String lastName, String login, EmployeeStatus status){
        this(guid, firstName, lastName, login);
        this.status = status;
    }

    public EmployeeModel(String guid, String firstName, String lastName, String login, String password,
                         String street, String complementary, String city, String state, String country, String zip,
                         String phone, String email, boolean sexMale, Date hireDate, Date fireDate,
                         EmployeeStatus status, long shopId, BigDecimal hRate, boolean tipsEligible,
                         boolean commissionEligible, BigDecimal commission, boolean isMerchant, boolean isSynced,
                        List<String> ignoreFields) {

        this.guid = guid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.login = login;
        this.password = password;
        this.street = street;
        this.complementary = complementary;
        this.city = city;
        this.state = state;
        this.country = country;
        this.zip = zip;
        this.phone = phone;
        this.email = email;
        this.sexMale = sexMale;
        this.hireDate = hireDate;
        this.fireDate = fireDate;
        this.status = status;
        this.shopId = shopId;
        this.hRate = hRate;
        this.tipsEligible = tipsEligible;
        this.commissionEligible = commissionEligible;
        this.commission = commission;
        this.isMerchant = isMerchant;
        this.isSynced = isSynced;

        this.mIgnoreFields = ignoreFields;
    }

    public EmployeeModel(Cursor c){
        this(
                c.getString(c.getColumnIndex(ShopStore.EmployeeTable.GUID)),
                c.getString(c.getColumnIndex(EmployeeTable.FIRST_NAME)),
                c.getString(c.getColumnIndex(EmployeeTable.LAST_NAME)),
                c.getString(c.getColumnIndex(ShopStore.EmployeeTable.LOGIN)),
                c.getString(c.getColumnIndex(ShopStore.EmployeeTable.PASSWORD)),
                c.getString(c.getColumnIndex(ShopStore.EmployeeTable.STREET)),
                c.getString(c.getColumnIndex(ShopStore.EmployeeTable.COMPLEMENTARY)),
                c.getString(c.getColumnIndex(ShopStore.EmployeeTable.CITY)),
                c.getString(c.getColumnIndex(ShopStore.EmployeeTable.STATE)),
                c.getString(c.getColumnIndex(ShopStore.EmployeeTable.COUNTRY)),
                c.getString(c.getColumnIndex(ShopStore.EmployeeTable.ZIP)),
                c.getString(c.getColumnIndex(ShopStore.EmployeeTable.PHONE)),
                c.getString(c.getColumnIndex(ShopStore.EmployeeTable.EMAIL)),
                c.getInt(c.getColumnIndex(ShopStore.EmployeeTable.SEX)) == 1,
                _nullableDate(c, c.getColumnIndex(ShopStore.EmployeeTable.HIRE_DATE)),
                _nullableDate(c, c.getColumnIndex(ShopStore.EmployeeTable.FIRE_DATE)),
                _employeeStatus(c, c.getColumnIndex(ShopStore.EmployeeTable.STATUS)),
                c.getLong(c.getColumnIndex(ShopStore.EmployeeTable.SHOP_ID)),
                _decimal(c, c.getColumnIndex(EmployeeTable.HOURLY_RATE), BigDecimal.ZERO),
                _bool(c, c.getColumnIndex(EmployeeTable.TIPS_ELIGIBLE)),
                _bool(c, c.getColumnIndex(EmployeeTable.ELIGIBLE_FOR_COMMISSION)),
                _decimal(c, c.getColumnIndex(EmployeeTable.COMMISSION), BigDecimal.ZERO),
                c.getInt(c.getColumnIndex(EmployeeTable.IS_MERCHANT)) == 1,
                c.getInt(c.getColumnIndex(EmployeeTable.IS_SYNC)) == 1,
                null
        );
    }

    public String fullName(){
        return UiHelper.concatFullname(firstName, lastName);
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.GUID)) v.put(ShopStore.EmployeeTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.FIRST_NAME)) v.put(EmployeeTable.FIRST_NAME, firstName);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.LAST_NAME)) v.put(EmployeeTable.LAST_NAME, lastName);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.LOGIN)) v.put(ShopStore.EmployeeTable.LOGIN, login);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.PASSWORD)) v.put(ShopStore.EmployeeTable.PASSWORD, password);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.STREET)) v.put(ShopStore.EmployeeTable.STREET, street);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.COMPLEMENTARY)) v.put(ShopStore.EmployeeTable.COMPLEMENTARY, complementary);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.CITY)) v.put(ShopStore.EmployeeTable.CITY, city);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.STATE)) v.put(ShopStore.EmployeeTable.STATE, state);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.COUNTRY)) v.put(ShopStore.EmployeeTable.COUNTRY, country);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.ZIP)) v.put(ShopStore.EmployeeTable.ZIP, zip);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.PHONE)) v.put(ShopStore.EmployeeTable.PHONE, phone);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.EMAIL)) v.put(ShopStore.EmployeeTable.EMAIL, email);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.SEX)) v.put(ShopStore.EmployeeTable.SEX, sexMale);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.HIRE_DATE)) _putDate(v, ShopStore.EmployeeTable.HIRE_DATE, hireDate);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.FIRE_DATE)) _putDate(v, ShopStore.EmployeeTable.FIRE_DATE, fireDate);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.STATUS)) v.put(ShopStore.EmployeeTable.STATUS, status.ordinal());
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.SHOP_ID)) v.put(ShopStore.EmployeeTable.SHOP_ID, shopId);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.HOURLY_RATE)) v.put(EmployeeTable.HOURLY_RATE, _decimal(hRate));
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.TIPS_ELIGIBLE)) v.put(EmployeeTable.TIPS_ELIGIBLE, tipsEligible);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.ELIGIBLE_FOR_COMMISSION)) v.put(EmployeeTable.ELIGIBLE_FOR_COMMISSION, commissionEligible);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.COMMISSION)) v.put(EmployeeTable.COMMISSION, _decimal(commission));
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.IS_MERCHANT)) v.put(EmployeeTable.IS_MERCHANT, isMerchant);
        if (mIgnoreFields == null || !mIgnoreFields.contains(EmployeeTable.IS_SYNC)) v.put(EmployeeTable.IS_SYNC, isSynced);
        return v;
    }

    @Override
    public String getIdColumn() {
        return EmployeeTable.GUID;
    }


    public static boolean isLoginValid (String login){
        Pattern pattern = Pattern.compile("^[0-9]{3}$");
        return pattern.matcher(login).matches();
    }

    public BigDecimal getInventoryItemCount() {
        return inventoryItemCount;
    }

    public void setInventoryItemCount(BigDecimal inventoryItemCount) {
        this.inventoryItemCount = inventoryItemCount;
    }
}
