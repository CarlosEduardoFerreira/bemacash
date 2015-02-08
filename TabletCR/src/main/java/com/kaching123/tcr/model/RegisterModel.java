package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.RegisterTable;

import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by pkabakov on 21.01.14.
 */
public class RegisterModel implements IValueModel {

    public long id;
    public String registerSerial;
    public String title;
    public RegisterStatus status;
    public int prepaidTid;
    public int blackstonePaymentCid;

    public RegisterModel(Cursor cursor) {
        this(cursor.getLong(cursor.getColumnIndex(RegisterTable.ID)),
            cursor.getString(cursor.getColumnIndex(RegisterTable.REGISTER_SERIAL)),
            cursor.getString(cursor.getColumnIndex(RegisterTable.TITLE)),
            RegisterStatus.values()[cursor.getInt(cursor.getColumnIndex(RegisterTable.STATUS))],
            cursor.getInt(cursor.getColumnIndex(RegisterTable.PREPAID_TID)),
            cursor.getInt(cursor.getColumnIndex(RegisterTable.BLACKSTONE_PAYMENT_CID))
        );
    }

    public RegisterModel(long id, String registerSerial, String title, RegisterStatus status, int prepaidTid, int blackstonePaymentCid) {
        this.id = id;
        this.registerSerial = registerSerial;
        this.title = title;
        this.status = status;
        this.prepaidTid = prepaidTid;
        this.blackstonePaymentCid = blackstonePaymentCid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(RegisterTable.ID, id);
        v.put(RegisterTable.REGISTER_SERIAL, registerSerial);
        v.put(RegisterTable.TITLE, title);
        v.put(RegisterTable.STATUS, _enum(status));
        v.put(RegisterTable.PREPAID_TID, prepaidTid);
        v.put(RegisterTable.BLACKSTONE_PAYMENT_CID, blackstonePaymentCid);
        return v;
    }

    @Override
    public String getGuid() {
        return String.valueOf(id);
    }

    public enum RegisterStatus {
        ACTIVE, INACTIVE, PAUSED, BLOCKED, PENDING
    }

}
