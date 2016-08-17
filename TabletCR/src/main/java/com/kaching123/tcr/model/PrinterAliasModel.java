package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore.PrinterAliasTable;

import java.io.Serializable;

/**
 * Created by vkompaniets on 11.02.14.
 */
public class PrinterAliasModel extends AliasModel {

    public PrinterAliasModel(String guid, String alias) {
        super(guid, alias);
    }

    @Override
    public ContentValues toValues() {
        ContentValues cv = new ContentValues();
        cv.put(PrinterAliasTable.GUID, guid);
        cv.put(PrinterAliasTable.ALIAS, alias);
        return cv;
    }
}
