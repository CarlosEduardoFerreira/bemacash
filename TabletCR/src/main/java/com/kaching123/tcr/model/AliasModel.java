package com.kaching123.tcr.model;

import android.content.ContentValues;

import com.kaching123.tcr.store.ShopStore;

import java.io.Serializable;

/**
 * Created by long.jiao on 6.7.16.
 */
public abstract class AliasModel implements Serializable, IValueModel {

    private static final long serialVersionUID = 1L;

    public String guid;
    public String alias;

    public AliasModel(String guid, String alias) {
        this.guid = guid;
        this.alias = alias;
    }

    @Override
    public String getGuid() {
        return guid;
    }

    @Override
    public String toString() {
        return alias;
    }
}
