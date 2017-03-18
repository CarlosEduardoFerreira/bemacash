package com.kaching123.tcr.jdbc.converters;

import android.util.Log;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ItemModel;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.jdbc.JdbcBuilder.FIELD_IS_DELETED;
import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by pkabakov on 25/12/13.
 */
public class TaxGroupJdbcConverter extends JdbcConverter<TaxGroupModel> {

    public static final String TABLE_NAME = "TAX_GROUP";
    private static final String ID = "ID";
    private static final String TITLE = "TITLE";
    private static final String TAX = "TAX";
    private static final String IS_DEFAULT = "IS_DEFAULT";

    @Override
    public TaxGroupModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.TaxGroupTable.GUID);
        if (!rs.has(TITLE)) ignoreFields.add(ShopStore.TaxGroupTable.TITLE);
        if (!rs.has(TAX)) ignoreFields.add(ShopStore.TaxGroupTable.TAX);
        if (!rs.has(IS_DEFAULT)) ignoreFields.add(ShopStore.TaxGroupTable.IS_DEFAULT);

        return new TaxGroupModel(
                rs.getString(ID),
                rs.getString(TITLE),
                rs.getBigDecimal(TAX),
                rs.getBoolean(IS_DEFAULT),
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
        return ShopStore.TaxGroupTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(TaxGroupModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(TITLE, model.title)
                    .put(TAX, model.tax)
                    .put(IS_DEFAULT, model.isDefault);
        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
    }

    @Override
    public SingleSqlCommand insertSQL(TaxGroupModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(TITLE, model.title)
                .add(TAX, model.tax)
                .add(IS_DEFAULT, model.isDefault)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(TaxGroupModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(TITLE, model.title)
                .add(TAX, model.tax)
                .add(IS_DEFAULT, model.isDefault)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
