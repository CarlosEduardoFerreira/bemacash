package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.UnitTable;

import java.io.Serializable;

import static com.kaching123.tcr.model.ContentValuesUtil._codeType;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;
import static com.kaching123.tcr.model.ContentValuesUtil._statusType;

/**
 * Created by mayer
 */
public class Unit implements IValueModel, Serializable {

    public String guid;
    public String itemId;
    public String serialCode;
    public CodeType codeType;
    public Status status;
    public int warrantyPeriod;
    public String orderId;
    public String childOrderId;

    public Unit() {
    }

    public Unit(String guid, String itemId, String serialCode, CodeType codeType, Status status, int warrantyPeriod, String orderId, String childOrderId) {
        this.guid = guid;
        this.itemId = itemId;
        this.serialCode = serialCode;
        this.codeType = codeType;
        this.status = status;
        this.warrantyPeriod = warrantyPeriod;
        this.orderId = orderId;
        this.childOrderId = childOrderId;
    }

    public Unit(Cursor c) {

        this.guid = c.getString(c.getColumnIndex(UnitTable.ID));
        this.itemId = c.getString(c.getColumnIndex(UnitTable.ITEM_ID));
        this.serialCode = c.getString(c.getColumnIndex(UnitTable.SERIAL_CODE));
        this.codeType = _codeType(c, c.getColumnIndex(UnitTable.CODE_TYPE));
        this.status = _statusType(c, c.getColumnIndex(UnitTable.STATUS));
        this.warrantyPeriod = c.getInt(c.getColumnIndex(UnitTable.WARRANTY_PERIOD));
        this.orderId = c.getString(c.getColumnIndex(UnitTable.SALE_ORDER_ID));
        this.childOrderId = c.getString(c.getColumnIndex(UnitTable.CHILD_ORDER_ID));
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        v.put(UnitTable.ID, guid);
        v.put(UnitTable.ITEM_ID, itemId);
        v.put(UnitTable.SERIAL_CODE, serialCode);
        v.put(UnitTable.CODE_TYPE, _enum(codeType));
        v.put(UnitTable.STATUS, _enum(status));
        v.put(UnitTable.WARRANTY_PERIOD, warrantyPeriod);
        v.put(UnitTable.SALE_ORDER_ID, orderId);
        v.put(UnitTable.CHILD_ORDER_ID, childOrderId);
        return v;
    }

    public ContentValues toUpdateValues() {
        ContentValues v = new ContentValues();
        v.put(UnitTable.SERIAL_CODE, serialCode);
        v.put(UnitTable.CODE_TYPE, _enum(codeType));
        v.put(UnitTable.SALE_ORDER_ID, orderId);
        v.put(UnitTable.STATUS, _enum(status));
        v.put(UnitTable.WARRANTY_PERIOD, warrantyPeriod);
        v.put(UnitTable.CHILD_ORDER_ID, childOrderId);
        return v;
    }

    public ContentValues getUpdateOrderId() {
        ContentValues v = new ContentValues(2);
        v.put(UnitTable.SALE_ORDER_ID, orderId);
        v.put(UnitTable.STATUS, _enum(status));
        return v;
    }

    public static enum Status {
        NEW, USED, BROKEN, SOLD;

        public static Status valueOf(int id) {
            return values()[id];
        }
    }

    public static enum CodeType {
        SN, IMEI, ICCID;

        public static CodeType valueOf(int id) {
            return values()[id];
        }
    }

    @Override
    public String toString() {
        return "Unit{" +
                "guid='" + guid + '\'' +
                ", itemId='" + itemId + '\'' +
                ", serialCode='" + serialCode + '\'' +
                ", codeType=" + codeType +
                ", status=" + status +
                ", warrantyPeriod=" + warrantyPeriod +
                ", orderId='" + orderId + '\'' +
                '}';
    }
}
