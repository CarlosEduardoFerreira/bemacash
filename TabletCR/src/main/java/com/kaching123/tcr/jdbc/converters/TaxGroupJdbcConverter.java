package com.kaching123.tcr.jdbc.converters;

import android.content.ContentValues;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.SQLException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by pkabakov on 25/12/13.
 */
public class TaxGroupJdbcConverter extends JdbcConverter<TaxGroupModel> {

    private static final String TABLE_NAME = "TAX_GROUP";

    private static final String ID = "ID";
    private static final String TITLE = "TITLE";
    private static final String TAX = "TAX";

    @Override
    public ContentValues toValues(ResultSet rs) throws SQLException {
        return new TaxGroupModel(
                rs.getString(ID),
                rs.getString(TITLE),
                rs.getBigDecimal(TAX)
        ).toValues();
    }

    @Override
    public TaxGroupModel toValues(JdbcJSONObject rs) throws JSONException {
        return new TaxGroupModel(
                rs.getString(ID),
                rs.getString(TITLE),
                rs.getBigDecimal(TAX)
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
    public SingleSqlCommand insertSQL(TaxGroupModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(TITLE, model.title)
                .add(TAX, model.tax)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(TaxGroupModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(TITLE, model.title)
                .add(TAX, model.tax)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

}
