package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;

import java.io.Serializable;
import java.util.List;

import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore.CardBrandTable;


/**
 * Created by Rodrigo Busata on 07/22/16.
 */
public class CardBrandModel implements IValueModel, Serializable {

    public String id;
    public String name;
    public String type;

    private List<String> mIgnoreFields;

    public CardBrandModel(Cursor c) {
        this.id = c.getString(c.getColumnIndex(CardBrandTable.ID));
        this.name = c.getString(c.getColumnIndex(CardBrandTable.NAME));
        this.type = c.getString(c.getColumnIndex(CardBrandTable.TYPE));

    }

    public CardBrandModel(String guid,
                          String name,
                          String type,
                          List<String> ignoreFields) {
        this.id = guid;
        this.name = name;
        this.type = type;

        this.mIgnoreFields = ignoreFields;
    }

    @Override
    public String getGuid() {
        return id;
    }

    @Override
    public ContentValues toValues() {
        ContentValues v = new ContentValues();
        if (mIgnoreFields == null || !mIgnoreFields.contains(CardBrandTable.ID)) v.put(CardBrandTable.ID, id);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CardBrandTable.NAME)) v.put(CardBrandTable.NAME, name);
        if (mIgnoreFields == null || !mIgnoreFields.contains(CardBrandTable.TYPE)) v.put(CardBrandTable.TYPE, type);

        return v;
    }

    @Override
    public String getIdColumn() {
        return CardBrandTable.ID;
    }

    public static String getIdByNameAndType(Context context, String data, String type) {
        String info[] = mapCardBrand(data, type);
        if (info == null) return null;

        try (
                Cursor c = ProviderAction.query(ShopProvider.contentUri(CardBrandTable.URI_CONTENT))
                        .projection(CardBrandTable.ID)
                        .where(CardBrandTable.NAME + " = ?", info[0])
                        .where(CardBrandTable.TYPE + " = ?", info[1])
                        .perform(context)
        ) {
            if (c != null && c.moveToFirst()) {
                return c.getString(0);
            }
        }

        return null;
    }

    private static String[] mapCardBrand(String data, String type) {
        if (type.toLowerCase().contains("credit")) type = PaymentTypes.CREDIT;
        else if (type.toLowerCase().contains("debit")) type = PaymentTypes.DEBIT;
        else if (type.toLowerCase().contains("voucher")) type = PaymentTypes.RESTAURANT;
        else return null;

        String name = "unknown";
        if (data.contains("AMERICAN EXPRESS")) {
            name = "americanexpress";

        } else if (data.contains("VISA")) {
            name = "visa";

        } else if (data.contains("MAESTRO") || data.contains("MASTERCARD") || data.contains("MAESTROCP")) {
            name = "master";

        } else if (data.contains("DINERS")) {
            name = "diners";

        } else if (data.contains("SODEXO ALIMENTAC")) {
            type = PaymentTypes.FOODSTAMP;
            name = "sodexo";

        } else if (data.contains("SODEXO REFEICAO ")) {
            type = PaymentTypes.RESTAURANT;
            name = "sodexo";

        } else if (data.contains("RESTAURAN") || data.contains("REFEICAO")) {
            type = PaymentTypes.RESTAURANT;

        } else if (data.contains("ALIMENTAC")) {
            type = PaymentTypes.FOODSTAMP;
        }
        return new String[]{name, type};
    }

    private interface PaymentTypes {
        String CREDIT = "credit";
        String DEBIT = "debit";
        String RESTAURANT = "restaurant";
        String FOODSTAMP = "foodstamp";
    }
}
