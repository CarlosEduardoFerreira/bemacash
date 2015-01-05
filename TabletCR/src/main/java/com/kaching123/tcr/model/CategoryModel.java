package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.CategoryTable;

import java.io.Serializable;
import java.math.BigDecimal;

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

    public CategoryModel(String guid, String departmentGuid, String title, String image, int orderNum, boolean commissionEligible, BigDecimal commission) {
        this.guid = guid;
        this.title = title;
        this.image = image;
        this.orderNum = orderNum;
        this.departmentGuid = departmentGuid;
        this.commissionEligible = commissionEligible;
        this.commission = commission;
    }

    public CategoryModel(String departmentGuid, String categoryName) {
        this.guid = null;
        this.orderNum = 0;
        this.departmentGuid = departmentGuid;
        this.title = categoryName;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(CategoryTable.GUID, guid);
        values.put(CategoryTable.DEPARTMENT_GUID, departmentGuid);
        values.put(CategoryTable.TITLE, title);
        values.put(CategoryTable.IMAGE, image);
        values.put(CategoryTable.ORDER_NUM, orderNum);
        values.put(CategoryTable.ELIGIBLE_FOR_COMMISSION, commissionEligible);
        values.put(CategoryTable.COMMISSION, _decimal(commission));

        return values;
    }
}
