package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.VariantItemModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by aakimov on 13/05/15.
 */
public class VariantItemJdbcConverter extends JdbcConverter<VariantItemModel> {

    private static final String TABLE_NAME = "VARIANT";

    private static final String ID = "ID";
    private static final String ITEM_ID = "ITEM_ID";
    private static final String SHOP_ID = "SHOP_ID";
    private static final String TITLE = "TITLE";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new VariantItemModel(
                rs.getString(ID),
                rs.getString(TITLE),
                rs.getString(ITEM_ID),
                rs.getLong(SHOP_ID)
        ).toValues();
    }

    @Override
    public VariantItemModel toValues(JdbcJSONObject rs) throws JSONException {
        return new VariantItemModel(
                rs.getString(ID),
                rs.getString(TITLE),
                rs.getString(ITEM_ID),
                rs.getLong(SHOP_ID)
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
    public SingleSqlCommand insertSQL(VariantItemModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(TITLE, model.name)
                .add(ITEM_ID, model.parentGuid)
                .add(SHOP_ID, model.shopId)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(VariantItemModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(TITLE, model.name)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

}
