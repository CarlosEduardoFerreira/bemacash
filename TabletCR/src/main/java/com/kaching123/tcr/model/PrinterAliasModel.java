package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;

import java.io.Serializable;

/**
 * Created by vkompaniets on 11.02.14.
 */
public class PrinterAliasModel implements Serializable, IValueModel {

    private static final long serialVersionUID = 1L;

    public String guid;
    public String alias;

    public PrinterAliasModel(String guid, String alias) {
        this.guid = guid;
        this.alias = alias;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public ContentValues toValues() {
        ContentValues cv = new ContentValues();
        cv.put(PrinterAliasTable.GUID, guid);
        cv.put(PrinterAliasTable.ALIAS, alias);
        return cv;
    }

    @Override
    public String toString() {
        return alias;
    }
}
