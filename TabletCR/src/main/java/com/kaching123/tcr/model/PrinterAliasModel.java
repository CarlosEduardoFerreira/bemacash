package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.database.Cursor;

import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;

import java.io.Serializable;
import java.util.List;

/**
 * Created by vkompaniets on 11.02.14.
 * Updated by Carlos on 02.17.17
 */
public class PrinterAliasModel extends AliasModel implements Serializable, IValueModel {

    private static final long serialVersionUID = 1L;

    public String guid;
    public String alias;

    public PrinterAliasModel() {
        super();
    }

    public PrinterAliasModel(String guid, String alias) {
        this.guid = guid;
        this.alias = alias;
    }

    public PrinterAliasModel(Cursor c) {
        this(
                c.getString(c.getColumnIndex(PrinterAliasTable.GUID)),
                c.getString(c.getColumnIndex(PrinterAliasTable.ALIAS))
        );
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues cv = new ContentValues();
        cv.put(ShopStore.DEFAULT_UPDATE_TIME_LOCAL, TcrApplication.get().getCurrentServerTimestamp());

        cv.put(PrinterAliasTable.GUID, guid);
        cv.put(PrinterAliasTable.ALIAS, alias);
        return cv;
    }

    @Override
    public String toString() {
        return alias;
    }

    @Override
    public String getIdColumn() {
        return PrinterAliasTable.GUID;
    }
}
