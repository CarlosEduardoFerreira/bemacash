package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CategoryTable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;

/**
 * Created by gdubina on 06/11/13.
 */
public class CategoryModel implements IValueModel, Serializable {

    public final String guid;
    public String title;
    public String image;
    public final int orderNum;
    public String departmentGuid;
    public boolean commissionEligible = true;
    public BigDecimal commission;

    private List<String> mIgnoreFields;

    public CategoryModel(String guid,
                         String departmentGuid,
                         String title,
                         String image,
                         int orderNum,
                         boolean commissionEligible,
                         BigDecimal commission,
                         List<String> ignoreFields) {
        this.guid = guid;
        this.title = title;
        this.image = image;
        this.orderNum = orderNum;
        this.departmentGuid = departmentGuid;
        this.commissionEligible = commissionEligible;
        this.commission = commission;

        this.mIgnoreFields = ignoreFields;
    }

    public CategoryModel(String departmentGuid, String categoryName) {
        this.guid = null;
        this.orderNum = 0;
        this.departmentGuid = departmentGuid;
        this.title = categoryName;
    }

    public CategoryModel(Cursor c) {
        this(c, null);
    }

    public CategoryModel(Cursor c, Integer orderNum) {
        this(
                c.getString(c.getColumnIndex(CategoryTable.GUID)),
                c.getString(c.getColumnIndex(CategoryTable.DEPARTMENT_GUID)),
                c.getString(c.getColumnIndex(CategoryTable.TITLE)),
                c.getString(c.getColumnIndex(CategoryTable.IMAGE)),
                orderNum != null ? orderNum : c.getInt(c.getColumnIndex(CategoryTable.ORDER_NUM)),
                c.getInt(c.getColumnIndex(CategoryTable.ELIGIBLE_FOR_COMMISSION)) == 1,
                new BigDecimal(c.getDouble(c.getColumnIndex(CategoryTable.COMMISSION))),
                null
        );
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(CategoryTable.GUID)) values.put(CategoryTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CategoryTable.DEPARTMENT_GUID)) values.put(CategoryTable.DEPARTMENT_GUID, departmentGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CategoryTable.TITLE)) values.put(CategoryTable.TITLE, title);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CategoryTable.IMAGE)) values.put(CategoryTable.IMAGE, image);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CategoryTable.ORDER_NUM)) values.put(CategoryTable.ORDER_NUM, orderNum);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CategoryTable.ELIGIBLE_FOR_COMMISSION)) values.put(CategoryTable.ELIGIBLE_FOR_COMMISSION, commissionEligible);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CategoryTable.COMMISSION)) values.put(CategoryTable.COMMISSION, _decimal(commission));

        return values;
    }

    @Override
    public String getIdColumn() {
        return CategoryTable.GUID;
    }
}
