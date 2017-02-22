package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.model.payment.MovementType;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.CashDrawerMovementTable;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._putEnum;
import static com.kaching123.tcr.util.ContentValuesUtilBase._enum;

/**
 * Created by gdubina on 03/12/13.
 */
public class CashDrawerMovementModel implements IValueModel {

    public String guid;
    public String shiftGuid;
    public String managerGuid;
    public MovementType type;
    public BigDecimal amount;
    public Date time;
    public String comment;

    private List<String> mIgnoreFields;

    public CashDrawerMovementModel(String guid, String shiftGuid, String managerGuid, MovementType type, BigDecimal amount, Date time, String comment, List<String> ignoreFields) {
        this.guid = guid;
        this.shiftGuid = shiftGuid;
        this.managerGuid = managerGuid;
        this.type = type;
        this.amount = amount;
        this.time = time;
        this.comment = comment;

        this.mIgnoreFields = ignoreFields;
    }

    public CashDrawerMovementModel(Cursor c) {
        this(c.getString(c.getColumnIndex(CashDrawerMovementTable.GUID)),
                c.getString(c.getColumnIndex(CashDrawerMovementTable.SHIFT_GUID)),
                c.getString(c.getColumnIndex(CashDrawerMovementTable.MANAGER_GUID)),
                _enum(MovementType.class, c.getString(c.getColumnIndex(CashDrawerMovementTable.TYPE)), null),
                new BigDecimal(c.getDouble(c.getColumnIndex(CashDrawerMovementTable.AMOUNT))),
                new Date(c.getLong(c.getColumnIndex(CashDrawerMovementTable.UPDATE_TIME))),
                c.getString(c.getColumnIndex(CashDrawerMovementTable.COMMENT)),
                null
        );
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(CashDrawerMovementTable.GUID)) v.put(CashDrawerMovementTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CashDrawerMovementTable.SHIFT_GUID)) v.put(CashDrawerMovementTable.SHIFT_GUID, shiftGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CashDrawerMovementTable.MANAGER_GUID)) v.put(CashDrawerMovementTable.MANAGER_GUID, managerGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CashDrawerMovementTable.TYPE)) _putEnum(v, CashDrawerMovementTable.TYPE, type);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CashDrawerMovementTable.AMOUNT)) v.put(CashDrawerMovementTable.AMOUNT, _decimal(amount));
        if (mIgnoreFields == null || !mIgnoreFields.contains(CashDrawerMovementTable.MOVEMENT_TIME)) v.put(CashDrawerMovementTable.MOVEMENT_TIME, time.getTime());
        if (mIgnoreFields == null || !mIgnoreFields.contains(CashDrawerMovementTable.COMMENT)) v.put(CashDrawerMovementTable.COMMENT, comment);
        return v;
    }

    @Override
    public String getIdColumn() {
        return CashDrawerMovementTable.GUID;
    }
}
