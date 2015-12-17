package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.ItemMatrixModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by aakimov on 13/05/15.
 */
public class ItemMatrixJdbcConverter extends JdbcConverter<ItemMatrixModel> {

    private static final String TABLE_NAME = "ITEM_MATRIX";

    private static final String ID = "ID";
    private static final String PARENT_ITEM_ID = "PARENT_ITEM_ID";
    private static final String CHILD_ITEM_ID = "CHILD_ITEM_ID";
    private static final String TITLE = "TITLE";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new ItemMatrixModel(
                rs.getString(ID),
                rs.getString(TITLE),
                rs.getString(PARENT_ITEM_ID),
                rs.getString(CHILD_ITEM_ID)
        ).toValues();
    }

    @Override
    public ItemMatrixModel toValues(JdbcJSONObject rs) throws JSONException {
        return new ItemMatrixModel(
                rs.getString(ID),
                rs.getString(TITLE),
                rs.getString(PARENT_ITEM_ID),
                rs.getString(CHILD_ITEM_ID)
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
    public SingleSqlCommand insertSQL(ItemMatrixModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(TITLE, model.name)
                .add(PARENT_ITEM_ID, model.parentItemGuid)
                .add(CHILD_ITEM_ID, model.childItemGuid)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(ItemMatrixModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(TITLE, model.name)
                .add(CHILD_ITEM_ID, model.childItemGuid)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

}
