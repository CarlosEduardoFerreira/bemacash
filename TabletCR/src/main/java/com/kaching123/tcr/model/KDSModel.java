package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.kaching123.tcr.store.ShopStore.KDSTable;

/**
 * Created by long.jiao on 06.21.16.
 */
public class KDSModel implements IValueModel, Parcelable {

    public String guid;
    public String stationId;

    public String aliasGuid;

    public KDSModel(String guid, String stationId, String aliasGuid) {
        this.guid = guid;
        this.stationId = stationId;
        this.aliasGuid = aliasGuid;
    }

    public KDSModel(Cursor c) {
        this(c.getString(c.getColumnIndex(KDSTable.GUID)),
                c.getString(c.getColumnIndex(KDSTable.STATION_ID)),
                c.getString(c.getColumnIndex(KDSTable.ALIAS_GUID)));
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues values = new ContentValues();
        values.put(KDSTable.GUID, guid);
        values.put(KDSTable.STATION_ID, stationId);
        values.put(KDSTable.ALIAS_GUID, aliasGuid);
        return values;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.guid);
        dest.writeString(this.stationId);
        dest.writeString(this.aliasGuid);
    }

    protected KDSModel(Parcel in) {
        this.guid = in.readString();
        this.stationId = in.readString();
        this.aliasGuid = in.readString();
    }

    public static final Parcelable.Creator<KDSModel> CREATOR = new Parcelable.Creator<KDSModel>() {
        @Override
        public KDSModel createFromParcel(Parcel source) {
            return new KDSModel(source);
        }

        @Override
        public KDSModel[] newArray(int size) {
            return new KDSModel[size];
        }
    };
}
