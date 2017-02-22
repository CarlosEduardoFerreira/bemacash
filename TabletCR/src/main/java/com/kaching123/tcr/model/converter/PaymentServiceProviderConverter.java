package com.kaching123.tcr.model.converter;

import com.telly.groundy.PublicGroundyTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.converters.JdbcConverter;
import com.kaching123.tcr.model.PaymentServiceProviderModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;

/**
 * Created by rodrigo.busata on 5/16/2016.
 */
public class PaymentServiceProviderConverter extends JdbcConverter<PaymentServiceProviderModel> {

    public static final String TABLE_NAME = "PAYMENT_SERVICE_PROVIDER";

    private static final String ID = "ID";
    private static final String SERVICE_NAME = "SERVICE_NAME";
    private static final String SERVICE_CODE = "SERVICE_CODE";
    private static final String SERVICE_CNPJ = "SERVICE_CNPJ";
    private static final String RESPONSE_FIELD_CODE = "RESPONSE_FIELD_CODE";

    @Override
    public PaymentServiceProviderModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.PaymentServiceProviderTable.ID);
        if (!rs.has(SERVICE_NAME)) ignoreFields.add(ShopStore.PaymentServiceProviderTable.SERVICE_NAME);
        if (!rs.has(SERVICE_CODE)) ignoreFields.add(ShopStore.PaymentServiceProviderTable.SERVICE_CODE);
        if (!rs.has(SERVICE_CNPJ)) ignoreFields.add(ShopStore.PaymentServiceProviderTable.SERVICE_CNPJ);
        if (!rs.has(RESPONSE_FIELD_CODE)) ignoreFields.add(ShopStore.PaymentServiceProviderTable.RESPONSE_FIELD_CODE);

        return new PaymentServiceProviderModel(
                rs.getString(ID),
                rs.getString(SERVICE_NAME),
                rs.getString(SERVICE_CODE),
                rs.getString(SERVICE_CNPJ),
                rs.getLong(RESPONSE_FIELD_CODE),
                ignoreFields);
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public String getGuidColumn() {
        return ID;
    }

    @Override
    public String getLocalGuidColumn() {
        return ShopStore.PaymentServiceProviderTable.ID;
    }

    @Override
    public JSONObject getJSONObject(PaymentServiceProviderModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.id)
                    .put(SERVICE_NAME, model.serviceName)
                    .put(SERVICE_CODE, model.serviceCode)
                    .put(SERVICE_CNPJ, model.serviceCnpj)
                    .put(RESPONSE_FIELD_CODE, model.responseFieldCode);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(PaymentServiceProviderModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(PaymentServiceProviderModel model, PublicGroundyTask.IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }
}