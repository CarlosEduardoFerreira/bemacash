package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.TaxGroupModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
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
    private static final String IS_DEFAULT = "IS_DEFAULT";

    public static boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        ResultSetMetaData rsmd = rs.getMetaData();
        int columns = rsmd.getColumnCount();
        for (int x = 1; x <= columns; x++) {
            if (columnName.equals(rsmd.getColumnName(x))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TaxGroupModel toValues(JdbcJSONObject rs) throws JSONException {
        return new TaxGroupModel(
                rs.getString(ID),
                rs.getString(TITLE),
                rs.getBigDecimal(TAX),
                rs.getInt(IS_DEFAULT) == 1);
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

}
