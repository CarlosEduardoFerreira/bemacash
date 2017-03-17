package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.RegisterTable;

import java.util.List;

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
    public String description;

    private List<String> mIgnoreFields;

    public RegisterModel(Cursor cursor) {
        this(cursor.getLong(cursor.getColumnIndex(RegisterTable.ID)),
            cursor.getString(cursor.getColumnIndex(RegisterTable.REGISTER_SERIAL)),
            cursor.getString(cursor.getColumnIndex(RegisterTable.DESCRIPTION)),
            cursor.getString(cursor.getColumnIndex(RegisterTable.TITLE)),
            RegisterStatus.values()[cursor.getInt(cursor.getColumnIndex(RegisterTable.STATUS))],
            cursor.getInt(cursor.getColumnIndex(RegisterTable.PREPAID_TID)),
            cursor.getInt(cursor.getColumnIndex(RegisterTable.BLACKSTONE_PAYMENT_CID)),
            null
        );
    }

    public RegisterModel(long id, String registerSerial, String description, String title,
                         RegisterStatus status, int prepaidTid, int blackstonePaymentCid, List<String> ignoreFields) {
        this.id = id;
        this.registerSerial = registerSerial;
        this.title = title;
        this.status = status;
        this.prepaidTid = prepaidTid;
        this.blackstonePaymentCid = blackstonePaymentCid;
        this.description = description;

        this.mIgnoreFields = ignoreFields;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        if (mIgnoreFields == null || !mIgnoreFields.contains(RegisterTable.ID)) v.put(RegisterTable.ID, id);
        if (mIgnoreFields == null || !mIgnoreFields.contains(RegisterTable.ID)) v.put(RegisterTable.REGISTER_SERIAL, registerSerial);
        if (mIgnoreFields == null || !mIgnoreFields.contains(RegisterTable.ID)) v.put(RegisterTable.TITLE, title);
        if (mIgnoreFields == null || !mIgnoreFields.contains(RegisterTable.ID)) v.put(RegisterTable.DESCRIPTION, description);
        if (mIgnoreFields == null || !mIgnoreFields.contains(RegisterTable.ID)) v.put(RegisterTable.STATUS, _enum(status));
        if (mIgnoreFields == null || !mIgnoreFields.contains(RegisterTable.ID)) v.put(RegisterTable.PREPAID_TID, prepaidTid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(RegisterTable.ID)) v.put(RegisterTable.BLACKSTONE_PAYMENT_CID, blackstonePaymentCid);
        return v;
    }

    @Override
    public String getIdColumn() {
        return RegisterTable.ID;
    }

    @Override
    public String getGuid() {
        return String.valueOf(id);
    }

    public enum RegisterStatus {
        ACTIVE, INACTIVE, PAUSED, BLOCKED, PENDING
    }

    @Override
    public boolean equals(Object o) {
        return this == o || (o != null && id == ((RegisterModel)o).id &&
                registerSerial.equals(((RegisterModel)o).registerSerial) &&
                title.equals(((RegisterModel)o).title));
    }
}
