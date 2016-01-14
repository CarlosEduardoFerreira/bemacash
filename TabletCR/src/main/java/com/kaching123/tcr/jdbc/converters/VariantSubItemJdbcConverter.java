package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.VariantSubItemModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by aakimov on 13/05/15.
 */
public class VariantSubItemJdbcConverter extends JdbcConverter<VariantSubItemModel> {

    private static final String TABLE_NAME = "SUB_VARIANT";

    private static final String ID = "ID";
    private static final String VARIANT_ID = "VARIANT_ID";
    private static final String VALUE = "VALUE";
    private static final String ITEM_ID = "ITEM_ID";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new VariantSubItemModel(
                rs.getString(ID),
                rs.getString(VALUE),
                rs.getString(VARIANT_ID),
                rs.getString(ITEM_ID)
        ).toValues();
    }

    @Override
    public VariantSubItemModel toValues(JdbcJSONObject rs) throws JSONException {
        return new VariantSubItemModel(
                rs.getString(ID),
                rs.getString(VALUE),
                rs.getString(VARIANT_ID),
                rs.getString(ITEM_ID)
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
    public SingleSqlCommand insertSQL(VariantSubItemModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(VALUE, model.name)
                .add(VARIANT_ID, model.parentVariantItemGuid)
                .add(ITEM_ID, model.itemGuid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(VariantSubItemModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(VALUE, model.name)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

}
