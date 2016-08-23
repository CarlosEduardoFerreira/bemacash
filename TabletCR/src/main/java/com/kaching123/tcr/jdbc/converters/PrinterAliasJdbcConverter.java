package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.PrinterAliasModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;

import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by vkompaniets on 11.02.14.
 */
public class PrinterAliasJdbcConverter extends JdbcConverter<PrinterAliasModel> {

    private static final String TABLE_NAME = "PRINTER_ALIAS";

    private static final String ID = "ID";
    private static final String ALIAS = "ALIAS";

    @Override
    public PrinterAliasModel toValues(JdbcJSONObject rs) throws JSONException {
        return new PrinterAliasModel(
                rs.getString(ID),
                rs.getString(ALIAS)
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
    public SingleSqlCommand insertSQL(PrinterAliasModel model, IAppCommandContext appCommandContext) {
        return _insert(TABLE_NAME, appCommandContext)
                .add(ID, model.guid)
                .add(ALIAS, model.alias)
                .build(JdbcFactory.getApiMethod(model));
    }

    @Override
    public SingleSqlCommand updateSQL(PrinterAliasModel model, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(ALIAS, model.alias)
                .where(ID, model.guid)
                .build(JdbcFactory.getApiMethod(model));
    }

}
