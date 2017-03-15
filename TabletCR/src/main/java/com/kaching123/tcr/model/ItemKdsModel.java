package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.KDSTable;

/**
 * Created by long.jiao on 06.21.16.
 */
public class ItemKdsModel implements IValueModel, Parcelable {

    public String guid;

    public String itemID;

    public String kdsID;

    public ItemKdsModel(String guid, String itemID, String kdsID) {
        this.guid = guid;
        this.itemID = itemID;
        this.kdsID = kdsID;
    }

    public ItemKdsModel(Cursor c) {
        this(c.getString(c.getColumnIndex(ShopStore.ItemKDSTable.ID)),
                c.getString(c.getColumnIndex(ShopStore.ItemKDSTable.ITEM_GUID)),
                c.getString(c.getColumnIndex(ShopStore.ItemKDSTable.KDS_ALIAS_GUID)));
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        values.put(ShopStore.ItemKDSTable.ID, guid);
        values.put(ShopStore.ItemKDSTable.ITEM_GUID, itemID);
        values.put(ShopStore.ItemKDSTable.KDS_ALIAS_GUID, kdsID);
        return values;
    }

    @Override
    public String getIdColumn() {
        return ShopStore.ItemKDSTable.ID;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.guid);
        dest.writeString(this.itemID);
        dest.writeString(this.kdsID);
    }

    protected ItemKdsModel(Parcel in) {
        this.guid = in.readString();
        this.itemID = in.readString();
        this.kdsID = in.readString();
    }

    public static final Creator<ItemKdsModel> CREATOR = new Creator<ItemKdsModel>() {
        @Override
        public ItemKdsModel createFromParcel(Parcel source) {
            return new ItemKdsModel(source);
        }

        @Override
        public ItemKdsModel[] newArray(int size) {
            return new ItemKdsModel[size];
        }
    };
}
