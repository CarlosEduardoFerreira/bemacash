package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcBuilder;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.RegisterModel;
import com.kaching123.tcr.model.RegisterModel.RegisterStatus;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.kaching123.tcr.jdbc.JdbcBuilder._update;
import static com.kaching123.tcr.model.ContentValuesUtil._enum;

/**
 * Created by pkabakov on 21.01.14.
 */
public class RegisterJdbcConverter extends JdbcConverter<RegisterModel> {

    public static final String TABLE_NAME = "REGISTER";

    private static final String ID = "ID";
    public static final String REGISTER_SERIAL = "REGISTER_SERIAL";
    public static final String DESCRIPTION = "DESCRIPTION";
    private static final String TITLE = "TITLE";
    public static final String STATUS = "STATUS";
    public static final String PREPAID_TID = "PREPAID_TID";
    public static final String BLACKSTONE_PAYMENT_CID = "BLACKSTONE_PAYMENT_CID";

    @Override
    public RegisterModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.RegisterTable.ID);
        if (!rs.has(REGISTER_SERIAL)) ignoreFields.add(ShopStore.RegisterTable.REGISTER_SERIAL);
        if (!rs.has(DESCRIPTION)) ignoreFields.add(ShopStore.RegisterTable.DESCRIPTION);
        if (!rs.has(TITLE)) ignoreFields.add(ShopStore.RegisterTable.TITLE);
        if (!rs.has(STATUS)) ignoreFields.add(ShopStore.RegisterTable.STATUS);
        if (!rs.has(PREPAID_TID)) ignoreFields.add(ShopStore.RegisterTable.PREPAID_TID);
        if (!rs.has(BLACKSTONE_PAYMENT_CID)) ignoreFields.add(ShopStore.RegisterTable.BLACKSTONE_PAYMENT_CID);

        return new RegisterModel(
                rs.getLong(ID),
                rs.getString(REGISTER_SERIAL),
                rs.getString(DESCRIPTION),
                rs.getString(TITLE),
                _enum(RegisterStatus.class, rs.getString(STATUS), RegisterStatus.ACTIVE),
                rs.optInt(PREPAID_TID),
                rs.optInt(BLACKSTONE_PAYMENT_CID),
                ignoreFields
        );
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
        return ShopStore.RegisterTable.ID;
    }

    @Override
    public JSONObject getJSONObject(RegisterModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.id)
                    .put(REGISTER_SERIAL, model.registerSerial)
                    .put(TITLE, model.title)
                    .put(DESCRIPTION, model.description)
                    .put(STATUS, model.status)
                    .put(PREPAID_TID, model.prepaidTid)
                    .put(BLACKSTONE_PAYMENT_CID, model.blackstonePaymentCid);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(RegisterModel model, IAppCommandContext appCommandContext) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SingleSqlCommand updateSQL(RegisterModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(TITLE, model.title)
                .add(STATUS, model.status)
                .where(ID, model.id)
                .build(JdbcFactory.getApiMethod(model));
    }

    public static String getGetShopIdQuery(){
        return String.format(Locale.US,
                "select %s from %s where %s = ? and %s <> ?",
                JdbcBuilder.FIELD_SHOP_ID,
                TABLE_NAME,
                REGISTER_SERIAL,
                STATUS
                );
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
