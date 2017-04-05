package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.store.ShopStore.ItemMovementTable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

public class ItemMovementModel implements Serializable, IValueModel{
	private static final long serialVersionUID = 1L;

	public String guid;
    public String itemGuid;
    public String orderGuid;
    public String itemUpdateFlag;
	public BigDecimal qty;
    public boolean manual;
    public Date createTime;
    public String operatorGuid;

    private List<String> mIgnoreFields;

    public ItemMovementModel(String guid,
                             String itemGuid,
                             String orderGuid,
                             String itemUpdateFlag,
                             BigDecimal qty,
                             boolean manual,
                             String operatorGuid,
                             Date createTime,
                             List<String> ignoreFields) {
        this.guid = guid;
        this.itemGuid = itemGuid;
        this.orderGuid = orderGuid;
        this.itemUpdateFlag = itemUpdateFlag;
        this.qty = qty;
        this.manual = manual;
        this.operatorGuid = operatorGuid;
        this.createTime = createTime;

        this.mIgnoreFields = ignoreFields;
    }

    public ItemMovementModel(Cursor c) {
        this(
                c.getString(c.getColumnIndex(ItemMovementTable.GUID)),
                c.getString(c.getColumnIndex(ItemMovementTable.ITEM_GUID)),
                c.getString(c.getColumnIndex(ItemMovementTable.ORDER_GUID)),
                c.getString(c.getColumnIndex(ItemMovementTable.ITEM_UPDATE_QTY_FLAG)),
                new BigDecimal(c.getDouble(c.getColumnIndex(ItemMovementTable.QTY))),
                c.getInt(c.getColumnIndex(ItemMovementTable.MANUAL)) == 1,
                c.getString(c.getColumnIndex(ItemMovementTable.OPERATOR_GUID)),
                new Date(c.getLong(c.getColumnIndex(ItemMovementTable.CREATE_TIME))),
                null
        );

    }

    @Override
    public String getGuid() {
        return guid;
    }

    public void setOrderGuid(String orderGuid) {
        this.orderGuid = orderGuid;
    }

    @Override
	public ContentValues toValues(){
		ContentValues values = new ContentValues();
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemMovementTable.GUID)) values.put(ItemMovementTable.GUID, guid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemMovementTable.ITEM_GUID)) values.put(ItemMovementTable.ITEM_GUID, itemGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemMovementTable.ORDER_GUID)) values.put(ItemMovementTable.ORDER_GUID, orderGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemMovementTable.ITEM_UPDATE_QTY_FLAG)) values.put(ItemMovementTable.ITEM_UPDATE_QTY_FLAG, itemUpdateFlag);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemMovementTable.QTY)) values.put(ItemMovementTable.QTY, _decimalQty(qty));
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemMovementTable.MANUAL)) values.put(ItemMovementTable.MANUAL, manual);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemMovementTable.OPERATOR_GUID)) values.put(ItemMovementTable.OPERATOR_GUID, operatorGuid);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ItemMovementTable.CREATE_TIME)) values.put(ItemMovementTable.CREATE_TIME, createTime.getTime());
		return values;
	}

    @Override
    public String getIdColumn() {
        return ItemMovementTable.GUID;
    }

    @Override
	public String toString() {
		return guid + "; " + itemGuid + "; " + qty;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItemMovementModel that = (ItemMovementModel) o;

        if (manual != that.manual) return false;
        if (guid != null ? !guid.equals(that.guid) : that.guid != null) return false;
        if (itemGuid != null ? !itemGuid.equals(that.itemGuid) : that.itemGuid != null)
            return false;
        if (orderGuid != null ? !orderGuid.equals(that.orderGuid) : that.orderGuid != null)
            return false;
        if (itemUpdateFlag != null ? !itemUpdateFlag.equals(that.itemUpdateFlag) : that.itemUpdateFlag != null)
            return false;
        if (qty != null ? !qty.equals(that.qty) : that.qty != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null)
            return false;
        return operatorGuid != null ? operatorGuid.equals(that.operatorGuid) : that.operatorGuid == null;

    }

    @Override
    public int hashCode() {
        int result = guid != null ? guid.hashCode() : 0;
        result = 31 * result + (itemGuid != null ? itemGuid.hashCode() : 0);
        result = 31 * result + (orderGuid != null ? orderGuid.hashCode() : 0);
        result = 31 * result + (itemUpdateFlag != null ? itemUpdateFlag.hashCode() : 0);
        result = 31 * result + (qty != null ? qty.hashCode() : 0);
        result = 31 * result + (manual ? 1 : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (operatorGuid != null ? operatorGuid.hashCode() : 0);
        return result;
    }
}
