package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.ItemMovementTable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

public class ItemMovementModel implements Serializable, IValueModel{
	private static final long serialVersionUID = 1L;

	public String guid;
    public String itemGuid;
    public String itemUpdateFlag;
	public BigDecimal qty;
    public boolean manual;
    public Date createTime;
    public String operatorGuid;

    public ItemMovementModel(String guid,
                             String itemGuid,
                             String itemUpdateFlag,
                             BigDecimal qty,
                             boolean manual,
                             String operatorGuid,
                             Date createTime) {
        this.guid = guid;
        this.itemGuid = itemGuid;
        this.itemUpdateFlag = itemUpdateFlag;
        this.qty = qty;
        this.manual = manual;
        this.operatorGuid = operatorGuid;
        this.createTime = createTime;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
	public ContentValues toValues(){
		ContentValues values = new ContentValues();

        values.put(ItemMovementTable.GUID, guid);
        values.put(ItemMovementTable.ITEM_GUID, itemGuid);
        values.put(ItemMovementTable.ITEM_UPDATE_QTY_FLAG, itemUpdateFlag);
        values.put(ItemMovementTable.QTY, _decimalQty(qty));
        values.put(ItemMovementTable.MANUAL, manual);
        values.put(ItemMovementTable.OPERATOR_GUID, operatorGuid);
        values.put(ItemMovementTable.CREATE_TIME, createTime.getTime());
		return values;
	}

	@Override
	public String toString() {
		return guid + "; " + itemGuid + "; " + qty;
	}

}
