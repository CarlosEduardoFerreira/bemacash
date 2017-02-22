package com.kaching123.tcr.model;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.getbase.android.db.provider.ProviderAction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kaching123.tcr.store.ShopProvider;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.ContentValuesUtilBase;

import static com.kaching123.tcr.model.ContentValuesUtil._long;

/**
 * Created by rodrigo.busata on 5/16/2016.
 */
public class PaymentServiceProviderModel implements com.kaching123.tcr.model.IValueModel, Serializable {

    public String id;
    public String serviceName;
    public String serviceCode;
    public String serviceCnpj;
    public Long responseFieldCode;

    private List<String> mIgnoreFields;

    public PaymentServiceProviderModel(String id, String serviceName, String serviceCode, String serviceCnpj, Long responseFieldCode, List<String> ignoreFields) {
        this.id = id;
        this.serviceName = serviceName;
        this.serviceCode = serviceCode;
        this.serviceCnpj = serviceCnpj;
        this.responseFieldCode = responseFieldCode;

        this.mIgnoreFields = ignoreFields;
    }

    public PaymentServiceProviderModel(Cursor cursor) {
        this(cursor.getString(cursor.getColumnIndex(ShopStore.PaymentServiceProviderTable.ID)),
                cursor.getString(cursor.getColumnIndex(ShopStore.PaymentServiceProviderTable.SERVICE_NAME)),
                cursor.getString(cursor.getColumnIndex(ShopStore.PaymentServiceProviderTable.SERVICE_CODE)),
                cursor.getString(cursor.getColumnIndex(ShopStore.PaymentServiceProviderTable.SERVICE_CNPJ)),
                ContentValuesUtilBase._long(cursor.getString(cursor.getColumnIndex(ShopStore.PaymentServiceProviderTable.RESPONSE_FIELD_CODE))),
                null);
    }

    private static ArrayList<PaymentServiceProviderModel> getPaymentServiceProvider(Context context) {
        final ArrayList<PaymentServiceProviderModel> objects = new ArrayList<>();
        try (
                Cursor c = ProviderAction.query(ShopProvider.contentUri(ShopStore.PaymentServiceProviderTable.URI_CONTENT))
                        .perform(context)
        ) {

            if (c != null && c.moveToFirst()) {
                do {
                    objects.add(new PaymentServiceProviderModel(c));

                } while (c.moveToNext());
            }

            return objects;
        }
    }

    public static Map<String, Integer> getPaymentServiceProviderMap(Context context) {
        ArrayList<PaymentServiceProviderModel> paymentProvidersModel = getPaymentServiceProvider(context);

        Map<String, Integer> paymentProviders = new HashMap<>();
        for (PaymentServiceProviderModel paymentModel : paymentProvidersModel) {
            paymentProviders.put(paymentModel.serviceCode, paymentModel.responseFieldCode.intValue());
        }

        return paymentProviders;
    }

    public static String getCnpjByServiceCode(Context context, String serviceCode) {
        String serviceCodeCnpj = null;
        try (
                Cursor c = ProviderAction.query(ShopProvider.contentUri(ShopStore.PaymentServiceProviderTable.URI_CONTENT))
                        .projection(ShopStore.PaymentServiceProviderTable.SERVICE_CNPJ)
                        .where(ShopStore.PaymentServiceProviderTable.SERVICE_CODE + " = ?", serviceCode)
                        .perform(context)
        ) {

            if (c != null && c.moveToFirst()) {
                serviceCodeCnpj = c.getString(c.getColumnIndex(ShopStore.PaymentServiceProviderTable.SERVICE_CNPJ));
            }
            return serviceCodeCnpj;
        }
    }

    public static int getIdByCode(Context context, String code) {
        try (
                Cursor c = ProviderAction.query(ShopProvider.contentUri(ShopStore.PaymentServiceProviderTable.URI_CONTENT))
                        .projection(ShopStore.PaymentServiceProviderTable.ID)
                        .where(ShopStore.PaymentServiceProviderTable.SERVICE_CODE + " = ?", code)
                        .perform(context)
        ) {
            if (c != null && c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return 0;
    }

    @Override
    public String getGuid() {
        return id;
    }

    @Override
    public ContentValues toValues() {
        ContentValues c = new ContentValues();
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.PaymentServiceProviderTable.ID)) c.put(ShopStore.PaymentServiceProviderTable.ID, id);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.PaymentServiceProviderTable.SERVICE_NAME)) c.put(ShopStore.PaymentServiceProviderTable.SERVICE_NAME, serviceName);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.PaymentServiceProviderTable.SERVICE_CODE)) c.put(ShopStore.PaymentServiceProviderTable.SERVICE_CODE, serviceCode);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.PaymentServiceProviderTable.SERVICE_CNPJ)) c.put(ShopStore.PaymentServiceProviderTable.SERVICE_CNPJ, serviceCnpj);
        if (mIgnoreFields == null || !mIgnoreFields.contains(ShopStore.PaymentServiceProviderTable.RESPONSE_FIELD_CODE)) c.put(ShopStore.PaymentServiceProviderTable.RESPONSE_FIELD_CODE, responseFieldCode);

        return c;
    }

    @Override
    public String getIdColumn() {
        return ShopStore.PaymentServiceProviderTable.ID;
    }

}