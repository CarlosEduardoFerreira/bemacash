package com.kaching123.tcr.jdbc.converters;

import com.kaching123.tcr.Logger;
import com.kaching123.tcr.jdbc.JdbcFactory;
import com.kaching123.tcr.model.PrinterAliasModel;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.kaching123.tcr.store.ShopStore;
import com.kaching123.tcr.util.JdbcJSONObject;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.kaching123.tcr.jdbc.JdbcBuilder.FIELD_IS_DELETED;
import static com.kaching123.tcr.jdbc.JdbcBuilder._insert;
import static com.kaching123.tcr.jdbc.JdbcBuilder._update;

/**
 * Created by vkompaniets on 11.02.14.
 */
public class PrinterAliasJdbcConverter extends JdbcConverter<PrinterAliasModel> {

    public static final String TABLE_NAME = "PRINTER_ALIAS";

    private static final String ID = "ID";
    private static final String ALIAS = "ALIAS";

    @Override
    public PrinterAliasModel toValues(JdbcJSONObject rs) throws JSONException {
        List<String> ignoreFields = new ArrayList<>();
        if (!rs.has(ID)) ignoreFields.add(ShopStore.PrinterAliasTable.GUID);
        if (!rs.has(ALIAS)) ignoreFields.add(ShopStore.PrinterAliasTable.ALIAS);

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
    public String getLocalGuidColumn() {
        return ShopStore.PrinterAliasTable.GUID;
    }

    @Override
    public JSONObject getJSONObject(PrinterAliasModel model){
        JSONObject json = null;

        try {
            json = new JSONObject()
                    .put(ID, model.guid)
                    .put(ALIAS, model.alias);

        } catch (JSONException e) {
            Logger.e("JSONException", e);
        }

        return json;
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

    public SingleSqlCommand deletePrinterAlias(PrinterAliasModel printerAliasModel, IAppCommandContext appCommandContext) {
        return _update(TABLE_NAME, appCommandContext)
                .add(ALIAS, printerAliasModel.alias)
                .add(FIELD_IS_DELETED, 1)
                .where(ID, printerAliasModel.guid)
                .build(JdbcFactory.getApiMethod(printerAliasModel));
    }

    @Override
    public boolean supportUpdateTimeLocalFlag() {
        return true;
    }
}
