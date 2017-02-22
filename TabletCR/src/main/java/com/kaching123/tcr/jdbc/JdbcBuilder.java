package com.kaching123.tcr.jdbc;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.FluentIterable;
import com.kaching123.tcr.Logger;
import com.kaching123.tcr.TcrApplication;
import com.kaching123.tcr.commands.rest.sync.SyncUtil;
import com.kaching123.tcr.service.DecimalQueryParam;
import com.kaching123.tcr.service.QueryParam;
import com.kaching123.tcr.service.SingleSqlCommand;
import com.telly.groundy.PublicGroundyTask.IAppCommandContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by gdubina on 08/11/13.
 */
public abstract class JdbcBuilder {

    public static final String FIELD_UPDATE_TIME_LOCAL = "UPDATE_TIME_LOCAL";
    public static final String FIELD_UPDATE_TIME = "UPDATE_TIME";
    public static final String FIELD_SHOP_ID = "SHOP_ID";
    public static final String FIELD_IS_DELETED = "IS_DELETED";

    protected final String methodPrefix;
    protected final String table;
    protected final String action;
    protected final ArrayList<Pair> columns = new ArrayList<>();
    protected final ArrayList<Pair> where = new ArrayList<>();

    private JdbcBuilder(String table, String action, String methodPrefix) {
        this.table = table;
        this.action = action;
        this.methodPrefix = methodPrefix;

        if (JdbcFactory.getConverter(table).supportUpdateTimeLocalFlag()){
            add(FIELD_UPDATE_TIME_LOCAL, SyncUtil.formatMillisec(new Date(TcrApplication.get().getCurrentServerTimestamp())));
        }
    }

    public JdbcBuilder add(String column, Object value) {
        if (value instanceof  String) add(column, (String) value);
        else if (value instanceof  BigDecimal) add(column, (BigDecimal) value);
        else if (value instanceof  Boolean) add(column, (boolean) value);
        else if (value instanceof  Date) add(column, (Date) value);
        else if (value instanceof  Enum) add(column, (Enum) value);
        else if (value instanceof  Integer) add(column, (int) value);
        else if (value instanceof  Long) add(column, (long) value);
        return this;
    }

    public JdbcBuilder add(String column, String value) {
        columns.add(new Pair(column, Types.VARCHAR, value));
        return this;
    }

    public JdbcBuilder add(String column, byte[] value) {
        columns.add(new Pair(column, Types.BLOB, value));
        return this;
    }

    public JdbcBuilder add(String column, BigDecimal value) {
        columns.add(new Pair(column, Types.DECIMAL, value));
        return this;
    }

    public JdbcBuilder add(String column, BigDecimal value, int decimalScale) {
        columns.add(new Pair(column, new DecimalQueryParam(value, decimalScale)));
        return this;
    }

    public JdbcBuilder add(String column, boolean value) {
        columns.add(new Pair(column, Types.BOOLEAN, value));
        return this;
    }

    public JdbcBuilder add(String column, Date date) {
        columns.add(new Pair(column, Types.TIMESTAMP, date));
        return this;
    }

    public JdbcBuilder add(String column, Enum value) {
        columns.add(new Pair(column, Types.VARCHAR, value == null ? null : value.name()));
        return this;
    }

    public JdbcBuilder add(String column, int value) {
        columns.add(new Pair(column, Types.INTEGER, value));
        return this;
    }

    public JdbcBuilder add(String column, long value) {
        columns.add(new Pair(column, Types.BIGINT, value));
        return this;
    }

    public JdbcBuilder add(String column, Long value) {
        columns.add(new Pair(column, Types.BIGINT, value));
        return this;
    }

    public JdbcBuilder where(String column, String value) {
        where.add(new Pair(column, Types.VARCHAR, value));
        return this;
    }

    public JdbcBuilder where(String column, long value) {
        where.add(new Pair(column, Types.BIGINT, value));
        return this;
    }


    protected String buildCmd(){
        JSONObject json = new JSONObject();
        try {
            json.put("table", table);
            json.put("action", action);
            json.put("args", getArgsJson());
            return json.toString();
        } catch (JSONException e) {
            Logger.e("Can't create cmd", e);
            return null;
        }
    }

    protected JSONObject getArgsJson() throws JSONException {
        JSONObject args = new JSONObject();
        for (Pair c : columns) {
            args.put(c.column, c.value.isNull() ? JSONObject.NULL : c.value.toJson());
        }
        return args;
    }

    protected JSONObject getWhereJson() throws JSONException {
        JSONObject args = new JSONObject();
        for (Pair c : where) {
            args.put(c.column, c.value.isNull() ? JSONObject.NULL : c.value.toJson());
        }

        if (JdbcFactory.getConverter(table).supportUpdateTimeLocalFlag()){
            args.put(FIELD_UPDATE_TIME_LOCAL, SyncUtil.formatMillisec(new Date(TcrApplication.get().getCurrentServerTimestamp())));
        }
        return args;
    }

    public SingleSqlCommand build(String method){
        return new SingleSqlCommand(
                methodPrefix + method,
                buildCmd()
        );
    }

    private static class Pair {
        final String column;
        final QueryParam value;

        private Pair(String column, int type, Object value) {
            this.column = column;
            this.value = new QueryParam(type, value);
        }

        private Pair(String column, QueryParam value) {
            this.column = column;
            this.value = value;
        }

    }

    public static JdbcBuilder _insert(String table, IAppCommandContext appCommandContext) {
        return new InsertBuilder(table)
                .add(FIELD_SHOP_ID, appCommandContext != null ? appCommandContext.getShopId() : TcrApplication.get().getShopPref().shopId().get());
    }

    public static JdbcBuilder _insert(long shopId, String table) {
        return new InsertBuilder(table)
                .add(FIELD_SHOP_ID, shopId);
    }

    public static InsertOrUpdateBuilder _insertOrUpdate(String table, IAppCommandContext appCommandContext) {
        InsertOrUpdateBuilder builder = new InsertOrUpdateBuilder(table);
        builder.add(FIELD_SHOP_ID, appCommandContext.getShopId());
        return builder;
    }

    public static JdbcBuilder _update(String table) {
        return _update(table, null);
    }

    public static JdbcBuilder _update(String table, IAppCommandContext appCommandContext) {
        return new UpdateBuilder(table);
    }

    private static final class UpdateBuilder extends JdbcBuilder {

        protected UpdateBuilder(String table) {
            super(table, "update", JdbcFactory.METHOD_UPDATE);
        }

        @Override
        protected JSONObject getArgsJson() throws JSONException {
            JSONObject json = new JSONObject();

            json.put("update", super.getArgsJson());
            json.put("where", getWhereJson());
            return json;
        }
    }

    private static final class InsertBuilder extends JdbcBuilder {

        protected InsertBuilder(String table) {
            super(table, "insert", JdbcFactory.METHOD_ADD);
        }
    }

    public static final class InsertOrUpdateBuilder extends JdbcBuilder {

        private InsertOrUpdateBuilder(String table) {
            super(table, "replace", "replace");
        }
    }

}
