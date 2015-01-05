package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.model.payment.MovementType;
import com.kaching123.tcr.store.ShopStore.CashDrawerMovementTable;

import java.math.BigDecimal;
import java.util.Date;

import static com.kaching123.tcr.model.ContentValuesUtil._decimal;
import static com.kaching123.tcr.model.ContentValuesUtil._putEnum;

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

    public CashDrawerMovementModel(String guid, String shiftGuid, String managerGuid, MovementType type, BigDecimal amount, Date time, String comment) {
        this.guid = guid;
        this.shiftGuid = shiftGuid;
        this.managerGuid = managerGuid;
        this.type = type;
        this.amount = amount;
        this.time = time;
        this.comment = comment;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(CashDrawerMovementTable.GUID, guid);
        v.put(CashDrawerMovementTable.SHIFT_GUID, shiftGuid);
        v.put(CashDrawerMovementTable.MANAGER_GUID, managerGuid);
        _putEnum(v, CashDrawerMovementTable.TYPE, type);
        v.put(CashDrawerMovementTable.AMOUNT, _decimal(amount));
        v.put(CashDrawerMovementTable.MOVEMENT_TIME, time.getTime());
        v.put(CashDrawerMovementTable.COMMENT, comment);
        return v;
    }
}
