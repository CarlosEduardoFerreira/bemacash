package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore;

/**
 * Created by long.jiao on 6.7.16.
 */
public class KDSAliasModel extends AliasModel {

    public KDSAliasModel(){
        super();
    }

    public KDSAliasModel(String guid, String alias) {
        super(guid, alias);
    }

    @Override
    public ContentValues toValues() {
        ContentValues cv = new ContentValues();
        cv.put(ShopStore.KDSAliasTable.GUID, guid);
        cv.put(ShopStore.KDSAliasTable.ALIAS, alias);
        return cv;
    }
}
