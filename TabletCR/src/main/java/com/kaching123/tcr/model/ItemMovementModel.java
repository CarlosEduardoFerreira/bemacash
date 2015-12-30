package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.R;
import com.kaching123.tcr.store.ShopStore.ItemMovementTable;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static com.kaching123.tcr.model.ContentValuesUtil._decimalQty;

public class ItemMovementModel implements Serializable, IValueModel{
    public enum JustificationType {
        NONE,
        SALE,
        REFUND,
        DROP,
        ADD_UNITS,
        CREATE_ITEM,
        FROM_IMPORT;

        public static int toValue(JustificationType type) {
            int value = R.string.justification_none;

            switch (type) {
                case NONE:
                    value = R.string.justification_none;
                    break;
                case SALE:
                    value = R.string.justification_sale;
                    break;
                case REFUND:
                    value = R.string.justification_refund;
                    break;
                case DROP:
                    value = R.string.justification_drop;
                    break;
                case ADD_UNITS:
                    value = R.string.justification_add_units;
                    break;
                case CREATE_ITEM:
                    value = R.string.justification_create_item;
                    break;
                case FROM_IMPORT:
                    value = R.string.justification_from_import;
                    break;
            }

            return value;
        }
    }

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public String guid;
    public String itemGuid;
    public String itemUpdateFlag;
    public String justification;
	public BigDecimal qty;
    public boolean manual;
    public Date createTime;
    public String operatorGuid;

    public ItemMovementModel(String guid,
                             String itemGuid,
                             String itemUpdateFlag,
                             String justification,
                             BigDecimal qty,
                             boolean manual,
                             String operatorGuid,
                             Date createTime) {
        this.guid = guid;
        this.itemGuid = itemGuid;
        this.itemUpdateFlag = itemUpdateFlag;
        this.justification = justification;
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
        values.put(ItemMovementTable.MOVEMENT_JUSTIFICATION, justification);
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
